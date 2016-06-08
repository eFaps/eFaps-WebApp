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

package org.efaps.ui.wicket.models.cell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * Responsable to hold the data for an AttributeSet.
 *
 * @author The eFaswp Team
 * @version $Id$
 */
public class UIFormCellSet
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * IS this UIFormCellSet in edit mode.
     */
    private final boolean editMode;

    /**
     * Headers for this set of cells.
     */
    private final List<UISetColumnHeader> headers = new ArrayList<UISetColumnHeader>();

    /**
     * Mapping of y-coordinate to x-coordinate 2 value.
     */
    private final Map<String, CellSetRow> instKey2row = new HashMap<String, CellSetRow>();

    /**
     * Rows fot this set.
     */
    private final List<CellSetRow> rows = new ArrayList<CellSetRow>();

    /**
     * Mapping of field name to index.
     */
    private final Map<String, Integer> indexes = new HashMap<String, Integer>();

    /**
     * @param _parent parent object
     * @param _fieldValue FieldValue
     * @param _instance instance
     * @param _value value
     * @param _icon icon
     * @param _label Label
     * @param _edit edit mode or not
     * @throws EFapsException on error
     */
    public UIFormCellSet(final AbstractUIObject _parent,
                         final Instance _instance,
                         final String _value,
                         final String _icon,
                         final String _label,
                         final boolean _edit)
        throws EFapsException
    {
        this.editMode = _edit;
    }

    /**
     * @return is this edit mode
     */
    public boolean isEditMode()
    {
        return this.editMode;
    }

    /**
     * @param _uiHeader Header to be added
     * @throws CacheReloadException on error
     */
    public void addHeader(final UISetColumnHeader _uiHeader)
        throws CacheReloadException
    {
        this.headers.add(_uiHeader);
    }

    /**
     * @param _rowInstance instance for the row to be added
     */
    public void addRow(final Instance _rowInstance)
    {
        final CellSetRow row = new CellSetRow(_rowInstance, this);
        this.rows.add(row);
        this.instKey2row.put(_rowInstance.getKey(), row);
    }

    /**
     * @param _rowInstance instance for the row to be added
     * @param _cellSetValue value for the row
     */
    public void addValue(final Instance _rowInstance,
                         final CellSetValue _cellSetValue)
    {
        this.instKey2row.get(_rowInstance.getKey()).add(_cellSetValue);
    }

    /**
     * Getter method for the instance variable {@link #rows}.
     *
     * @return value of instance variable {@link #rows}
     */
    public List<CellSetRow> getRows()
    {
        return this.rows;
    }

    /**
     * Getter method for the instance variable {@link #headers}.
     *
     * @return value of instance variable {@link #headers}
     */
    public List<UISetColumnHeader> getHeaders()
    {
        return this.headers;
    }

    /**
     * Add a new Row.
     *
     * @throws EFapsException on error
     */
    public void addNewRow()
        throws EFapsException
    {
        final CellSetRow row = new CellSetRow(this);
        this.rows.add(row);
        for (final UISetColumnHeader header : this.headers) {
            final UIValue uiValue = UIValue
                            .get(Field.get(header.getFieldId()), Attribute.get(header.getAttrId()), null);
            final CellSetValue cellSetValue = new CellSetValue(null, null, this, uiValue);
            row.add(cellSetValue);
        }
    }

    /**
     * @param _inputName
     * @return
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
     *
     */
    public void resetIndex()
    {
        this.indexes.clear();
    }
}
