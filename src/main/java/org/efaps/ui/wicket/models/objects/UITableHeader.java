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

import org.apache.wicket.util.io.IClusterable;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.datamodel.ui.DecimalUI;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.datamodel.ui.NumberUI;
import org.efaps.admin.datamodel.ui.StringUI;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.Filter;
import org.efaps.api.ui.FilterType;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
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
    public enum FilterValueType
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
     * Is this header sortable.
     */
    private boolean sortable;

    /**
     * The type of the filter.
     */
    private FilterValueType filterType = UITableHeader.FilterValueType.NONE;

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
    private final FieldConfiguration fieldConfig;

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
     * @param _fieldConfig      Field Configuration
     * @param _sortdirection    sort direction
     * @param _attr             attribute this field is used for
     */
    public UITableHeader(final AbstractUIHeaderObject _uiHeaderObject,
                         final FieldConfiguration _fieldConfig,
                         final SortDirection _sortdirection,
                         final Attribute _attr)
    {
        this.uiHeaderObject = _uiHeaderObject;
        this.fieldConfig = _fieldConfig;
        this.sortable = _fieldConfig.getField().isSortAble();
        this.filter = !_fieldConfig.getField().getFilter().getType().equals(FilterType.NONE);
        setFilterApplied(_fieldConfig.getField().getFilter().isRequired());
        this.sortDirection = _sortdirection;
        this.width = _fieldConfig.getWidthWeight();
        this.attrId = _attr != null ? _attr.getId() : 0;

        if (_fieldConfig.getField().getFilter().getType().equals(FilterType.FREETEXT)) {
            IUIProvider uiProvider = _fieldConfig.getField().getUIProvider();
            if (uiProvider == null && _attr != null) {
                uiProvider = _attr.getAttributeType().getUIProvider();
            }
            if (uiProvider != null) {
                if (uiProvider instanceof StringUI) {
                    this.filterType = UITableHeader.FilterValueType.TEXT;
                } else if (uiProvider instanceof NumberUI) {
                    this.filterType = UITableHeader.FilterValueType.INTEGER;
                } else if (uiProvider instanceof DecimalUI) {
                    this.filterType = UITableHeader.FilterValueType.DECIMAL;
                } else if (uiProvider instanceof DateUI || uiProvider instanceof DateTimeUI) {
                    this.filterType = UITableHeader.FilterValueType.DATE;
                }
            }
        }

        if (_fieldConfig.getField().getFilter().getType().equals(FilterType.FREETEXT) && _attr == null
                        && _fieldConfig.getField().getUIProvider() == null) {
            UITableHeader.LOG.warn("UIProvider is require when the field has no attribute, Field:{}",
                            _fieldConfig.getField().getName());
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
        return this.fieldConfig.getLabel();
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
        return this.fieldConfig.getField().getId();
    }

    /**
     * @return the field this haeder belongs to.
     */
    public Field getField()
    {
        return this.fieldConfig.getField();
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
    public FilterValueType getFilterType()
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
        return this.fieldConfig.isFixedWidth();
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

    /**
     * Getter method for the instance variable {@link #fieldConfig}.
     *
     * @return value of instance variable {@link #fieldConfig}
     */
    public FieldConfiguration getFieldConfig()
    {
        return this.fieldConfig;
    }
}
