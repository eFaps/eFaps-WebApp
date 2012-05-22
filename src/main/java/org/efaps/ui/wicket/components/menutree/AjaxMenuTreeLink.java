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

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxMenuTreeLink
    extends AbstractAjaxLink
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

        /**
     * @param _wicketId Wicket ID for the Component
     * @param _node     node the component belongs to
     */
    public AjaxMenuTreeLink(final String _wicketId,
                            final DefaultMutableTreeNode _node)
    {
        super(_wicketId, _node);
    }

    @Override
    public void onClick(final AjaxRequestTarget _target)
    {
//        final UIMenuItem model = (UIMenuItem) getNode().getUserObject();
//        final MenuTree menutree = this.findParent(MenuTree.class);
//
//        menutree.changeContent(model, _target);
//
//        menutree.getTreeState().selectNode(getNode(), true);
//        menutree.updateTree(_target);
    }
}
