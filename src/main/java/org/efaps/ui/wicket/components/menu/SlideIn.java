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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.ui.wicket.util.DojoClass;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SlideIn.
 *
 * @author The eFaps Team
 */
public class SlideIn
    extends WebComponent
    implements IRequestListener
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SlideIn.class);

    /**
     * Reference to the style sheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(SlideIn.class, "Slide.css");

    /** The menu items. */
    private final Map<String, UIMenuItem> menuItems = new HashMap<>();

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
                        DojoClasses.NodeListFx, DojoClasses.domClass);

        final StringBuilder js = new StringBuilder()
                        .append("var sn = dom.byId('slidein');\n");

        for (final UIMenuItem childItem : menuItem.getChildren()) {
            js.append(getMenuItem(childItem));
        }

        js.append("on(dom.byId(\"slideinCollapse\"), \"click\", function(evt) {\n")
            .append("var anim;\n")
            .append("if (domGeom.getContentBox(evt.currentTarget).w > 100) {\n")
            .append(" domClass.replace(\"slideinCollapse\", \"expand\", \"collapse\"); \n")
            .append(" anim = fx.combine([\n")
            .append(" baseFx.animateProperty({\n")
            .append("node: \"slideinPane\",\n")
            .append(" properties: {\n")
            .append(" width: 56\n")
            .append("}\n")
            .append("}),\n")
            .append("baseFx.animateProperty({\n")
            .append(" node: \"slideinPane_splitter\",\n")
            .append(" properties: {\n")
            .append("left: 40\n")
            .append("}\n")
            .append("}),\n")
            .append("baseFx.animateProperty({\n")
            .append(" node: \"mainPanel\",\n")
            .append("properties: {\n")
            .append("left: 60,\n")
            .append(" width:1780\n")
            .append("}\n")
            .append("})\n")
            .append("]);\n")
            .append("} else {\n")
            .append(" domClass.replace(\"slideinCollapse\", \"collapse\", \"expand\"); \n")
            .append("anim = fx.combine([\n")
            .append("baseFx.animateProperty({\n")
            .append("node: \"slideinPane\",\n")
            .append(" properties: {\n")
            .append(" width: 200\n")
            .append("}\n")
            .append("}),\n")
            .append("baseFx.animateProperty({\n")
            .append("node: \"slideinPane_splitter\",\n")
            .append("properties: {\n")
            .append("left: 40\n")
            .append(" }\n")
            .append("}),\n")
            .append("baseFx.animateProperty({\n")
            .append("node: \"mainPanel\",\n")
            .append("properties: {\n")
            .append("left: 220\n")
            .append("}\n")
            .append("})\n")
            .append("]);\n")
            .append("}\n")
            .append("aspect.after(anim, \"onEnd\", function(){\n")
            .append("registry.byId(\"borderPanel\").resize();\n")
            .append("});\n")
            .append("anim.play();\n")
            .append("});\n");

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
            .append("var ").append(node2).append(" = domConstruct.create(\"div\", { class: \"title");
        if (!_menuItem.getChildren().isEmpty()) {
            js.append(" closed");
        }
        js.append("\"},").append(node1).append(");\n")
            .append(getImage(_menuItem, node2))
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

        if (_menuItem.getChildren().isEmpty()) {
            js.append(" on(").append(_titleNode).append(", \"click\", function(evt) {\n")
                  .append("registry.byId(\"").append("mainPanel").append(
                          "\").set(\"content\", domConstruct.create(\"iframe\", {").append("\"id\": \"")
                      .append(MainPage.IFRAME_ID).append("\",\"src\": \"").append(getUrl(_menuItem))
                      .append("\",\"style\": \"border: 0; width: 100%; height: 99%\"").append(",\"id\": \"")
                        .append(MainPage.IFRAME_ID).append("\"").append("}));")
                .append("});\n");
        } else {
            js.append("var ").append(node1).append(" = domConstruct.create(\"div\",")
                .append("{ class: \"nested\", style: \"display:none\"}, ")
                .append(_parentNode).append(");\n")
                .append(" on(").append(_titleNode).append(", \"click\", function(evt) {\n")
                .append("if (domStyle.get(").append(node1).append(", \"display\") !== \"none\") {\n")
                .append(" domClass.replace(evt.currentTarget, \"closed\", \"open\"); \n")
                .append("fx.wipeOut({\n")
                .append("node: ").append(node1).append("\n")
                .append("}).play();\n")
                .append("} else {\n")
                .append(" domClass.replace(evt.currentTarget, \"open\", \"closed\"); \n")
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
                .append("var ").append(node3).append(" = domConstruct.create(\"div\", { class: \"title");
            if (!childItem.getChildren().isEmpty()) {
                js.append(" closed");
            }
            js.append("\"},").append(node2).append(");\n")
                .append(getImage(childItem, node3))
                .append("domConstruct.create(\"span\", { class: \"menutitle\", innerHTML: \"")
                .append(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeHtml4(childItem.getLabel())))
                .append("\"} , ").append(node3).append("); \n")
                .append(getSubMenuItem(childItem, node2, node3));
        }
        return js;
    }

    /**
     * Gets the url.
     *
     * @param _menuItem the menu item
     * @return the url
     */
    private CharSequence getUrl(final UIMenuItem _menuItem)
    {
        final PageParameters pageParameters = new PageParameters();
        final String key = RandomStringUtils.randomAlphabetic(6);
        this.menuItems.put(key, _menuItem);
        pageParameters.add("m", key);
        return urlForListener(pageParameters);
    }

    /**
     * Gets the image.
     *
     * @param _menuItem the menu item
     * @param _node the node
     * @return the image
     */
    private CharSequence getImage(final UIMenuItem _menuItem,
                                  final String _node)
    {
        final String label = _menuItem.getLabel();
        String content = "";
        if (StringUtils.isNotEmpty(label)) {
            content = StringUtils.left(label, 1).toUpperCase();
        }
        final StringBuilder js = new StringBuilder();
        js.append("domConstruct.create(\"span\", {\n")
            .append("class: \"circle ").append(content).append(" \"\n,")
            .append("innerHTML: \"").append(content).append("\"\n")
            .append("}, ").append(_node).append(");\n");
        return js;
    }

    @Override
    public void onRequest()
    {
        final StringValue key = getRequestCycle().getRequest().getQueryParameters().getParameterValue("m");
        final UIMenuItem menuItem = this.menuItems.get(key.toString());

        try {
            final AbstractCommand command = menuItem.getCommand();
            if (command.getTargetTable() != null) {
                if (command.getTargetStructurBrowserField() != null) {
                    final StructurBrowserPage page = new StructurBrowserPage(menuItem.getCommandUUID(), menuItem
                                    .getInstanceKey());
                    setResponsePage(page);
                } else {
                    if ("GridX".equals(Configuration.getAttribute(ConfigAttribute.TABLEDEFAULTTYPECONTENT))) {
                        final GridPage page = new GridPage(Model.of(UIGrid.get(command.getUUID(),
                                        PagePosition.CONTENT)));
                        setResponsePage(page);
                    } else {
                        final TablePage page = new TablePage(menuItem.getCommandUUID(), menuItem.getInstanceKey());
                        setResponsePage(page);
                    }
                }
            } else if (command.getTargetForm() != null) {
                final UIForm uiForm = new UIForm(menuItem.getCommandUUID(), menuItem.getInstanceKey()).setPagePosition(
                                PagePosition.CONTENT);
                final FormPage page = new FormPage(Model.of(uiForm));
                setResponsePage(page);
            }
        } catch (final EFapsException e) {
            SlideIn.LOG.error("Catched", e);
        }
    }
}
