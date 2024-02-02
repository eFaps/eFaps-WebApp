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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.efaps.util.RandomUtil;

/**
 * The Class AbstractUI.
 */
public abstract class AbstractUI
    implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The idmap. */
    private final Map<String, Long> random2id = new HashMap<>();

    /**
     * Gets the random for ID.
     *
     * @param _id the id
     * @return the random for ID
     */
    public String getRandom4ID(final Long _id)
    {
        final String rid = RandomUtil.randomAlphanumeric(8);
        this.random2id.put(rid, _id);
        return rid;
    };

    /**
     * Gets the ID for random.
     *
     * @param _rid the rid
     * @return the ID for random
     */
    public Long getID4Random(final String _rid)
    {
        return this.random2id.get(_rid);
    };
}
