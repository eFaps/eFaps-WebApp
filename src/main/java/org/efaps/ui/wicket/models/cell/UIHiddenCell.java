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

package org.efaps.ui.wicket.models.cell;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIHiddenCell extends AbstractUICell
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Was this cell already added to the form.
     */
    private boolean added = false;

    /**
     * @param _parent       parent
     * @param _fieldValue   field value
     * @param _instanceKey  instance key
     * @param _cellvalue    value of the cell
     */
    public UIHiddenCell(final AbstractUIObject _parent, final FieldValue _fieldValue, final String _instanceKey,
                        final String _cellvalue)
    {
        super(_parent, _fieldValue, _instanceKey, _cellvalue);
    }

    /**
     * Getter method for instance variable {@link #added}.
     *
     * @return value of instance variable {@link #added}
     */
    public boolean isAdded()
    {
        return this.added;
    }

    /**
     * Setter method for instance variable {@link #added}.
     *
     * @param _added value for instance variable {@link #added}
     */
    public void setAdded(final boolean _added)
    {
        this.added = _added;
    }
}
