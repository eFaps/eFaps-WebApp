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

import java.io.Serializable;
import java.util.Collection;

import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.Filter;
import org.efaps.ui.wicket.models.field.FieldConfiguration;

public class Column
    implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Id of the field this UITableHeader belongs to.
     */
    private FieldConfiguration fieldConfig;

    /** The data type. */
    private String dataType;

    /** The enum values. */
    private Collection<String> enumValues;

    /**
     * Gets the field name.
     *
     * @return the field name
     */
    public String getFieldName()
    {
        return getFieldConfig().getName();
    }

    /**
     * Gets the filter.
     *
     * @return the filter belonging to this header.
     */
    public Filter getFilter()
    {
        return getField().getFilter();
    }

    /**
     * Gets the field.
     *
     * @return the field this haeder belongs to.
     */
    public Field getField()
    {
        return getFieldConfig().getField();
    }

    /**
     * Gets the id of the field this UITableHeader belongs to.
     *
     * @return the id of the field this UITableHeader belongs to
     */
    public FieldConfiguration getFieldConfig()
    {
        return this.fieldConfig;
    }

    /**
     * Gets the label.
     *
     * @return translated label
     */
    public String getLabel()
    {
        return getFieldConfig().getLabel();
    }

    /**
     * Sets the field config.
     *
     * @param _fieldConfig the field config
     * @return the column
     */
    protected Column setFieldConfig(final FieldConfiguration _fieldConfig)
    {
        this.fieldConfig = _fieldConfig;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #dataType}.
     *
     * @return value of instance variable {@link #dataType}
     */
    public String getDataType()
    {
        return this.dataType;
    }

    /**
     * Setter method for instance variable {@link #dataType}.
     *
     * @param _dataType value for instance variable {@link #dataType}
     * @return the column
     */
    protected Column setDataType(final String _dataType)
    {
        this.dataType = _dataType;
        return this;
    }

    /**
     * Gets the enum values.
     *
     * @return the enum values
     */
    public Collection<String> getEnumValues()
    {
        return this.enumValues;
    }

    /**
     * Sets the enum values.
     *
     * @param _enumValues the new enum values
     */
    protected void setEnumValues(final Collection<String> _enumValues)
    {
        this.enumValues = _enumValues;
    }
}
