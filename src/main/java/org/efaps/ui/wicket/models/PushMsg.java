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
package org.efaps.ui.wicket.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PushMsg
    implements IWebSocketPushMessage, Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Content of  this message.
     */
    private final String content;

    /**
     * timestamp of this message.
     */
    private final Date timestamp;

    /**
     * @param _content content of the message
     */
    public PushMsg(final String _content)
    {
        this.content = _content;
        this.timestamp = new Date();
    }

    /**
     * @return the content of the message
     */
    public String getContent()
    {
        return this.content;
    }

    /**
     * @return the timestamp of the message
     */
    public Date getTimestamp()
    {
        return this.timestamp;
    }

    @Override
    public String toString()
    {
        final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(this.timestamp) + ": " + this.content;
    }
}
