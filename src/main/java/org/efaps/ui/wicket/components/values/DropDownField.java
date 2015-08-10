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

package org.efaps.ui.wicket.components.values;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.models.cell.CellSetValue;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.objects.DropDownOption;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Render a DropDown Field.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DropDownField
    extends DropDownChoice<DropDownOption>
    implements IFieldConfig
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DropDownField.class);

    /**
     * value of this field.
     */
    private final AbstractUIField cellvalue;

    /**
     * Was the value already converted.
     */
    private boolean converted = false;

    /**
     * @param _wicketId     wicket id for this component
     * @param _model        Model for this component
     * @param _choices    Choices for the dropdowns
     * @param _fieldConfiguration    Configurationobject for this component.
     */
    public DropDownField(final String _wicketId,
                         final Model<AbstractUIField> _model,
                         final IModel<Map<Object, Object>> _choices)
    {
        super(_wicketId);
        setOutputMarkupId(true);
        this.cellvalue = _model.getObject();
        final Serializable value = this.cellvalue.getValue().getDbValue();
        if (value != null) {
            setDefaultModel(Model.of(new DropDownOption(String.valueOf(value), null)));
        } else {
            setDefaultModel(new Model<String>());
        }
        setChoices(DropDownField.getSelectChoices(_choices.getObject()));
        setChoiceRenderer(new ChoiceRenderer());
    }

    /**
     * @param _wicketId     wicket id for this component
     * @param _model        Model for this component
     * @param _choices    Choices for the dropdowns
     */
    public DropDownField(final String _wicketId,
                         final Model<AbstractUIField> _model,
                         final List<DropDownOption> _choices)
    {
        super(_wicketId);
        this.cellvalue = _model.getObject();
        for (final DropDownOption choice : _choices) {
            if (choice.isSelected()) {
                setDefaultModel(Model.of(choice));
                break;
            }
        }
        if (getDefaultModel() == null) {
            if (_choices.isEmpty()) {
                setDefaultModel(new Model<String>());
            } else {
                setDefaultModel(Model.of(_choices.get(0)));
            }
        }
        setChoices(_choices);
        setChoiceRenderer(new ChoiceRenderer());
        if (this.cellvalue.getFieldConfiguration().getField().hasEvents(EventType.UI_FIELD_UPDATE)) {
            final List<EventDefinition> events =
                            this.cellvalue.getFieldConfiguration().getField().getEvents(EventType.UI_FIELD_UPDATE);
            String eventName = "onchange";
            for (final EventDefinition event : events) {
               eventName = event.getProperty("Event") == null ? "onchange" : event.getProperty("Event");
            }
            add(new AjaxFieldUpdateBehavior(eventName, Model.of(this.cellvalue)));
        }
    }

    @Override
    protected CharSequence getDefaultChoice(final String _selectedValue)
    {
        // do not set a default choice, because the esjp must do that
        return "";
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("select");
        _tag.append("style", "text-align:" + getFieldConfig().getAlign(), ";");
        super.onComponentTag(_tag);
    }

    @Override
    public String getInputName()
    {
        String ret = "";
        try {
            ret = getFieldConfig().getName();
        } catch (final EFapsException e) {
            DropDownField.LOG.error("EFapsException", e);
        }
        return ret;
    }

    @Override
    public void convertInput()
    {
        this.converted = true;
        int i = 0;
        if (this.cellvalue instanceof CellSetValue) {
            final UIFormCellSet cellset = ((CellSetValue) this.cellvalue).getCellSet();
            i = cellset.getIndex(getInputName());
        }
        final String[] value = getInputAsArray();

        setConvertedInput(new DropDownOption(
                        String.valueOf(value != null && value.length > 0 && value[i] != null ? trim(value[i]) : null),
                        null));
    }

    @Override
    public void updateModel()
    {
        if (!this.converted) {
            convertInput();
        }
        setModelObject(getConvertedInput());
        try {
            this.cellvalue.setValue(UIValue.get(this.cellvalue.getValue().getField(), this.cellvalue.getValue()
                            .getAttribute(), ((DropDownOption) getDefaultModelObject()).getValue()));
        } catch (final CacheReloadException e) {
            DropDownField.LOG.error("EFapsException", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfiguration getFieldConfig()
    {
        return this.cellvalue.getFieldConfiguration();
    }


    /**
     * @param _values value to convert to dropdown otions
     * @return list of options
     */
    private static List<DropDownOption> getSelectChoices(final Map<Object, Object> _values)
    {
        final List<DropDownOption> list = new ArrayList<DropDownOption>();
        for (final Entry<Object, Object> entry : _values.entrySet()) {
            list.add(new DropDownOption(String.valueOf(entry.getKey()),
                            String.valueOf(entry.getValue())));
        }
        return list;
    }

    @Override
    public IModel<String> getLabel()
    {
        String ret = null;
        try {
            ret = this.cellvalue.getLabel();
        } catch (final EFapsException e) {
            DropDownField.LOG.error("EFapsException", e);
        }
        return  Model.of(ret);
    }

    /**
     * The renderer for this dropdown.
     */
    public final class ChoiceRenderer
        implements IChoiceRenderer<DropDownOption>
    {
        /**
         * Needed fro serialization.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(final DropDownOption _option)
        {
            return _option.getLabel();
        }

        @Override
        public String getIdValue(final DropDownOption _object,
                                 final int _index)
        {
            return _object.getValue();
        }

        @Override
        public DropDownOption getObject(final String _id,
                                        final IModel<? extends List<? extends DropDownOption>> _choices)
        {
            DropDownOption ret = null;
            final List<?> choices = _choices.getObject();
            for (int index = 0; index < choices.size(); index++)
            {
                final DropDownOption choice = (DropDownOption) choices.get(index);
                if (getIdValue(choice, index).equals(_id))
                {
                    ret =  choice;
                    break;
                }
            }
            return ret;
        }
    }
}
