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
package org.efaps.ui.wicket.store;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.util.lang.Objects;

/**
 * The Class SerializedPage.
 *
 * @author The eFaps Team
 */
public class SerializedPage
    implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The id of the serialized {@link IManageablePage}.
     */
    private final int pageId;

    /**
     * The id of the http session in which the serialized
     * {@link IManageablePage} is used.
     */
    private final String sessionId;

    /**
     * The serialized {@link IManageablePage}.
     */
    private final byte[] data;

    /** The key. */
    private final String key;

    /**
     * Instantiates a new serialized page.
     *
     * @param _sessionId the session id
     * @param _pageId the page id
     * @param _key the key
     * @param _data the data
     */
    public SerializedPage(final String _sessionId,
                          final int _pageId,
                          final String _key,
                          final byte[] _data)
    {
        this.pageId = _pageId;
        this.sessionId = _sessionId;
        this.data = _data;
        this.key = _key;
    }

    /**
     * Gets the serialized {@link IManageablePage}.
     *
     * @return the serialized {@link IManageablePage}
     */
    public byte[] getData()
    {
        return this.data;
    }

    /**
     * Gets the id of the serialized {@link IManageablePage}.
     *
     * @return the id of the serialized {@link IManageablePage}
     */
    public int getPageId()
    {
        return this.pageId;
    }

    /**
     * Gets the id of the http session in which the serialized
     * {@link IManageablePage} is used.
     *
     * @return the id of the http session in which the serialized
     *         {@link IManageablePage} is used
     */
    public String getSessionId()
    {
        return this.sessionId;
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    public String getKey()
    {
        return this.key;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret = false;
        if (this == _obj) {
            ret = true;
        } else if (!(_obj instanceof SerializedPage)) {
            ret = false;
        } else {
            final SerializedPage rhs = (SerializedPage) _obj;
            ret = Objects.equal(getPageId(), rhs.getPageId()) && Objects.equal(getSessionId(), rhs.getSessionId());
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(getPageId(), getSessionId());
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
