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

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@HandlesTypes(
{ ServerApplicationConfig.class, ServerEndpoint.class, Endpoint.class })
public class SocketInitializer
    implements ServletContainerInitializer
{

    @Override
    public void onStartup(final Set<Class<?>> _c,
                          final ServletContext _ctx)
        throws ServletException
    {
        final ServerContainer wsContainer = (ServerContainer) _ctx
                        .getAttribute(javax.websocket.server.ServerContainer.class.getName());

        if (wsContainer != null) {
            final String maxIdle = _ctx.getInitParameter("socketMaxIdleTime");
            if (maxIdle != null) {
                wsContainer.setDefaultMaxSessionIdleTimeout(Long.parseLong(maxIdle));
            }

            final String maxTxt = _ctx.getInitParameter("socketMaxTextMessageSize");
            if (maxTxt != null) {
                wsContainer.setDefaultMaxTextMessageBufferSize(Integer.parseInt(maxTxt));
            }

            final String maxBin = _ctx.getInitParameter("socketMaxBinaryMessageSize");
            if (maxBin != null) {
                wsContainer.setDefaultMaxBinaryMessageBufferSize(Integer.parseInt(maxBin));
            }

            final String asyncTime = _ctx.getInitParameter("socketAsyncSendTimeout");
            if (asyncTime != null) {
                wsContainer.setAsyncSendTimeout(Long.parseLong(asyncTime));
            }
        }
    }
}
