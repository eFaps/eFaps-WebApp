/*
 * Copyright 2003 - 2011 The eFaps Team
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.efaps.db.Instance;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CellSetRow
    implements Serializable
{

    private final String oid;

    private final List<CellSetValue> values = new ArrayList<CellSetValue>();

    private final UIFormCellSet parent;

    /**
     * @param _rowInstance
     * @param _uiFormCellSet
     */
    public CellSetRow(final UIFormCellSet _parent)
    {
        this(null, _parent);
    }



    /**
     *
     * @param _uiFormCellSet
     */
    public CellSetRow(final Instance _rowInstance,
                      final UIFormCellSet _parent)
    {
        this.oid = _rowInstance == null ? null : _rowInstance.getOid();
        this.parent = _parent;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _cellSetValue
     */
    public void add(final CellSetValue _cellSetValue)
    {
        this.values.add(_cellSetValue);
    }

    /**
     * @return
     *
     */
    public List<CellSetValue> getValues()
    {
        return this.values;
    }

    /**
     * Getter method for the instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public UIFormCellSet getParent()
    {
        return this.parent;
    }

    /**
     * Getter method for the instance variable {@link #oid}.
     *
     * @return value of instance variable {@link #oid}
     */
    public String getOid()
    {
        return this.oid;
    }
}
