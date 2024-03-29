/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.ws.api.HttpSessionCopy;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.access.user.AccessCache;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.UserAttributesSet;
import org.efaps.api.background.IExecutionBridge;
import org.efaps.api.ui.ILoginProvider;
import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.ui.wicket.components.IRecent;
import org.efaps.ui.wicket.components.menu.LinkItem;
import org.efaps.ui.wicket.connectionregistry.RegistryManager;
import org.efaps.ui.wicket.models.EmbeddedLink;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.info.GatherInfoPage;
import org.efaps.ui.wicket.request.EFapsMultipartRequest;
import org.efaps.ui.wicket.request.EFapsRequest;
import org.efaps.ui.wicket.store.InfinispanPageStore;
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
    private final Map<String, Object> sessionAttributes = new HashMap<>();

    /**
     * File to be shown by the ShowFileCallBackBehavior.
     */
    private File file;

    /**
     * Stack that contains the recent visited components.
     */
    private final Stack<IRecent> recentStack = new Stack<>();

    /**
     * Links that are embeded in html, generated outside this wicket app.
     */
    private final List<EmbeddedLink> embededlinks = new ArrayList<>();

    /**
     * Size of the Stack for the recent objects.
     */
    private final int stackSize;

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
        stackSize = Configuration.getAttributeAsInteger(ConfigAttribute.RECENTCACHESIZE);
    }

    /**
     * Adds the execution bridge.
     *
     * @param _bridge the _bridge
     */
    public synchronized void addExecutionBridge(final IExecutionBridge _bridge)
    {
        bind();
        executionBridges.add(_bridge);
    }

    /**
     * Prune finished tasks.
     */
    public synchronized void  pruneFinishedTasks()
    {
        final ArrayList<IExecutionBridge> nonFinishedBridges = new ArrayList<>();
        for (final IExecutionBridge bridge: executionBridges) {
            if (!bridge.isFinished()) {
                nonFinishedBridges.add(bridge);
            }
        }
        executionBridges = nonFinishedBridges;
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
        final int min = Math.min(_size, executionBridges.size());
        return new ArrayList<>(executionBridges.subList(_start, min)).iterator();
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
        for (final IExecutionBridge bridge : executionBridges) {
            if (bridge.getJobName().equals(_jobName)) {
                ret = bridge;
                if (_prune && ret.isFinished()) {
                    executionBridges.remove(ret);
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
    public long countJobs()
    {
        return executionBridges.size();
    }

    /**
     * @param _recent Recent Object to be pushed on the stack.
     */
    public void addRecent(final IRecent _recent)
    {
        if (stackSize > 0) {
            recentStack.push(_recent);
            if (recentStack.size() > stackSize) {
                recentStack.remove(0);
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
        if (clientInfo == null) {
            final RequestCycle requestCycle = RequestCycle.get();
            clientInfo = new WebClientInfo(requestCycle);
        }
        return (WebClientInfo) clientInfo;
    }

    /**
     * @return the most recent object from the stack, null if stack is empty
     */
    public IRecent getRecent()
    {
        return recentStack.empty() ? null : recentStack.peek();
    }

    /**
     * @return a list with IRecent in the order that a sequential
     * pop on the stack would return
     */
    public List<IRecent> getAllRecents()
    {
        @SuppressWarnings("unchecked")
        final Stack<IRecent> clone = (Stack<IRecent>) recentStack.clone();
        Collections.reverse(clone);
        return clone;
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
        if (userName != null) {
            ret = true;
        } else if (!isSessionInvalidated())  {
            ret = lazyLogin();
        }
        return ret;
    }

    /**
     * Lazy login is used in copmination with a Single Sign On mechanism.
     *
     * @return true, if successful
     */
    private boolean lazyLogin()
    {
        boolean ret = false;
        final HttpServletRequest httpRequest = ((ServletWebRequest) RequestCycle.get().getRequest())
                        .getContainerRequest();
        final HttpSession httpSession = httpRequest.getSession(false);
        if (httpSession != null && !(httpSession instanceof HttpSessionCopy)) {
            for (final ILoginProvider loginProvider : EFapsApplication.get().getLoginProviders()) {
                userName = loginProvider.login(httpSession);
                if (userName != null) {
                    break;
                }
            }
            if (userName != null) {
                openContext();
                try {
                    setAttribute(EFapsSession.LOGIN_ATTRIBUTE_NAME, userName);
                    sessionAttributes.put(UserAttributesSet.CONTEXTMAPKEY, new UserAttributesSet(
                                    userName));
                } catch (final EFapsException e) {
                    EFapsSession.LOG.error("Problems with setting UserAttribues.", e);
                }
                RegistryManager.registerUserSession(userName, getId());
                ret = true;
                RequestCycle.get().setResponsePage(GatherInfoPage.class);
            }
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
            userName = name.toString();
            // on login a valid Context for the User must be opened to ensure that the
            // session attributes that depend on the user are set correctly before any
            // further requests are made (e.g. setting the current company
            openContext();
            setAttribute(EFapsSession.LOGIN_ATTRIBUTE_NAME, userName);
            RegistryManager.registerUserSession(userName, getId());
        } else {
            userName = null;
            sessionAttributes.clear();
        }
    }

    /**
     * Logs a user out and stores the UserAttribues in the eFaps database.
     */
    public final void logout()
    {
        if (sessionAttributes.containsKey(UserAttributesSet.CONTEXTMAPKEY)) {
            try {
                UsageRegistry.store();
                ((UserAttributesSet) sessionAttributes.get(UserAttributesSet.CONTEXTMAPKEY)).storeInDb();
                AccessCache.clean4Person(Context.getThreadContext().getPersonId());
            } catch (final EFapsException e) {
                EFapsSession.LOG.error("Error on logout", e);
            } finally {
                sessionAttributes.clear();
                removeAttribute(EFapsSession.LOGIN_ATTRIBUTE_NAME);
                invalidate();
            }
        }
        closeContext();
        userName = null;
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
            if (Context.isTMActive()) {
                Context.getThreadContext();
            } else {
                Context.begin();
            }
            boolean ok = false;

            try {
                // on a new login the cache for Person is reseted
                Person.reset(_name);
                final EFapsApplication app = (EFapsApplication) getApplication();
                final LoginHandler loginHandler = new LoginHandler(app.getApplicationKey());
                final Person person = loginHandler.checkLogin(_name, _passwd);
                if (person != null && !person.getRoles().isEmpty()) {
                    loginOk = true;
                    sessionAttributes.put(UserAttributesSet.CONTEXTMAPKEY, new UserAttributesSet(_name));
                }
                ok = true;
            } finally {
                if (ok && Context.isTMActive()) {
                    Context.commit();
                } else {
                    if (Context.isTMMarkedRollback()) {
                        EFapsSession.LOG.error("transaction is marked to roll back");
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
                        final Map<String, String[]> parameters = new HashMap<>();
                        final IRequestParameters reqPara = request.getRequestParameters();
                        for (final String name : reqPara.getParameterNames()) {
                            final List<StringValue> values = reqPara.getParameterValues(name);
                            final String[] valArray;
                            if (values == null) {
                                valArray = ArrayUtils.EMPTY_STRING_ARRAY;
                            } else {
                                valArray = new String[values.size()];
                                int i = 0;
                                for (final StringValue value : values) {
                                    valArray[i] = value.toString();
                                    i++;
                                }
                            }
                            parameters.put(name, valArray);
                        }
                        if (Context.isThreadActive()) {
                            Context.getThreadContext().close();
                        }
                        Context.begin(userName, super.getLocale(), sessionAttributes, parameters, null,
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
        file = _file;
    }

    /**
     * Getter method for instance variable {@link #file}.
     *
     * @return value of instance variable {@link #file}
     */
    public File getFile()
    {
        return file;
    }

    /**
     * @param _embededLink link to add
     */
    public void addEmbededLink(final EmbeddedLink _embededLink)
    {
        embededlinks.add(_embededLink);
    }

    /**
     * Getter method for the instance variable {@link #linkElements}.
     *
     * @return value of instance variable {@link #linkElements}
     */
    public List<EmbeddedLink> getEmbededLinks()
    {
        return embededlinks;
    }

    @Override
    public void onInvalidate()
    {
        EFapsSession.LOG.trace("Session invalidated: {}", this);
        RegistryManager.removeUserSession(getId());
        InfinispanPageStore.removePages4Session(getId());
        // invalidation came from other process
        if (userName != null) {
            userName = null;
        }
        super.onInvalidate();
        final RequestCycle cycle = RequestCycle.get();
        if (cycle != null) {
            final HttpServletRequest httpRequest = ((ServletWebRequest) RequestCycle.get().getRequest())
                            .getContainerRequest();
            try {
                httpRequest.logout();
            } catch (final ServletException e) {
                EFapsSession.LOG.error("Catched erroror for logout", e);
            }
            invalidateNow();
        }
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("userName", userName)
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
            parameterName = _parameterName;
            fileItem = _fileItem;
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
            return fileItem.getContentType();
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
            return fileItem.getInputStream();
        }

        /**
         * Get the name of the file item.
         *
         * @return name of the file item
         */
        @Override
        public String getName()
        {
            return fileItem.getName();
        }

        /**
         * Get the name of the parameter.
         *
         * @return name of the parameter
         */
        @Override
        public String getParameterName()
        {
            return parameterName;
        }

        /**
         * Get the size of the file item.
         *
         * @return size in byte
         */
        @Override
        public long getSize()
        {
            return fileItem.getSize();
        }
    }
}
