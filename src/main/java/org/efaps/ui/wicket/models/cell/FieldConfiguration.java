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

import org.efaps.admin.ui.field.Field;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldConfiguration
    implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final long fieldId;

    /**
     * @param _field
     */
    public FieldConfiguration(final long _fieldId)
    {
        this.fieldId = _fieldId;
    }

    public String getName()
    {
        return getField().getName();
    }

    protected Field getField()
    {
        return Field.get(this.fieldId);
    }


    public String getAlign()
    {
        return getField().getAlign();
    }

    public int getSize()
    {
        return getField().getCols();
    }

}