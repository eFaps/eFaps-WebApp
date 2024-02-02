/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.components.values;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.request.EFapsRequestParametersAdapter;
import org.efaps.util.RandomUtil;

/**
 * Field to render a Boolean using two radio buttons.
 *
 * @author The eFaps Team
 */
public class BooleanField
    extends FormComponentPanel<Boolean>
    implements IFieldConfig, ILabelProvider<String>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Configuration for this field.
     */
    private final FieldConfiguration fieldConfiguration;

    /**
     * Label for this field.
     */
    private final String label;

    /** The input name. */
    private String inputName;

    /**
     * Instantiates a new boolean field.
     *
     * @param _wicketId wicket id for this component
     * @param _value value of this component
     * @param _choices choices
     * @param _fieldConfiguration configuration for this field
     * @param _label label for this field
     * @param _uniqueName the unique name
     */
    public BooleanField(final String _wicketId,
                        final Object _value,
                        final IModel<Map<Object, Object>> _choices,
                        final FieldConfiguration _fieldConfiguration,
                        final String _label,
                        final boolean _uniqueName)
    {
        super(_wicketId, new Model<Boolean>());
        setOutputMarkupId(true);
        setRequired(_fieldConfiguration.getField().isRequired());
        this.fieldConfiguration = _fieldConfiguration;
        this.label = _label;
        // make a unique name if in a fieldset
        this.inputName = _fieldConfiguration.getName()
                        + (_uniqueName ? "_" + RandomUtil.randomAlphabetic(4) : "");
        final RadioGroup<Boolean> radioGroup = new RadioGroup<Boolean>("radioGroup") {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public String getInputName()
            {
                return BooleanField.this.inputName;
            }
        };

        if (_value == null) {
            radioGroup.setDefaultModel(new Model<Boolean>());
        } else {
            radioGroup.setDefaultModel(Model.of((Boolean) _value));
        }
        add(radioGroup);
        final Iterator<Entry<Object, Object>> iter = _choices.getObject().entrySet().iterator();

        final Entry<Object, Object> first = iter.next();
        final Boolean firstVal = (Boolean) first.getValue();
        final Radio<Boolean> radio1 = new Radio<Boolean>("choice1", Model.of(firstVal), radioGroup) {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public String getValue()
            {
                return firstVal.toString();
            }
        };
        radio1.setLabel(Model.of((String) first.getKey()));
        radioGroup.add(radio1);
        final String markupId1 = radio1.getMarkupId(true);
        radioGroup.add(new Label("label1", Model.of((String) first.getKey())) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("for", markupId1);
            }
        });

        final Entry<Object, Object> second = iter.next();
        final Boolean secondVal = (Boolean) second.getValue();
        final Radio<Boolean> radio2 = new Radio<Boolean>("choice2", Model.of(secondVal), radioGroup) {
            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public String getValue()
            {
                return secondVal.toString();
            }
        };
        radio2.setLabel(Model.of((String) second.getKey()));
        radioGroup.add(radio2);
        final String markupId2 = radio2.getMarkupId(true);
        radioGroup.add(new Label("label2", Model.of((String) second.getKey())) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("for", markupId2);
            }
        });
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
     * {@inheritDoc}
     */
    @Override
    public FieldConfiguration getFieldConfig()
    {
        return this.fieldConfiguration;
    }

    @Override
    public IModel<String> getLabel()
    {
        final IModel<String> ret;
        if (this.label == null) {
            ret = Model.of(getFieldConfig().getLabel());
        } else {
            ret = Model.of(this.label);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void convertInput()
    {
        setConvertedInput(visitChildren(RadioGroup.class, (_radioGroup, _visit) -> _visit.stop(
                        ((FormComponent<Boolean>) _radioGroup).getConvertedInput())));

        // if a unique name was generated set the values in teh parameters
        if (!getFieldConfiguration().getName().equals(this.inputName)) {
            final EFapsRequestParametersAdapter parameters = (EFapsRequestParametersAdapter) getRequest()
                            .getRequestParameters();
            parameters.addParameterValue(getFieldConfiguration().getName(),
                            getConvertedInput() == null ? "" : getConvertedInput().toString());
        }
    }
}
