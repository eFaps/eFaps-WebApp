/*
 * Copyright 2003 - 2009 The eFaps Team
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.PageParameters;

import org.efaps.ui.wicket.models.cell.UIHiddenCell;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class UIAbstractPageObject extends AbstractUIObject
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final List<UIHiddenCell> hiddenCells = new ArrayList<UIHiddenCell>();

    /**
     * Constructor evaluating the UUID for the command and the oid from an
     * Opener instance.
     *
     * @param _parameters PageParameters for this Model
     */
    public UIAbstractPageObject(final PageParameters _parameters)
    {
        super(_parameters);
    }

    /**
     * Constructor.
     *
     * @param _commandUUID UUID for this Model
     * @param _instanceKey instance id for this Model
     * @param _openerId id of the opener
     */
    public UIAbstractPageObject(final UUID _commandUUID, final String _instanceKey, final String _openerId)
    {
        super(_commandUUID, _instanceKey, _openerId);
    }

    /**
     * Constructor.
     *
     * @param _commandUUID UUID for this Model
     * @param _instanceKey instance id for this Model
     */
    public UIAbstractPageObject(final UUID _commandUUID, final String _instanceKey)
    {
        this(_commandUUID, _instanceKey, null);
    }

    /**
     * Getter method for instance variable {@link #hiddenCells}.
     *
     * @return value of instance variable {@link #hiddenCells}
     */
    public List<UIHiddenCell> getHiddenCells()
    {
        return this.hiddenCells;
    }

    /**
     * Method to add a hidden Cell to the list {@link #hiddenCells}.
     * @param _hiddenCell cell to add
     */
    public void addHidden(final UIHiddenCell _hiddenCell) {
        this.hiddenCells.add(_hiddenCell);
    }

}
