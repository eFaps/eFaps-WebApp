/*
 * Copyright 2003 - 2009 The eFaps Team
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

import org.apache.wicket.IClusterable;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.field.Field;

/**
 * @author jmox
 * @version $Id$
 */
public class UITableHeader implements IClusterable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The label for this header.
     */
    private final String label;

    /**
     * Is this header sortable.
     */
    private boolean sortable;

    /**
     * Name of the header.
     */
    private final String name;

    /**
     * Is this header filterable.
     */
    private final boolean filterable;

    /**
     * has this header a fixed width.
     */
    private final boolean fixedWidth;

    /**
     * Sort direction.
     */
    private SortDirection sortDirection;

    /**
     * Width of this header;
     */
    private int width;

    /**
     * Markup id of this header.
     */
    private String markupId;

    /**
     * @param _field            field
     * @param _sortdirection    sort direction
     */
    public UITableHeader(final Field _field, final SortDirection _sortdirection)
    {
        this.label = _field.getLabel();
        this.sortable = _field.isSortAble();
        this.name = _field.getName();
        this.filterable = _field.isFilterable();
        this.sortDirection = _sortdirection;
        this.width = _field.getWidth();
        this.fixedWidth = _field.isFixedWidth();
    }

    /**
     * @return translated label
     */
    public String getLabel()
    {
        String ret = "";
        if (this.label != null) {
            ret = DBProperties.getProperty(this.label);
        }
        return ret;
    }

    /**
     * This is the getter method for the instance variable {@link #sortable}.
     *
     * @return value of instance variable {@link #sortable}
     */

    public boolean isSortable()
    {
        return this.sortable;
    }

    /**
     * Setter method for instance variable {@link #sortable}.
     *
     * @param _sortable value for instance variable {@link #sortable}
     */
    public void setSortable(final boolean _sortable)
    {
        this.sortable = _sortable;
    }

    /**
     * This is the getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */

    public String getName()
    {
        return this.name;
    }

    /**
     * This is the getter method for the instance variable {@link #filterable}.
     *
     * @return value of instance variable {@link #filterable}
     */

    public boolean isFilterable()
    {
        return this.filterable;
    }

    /**
     * This is the getter method for the instance variable
     * {@link #sortDirection}.
     *
     * @return value of instance variable {@link #sortDirection}
     */

    public SortDirection getSortDirection()
    {
        return this.sortDirection;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #sortDirection}.
     *
     * @param _sortDirection the sortDirection to set
     */
    public void setSortDirection(final SortDirection _sortDirection)
    {
        this.sortDirection = _sortDirection;
    }

    /**
     * This is the getter method for the instance variable {@link #width}.
     *
     * @return value of instance variable {@link #width}
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * This is the setter method for the instance variable {@link #width}.
     *
     * @param _width the width to set
     */
    public void setWidth(final int _width)
    {
        this.width = _width;
    }

    /**
     * This is the getter method for the instance variable {@link #fixedWidth}.
     *
     * @return value of instance variable {@link #fixedWidth}
     */
    public boolean isFixedWidth()
    {
        return this.fixedWidth;
    }

    /**
     * This is the getter method for the instance variable {@link #markupId}.
     *
     * @return value of instance variable {@link #markupId}
     */
    public String getMarkupId()
    {
        return this.markupId;
    }

    /**
     * This is the setter method for the instance variable {@link #markupId}.
     *
     * @param _markupId the markupId to set
     */
    public void setMarkupId(final String _markupId)
    {
        this.markupId = _markupId;
    }
}
