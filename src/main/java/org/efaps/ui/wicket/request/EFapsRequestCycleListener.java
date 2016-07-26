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

package org.efaps.ui.wicket.request;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.session.ISessionStore;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.connectionregistry.RegistryManager;
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
    extends AbstractRequestCycleListener
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EFapsRequestCycleListener.class);

    /**
     * Method to get the EFapsSession.
     *
     * @param _request Request the Session is wanted for
     * @return EFapsSession
     */
    private EFapsSession getEFapsSession(final Request _request)
    {
        final ISessionStore sessionStore = WebApplication.get().getSessionStore();
        final EFapsSession session = (EFapsSession) sessionStore.lookup(_request);
        return session;
    }

    /**
     * Called when the request cycle object is beginning its response.
     *
     * @param _cycle    RequestCycle this Listener belongs to
     */
    @Override
    public void onBeginRequest(final RequestCycle _cycle)
    {
        final EFapsSession session = getEFapsSession(_cycle.getRequest());
        if (session != null) {
            session.openContext();
            RegistryManager.registerActivity(session);
        }
        EFapsRequestCycleListener.LOG.debug("Begin of Request.");
    }

    /**
     * Called when the request cycle object has finished its response.
     *
     * @param _cycle    RequestCycle this Listener belongs to
     */
    @Override
    public void onEndRequest(final RequestCycle _cycle)
    {
        final EFapsSession session = getEFapsSession(_cycle.getRequest());
        if (session != null) {
            session.closeContext();
        }
        EFapsRequestCycleListener.LOG.debug("End of Request.");
    }
}
