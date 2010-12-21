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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
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
     * Script needed for the ajax call.
     */
    private CharSequence script;

    /**
     * Do be able to have more than one table in a form that can add new rows,
     * it is necessary to have unique function names.
     */
    private String functionName;

    /**
     * Constructor called from the rowpanel for each row.
     *
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _rowPanel rowpanel that must be removed
     */
    public AjaxEditRowPanel(final String _wicketId,
                            final IModel<UIStructurBrowser> _model,
                            final TreeNode _node)
    {
        super(_wicketId, _model);
        final UIStructurBrowser uiStru = (UIStructurBrowser) ((DefaultMutableTreeNode) _node).getUserObject();



        final RemoveRow delLink = new RemoveRow("delLink", _model, _node);
        this.add(delLink);
        final StaticImageComponent delImage = new StaticImageComponent("delIcon");
        delImage.setReference(AjaxEditRowPanel.ICON_DELETE);
        delLink.add(delImage);

        if (uiStru.isAllowChilds()) {
            final InsertChildFolder insertFolderlink = new InsertChildFolder("addFolderLink", _model, _node);
            this.add(insertFolderlink);
            final StaticImageComponent insertFolderImage = new StaticImageComponent("addFolderIcon");
            insertFolderImage.setReference(AjaxEditRowPanel.ICON_FOLDER_ADD);
            insertFolderlink.add(insertFolderImage);

            final InsertChildRow insertlink = new InsertChildRow("addLink", _model, _node);
            this.add(insertlink);
            final StaticImageComponent insertImage = new StaticImageComponent("addIcon");
            insertImage.setReference(AjaxEditRowPanel.ICON_ADD);
            insertlink.add(insertImage);
        } else {
            add(new WebComponent("addFolderLink").setVisible(false));
            final InsertRow insertlink = new InsertRow("addLink", _model, _node);
            this.add(insertlink);
            final StaticImageComponent insertImage = new StaticImageComponent("addIcon");
            insertImage.setReference(AjaxEditRowPanel.ICON_ADD);
            insertlink.add(insertImage);
        }
        add(new WebComponent("script").setVisible(false));
    }


    /**
     * Class renders a component containing a script to remove a row from a
     * table.
     */
    public class RemoveRow
        extends AjaxLink<UIStructurBrowser>
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;
        private final DefaultMutableTreeNode node;
        /**
         * @param _wicketId wicket id for this component
         * @param _rowPanel Rowpanel that must be removed
         */
        /**
         * @param _wicketId wicket ID of this component
         */
        public RemoveRow(final String _wicketId,
                         final IModel<UIStructurBrowser> _model,
                         final TreeNode _node)
        {
            super(_wicketId, _model);
            this.node = (DefaultMutableTreeNode) _node;
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

        /* (non-Javadoc)
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final StructurBrowserTreeTable treeTable = findParent(StructurBrowserTreeTable.class);
            final DefaultTreeModel treeModel = (DefaultTreeModel) treeTable.getModelObject();
            treeModel.removeNodeFromParent(this.node);
            treeTable.updateTree(_target);
        }
    }


    /**
     * Render an insert button.
     */
    public class InsertRow
        extends RemoveRow
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;


        /**
         * @param _wicketId wicket ID of this component
         */
        public InsertRow(final String _wicketId,
                         final IModel<UIStructurBrowser> _model,
                         final TreeNode _node)
        {
            super(_wicketId, _model, _node);

        }




        /* (non-Javadoc)
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final UIStructurBrowser strucBr = (UIStructurBrowser) getNode().getUserObject();

            UIStructurBrowser newStruBrws = null;
            try {
                newStruBrws = strucBr.getClone4New();
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            final DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newStruBrws);
            newTreeNode.setAllowsChildren(false);
            final StructurBrowserTreeTable treeTable = findParent(StructurBrowserTreeTable.class);
            final DefaultTreeModel treeModel = (DefaultTreeModel) treeTable.getModelObject();
            treeModel.insertNodeInto(newTreeNode, (DefaultMutableTreeNode) getNode().getParent(),
                            getNode().getParent().getIndex(getNode()));
            treeTable.updateTree(_target);
        }
    }


    public class InsertChildFolder
        extends InsertRow
    {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId
         * @param _model
         * @param _node
         */
        public InsertChildFolder(final String _wicketId,
                            final IModel<UIStructurBrowser> _model,
                            final TreeNode _node)
        {
            super(_wicketId, _model, _node);
        }

        /* (non-Javadoc)
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final UIStructurBrowser strucBr = (UIStructurBrowser) getNode().getUserObject();

            UIStructurBrowser newStruBrws = null;
            try {
                newStruBrws = strucBr.getClone4New();
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            newStruBrws.setAllowChilds(true);
            final DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newStruBrws);
            newTreeNode.setAllowsChildren(true);
            final StructurBrowserTreeTable treeTable = findParent(StructurBrowserTreeTable.class);
            final DefaultTreeModel treeModel = (DefaultTreeModel) treeTable.getModelObject();
            treeModel.insertNodeInto(newTreeNode, getNode(), getNode().getChildCount());
            treeTable.updateTree(_target);
        }
    }


    public class InsertChildRow
        extends InsertRow
    {

        /**
     *
     */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId
         * @param _model
         * @param _node
         */
        public InsertChildRow(final String _wicketId,
                              final IModel<UIStructurBrowser> _model,
                              final TreeNode _node)
        {
            super(_wicketId, _model, _node);
        }

        /*
         * (non-Javadoc)
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final UIStructurBrowser strucBr = (UIStructurBrowser) getNode().getUserObject();

            UIStructurBrowser newStruBrws = null;
            try {
                newStruBrws = strucBr.getClone4New();
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            newStruBrws.setAllowChilds(false);
            final DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode(newStruBrws);
            newTreeNode.setAllowsChildren(false);
            final StructurBrowserTreeTable treeTable = findParent(StructurBrowserTreeTable.class);
            final DefaultTreeModel treeModel = (DefaultTreeModel) treeTable.getModelObject();
            treeModel.insertNodeInto(newTreeNode, getNode(), getNode().getChildCount());
            treeTable.updateTree(_target);
        }
    }
}
