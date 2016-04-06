/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.ui.wicket.components.values;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.IEnum;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.objects.RadioOption;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class RadioField
    extends RadioChoice<RadioOption>
    implements IFieldConfig
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RadioField.class);

    /** The field config. */
    private final FieldConfiguration fieldConfig;

    /** The cellvalue. */
    private final AbstractUIField cellvalue;

    /**
     * Instantiates a new radio field.
     *
     * @param _id the id
     * @param _model the model
     * @param _value the value
     * @param _fieldConfiguration the field configuration
     */
    public RadioField(final String _id,
                      final Model<AbstractUIField> _model,
                      final Object _value,
                      final FieldConfiguration _fieldConfiguration)
    {
        super(_id);
        this.fieldConfig = _fieldConfiguration;
        this.cellvalue = _model.getObject();
        final Serializable value = this.cellvalue.getValue().getDbValue();
        try {
            if (value != null && value instanceof IEnum) {
                setDefaultModel(Model.of(new RadioOption((IEnum) _value)));
            } else {
                setDefaultModel(new Model<String>());
            }
            setChoices(RadioOption.getChoices(this.cellvalue));
            if (getChoices() != null && getChoices().size() > 1) {
                setSuffix("<br/>");
            }
            setLabel(Model.of(this.cellvalue.getLabel()));
        } catch (final EFapsException e) {
            LOG.error("Cateched EFapsException", e);
        }
        setChoiceRenderer(new ChoiceRenderer());
        setOutputMarkupId(true);
        setRequired(_fieldConfiguration.getField().isRequired());
    }

    /**
     * Instantiates a new radio field.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @param _radios the radios
     */
    public RadioField(final String _wicketId,
                      final Model<AbstractUIField> _model,
                      final List<RadioOption> _radios)
    {
        super(_wicketId);
        if (_radios != null && _radios.size() > 1) {
            setSuffix("<br/>");
        }
        this.cellvalue = _model.getObject();
        this.fieldConfig = this.cellvalue.getFieldConfiguration();
        try {
            Model<?> model = null;
            if (_radios != null) {
                for (final RadioOption option : _radios) {
                    if (option.isSelected()) {
                        model = Model.of(option);
                        break;
                    }
                }
            }
            setDefaultModel(model == null ? new Model<String>() : model);
            setChoices(_radios);
            setLabel(Model.of(this.cellvalue.getLabel()));
        } catch (final EFapsException e) {
            LOG.error("Cateched EFapsException", e);
        }
        setChoiceRenderer(new ChoiceRenderer());
        setOutputMarkupId(true);
        setRequired(this.fieldConfig.getField().isRequired());
    }

    @Override
    public FieldConfiguration getFieldConfig()
    {
        return this.fieldConfig;
    }

    @Override
    public String getInputName()
    {
        String ret = "";
        try {
            ret = getFieldConfig().getName();
        } catch (final EFapsException e) {
            RadioField.LOG.error("Catched Exception on get Input Name", e);
        }
        return ret;
    }

    /**
     * The renderer for this checkbox.
     */
    public final class ChoiceRenderer
        implements IChoiceRenderer<RadioOption>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(final RadioOption _option)
        {
            return _option.getLabel();
        }

        @Override
        public String getIdValue(final RadioOption _object,
                                 final int _index)
        {
            return _object.getValue();
        }

        @Override
        public RadioOption getObject(final String _id,
                                     final IModel<? extends List<? extends RadioOption>> _choices)
        {
            RadioOption ret = null;
            final List<?> choices = _choices.getObject();
            for (int index = 0; index < choices.size(); index++) {
                final RadioOption choice = (RadioOption) choices.get(index);
                if (getIdValue(choice, index).equals(_id)) {
                    ret = choice;
                    break;
                }
            }
            return ret;
        }
    }
}
