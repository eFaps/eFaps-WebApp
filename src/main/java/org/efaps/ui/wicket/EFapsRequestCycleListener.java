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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.session.ISessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends the
 * {@link org.apache.wicket.protocol.http.WebRequestCycle} to throw a own
 * ErrorPage and to open/close the Context on begin/end of a Request.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsRequestCycleListener
    implements IRequestCycleListener
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsRequestCycleListener.class);

    /**
     * Map used as a cache.
     */
    private final Map<String, Object> cache = new HashMap<String, Object>();

    /**
     * Method to get the EFapsSession.
     *
     * @return EFapsSession
     */
    private EFapsSession getEFapsSession(final Request _request)
    {
        final ISessionStore sessionStore = WebApplication.get().getSessionStore();
        final EFapsSession session = (EFapsSession) sessionStore.lookup(_request);
        return session;
    }


    /**
     * This Method stores a Component in the Cache.
     *
     * @param _key Key the Component should be stored in
     * @param _object Object to be stored
     * @see #componentcache
     */
    public void putIntoCache(final String _key,
                             final Object _object)
    {
        this.cache.put(_key, _object);
    }

    /**
     * Retrieve a Component from the ComponentCache.
     *
     * @param _key Key of the Component to be retrieved
     * @return Component if found, else null
     * @see #componentcache
     */
    public Object getFromCache(final String _key)
    {
        return this.cache.get(_key);
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.request.cycle.IRequestCycleListener#onBeginRequest(org.apache.wicket.request.cycle.RequestCycle)
     */
    @Override
    public void onBeginRequest(final RequestCycle _cycle)
    {
        final EFapsSession session = getEFapsSession(_cycle.getRequest());
        if (session != null) {
            session.openContext();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.request.cycle.IRequestCycleListener#onEndRequest(org.apache.wicket.request.cycle.RequestCycle)
     */
    @Override
    public void onEndRequest(final RequestCycle _cycle)
    {
        final EFapsSession session = getEFapsSession(_cycle.getRequest());
        if (session != null) {
            session.closeContext();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.request.cycle.IRequestCycleListener#onDetach(org.apache.wicket.request.cycle.RequestCycle)
     */
    @Override
    public void onDetach(final RequestCycle _cycle)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.wicket.request.cycle.IRequestCycleListener#onRequestHandlerResolved(org.apache.wicket.request.cycle.RequestCycle, org.apache.wicket.request.IRequestHandler)
     */
    @Override
    public void onRequestHandlerResolved(final RequestCycle _cycle,
                                         final IRequestHandler _handler)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.wicket.request.cycle.IRequestCycleListener#onRequestHandlerScheduled(org.apache.wicket.request.cycle.RequestCycle, org.apache.wicket.request.IRequestHandler)
     */
    @Override
    public void onRequestHandlerScheduled(final RequestCycle _cycle,
                                          final IRequestHandler _handler)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.wicket.request.cycle.IRequestCycleListener#onException(org.apache.wicket.request.cycle.RequestCycle, java.lang.Exception)
     */
    @Override
    public IRequestHandler onException(final RequestCycle _cycle,
                                       final Exception _exception)
    {
//        final Page ret;
//        if (_exception instanceof AuthorizationException) {
//            ret =  super.onRuntimeException(_page, _exception);
//        } else if (_exception instanceof PageExpiredException || _exception instanceof InvalidUrlException) {
//            final EFapsSession session = (EFapsSession) Session.get();
//            if (session.isTemporary() || !session.isLogedIn()) {
//                // this was an actual session expiry or the user has loged out
//                EFapsWebRequestCycle.LOG.info("session expired and request cannot be honored, "
//                                + "redirected to LoginPage");
//                ret =  new LoginPage();
//            } else {
//                EFapsWebRequestCycle.LOG.error("unable to find page for an active session!");
//                ret = new ErrorPage(_exception);
//            }
//        } else {
//            ret = new ErrorPage(_exception);
//        }
//        return ret;
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.request.cycle.IRequestCycleListener#onExceptionRequestHandlerResolved(org.apache.wicket.request.cycle.RequestCycle, org.apache.wicket.request.IRequestHandler, java.lang.Exception)
     */
    @Override
    public void onExceptionRequestHandlerResolved(final RequestCycle _cycle,
                                                  final IRequestHandler _handler,
                                                  final Exception _exception)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.wicket.request.cycle.IRequestCycleListener#onRequestHandlerExecuted(org.apache.wicket.request.cycle.RequestCycle, org.apache.wicket.request.IRequestHandler)
     */
    @Override
    public void onRequestHandlerExecuted(final RequestCycle _cycle,
                                         final IRequestHandler _handler)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.wicket.request.cycle.IRequestCycleListener#onUrlMapped(org.apache.wicket.request.cycle.RequestCycle, org.apache.wicket.request.IRequestHandler, org.apache.wicket.request.Url)
     */
    @Override
    public void onUrlMapped(final RequestCycle _cycle,
                            final IRequestHandler _handler,
                            final Url _url)
    {
        // TODO Auto-generated method stub

    }
}
