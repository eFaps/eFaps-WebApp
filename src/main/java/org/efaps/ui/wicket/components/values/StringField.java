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

import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.Model;
import org.drools.core.util.StringUtils;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.api.ci.UIFormFieldProperty;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.field.set.UIFieldSet;
import org.efaps.ui.wicket.models.field.set.UIFieldSetValue;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class StringField
    extends AbstractField<String>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StringField.class);

    /**
     * Was the value already converted.
     */
    private boolean converted = false;

    /**
     * @param _wicketId wicket id for this component
     * @param _model    model for this componet
     * @param _config   Config
     * @throws EFapsException on error
     */
    public StringField(final String _wicketId,
                       final Model<AbstractUIField> _model,
                       final FieldConfiguration _config)
        throws EFapsException
    {
        super(_wicketId, _model, _config);
        setRequired(_config.getField().isRequired());
        setType(String.class);
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        final int rows = getFieldConfig().getRows();
        if (rows > 1) {
            _tag.setName("textarea");
            _tag.put("rows", rows);
            if (getFieldConfig().hasProperty(UIFormFieldProperty.COLUMNS)) {
                _tag.put("cols", getFieldConfig().getProperty(UIFormFieldProperty.COLUMNS));
                _tag.remove("maxlength");
            }
        }
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        if ("textarea".equals(_openTag.getName())) {
            final String body = StringEscapeUtils.escapeHtml4(_openTag.getAttribute("value"));
            replaceComponentTagBody(_markupStream, _openTag, body);
            _openTag.remove("value");
        } else {
            super.onComponentTagBody(_markupStream, _openTag);
        }
    }

    @Override
    protected String getModelValue()
    {
        String ret = super.getModelValue();
        if (StringUtils.isEmpty(ret) && TargetMode.SEARCH.equals(getUIField().getParent().getMode())) {
            ret = "*";
        }
        return ret;
    }

    @Override
    public void convertInput()
    {
        this.converted = true;
        int i = 0;
        if (getUIField() instanceof UIFieldSetValue) {
            final UIFieldSet cellset = ((UIFieldSetValue) getUIField()).getCellSet();
            i = cellset.getIndex(getInputName());
        }
        final String[] value = getInputAsArray();
        setConvertedInput(value != null && value.length > 0 && value[i] != null ? trim(value[i]) : null);
    }

    @Override
    public void updateModel()
    {
        if (!this.converted) {
            convertInput();
        }
        setModelObject(getConvertedInput());
        try {
            getUIField().setValue(UIValue.get(getUIField().getValue().getField(), getUIField().getValue()
                            .getAttribute(), getDefaultModelObject()));
        } catch (final CacheReloadException e) {
            StringField.LOG.error("Catched error on updateModel", e);
        }
    }
}

