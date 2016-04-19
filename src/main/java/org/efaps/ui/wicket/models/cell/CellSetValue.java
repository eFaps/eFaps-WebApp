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

import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.field.FieldSetConfiguration;
import org.efaps.ui.wicket.models.field.ISortable;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CellSetValue
    extends AbstractUICellValue
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Set the value belongs to.
     */
    private final UIFormCellSet cellSet;

    /**
     * @param _instanceKey key of the instacne
     * @param _parent parent object
     * @param _set set
     * @param _value value
     * @throws EFapsException on error
     */
    public CellSetValue(final String _instanceKey,
                        final AbstractUIObject _parent,
                        final UIFormCellSet _set,
                        final UIValue _value)
        throws EFapsException
    {
        super(_instanceKey, _parent, _value);
        this.cellSet = _set;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FieldConfiguration getNewFieldConfiguration()
        throws EFapsException
    {
        FieldConfiguration ret;
        if (getValue().getAttribute() == null) {
            ret = super.getNewFieldConfiguration();
        } else {
            ret = new FieldSetConfiguration(getValue().getField().getId(), getValue().getAttribute().getId());
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #cellSet}.
     *
     * @return value of instance variable {@link #cellSet}
     */
    public UIFormCellSet getCellSet()
    {
        return this.cellSet;
    }

    /* (non-Javadoc)
     * @see org.efaps.ui.wicket.models.field.ISortable#getCompareValue()
     */
    @Override
    public Comparable<?> getCompareValue()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final ISortable _o)
    {
        // TODO Auto-generated method stub
        return 0;
    }
}
