/*
 * Copyright 2003 - 2021 The eFaps Team
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.lang.Generics;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.Context;
import org.efaps.ui.wicket.behaviors.KeepAliveBehavior;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;
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
    private static final MetaDataKey<ConcurrentMap<String, Set<String>>> USER2SESSION =
                    new MetaDataKey<ConcurrentMap<String, Set<String>>>()
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
    private static final MetaDataKey<Set<String>> INVALIDATED =
                    new MetaDataKey<Set<String>>()
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
     * MetaDataKey for the last Request Time of a Session.
     */
    private static final MetaDataKey<ConcurrentMap<String, Long>> KEEPALIVE =
                    new MetaDataKey<ConcurrentMap<String, Long>>()
            {
                private static final long serialVersionUID = 1L;
            };

    /**
     * Used to store if the KeepAlive threat was started.
     */
    private boolean keepAlive = false;

    /**
     * @param _sessionID sessionId to be check for invalid
     * @return true if valid, else false
     */
    public boolean sessionValid(final String _sessionID)
    {
        Set<String> invalidated = Session.get().getApplication().getMetaData(ConnectionRegistry.INVALIDATED);

        if (invalidated == null) {
            synchronized (ConnectionRegistry.INVALIDATED) {
                invalidated = Session.get().getApplication().getMetaData(ConnectionRegistry.INVALIDATED);
                if (invalidated == null) {
                    invalidated = ConcurrentHashMap.newKeySet();
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
        ConnectionRegistry.LOG.debug("Register Activity for Session: {}", _sessionID);
    }

    /**
     * @param _sessionID    id of the session the keep alive belongs to
     * @param _date         time to register
     */
    public void registerKeepAlive(final String _sessionID,
                                  final Date _date)
    {
        ConcurrentMap<String, Long> keepalive = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.KEEPALIVE);

        if (keepalive == null) {
            synchronized (ConnectionRegistry.KEEPALIVE) {
                keepalive = Session.get().getApplication().getMetaData(ConnectionRegistry.KEEPALIVE);
                if (keepalive == null) {
                    keepalive = Generics.<String, Long>newConcurrentHashMap();
                    Session.get().getApplication().setMetaData(ConnectionRegistry.KEEPALIVE, keepalive);
                }
            }
        }
        keepalive.put(_sessionID, _date.getTime());
        ConnectionRegistry.LOG.debug("Register KeepAlive for Session: {}", _sessionID);
    }

    /**
     * @param _sessionID session to marked as invalid
     */
    public void markSessionAsInvalid(final String _sessionID)
    {
        Set<String> invalidated = Session.get().getApplication().getMetaData(ConnectionRegistry.INVALIDATED);

        if (invalidated == null) {
            synchronized (ConnectionRegistry.INVALIDATED) {
                invalidated = Session.get().getApplication().getMetaData(ConnectionRegistry.INVALIDATED);
                if (invalidated == null) {
                    invalidated = ConcurrentHashMap.newKeySet();
                    Session.get().getApplication().setMetaData(ConnectionRegistry.INVALIDATED, invalidated);
                }
            }
        }
        invalidated.add(_sessionID);
        ConnectionRegistry.LOG.debug("Marked Session: {} as invalid", _sessionID);
    }

    /**
     * @param _userName userName
     * @param _sessionId sessionid
     */
    private void registerLogin4History(final String _userName,
                                       final String _sessionId)
    {
        try {
            final WebRequest req = (WebRequest) RequestCycle.get().getRequest();
            final HttpServletRequest httpReq = (HttpServletRequest) req.getContainerRequest();
            String ipAddress = httpReq.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = httpReq.getRemoteAddr();
            }

            final Class<?> clazz = Class.forName("org.efaps.esjp.common.history.LoginHistory", true,
                            EFapsClassLoader.getInstance());
            final Object obj = clazz.newInstance();
            final Method method = clazz.getMethod("register", String.class, String.class, String.class);
            method.invoke(obj, _userName, _sessionId, ipAddress);
        } catch (final ClassNotFoundException e) {
            ConnectionRegistry.LOG.error("Error on registering Login", e);
        } catch (final NoSuchMethodException e) {
            ConnectionRegistry.LOG.error("Error on registering Login", e);
        } catch (final SecurityException e) {
            ConnectionRegistry.LOG.error("Error on registering Login", e);
        } catch (final IllegalAccessException e) {
            ConnectionRegistry.LOG.error("Error on registering Login", e);
        } catch (final IllegalArgumentException e) {
            ConnectionRegistry.LOG.error("Error on registering Login", e);
        } catch (final InvocationTargetException e) {
            ConnectionRegistry.LOG.error("Error on registering Login", e);
        } catch (final InstantiationException e) {
            ConnectionRegistry.LOG.error("Error on registering Login", e);
        }
    }

    /**
     * @param _userName userName
     * @param _sessionId sessionid
     */
    private void registerLogout4History(final String _userName,
                                        final String _sessionId)
    {
        boolean contextOpened = false;
        try {
            if (!Context.isThreadActive()) {
                Context.begin(_userName);
                contextOpened = true;
            }
            final Class<?> clazz = Class.forName("org.efaps.esjp.common.history.LogoutHistory", true,
                            EFapsClassLoader.getInstance());
            final Object obj = clazz.newInstance();
            final Method method = clazz.getMethod("register", String.class, String.class, String.class);
            method.invoke(obj, _userName, _sessionId, "N.A.");
        } catch (final ClassNotFoundException e) {
            ConnectionRegistry.LOG.error("Error on registering Logout", e);
        } catch (final NoSuchMethodException e) {
            ConnectionRegistry.LOG.error("Error on registering Logout", e);
        } catch (final SecurityException e) {
            ConnectionRegistry.LOG.error("Error on registering Logout", e);
        } catch (final IllegalAccessException e) {
            ConnectionRegistry.LOG.error("Error on registering Logout", e);
        } catch (final IllegalArgumentException e) {
            ConnectionRegistry.LOG.error("Error on registering Logout", e);
        } catch (final InvocationTargetException e) {
            ConnectionRegistry.LOG.error("Error on registering Logout", e);
        } catch (final InstantiationException e) {
            ConnectionRegistry.LOG.error("Error on registering Logout", e);
        } catch (final EFapsException e) {
            ConnectionRegistry.LOG.error("Error on registering Logout", e);
        } finally {
            if (contextOpened && Context.isThreadActive()) {
                try {
                    Context.commit();
                } catch (final EFapsException e) {
                    ConnectionRegistry.LOG.error("Error on registering Logout", e);
                }
            }
        }
    }


    /**
     * @param _userName login of the user
     * @param _sessionID    SessionId assigned
     */
    protected void setUser(final String _userName,
                           final String _sessionID)
    {
        ConnectionRegistry.LOG.debug("register user: '{}', session: '{}'", _userName, _sessionID);
        ConcurrentMap<String, Set<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);

        if (user2session == null) {

            synchronized (ConnectionRegistry.USER2SESSION) {
                user2session = Session.get().getApplication().getMetaData(ConnectionRegistry.USER2SESSION);
                if (user2session == null) {
                    user2session = Generics.<String, Set<String>>newConcurrentHashMap();
                    Session.get().getApplication().setMetaData(ConnectionRegistry.USER2SESSION, user2session);
                }
            }
        }
        Set<String> sessions = user2session.get(_userName);
        if (sessions == null) {
            synchronized (ConnectionRegistry.USER2SESSION) {
                sessions = user2session.get(_userName);
                if (sessions == null) {
                    sessions = ConcurrentHashMap.newKeySet();
                    user2session.put(_userName, sessions);
                }
            }
        }
        sessions.add(_sessionID);
        ConnectionRegistry.LOG.debug("Added User '{}' for Session: {}", _userName, _sessionID);
        registerLogin4History(_userName, _sessionID);
    }

    /**
     * @param _sessionID    Sessionid the message belongs to
     * @param _key          key the message belongs to
     */
    public void addMsgConnection(final String _sessionID,
                                 final IKey _key)
    {
        initKeepAlive();
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
        session2key.put(_sessionID, _key);
        ConnectionRegistry.LOG.debug("Added Message Connection for Session: {}", _sessionID);
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
     * @param _login        login of the user to be removed for the registry
     * @param _sessionID    id to be removed
     * @param _application  Application that contains the mapping
     */
    protected void removeUser(final String _login,
                              final String _sessionID,
                              final Application _application)
    {
        if (_login != null) {
            ConnectionRegistry.LOG.debug("remove user: '{}', session: '{}'", _login, _sessionID);
            ConcurrentMap<String, Set<String>> user2session = _application.getMetaData(
                            ConnectionRegistry.USER2SESSION);
            synchronized (ConnectionRegistry.USER2SESSION) {
                user2session = _application.getMetaData(ConnectionRegistry.USER2SESSION);
                if (user2session != null) {
                    final Set<String> sessions = user2session.get(_login);
                    sessions.remove(_sessionID);
                    if (sessions.isEmpty()) {
                        user2session.remove(_login);
                    }
                }
            }
            synchronized (ConnectionRegistry.KEEPALIVE) {
                final ConcurrentMap<String, Long> keepalive = _application.getMetaData(ConnectionRegistry.KEEPALIVE);
                if (keepalive != null) {
                    keepalive.remove(_sessionID);
                }
            }
            ConnectionRegistry.LOG.debug("Removed User '{}' for Session: {}", _login, _sessionID);
            registerLogout4History(_login, _sessionID);
        }
    }

    /**
     * @param _login login of the user the session is wanted for
     * @return Connections for the user, empty list if not found
     */
    public List<IWebSocketConnection> getConnections4User(final String _login)
    {
        final List<IWebSocketConnection> ret = new ArrayList<>();
        final ConcurrentMap<String, Set<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        final ConcurrentMap<String, IKey> sessionId2pageId = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.SESSION2KEY);
        final Set<String> sessionIds = user2session.get(_login);

        if (sessionIds != null && !sessionIds.isEmpty()) {
            final Iterator<String> iter = sessionIds.iterator();
            while (iter.hasNext()) {
                final String sessionId = iter.next();
                final IKey key = sessionId2pageId.get(sessionId);
                if (key != null) {
                    final IWebSocketConnectionRegistry registry = WebSocketSettings.Holder.get(EFapsApplication.get())
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
        final ConcurrentMap<String, Set<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        final ConcurrentMap<String, IKey> sessionId2key = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.SESSION2KEY);
        final Set<String> sessionIds = user2session.get(_login);

        if (sessionIds.contains(_sessionId)) {
            final IKey key = sessionId2key.get(_sessionId);
            if (key != null) {
                final IWebSocketConnectionRegistry registry = WebSocketSettings.Holder.get(EFapsApplication.get())
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
        List<String> ret = new ArrayList<>();
        final ConcurrentMap<String, Set<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        if (user2session != null) {
            final Set<String> sessions = user2session.get(_userName);
            ret = new ArrayList<>(sessions);
        }
        return ret;
    }

    /**
     * @return list of all registered Users
     */
    public List<String> getUsers()
    {
        List<String> ret = new ArrayList<>();
        final ConcurrentMap<String, Set<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        if (user2session != null) {
            ret = new ArrayList<>(user2session.keySet());
        }
        return ret;
    }

    /**
     * @return the map of Session currently active
     */
    public Map<String, Set<String>> getSessions4Users()
    {
        final ConcurrentMap<String, Set<String>> user2session = Session.get().getApplication()
                        .getMetaData(ConnectionRegistry.USER2SESSION);
        final Map<String, Set<String>> tmpmap = new TreeMap<>();
        if (user2session != null) {
            for (final Entry<String, Set<String>> entry : user2session.entrySet()) {
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
        final Map<String, Long> ret;
        if (lastactive == null) {
            ret = Collections.unmodifiableMap(Collections.<String, Long>emptyMap());
        } else {
            ret = Collections.unmodifiableMap(lastactive);
        }
        return ret;
    }

    /**
     * Send the KeepAlive.
     * @param _application Application the KeepAlive will be send for
     */
    public void sendKeepAlive(final Application _application)
    {
        final long reference = new Date().getTime();
        final ConcurrentMap<String, IKey> sessionId2key = _application.getMetaData(ConnectionRegistry.SESSION2KEY);
        final ConcurrentMap<String, Long> keepalive = _application.getMetaData(ConnectionRegistry.KEEPALIVE);
        if (keepalive != null) {
            for (final Entry<String, Long> entry : keepalive.entrySet()) {
                if (reference - entry.getValue()
                                > Configuration.getAttributeAsInteger(ConfigAttribute.WEBSOCKET_KATH) * 1000) {
                    final IKey key = sessionId2key.get(entry.getKey());
                    if (key != null) {
                        final IWebSocketConnectionRegistry registry = WebSocketSettings.Holder.get(
                                        _application).getConnectionRegistry();
                        final IWebSocketConnection conn = registry.getConnection(_application, entry.getKey(), key);
                        if (conn != null) {
                            try {
                                conn.sendMessage(KeepAliveBehavior.MSG);
                                ConnectionRegistry.LOG.debug("Send KeepAlive for Session: {}", entry.getKey());
                            } catch (final IOException e) {
                                ConnectionRegistry.LOG.error("Catched error", e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Init the KeepAlive mechanism.
     */
    private void initKeepAlive()
    {
        if (!keepAlive) {
            keepAlive = true;
            final KeepAliveTask keepAliveTask = new KeepAliveTask(EFapsApplication.get().getApplicationKey());
            final Timer timer = new Timer(true);
            // every two minutes
            timer.scheduleAtFixedRate(keepAliveTask, 0 * 1000,
                            Configuration.getAttributeAsInteger(ConfigAttribute.WEBSOCKET_KASP) * 1000);
        }
    }

    /**
     * task to send the keep alive messages.
     */
    private class KeepAliveTask
        extends TimerTask
    {
        /**
         * Key to the application this task belong to.
         */
        private final String applicationKey;

        /**
         * @param _applicationKey key to the application this task belong to
         */
        KeepAliveTask(final String _applicationKey)
        {
            applicationKey = _applicationKey;
        }

        @Override
        public void run()
        {
            Application.get(applicationKey);

        }
    }
}
