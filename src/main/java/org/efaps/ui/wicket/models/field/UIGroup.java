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

package org.efaps.ui.wicket.models.field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIGroup
    implements Serializable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * List of fields belonging to this group.
     */
    private final List<AbstractUIField> fields = new ArrayList<AbstractUIField>();

    /**
     * @param _uiField add an field to this group
     */
    public void add(final AbstractUIField _uiField)
    {
        this.fields.add(_uiField);
    }

    /**
     * Getter method for the instance variable {@link #fields}.
     *
     * @return value of instance variable {@link #fields}
     */
    public List<AbstractUIField> getFields()
    {
        return this.fields;
    }

}
