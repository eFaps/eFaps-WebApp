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

import java.util.stream.Collectors;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.ui.AbstractWithUoMProvider;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.objects.DropDownOption;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumberWithUoMField
    extends FormComponentPanel<AbstractUIField>
    implements IFieldConfig, ILabelProvider<String>
{

    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(NumberWithUoMField.class);

    public NumberWithUoMField(final String _wicketId,
                              final IModel<AbstractUIField> _model) throws EFapsException
    {
        super(_wicketId, _model);
        final FieldConfiguration config = _model.getObject().getFieldConfiguration();
        final Object[] value = (Object[]) _model.getObject().getValue().getEditValue(_model.getObject().getParent().getMode());
        Model<Number> numberModel;
        if (value == null) {
            numberModel = Model.of();
        } else {
            numberModel = Model.of((Number) value[0]);
        }

        final TextField<Number> valueField = new TextField<Number>("value", numberModel)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public String getInputName()
            {
                return config.getField().getName();
            }
        };
        valueField.setRequired(config.getField().isRequired());
        valueField.setOutputMarkupId(true);
        add(valueField);

        final DropDownChoice<DropDownOption> uomField = new DropDownChoice<DropDownOption>("uom")
        {

            private static final long serialVersionUID = 1L;

            @Override
            public String getInputName()
            {
                return config.getField().getName() + AbstractWithUoMProvider.UOMSUFFIX;
            }
        };
        try {
            final Dimension dimension = _model.getObject().getValue().getAttribute().getDimension();
            UoM uom;
            if (value != null && value[1] instanceof UoM) {
                uom = (UoM) value[1];
            } else {
                uom = dimension.getBaseUoM();
            }
            uomField.setDefaultModel(Model.of(new DropDownOption(String.valueOf(uom.getId()), uom.getSymbol())));
            uomField.setChoices(dimension.getUoMs().stream()
                            .map(auom -> new DropDownOption(String.valueOf(auom.getId()), auom.getSymbol()))
                            .collect(Collectors.toList()));
            uomField.setChoiceRenderer(new DropDownField.ChoiceRenderer());
        } catch (final CacheReloadException e) {
            LOG.error("Catched error while evaluating dimension", e);
        }
        uomField.setOutputMarkupId(true);
        add(uomField);
    }

    @Override
    public FieldConfiguration getFieldConfig()
    {
        return getModelObject().getFieldConfiguration();
    }

    @Override
    public IModel<String> getLabel()
    {
        String label = "";
        try {
            label = getModelObject().getLabel();
        } catch (final EFapsException e) {
            LOG.error("Catched error while evaluating label", e);
        }
        return Model.of(label);
    }
}
