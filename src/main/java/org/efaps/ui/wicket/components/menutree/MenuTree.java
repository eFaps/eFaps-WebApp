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

package org.efaps.ui.wicket.components.menutree;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.tree.ITreeState;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.update.AbstractAjaxUpdateBehavior;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class MenuTree
    extends AbstractTree
{
    /**
     * Reference to icon for remove button.
     */
    public static final EFapsContentReference ICON_REMOVE = new EFapsContentReference(MenuTree.class, "Remove.gif");
    /**
     * Reference to icon for go into button.
     */
    public static final EFapsContentReference ICON_GOINTO = new EFapsContentReference(MenuTree.class, "GoInto.gif");

    /**
     * Reference to icon for go up button.
     */
    public static final EFapsContentReference ICON_GOUP = new EFapsContentReference(MenuTree.class, "GoUp.gif");

    /**
     * Reference to icon for closed child button.
     */
    public static final EFapsContentReference ICON_CHILDCLOSED = new EFapsContentReference(MenuTree.class,
                    "ChildClosed.gif");

    /**
     * Reference to icon for open child button.
     */
    public static final EFapsContentReference ICON_CHILDOPENED = new EFapsContentReference(MenuTree.class,
                    "ChildOpened.gif");

    /**
     * Reference to style sheet for the menutree.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(MenuTree.class, "MenuTree.css");

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MenuTree.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * This Instance variable holds the key which is used to retrieve a item of this ListMenuPanel from the Map in the
     * Session {@link #org.efaps.ui.wicket.EFapsSession}.
     */
    private final String menuKey;

    /**
     * Map stores the oid to node.
     */
    private final Map<String, DefaultMutableTreeNode> instanceKey2Node = new HashMap<String, DefaultMutableTreeNode>();

    /**
     * Constructor used for a new MenuTree.
     *
     * @param _wicketId     wicket id of the component
     * @param _commandUUID  uuid of the command
     * @param _oid          oid
     * @param _menukey      key to the menu
     * @param _selectCmdUUID    UUID of the selected Command
     */
    public MenuTree(final String _wicketId,
                    final UUID _commandUUID,
                    final String _oid,
                    final String _menukey,
                    final UUID _selectCmdUUID)
    {
        super(_wicketId);

        final ITreeState treestate = getTreeState();
        treestate.expandAll();

        this.menuKey = _menukey;
        final UIMenuItem model = new UIMenuItem(_commandUUID, _oid);

        setDefaultModel(new Model<Serializable>((Serializable) model.getTreeModel()));

        add(StaticHeaderContrBehavior.forCss(MenuTree.CSS));
        ((EFapsSession) getSession()).putIntoCache(this.menuKey, this);

        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) ((DefaultTreeModel) getDefaultModelObject())
                        .getRoot();

        boolean noChildSelected = true;
        final Enumeration<?> newNodes = rootNode.children();

        while (newNodes.hasMoreElements()) {
            final DefaultMutableTreeNode newNode = (DefaultMutableTreeNode) newNodes.nextElement();
            final UIMenuItem newmodel = (UIMenuItem) newNode.getUserObject();
            if (_selectCmdUUID == null) {
                if (newmodel.isDefaultSelected()) {
                    getTreeState().selectNode(newNode, true);
                    noChildSelected = false;
                }
            } else {
                if (newmodel.getCommandUUID().equals(_selectCmdUUID)) {
                    getTreeState().selectNode(newNode, true);
                    noChildSelected = false;
                }
            }
        }
        if (noChildSelected) {
            getTreeState().selectNode(rootNode, true);
        }

        final AjaxUpdateBehavior update = new AjaxUpdateBehavior();
        this.add(update);
    }

    /**
     * Constructor used from the ajax links for go into and go up.
     *
     * @param _wicketId wicket id of the component
     * @param _model model for the tree
     * @param _menukey key to the menu
     */
    protected MenuTree(final String _wicketId,
                       final TreeModel _model,
                       final String _menukey)
    {
        super(_wicketId);
        this.menuKey = _menukey;
        setDefaultModel(new Model<Serializable>((Serializable) _model));

        final ITreeState treestate = getTreeState();
        treestate.expandAll();

        add(StaticHeaderContrBehavior.forCss(MenuTree.CSS));
        ((EFapsSession) getSession()).putIntoCache(this.menuKey, this);

        final AjaxUpdateBehavior update = new AjaxUpdateBehavior();
        this.add(update);
    }

    /**
     * This is the getter method for the instance variable {@link #menuKey}.
     *
     * @return value of instance variable {@link #menuKey}
     */
    public String getMenuKey()
    {
        return this.menuKey;
    }

    /**
     * Method to add a child menu.
     * @param _commandUUID  UUID of the command
     * @param _instanceKey  instance key
     * @param _target       target
     */
    public void addChildMenu(final UUID _commandUUID,
                             final String _instanceKey,
                             final AjaxRequestTarget _target)
    {

        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) getTreeState().getSelectedNodes().iterator()
                        .next();
        final Enumeration<?> childs = node.children();
        boolean old = false;
        while (childs.hasMoreElements()) {

            final DefaultMutableTreeNode child = (DefaultMutableTreeNode) childs.nextElement();

            final UIMenuItem childmodel = (UIMenuItem) child.getUserObject();
            if (childmodel.getInstanceKey().equals(_instanceKey) && childmodel.getCommandUUID().equals(_commandUUID)) {
                getTreeState().selectNode(child, true);
                old = true;
            }
        }
        if (!old) {
            final UIMenuItem model = new UIMenuItem(_commandUUID, _instanceKey);
            final DefaultMutableTreeNode rootNode = model.getNode();
            node.add(rootNode);
            boolean noChildSelected = true;
            final Enumeration<?> newNodes = rootNode.children();
            while (newNodes.hasMoreElements()) {
                final DefaultMutableTreeNode newNode = (DefaultMutableTreeNode) newNodes.nextElement();
                final UIMenuItem newmodel = (UIMenuItem) newNode.getUserObject();
                if (newmodel.isDefaultSelected()) {
                    getTreeState().selectNode(newNode, true);
                    noChildSelected = false;
                }
            }
            if (noChildSelected) {
                getTreeState().selectNode(rootNode, true);
            }
        }
        getTreeState().expandNode(node);
        updateTree(_target);
    }

    /**
     * Populates the tree item. It creates all necessary components for the tree to work properly.
     *
     * @param _item item to populate
     * @param _level level of the item
     */
    @Override
    protected void populateTreeItem(final WebMarkupContainer _item,
                                    final int _level)
    {

        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) _item.getDefaultModelObject();

        final UIMenuItem model = (UIMenuItem) node.getUserObject();

        // mark the item as selected/not selected
        _item.add(new Behavior()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTag(final Component _component,
                                       final ComponentTag _tag)
            {
                super.onComponentTag(_component, _tag);
                _tag.put("title", model.getLabel());
                if (getTreeState().isNodeSelected(node)) {
                    _tag.put("class", "eFapsMenuTreeRowSelected");
                } else {
                    _tag.put("class", "eFapsMenuTreeRow");
                }
            }
        });

        // if we have a header store it to be accessible through the oid
        if (model.isHeader()) {
            ((EFapsSession) getSession()).addUpdateBehaviors(model.getInstanceKey(), getBehaviors(
                            AjaxUpdateBehavior.class).get(0));
            this.instanceKey2Node.put(model.getInstanceKey(), node);
        }

        _item.add(new Indentation("intend", _level));

        final AjaxMenuTreeLink link = new AjaxMenuTreeLink("link", node);
        _item.add(link);

        final Label label = new Label("label", model.getLabel());
        link.add(label);

        if (node.children().hasMoreElements() && !node.isRoot() && !model.isStepInto()) {

            final AjaxExpandLink expandLink = new AjaxExpandLink("expandLink", node);
            _item.add(expandLink);

            if (getTreeState().isNodeExpanded(node)) {
                expandLink.add(new StaticImageComponent("expandIcon", MenuTree.ICON_CHILDOPENED));
            } else {
                expandLink.add(new StaticImageComponent("expandIcon", MenuTree.ICON_CHILDCLOSED));
            }
        } else {
            _item.add(new WebMarkupContainer("expandLink").setVisible(false));
        }

        if (model.isHeader()) {
            label.add(AttributeModifier.append("class", "eFapsMenuTreeHeader"));

            String imageUrl = model.getImage();
            if (imageUrl == null) {
                try {
                    imageUrl = model.getTypeImage();
                } catch (final EFapsException e) {
                    MenuTree.LOG.error("Error on retrieving the image for a image: {}", model.getImage());
                }
            }
            if (imageUrl == null) {
                link.add(new WebMarkupContainer("icon").setVisible(false));
            } else {
                link.add(new StaticImageComponent("icon", imageUrl));
            }

            if (node.isRoot()) {
                _item.add(new WebMarkupContainer("goIntolink").setVisible(false));
                _item.add(new WebMarkupContainer("removelink").setVisible(false));
                _item.add(new WebMarkupContainer("goUplink").setVisible(false));
            } else if (model.isStepInto()) {
                _item.add(new WebMarkupContainer("goIntolink").setVisible(false));
                _item.add(new WebMarkupContainer("removelink").setVisible(false));
                final AjaxGoUpLink goUplink = new AjaxGoUpLink("goUplink", node);
                _item.add(goUplink);
                goUplink.add(new StaticImageComponent("goUpIcon", MenuTree.ICON_GOUP));
            } else {
                final AjaxGoIntoLink goIntolink = new AjaxGoIntoLink("goIntolink", node);
                _item.add(goIntolink);
                goIntolink.add(new StaticImageComponent("goIntoIcon", MenuTree.ICON_GOINTO));

                final AjaxRemoveLink removelink = new AjaxRemoveLink("removelink", node);
                _item.add(removelink);
                removelink.add(new StaticImageComponent("removeIcon", MenuTree.ICON_REMOVE));
                _item.add(new WebMarkupContainer("goUplink").setVisible(false));
            }
        } else {
            label.add(AttributeModifier.append("class", "eFapsMenuTreeItem"));
            link.add(new WebMarkupContainer("icon").setVisible(false));
            _item.add(new WebMarkupContainer("goIntolink").setVisible(false));
            _item.add(new WebMarkupContainer("removelink").setVisible(false));
            _item.add(new WebMarkupContainer("goUplink").setVisible(false));
        }
    }

    /**
     * Method to change the content of the page.
     *
     * @param _model Model of the item
     * @param _target Target
     */
    public void changeContent(final UIMenuItem _model,
                              final AjaxRequestTarget _target)
    {
        InlineFrame page = null;
        if (_model.getCommand().getTargetTable() != null) {

            page = new InlineFrame(ContentContainerPage.IFRAME_WICKETID, new IPageLink() {

                private static final long serialVersionUID = 1L;

                public Page getPage()
                {
                    Page ret;
                    try {
                        if (_model.getCommand().getTargetStructurBrowserField() != null) {
                            ret = new StructurBrowserPage(_model.getCommandUUID(), _model.getInstanceKey());
                        } else {
                            ret = new TablePage(_model.getCommandUUID(), _model.getInstanceKey())
                                            .setMenuTreeKey(getMenuKey());
                        }
                    } catch (final EFapsException e) {
                        ret = new ErrorPage(e);
                    }
                    return ret;
                }

                public Class<TablePage> getPageIdentity()
                {
                    return TablePage.class;
                }
            });
        } else {
            page = new InlineFrame(ContentContainerPage.IFRAME_WICKETID, new IPageLink() {

                private static final long serialVersionUID = 1L;

                public Page getPage()
                {
                    Page page;
                    try {
                        page = new FormPage(_model.getCommandUUID(), _model.getInstanceKey());
                        ((FormPage) page).setMenuTreeKey(getMenuKey());
                    } catch (final EFapsException e) {
                        page = new ErrorPage(e);
                    }
                    return page;
                }

                public Class<FormPage> getPageIdentity()
                {
                    return FormPage.class;
                }
            });
        }

        final InlineFrame component = (InlineFrame) getPage().get(((ContentContainerPage) getPage()).getInlinePath());
        page.setOutputMarkupId(true);

        component.replaceWith(page);
        _target.add(page.getParent());

    }

    /**
     * Class is used to produce indentations.
     *
     */
    public class Indentation
        extends WebMarkupContainer
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Stores the level of indentation.
         */
        private final int level;

        /**
         * Constructor.
         *
         * @param _wicketId wicket id of the component
         * @param _level level of indentation
         */
        public Indentation(final String _wicketId,
                           final int _level)
        {
            super(_wicketId);
            this.level = _level;
            setRenderBodyOnly(true);
        }

        /**
         * Render the indentation.
         *
         * @param _markupStream markup stream
         * @param _openTag open Tag
         *
         */
        @Override
        public void onComponentTagBody(final MarkupStream _markupStream,
                                        final ComponentTag _openTag)
        {
            final Response response = RequestCycle.get().getResponse();
            for (int i = this.level - 1; i >= 0; --i) {
                response.write("<td class=\"eFapsMenuTreeIntend\"><div></div></td>");
            }
        }
    }

    /**
     * Behavior to update the menu tree via ajax.
     *
     */
    public class AjaxUpdateBehavior
        extends AbstractAjaxUpdateBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Respond doing the update.
         *
         * @param _target ajax target
         */
        @Override
        protected void respond(final AjaxRequestTarget _target)
        {
            final DefaultMutableTreeNode node = MenuTree.this.instanceKey2Node.get(getInstanceKey());
            if (node != null) {
                final DefaultTreeModel treemodel = (DefaultTreeModel) getComponent().getDefaultModel().getObject();
                final UIMenuItem model = (UIMenuItem) node.getUserObject();
                final MenuTree tree = (MenuTree) getComponent();
                if (getMode() == TargetMode.EDIT) {
                    model.requeryLabel();
                    treemodel.nodeChanged(node);
                    tree.updateTree(_target);
                }
            }
        }

        /**
         * Method to get the ajax callback.
         *
         * @return String containing the script
         */
        @Override
        public String getAjaxCallback()
        {
            String ret = "";
            if (getMode() == TargetMode.EDIT) {
                ret = getCallbackScript().toString();
            }
            return ret;
        }
    }
}
