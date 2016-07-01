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
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class LabelField
    extends Label
    implements ILabelProvider<String>
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Configuration object.
     */
    private final FieldConfiguration fieldConfiguration;

    /**
     * Label for this field.
     */
    private final String label;

    /**
     * Instantiates a new label field.
     *
     * @param _wicketId the wicket id
     * @param _readOnlyValue the read only value
     * @param _uiField the ui field
     * @throws EFapsException on error
     */
    public LabelField(final String _wicketId,
                      final Model<?> _readOnlyValue,
                      final AbstractUIField _uiField)
        throws EFapsException
    {
        this(_wicketId, _readOnlyValue, _uiField.getFieldConfiguration(), _uiField.getLabel());
    }

    /**
     * @param _wicketId wicketid
     * @param _readOnlyValue read only value
     * @param _fieldConfiguration FieldConfiguration for this labelField
     * @param _label label for the Field
     */
    public LabelField(final String _wicketId,
                      final Model<?> _readOnlyValue,
                      final FieldConfiguration _fieldConfiguration,
                      final String _label)
    {
        super(_wicketId, _readOnlyValue);
        this.fieldConfiguration = _fieldConfiguration;
        this.label = _label;
    }

    @Override
    public IModel<String> getLabel()
    {
        return Model.of(this.label);
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        _tag.put("name", this.fieldConfiguration.getName());
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        final String content = getDefaultModelObjectAsString();
        if (content.contains("\n")) {
            final CharSequence body = Strings.toMultilineMarkup(content);
            replaceComponentTagBody(_markupStream, _openTag, body);
        } else {
            replaceComponentTagBody(_markupStream, _openTag, content);
        }
    }
}
