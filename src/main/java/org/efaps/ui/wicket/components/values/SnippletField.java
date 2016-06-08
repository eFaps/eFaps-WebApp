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


import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxAttributeName;
import org.apache.wicket.ajax.json.JSONException;
import org.apache.wicket.ajax.json.JSONObject;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.api.ui.IUserInterface;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class SnippletField
    extends Label
    implements ILabelProvider<String>, IFieldConfig
{

    /**
     * Neede for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The label for this Field.
     */
    private final IModel<String> label;

    /** The field config. */
    private final FieldConfiguration fieldConfig;

    /**
     * Instantiates a new snipplet field.
     *
     * @param _wicketId wicket if of this component
     * @param _model model for this component
     * @param _labelModel label for this component
     * @param _uiField the ui field
     */
    public SnippletField(final String _wicketId,
                         final IModel<String> _model,
                         final IModel<String> _labelModel,
                         final AbstractUIField _uiField)
    {
        super(_wicketId, _model);
        setEscapeModelStrings(false);
        this.label = _labelModel;
        this.fieldConfig = _uiField == null ? null : _uiField.getFieldConfiguration();
        if (_uiField != null && _uiField.isFieldUpdate()) {
            final List<EventDefinition> events = _uiField.getFieldConfiguration().getField().getEvents(
                            EventType.UI_FIELD_UPDATE);
            String eventName = "change";
            for (final EventDefinition event : events) {
                eventName = event.getProperty("Event") == null ? "change" : event.getProperty("Event");
            }
            final String html = (String) getDefaultModelObject();

            final String tmpId;
            if (html.contains(IUserInterface.EFAPSTMPTAG)) {
                tmpId = RandomStringUtils.randomAlphanumeric(12);
                setDefaultModelObject(html.replace(IUserInterface.EFAPSTMPTAG, " id=\"" + tmpId + "\" "));
            } else {
                tmpId = null;
            }
            add(new AjaxFieldUpdateBehavior(eventName, Model.of(_uiField), false)
            {
                /** The Constant serialVersionUID. */
                private static final long serialVersionUID = 1L;

                @Override
                protected void postprocessConfiguration(final JSONObject _attributesJson,
                                                        final Component _component)
                    throws JSONException
                {
                    if (tmpId != null) {
                        _attributesJson.put(AjaxAttributeName.MARKUP_ID.jsonName(), tmpId);
                    }
                }
            });
        }
    }

    @Override
    public IModel<String> getLabel()
    {
        return this.label;
    }

    @Override
    protected void onBeforeRender()
    {
        super.onBeforeRender();
        for (final Behavior behavior : getBehaviors()) {
            if (behavior instanceof IFormValidator) {
                final Form<?> form = findParent(Form.class);
                form.add(behavior);
            }
        }
    }

    @Override
    public FieldConfiguration getFieldConfig()
    {
        return this.fieldConfig;
    }
}
