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
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.components.menutree.CallUpdateTreeMenuBehavior;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class extends a Link to work inside a content container. Used also by the
 * StructurBrowserTable. Updates the menu and the page itself.
 *
 * @author The eFaps Team
 */
public class MenuContentLink
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
    private static final Logger LOG = LoggerFactory.getLogger(MenuContentLink.class);

    /**
     * Content for this link.
     */
    private final String content;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     * @param _model model for thid component
     * @param _content the content
     */
    public MenuContentLink(final String _wicketId,
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
     * Getter method for the instance variable {@link #content}.
     *
     * @return value of instance variable {@link #content}
     */
    protected String getContent()
    {
        return this.content;
    }

    @Override
    public IModel<String> getLabel()
    {
        String ret = "NONE";
        try {
            ret = ((AbstractUIField) getDefaultModelObject()).getLabel();
        } catch (final EFapsException e) {
            MenuContentLink.LOG.error("Catched error on evaluating label: {}", this);
        }
        return Model.of(ret);
    }

    /**
     * The tag must be overwritten.
     *
     * @param _tag tag to write.
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        _tag.setName("a");
        _tag.put("href", "#");;
        onComponentTagInternal(_tag);
    }

    /**
     * Add to the tag.
     * @param _tag tag to write
     */
    protected void onComponentTagInternal(final ComponentTag _tag)
    {
        _tag.put("name", getConfig().getName());
        _tag.append("style", "text-align:" + getConfig().getAlign(), ";");
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
            MenuContentLink.LOG.error("Catched error on setting tag body for: {}", this);
        }
        replaceComponentTagBody(_markupStream, _openTag, ret == null ? "" : String.valueOf(ret));
    }

    @Override
    public void onClick()
    {
        final AbstractUIField uiField = (AbstractUIField) getDefaultModelObject();
        Instance instance = null;
        if (uiField.getInstanceKey() != null) {
            AbstractCommand menu = null;
            try {
                instance = uiField.getInstance();
                menu = Menu.getTypeTreeMenu(instance.getType());
                // CHECKSTYLE:OFF
            } catch (final Exception e) {
                throw new RestartResponseException(new ErrorPage(e));
            } // CHECKSTYLE:ON
            if (menu == null) {
                final Exception ex = new Exception("no tree menu defined for type " + instance.getType().getName());
                throw new RestartResponseException(new ErrorPage(ex));
            }

            for (final AbstractCommand childcmd : ((Menu) menu).getCommands()) {
                if (childcmd.isDefaultSelected()) {
                    menu = childcmd;
                    break;
                }
            }

            Page page;
            try {
                if (menu.getTargetTable() != null) {
                    if (menu.getTargetStructurBrowserField() == null) {
                        page = new TablePage(menu.getUUID(), uiField.getInstanceKey(), getPage().getPageReference());
                    } else {
                        page = new StructurBrowserPage(menu.getUUID(),
                                        uiField.getInstanceKey(), getPage().getPageReference());
                    }
                } else {
                    final UIForm uiForm = new UIForm(menu.getUUID(),  uiField.getInstanceKey())
                                    .setPagePosition(PagePosition.TREE);
                    page = new FormPage(Model.of(uiForm));
                }
            } catch (final EFapsException e) {
                page = new ErrorPage(e);
            }
            page.add(new CallUpdateTreeMenuBehavior(instance));
            getRequestCycle().setResponsePage(page);
        }
    }
}
