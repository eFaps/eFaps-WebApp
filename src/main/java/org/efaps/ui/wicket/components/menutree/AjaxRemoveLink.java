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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.menutree;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * This Class renders a Link which removes a Child from a MenuTree.
 *
 * @author The eFaps Team
 * @version $Id:AjaxRemoveLink.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxRemoveLink
    extends AbstractAjaxLink
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Construtor setting the ID and the Node of this Component.
     *
     * @param _wicketId wicketid for this component
     * @param _node     node for his component
     */
    public AjaxRemoveLink(final String _wicketId,
                          final DefaultMutableTreeNode _node)
    {
        super(_wicketId, _node);
    }

    @Override
    public void onClick(final AjaxRequestTarget _target)
    {
//        final MenuTree menutree = findParent(MenuTree.class);
//        final DefaultMutableTreeNode parent =
//                        (DefaultMutableTreeNode) getNode().getParent();
//        final DefaultMutableTreeNode selected =
//                        (DefaultMutableTreeNode) menutree.getTreeState().getSelectedNodes()
//                                        .iterator().next();
//        boolean selectParent = false;
//        if (getNode().isNodeDescendant(selected)) {
//            selectParent = true;
//        }
//        menutree.getTreeState().selectNode(parent, true);
//
//        ((DefaultTreeModel) menutree.getDefaultModelObject())
//                        .removeNodeFromParent(getNode());
//
//        if (selectParent) {
//            menutree.getTreeState().selectNode(parent, true);
//            menutree.changeContent((UIMenuItem) parent.getUserObject(), _target);
//        } else {
//            menutree.getTreeState().selectNode(selected, true);
//            _target.add(menutree.getNodeComponent(parent));
//        }
//        menutree.updateTree(_target);
    }
}
