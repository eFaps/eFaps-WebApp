/*
 * Copyright 2003 - 2012 The eFaps Team
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

import org.efaps.admin.datamodel.ui.FieldValue;
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
    extends UIFormCell
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * IS this UIFormCellSet inedit mode.
     */
    private final boolean editMode;

    private final List<UISetColumnHeader> headers = new ArrayList<UISetColumnHeader>();

    /**
     * Mapping of y-coordinate to x-coordinate 2 value.
     */
    private final Map<String, CellSetRow> instKey2row = new HashMap<String, CellSetRow>();

    private final List<CellSetRow> rows = new ArrayList<CellSetRow>();


    /**
     * @param _parent       parent object
     * @param _fieldValue   FieldValue
     * @param _instance     instance
     * @param _value        value
     * @param _icon         icon
     * @param _label        Label
     * @param _edit         edit mode or not
     * @throws EFapsException   on error
     */
    public UIFormCellSet(final AbstractUIObject _parent,
                         final FieldValue _fieldValue,
                         final Instance _instance,
                         final String _value,
                         final String _icon,
                         final String _label,
                         final boolean _edit)
        throws EFapsException
    {
        super(_parent, _fieldValue, _instance, _value, null, _icon, _label, "");
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
     * @param _column       x-coordinate
     * @param _uiFormCell   UIFormCell used as definition
     * @throws CacheReloadException on error
     */
    public void addHeader(final UISetColumnHeader _uiHeader)
        throws CacheReloadException
    {
        this.headers.add(_uiHeader);
    }

    /**
     * @param _next
     */
    public void addRow(final Instance _rowInstance)
    {
        final CellSetRow row = new CellSetRow(_rowInstance);
        this.rows.add(row);
        this.instKey2row.put(_rowInstance.getKey(), row);
    }

    /**
     * @param _rowInstance
     * @param _cellSetValue
     */
    public void addValue(final Instance _rowInstance,
                         final CellSetValue _cellSetValue)
    {
        this.instKey2row.get(_rowInstance.getKey()).add(_cellSetValue);
    }

    /**
     * @return
     *
     */
    public List<CellSetRow> getRows()
    {
        return this.rows;
    }

    /**
     * @return
     * @return
     */
    public List<UISetColumnHeader> getHeaders()
    {
        return this.headers;
    }
}
