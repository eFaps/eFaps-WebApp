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

import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.cache.CacheReloadException;

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
     *
     */
    private static final long serialVersionUID = 1L;
    private final UIFormCellSet set;

    /**
     * @param _instanceKey
     * @param _parent
     * @param _object
     * @param _child
     */
    public CellSetValue(final String _instanceKey,
                        final AbstractUIObject _parent,
                        final UIFormCellSet _set,
                        final UIValue _value)
    {
        super(_instanceKey, _parent, _value);
        this.set = _set;
    }
    /* (non-Javadoc)
     * @see org.efaps.ui.wicket.models.cell.AbstractUICellValue#getNewFieldConfiguration()
     */
    @Override
    protected FieldConfiguration getNewFieldConfiguration()
    {
        FieldSetConfiguration ret = null;
        try {
            ret =  new FieldSetConfiguration(getValue().getField().getId(), getValue().getAttribute().getId());
        } catch (final CacheReloadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }
}