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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractMenuNode
    extends Node<UIMenuItem>
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Tree this Node belongs to.
     */
    private final AbstractTree<UIMenuItem> tree;

    /**
     * @param _wicketId     wicket id for this component
     * @param _tree         tree this node belongs to
     * @param _model        model for this component
     */
    public AbstractMenuNode(final String _wicketId,
                            final AbstractTree<UIMenuItem> _tree,
                            final IModel<UIMenuItem> _model)
    {
        super(_wicketId, _tree, _model);
        this.tree = _tree;
        add(AttributeModifier.append("class", "eFapsMenuTreeNode"));
    }


    /**
     * Getter method for the instance variable {@link #tree}.
     *
     * @return value of instance variable {@link #tree}
     */
    public AbstractTree<UIMenuItem> getTree()
    {
        return this.tree;
    }


    @Override
    protected MarkupContainer createJunctionComponent(final String _wicketId)
    {
        return new AjaxLink<UIMenuItem>(_wicketId)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget _target)
            {
                toggle();
            }

            /* (non-Javadoc)
             * @see org.apache.wicket.ajax.markup.html.AjaxLink#onComponentTag(org.apache.wicket.markup.ComponentTag)
             */
            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                if (!((UIMenuItem) AbstractMenuNode.this.getDefaultModelObject()).hasChildren()) {
                    _tag.append("style", "display:none", ";");
                }
            }
        };
    }
}
