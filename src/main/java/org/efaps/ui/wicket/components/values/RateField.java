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

import java.math.BigDecimal;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.RateUI;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.models.field.FieldConfiguration;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class RateField
    extends FormComponentPanel<RateUI.Value>
    implements IFieldConfig, ILabelProvider<String>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Configuration for this field.
     */
    private final FieldConfiguration fieldConfiguration;

    /**
     * @param _id
     */
    public RateField(final String _wicketId,
                     final IModel<RateUI.Value> _model,
                     final FieldConfiguration _config)
    {
        super(_wicketId, _model);
        this.fieldConfiguration = _config;
        setType(RateUI.Value.class);
        setOutputMarkupId(true);
        setRequired(getFieldConfig().getField().isRequired());
        final TextField<BigDecimal> rate = new TextField<BigDecimal>("rate", Model.of(_model.getObject().getRate()))
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;



            @Override
            public String getInputName()
            {
                return getFieldConfig().getName();
            }

            @Override
            protected String[] getInputTypes()
            {
                return getInputTypes4Rate();
            }

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                onComponentTag4Rate(_tag);
                super.onComponentTag(_tag);
            }
        };

        if (_config.getField().hasEvents(EventType.UI_FIELD_UPDATE)) {
            final List<EventDefinition> events = _config.getField().getEvents(EventType.UI_FIELD_UPDATE);
            String eventName = "change";
            for (final EventDefinition event : events) {
                eventName = event.getProperty("Event") == null ? "change" : event.getProperty("Event");
            }
            rate.add(new AjaxFieldUpdateBehavior(eventName, _model, false));
        }

        add(rate);
        final TextField<Boolean> inverted = new org.apache.wicket.markup.html.form.HiddenField<Boolean>("inverted",
                        Model.of(_model.getObject().isInverted()))
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public String getInputName()
            {
                return getFieldConfig().getName() + RateUI.INVERTEDSUFFIX;
            }
        };
        add(inverted);
    }

    /**
     * Gets the input types4 rate.
     *
     * @return the input types4 rate
     */
    protected String[] getInputTypes4Rate()
    {
        return null;
    }

    /**
     * On component tag4 rate.
     *
     * @param _tag the _tag
     */
    protected void onComponentTag4Rate(final ComponentTag _tag)
    {
        //used by HiddenRateField
    }

    @Override
    public FieldConfiguration getFieldConfig()
    {
        return this.fieldConfiguration;
    }

    @Override
    public IModel<String> getLabel()
    {
        return Model.of(getFieldConfig().getLabel());
    }
}
