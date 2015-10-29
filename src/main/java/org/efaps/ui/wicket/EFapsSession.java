/*
 * Copyright 2003 - 2014 The eFaps Team
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.access.AccessCache;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.UserAttributesSet;
import org.efaps.api.background.IExecutionBridge;
import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.ui.wicket.components.IRecent;
import org.efaps.ui.wicket.components.menu.LinkItem;
import org.efaps.ui.wicket.models.EmbeddedLink;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.request.EFapsMultipartRequest;
import org.efaps.ui.wicket.request.EFapsRequest;
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

    /** The execution bridges. */
    private List<IExecutionBridge> executionBridges = Collections.synchronizedList(new ArrayList<IExecutionBridge>());

    /**
     * This instance Map is a Cache for Components, which must be able to be
     * accessed from various PageMaps.
     *
     * @see #getFromCache(String)
     * @see #putIntoCache(String, Component)
     * @see #removeFromCache(String)
     */
    private final Map<String, Component> componentcache = new HashMap<>();

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
     * File to be shown by the ShowFileCallBackBehavior.
     */
    private File file;

    /**
     * Stack that contains the recent visited components.
     */
    private final Stack<IRecent> recentStack = new Stack<IRecent>();

    /**
     * Links that are embeded in html, generated outside this wicket app.
     */
    private final List<EmbeddedLink> embededlinks = new ArrayList<EmbeddedLink>();

    /**
     * Size of the Stack for the recent objects.
     */
    private final int stackSize;

    /**
     * Application Key.
     */
    private final String appKey;

    /**
     * Standard Constructor from Wicket.
     *
     * @param _request Request
     * @param _appKey application key
     * @throws EFapsException
     */
    public EFapsSession(final Request _request,
                        final String _appKey)
    {
        super(_request);
        this.appKey = _appKey;
        this.stackSize = Configuration.getAttributeAsInteger(ConfigAttribute.RECENTCACHESIZE);
    }

    /**
     * Adds the execution bridge.
     *
     * @param _bridge the _bridge
     */
    public synchronized void addExecutionBridge(final IExecutionBridge _bridge) {
        bind();
        this.executionBridges.add(_bridge);
    }

    /**
     * Prune finished tasks.
     */
    public synchronized void  pruneFinishedTasks() {
        final ArrayList<IExecutionBridge> nonFinishedBridges = new ArrayList<>();
        for(final IExecutionBridge bridge: this.executionBridges) {
            if (!bridge.isFinished()) {
                nonFinishedBridges.add(bridge);
            }
        }
        this.executionBridges = nonFinishedBridges;
    }

    /**
     * Gets the tasks page.
     *
     * @param _start the _start
     * @param _size the _size
     * @return the tasks page
     */
    public Iterator<IExecutionBridge> getJobsPage(final int _start,
                                                  final int _size)
    {
        final int min = Math.min(_size, this.executionBridges.size());
        return new ArrayList<IExecutionBridge>(this.executionBridges.subList(_start, min)).iterator();
    }

    /**
     * Gets the bridge for job.
     *
     * @param _jobName the _job name
     * @param _prune the _prune
     * @return the bridge4 job
     */
    public IExecutionBridge getBridge4Job(final String _jobName,
                                          final boolean _prune)
    {
        IExecutionBridge ret = null;
        for (final IExecutionBridge bridge : this.executionBridges) {
            if (bridge.getJobName().equals(_jobName)) {
                ret = bridge;
                if (_prune && ret.isFinished()) {
                    this.executionBridges.remove(ret);
                }
                break;
            }
        }
        return ret;
    }

    /**
     * Count jobs.
     *
     * @return the long
     */
    public long countJobs() {
        return this.executionBridges.size();
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
        if (_recent instanceof LinkItem) {
            final Object object = ((LinkItem) _recent).getDefaultModelObject();
            if (object instanceof UIMenuItem) {
                UsageRegistry.register(((UIMenuItem) object).getKey4UsageRegistry());
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
            getConnectionRegistry().setUser(this.userName, getId());
        } else {
            this.userName = null;
            this.sessionAttributes.clear();
        }
    }

    /**
     * Get the Connection Registry.
     *
     * @return value of instance Connection Registry
     */
    public ConnectionRegistry getConnectionRegistry()
    {
        return EFapsApplication.get().getConnectionRegistry();
    }

    /**
     * Logs a user out and stores the UserAttribues in the eFaps database.
     */
    public final void logout()
    {

        if (this.sessionAttributes.containsKey(UserAttributesSet.CONTEXTMAPKEY)) {
            try {
                UsageRegistry.store();
                ((UserAttributesSet) this.sessionAttributes.get(UserAttributesSet.CONTEXTMAPKEY)).storeInDb();
                AccessCache.clean4Person(Context.getThreadContext().getPersonId());
            } catch (final EFapsException e) {
                EFapsSession.LOG.error("Error on logout", e);
            } finally {
                this.sessionAttributes.clear();
                removeAttribute(EFapsSession.LOGIN_ATTRIBUTE_NAME);
                invalidate();
            }
        }
        closeContext();
        getConnectionRegistry().removeUser(this.userName, getId());
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
                    if (request instanceof EFapsRequest || request instanceof EFapsMultipartRequest) {
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
                                        Context.Inheritance.Inheritable);
                        // set the locale in the context and in the session
                        setLocale(Context.getThreadContext().getLocale());
                        setAttribute(UserAttributesSet.CONTEXTMAPKEY, Context.getThreadContext().getUserAttributes());
                        Context.getThreadContext().setPath(request.getContextPath());
                    }
                }
            } catch (final EFapsException e) {
                EFapsSession.LOG.error("could not initialise the context", e);
                throw new RestartResponseException(new ErrorPage(e));
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
     * Save the Context.
     */
    public void saveContext()
    {
        closeContext();
        openContext();
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
     * @param _embededLink link to add
     */
    public void addEmbededLink(final EmbeddedLink _embededLink)
    {
        this.embededlinks.add(_embededLink);
    }

    /**
     * Getter method for the instance variable {@link #linkElements}.
     *
     * @return value of instance variable {@link #linkElements}
     */
    public List<EmbeddedLink> getEmbededLinks()
    {
        return this.embededlinks;
    }

    @Override
    public void onInvalidate()
    {
        EFapsSession.LOG.trace("Session invalidated: {}", this);
        if (this.userName != null) {
            final EFapsApplication app = (EFapsApplication) Application.get(this.appKey);
            app.getConnectionRegistry().removeUser(this.userName, getId(), app);
        }
        super.onInvalidate();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("userName", this.userName)
                        .append("sessionId", getId()).build();
    }

    /**
     * @return the current EFapsSession
     */
    public static EFapsSession get()
    {
        return (EFapsSession) Session.get();
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
        @Override
        public void close()
        {
            // not needed yet
        }

        /**
         * Method to get the content type of the fileitem.
         *
         * @return content type
         */
        @Override
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
        @Override
        public InputStream getInputStream() throws IOException
        {
            return this.fileItem.getInputStream();
        }

        /**
         * Get the name of the file item.
         *
         * @return name of the file item
         */
        @Override
        public String getName()
        {
            return this.fileItem.getName();
        }

        /**
         * Get the name of the parameter.
         *
         * @return name of the parameter
         */
        @Override
        public String getParameterName()
        {
            return this.parameterName;
        }

        /**
         * Get the size of the file item.
         *
         * @return size in byte
         */
        @Override
        public long getSize()
        {
            return this.fileItem.getSize();
        }
    }
}
