/*
 * Copyright 2003 - 2021 The eFaps Team
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
 */

package org.efaps.ui.wicket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.application.AbstractClassResolver;
import org.apache.wicket.application.CompoundClassResolver;
import org.apache.wicket.application.DefaultClassResolver;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.javascript.DefaultJavaScriptCompressor;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.page.XmlPartialPageUpdate;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.ws.api.event.WebSocketTextPayload;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.efaps.admin.AppConfigHandler;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.background.IJob;
import org.efaps.api.ui.ILoginProvider;
import org.efaps.db.Context;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.ui.filter.AbstractFilter;
import org.efaps.ui.wicket.background.ExecutionBridge;
import org.efaps.ui.wicket.background.JobContext;
import org.efaps.ui.wicket.background.JobRunnable;
import org.efaps.ui.wicket.behaviors.KeepAliveBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior.ACAjaxRequestTarget;
import org.efaps.ui.wicket.connectionregistry.RegistryManager;
import org.efaps.ui.wicket.pages.error.UnexpectedErrorPage;
import org.efaps.ui.wicket.pages.login.LoginPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.pages.pivot.JsonResponsePage;
import org.efaps.ui.wicket.request.EFapsRequest;
import org.efaps.ui.wicket.request.EFapsRequestCycleListener;
import org.efaps.ui.wicket.request.EFapsResourceAggregator;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;
import org.efaps.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class presents the WebApplication for eFaps using the Wicket-Framework.
 * <br/>
 * It is the first class which is instantiated from the WicketServlet. Here the
 * Sessions for each user a created and basic Settings are set.
 *
 * @author The eFaps Team
 */
