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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldSetConfiguration
    extends FieldConfiguration
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Id of the related attribute, 0 if none.
     */
    private final long attrId;

    /**
     * @param _fieldId id of the field
     * @param _attrId id of the attribute
     */
    public FieldSetConfiguration(final long _fieldId,
                                 final long _attrId)
    {
        super(_fieldId);
        this.attrId = _attrId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
        throws EFapsException
    {
        final String ret;
        if (this.attrId > 0) {
            ret = super.getName() + "_" + Attribute.get(this.attrId).getName();
        } else {
            ret = super.getName();
        }
        return ret;
    }
}
