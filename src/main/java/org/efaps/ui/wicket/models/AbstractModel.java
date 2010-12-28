/*
 * Copyright 2003 - 2010 The eFaps Team
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

import org.apache.wicket.model.IModel;

/**
 * Basic Model for all Objects for the UserInterface.
 *
 * @param <T> Object Type
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractModel<T>
    implements IModel<T>
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The model Object.
     */
    private T uiObject;

    /**
     * @param _uiObject The model object
     */
    public AbstractModel(final T _uiObject)
    {
        this.uiObject = _uiObject;
    }

    /**
     * Gets the model object.
     *
     * @return The model object
     */
    public T getObject()
    {
        return this.uiObject;
    }

    /**
     * Sets the model object.
     *
     * @param _uiObject The model object
     */
    public void setObject(final T _uiObject)
    {
        this.uiObject = _uiObject;
    }

    /**
     * Detaches model after use. This is generally used to null out transient
     * references that can be re-attached later.
     */
    public void detach()
    {
        // Not implemented.
    }

}
