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

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.api.ci.UIFormFieldProperty;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.field.validators.StandartValidator;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @param <T> Type extending Serializable
 */
public abstract class AbstractField<T extends Serializable>
    extends TextField<T>
    implements IFieldConfig, IUIField, ILabelProvider<String>
{
    /**
     * Needed for serialziation.
     */
    private static final long serialVersionUID = 1L;

    /**
     * FieldConfiguration for this field.
     */
    private final FieldConfiguration config;

    /**
     * value of this field.
     */
    private final AbstractUIField uiField;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _config Config
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public AbstractField(final String _wicketId,
                         final Model<AbstractUIField> _model,
                         final FieldConfiguration _config)
        throws EFapsException
    {
        super(_wicketId, Model.of((T) _model.getObject().getValue().getEditValue(
                        _model.getObject().getParent().getMode())));
        this.config = _config;
        this.uiField = _model.getObject();
        this.add(new StandartValidator<>(this));
        setOutputMarkupId(true);
        setLabel(Model.of(this.uiField.getLabel()));

        if (_config.getField().hasEvents(EventType.UI_FIELD_UPDATE)) {
            final List<EventDefinition> events = _config.getField().getEvents(EventType.UI_FIELD_UPDATE);
            String eventName = "change";
            for (final EventDefinition event : events) {
                eventName = event.getProperty("Event") == null ? "change" : event.getProperty("Event");
            }
            add(new AjaxFieldUpdateBehavior(eventName, Model.of(this.uiField), false));
        }
        // only if explecitely set and not part of a table set the with here
        if (getFieldConfig().hasProperty(UIFormFieldProperty.WIDTH.value())
                        && !(_model.getObject().getParent() instanceof UITable)) {
            add(new AttributeAppender("style", "width:" + getFieldConfig().getWidth(), ";"));
        }
        add(new AttributeAppender("style", "text-align:" + getFieldConfig().getAlign(), ";"));
    }

    /**
     * @param _wicketId wicket id for this component
     * @param _config Config
     */
    public AbstractField(final String _wicketId,
                         final FieldConfiguration _config)
    {
        super(_wicketId, Model.<T>of());
        this.config = _config;
        this.uiField = null;
        setLabel(Model.of(_config.getLabel()));
        setOutputMarkupId(true);
        setType(String.class);
    }

    /**
     * Getter method for the instance variable {@link #cellvalue}.
     *
     * @return value of instance variable {@link #cellvalue}
     */
    @Override
    public AbstractUIField getUIField()
    {
        return this.uiField;
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("input");
        if (getFieldConfig().hasProperty(UIFormFieldProperty.COLUMNS.value())) {
            _tag.put("maxlength", getFieldConfig().getProperty(UIFormFieldProperty.COLUMNS.value()));
        }
        if (getInputTypes() != null) {
            _tag.put("type", getInputTypes()[0]);
        }
        super.onComponentTag(_tag);
    }

    @Override
    public String getInputName()
    {
        return getFieldConfig().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfiguration getFieldConfig()
    {
        return this.config;
    }
}
