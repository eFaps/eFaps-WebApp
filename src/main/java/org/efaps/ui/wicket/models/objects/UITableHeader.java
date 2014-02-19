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

import org.apache.wicket.util.io.IClusterable;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.datamodel.ui.DecimalUI;
import org.efaps.admin.datamodel.ui.NumberUI;
import org.efaps.admin.datamodel.ui.StringUI;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.Filter;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class UITableHeader
    implements IClusterable
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UITableHeader.class);

    /**
     * Enum for the different types of filter.
     */
    public static enum FilterType
    {
        /** Date. */
        DATE,
        /** Decimal. */
        DECIMAL,
        /** Integer. */
        INTEGER,
        /** none. */
        NONE,
        /** Text. */
        TEXT;
    }

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
     * The type of the filter.
     */
    private FilterType filterType = UITableHeader.FilterType.NONE;

    /**
     * has this header a fixed width.
     */
    private final boolean fixedWidth;

    /**
     * Sort direction.
     */
    private SortDirection sortDirection;

    /**
     * Width of this header.
     */
    private int width;

    /**
     * Markup id of this header.
     */
    private String markupId;

    /**
     * Id of the attribute this UITableHeader is based on.
     */
    private final long attrId;

    /**
     * Id of the field this UITableHeader belongs to.
     */
    private final long fieldId;

    /**
     * Is the filter activated.
     */
    private boolean filter;

    /**
     * Is the filter actually applied.
     */
    private boolean filterApplied = false;

    /**
     * The header object this header is embedded in.
     */
    private AbstractUIHeaderObject uiHeaderObject;

    /**
     * @param _uiHeaderObject   the header object this header is embedded in
     * @param _field            field
     * @param _sortdirection    sort direction
     * @param _attr             attribute this field is used for
     */
    public UITableHeader(final AbstractUIHeaderObject _uiHeaderObject,
                         final Field _field,
                         final SortDirection _sortdirection,
                         final Attribute _attr)
    {
        this.uiHeaderObject = _uiHeaderObject;
        this.label = _field.getLabel();
        this.sortable = _field.isSortAble();
        this.filter = !_field.getFilter().getType().equals(Filter.Type.NONE);
        setFilterApplied(_field.getFilter().isRequired());
        this.sortDirection = _sortdirection;
        this.width = _field.getWidth();
        this.fixedWidth = _field.isFixedWidth();
        this.fieldId = _field.getId();
        this.attrId = _attr != null ? _attr.getId() : 0;

        if (_field.getFilter().getType().equals(Filter.Type.FREETEXT)) {
            final UIInterface uiInterface = _field.getClassUI();
            final UUID attrTypeUUId = _attr == null ? null : _attr.getAttributeType().getUUID();
            // String
            if (UUID.fromString("72221a59-df5d-4c56-9bec-c9167de80f2b").equals(attrTypeUUId)
                            || (uiInterface != null && uiInterface instanceof StringUI)) {
                this.filterType = UITableHeader.FilterType.TEXT;
                // Integer
            } else if (UUID.fromString("41451b64-cb24-4e77-8d9e-5b6eb58df56f").equals(attrTypeUUId)
                            || (uiInterface != null && uiInterface instanceof NumberUI)) {
                this.filterType = UITableHeader.FilterType.INTEGER;
                // Decimal
            } else if (UUID.fromString("358d1f0e-43ae-425d-a4a0-8d5bad6f40d7").equals(attrTypeUUId)
                            || (uiInterface != null && uiInterface instanceof DecimalUI)) {
                this.filterType = UITableHeader.FilterType.DECIMAL;
                // Date or DateTime
            } else if (UUID.fromString("68ce3aa6-e3e8-40bb-b48f-2a67948c2e7e").equals(attrTypeUUId)
                            || UUID.fromString("e764db0f-70f2-4cd4-b2fe-d23d3da72f78").equals(attrTypeUUId)
                            || (uiInterface != null
                                && (uiInterface instanceof DateUI || uiInterface instanceof DateTimeUI))) {
                this.filterType = UITableHeader.FilterType.DATE;
            }
        }

        if (_field.getFilter().getType().equals(Filter.Type.FREETEXT) && _attr == null) {
            UITableHeader.LOG.warn("ClassNameUI is require when the field have not an attribute");
        }
    }

    /**
     * Get the Attribute belonging to this UITableHeader.
     *
     * @return Attribute
     * @throws CacheReloadException on error
     */
    public Attribute getAttribute()
        throws CacheReloadException
    {
        return Attribute.get(this.attrId);
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
     * Setter method for instance variable {@link #filter}.
     *
     * @param _filter value for instance variable {@link #filter}
     */
    public void setFilter(final boolean _filter)
    {
        this.filter = _filter;
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
     * This is the getter method for the instance variable {@link #fieldName}.
     *
     * @return value of instance variable {@link #fieldName}
     */

    public String getFieldName()
    {
        return getField().getName();
    }

    /**
     * Getter method for instance variable {@link #fieldId}.
     *
     * @return value of instance variable {@link #fieldId}
     */
    public long getFieldId()
    {
        return this.fieldId;
    }

    /**
     * @return the field this haeder belongs to.
     */
    public Field getField()
    {
        return Field.get(getFieldId());
    }

    /**
     * This is the getter method for the instance variable {@link #filter}.
     *
     * @return value of instance variable {@link #filter}
     */
    public boolean isFilter()
    {
        return this.filter;
    }

    /**
     * @return the filter belonging to this header.
     */
    public Filter getFilter()
    {
        return getField().getFilter();
    }

    /**
     * Getter method for instance variable {@link #filterType}.
     *
     * @return value of instance variable {@link #filterType}
     */
    public FilterType getFilterType()
    {
        return this.filterType;
    }

    /**
     * This is the getter method for the instance variable {@link #sortDirection}.
     *
     * @return value of instance variable {@link #sortDirection}
     */

    public SortDirection getSortDirection()
    {
        return this.sortDirection;
    }

    /**
     * This is the setter method for the instance variable {@link #sortDirection}.
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

    /**
     * Getter method for instance variable {@link #filterApplied}.
     *
     * @return value of instance variable {@link #filterApplied}
     */
    public boolean isFilterApplied()
    {
        return this.filterApplied;
    }

    /**
     * Setter method for instance variable {@link #filterApplied}.
     *
     * @param _filterApplied value for instance variable {@link #filterApplied}
     */
    public void setFilterApplied(final boolean _filterApplied)
    {
        this.filterApplied = _filterApplied;
    }

    /**
     * Getter method for the instance variable {@link #uiHeaderObject}.
     *
     * @return value of instance variable {@link #uiHeaderObject}
     */
    public AbstractUIHeaderObject getUiHeaderObject()
    {
        return this.uiHeaderObject;
    }

    /**
     * Setter method for instance variable {@link #uiHeaderObject}.
     *
     * @param _uiHeaderObject value for instance variable {@link #uiHeaderObject}
     */
    public void setUiHeaderObject(final AbstractUIHeaderObject _uiHeaderObject)
    {
        this.uiHeaderObject = _uiHeaderObject;
    }
}
