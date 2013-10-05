/*
 * Copyright 2003 - 2013 The eFaps Team
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

import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.IWebSocketConnectionRegistry;
import org.apache.wicket.util.lang.Generics;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ConnectionRegistry
{

    /**
     * MetaDataKey for User to Session Mapping.
     */
    private static final MetaDataKey<ConcurrentMap<String, String>> USER2SESSION =
                    new MetaDataKey<ConcurrentMap<String, String>>()
            {
                private static final long serialVersionUID = 1L;
            };

    /**
     * MetaDataKey for Session to Page Mapping.
     */
    private static final MetaDataKey<ConcurrentMap<String, Integer>> SESSION2PAGEID =
                    new MetaDataKey<ConcurrentMap<String, Integer>>()
            {
                private static final long serialVersionUID = 1L;
            };

    /**
     * @param _userName login of the user
     * @param _sessionId    SessionId assigned
     */
    public void setUser(final String _userName,
                        final String _sessionId)
    {
        ConcurrentMap<String, String> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);

        if (user2session == null) {

            synchronized (ConnectionRegistry.USER2SESSION) {
                user2session = Session.get().getApplication().getMetaData(ConnectionRegistry.USER2SESSION);
                if (user2session == null) {
                    user2session = Generics.newConcurrentHashMap();
                    Session.get().getApplication().setMetaData(ConnectionRegistry.USER2SESSION, user2session);
                }
            }
        }
        user2session.put(_userName, _sessionId);
    }

    /**
     * @param _sessionId    Sessionid the message belongs to
     * @param _pageId       PageId the message belongs to
     */
    public void addMsgConnection(final String _sessionId,
                                 final Integer _pageId)
    {
        ConcurrentMap<String, Integer> session2pageId = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.SESSION2PAGEID);
        if (session2pageId == null) {

            synchronized (ConnectionRegistry.SESSION2PAGEID) {
                session2pageId = Session.get().getApplication().getMetaData(ConnectionRegistry.SESSION2PAGEID);
                if (session2pageId == null) {
                    session2pageId = Generics.newConcurrentHashMap();
                    Session.get().getApplication().setMetaData(ConnectionRegistry.SESSION2PAGEID, session2pageId);
                }
            }
        }
        session2pageId.put(_sessionId, _pageId);

    }

    /**
     * @param _login login of the user to be remved for the registry
     */
    public void removeUser(final String _login)
    {
        ConcurrentMap<String, String> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);

        if (user2session == null) {
            synchronized (ConnectionRegistry.USER2SESSION) {
                user2session = Session.get().getApplication().getMetaData(ConnectionRegistry.USER2SESSION);
                if (user2session != null) {
                    user2session.remove(_login);
                }
            }
        }
    }

    /**
     * @param _login login of the user the session is wanted for
     * @return Connection for the user, <code>null</code> if not found
     */
    public IWebSocketConnection getConnection4User(final String _login)
    {
        IWebSocketConnection ret = null;

        final ConcurrentMap<String, String> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        final ConcurrentMap<String, Integer> sessionId2pageId = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.SESSION2PAGEID);
        final String sessionId = user2session.get(_login);

        if (sessionId != null) {
            final Integer pageId = sessionId2pageId.get(sessionId);
            if (pageId != null) {
                final IWebSocketConnectionRegistry registry = IWebSocketSettings.Holder.get(EFapsApplication.get())
                                .getConnectionRegistry();
                ret = registry.getConnection(EFapsApplication.get(), sessionId, pageId);
            }
        }
        return ret;
    }
}
