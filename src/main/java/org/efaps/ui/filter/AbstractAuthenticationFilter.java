/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.ui.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.efaps.db.Context;
import org.efaps.jaas.LoginHandler;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractAuthenticationFilter
    extends AbstractFilter
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAuthenticationFilter.class);

    /**
     * The string is name of the parameter used to define the application.
     *
     * @see #init
     */
    private static final String INIT_PARAM_APPLICATION = "application";

    /**
     * Login handler used to check the login in method {@link #checkLogin}.
     *
     * @see #init
     * @see #checkLogin
     */
    private LoginHandler loginHandler = null;

    /**
     * Called by the web container to indicate to a filter that it is being
     * placed into service. The servlet container calls the init method exactly
     * once after instantiating the filter. The init method must complete
     * successfully before the filter is asked to do any filtering work.
     *
     * The web container cannot place the filter into service if the init method
     * either 1.Throws a ServletException 2.Does not return within a time period
     * defined by the web container // sets the login handler
     *
     * @param _filterConfig filter configuration instance
     * @see #INIT_PARAM_TITLE
     * @see #title
     * @see #INIT_PARAM_APPLICATION
     * @see #loginhandler
     * @throws ServletException on error
     */
    @Override
    public void init(final FilterConfig _filterConfig)
        throws ServletException
    {
        super.init(_filterConfig);
        final String applInit = _filterConfig.getInitParameter(AbstractAuthenticationFilter.INIT_PARAM_APPLICATION);
        this.loginHandler = new LoginHandler(applInit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy()
    {
        super.destroy();
        this.loginHandler = null;
    }

    /**
     * If the current user is already logged in, nothing is filtered.
     * @param _request      HttpServletRequest
     * @param _response     HttpServletResponse
     * @param _chain        FilterChain
     * @see #doAuthenticate
     * @throws IOException on error
     * @throws ServletException on error
     */
    @Override
    protected void doFilter(final HttpServletRequest _request,
                            final HttpServletResponse _response,
                            final FilterChain _chain)
        throws IOException, ServletException
    {
        if (isLoggedIn(_request)) {
            _chain.doFilter(_request, _response);
        } else {
            doAuthenticate(_request, _response, _chain);
        }
    }

    /**
     * Authentication method.
     * @param _request      HttpServletRequest
     * @param _response     HttpServletResponse
     * @param _chain        FilterChain
     * @see #doAuthenticate
     * @throws IOException on error
     * @throws ServletException on error
     */
    protected abstract void doAuthenticate(final HttpServletRequest _request,
                                           final HttpServletResponse _response,
                                           final FilterChain _chain)
        throws IOException, ServletException;

    /**
     * Checks if the user with given name and password is allowed to login.
     *
     * @param _name user name to check
     * @param _passwd password to check
     * @return <i>true</i> if the user is allowed to login (name and password is
     *         correct), otherweise <i>false</i>
     * @see #loginhandler
     * @see #doFilter
     */
    protected boolean checkLogin(final String _name,
                                 final String _passwd)
    {

        boolean loginOk = false;

        Context context = null;
        try {
            context = Context.begin();

            boolean ok = false;

            try {
                if (this.loginHandler.checkLogin(_name, _passwd) != null) {
                    loginOk = true;
                }
                ok = true;
            } finally {

                if (ok && context.allConnectionClosed() && Context.isTMActive()) {
                    Context.commit();
                } else {
                    if (Context.isTMMarkedRollback()) {
                        AbstractAuthenticationFilter.LOG.error("transaction is marked to roll back");
                    } else if (!context.allConnectionClosed()) {
                        AbstractAuthenticationFilter.LOG.error("not all connection to database are closed");
                    } else {
                        AbstractAuthenticationFilter.LOG.error("transaction manager in undefined status");
                    }
                    Context.rollback();
                }
            }
        } catch (final EFapsException e) {
            AbstractAuthenticationFilter.LOG.error("could not check name and password", e);
        } finally {
            context.close();
        }
        return loginOk;
    }
}
