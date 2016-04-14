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

import org.apache.wicket.PageReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.RecentObjectLink;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class is used as link to load a page from an popup window inside the opener window.
 *
 * @author The eFaps Team
 */
public class LoadInTargetAjaxLink
    extends AjaxLink<AbstractUIField>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoadInTargetAjaxLink.class);

    /**
     * target used for the script.
     */
    public enum ScriptTarget
    {
        /** top. */
        TOP("top"),
        /** opener. */
        OPENER("opener.top");

        /***/
        private String key;
        /**
         * @param _key key
         */
        ScriptTarget(final String _key)
        {
            this.key = _key;
        }
    }

    /**
     * target for this link.
     */
    private final ScriptTarget target;

    /** The content. */
    private final String content;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id for this component
     * @param _model    model for this component
     * @param _content the content
     * @param _target   target for this link.
     */
    public LoadInTargetAjaxLink(final String _wicketId,
                                final IModel<AbstractUIField> _model,
                                final String _content,
                                final ScriptTarget _target)
    {
        super(_wicketId, _model);
        this.target = _target;
        this.content = _content;
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
        _tag.put("href", "#");
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

    /**
     * Method to load something inside the opener window.
     *
     * @param _target AjaxRequestTarget
     */
    @Override
    @SuppressWarnings("checkstyle:illegalcatch")
    public void onClick(final AjaxRequestTarget _target)
    {
        Instance instance = null;
        try {
            if (this.target.equals(ScriptTarget.TOP)) {
                final PageReference reference = ((AbstractContentPage) getPage()).getCalledByPageReference();
                if (reference != null) {
                    final UIMenuItem menuItem = (UIMenuItem) ((ContentContainerPage) reference.getPage()).getMenuTree()
                                    .getSelected().getDefaultModelObject();
                    final RecentObjectLink link = new RecentObjectLink(menuItem);
                    if (link != null) {
                        ((EFapsSession) getSession()).addRecent(link);
                    }
                }
            }
            final AbstractUIField uiField = super.getModelObject();
            if (uiField.getInstanceKey() != null) {
                Menu menu = null;
                try {
                    instance = uiField.getInstance();
                    menu = Menu.getTypeTreeMenu(instance.getType());
                } catch (final Exception e) {
                    if (menu == null) {
                        throw new EFapsException(LoadInTargetAjaxLink.class, "NoTreeMenu", instance);
                    }
                }

                final ContentContainerPage page = new ContentContainerPage(menu.getUUID(), uiField.getInstanceKey(),
                                uiField.getParent() instanceof UIStructurBrowser);
                final CharSequence url = urlFor(new RenderPageRequestHandler(new PageProvider(page)));
                // touch the page to ensure that the pagemanager stores it to be accessible
                getSession().getPageManager().touchPage(page);
                final StringBuilder js = new StringBuilder()
                    .append(this.target.key).append(".dijit.registry.byId(\"").append("mainPanel")
                    .append("\").set(\"content\", dojo.create(\"iframe\",{")
                    .append("\"id\": \"").append(MainPage.IFRAME_ID)
                    .append("\",\"src\": \"./wicket/").append(url)
                    .append("\",\"style\": \"border: 0; width: 100%; height: 99%\"")
                    .append("}));");
                _target.appendJavaScript(js);
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }
}

