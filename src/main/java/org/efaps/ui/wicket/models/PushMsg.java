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

    private final String content;
    private final Date timestamp;

    public PushMsg(final String content)
    {
        this.content = content;
        this.timestamp = new Date();
    }

    public String getContent()
    {
        return this.content;
    }

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
