/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.efaps.ui.wicket.connectionregistry.RegistryManager;
import org.efaps.ui.wicket.connectionregistry.UserSession;
import org.joda.time.DateTime;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class UIUserSession
    extends UIUser implements IWebSocketPushMessage
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * SessionId for the user.
     */
    private String sessionId;

    /**
     * last Activity as date long.
     */
    private final long lastActivity;

    /**
     * @param _userName username
     * @param _sessionId    sessionid
     * @param _lastActivity last registered activity
     */
    public UIUserSession(final String _userName,
                         final String _sessionId,
                         final Long _lastActivity)
    {
        super(_userName);
        this.sessionId = _sessionId;
        this.lastActivity = _lastActivity == null ? 100 : _lastActivity;
    }

    /**
     * Getter method for the instance variable {@link #sessionId}.
     *
     * @return value of instance variable {@link #sessionId}
     */
    public String getSessionId()
    {
        return this.sessionId;
    }

    /**
     * Setter method for instance variable {@link #sessionId}.
     *
     * @param _sessionId value for instance variable {@link #sessionId}
     */
    public void setSessionId(final String _sessionId)
    {
        this.sessionId = _sessionId;
    }

    /**
     * Getter method for the instance variable {@link #lastActivity}.
     *
     * @return value of instance variable {@link #lastActivity}
     */
    public DateTime getLastActivity()
    {
        return new DateTime(this.lastActivity);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Gets the UI user sessions.
     *
     * @return list of currently registered Users
     */
    public static List<UIUserSession> getUIUserSessions()
    {
        final List<UIUserSession> ret = new ArrayList<>();
        final Collection<UserSession> userSessions = RegistryManager.getUserSessions();
        for (final UserSession userSession : userSessions) {
            ret.add(new UIUserSession(userSession.getUserName(), userSession.getSessionId(), userSession
                            .getLastActivity()));
        }
        return ret;
    }
}
