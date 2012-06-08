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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.dbproperty.DBProperties;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UISetColumnHeader
    implements Serializable
{

    private final String label;

    private final long attrId;


    /**
     * @param _label
     * @param _child
     * @param _object4Compare
     */
    public UISetColumnHeader(final String _label,
                             final Attribute _child)
    {
        this.label = DBProperties.getProperty(_label + "/" + _child.getName());
        this.attrId = _child.getId();
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

}
