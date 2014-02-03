/*
 * Copyright 2003 - 2014 The eFaps Team
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
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SetEditedBehavior
    extends Behavior
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see org.apache.wicket.behavior.AbstractBehavior#onComponentTag(org.apache.wicket.Component,
     * org.apache.wicket.markup.ComponentTag)
     * @param _component component
     * @param _tag       tag
     */
    @Override
    public void onComponentTag(final Component _component,
                               final ComponentTag _tag)
    {
        super.onComponentTag(_component, _tag);
        _tag.put("onchange", "eFapsSetEdited(this);");
    }

    /**
     * @return CharSequence with script
     */
    private CharSequence getJavaScript()
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("function eFapsSetEdited(_element){")
            .append("require([\"dojo/dom\",\"dojo/query\",\"dojo/dom-style\",\"dojo/dom-class\",")
            .append("\"dojo/NodeList-traverse\"],")
            .append("function(_dom,_query, _style, _domClass) {")
            .append(" _query(_element).parents(\".tree-branch\").forEach(function(_node){")
            .append("_query(_node).children(\".tree-node\").forEach(function(_node2){")
            .append(" _query(_node2).children(\".tree-junction-expanded\").forEach(function(_node3){")
            .append("_style.set(_node3, \"visibility\", \"hidden\");")
            .append("});")
            .append("});")
            .append("_query(_node).children(\".eFapsTableCellEdit\").forEach(function(_node2){")
            .append(" _domClass.add(_node2,\"edited\");")
            .append(" });")
            .append("});")
            .append(" });")
             .append("};");
        return ret;
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        _response.render(JavaScriptHeaderItem.forScript(getJavaScript(), SetEditedBehavior.class.getName()));
    }
}
