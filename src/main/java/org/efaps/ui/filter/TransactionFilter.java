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

package org.efaps.ui.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.efaps.admin.user.UserAttributesSet;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class TransactionFilter
    extends AbstractFilter
{
    /**
     * Name of the session variable for the login forward (after the login is
     * done this is the next page).
     */
    public static final String SESSIONPARAM_LOGIN_FORWARD = "login.forward";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TransactionFilter.class);

    /**
     * Name of the session variable for the login forward (after the login is
     * done this is the next page).
     */
    private static final String SESSION_CONTEXT_ATTRIBUTES = "contextAttributes";

    /**
     * The string is name of the parameter used to define the url login page.
     */
    private static final String INIT_PARAM_URL_LOGIN_PAGE = "urlLoginPage";

    /**
     * All uris which are not needed filtered by security check (password check)
     * are stored in this set variable.
     */
    private final Set<String> exludeUris = new HashSet<String>();

    /**
     * The string is URI to which a forward must be made if the user is not
     * logged in.
     */
    private String notLoggedInForward = null;

    /**
     * Called by the web container to indicate to a filter that it is being
     * placed into service. The servlet container calls the init method exactly
     * once after instantiating the filter. The init method must complete
     * successfully before the filter is asked to do any filtering work. The web
     * container cannot place the filter into service if the init method either
     *
     * <ol>
     * <li>Throws a ServletException</li>
     * <li>Does not return within a time period defined by the web container</li>
     * </ol>
     * @param _filterConfig filterconfig
     * @throws ServletException on error
     */
    @Override
    public void init(final FilterConfig _filterConfig)
        throws ServletException
    {
        super.init(_filterConfig);
        final String root = "/" + _filterConfig.getServletContext().getServletContextName() + "/";

        this.notLoggedInForward = "/" + _filterConfig.getInitParameter(TransactionFilter.INIT_PARAM_URL_LOGIN_PAGE);

        if ((this.notLoggedInForward == null) || (this.notLoggedInForward.length() == 0)) {
            throw new ServletException("Init parameter " + "'" + TransactionFilter.INIT_PARAM_URL_LOGIN_PAGE
                                        + "' not defined");
        }

        this.exludeUris.add((root + this.notLoggedInForward).replaceAll("//+", "/"));
        this.exludeUris.add((root + "/servlet/login").replaceAll("//+", "/"));
    }

    /**
     * @param _request servlet request
     * @param _response servlet response
     * @param _chain filter chain
     * @throws IOException on error
     * @throws ServletException on error
     *
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void doFilter(final HttpServletRequest _request,
                            final HttpServletResponse _response,
                            final FilterChain _chain)
        throws IOException, ServletException
    {

        Context context = null;
        try {
            Locale locale = null;

            locale = _request.getLocale();

            final Map<String, String[]> params = new HashMap<String, String[]>(_request.getParameterMap());

            context = Context.begin(getLoggedInUser(_request), locale, getContextSessionAttributes(_request), params,
                            null, true);

        } catch (final EFapsException e) {
            TransactionFilter.LOG.error("could not initialise the context", e);
            throw new ServletException(e);
        }

        // TODO: is a open sql connection in the context returned automatically?
        try {
            boolean ok = false;
            try {
                _chain.doFilter(_request, _response);
                ok = true;
            } finally {

                if (ok && context.allConnectionClosed() && Context.isTMActive()) {
                    Context.commit();
                } else {
                    if (Context.isTMMarkedRollback()) {
                        TransactionFilter.LOG.error("transaction is marked to roll back");
                        // TODO: throw of Exception is not a good idea... if an
                        // exception is
                        // thrown in
                        // the try code, this exception is overwritten!
                        // throw new
                        // ServletException("transaction in undefined status");
                    } else if (!context.allConnectionClosed()) {
                        TransactionFilter.LOG.error("not all connection to database are closed");
                    } else {
                        TransactionFilter.LOG.error("transaction manager in undefined status");
                    }
                    Context.rollback();
                }
            }
        } catch (final EFapsException e) {
            TransactionFilter.LOG.error("", e);
            throw new ServletException(e);
        }
    }

    /**
     * @param _request http servlet request
     * @return map of session attributes used for the context object
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getContextSessionAttributes(final HttpServletRequest _request)
    {
        Map<String, Object> map = (Map<String, Object>) _request.getSession().getAttribute(
                        TransactionFilter.SESSION_CONTEXT_ATTRIBUTES);

        if (map == null) {
            map = new HashMap<String, Object>();
            _request.getSession().setAttribute(TransactionFilter.SESSION_CONTEXT_ATTRIBUTES, map);
        }
        // add the user attributes to the call
        if (_request.getSession().getAttribute(UserAttributesSet.CONTEXTMAPKEY) != null) {
            map.put(UserAttributesSet.CONTEXTMAPKEY, _request.getSession()
                            .getAttribute(UserAttributesSet.CONTEXTMAPKEY));
        }
        return map;
    }
}
