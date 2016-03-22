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

package org.efaps.ui.wicket.components.links;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class extends a Link open a new content container. Used also by the
 * StructurBrowserTable.
 *
 * @author The eFaps Team
 */
public class ContentContainerLink
    extends Link<AbstractUIField>
    implements ILabelProvider<String>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ContentContainerLink.class);

    /**
     * Content for this link.
     */
    private final String content;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     * @param _model model fore this component
     */
    public ContentContainerLink(final String _wicketId,
                                final IModel<AbstractUIField> _model,
                                final String _content)
    {
        super(_wicketId, _model);
        this.content = _content;
    }

    /**
     * Getter method for the instance variable {@link #config}.
     *
     * @return value of instance variable {@link #config}
     */
    protected FieldConfiguration getConfig()
    {
        return ((AbstractUIField) getDefaultModelObject()).getFieldConfiguration();
    }

    /**
     * The tag must be overwritten.
     *
     * @param _tag tag to write.
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("a");
        super.onComponentTag(_tag);
        onComponentTagInternal(_tag);
    }

    /**
     * Add to the tag.
     * @param _tag tag to write
     */
    protected void onComponentTagInternal(final ComponentTag _tag)
    {
        try {
            _tag.put("name", getConfig().getName());
            _tag.append("style", "text-align:" + getConfig().getAlign(), ";");
        } catch (final EFapsException e) {
            LOG.error("Catched error on setting name in tag for: {}", this);
        }
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        Object ret = null;
        try {
            if (this.content == null) {
                final AbstractUIField uiField = (AbstractUIField) getDefaultModelObject();
                ret = uiField.getValue().getReadOnlyValue(uiField.getParent().getMode());
            } else {
                ret = this.content;
            }
        } catch (final EFapsException e) {
            LOG.error("Catched error on setting tag body for: {}", this);
        }
        replaceComponentTagBody(_markupStream, _openTag, ret == null ? "" : String.valueOf(ret));
    }

    @Override
    public IModel<?> getBody()
    {
        String body ="";
        try {
            if (this.content == null) {
                final AbstractUIField uiField = (AbstractUIField) getDefaultModelObject();
                body = String.valueOf( uiField.getValue().getReadOnlyValue(uiField.getParent().getMode()));
            } else {
                body = this.content;
            }
        } catch (final EFapsException e) {
            LOG.error("Catched error on setting tag body for: {}", this);
        }
        return Model.of(body);
    }

    /**
     * Method is executed on click.
     */
    @Override
    public void onClick()
    {
        Instance instance = null;
        final AbstractUIField uiField = super.getModelObject();
        if (uiField.getInstanceKey() != null) {
            Menu menu = null;
            try {
                instance = uiField.getInstance();
                menu = Menu.getTypeTreeMenu(instance.getType());
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
            if (menu == null) {
                final Exception ex = new Exception("no tree menu defined for type " + instance.getType().getName());
                throw new RestartResponseException(new ErrorPage(ex));
            }

            Page page;
            try {
                page = new ContentContainerPage(menu.getUUID(), uiField.getInstanceKey(),
                                getPage() instanceof StructurBrowserPage);
            } catch (final EFapsException e) {
                page = new ErrorPage(e);
            }
            this.setResponsePage(page);
        }
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
}
