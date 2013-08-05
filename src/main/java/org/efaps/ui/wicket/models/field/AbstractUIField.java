/*
 * Copyright 2003 - 2013 The eFaps Team
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.BooleanUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.datamodel.ui.DecimalUI;
import org.efaps.admin.datamodel.ui.LinkWithRangesUI;
import org.efaps.admin.datamodel.ui.NumberUI;
import org.efaps.admin.datamodel.ui.StringUI;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.components.values.BooleanField;
import org.efaps.ui.wicket.components.values.DropDownField;
import org.efaps.ui.wicket.components.values.LabelField;
import org.efaps.ui.wicket.components.values.StringField;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.objects.AbstractUIModeObject;
import org.efaps.util.EFapsException;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractUIField
    extends AbstractInstanceObject
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Configuration of the related field.
     */
    private FieldConfiguration fieldConfiguration;

    /**
     * Parent Object.
     */
    private final AbstractUIModeObject parent;

    /**
     * UserInterface Value.
     */
    private UIValue value;

    /**
     * @param _instanceKey key to the instance
     * @param _parent       parent object
     * @param _value        value
     * @throws EFapsException on error
     */
    public AbstractUIField(final String _instanceKey,
                           final AbstractUIModeObject _parent,
                           final UIValue _value)
        throws EFapsException
    {
        super(_instanceKey);
        this.parent = _parent;
        this.value = _value;
        this.fieldConfiguration = getNewFieldConfiguration();

    }

    /**
     * Getter method for the instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public AbstractUIModeObject getParent()
    {
        return this.parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasInstanceManager()
        throws EFapsException
    {
        return getParent() != null ? getParent().hasInstanceManager() : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instance getInstanceFromManager()
        throws EFapsException
    {
        return getParent().getInstanceFromManager();
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
     * Setter method for instance variable {@link #value}.
     *
     * @param _value value for instance variable {@link #value}
     */

    public void setValue(final UIValue _value)
    {
        this.value = _value;
    }

    /**
     * @return a new FieldConfiguration
     * @throws EFapsException on error
     */
    protected FieldConfiguration getNewFieldConfiguration()
        throws EFapsException
    {
        return new FieldConfiguration(getValue().getField().getId());
    }

    /**
     * @return is this value editable
     */
    public boolean editable()
    {
        return getValue().getField().isEditableDisplay(getParent().getMode());
    }

    /**
     * @param _wicketId wicket id
     * @return Component
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public Component getComponent(final String _wicketId)
        throws EFapsException
    {
        Component ret;
        if (editable()) {
            if (getValue().getUIProvider() instanceof StringUI) {
                ret = new StringField(_wicketId, Model.of(this), getFieldConfiguration());
            } else if (getValue().getUIProvider() instanceof LinkWithRangesUI) {
                ret = new DropDownField(_wicketId, Model.of(this),
                                Model.ofMap((Map<Object, Object>) getValue().getEditValue(
                                                getParent().getMode())),
                                getFieldConfiguration());
            } else if (getValue().getUIProvider() instanceof BooleanUI) {
                ret = new BooleanField(_wicketId, getValue().getDbValue(),
                                Model.ofMap((Map<Object, Object>) getValue().getEditValue(
                                                getParent().getMode())),
                                getFieldConfiguration());
            } else {
                ret = new Label(_wicketId, (String) getValue().getEditValue(getParent().getMode()));
            }
        } else {
            if (getValue().getUIProvider() instanceof LinkWithRangesUI) {
                String strValue = "";
                if (getValue().getDbValue() != null) {
                    final Map<Object, Object> map = (Map<Object, Object>) getValue()
                                    .getReadOnlyValue(getParent().getMode());
                    strValue = String.valueOf(map.get(getValue().getDbValue()));
                }
                ret = new LabelField(_wicketId, strValue, getFieldConfiguration().getLabel());
            } else if (getValue().getUIProvider() instanceof BooleanUI) {
                String strValue = "";
                if (getValue().getDbValue() != null) {
                    final Map<Object, Object> map = (Map<Object, Object>) getValue()
                                    .getReadOnlyValue(getParent().getMode());
                    for (final Entry<Object, Object> entry : map.entrySet()) {
                        if (entry.getValue().equals(getValue().getDbValue())) {
                            strValue = (String) entry.getKey();
                            break;
                        }
                    }
                }
                ret = new LabelField(_wicketId, strValue, getFieldConfiguration().getLabel());
            } else if (getValue().getUIProvider() instanceof DateUI) {
                String strValue = "";
                final Object valueTmp = getValue().getReadOnlyValue(getParent().getMode());
                if (valueTmp instanceof DateTime) {
                    final DateTime datetime = (DateTime) valueTmp;
                    final DateMidnight dateTmp = new DateMidnight(datetime.getYear(), datetime.getMonthOfYear(),
                                    datetime.getDayOfMonth(), Context.getThreadContext().getChronology());
                    final DateTimeFormatter formatter = DateTimeFormat.mediumDate();
                    strValue = dateTmp.toString(formatter.withLocale(Context.getThreadContext().getLocale()));
                }
                ret = new LabelField(_wicketId, strValue, getFieldConfiguration().getLabel());
            } else if (getValue().getUIProvider() instanceof DecimalUI) {
                String strValue = "";
                final Object valueTmp = getValue().getReadOnlyValue(getParent().getMode());
                if (valueTmp instanceof Number) {
                    final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                                .getLocale());
                    if (getValue().getAttribute() != null) {
                        formatter.setMaximumFractionDigits(getValue().getAttribute().getScale());
                    }
                    strValue = formatter.format(valueTmp);
                }
                ret = new LabelField(_wicketId, strValue, getFieldConfiguration().getLabel());
            } else if (getValue().getUIProvider() instanceof NumberUI) {
                String strValue = "";
                final Object valueTmp = getValue().getReadOnlyValue(getParent().getMode());
                if (valueTmp instanceof Number) {
                    strValue = String.valueOf(valueTmp);
                } else {
                    strValue = valueTmp == null ? "" : String.valueOf(valueTmp);
                }
                ret = new LabelField(_wicketId, strValue, getFieldConfiguration().getLabel());
            } else {
                ret = new LabelField(_wicketId,
                                String.valueOf(getValue().getReadOnlyValue(getParent().getMode())),
                                getFieldConfiguration().getLabel());
            }
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #fieldConfiguration}.
     *
     * @return value of instance variable {@link #fieldConfiguration}
     */
    public FieldConfiguration getFieldConfiguration()
    {
        return this.fieldConfiguration;
    }

    /**
     * Setter method for instance variable {@link #fieldConfiguration}.
     *
     * @param _fieldConfiguration value for instance variable {@link #fieldConfiguration}
     */

    protected void setFieldConfiguration(final FieldConfiguration _fieldConfiguration)
    {
        this.fieldConfiguration = _fieldConfiguration;
    }

    @Override
    public String toString()
    {
        return getValue().toString();
    }

}
