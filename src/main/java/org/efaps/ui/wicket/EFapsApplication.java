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

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.javascript.DefaultJavaScriptCompressor;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderResponseDecorator;
import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.http.WebRequest;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.ui.filter.AbstractFilter;
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
        final Set<String> temp = new HashSet<String>();
        if (loginRolesTmp != null) {
            final String[] loginRolesAr = loginRolesTmp.split(",");
            for (final String loginRole : loginRolesAr) {
                temp.add(loginRole);
            }
        }
        AppAccessHandler.init(appKey, temp);

        super.init();
        getApplicationSettings().setPageExpiredErrorPage(LoginPage.class);
        getApplicationSettings().setInternalErrorPage(UnexpectedErrorPage.class);

        getApplicationSettings().setUploadProgressUpdatesEnabled(true);

        getDebugSettings().setAjaxDebugModeEnabled(false);
        getDebugSettings().setDevelopmentUtilitiesEnabled(false);

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
        return new EFapsSession(_request);
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
    }
}
