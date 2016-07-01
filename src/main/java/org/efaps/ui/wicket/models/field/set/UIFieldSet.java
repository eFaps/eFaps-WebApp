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

package org.efaps.ui.wicket.models.field.set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.field.IUIElement;
import org.efaps.ui.wicket.models.objects.AbstractUIModeObject;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class UIFieldSet
    extends AbstractUIModeObject
    implements IUIElement
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Headers for this set of cells.
     */
    private final List<UIFieldSetColHeader> headers = new ArrayList<UIFieldSetColHeader>();

    /**
     * Rows fot this set.
     */
    private final List<UIFieldSetRow> rows = new ArrayList<UIFieldSetRow>();

    /**
     * Mapping of y-coordinate to x-coordinate 2 value.
     */
    private final Map<String, UIFieldSetRow> instKey2row = new HashMap<String, UIFieldSetRow>();

    /**
     * Mapping of field name to index.
     */
    private final Map<String, Integer> indexes = new HashMap<String, Integer>();

    /** The parent. */
    private final AbstractUIObject parent;

    /**
     * Configuration of the related field.
     */
    private final FieldConfiguration fieldConfiguration;

    /** The value. */
    private final UIValue value;

    /**
     * Instantiates a new UI field set.
     *
     * @param _parent the parent
     * @param _instance the _instance
     * @param _uiValue the ui value
     * @throws CacheReloadException the cache reload exception
     */
    public UIFieldSet(final AbstractUIObject _parent,
                      final Instance _instance,
                      final UIValue _uiValue)
        throws CacheReloadException
    {
        super(_instance == null ? null : _instance.getKey());
        this.parent = _parent;
        this.value = _uiValue;
        this.fieldConfiguration = new FieldConfiguration(_uiValue.getField().getId());
    }

    /**
     * @param _uiHeader Header to be added
     * @throws CacheReloadException on error
     */
    public void addHeader(final UIFieldSetColHeader _uiHeader)
        throws CacheReloadException
    {
        this.headers.add(_uiHeader);
    }

    /**
     * @param _rowInstance instance for the row to be added
     */
    public void addRow(final Instance _rowInstance)
    {
        final UIFieldSetRow row = new UIFieldSetRow(_rowInstance, this);
        this.rows.add(row);
        this.instKey2row.put(_rowInstance.getKey(), row);
    }

    /**
     * Adds the value.
     *
     * @param _rowInstance instance for the row to be added
     * @param _uiFieldSetValue the _ui field set value
     */
    public void addValue(final Instance _rowInstance,
                         final UIFieldSetValue _uiFieldSetValue)
    {
        this.instKey2row.get(_rowInstance.getKey()).add(_uiFieldSetValue);
    }

    @Override
    public Instance getInstanceFromManager()
        throws EFapsException
    {
        return getInstance();
    }

    @Override
    public boolean hasInstanceManager()
        throws EFapsException
    {
        return false;
    }

    /**
     * Gets the index.
     *
     * @param _inputName the _input name
     * @return the index
     */
    public int getIndex(final String _inputName)
    {
        Integer ret = 0;
        if (this.indexes.containsKey(_inputName)) {
            ret = this.indexes.get(_inputName) + 1;
        }
        this.indexes.put(_inputName, ret);
        return ret;
    }

    /**
     * Reset index.
     */
    public void resetIndex()
    {
        this.indexes.clear();
    }

    /**
     * Getter method for the instance variable {@link #headers}.
     *
     * @return value of instance variable {@link #headers}
     */
    public List<UIFieldSetColHeader> getHeaders()
    {
        return this.headers;
    }

    /**
     * Getter method for the instance variable {@link #rows}.
     *
     * @return value of instance variable {@link #rows}
     */
    public List<UIFieldSetRow> getRows()
    {
        return this.rows;
    }

    /**
     * Add a new Row.
     *
     * @throws EFapsException on error
     */
    public void addNewRow()
        throws EFapsException
    {
        final UIFieldSetRow row = new UIFieldSetRow(this);
        this.rows.add(row);
        for (final UIFieldSetColHeader header : getHeaders()) {
            final UIValue uiValue = UIValue
                            .get(Field.get(header.getFieldId()), Attribute.get(header.getAttrId()), null);
            final UIFieldSetValue cellSetValue = new UIFieldSetValue(getParent(), null, this, uiValue);
            row.add(cellSetValue);
        }
    }

    /**
     * Getter method for the instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public AbstractUIObject getParent()
    {
        return this.parent;
    }

    /**
     * Getter method for the instance variable {@link #fieldConfiguration}.
     *
     * @return value of instance variable {@link #fieldConfiguration}
     */
    public FieldConfiguration getFieldConfiguration()
    {
        return this.fieldConfiguration;
    }

    /**
     * Gets the label.
     *
     * @return the label
     * @throws EFapsException the e faps exception
     */
    public String getLabel()
        throws EFapsException
    {
        return getFieldConfiguration().evalLabel(getValue(), getInstance());
    }

    /**
     * Getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */
    public UIValue getValue()
    {
        return this.value;
    }
}
