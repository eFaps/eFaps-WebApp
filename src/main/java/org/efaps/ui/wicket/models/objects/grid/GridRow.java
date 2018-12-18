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
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.db.Instance;

public class GridRow
    extends ArrayList<GridCell>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The instance. */
    private final Instance instance;

    /** The children. */
    private final List<GridRow> children = new ArrayList<>();

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

    /**
     * Adds the child.
     *
     * @param _gridRow the grid row
     * @return the grid row
     */
    public GridRow addChild(final GridRow _gridRow)
    {
        this.children.add(_gridRow);
        return this;
    }

    /**
     * Gets the children.
     *
     * @return the children
     */
    public List<GridRow> getChildren()
    {
        return this.children;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
