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
package org.efaps.ui.wicket.models.field.set;

import java.io.Serializable;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.field.Field;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class UIFieldSetColHeader
    implements Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Label for this column.
     */
    private final String label;

    /**
     * Id of the attribute, 0 if none.
     */
    private final long attrId;

    /**
     * Id of the fiel this column header belongs to.
     */
    private final long fieldId;

    /**
     * @param _label    label
     * @param _child    child attribute
     * @param _field    Field
     */
    public UIFieldSetColHeader(final String _label,
                               final Attribute _child,
                               final Field _field)
    {
        if (_child == null) {
            this.label = DBProperties.getProperty(_label);
            this.attrId = 0;
        } else {
            this.label = DBProperties.getProperty(_label + "/" + _child.getName());
            this.attrId = _child.getId();
        }
        this.fieldId = _field.getId();
    }

    /**
     * Getter method for the instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Getter method for the instance variable {@link #attrId}.
     *
     * @return value of instance variable {@link #attrId}
     */
    public long getAttrId()
    {
        return this.attrId;
    }

    /**
     * Getter method for the instance variable {@link #fieldId}.
     *
     * @return value of instance variable {@link #fieldId}
     */
    public long getFieldId()
    {
        return this.fieldId;
    }
}
