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


package org.efaps.ui.wicket.behaviors.dojo;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class MessageListenerBehavior
    extends AbstractDojoBehavior
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Render to the web response whatever the component wants to contribute to
     * the head section.
     *
     * @param _component    Component this behavior belongs to
     * @param _response     Response object
     */
    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        _response.render(JavaScriptHeaderItem.forScript(MessageListenerBehavior.getScript(_component),
                            _component.getClass().getName() + "_MessageListener"));

    }

    /**
     * @param _component _component the script will be rendered for
     * @return the Javascript
     */
    private static CharSequence getScript(final Component _component)
    {
        final StringBuilder js = new StringBuilder()
            .append("require([\"dojo/dom\",\"dojo/dom-style\", \"dojo/on\"],")
            .append("function(dom,domStyle,on){\n")
            .append("on(window, \"message\", function(event) {\n")
            .append("var node = dom.byId('").append(_component.getMarkupId(true)).append("');\n")
            .append("if (event.data==\"\") {\n")
            .append("domStyle.set(node, \"display\", \"none\");\n")
            .append("} else {\n")
            .append("domStyle.set(node, \"display\", \"block\");\n")
            .append("node.innerHTML = event.data;\n")
            .append("}\n")
            .append("});")
            .append("});");
        return js;
    }
}
