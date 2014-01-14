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
     *  Configurationobject for this component.
     */
    private final FieldConfiguration config;

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
      *@param _model        Model for this component
     * @param _choices    Choices for the dropdowns
     * @param _fieldConfiguration    Configurationobject for this component.
     */
    public DropDownField(final String _wicketId,
                         final Model<AbstractUIField> _model,
                         final IModel<Map<Object, Object>> _choices,
                         final FieldConfiguration _fieldConfiguration)
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
        this.config = _fieldConfiguration;
        setChoices(DropDownField.getSelectChoices(_choices.getObject()));
        setChoiceRenderer(new ChoiceRenderer());
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("select");
        _tag.append("style", "text-align:" + this.config.getAlign(), ";");
        super.onComponentTag(_tag);
    }

    @Override
    public String getInputName()
    {
        String ret = "";
        try {
            ret = this.config.getName();
        } catch (final EFapsException e) {
            DropDownField.LOG.error("EFapsException", e);
        }
        return ret;
    }

    @Override
    protected void convertInput()
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfiguration getFieldConfig()
    {
        return this.config;
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
    }
}
