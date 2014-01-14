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
import java.util.List;

import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.field.AbstractUIField;
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


    private final FieldConfiguration fieldConfig;

    /**
     * value of this field.
     */
    private final AbstractUIField cellvalue;

    /**
     * @param _id
     * @param _choices
     */
    public CheckBoxField(final String _id,
                         final Model<AbstractUIField> _model,
                         final List<Object> _choices,
                         final FieldConfiguration _fieldConfiguration)
    {
        super(_id);
        this.fieldConfig  = _fieldConfiguration;
        this.cellvalue = _model.getObject();
        final Serializable value = this.cellvalue.getValue().getDbValue();
        try {
            if (value != null) {
                setDefaultModel(Model.of(CheckBoxOption.getChoices(this.cellvalue, _choices)));
            } else {
                setDefaultModel(new Model<String>());
            }
            setChoices(CheckBoxOption.getChoices(this.cellvalue, null));
            setLabel(Model.of(_fieldConfiguration.getLabel(this.cellvalue)));
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setChoiceRenderer(new ChoiceRenderer());
        setOutputMarkupId(true);
        setRequired(_fieldConfiguration.getField().isRequired());
    }

    /**
     * Getter method for the instance variable {@link #fieldConfiguration}.
     *
     * @return value of instance variable {@link #fieldConfiguration}
     */
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
            CheckBoxField.LOG.error("Catched Exception on get Input Name", e);
        }
        return ret;
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
    }

}
