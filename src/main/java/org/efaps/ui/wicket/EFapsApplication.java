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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
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
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
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
import org.apache.wicket.resource.DynamicJQueryResourceReference;
import org.apache.wicket.response.filter.IResponseFilter;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.efaps.admin.AppConfigHandler;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.background.IJob;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.ui.filter.AbstractFilter;
import org.efaps.ui.wicket.behaviors.KeepAliveBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior.ACAjaxRequestTarget;
import org.efaps.ui.wicket.pages.error.UnexpectedErrorPage;
import org.efaps.ui.wicket.pages.login.LoginPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.request.EFapsRequest;
import org.efaps.ui.wicket.request.EFapsRequestCycleListener;
import org.efaps.ui.wicket.request.EFapsResourceAggregator;

/**
 * This Class presents the WebApplication for eFaps using the Wicket-Framework. <br/>
 * It is the first class which is instantiated from the WicketServlet. Here the
 * Sessions for each user a created and basic Settings are set.
 *
 * @author Jan Moxter
 * @version $Id$
 */
public class EFapsApplication
    extends WebApplication
{
    /**
     * Registry for the Connections of Users in this application.
     */
    private ConnectionRegistry connectionRegistry;

    /** The executor service. */
    private final ExecutorService executorService =  new ThreadPoolExecutor(10, 10,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new ThreadFactory() {
                        @Override
                        public Thread newThread(final Runnable _r)
                        {
                            final Thread ret = Executors.defaultThreadFactory().newThread(_r);
                            ret.setName("eFaps-Process-" + ret.getId());
                            return ret;
                        }});

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
        final String appKey = getInitParameter(AbstractFilter.INITPARAM_APP_KEY);
        final String loginRolesTmp = getInitParameter(AbstractFilter.INITPARAM_LOGIN_ROLES);
        final Set<UUID> temp = new HashSet<UUID>();
        if (loginRolesTmp != null) {
            final String[] loginRolesAr = loginRolesTmp.split(",");
            for (final String loginRole : loginRolesAr) {
                temp.add(UUID.fromString(loginRole));
            }
        }
        AppAccessHandler.init(appKey, temp);

        final Map<String, String> map = new HashMap<String, String>();
        for (final AppConfigHandler.Parameter param : AppConfigHandler.Parameter.values()) {
            final String configTmp = getInitParameter(param.getKey());
            if (configTmp != null) {
                map.put(param.getKey(), configTmp);
            }
        }
        if (!map.containsKey(AppConfigHandler.Parameter.TEMPFOLDER.getKey())) {
            map.put(AppConfigHandler.Parameter.TEMPFOLDER.getKey(),
                            getStoreSettings().getFileStoreFolder().toURI().toString());
        }
        AppConfigHandler.init(map);

        super.init();

        getJavaScriptLibrarySettings().setJQueryReference(new DynamicJQueryResourceReference());

        getApplicationSettings().setPageExpiredErrorPage(LoginPage.class);
        getApplicationSettings().setInternalErrorPage(UnexpectedErrorPage.class);

        final CompoundClassResolver resolver = new CompoundClassResolver();
        resolver.add(new DefaultClassResolver());
        resolver.add(new AbstractClassResolver() {
            @Override
            public ClassLoader getClassLoader()
            {
                return EFapsClassLoader.getInstance();
            }

        });
        getApplicationSettings().setClassResolver(resolver);

        getApplicationSettings().setUploadProgressUpdatesEnabled(true);

        getDebugSettings().setAjaxDebugModeEnabled(false);
        getDebugSettings().setDevelopmentUtilitiesEnabled(false);

        getStoreSettings().setMaxSizePerSession(Bytes.megabytes(20));
        getStoreSettings().setInmemoryCacheSize(5);

        getMarkupSettings().setStripWicketTags(true);
        getMarkupSettings().setStripComments(true);
        getMarkupSettings().setCompressWhitespace(true);
        getMarkupSettings().setAutomaticLinking(false);

        getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
        getRequestCycleListeners().add(new EFapsRequestCycleListener());
        getRequestLoggerSettings().setRequestLoggerEnabled(false);

        getSecuritySettings().setAuthorizationStrategy(new EFapsFormBasedAuthorizationStartegy());

        getResourceSettings().setJavaScriptCompressor(new DefaultJavaScriptCompressor());

        // allow svg resources
        final IPackageResourceGuard guard = getResourceSettings().getPackageResourceGuard();
        if (guard instanceof SecurePackageResourceGuard) {
            ((SecurePackageResourceGuard) guard).addPattern("+*.svg");
        }

        setHeaderResponseDecorator(new IHeaderResponseDecorator()
        {

            @Override
            public IHeaderResponse decorate(final IHeaderResponse _response)
            {
                return new EFapsResourceAggregator(_response);
            }
        });
        this.connectionRegistry = new ConnectionRegistry();

        getRequestCycleSettings().addResponseFilter(new IResponseFilter()
        {
            @Override
            public AppendingStringBuffer filter(final AppendingStringBuffer _responseBuffer)
            {
                AppendingStringBuffer ret;
                if (RequestCycle.get().getActiveRequestHandler() instanceof ACAjaxRequestTarget) {
                    ret = new AppendingStringBuffer().append(_responseBuffer.subSequence(0,
                                    _responseBuffer.length() - XmlPartialPageUpdate.END_ROOT_ELEMENT.length()));
                } else {
                    ret = _responseBuffer;
                }
                return ret;
            }
        });
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
     * @param _servletRequest   request
     * @param _filterPath       path
     *
     * @return a new WebRequest
     */
    @Override
    public WebRequest newWebRequest(final HttpServletRequest _servletRequest,
                                    final String _filterPath)
    {
        return new EFapsRequest(_servletRequest, _filterPath);
    }

    /**
     * Getter method for the instance variable {@link #connectionRegistry}.
     *
     * @return value of instance variable {@link #connectionRegistry}
     */
    public final ConnectionRegistry getConnectionRegistry()
    {
        return this.connectionRegistry;
    }

    @Override
    public void onEvent(final IEvent<?> _event)
    {
        if (_event.getPayload() instanceof WebSocketTextPayload) {
            final WebSocketTextPayload wsEvent = (WebSocketTextPayload) _event.getPayload();
            if (wsEvent != null) {
                final TextMessage msg = wsEvent.getMessage();
                if (KeepAliveBehavior.MSG.equals(msg.getText())) {
                    getConnectionRegistry().registerKeepAlive(Session.get().getId(), new Date());
                    _event.stop();
                }
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ExecutionBridge launch(final IJob _job) {
        // we are on WEB thread so services should be normally injected.
        final ExecutionBridge bridge = new ExecutionBridge();
        // register bridge on session
        bridge.setJobName("EFapsJob-" + EFapsSession.get().countJobs() + 1 + "-"
                        + RandomStringUtils.randomAlphanumeric(4));
        EFapsSession.get().addExecutionBridge(bridge);
        // run the task
        this.executorService.execute(new JobRunnable(_job, bridge));
        return bridge;
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
                if (((EFapsSession) Session.get()).isLogedIn()
                                || EFapsNoAuthorizationNeededInterface.class.isAssignableFrom(_componentClass)) {
                    ret = true;
                } else {
                    throw new RestartResponseAtInterceptPageException(LoginPage.class);
                }
            }
            return ret;
        }

        @Override
        public boolean isResourceAuthorized(final IResource resource,
                                            final PageParameters parameters)
        {
            return true;
        }
    }
}
