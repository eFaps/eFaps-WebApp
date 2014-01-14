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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.IWebSocketConnectionRegistry;
import org.apache.wicket.util.collections.ConcurrentHashSet;
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
    private static final MetaDataKey<ConcurrentMap<String, ConcurrentHashSet<String>>> USER2SESSION =
                    new MetaDataKey<ConcurrentMap<String, ConcurrentHashSet<String>>>()
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
        ConcurrentMap<String, ConcurrentHashSet<String>> user2session = Session.get().getApplication()
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
        ConcurrentHashSet<String> sessions = user2session.get(_userName);
        if (sessions == null) {
            synchronized (ConnectionRegistry.USER2SESSION) {
                sessions = user2session.get(_userName);
                if (sessions == null) {
                    sessions = new ConcurrentHashSet<String>();
                    user2session.put(_userName, sessions);
                }
            }
        }
        sessions.add(_sessionId);
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
     * @param _login        login of the user to be remved for the registry
     * @param _sessionId    id to be removed
     */
    public void removeUser(final String _login,
                           final String _sessionId)
    {
        ConcurrentMap<String, ConcurrentHashSet<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        synchronized (ConnectionRegistry.USER2SESSION) {
            user2session = Session.get().getApplication().getMetaData(ConnectionRegistry.USER2SESSION);
            if (user2session != null) {
                final ConcurrentHashSet<String> sessions = user2session.get(_login);
                sessions.remove(_sessionId);
                if (sessions.isEmpty()) {
                    user2session.remove(_login);
                }
            }
        }
    }

    /**
     * @param _login login of the user the session is wanted for
     * @return Connections for the user, empty list if not found
     */
    public List<IWebSocketConnection> getConnections4User(final String _login)
    {
        final List<IWebSocketConnection> ret = new ArrayList<IWebSocketConnection>();
        final ConcurrentMap<String, ConcurrentHashSet<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        final ConcurrentMap<String, Integer> sessionId2pageId = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.SESSION2PAGEID);
        final ConcurrentHashSet<String> sessionIds = user2session.get(_login);

        if (sessionIds != null && !sessionIds.isEmpty()) {
            final Iterator<String> iter = sessionIds.iterator();
            while (iter.hasNext()) {
                final String sessionId = iter.next();
                final Integer pageId = sessionId2pageId.get(sessionId);
                if (pageId != null) {
                    final IWebSocketConnectionRegistry registry = IWebSocketSettings.Holder.get(EFapsApplication.get())
                                    .getConnectionRegistry();
                    final IWebSocketConnection conn = registry.getConnection(EFapsApplication.get(), sessionId, pageId);
                    if (conn != null) {
                        ret.add(conn);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * @param _login login of the user the session is wanted for
     * @param _sessionId sessionid the connection is wanted for
     * @return Connection for the user, <code>null</code> if not found
     */
    public IWebSocketConnection getConnection4UserSession(final String _login,
                                                          final String _sessionId)
    {
        IWebSocketConnection ret = null;
        final ConcurrentMap<String, ConcurrentHashSet<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        final ConcurrentMap<String, Integer> sessionId2pageId = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.SESSION2PAGEID);
        final ConcurrentHashSet<String> sessionIds = user2session.get(_login);

        if (sessionIds.contains(_sessionId)) {

            final Integer pageId = sessionId2pageId.get(_sessionId);
            if (pageId != null) {
                final IWebSocketConnectionRegistry registry = IWebSocketSettings.Holder.get(EFapsApplication.get())
                                .getConnectionRegistry();
                ret = registry.getConnection(EFapsApplication.get(), _sessionId, pageId);
            }
        }
        return ret;
    }

    /**
     * @param _userName login of the user
     * @return list of sessionIds for the given user
     */
    public List<String> getSession4User(final String _userName)
    {
        List<String> ret = new ArrayList<String>();
        final ConcurrentMap<String, ConcurrentHashSet<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        if (user2session != null) {
            final ConcurrentHashSet<String> sessions = user2session.get(_userName);
            ret = new ArrayList<String>(sessions);
        }
        return ret;
    }


    /**
     * @return list of all registered Users
     */
    public List<String> getUsers()
    {
        List<String> ret = new ArrayList<String>();
        final ConcurrentMap<String, ConcurrentHashSet<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        if (user2session != null) {
            ret = new ArrayList<String>(user2session.keySet());
        }
        return ret;
    }

    /**
     * @return the map of Session currently active
     */
    public Map<String, Set<String>> getSessions4Users()
    {
        final ConcurrentMap<String, ConcurrentHashSet<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        final Map<String, Set<String>> tmpmap = new TreeMap<String, Set<String>>();
        if (user2session != null) {
            for (final Entry<String, ConcurrentHashSet<String>> entry : user2session.entrySet()) {
                tmpmap.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
            }
        }
        return Collections.unmodifiableMap(tmpmap);
    }
}
