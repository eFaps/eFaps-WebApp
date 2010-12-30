/*
 * Copyright 2003 - 2010 The eFaps Team
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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.behaviors.AbstractAjaxCallBackBehavior;
import org.efaps.ui.wicket.models.cell.UIStructurBrowserTableCell;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.util.EFapsException;

/**
 * This class renders a TreeTable, which loads the children asynchron.<br>
 * The items of the tree consists of junction link, icon and label. An
 * additional arrow showing the direction of the child can be rendered
 * depending on a Tristate. The table shows the columns as defined in the model.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StructurBrowserTreeTable
    extends TreeTable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ResourceReference to the StyleSheet used for this TreeTable.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(StructurBrowserTreeTable.class,
                    "StructurTreeTable.css");

    /**
     * Must the link update the parent in the link.
     */
    private final boolean parentLink;

    /**
     * Constructor.
     *
     * @param _wicketId     wicket id for this component
     * @param _model        model
     * @param _columns      columns
     * @param _parentLink   must the link be done over the parent
     */
    public StructurBrowserTreeTable(final String _wicketId,
                                    final IModel<UIStructurBrowser> _model,
                                    final IColumn[] _columns,
                                    final boolean _parentLink)
    {
        super(_wicketId, _model.getObject().getTreeModel(), _columns);
        this.add(StaticHeaderContributor.forCss(StructurBrowserTreeTable.CSS));
        this.parentLink = _parentLink;

        setRootLess(!(_model.getObject().isCreateMode() || _model.getObject().isEditMode()));

        final ITreeState treeState = getTreeState();

        treeState.addTreeStateListener(new AsyncronTreeUpdateListener());

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModelObject().getRoot();
        if (!isRootLess() && _model.getObject().isForceExpanded()) {
            getTreeState().expandNode(root);
        }
        expandChildren(root);
    }

    /**
     * Recursive method that expands all children that should be expanded.
     *
     * @param _parent parent
     */
    private void expandChildren(final DefaultMutableTreeNode _parent)
    {
        for (int i = 0; i < _parent.getChildCount(); i++) {
            final DefaultMutableTreeNode child = (DefaultMutableTreeNode) _parent.getChildAt(i);
            if (!(child instanceof UIStructurBrowser.BogusNode)) {
                final UIStructurBrowser struturBrowser = (UIStructurBrowser) child.getUserObject();
                if (struturBrowser.isExpanded()) {
                    getTreeState().expandNode(child);
                    expandChildren(child);
                }
            }
        }
    }

    /**
     * @return null
     */
    @Override
    protected ResourceReference getCSS()
    {
        // return null here and set a own HeaderContributor, to use eFaps own
        // CSSResourceReference
        return null;
    }

    /**
     * Method is called to create a new Icon.
     *
     * @param _parent parent node
     * @param _wicketId wicket id for the new node
     * @param _node related TreeNode
     * @return Component
     */
    @Override
    protected Component newNodeIcon(final MarkupContainer _parent,
                                    final String _wicketId,
                                    final TreeNode _node)
    {
        final UIStructurBrowser model = (UIStructurBrowser) ((DefaultMutableTreeNode) _node).getUserObject();
        Component ret;
        if (model.getImage() == null) {
            ret = super.newNodeIcon(_parent, _wicketId, _node);
        } else {
            ret = new WebMarkupContainer(_wicketId)
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(final ComponentTag _tag)
                {
                    super.onComponentTag(_tag);
                    _tag.put("style", "background-image: url('" + model.getImage() + "')");
                }
            };
        }
        return ret;
    }

    /**
     * Method to add a new Fragment.
     *
     * @param _parent parent node
     * @param _wicketId wicket id for the new node
     * @param _node related TreeNode
     * @param _level level
     * @param _nodeCallback callback
     * @return Component
     */
    @Override
    protected Component newTreePanel(final MarkupContainer _parent,
                                     final String _wicketId,
                                     final TreeNode _node,
                                     final int _level,
                                     final IRenderNodeCallback _nodeCallback)
    {
        return new StructurBrowserTreeFragment(_wicketId, _node, _level, _nodeCallback);
    }

    /**
     * Method creates a new node .
     *
     * @param _parent parent node
     * @param _wicketId wicket id for the new node
     * @param _node related TreeNode
     * @return MarkupContainer
     */
    @Override
    protected MarkupContainer newNodeLink(final MarkupContainer _parent,
                                          final String _wicketId,
                                          final TreeNode _node)
    {
        MarkupContainer ret;
        if (this.parentLink) {
            ret = new WebMarkupContainer(_wicketId)
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(final ComponentTag _tag)
                {
                    _tag.setName("a");
                    _tag.put("class", "node-link");
                    _tag.put("href", "#");
                    super.onComponentTag(_tag);
                }

                @Override
                protected void onBeforeRender()
                {
                    this.add(new AjaxParentCallBackBehavior(_node));
                    super.onBeforeRender();
                }
            };
        } else {

            ret = newLink(_parent, _wicketId, new ILinkCallback()
            {

                private static final long serialVersionUID = 1L;

                public void onClick(final AjaxRequestTarget _target)
                {
                    Instance instance = null;
                    final UIStructurBrowser model = (UIStructurBrowser) ((DefaultMutableTreeNode) _node)
                                    .getUserObject();

                    if (model.getInstanceKey() != null) {
                        Menu menu = null;
                        try {
                            instance = model.getInstance();
                            menu = Menu.getTypeTreeMenu(instance.getType());
                        } catch (final EFapsException e) {
                            throw new RestartResponseException(new ErrorPage(e));
                        }
                        if (menu == null) {
                            final EFapsException excep = new EFapsException(this.getClass(), "newNodeLink.noTreeMenu",
                                            instance.getType().getName());
                            throw new RestartResponseException(new ErrorPage(excep));
                        }
                        Page page;
                        try {
                            if (model.getTarget() == Target.POPUP) {
                                page = new ContentContainerPage(menu.getUUID(), model.getInstanceKey());
                            } else {
                                page = new ContentContainerPage(getPage().getPageMap(), menu.getUUID(), model
                                                .getInstanceKey(), true);
                            }
                        } catch (final EFapsException e) {
                            page = new ErrorPage(e);
                        }
                        setResponsePage(page);
                    }
                }
            });
        }
        return ret;
    }

    @Override
    protected ResourceReference getNodeIcon(final TreeNode _node)
    {
        final ResourceReference ret;
        if (!_node.getAllowsChildren()) {
            ret = getItem();
        } else {
            if (isNodeExpanded(_node)) {
                ret = getFolderOpen();
            } else {
                ret =  getFolderClosed();
            }
        }
        return ret;
    }

    /**
     * This class renders a Fragment of the TreeTable, representing
     * a Node including the junctionlink, the icon etc.
     *
     */
    private class StructurBrowserTreeFragment
        extends Panel
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id for this component
         * @param _node node
         * @param _level level
         * @param _nodeCallback callback
         */
        public StructurBrowserTreeFragment(final String _wicketId,
                                           final TreeNode _node,
                                           final int _level,
                                           final IRenderNodeCallback _nodeCallback)
        {
            super(_wicketId);
            final UIStructurBrowser uiStru = (UIStructurBrowser) ((DefaultMutableTreeNode) _node).getUserObject();

            add(newIndentation(this, "indent", _node, _level));

            if (uiStru.isForceExpanded()) {
                final WebMarkupContainer junctionLink = new WebMarkupContainer("link")
                {
                    private static final long serialVersionUID = 1L;

                    /**
                     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag )
                     */
                    @Override
                    protected void onComponentTag(final ComponentTag _tag)
                    {
                        super.onComponentTag(_tag);
                        _tag.put("onclick", "return false");
                    }
                };
                add(junctionLink);
                junctionLink.add(newJunctionImage(junctionLink, "image", _node));
            } else {
                add(newJunctionLink(this, "link", "image", _node));
            }
            final WebComponent direction = new WebComponent("direction");
            add(direction);

            if (uiStru == null || uiStru.getDirection() == null) {
                direction.setVisible(false);
            } else if (uiStru.getDirection()) {
                direction.add(new SimpleAttributeModifier("class", "directionDown"));
            } else {
                direction.add(new SimpleAttributeModifier("class", "directionUp"));
            }
            final MarkupContainer nodeLink;
            if (uiStru.isRoot()) {
                nodeLink = new WebMarkupContainer("nodeLink");
                nodeLink.add(new Label("label", DBProperties.getProperty(uiStru.getCommand().getName()
                                + ".StructurBrowser.root")));
                add(new WebComponent("rowId").setVisible(false));
                add(new WebComponent("level").setVisible(false));
                add(new WebComponent("allowChilds").setVisible(false));
            } else {
                final UIStructurBrowserTableCell uiObject = uiStru.getColumnValue(uiStru.getBrowserFieldIndex());

                if ((uiStru.isEditMode() || uiStru.isCreateMode()) &&  uiObject.getDisplay().equals(Display.EDITABLE)) {
                    nodeLink = new WebMarkupContainer("nodeLink");
                    nodeLink.add(new TreeCellPanel("label", _node, uiStru.getBrowserFieldIndex()));
                } else {
                    nodeLink = newNodeLink(this, "nodeLink", _node);
                    nodeLink.add(new Label("label", new AbstractReadOnlyModel<String>()
                    {

                        private static final long serialVersionUID = 1L;

                        /**
                         * @see org.apache.wicket.model.AbstractReadOnlyModel#getObject()
                         */
                        @Override
                        public String getObject()
                        {
                            return _nodeCallback.renderNode(_node);
                        }
                    }));
                }

                final WebComponent rowId = new WebComponent("rowId") {

                    /**
                     * Needed for serialization.
                     */
                    private static final long serialVersionUID = 1L;

                    /**
                     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
                     */
                    @Override
                    protected void onComponentTag(final ComponentTag _tag)
                    {
                        super.onComponentTag(_tag);
                        if (uiObject.getUserinterfaceId() == null) {
                            final AbstractUIPageObject uiPageObject = (AbstractUIPageObject) getPage()
                                .getDefaultModelObject();
                            uiObject.setUserinterfaceId(uiPageObject.getNewRandom());

                            try {
                                uiPageObject.getUiID2Oid().put(uiObject.getUserinterfaceId(),
                                               uiObject.getInstance() == null ? null : uiObject.getInstance().getOid());
                            } catch (final EFapsException e) {
                                throw new RestartResponseException(new ErrorPage(e));
                            }
                        }
                        _tag.put("name", EFapsKey.TABLEROW_NAME.getKey());
                        _tag.put("value", uiObject.getUserinterfaceId());
                        _tag.put("type" , "hidden");
                    }
                };
                this.add(rowId);

                final WebComponent type = new WebComponent("allowChilds") {

                    /**
                     * Needed for serialization.
                     */
                    private static final long serialVersionUID = 1L;

                    /**
                     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
                     */
                    @Override
                    protected void onComponentTag(final ComponentTag _tag)
                    {
                        super.onComponentTag(_tag);

                        _tag.put("name", EFapsKey.STRUCBRWSR_ALLOWSCHILDS.getKey());
                        _tag.put("value", _node.getAllowsChildren());
                        _tag.put("type" , "hidden");
                    }
                };
                this.add(type);

                final WebComponent level = new WebComponent("level") {

                    /**
                     * Needed for serialization.
                     */
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onComponentTag(final ComponentTag _tag)
                    {
                        super.onComponentTag(_tag);

                        _tag.put("name", EFapsKey.STRUCBRWSR_LEVEL.getKey());
                        _tag.put("value", _level);
                        _tag.put("type" , "hidden");
                    }
                };
                this.add(level);
            }
            add(nodeLink);
            nodeLink.add(newNodeIcon(nodeLink, "icon", _node));
        }
    }

    /**
     * Class is used to call an event from inside the parent.
     *
     */
    public class AjaxParentCallBackBehavior
        extends AbstractAjaxCallBackBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Node belonging to this call back behavior.
         */
        private final TreeNode node;

        /**
         * Constructor.
         * @param _node current node
         */
        public AjaxParentCallBackBehavior(final TreeNode _node)
        {
            super("onClick", Target.PARENT);
            this.node = _node;
        }

        /**
         * Method is executed on mouseup.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            Instance instance = null;
            final UIStructurBrowser model = (UIStructurBrowser) ((DefaultMutableTreeNode) this.node).getUserObject();

            if (model.getInstanceKey() != null) {
                Menu menu = null;
                try {
                    instance = model.getInstance();
                    menu = Menu.getTypeTreeMenu(instance.getType());
                } catch (final EFapsException e) {
                    throw new RestartResponseException(new ErrorPage(e));
                }
                if (menu == null) {
                    final EFapsException excep = new EFapsException(this.getClass(), "newNodeLink.noTreeMenu",
                                    instance.getType().getName());
                    throw new RestartResponseException(new ErrorPage(excep));
                }
                Page page;
                try {
                    if (model.getTarget() == org.efaps.admin.ui.AbstractCommand.Target.POPUP) {
                        page = new ContentContainerPage(menu.getUUID(), model.getInstanceKey());
                    } else {
                        page = new ContentContainerPage(PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME), menu.getUUID(),
                                        model.getInstanceKey(), true);
                    }
                } catch (final EFapsException e) {
                    page = new ErrorPage(e);
                }
                setResponsePage(page);
            }
        }
    }
}
