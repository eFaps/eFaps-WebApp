/*
 * Copyright 2003 - 2011 The eFaps Team
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
import javax.swing.tree.DefaultTreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * This class renders a Link which is used to collapse and expand
 * the ChildItems of a Header inside a MenuTree.
 *
 * @author The eFaps Team
 * @version $Id:AjaxCollapseLink.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxExpandLink
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
     * @param _node     node for this component
     */
    public AjaxExpandLink(final String _wicketId,
                          final DefaultMutableTreeNode _node)
    {
        super(_wicketId, _node);
    }

    @Override
    public void onClick(final AjaxRequestTarget _target)
    {
        final MenuTree menutree = findParent(MenuTree.class);
        if (menutree.getTreeState().isNodeExpanded(getNode())) {
            menutree.getTreeState().collapseNode(getNode());
            menutree.nodeCollapsed(getNode());
        } else {
            menutree.getTreeState().expandNode(getNode());
            menutree.nodeExpanded(getNode());
        }
        ((DefaultTreeModel) menutree.getDefaultModelObject()).nodeChanged(getNode());
        menutree.updateTree(_target);
    }
}
