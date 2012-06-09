/*
 * Copyright 2003 - 2012 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.upload.FileItem;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.UserAttributesSet;
import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.ui.wicket.behaviors.update.UpdateInterface;
import org.efaps.ui.wicket.components.IRecent;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A WebSession subclass that is used e.g. as a store for behaviors that last a
 * Session and provides functionalities like open/close a Context and
 * login/logout a User.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsSession
    extends WebSession
{
    /**
     * This variable is used as the Key to the UserName stored in the
     * SessionAttributes.
     */
    public static final String LOGIN_ATTRIBUTE_NAME = "org.efaps.ui.wicket.LoginAttributeName";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsSession.class);

    /**
     * This instance Map is a Cache for Components, which must be able to be
     * accessed from various PageMaps.
     *
     * @see #getFromCache(String)
     * @see #putIntoCache(String, Component)
     * @see #removeFromCache(String)
     */
    private final Map<String, Component> componentcache = new HashMap<String, Component>();

    /**
     * This instance variable holds the Name of the logged in user. It is also
     * used to check if a user is logged in, by returning that a user is logged
     * in, if this variable is not null.
     *
     * @see #isLogedIn()
     * @see #checkin()
     * @see #checkout()
     */
    private String userName;

    /**
     * This instance map stores the Attributes which are valid for the whole
     * session. It is passed on to the Context while opening it.
     *
     * @see #openContext()
     */
    private final Map<String, Object> sessionAttributes = new HashMap<String, Object>();

    /**
     * This instance map stores the Behaviors that will be called through the
     * UpdateInterface.
     *
     * @see #addUpdateBehaviors(String, UpdateInterface)
     * @see #getUpdateBehavior(String)
     * @see #getUpdateBehaviors()
     */
    private final Map<String, List<UpdateInterface>> updateBehaviors = new HashMap<String, List<UpdateInterface>>();

    /**
     * File to be shown by the ShowFileCallBackBehavior.
     */
    private File file;

    /**
     * Stack that contains the recent visited components.
     */
    private final Stack<IRecent> recentStack = new Stack<IRecent>();

    /**
     * Size of the Stack for the recent objects.
     */
    private final int stackSize;

    /**
     * Standard Constructor from Wicket.
     *
     * @param _request Request
     * @throws EFapsException
     */
    public EFapsSession(final Request _request)
    {
        super(_request);
        this.stackSize = Configuration.getAttributeAsInteger(ConfigAttribute.RECENTCACHESIZE);
    }

    /**
     * @param _recent Recent Object to be pushed on the stack.
     */
    public void addRecent(final IRecent _recent)
    {
        if (this.stackSize > 0) {
            this.recentStack.push(_recent);
            if (this.recentStack.size() > this.stackSize) {
                this.recentStack.remove(0);
            }
        }
    }


    @Override
    public WebClientInfo getClientInfo()
    {
        if (this.clientInfo == null) {
            final RequestCycle requestCycle = RequestCycle.get();
            this.clientInfo = new WebClientInfo(requestCycle);
        }
        return (WebClientInfo) this.clientInfo;
    }

    /**
     * @return the most recent object from the stack, null if stack is empty
     */
    public IRecent getRecent()
    {
        return this.recentStack.empty() ? null : this.recentStack.peek();
    }

    /**
     * @return a list with IRecent in the order that a sequential
     * pop on the stack would return
     */
    public List<IRecent> getAllRecents()
    {
        @SuppressWarnings("unchecked")
        final Stack<IRecent> clone = (Stack<IRecent>) this.recentStack.clone();
        Collections.reverse(clone);
        return clone;
    }

    /**
     * method to add a Behavior to the {@link #updateBehaviors}. The behavior
     * will only be added if no update behavior with the same Id is existing in
     * the List related to the given oid.
     *
     * @param _oid Oid (used as key in the map)
     * @param _behavior (behavoir to be added)
     */
    public void addUpdateBehaviors(final String _oid,
                                   final UpdateInterface _behavior)
    {
        List<UpdateInterface> behaviors;

        if (this.updateBehaviors.containsKey(_oid)) {
            behaviors = this.updateBehaviors.get(_oid);
            for (int i = 0; i < behaviors.size(); i++) {
                if (behaviors.get(i).getId().equals(_behavior.getId())) {
                    behaviors.remove(i);
                    break;
                }
            }
        } else {
            behaviors = new ArrayList<UpdateInterface>();

        }
        behaviors.add(_behavior);
        this.updateBehaviors.put(_oid, behaviors);
    }

    /**
     * Method that returns the behaviors as aList that rely to a specified oid.
     *
     * @param _oid OID to get the List for
     * @return List with Behaviors
     */
    public List<UpdateInterface> getUpdateBehavior(final String _oid)
    {
        return this.updateBehaviors.get(_oid);
    }

    /**
     * This is the getter method for the instance variable
     * {@link #updateBehaviors}.
     *
     * @return value of instance variable {@link #updateBehaviors}
     */
    public Map<String, List<UpdateInterface>> getUpdateBehaviors()
    {
        return this.updateBehaviors;
    }

    /**
     * This Method stores a Component in the Cache.
     *
     * @param _key Key the Component should be stored in
     * @param _component Component to be stored
     * @see #componentcache
     */
    public void putIntoCache(final String _key,
                             final Component _component)
    {
        this.componentcache.remove(_key);
        this.componentcache.put(_key, _component);
    }

    /**
     * Retrieve a Component from the ComponentCache.
     *
     * @param _key Key of the Component to be retrieved
     * @return Component if found, else null
     * @see #componentcache
     */
    public Component getFromCache(final String _key)
    {
        return this.componentcache.get(_key);
    }

    /**
     * Remove a Component from the ComponentCache.
     *
     * @param _key Key to the Component to be removed
     * @see #componentcache
     */
    public void removeFromCache(final String _key)
    {
        this.componentcache.remove(_key);
    }

    /**
     * Method to check if a user is checked in.
     *
     * @return true if a user is checked in, else false
     * @see #userName
     */
    public boolean isLogedIn()
    {
        boolean ret = false;
        if (this.userName != null) {
            ret = true;
        }
        return ret;

    }

    /**
     * Method to log a user with the Parameters from the Request in.
     *
     * @see #checkLogin(String, String)
     */
    public final void login()
    {
        final IRequestParameters paras = RequestCycle.get().getRequest().getRequestParameters();
        final StringValue name = paras.getParameterValue("name");
        final StringValue pwd = paras.getParameterValue("password");
        if (checkLogin(name.toString(), pwd.toString())) {
            this.userName = name.toString();
            // on login a valid Context for the User must be opened to ensure that the
            // session attributes that depend on the user are set correctly before any
            // further requests are made (e.g. setting the current company
            openContext();
            setAttribute(EFapsSession.LOGIN_ATTRIBUTE_NAME, this.userName);
        } else {
            this.userName = null;
            this.sessionAttributes.clear();
        }
    }

    /**
     * Logs a user out and stores the UserAttribues in the eFaps database.
     */
    public final void logout()
    {
        if (this.sessionAttributes.containsKey(UserAttributesSet.CONTEXTMAPKEY)) {
            try {
                ((UserAttributesSet) this.sessionAttributes.get(UserAttributesSet.CONTEXTMAPKEY)).storeInDb();
            } catch (final EFapsException e) {
                EFapsSession.LOG.error("Error on logout", e);
            } finally {
                this.sessionAttributes.clear();
                removeAttribute(EFapsSession.LOGIN_ATTRIBUTE_NAME);
                invalidate();
            }
        }
        closeContext();
        this.userName = null;
    }

    /**
     * method to check the LoginInformation (Name and Password) against the
     * eFapsDatabase. To check the Information a Context is opened an afterwards
     * closed. It also puts a new Instance of UserAttributes into the instance
     * map {@link #sessionAttributes}. The person returned must have at least one
     * role asigned to be confirmed as value.
     *
     * @param _name Name of the User to be checked in
     * @param _passwd Password of the User to be checked in
     * @return true if LoginInformation was valid, else false
     */
    private boolean checkLogin(final String _name,
                               final String _passwd)
    {

        boolean loginOk = false;
        try {
            Context context = null;

            if (Context.isTMActive()) {
                context = Context.getThreadContext();
            } else {
                context = Context.begin();
            }
            boolean ok = false;

            try {
                // on a new login the cache for Person is reseted
                Person.initialize();
                final EFapsApplication app = (EFapsApplication) getApplication();
                final LoginHandler loginHandler = new LoginHandler(app.getApplicationKey());
                final Person person = loginHandler.checkLogin(_name, _passwd);
                if (person != null && !person.getRoles().isEmpty()) {
                    loginOk = true;
                    this.sessionAttributes.put(UserAttributesSet.CONTEXTMAPKEY, new UserAttributesSet(_name));
                }
                ok = true;
            } finally {
                if (ok && context.allConnectionClosed() && Context.isTMActive()) {
                    Context.commit();
                } else {
                    if (Context.isTMMarkedRollback()) {
                        EFapsSession.LOG.error("transaction is marked to roll back");
                    } else if (!context.allConnectionClosed()) {
                        EFapsSession.LOG.error("not all connection to database are closed");
                    } else {
                        EFapsSession.LOG.error("transaction manager in undefined status");
                    }
                    Context.rollback();
                }
            }
        } catch (final EFapsException e) {
            EFapsSession.LOG.error("could not check name and password", e);
        }
        return loginOk;
    }

    /**
     * Method that opens a new Context in eFaps, setting the User, the Locale,
     * the Attributes of this Session {@link #sessionAttributes} and the
     * RequestParameters for the Context.
     *
     * @see #attach()
     */
    public void openContext()
    {
        if (isLogedIn()) {
            try {
                if (!Context.isTMActive()) {
                    final ServletWebRequest request = (ServletWebRequest) RequestCycle.get().getRequest();

                    final Map<String, String[]> parameters = new HashMap<String, String[]>();
                    final IRequestParameters reqPara = request.getRequestParameters();
                    for (final String name : reqPara.getParameterNames()) {
                        final List<StringValue> values = reqPara.getParameterValues(name);
                        final String[] valArray = new String[values.size()];
                        int i = 0;
                        for (final StringValue value : values) {
                            valArray[i] = value.toString();
                            i++;
                        }
                        parameters.put(name, valArray);
                    }

                    Context.begin(this.userName, super.getLocale(), this.sessionAttributes, parameters, null,
                                    true);
                    // set the locale in the context and in the session
                    setLocale(Context.getThreadContext().getLocale());
                    setAttribute(UserAttributesSet.CONTEXTMAPKEY, Context.getThreadContext().getUserAttributes());
                    Context.getThreadContext().setPath(request.getContextPath());
                }
            } catch (final EFapsException e) {
                EFapsSession.LOG.error("could not initialise the context", e);
                throw new RestartResponseException(new ErrorPage(e));
//            } catch (final FileUploadException e) {
//                EFapsSession.LOG.error("could not initialise the context", e);
//                throw new RestartResponseException(new ErrorPage(e));
            }
        }
    }

    /**
     * Method that closes the opened Context {@link #openContext()}, by
     * committing or rollback it.
     *
     * @see #detach()
     */
    public void closeContext()
    {
        if (isLogedIn()) {
            try {
                if (!Context.isTMNoTransaction()) {
                    if (Context.isTMActive()) {
                        Context.commit();
                    } else {
                        Context.rollback();
                    }
                }
            } catch (final SecurityException e) {
                throw new RestartResponseException(new ErrorPage(e));
            } catch (final IllegalStateException e) {
                throw new RestartResponseException(new ErrorPage(e));
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
        }
    }

    /**
     * @param _file FIle to be used for the ShowFileCallBackBehavior
     */
    public void setFile(final File _file)
    {
        this.file = _file;
    }

    /**
     * Getter method for instance variable {@link #file}.
     *
     * @return value of instance variable {@link #file}
     */
    public File getFile()
    {
        return this.file;
    }

    /**
     * This Class is used to pass the FileItems along with its Parameters to the
     * Context.
     */
    public static class FileParameter implements Context.FileParameter
    {

        /**
         * The FileItem of this FileParameter.
         */
        private final FileItem fileItem;

        /**
         * The Name of the Parameter of the FileParameter.
         */
        private final String parameterName;

        /**
         * Constructor setting the Name of the Parameter and the FileItem.
         *
         * @param _parameterName name of the parameter
         * @param _fileItem file item
         */
        public FileParameter(final String _parameterName, final FileItem _fileItem)
        {
            this.parameterName = _parameterName;
            this.fileItem = _fileItem;
        }

        /**
         * Not needed.
         */
        public void close()
        {
            // not needed yet
        }

        /**
         * Method to get the content type of the fileitem.
         *
         * @return content type
         */
        public String getContentType()
        {
            return this.fileItem.getContentType();
        }

        /**
         * Get the input stream of the fileitem.
         *
         * @return Inputstream
         * @throws IOException on error
         */
        public InputStream getInputStream() throws IOException
        {
            return this.fileItem.getInputStream();
        }

        /**
         * Get the name of the file item.
         *
         * @return name of the file item
         */
        public String getName()
        {
            return this.fileItem.getName();
        }

        /**
         * Get the name of the parameter.
         *
         * @return name of the parameter
         */
        public String getParameterName()
        {
            return this.parameterName;
        }

        /**
         * Get the size of the file item.
         *
         * @return size in byte
         */
        public long getSize()
        {
            return this.fileItem.getSize();
        }
    }
}
