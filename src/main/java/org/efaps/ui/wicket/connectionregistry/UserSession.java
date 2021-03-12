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
 */
package org.efaps.ui.wicket.connectionregistry;

import java.util.Date;

import org.apache.wicket.protocol.ws.api.registry.IKey;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
/**
 * The Class UserSession.
 *
 * @author The eFaps Team
 */
@Indexed
public class UserSession
{

    /** The user name. */
    @KeywordField
    private String userName;

    /** The session id. */
    private String sessionId;

    /** The last activity. */
    private long lastActivity;

    /** The invalidated. */
    private boolean invalidated;

    /** The connection key. */
    private IKey connectionKey;

    /**
     * Instantiates a new user session.
     */
    public UserSession()
    {
        lastActivity = new Date().getTime();
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Sets the user name.
     *
     * @param _userName the user name
     * @return the user session
     */
    public UserSession setUserName(final String _userName)
    {
        userName = _userName;
        return this;
    }

    /**
     * Gets the session id.
     *
     * @return the session id
     */
    public String getSessionId()
    {
        return sessionId;
    }

    /**
     * Sets the session id.
     *
     * @param _sessionId the session id
     * @return the user session
     */
    public UserSession setSessionId(final String _sessionId)
    {
        sessionId = _sessionId;
        return this;
    }

    /**
     * Register activity.
     *
     * @return the user session
     */
    public UserSession registerActivity()
    {
        lastActivity = new Date().getTime();
        return this;
    }

    /**
     * Gets the last activity.
     *
     * @return the last activity
     */
    public long getLastActivity()
    {
        return lastActivity;
    }

    /**
     * Checks if is invalidated.
     *
     * @return the invalidated
     */
    public boolean isInvalidated()
    {
        return invalidated;
    }

    /**
     * Mark invalid.
     *
     * @return the user session
     */
    public UserSession markInvalid()
    {
        invalidated = true;
        return this;
    }

    /**
     * Gets the connection key.
     *
     * @return the connection key
     */
    public IKey getConnectionKey()
    {
        return connectionKey;
    }

    /**
     * Sets the connection key.
     *
     * @param _connectionKey the connection key
     * @return the user session
     */
    public UserSession setConnectionKey(final IKey _connectionKey)
    {
        connectionKey = _connectionKey;
        return this;
    }
}
