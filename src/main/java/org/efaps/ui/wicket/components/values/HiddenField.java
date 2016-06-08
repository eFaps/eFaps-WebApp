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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class HiddenField
    extends AbstractField<String>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new hidden field.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @param _config the config
     * @throws EFapsException on error
     */
    public HiddenField(final String _wicketId,
                       final Model<AbstractUIField> _model,
                       final FieldConfiguration _config)
        throws EFapsException
    {
        this(_wicketId, (String) _model.getObject().getValue().getHiddenValue(
                        _model.getObject().getParent().getMode()), _config);
    }

    /**
     * Instantiates a new hidden field.
     *
     * @param _wicketId the wicket id
     * @param _modelValue the model value
     * @param _config the config
     * @throws EFapsException on error
     */
    public HiddenField(final String _wicketId,
                       final String _modelValue,
                       final FieldConfiguration _config)
        throws EFapsException
    {
        super(_wicketId, _config);
        setModel(Model.of(_modelValue));
        setType(String.class);
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.put("type", "hidden");
        super.onComponentTag(_tag);
    }

    @Override
    protected String[] getInputTypes()
    {
        return new String[] { "hidden" };
    }
}
