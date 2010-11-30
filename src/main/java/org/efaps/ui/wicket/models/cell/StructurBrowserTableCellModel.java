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

package org.efaps.ui.wicket.models.cell;

import org.efaps.ui.wicket.models.AbstractModel;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StructurBrowserTableCellModel
    extends AbstractModel<UIStructurBrowserTableCell>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The uiobject for this model.
     */
    private UIStructurBrowserTableCell uiStrucBrowserTable;

    /**
     * @param _uitable The uiobject for this mode
     */
    public StructurBrowserTableCellModel(final UIStructurBrowserTableCell _uitable)
    {
        this.uiStrucBrowserTable = _uitable;
    }

    /**
     * Gets the model object.
     *
     * @return The model object
     */
    @Override
    public UIStructurBrowserTableCell getObject()
    {
        return this.uiStrucBrowserTable;
    }

    /**
     * Sets the model object.
     *
     * @param _object The model object
     */
    @Override
    public void setObject(final UIStructurBrowserTableCell _object)
    {
        this.uiStrucBrowserTable = _object;
    }
}
