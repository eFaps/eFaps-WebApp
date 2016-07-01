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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.efaps.db.Instance;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class UIFieldSetRow
    implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String oid;

    private final List<UIFieldSetValue> values = new ArrayList<UIFieldSetValue>();

    private final UIFieldSet parent;

    /**
     * @param _rowInstance
     * @param _uiFormCellSet
     */
    public UIFieldSetRow(final UIFieldSet _parent)
    {
        this(null, _parent);
    }

    /**
     *
     * @param _uiFormCellSet
     */
    public UIFieldSetRow(final Instance _rowInstance,
                         final UIFieldSet _parent)
    {
        this.oid = _rowInstance == null ? null : _rowInstance.getOid();
        this.parent = _parent;
    }

    /**
     * @param _cellSetValue
     */
    public void add(final UIFieldSetValue _cellSetValue)
    {
        this.values.add(_cellSetValue);
    }

    /**
     * @return
     *
     */
    public List<UIFieldSetValue> getValues()
    {
        return this.values;
    }

    /**
     * Getter method for the instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public UIFieldSet getParent()
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
