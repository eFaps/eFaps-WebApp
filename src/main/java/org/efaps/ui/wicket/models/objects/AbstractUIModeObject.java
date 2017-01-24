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


package org.efaps.ui.wicket.models.objects;

import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.util.cache.CacheReloadException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class AbstractUIModeObject
    extends AbstractInstanceObject
    implements IModeObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The instance variable stores the mode of the form.
     *
     * @see #getMode
     * @see #setMode
     */
    private TargetMode mode = TargetMode.UNKNOWN;


    /**
     * Constructor.
     *
     * @param _instanceKey instance id for this Model
     * @throws CacheReloadException on error
     */
    public AbstractUIModeObject(final String _instanceKey)
        throws CacheReloadException
    {
        super(_instanceKey);
    }


    /**
     * This is the getter method for the instance variable {@link #mode}.
     *
     * @return value of instance variable {@link #mode}
     * @see #mode
     * @see #setMode
     */
    public TargetMode getMode()
    {
        return this.mode;
    }

    /**
     * This is the setter method for the instance variable {@link #mode}.
     *
     * @param _mode new value for instance variable {@link #mode}
     * @see #mode
     * @see #getMode
     */
    public AbstractUIModeObject setMode(final TargetMode _mode)
    {
        this.mode = _mode;
        return this;
    }

    /**
     * Method to check if mode is create.
     *
     * @see #mode
     * @return true if mode is create
     */
    public boolean isCreateMode()
    {
        return getMode() == TargetMode.CREATE;
    }

    /**
     * Method to check if mode is edit.
     *
     * @see #mode
     * @return true if mode is edit
     */
    public boolean isEditMode()
    {
        return getMode() == TargetMode.EDIT;
    }

    /**
     * Method to check if mode is search.
     *
     * @see #mode
     * @return true if mode is search
     */
    public boolean isSearchMode()
    {
        return getMode() == TargetMode.SEARCH;
    }

    /**
     * Method to check if mode is view.
     *
     * @see #mode
     * @return true if mode is view
     */
    public boolean isViewMode()
    {
        return getMode() == TargetMode.VIEW || getMode() == TargetMode.UNKNOWN;
    }

    /**
     * Method to check if mode is view.
     *
     * @see #mode
     * @return true if mode is print
     */
    public boolean isPrintMode()
    {
        return getMode() == TargetMode.PRINT;
    }

}
