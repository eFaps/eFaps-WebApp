/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.components.heading;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.behaviors.dojo.OnDojoReadyHeaderItem;
import org.efaps.ui.wicket.models.objects.UIHeading;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;

/**
 * Class is used to render a panel containing a title for a page or a subtitle
 * inside a page.
 *
 * @author The eFaps Team
 * @version $Id:TitelPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class HeadingPanel
    extends Panel
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id for this component
     * @param _headingmodel model for this heading
     */
    public HeadingPanel(final String _wicketId,
                        final Model<UIHeading> _headingmodel)
    {
        super(_wicketId, _headingmodel);
        setOutputMarkupId(true);
        addComponents(_headingmodel);
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        final StringBuilder js = new StringBuilder()
            .append("require([\"dojo/query\", \"dojo/dom\", \"dojo/dom-class\", \"dojo/NodeList-traverse\"],")
            .append("function(query, dom, domClass){\n")
            .append("var add = true;\n")
            .append("var node = dom.byId(_nodeID);\n")
            .append("domClass.toggle(node, \"eFapsCollapsedSection\");\n")
            .append("dom.byId(\"status_\" + _nodeID).value = domClass.contains(node, \"eFapsCollapsedSection\");")
            .append("query(node).nextAll().some(function(_node){\n")
            .append("if (add) {\n")
            .append("var x = query(\"div[class^='eFapsHeading']\", _node);\n")
            .append("if (x.length > 0){\n")
            .append("add = false;\n")
            .append("} else {\n")
            .append("domClass.toggle(_node, \"eFapsHiddenSection\");\n")
            .append("}\n")
            .append("}});\n")
            .append("});\n");

        final StringBuilder js2 = new StringBuilder().append("function toggleSection(_nodeID){")
            .append(DojoWrapper.require(js, DojoClasses.query, DojoClasses.dom, DojoClasses.domClass,
                        DojoClasses.NodeListTraverse)).append("}\n");

        _response.render(JavaScriptHeaderItem.forScript(js2, HeadingPanel.class.getName()));

        if (((UIHeading) getDefaultModelObject()).isCollapsible()
                        && ((UIHeading) getDefaultModelObject()).getCollapsed()) {
            final StringBuilder js3 = new StringBuilder();
            js3.append("toggleSection('").append(getMarkupId(true)).append("');");
                _response.render(OnDojoReadyHeaderItem.forScript(js3));
        }
    }

    /**
     * Method to add the Component to this Panel.
     *
     * @param _headingmodel model of the heading.
     */
    public void addComponents(final Model<UIHeading> _headingmodel)
    {
        final WebMarkupContainer container = new WebMarkupContainer("container");

        this.add(container);
        if (_headingmodel.getObject().getLevel() == 0) {
            container.add(AttributeModifier.replace("class", "eFapsFrameTitle"));
        } else {
            container.add(AttributeModifier.replace("class", "eFapsHeading" + _headingmodel.getObject().getLevel()));
        }
        container.add(new Label("heading", _headingmodel.getObject().getLabel()));

        final String toggleId = this.getMarkupId(true);
        final Component span = new WebComponent("toggle") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("onclick", "toggleSection('" + toggleId + "');");
            }

            @Override
            public boolean isVisible()
            {
                return _headingmodel.getObject().getLevel() > 0 && _headingmodel.getObject().isCollapsible();
            }
        };
        container.add(span);

        final Component status = new WebComponent("status") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("name", _headingmodel.getObject().getName());
                _tag.put("value", false);
            }

            @Override
            public boolean isVisible()
            {
                return _headingmodel.getObject().getLevel() > 0 && _headingmodel.getObject().isCollapsible();
            }
        };
        status.setMarkupId("status_" + toggleId);
        container.add(status);
    }
}
