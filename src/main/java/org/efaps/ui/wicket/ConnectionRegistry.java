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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.util.collections.ConcurrentHashSet;
import org.apache.wicket.util.lang.Generics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ConnectionRegistry
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionRegistry.class);

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
    private static final MetaDataKey<ConcurrentMap<String, IKey>> SESSION2KEY =
                    new MetaDataKey<ConcurrentMap<String, IKey>>()
            {
                private static final long serialVersionUID = 1L;
            };

    /**
     * MetaDataKey for Session that must be invalidated on next request.
     */
    private static final MetaDataKey<ConcurrentHashSet<String>> INVALIDATED =
                    new MetaDataKey<ConcurrentHashSet<String>>()
            {
                private static final long serialVersionUID = 1L;
            };

    /**
     * MetaDataKey for the last Request Time of a Session.
     */
    private static final MetaDataKey<ConcurrentMap<String, Long>> LASTACTIVE =
                    new MetaDataKey<ConcurrentMap<String, Long>>()
            {
                private static final long serialVersionUID = 1L;
            };

    /**
     * @param _sessionID sessionId to be check for invalid
     * @return true if valid, else false
     */
    public boolean sessionValid(final String _sessionID)
    {
        ConcurrentHashSet<String> invalidated = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.INVALIDATED);

        if (invalidated == null) {
            synchronized (ConnectionRegistry.INVALIDATED) {
                invalidated = Session.get().getApplication().getMetaData(ConnectionRegistry.INVALIDATED);
                if (invalidated == null) {
                    invalidated = new ConcurrentHashSet<String>();
                    Session.get().getApplication().setMetaData(ConnectionRegistry.INVALIDATED, invalidated);
                }
            }
        }
        return !invalidated.contains(_sessionID);
    }

    /**
     * @param _sessionID sessionId the Activit will be registered for
     * @param _date date of the activity
     */
    public void registerSessionActivity(final String _sessionID,
                                        final Date _date)
    {
        ConcurrentMap<String, Long> lastactive = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.LASTACTIVE);

        if (lastactive == null) {
            synchronized (ConnectionRegistry.LASTACTIVE) {
                lastactive = Session.get().getApplication().getMetaData(ConnectionRegistry.LASTACTIVE);
                if (lastactive == null) {
                    lastactive = Generics.<String, Long>newConcurrentHashMap();
                    Session.get().getApplication().setMetaData(ConnectionRegistry.LASTACTIVE, lastactive);
                }
            }
        }
        lastactive.put(_sessionID, _date.getTime());
    }

    /**
     * @param _sessionID session to marked as invalid
     */
    public void markSessionAsInvalid(final String _sessionID)
    {
        ConcurrentHashSet<String> invalidated = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.INVALIDATED);

        if (invalidated == null) {
            synchronized (ConnectionRegistry.INVALIDATED) {
                invalidated = Session.get().getApplication().getMetaData(ConnectionRegistry.INVALIDATED);
                if (invalidated == null) {
                    invalidated = new ConcurrentHashSet<String>();
                    Session.get().getApplication().setMetaData(ConnectionRegistry.INVALIDATED, invalidated);
                }
            }
        }
        invalidated.add(_sessionID);
    }

    /**
     * @param _userName login of the user
     * @param _sessionId    SessionId assigned
     */
    protected void setUser(final String _userName,
                           final String _sessionId)
    {
        ConnectionRegistry.LOG.debug("register user: '{}', session: '{}'", _userName, _sessionId);
        ConcurrentMap<String, ConcurrentHashSet<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);

        if (user2session == null) {

            synchronized (ConnectionRegistry.USER2SESSION) {
                user2session = Session.get().getApplication().getMetaData(ConnectionRegistry.USER2SESSION);
                if (user2session == null) {
                    user2session = Generics.<String, ConcurrentHashSet<String>>newConcurrentHashMap();
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
     * @param _key          key the message belongs to
     */
    public void addMsgConnection(final String _sessionId,
                                 final IKey _key)
    {
        ConcurrentMap<String, IKey> session2key = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.SESSION2KEY);
        if (session2key == null) {

            synchronized (ConnectionRegistry.SESSION2KEY) {
                session2key = Session.get().getApplication().getMetaData(ConnectionRegistry.SESSION2KEY);
                if (session2key == null) {
                    session2key = Generics.<String, IKey>newConcurrentHashMap();
                    Session.get().getApplication().setMetaData(ConnectionRegistry.SESSION2KEY, session2key);
                }
            }
        }
        session2key.put(_sessionId, _key);
    }

    /**
     * @param _login        login of the user to be remved for the registry
     * @param _sessionId    id to be removed
     */
    protected void removeUser(final String _login,
                              final String _sessionId)
    {
       removeUser(_login, _sessionId, Session.get().getApplication());
    }

    /**
     * @param _login        login of the user to be remved for the registry
     * @param _sessionId    id to be removed
     * @param _application  Application taht contains the mapping
     */
    protected void removeUser(final String _login,
                              final String _sessionId,
                              final Application _application)
    {
        ConnectionRegistry.LOG.debug("remove user: '{}', session: '{}'", _login, _sessionId);
        ConcurrentMap<String, ConcurrentHashSet<String>> user2session = _application.getMetaData(
                        ConnectionRegistry.USER2SESSION);
        synchronized (ConnectionRegistry.USER2SESSION) {
            user2session = _application.getMetaData(ConnectionRegistry.USER2SESSION);
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
        final ConcurrentMap<String, IKey> sessionId2pageId = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.SESSION2KEY);
        final ConcurrentHashSet<String> sessionIds = user2session.get(_login);

        if (sessionIds != null && !sessionIds.isEmpty()) {
            final Iterator<String> iter = sessionIds.iterator();
            while (iter.hasNext()) {
                final String sessionId = iter.next();
                final IKey key = sessionId2pageId.get(sessionId);
                if (key != null) {
                    final IWebSocketConnectionRegistry registry = IWebSocketSettings.Holder.get(EFapsApplication.get())
                                    .getConnectionRegistry();
                    final IWebSocketConnection conn = registry.getConnection(EFapsApplication.get(), sessionId, key);
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
        final ConcurrentMap<String, IKey> sessionId2key = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.SESSION2KEY);
        final ConcurrentHashSet<String> sessionIds = user2session.get(_login);

        if (sessionIds.contains(_sessionId)) {
            final IKey key = sessionId2key.get(_sessionId);
            if (key != null) {
                final IWebSocketConnectionRegistry registry = IWebSocketSettings.Holder.get(EFapsApplication.get())
                                .getConnectionRegistry();
                ret = registry.getConnection(EFapsApplication.get(), _sessionId, key);
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

    /**
     * @return the map of Session with its last registered activity.
     */
    public Map<String, Long> getSessionsActivity()
    {
        final ConcurrentMap<String, Long> lastactive = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.LASTACTIVE);
        Map<String, Long> ret;
        if (lastactive == null) {
            ret = Collections.unmodifiableMap(Collections.<String, Long>emptyMap());
        } else {
            ret = Collections.unmodifiableMap(lastactive);
        }
        return ret;
    }
}
