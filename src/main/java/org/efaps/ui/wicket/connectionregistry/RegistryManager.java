/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */
package org.efaps.ui.wicket.connectionregistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Session;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.apache.wicket.protocol.ws.api.registry.IWebSocketConnectionRegistry;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.Context;
import org.efaps.ui.wicket.EFapsApplication;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.InfinispanCache;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.infinispan.Cache;
import org.infinispan.query.CacheQuery;
import org.infinispan.query.ResultIterator;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class RegistryManager.
 *
 * @author The eFaps Team
 */
public final class RegistryManager
{
    /**
     * Name of the Cache for Instances.
     */
    public static final String SESSIONCACHE = RegistryManager.class.getName() + ".Session";

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RegistryManager.class);


    /**
     * Instantiates a new registry manager.
     */
    private RegistryManager()
    {
    }

    /**
     * Register user session.
     *
     * @param _userName the user name
     * @param _sessionId the session ID
     */
    public static void registerUserSession(final String _userName,
                                           final String _sessionId)
    {
        if (EFapsApplication.getMaxInactiveInterval() > 0) {
            getCache().put(_sessionId, new UserSession().setUserName(_userName).setSessionId(_sessionId),
                           EFapsApplication.getMaxInactiveInterval() + 600, TimeUnit.SECONDS);
        } else {
            getCache().put(_sessionId, new UserSession().setUserName(_userName).setSessionId(_sessionId));
        }
        registerLogin4History(_userName, _sessionId);
    }

    /**
     * @param _userName userName
     * @param _sessionId sessionid
     */
    private static void registerLogin4History(final String _userName,
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
        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                        | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOG.error("Error on registering Login", e);
        }
    }


    /**
     * Register activity.
     *
     * @param _session the session
     */
    public static void registerActivity(final EFapsSession _session)
    {
        if (_session.isLogedIn() && getCache().containsKey(_session.getId())) {
            final UserSession userSession = getCache().get(_session.getId());
            if (userSession.isInvalidated()) {
                _session.invalidate();
            } else {
                userSession.registerActivity();
            }
        }
    }

    /**
     * Register keep alive.
     *
     * @param _session the session
     */
    public static void registerKeepAlive(final Session _session)
    {
        // TODO Auto-generated method stub
    }

    /**
     * Adds the msg connection.
     *
     * @param _sessionId the session ID
     * @param _key the key
     */
    public static void addMsgConnection(final String _sessionId,
                                        final IKey _key)
    {
        if (getCache().containsKey(_sessionId)) {
            getCache().get(_sessionId).setConnectionKey(_key);
        }
        RegistryManager.LOG.debug("Added Message Connection for Session: {}", _sessionId);
    }

    /**
     * Invalidate session.
     *
     * @param _sessionId the session id
     */
    public static void invalidateSession(final String _sessionId)
    {
        if (getCache().containsKey(_sessionId)) {
            getCache().get(_sessionId).markInvalid();
        }
    }

    /**
     * Removes the user session.
     *
     * @param _sessionId the session id
     */
    public static void removeUserSession(final String _sessionId)
    {
        if (getCache().containsKey(_sessionId)) {
            registerLogout4History(getCache().get(_sessionId).getUserName(), _sessionId);
            getCache().remove(_sessionId);
        }

    }

    /**
     * @param _userName userName
     * @param _sessionId sessionid
     */
    private static void registerLogout4History(final String _userName,
                                               final String _sessionId)
    {
        boolean contextOpened = false;
        try {
            if (!Context.isThreadActive()) {
                Context.begin(_userName);
                contextOpened = true;
            }
            final Class<?> clazz = Class.forName("org.efaps.esjp.common.history.LogoutHistory", true, EFapsClassLoader
                            .getInstance());
            final Object obj = clazz.newInstance();
            final Method method = clazz.getMethod("register", String.class, String.class, String.class);
            method.invoke(obj, _userName, _sessionId, "N.A.");
        } catch (final ClassNotFoundException | EFapsException | InstantiationException | IllegalAccessException
                        | NoSuchMethodException | SecurityException | IllegalArgumentException
                        | InvocationTargetException e) {
            LOG.error("Error on registering Logout", e);
        } finally {
            if (contextOpened && Context.isThreadActive()) {
                try {
                    Context.commit();
                } catch (final EFapsException e) {
                    LOG.error("Error on registering Logout", e);
                }
            }
        }
    }

    /**
     * Gets the users.
     *
     * @return the users
     */
    public static Set<String> getUsers()
    {
        final Set<String> ret = new HashSet<>();
        for (final UserSession userSession : getCache().values()) {
            ret.add(userSession.getUserName());
        }
        return ret;
    }

    /**
     * Gets the user sessions.
     *
     * @return the user sessions
     */
    public static Collection<UserSession> getUserSessions()
    {
        return Collections.unmodifiableCollection(getCache().values());
    }

    /**
     * Gets the connections 4 user.
     *
     * @param _login the login
     * @return the connections 4 user
     */
    public static List<IWebSocketConnection> getConnections4User(final String _login)
    {
        final List<IWebSocketConnection> ret = new ArrayList<>();
        final SearchManager searchManager = Search.getSearchManager(getCache());
        final QueryBuilder qbldr = searchManager.buildQueryBuilderForClass(UserSession.class).get();
        final CacheQuery query = searchManager.getQuery(qbldr.keyword().onField("userName").matching(_login)
                        .createQuery());
        try (final ResultIterator iter = query.iterator()) {
            while (iter.hasNext()) {
                final UserSession userSession = (UserSession) iter.next();
                if (userSession.getConnectionKey() != null) {
                    final IWebSocketConnectionRegistry registry = WebSocketSettings.Holder.get(EFapsApplication.get())
                                    .getConnectionRegistry();
                    final IWebSocketConnection conn = registry.getConnection(EFapsApplication.get(), userSession
                                    .getSessionId(), userSession.getConnectionKey());
                    if (conn != null) {
                        ret.add(conn);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Gets the connection 4 session.
     *
     * @param _sessionId the session id
     * @return the connection 4 session
     */
    public static IWebSocketConnection getConnection4Session(final String _sessionId)
    {
        IWebSocketConnection ret = null;
        if (getCache().containsKey(_sessionId)) {
            final UserSession userSession = getCache().get(_sessionId);
            if (userSession.getConnectionKey() != null) {
                final IWebSocketConnectionRegistry registry = WebSocketSettings.Holder.get(EFapsApplication.get())
                                .getConnectionRegistry();
                ret = registry.getConnection(EFapsApplication.get(),
                                userSession.getSessionId(), userSession.getConnectionKey());
            }
        }
        return ret;
    }

    /**
     * Gets the cache.
     *
     * @return the cache
     */
    private static Cache<String, UserSession> getCache()
    {
        return InfinispanCache.get().getIgnReCache(SESSIONCACHE);
    }
}
