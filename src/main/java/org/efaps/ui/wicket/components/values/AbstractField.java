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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 * @param <T> Type extending Serializable
 */
public abstract class AbstractField<T extends Serializable>
    extends TextField<T>
    implements IFieldConfig, IUIField, ILabelProvider<String>
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractField.class);

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
    private final AbstractUIField cellvalue;

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
        this.cellvalue = _model.getObject();
        this.add(new Validator<T>(this));
        setOutputMarkupId(true);
        setLabel(Model.of(this.cellvalue.getLabel()));

        if (_config.getField().hasEvents(EventType.UI_FIELD_UPDATE)) {
            final List<EventDefinition> events = _config.getField().getEvents(EventType.UI_FIELD_UPDATE);
            String eventName = "onchange";
            for (final EventDefinition event : events) {
               eventName = event.getProperty("Event") == null ? "onchange" : event.getProperty("Event");
            }
            add(new AjaxFieldUpdateBehavior(eventName, Model.of(this.cellvalue)));
        }
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
        this.cellvalue = null;
        setLabel(Model.of(_config.getLabel()));
        setOutputMarkupId(true);
    }

    /**
     * Getter method for the instance variable {@link #config}.
     *
     * @return value of instance variable {@link #config}
     */
    protected FieldConfiguration getConfig()
    {
        return this.config;
    }

    /**
     * Getter method for the instance variable {@link #cellvalue}.
     *
     * @return value of instance variable {@link #cellvalue}
     */
    @Override
    public AbstractUIField getCellvalue()
    {
        return this.cellvalue;
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("input");
        _tag.append("style", "text-align:" + getConfig().getAlign(), ";");
        _tag.put("size", getConfig().getSize());
        super.onComponentTag(_tag);
    }

    @Override
    public String getInputName()
    {
        String ret = "";
        try {
            ret = getConfig().getName();
        } catch (final EFapsException e) {
            AbstractField.LOG.error("Catched Exception on get Input Name", e);
        }
        return ret;
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
