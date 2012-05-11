/*
 * Copyright 2003 - 2012 The eFaps Team
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


package org.efaps.ui.wicket.models;


/**
 * Standard Simple implementation of the model.
 *
 * @param <T> Object Type
 * @author The eFaps Team
 * @version $Id$
 */
public class UIModel<T>
    extends AbstractModel<T>
{
    /**
     *  Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _uiObject The model object
     */
    public UIModel(final T _uiObject)
    {
        super(_uiObject);
    }
}
