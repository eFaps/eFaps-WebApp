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

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * Abstract class for all cell types in the UserInterface.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractUICell
    extends AbstractInstanceObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Parent object of this cell object.
     */
    private final AbstractUIObject parent;

    /**
     * Id of the field this cell is based on.
     */
    private final long fieldId;

    /**
     * Instance variable storing the name of the field.
     */
    private final String name;

    /**
     * instance variable storing the target of the field.
     */
    private final Target target;

    /**
     * Stores the underlying user interface class for this cell.
     */
    private final UIInterface uiClass;

    /**
     * Stores the display type for the field.
     */
    private final Display display;

    /**
     * Variable storing the string representation of the value. of the field.
     */
    private String cellValue;


    /**
     * Variable storing the id of the attribute for the field.
     */
    private final long attributeId;

    /**
     * Component this cell belongs to.
     */
    private WebMarkupContainer component;

    /**
     * @param _parent       parent
     * @param _fieldValue   field value
     * @param _instanceKey  instance key
     * @param _cellvalue    value of the cell
     */
    public AbstractUICell(final AbstractUIObject _parent,
                          final FieldValue _fieldValue,
                          final String _instanceKey,
                          final String _cellvalue)
    {
        super(_instanceKey);
        this.parent = _parent;
        this.fieldId = _fieldValue.getField().getId();
        this.name = _fieldValue.getField().getName();
        this.uiClass = _fieldValue.getClassUI();
        this.target = _fieldValue.getField().getTarget();
        this.display = _fieldValue.getField().getDisplay(_parent.getMode());
        this.cellValue = _cellvalue;
        this.attributeId = _fieldValue.getAttribute() == null ? 0 : _fieldValue.getAttribute().getId();
    }

    /**
     * Getter method for instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public AbstractUIObject getParent()
    {
        return this.parent;
    }

    /**
     * Getter method for instance variable {@link #fieldId}.
     *
     * @return value of instance variable {@link #fieldId}
     */
    public long getFieldId()
    {
        return this.fieldId;
    }

    /**
     * Get the field this cell belong to.
     * @return Field
     */
    public Field getField()
    {
        return Field.get(this.fieldId);
    }

    /**
     * This is the getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * This is the getter method for the instance variable {@link #target}.
     *
     * @return value of instance variable {@link #target}
     */

    public Target getTarget()
    {
        return this.target;
    }

    /**
     * This is the getter method for the instance variable {@link #uiClass}.
     *
     * @return value of instance variable {@link #uiClass}
     */
    public UIInterface getUiClass()
    {
        return this.uiClass;
    }

    /**
     * Getter method for instance variable {@link #display}.
     *
     * @return value of instance variable {@link #display}
     */
    public Display getDisplay()
    {
        return this.display;
    }

    /**
     * This is the getter method for the instance variable {@link #cellvalue}.
     *
     * @return value of instance variable {@link #cellvalue}
     */
    public String getCellValue()
    {
        return this.cellValue;
    }

    /**
     * Setter method for instance variable {@link #cellValue}.
     *
     * @param _cellValue value for instance variable {@link #cellValue}
     */

    public void setCellValue(final String _cellValue)
    {
        this.cellValue = _cellValue;
    }

    /**
     * Method to get the Attribute used for this cell.
     *
     * @return Attribute if exists, else null
     * @throws CacheReloadException on error
     */
    public Attribute getAttribute()
        throws CacheReloadException
    {
        return Attribute.get(this.attributeId);
    }

    /**
     * @see org.efaps.ui.wicket.models.AbstractInstanceObject#hasInstanceManager()
     * @return false
     */
    @Override
    public boolean hasInstanceManager()
    {
        return this.parent != null ? this.parent.hasInstanceManager() : false;
    }

    /**
     * @see org.efaps.ui.wicket.models.AbstractInstanceObject#getInstanceFromManager()
     * @return  Instance
     * @throws EFapsException on error
     */
    @Override
    public Instance getInstanceFromManager()
        throws EFapsException
    {
        Instance ret = null;
        if (getParent() != null) {
            final AbstractCommand cmd = getParent().getCommand();
            final List<Return> rets = cmd.executeEvents(EventType.UI_INSTANCEMANAGER, ParameterValues.OTHERS,
                            getInstanceKey(), ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
            ret = (Instance) rets.get(0).get(ReturnValues.VALUES);
        }
        return ret;
    }



    /**
     * Getter method for instance variable {@link #component}.
     *
     * @return value of instance variable {@link #component}
     */
    public WebMarkupContainer getComponent()
    {
        return this.component;
    }

    /**
     * Setter method for instance variable {@link #component}.
     *
     * @param _component value for instance variable {@link #component}
     */

    public void setComponent(final WebMarkupContainer _component)
    {
        this.component = _component;
    }
}
