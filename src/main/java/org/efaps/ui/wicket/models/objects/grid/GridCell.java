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
package org.efaps.ui.wicket.models.objects.grid;

import java.io.Serializable;

import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.field.FieldConfiguration;

public class GridCell
    implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The field config. */
    private FieldConfiguration fieldConfig;

    /** The value. */
    private Object value;

    /** The sort value. */
    private Object sortValue;

    /** The instance. */
    private Instance instance;

    /**
     * Gets the single instance of Cell.
     *
     * @return single instance of Cell
     */
    public Instance getInstance()
    {
        return this.instance;
    }

    /**
     * Sets the instance.
     *
     * @param _instance the instance
     * @return the cell
     */
    protected GridCell setInstance(final Instance _instance)
    {
        this.instance = _instance;
        return this;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue()
    {
        return String.valueOf(this.value);
    }

    /**
     * Sets the value.
     *
     * @param _value the value
     * @return the cell
     */
    protected GridCell setValue(final Object _value)
    {
        this.value = _value;
        return this;
    }

    /**
     * Gets the sort value.
     *
     * @return the sort value
     */
    public Object getSortValue()
    {
        return this.sortValue;
    }

    /**
     * Sets the sort value.
     *
     * @param _sortValue the sort value
     * @return the cell
     */
    protected GridCell setSortValue(final Object _sortValue)
    {
        this.sortValue = _sortValue;
        return this;
    }

    /**
     * Gets the field config.
     *
     * @return the field config
     */
    public FieldConfiguration getFieldConfig()
    {
        return this.fieldConfig;
    }

    /**
     * Sets the field config.
     *
     * @param _fieldConfig the field config
     * @return the cell
     */
    protected GridCell setFieldConfig(final FieldConfiguration _fieldConfig)
    {
        this.fieldConfig = _fieldConfig;
        return this;
    }

}
