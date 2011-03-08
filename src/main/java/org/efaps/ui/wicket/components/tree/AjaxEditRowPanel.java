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

import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * Panel renders the add, insert and remove buttons for tables on insert.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxEditRowPanel
    extends Panel
{

    /**
     * Content reference for the delete icon.
     */
    private static final EFapsContentReference ICON_ADD = new EFapsContentReference(AjaxEditRowPanel.class,
                    "add.png");

    /**
     * Content reference for the delete icon.
     */
    private static final EFapsContentReference ICON_DELETE = new EFapsContentReference(AjaxEditRowPanel.class,
                    "delete.png");

    /**
     * Content reference for the add folder icon.
     */
    private static final EFapsContentReference ICON_FOLDER_ADD = new EFapsContentReference(AjaxEditRowPanel.class,
                    "folder_add.png");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor called from the rowpanel for each row.
     *
     * @param _wicketId wicket id for this component
     * @param _model    model for this component
     * @param _node     Node this Panel belongs to
     */
    public AjaxEditRowPanel(final String _wicketId,
                            final IModel<UIStructurBrowser> _model,
                            final TreeNode _node)
    {
        super(_wicketId, _model);
        final UIStructurBrowser uiStru = (UIStructurBrowser) ((DefaultMutableTreeNode) _node).getUserObject();

        if (uiStru.isRoot()) {
            add(new WebComponent("delLink").setVisible(false));
        } else {
            final RemoveRow delLink = new RemoveRow("delLink", _model, _node);
            this.add(delLink);
            final StaticImageComponent delImage = new StaticImageComponent("delIcon");
            delImage.setReference(AjaxEditRowPanel.ICON_DELETE);
            delLink.add(delImage);
        }
        if (uiStru.isAllowChilds()) {
            final InsertChildFolder insertFolderlink = new InsertChildFolder("addFolderLink", _model, _node);
            this.add(insertFolderlink);
            final StaticImageComponent insertFolderImage = new StaticImageComponent("addFolderIcon");
            insertFolderImage.setReference(AjaxEditRowPanel.ICON_FOLDER_ADD);
            insertFolderlink.add(insertFolderImage);

            if (uiStru.isRoot() || !uiStru.isAllowItems()) {
                add(new WebComponent("addLink").setVisible(false));
            } else {
                final InsertChildRow insertlink = new InsertChildRow("addLink", _model, _node);
                this.add(insertlink);
                final StaticImageComponent insertImage = new StaticImageComponent("addIcon");
                insertImage.setReference(AjaxEditRowPanel.ICON_ADD);
                insertlink.add(insertImage);
            }
        } else {
            add(new WebComponent("addFolderLink").setVisible(false));
            final InsertRow insertlink = new InsertRow("addLink", _model, _node);
            this.add(insertlink);
            final StaticImageComponent insertImage = new StaticImageComponent("addIcon");
            insertImage.setReference(AjaxEditRowPanel.ICON_ADD);
            insertlink.add(insertImage);
        }
    }


    /**
     * Class renders a component containing a script to remove a row from a
     * table.
     */
    public class RemoveRow
        extends WebMarkupContainer
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Node this link belongs to.
         */
        private final DefaultMutableTreeNode node;

        /**
         * @param _wicketId wicket ID of this component
         * @param _model    model for this component
         * @param _node     node this link belongs to
         */
        public RemoveRow(final String _wicketId,
                         final IModel<UIStructurBrowser> _model,
                         final TreeNode _node)
        {
            super(_wicketId, _model);
            this.node = (DefaultMutableTreeNode) _node;


            add(new AjaxFormSubmitBehavior(null, "onclick")
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(final AjaxRequestTarget _target)
                {
                    try {
                        AjaxEditRowPanel.RemoveRow.this.onSubmit(_target, getForm());
                    } catch (final EFapsException e) {
                        onError(_target);
                    }
                }

                @Override
                protected void onError(final AjaxRequestTarget _target)
                {
                    //not implemented
                }

                @Override
                protected CharSequence getEventHandler()
                {
                    return new AppendingStringBuffer(super.getEventHandler()).append("; return false;");
                }
            });
        }

        /**
         * Getter method for the instance variable {@link #node}.
         *
         * @return value of instance variable {@link #node}
         */
        public DefaultMutableTreeNode getNode()
        {
            return this.node;
        }

        /**
         * @param _target   AjaxRequestTarget
         * @param _form     Form
         * @throws EFapsException on error
         */
        public void onSubmit(final AjaxRequestTarget _target,
                             final Form<?> _form)
            throws EFapsException
        {
            final UIStructurBrowser strucBr = (UIStructurBrowser) getNode().getUserObject();
            final Map<String, String> uiID2Oid = ((AbstractUIPageObject) (findPage().getDefaultModelObject()))
                                                            .getUiID2Oid();
            strucBr.executeListener(UIStructurBrowser.ExecutionStatus.NODE_REMOVE, uiID2Oid);
            final String js = strucBr.setValuesFromUI(Context.getThreadContext().getParameters(), getNode());
            final StructurBrowserTreeTable treeTable = findParent(StructurBrowserTreeTable.class);
            final DefaultTreeModel treeModel = (DefaultTreeModel) treeTable.getModelObject();
            treeModel.removeNodeFromParent(this.node);
            treeTable.updateTree(_target);
            _target.appendJavascript(js);
        }
    }

    /**
     * Render an insert button.
     */
    public class InsertRow
        extends AjaxEditRowPanel.RemoveRow
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket ID of this component
         * @param _model    model for this component
         * @param _node     node this link belongs to
         */
        public InsertRow(final String _wicketId,
                         final IModel<UIStructurBrowser> _model,
                         final TreeNode _node)
        {
            super(_wicketId, _model, _node);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSubmit(final AjaxRequestTarget _target,
                             final Form<?> _form)
            throws EFapsException
        {
            final UIStructurBrowser strucBr = (UIStructurBrowser) getNode().getUserObject();
            final Map<String, String> uiID2Oid = ((AbstractUIPageObject) (findPage().getDefaultModelObject()))
                                                        .getUiID2Oid();
            strucBr.executeListener(UIStructurBrowser.ExecutionStatus.NODE_INSERTITEM, uiID2Oid);
            final String js = strucBr.setValuesFromUI(Context.getThreadContext().getParameters(), getNode());
            UIStructurBrowser newStruBrws = null;
            try {
                newStruBrws = strucBr.getClone4New();
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
            newStruBrws.setAllowChilds(false);
            newStruBrws.checkHideColumn4Row();
            final DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newStruBrws);
            newTreeNode.setAllowsChildren(false);
            final StructurBrowserTreeTable treeTable = findParent(StructurBrowserTreeTable.class);
            final DefaultTreeModel treeModel = (DefaultTreeModel) treeTable.getModelObject();
            treeModel.insertNodeInto(newTreeNode, (DefaultMutableTreeNode) getNode().getParent(),
                            getNode().getParent().getIndex(getNode()));
            treeTable.updateTree(_target);
            _target.appendJavascript(js);
        }
    }


    /**
     * Link that inserts a folder as child.
     */
    public class InsertChildFolder
        extends AjaxEditRowPanel.InsertRow
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket ID of this component
         * @param _model    model for this component
         * @param _node     node this link belongs to
         */
        public InsertChildFolder(final String _wicketId,
                            final IModel<UIStructurBrowser> _model,
                            final TreeNode _node)
        {
            super(_wicketId, _model, _node);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSubmit(final AjaxRequestTarget _target,
                             final Form<?> _form)
            throws EFapsException
        {
            final UIStructurBrowser strucBr = (UIStructurBrowser) getNode().getUserObject();
            final Map<String, String> uiID2Oid = ((AbstractUIPageObject) (findPage().getDefaultModelObject()))
                                                        .getUiID2Oid();
            strucBr.executeListener(UIStructurBrowser.ExecutionStatus.NODE_INSERTCHILDFOLDER, uiID2Oid);
            final String js = strucBr.setValuesFromUI(Context.getThreadContext().getParameters(), getNode());
            UIStructurBrowser newStruBrws = null;
            try {
                newStruBrws = strucBr.getClone4New();
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
            strucBr.getChilds().add(newStruBrws);
            newStruBrws.setAllowChilds(true);
            newStruBrws.setAllowItems(newStruBrws.checkForAllowItems(newStruBrws.getInstance()));
            newStruBrws.checkHideColumn4Row();
            final DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newStruBrws);
            newTreeNode.setAllowsChildren(true);
            final StructurBrowserTreeTable treeTable = findParent(StructurBrowserTreeTable.class);
            final DefaultTreeModel treeModel = (DefaultTreeModel) treeTable.getModelObject();
            treeModel.insertNodeInto(newTreeNode, getNode(), getNode().getChildCount());
            treeTable.updateTree(_target);
            _target.appendJavascript(js);
        }
    }

    /**
     * Link that inserts a row as a child to the given node.
     */
    public class InsertChildRow
        extends AjaxEditRowPanel.InsertRow
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket ID of this component
         * @param _model    model for this component
         * @param _node     node this link belongs to
         */
        public InsertChildRow(final String _wicketId,
                              final IModel<UIStructurBrowser> _model,
                              final TreeNode _node)
        {
            super(_wicketId, _model, _node);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onSubmit(final AjaxRequestTarget _target,
                            final Form<?> _form)
            throws EFapsException
        {
            final UIStructurBrowser strucBr = (UIStructurBrowser) getNode().getUserObject();
            final Map<String, String> uiID2Oid = ((AbstractUIPageObject) (findPage().getDefaultModelObject()))
                                                        .getUiID2Oid();
            strucBr.executeListener(UIStructurBrowser.ExecutionStatus.NODE_INSERTCHILDITEM, uiID2Oid);
            final String js = strucBr.setValuesFromUI(Context.getThreadContext().getParameters(), getNode());
            UIStructurBrowser newStruBrws = null;
            try {
                newStruBrws = strucBr.getClone4New();
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
            newStruBrws.setAllowChilds(false);
            newStruBrws.checkHideColumn4Row();
            final DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newStruBrws);
            newTreeNode.setAllowsChildren(false);
            final StructurBrowserTreeTable treeTable = findParent(StructurBrowserTreeTable.class);

            final DefaultTreeModel treeModel = (DefaultTreeModel) treeTable.getModelObject();
            treeModel.insertNodeInto(newTreeNode, getNode(), getNode().getChildCount());
            treeTable.updateTree(_target);
            _target.appendJavascript(js);
        }
    }
}
