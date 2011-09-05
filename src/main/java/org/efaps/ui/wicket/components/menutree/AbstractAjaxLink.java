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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.ui.wicket.components.menutree;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.ajax.markup.html.AjaxLink;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractAjaxLink
    extends AjaxLink<Object>
{
    /**
     * Needed for serialiazzation.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The current node.
     */
    private final DefaultMutableTreeNode node;

    /**
     * Construtor setting the ID and the Node of this Component.
     *
     * @param _wicketId wicketid for this component
     * @param _node     node for this component
     */
    public AbstractAjaxLink(final String _wicketId,
                            final DefaultMutableTreeNode _node)
    {
        super(_wicketId);
        this.node = _node;
    }

    /**
     * Getter method for the instance variable {@link #node}.
     *
     * @return value of instance variable {@link #node}
     */
    protected DefaultMutableTreeNode getNode()
    {
        return this.node;
    }
}
