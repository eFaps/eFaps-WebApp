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

package org.efaps.ui.wicket.models.objects;

import java.util.UUID;

import org.efaps.admin.ui.Search;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class UISearchItem
    extends UIMenuItem
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * UUID of the Search.
     */
    private final UUID searchuuid;

    /**
     * @param _uuid UUID
     * @throws CacheReloadException on error
     */
    public UISearchItem(final UUID _uuid)
        throws CacheReloadException
    {
        this(_uuid, null);
    }

    /**
     * @param _uuid UUID
     * @param _instanceKey key to the instance
     * @throws CacheReloadException on error
     */
    public UISearchItem(final UUID _uuid,
                        final String _instanceKey)
        throws CacheReloadException
    {
        super(_uuid, _instanceKey);
        this.searchuuid = _uuid;
    }

    /**
     * @return the Search
     * @throws CacheReloadException on error
     */
    public Search getSearch()
        throws CacheReloadException
    {
        return Search.get(this.searchuuid);
    }

}
