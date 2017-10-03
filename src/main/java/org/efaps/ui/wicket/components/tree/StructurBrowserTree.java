/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.components.tree;

import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.SetModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.menutree.TreeMenuModel;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * This class renders a Tree, which loads the children asynchron.<br>
 * The items of the tree consists of junction link, icon and label. An
 * additional arrow showing the direction of the child can be rendered depending
 * on a Tristate.
 *
 * @author The eFaps Team
 */
public class StructurBrowserTree
    extends NestedTree<UIStructurBrowser>
{

    /**
     * ResourceReference to the StyleSheet used for this Tree.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(StructurBrowserTree.class,
                    "StructurTree.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId WicketId of the Tree
     * @param _model Model for this Tree
     */
    public StructurBrowserTree(final String _wicketId,
                               final IModel<UIStructurBrowser> _model)
    {
        super(_wicketId, new StructurBrowserProvider(_model),
                        new SetModel<>(_model.getObject().getExpandedBrowsers()));
        if ("human".equals(Configuration.getAttribute(ConfigAttribute.STRUCTREE_CLASS))) {
            add(new HumanTheme());
        } else if ("windows".equals(Configuration.getAttribute(ConfigAttribute.STRUCTREE_CLASS))) {
            add(new WindowsTheme());
        }
    }

    @Override
    protected Component newContentComponent(final String _wicketId,
                                            final IModel<UIStructurBrowser> _model)
    {
        return new Folder<UIStructurBrowser>(_wicketId, this, _model)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component newLabelComponent(final String _id,
                                                  final IModel<UIStructurBrowser> _model)
            {
                return new ItemLink(_id, _model);
            }

            @Override
            protected boolean isClickable()
            {
                return true;
            }
        };
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(StructurBrowserTree.CSS));
    }

    /**
     * A Link component.
     */
    public class ItemLink
        extends WebMarkupContainer
        implements IRequestListener
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicketID for this component
         * @param _model    modle for this component
         */
        public ItemLink(final String _wicketId,
                        final IModel<UIStructurBrowser> _model)
        {
            super(_wicketId, _model);
            add(new ItemLinkBehavior());
        }

        @Override
        public void onComponentTagBody(final MarkupStream _markupStream,
                                       final ComponentTag _openTag)
        {
            final UIStructurBrowser uiStrBrws = (UIStructurBrowser) getDefaultModelObject();
            super.replaceComponentTagBody(_markupStream, _openTag, uiStrBrws.getLabel());
        }

        @Override
        public void onRequest()
        {
            final UIStructurBrowser uiStrBrws = (UIStructurBrowser) getDefaultModelObject();
            Page page;
            try {
                if (uiStrBrws.getCommand().getTargetTable() != null) {
                    if (uiStrBrws.getCommand().getTargetStructurBrowserField() != null) {
                        page = new StructurBrowserPage(Model.of(new UIStructurBrowser(uiStrBrws.getCommandUUID(),
                                        uiStrBrws.getInstanceKey()).setPagePosition(PagePosition.CONTENT)), getPage()
                                                        .getPageReference());
                    } else {
                        page = new TablePage(Model.of(new UITable(uiStrBrws.getCommandUUID(), uiStrBrws
                                        .getInstanceKey()).setPagePosition(PagePosition.CONTENT)), getPage()
                                                        .getPageReference());
                    }
                } else {
                    final UIForm uiForm = new UIForm(uiStrBrws.getCommandUUID(), uiStrBrws.getInstanceKey())
                                    .setPagePosition(PagePosition.CONTENT);
                    page = new FormPage(Model.of(uiForm), getPage().getPageReference());
                }
            } catch (final EFapsException e) {
                page = new ErrorPage(e);
            }
            setResponsePage(page);
        }
    }

    /**
     * Link.
     */
    private final class ItemLinkBehavior
        extends AjaxEventBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        ItemLinkBehavior()
        {
            super("click");
        }

        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            try {
                final UIStructurBrowser uiStrBrws = (UIStructurBrowser) getComponent().getDefaultModelObject();
                final MenuTree menutree = ((ContentContainerPage) getPage()).getMenuTree();
                final TreeMenuModel treeProvider = (TreeMenuModel) menutree.getProvider();
                treeProvider.setModel(uiStrBrws.getCommandUUID(), uiStrBrws.getInstanceKey());
                treeProvider.getRoots().next().setHeader(true);
                menutree.setDefault(null);
                menutree.setSelected(null);
                _target.add(menutree);
            } catch (final CacheReloadException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
        }

        @Override
        protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
        {
            super.updateAjaxAttributes(_attributes);
            final AjaxCallListener listener = new AjaxCallListener();
            final StringBuilder js = new StringBuilder();
            js.append("dijit.registry.byId(\"").append(((ContentContainerPage) getPage()).getCenterPanelId())
                .append("\").set(\"content\", dojo.create(\"iframe\", {")
                .append("\"src\": \"")
                .append(getComponent().urlForListener(new PageParameters()))
                .append("\",\"style\": \"border: 0; width: 100%; height: 99%\"")
                .append("})); ");
            listener.onAfter(js);
            _attributes.getAjaxCallListeners().add(listener);
        }
    }
}