public class EFapsApplication
    extends WebApplication
{

    /** The max inactive interval. */
    private static int MAXINACTIVEINTERVAL = 0;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsApplication.class);

    /** The executor service. */
    private final ExecutorService executorService = new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(), (ThreadFactory) _r -> {
                        final Thread ret = Executors.defaultThreadFactory().newThread(_r);
                        ret.setName("eFaps-Process-" + ret.getId());
                        ret.setContextClassLoader(EFapsClassLoader.getInstance());
                        ret.setUncaughtExceptionHandler((_thread,
                                                         _throwable) -> {
                            EFapsApplication.LOG.error("Caught error from Thread '{}'", _thread.getName());
                            EFapsApplication.LOG.error("->", _throwable);
                        });
                        return ret;
                    });

    /** The loginProviders. */
    private final List<ILoginProvider> loginProviders = new ArrayList<>();

    /**
     * @see org.apache.wicket.Application#getHomePage()
     * @return Class of the main page
     */
    @Override
    public Class<MainPage> getHomePage()
    {
        return MainPage.class;
    }

    /**
     * @see org.apache.wicket.protocol.http.WebApplication#init()
     */
    @Override
    protected void init()
    {
        super.init();

        final String appKey = getInitParameter(AbstractFilter.INITPARAM_APP_KEY);
        final String loginRolesTmp = getInitParameter(AbstractFilter.INITPARAM_LOGIN_ROLES);
        final Set<UUID> temp = new HashSet<>();
        if (loginRolesTmp != null) {
            final String[] loginRolesAr = loginRolesTmp.split(",");
            for (final String loginRole : loginRolesAr) {
                temp.add(UUID.fromString(loginRole));
            }
        }
        AppAccessHandler.init(appKey, temp);

        final Map<String, String> map = new HashMap<>();
        for (final AppConfigHandler.Parameter param : AppConfigHandler.Parameter.values()) {
            final String configTmp = getInitParameter(param.getKey());
            if (configTmp != null) {
                map.put(param.getKey(), configTmp);
            }
        }
        if (!map.containsKey(AppConfigHandler.Parameter.TEMPFOLDER.getKey())) {
            map.put(AppConfigHandler.Parameter.TEMPFOLDER.getKey(), getStoreSettings().getFileStoreFolder().toURI()
                            .toString());
        }
        AppConfigHandler.init(map);

       // getJavaScriptLibrarySettings().setJQueryReference(JQueryResourceReference.getV3());

        getApplicationSettings().setPageExpiredErrorPage(MainPage.class);
        getApplicationSettings().setInternalErrorPage(UnexpectedErrorPage.class);

        final CompoundClassResolver resolver = new CompoundClassResolver();
        resolver.add(new DefaultClassResolver());
        resolver.add(new AbstractClassResolver()
        {

            @Override
            public ClassLoader getClassLoader()
            {
                return EFapsClassLoader.getInstance();
            }

        });
        getApplicationSettings().setClassResolver(resolver);

        getApplicationSettings().setUploadProgressUpdatesEnabled(true);

        getDebugSettings().setAjaxDebugModeEnabled(true);
        getDebugSettings().setDevelopmentUtilitiesEnabled(false);

        setPageManagerProvider(new EFapsPageManagerProvider(this));

        getStoreSettings().setMaxSizePerSession(Bytes.megabytes(Configuration.getAttributeAsInteger(
                        ConfigAttribute.STORE_MAXSIZEPERSESSION)));

        getMarkupSettings().setStripWicketTags(true);
        getMarkupSettings().setStripComments(true);
        getMarkupSettings().setCompressWhitespace(true);
        getMarkupSettings().setAutomaticLinking(false);

        getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
        getRequestCycleListeners().add(new EFapsRequestCycleListener());
        getRequestLoggerSettings().setRequestLoggerEnabled(false);

        getCspSettings().blocking().disabled();

        getSecuritySettings().setAuthorizationStrategy(new EFapsFormBasedAuthorizationStartegy());

        getResourceSettings().setJavaScriptCompressor(new DefaultJavaScriptCompressor());

        // allow svg resources
        final IPackageResourceGuard guard = getResourceSettings().getPackageResourceGuard();
        if (guard instanceof SecurePackageResourceGuard) {
            ((SecurePackageResourceGuard) guard).addPattern("+*.svg");
            ((SecurePackageResourceGuard) guard).addPattern("+*.json");
        }

        getHeaderResponseDecorators().add(_response -> new EFapsResourceAggregator(_response));
        getRequestCycleSettings().addResponseFilter(_responseBuffer -> {
            final AppendingStringBuffer ret;
            if (RequestCycle.get().getActiveRequestHandler() instanceof ACAjaxRequestTarget) {
                ret = new AppendingStringBuffer().append(_responseBuffer.subSequence(0, _responseBuffer.length()
                                - XmlPartialPageUpdate.END_ROOT_ELEMENT.length()));
            } else {
                ret = _responseBuffer;
            }
            return ret;
        });

        final ServiceLoader<ILoginProvider> serviceLoaderLogins = ServiceLoader.load(ILoginProvider.class);
        for (final ILoginProvider loginProvider : serviceLoaderLogins) {
            LOG.info("[{}] registered: {}", getName(), loginProvider);
            loginProviders.add(loginProvider);
        }
        mountPage("/" + RandomUtil.randomAlphabetic(16), JsonResponsePage.class);
    }

    /**
     * @see org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.wicket.Request,
     *      org.apache.wicket.Response)
     * @param _request the request
     * @param _response the response
     * @return a new Session for the request
     */
    @Override
    public Session newSession(final Request _request,
                              final Response _response)
    {
        return new EFapsSession(_request, getApplicationKey());
    }

    /**
     * @param _servletRequest request
     * @param _filterPath path
     *
     * @return a new WebRequest
     */
    @Override
    public WebRequest newWebRequest(final HttpServletRequest _servletRequest,
                                    final String _filterPath)
    {
        if (EFapsApplication.MAXINACTIVEINTERVAL == 0) {
            final int interval = _servletRequest.getSession().getMaxInactiveInterval();
            if (interval == 0) {
                EFapsApplication.MAXINACTIVEINTERVAL = -1;
            } else {
                EFapsApplication.MAXINACTIVEINTERVAL = interval;
            }
        }
        return new EFapsRequest(_servletRequest, _filterPath);
    }

    @Override
    public void onEvent(final IEvent<?> _event)
    {
        if (_event.getPayload() instanceof WebSocketTextPayload) {
            final WebSocketTextPayload wsEvent = (WebSocketTextPayload) _event.getPayload();
            if (wsEvent != null) {
                final TextMessage msg = wsEvent.getMessage();
                if (KeepAliveBehavior.MSG.equals(msg.getText())) {
                    RegistryManager.registerKeepAlive(Session.get());
                    _event.stop();
                }
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launch a job.
     *
     * @param _job the _job
     * @param _jobName the _job name
     * @return the execution bridge
     * @throws EFapsException on error
     */
    public ExecutionBridge launch(final IJob _job,
                                  final String _jobName)
        throws EFapsException
    {
        // we are on WEB thread so services should be normally injected.
        final ExecutionBridge bridge = new ExecutionBridge();
        // register bridge on session
        if (_jobName == null) {
            bridge.setJobName("EFapsJob-" + EFapsSession.get().countJobs() + 1 + "-"
                            + RandomUtil.randomAlphanumeric(4));
        } else {
            bridge.setJobName(_jobName);
        }
        bridge.setJobContext(new JobContext().setUserName(Context.getThreadContext().getPerson().getName()).setLocale(
                        Context.getThreadContext().getLocale()).setCompanyUUID(Context.getThreadContext().getCompany()
                                        .getUUID()));

        EFapsSession.get().addExecutionBridge(bridge);
        // run the task
        executorService.execute(new JobRunnable(_job, bridge));
        return bridge;
    }

    /**
     * Gets the loginProviders.
     *
     * @return the loginProviders
     */
    public List<ILoginProvider> getLoginProviders()
    {
        return Collections.unmodifiableList(loginProviders);
    }

    /**
     * Get EFapsApplication for current thread.
     *
     * @return The current thread's Application
     */
    public static EFapsApplication get()
    {
        return (EFapsApplication) Application.get();
    }

    /**
     * Gets the max inactive interval.
     *
     * @return the max inactive interval
     */
    public static int getMaxInactiveInterval()
    {
        return EFapsApplication.MAXINACTIVEINTERVAL < 0 ? 0 : EFapsApplication.MAXINACTIVEINTERVAL;
    }

    /**
     * The Class presents the Strategy to authorize pages in this
     * WebApplication.
     */
    private class EFapsFormBasedAuthorizationStartegy
        implements IAuthorizationStrategy
    {

        /**
         * Wicket has got the possibility to check for specific actions like
         * render or enable if this given action is authorized. eFaps does not
         * use this check and returns always true.
         *
         * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
         *      org.apache.wicket.authorization.Action)
         * @param _component Component to be checked
         * @param _action action to be checked
         * @return true
         */
        @Override
        public boolean isActionAuthorized(final Component _component,
                                          final Action _action)
        {
            return true;
        }

        /**
         * For all Pages it will be checked if a User is logged in or if the
         * Page implements the EFapsNoAuthendPageInterface, if non of both we
         * will redirect to the LoginPage.
         *
         * @param _componentClass class to be checked
         * @param <T> IRequestableComponent to be checked
         * @return true, if checks if is instantiation authorized
         */
        @Override
        public <T extends IRequestableComponent> boolean isInstantiationAuthorized(final Class<T> _componentClass)
        {
            boolean ret = true;
            if (WebPage.class.isAssignableFrom(_componentClass)) {
                if (((EFapsSession) Session.get()).isLogedIn() || EFapsNoAuthorizationNeededInterface.class
                                .isAssignableFrom(_componentClass)) {
                    ret = true;
                } else {
                    throw new RestartResponseAtInterceptPageException(LoginPage.class);
                }
            }
            return ret;
        }

        @Override
        public boolean isResourceAuthorized(final IResource _resource,
                                            final PageParameters _parameters)
        {
            return true;
        }
    }
}
