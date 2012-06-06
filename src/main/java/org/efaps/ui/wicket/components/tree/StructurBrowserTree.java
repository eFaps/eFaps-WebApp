/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.ui.wicket.components.tree;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.SetModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.menutree.TreeMenuModel;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.util.EFapsException;

/**
 * This class renders a Tree, which loads the children asynchron.<br>
 * The items of the tree consists of junction link, icon and label. An
 * additional arrow showing the direction of the child can be rendered depending
 * on a Tristate.
 *
 * @author The eFaps Team
 * @version $Id: StructurBrowserTree.java 7556 2012-05-29 19:51:07Z
 *          jan@moxter.net $
 */
public class StructurBrowserTree
    extends NestedTree<UIStructurBrowser>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ResourceReference to the StyleSheet used for this Tree.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(StructurBrowserTree.class,
                    "StructurTree.css");

    /**
     * @param _wicketId WicketId of the Tree
     * @param _model    Model for this Tree
     */
    public StructurBrowserTree(final String _wicketId,
                               final IModel<UIStructurBrowser> _model)
    {
        super(_wicketId, new StructurBrowserProvider(_model),
                        new SetModel<UIStructurBrowser>(_model.getObject().getExpandedBrowsers()));
        add(new WindowsTheme());
        add(StaticHeaderContrBehavior.forCss(StructurBrowserTree.CSS));
    }

    /*
     * (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree#
     * newContentComponent(java.lang.String, org.apache.wicket.model.IModel)
     */
    @Override
    protected Component newContentComponent(final String _wicketId,
                                            final IModel<UIStructurBrowser> _model)
    {
        // TODO Auto-generated method stub
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

    public class ItemLink
        extends WebMarkupContainer
        implements ILinkListener
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _id
         * @param _model
         */
        public ItemLink(final String _id,
                        final IModel<UIStructurBrowser> _model)
        {
            super(_id, _model);
            add(new ItemLinkBehavior());
        }

        @Override
        public void onComponentTagBody(final MarkupStream _markupStream,
                                       final ComponentTag _openTag)
        {
            final UIStructurBrowser uiStrBrws = (UIStructurBrowser) getDefaultModelObject();
            super.replaceComponentTagBody(_markupStream, _openTag, uiStrBrws.getLabel());
        }

        /*
         * (non-Javadoc)
         * @see org.apache.wicket.markup.html.link.ILinkListener#onLinkClicked()
         */
        @Override
        public void onLinkClicked()
        {
            final UIStructurBrowser uiStrBrws = (UIStructurBrowser) getDefaultModelObject();
            Page page;
            try {
                if (uiStrBrws.getCommand().getTargetTable() != null) {
                    if (uiStrBrws.getCommand().getTargetStructurBrowserField() != null) {
                        page = new StructurBrowserPage(uiStrBrws.getCommandUUID(),
                                        uiStrBrws.getInstanceKey(), getPage()
                                                  .getPageReference());
                    } else {
                        page = new TablePage(uiStrBrws.getCommandUUID(), uiStrBrws.getInstanceKey(), getPage()
                                        .getPageReference());
                    }
                } else {
                    page = new FormPage(uiStrBrws.getCommandUUID(), uiStrBrws.getInstanceKey(), getPage()
                                    .getPageReference());
                }
            } catch (final EFapsException e) {
                page = new ErrorPage(e);
            }
            setResponsePage(page);
        }
    }

    private final class ItemLinkBehavior
        extends AjaxEventBehavior
    {

        private static final long serialVersionUID = 1L;

        public ItemLinkBehavior()
        {
            super("onclick");
        }

        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final UIStructurBrowser uiStrBrws = (UIStructurBrowser) getComponent().getDefaultModelObject();
            final MenuTree menutree = ((ContentContainerPage) getPage()).getMenuTree();
            final TreeMenuModel treeProvider = (TreeMenuModel) menutree.getProvider();
            treeProvider.setModel(uiStrBrws.getCommandUUID(), uiStrBrws.getInstanceKey());
            treeProvider.getRoots().next().setHeader(true);
            menutree.setDefault(null);
            menutree.setSelected(null);
            _target.add(menutree);
        }

        /*
         * (non-Javadoc)
         * @see
         * org.apache.wicket.ajax.AjaxEventBehavior#updateAjaxAttributes(org
         * .apache.wicket.ajax.attributes.AjaxRequestAttributes)
         */
        @Override
        protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
        {
            super.updateAjaxAttributes(_attributes);
            final AjaxCallListener listener = new AjaxCallListener();
            final StringBuilder js = new StringBuilder();
            js.append("dijit.byId(\"").append(((ContentContainerPage) getPage()).getCenterPanelId())
                .append("\").set(\"content\", dojo.create(\"iframe\", {")
                .append("\"src\": \"")
                .append(getComponent().urlFor(ILinkListener.INTERFACE, new PageParameters()))
                .append("\",\"style\": \"border: 0; width: 100%; height: 100%\"")
                .append("})); ");
            listener.onAfter(js);
            _attributes.getAjaxCallListeners().add(listener);
        }
    }
}
