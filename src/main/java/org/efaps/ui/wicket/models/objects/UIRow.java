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

import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.cell.UIHiddenCell;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.util.EFapsException;

/**
 * Class represents one row in the UITable.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIRow
    extends AbstractInstanceObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The id this row got for presentation in the userinterface.
     */
    private String userinterfaceId;

    /**
     * The instance variable stores the values for the table.
     *
     * @see #getValues
     */
    private final List<UITableCell> values = new ArrayList<UITableCell>();

    /**
     * This list contains the hidden cells for this row.
     */
    private final List<UIHiddenCell> hidden = new ArrayList<UIHiddenCell>();

    /**
     * Parent of this row.
     */
    private final AbstractUIObject parent;


    /**
     * Constructor used in case that no instances are given. e.g. on create.
     * @param _parent Parent object of this row
     */
    public UIRow(final AbstractUIObject _parent)
    {
        this(_parent, null);
    }

    /**
     * The constructor creates a new instance of class Row.
     * @param _parent Parent object of this row
     * @param _instanceKeys string with all oids for this row
     */
    public UIRow(final AbstractUIObject _parent, final String _instanceKeys)
    {
        super(_instanceKeys);
        this.parent = _parent;
    }

    /**
     * The instance method adds a new attribute value (from instance
     * {@link AttributeTypeInterface}) to the values.
     *
     * @param _cellmodel cell model to add
     * @see #values
     */
    public void add(final UITableCell _cellmodel)
    {
        this.values.add(_cellmodel);
    }

    /**
     * Add a hidden cell to this row.
     *
     * @param _hiddenCell hidden cell to be added
     */
    public void addHidden(final UIHiddenCell _hiddenCell)
    {
        this.hidden.add(_hiddenCell);
    }

    /**
     * Getter method for instance variable {@link #hidden}.
     *
     * @return value of instance variable {@link #hidden}
     */
    public List<UIHiddenCell> getHidden()
    {
        return this.hidden;
    }

    /**
     * The instance method returns the size of the array list {@link #values}.
     *
     * @see #values
     * @return size of instance variable {@link #values}
     */
    public int getSize()
    {
        return getValues().size();
    }

    /**
     * This is the getter method for the values variable {@link #values}.
     *
     * @return value of values variable {@link #values}
     * @see #values
     */
    public List<UITableCell> getValues()
    {
        return this.values;
    }

    /**
     * Getter method for the instance variable {@link #userinterfaceId}.
     *
     * @return value of instance variable {@link #userinterfaceId}
     */
    public String getUserinterfaceId()
    {
        return this.userinterfaceId;
    }

    /**
     * Setter method for instance variable {@link #userinterfaceId}.
     *
     * @param _userinterfaceId value for instance variable {@link #userinterfaceId}
     */

    public void setUserinterfaceId(final String _userinterfaceId)
    {
        this.userinterfaceId = _userinterfaceId;
    }

    /**
     * Getter method for the instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public AbstractUIObject getParent()
    {
        return this.parent;
    }

    /**
     * @see org.efaps.ui.wicket.models.AbstractInstanceObject#getInstanceFromManager()
     * @throws EFapsException on error
     * @return Instance
     */
    @Override
    public Instance getInstanceFromManager()
        throws EFapsException
    {
        return getParent().getInstanceFromManager();
    }

    /**
     * @see org.efaps.ui.wicket.models.AbstractInstanceObject#hasInstanceManager()
     * @return parent
     */
    @Override
    public boolean hasInstanceManager()
    {
        return getParent().hasInstanceManager();
    }
}
