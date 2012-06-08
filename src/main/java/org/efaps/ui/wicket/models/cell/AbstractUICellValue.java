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

import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.LinkWithRangesUI;
import org.efaps.admin.datamodel.ui.StringUI;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.components.values.DropDownField;
import org.efaps.ui.wicket.components.values.StringField;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AbstractUICellValue
    extends AbstractInstanceObject
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final AbstractUIObject parent;

    private final UIValue value;

    private final FieldConfiguration fieldConfiguration;

    public AbstractUICellValue(final String _instanceKey,
                               final AbstractUIObject _parent,
                               final UIValue _value)
    {
        super(_instanceKey);
        this.parent = _parent;
        this.value = _value;
        this.fieldConfiguration = getNewFieldConfiguration();
    }

    protected FieldConfiguration getNewFieldConfiguration()
    {
        return new FieldConfiguration(getValue().getField().getId());
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
     * @return Instance
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
     * Getter method for instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public AbstractUIObject getParent()
    {
        return this.parent;
    }

    @Override
    public String toString()
    {
        return this.value.toString();
    }

    public boolean editable()
    {
        return this.value.getField().isEditableDisplay(this.parent.getMode());
    }

    /**
     * Getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */
    public UIValue getValue()
    {
        return this.value;
    }

    /**
     * @param _string
     * @return
     * @throws EFapsException
     */
    public Component getComponent(final String _wicketId)
        throws EFapsException
    {
        Component ret;
        if (editable()) {
            if (getValue().getUIProvider() instanceof StringUI) {
                ret = new StringField(_wicketId, Model.of((String) getValue().getEditValue(getParent().getMode())),
                                getFieldConfiguration());
            } else if (getValue().getUIProvider() instanceof LinkWithRangesUI) {
                ret = new DropDownField(_wicketId, getValue().getDbValue(),
                                Model.ofMap((Map<Object, Object>) getValue().getEditValue(
                                getParent().getMode())),
                                getFieldConfiguration());
            } else {
                ret = new Label(_wicketId, (String) getValue().getEditValue(getParent().getMode()));
            }
        } else {
            if (getValue().getUIProvider() instanceof LinkWithRangesUI) {
                String label = "";
                if (getValue().getDbValue() != null) {
                    final Map<Object, Object> map = (Map<Object, Object>) getValue()
                                .getReadOnlyValue(getParent().getMode());
                    label =String.valueOf(map.get(getValue().getDbValue()));
                }
                ret = new Label(_wicketId, label);
            } else {
                ret = new Label(_wicketId, (String) getValue().getReadOnlyValue(getParent().getMode()));
            }
        }
        return ret;
    }

    public FieldConfiguration getFieldConfiguration()
    {
        return this.fieldConfiguration;
    }

}
