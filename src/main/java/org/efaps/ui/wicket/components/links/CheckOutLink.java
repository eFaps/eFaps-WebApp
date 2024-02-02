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
package org.efaps.ui.wicket.components.links;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class CheckOutLink
    extends WebMarkupContainer
    implements ILabelProvider<String>
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CheckOutLink.class);

    /** The content. */
    private final String content;

    /**
     * Instantiates a new check out link.
     *
     * @param _wicketId the _wicket id
     * @param _model the _model
     * @param _content the _content
     */
    public CheckOutLink(final String _wicketId,
                        final IModel<AbstractUIField> _model,
                        final String _content)
    {
        super(_wicketId, _model);
        this.content = _content;
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("a");
        super.onComponentTag(_tag);
        final AbstractUIField uiField = (AbstractUIField) getDefaultModelObject();
        try {
            final StringBuilder href = new StringBuilder();
            href.append("../servlet/checkout?").append("oid=").append(uiField.getInstance().getOid());
            _tag.put("href", href);
            _tag.put("target", "_blank");
        } catch (final EFapsException e) {
            LOG.error("Catched EFapsException", e);
        }

    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        Object contentValue = null;
        try {
            if (this.content == null) {
                final AbstractUIField uiField = (AbstractUIField) getDefaultModelObject();
                contentValue = uiField.getValue().getReadOnlyValue(uiField.getParent().getMode());
            } else {
                contentValue = this.content;
            }
        } catch (final EFapsException e) {
            LOG.error("Catched error on setting tag body for: {}", this);
        }
        replaceComponentTagBody(_markupStream, _openTag, contentValue == null ? "" : String.valueOf(contentValue));
    }

    @Override
    public IModel<String> getLabel()
    {
        String ret = "NONE";
        try {
            ret = ((AbstractUIField) getDefaultModelObject()).getLabel();
        } catch (final EFapsException e) {
            LOG.error("Catched error on evaluating label: {}", this);
        }
        return Model.of(ret);
    }

    /**
     * Getter method for the instance variable {@link #content}.
     *
     * @return value of instance variable {@link #content}
     */
    protected String getContent()
    {
        return this.content;
    }
}
