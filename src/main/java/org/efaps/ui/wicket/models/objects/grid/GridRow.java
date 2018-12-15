/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.ui.wicket.models.objects.grid;

import java.util.ArrayList;

import org.efaps.db.Instance;

public class GridRow
    extends ArrayList<GridCell>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The instance. */
    private final Instance instance;

    /**
     * Instantiates a new row.
     *
     * @param _instance the instance
     */
    public GridRow(final Instance _instance)
    {
        this.instance = _instance;
    }

    /**
     * Gets the single instance of Row.
     *
     * @return single instance of Row
     */
    public Instance getInstance()
    {
        return this.instance;
    }
}
