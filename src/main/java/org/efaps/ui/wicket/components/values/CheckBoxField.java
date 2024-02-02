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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.BooleanUI;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.objects.CheckBoxOption;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CheckBoxField
    extends CheckBoxMultipleChoice<CheckBoxOption>
    implements IFieldConfig
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CheckBoxField.class);

    /**
     * Configuration of the underlying field.
     */
    private final FieldConfiguration fieldConfig;

    /**
     * value of this field.
     */
    private final AbstractUIField cellvalue;

    /**
     * @param _wicketId wicktId of this component
     * @param _model model for this component
     * @param _choices list of choices
     * @param _fieldConfiguration configuration
     */
    public CheckBoxField(final String _wicketId,
                         final Model<AbstractUIField> _model,
                         final List<Object> _choices,
                         final FieldConfiguration _fieldConfiguration)
    {
        super(_wicketId);
        this.fieldConfig = _fieldConfiguration;
        this.cellvalue = _model.getObject();
        try {
            final Object value = this.cellvalue.getValue().getEditValue(this.cellvalue.getParent().getMode());
            final IUIProvider uiProvider = this.cellvalue.getValue().getUIProvider();
            // not booleanUI and not null OR BooleanUI and true
            if (!(uiProvider instanceof BooleanUI) && value != null
                            || uiProvider instanceof BooleanUI && value != null
                            && BooleanUtils.isTrue((Boolean) this.cellvalue.getValue().getDbValue())) {
                setDefaultModel(Model.of(CheckBoxOption.getChoices(this.cellvalue, _choices)));
            } else {
                setDefaultModel(Model.of(Collections.emptyList()));
            }
            setChoices(CheckBoxOption.getChoices(this.cellvalue, null));
            setLabel(Model.of(this.cellvalue.getLabel()));
            if (getChoices() != null && getChoices().size() > 1) {
                setSuffix("<br/>");
            }
        } catch (final EFapsException e) {
            CheckBoxField.LOG.error("Caught excpetion", e);
        }
        setChoiceRenderer(new ChoiceRenderer());
        setOutputMarkupId(true);
        setRequired(_fieldConfiguration.getField().isRequired());
    }

    /**
     * Instantiates a new check box field.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @param _checkBoxes the check boxes
     */
    public CheckBoxField(final String _wicketId,
                         final Model<AbstractUIField> _model,
                         final List<CheckBoxOption> _checkBoxes)
    {
        super(_wicketId);
        if (_checkBoxes != null && _checkBoxes.size() > 1) {
            setSuffix("<br/>");
        }
        this.cellvalue = _model.getObject();
        this.fieldConfig = this.cellvalue.getFieldConfiguration();
        try {
            final List<CheckBoxOption> selected = new ArrayList<>();
            final IModel<?> model = Model.of(selected);
            if (_checkBoxes != null) {
                for (final CheckBoxOption option : _checkBoxes) {
                    if (option.isSelected()) {
                        selected.add(option);
                    }
                }
            }
            setDefaultModel(model);
            setChoices(_checkBoxes);
            setLabel(Model.of(this.cellvalue.getLabel()));
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setChoiceRenderer(new ChoiceRenderer());
        setOutputMarkupId(true);
        setRequired(this.fieldConfig.getField().isRequired());
    }

    /**
     * Getter method for the instance variable {@link #fieldConfiguration}.
     *
     * @return value of instance variable {@link #fieldConfiguration}
     */
    @Override
    public FieldConfiguration getFieldConfig()
    {
        return this.fieldConfig;
    }

    @Override
    public String getInputName()
    {
        return getFieldConfig().getName();
    }

    /**
     * The renderer for this checkbox.
     */
    public final class ChoiceRenderer
        implements IChoiceRenderer<CheckBoxOption>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(final CheckBoxOption _option)
        {
            return _option.getLabel();
        }

        @Override
        public String getIdValue(final CheckBoxOption _object,
                                 final int _index)
        {
            return _object.getValue();
        }

        @Override
        public CheckBoxOption getObject(final String _id,
                                        final IModel<? extends List<? extends CheckBoxOption>> _choices)
        {
            CheckBoxOption ret = null;
            final List<?> choices = _choices.getObject();
            for (int index = 0; index < choices.size(); index++) {
                final CheckBoxOption choice = (CheckBoxOption) choices.get(index);
                if (getIdValue(choice, index).equals(_id)) {
                    ret = choice;
                    break;
                }
            }
            return ret;
        }
    }
}
