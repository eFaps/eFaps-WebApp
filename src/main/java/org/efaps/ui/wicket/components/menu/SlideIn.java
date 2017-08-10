/*
 * Copyright 2003 - 2017 The eFaps Team
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
 */
package org.efaps.ui.wicket.components.menu;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.DojoClass;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;

/**
 * The Class SlideIn.
 *
 * @author The eFaps Team
 */
public class SlideIn
    extends WebComponent
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the style sheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(SlideIn.class, "Slide.css");

    /**
     * Instantiates a new slide in.
     *
     * @param _id the id
     * @param _model the model
     */
    public SlideIn(final String _id,
                   final IModel<UIMenuItem> _model)
    {
        super(_id, _model);
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(SlideIn.CSS));
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        final UIMenuItem menuItem = (UIMenuItem) getDefaultModelObject();

        final Set<DojoClass> dojoClasses = new HashSet<>();
        Collections.addAll(dojoClasses, DojoClasses.aspect, DojoClasses.ready, DojoClasses.registry,
                        DojoClasses.domConstruct, DojoClasses.dom, DojoClasses.fx, DojoClasses.basefx, DojoClasses.on,
                        DojoClasses.domStyle, DojoClasses.domGeom, DojoClasses.query, DojoClasses.NodeListDom,
                        DojoClasses.NodeListFx);

        final StringBuilder js = new StringBuilder()
                        .append("var sn = dom.byId('slidein');\n");

        for (final UIMenuItem childItem : menuItem.getChildren()) {
            js.append(getMenuItem(childItem));
        }

        final StringBuilder html = new StringBuilder().append("<script type=\"text/javascript\">")
                        .append(DojoWrapper.require(js, dojoClasses.toArray(new DojoClass[dojoClasses.size()])))
                        .append("\n</script>");

        replaceComponentTagBody(_markupStream, _openTag, html);
    }

    /**
     * Gets the menu item.
     *
     * @param _menuItem the menu item
     * @return the menu item
     */
    private CharSequence getMenuItem(final UIMenuItem _menuItem)
    {
        final StringBuilder js = new StringBuilder();

        final String node1 = RandomStringUtils.randomAlphabetic(3);
        final String node2 = RandomStringUtils.randomAlphabetic(3);

        js.append("var ").append(node1).append(" = domConstruct.create(\"div\", { class: \"menueentry\"}, sn);\n")
            .append("var ").append(node2).append(" = domConstruct.create(\"div\", { class: \"title\"}, ")
                .append(node1).append(");\n")
            .append("domConstruct.create(\"span\", { class: \"menutitle\", innerHTML: \"")
            .append(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeHtml4(_menuItem.getLabel())))
            .append("\"} , ").append(node2).append("); \n")
            .append(getSubMenuItem(_menuItem, node1, node2));
        return js;
    }

    /**
     * Gets the sub menu item.
     *
     * @param _menuItem the menu item
     * @param _parentNode the parent node
     * @param _titleNode the title node
     * @return the sub menu item
     */
    private CharSequence getSubMenuItem(final UIMenuItem _menuItem,
                                        final String _parentNode,
                                        final String _titleNode)
    {
        final StringBuilder js = new StringBuilder();
        final String node1 = RandomStringUtils.randomAlphabetic(3);

        if (!_menuItem.getChildren().isEmpty()) {
            js.append("var ").append(node1).append(" = domConstruct.create(\"div\",")
                .append("{ class: \"nested\", style: \"display:none\"}, ")
                .append(_parentNode).append(");\n")
                .append(" on(").append(_titleNode).append(", \"click\", function(evt) {\n")
                .append("if (domStyle.get(").append(node1).append(", \"display\") !== \"none\") {\n")
                .append("fx.wipeOut({\n")
                .append("node: ").append(node1).append("\n")
                .append("}).play();\n")
                .append("} else {\n")
                .append("fx.wipeIn({\n")
                .append("node: ").append(node1).append("\n")
                .append("}).play();\n")
                .append("}\n")
                .append("});\n");
        }
        for (final UIMenuItem childItem : _menuItem.getChildren()) {
            final String node2 = RandomStringUtils.randomAlphabetic(3);
            final String node3 = RandomStringUtils.randomAlphabetic(3);
            js.append("var ").append(node2).append(" = domConstruct.create(\"div\",")
                .append("{ class: \"menueentry\"}, ").append(node1).append(");\n")
                .append("var ").append(node3).append(" = domConstruct.create(\"div\", { class: \"title\"}, ")
                    .append(node2).append(");\n")
                .append("domConstruct.create(\"span\", { class: \"menutitle\", innerHTML: \"")
                .append(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeHtml4(childItem.getLabel())))
                .append("\"} , ").append(node3).append("); \n")
                .append(getSubMenuItem(childItem, node2, node3));
        }
        return js;
    }
}
