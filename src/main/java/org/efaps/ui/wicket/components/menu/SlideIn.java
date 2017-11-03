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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.Checkout;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.ui.wicket.components.menu.behaviors.AjaxMenuItem;
import org.efaps.ui.wicket.components.menu.behaviors.ExecBehavior;
import org.efaps.ui.wicket.components.menu.behaviors.OpenModalBehavior;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.connection.ConnectionPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.dashboard.DashboardPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.ui.wicket.util.DojoClass;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.ui.wicket.util.RandomUtil;
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
            .append("if (domGeom.getContentBox(evt.currentTarget.parentNode).w > 100) {\n")
            .append(" domClass.replace(evt.currentTarget.parentNode, \"expand\", \"collapse\"); \n")
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
            .append(" domClass.replace(evt.currentTarget.parentNode, \"collapse\", \"expand\"); \n")
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

        final String node1 = RandomUtil.randomAlphabetic(4);
        final String node2 = RandomUtil.randomAlphabetic(4);

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
        final String node1 = RandomUtil.randomAlphabetic(4);

        if (_menuItem.getChildren().isEmpty()) {
            js.append(" on(").append(_titleNode).append(", \"click\", function(evt) {\n")
                .append(getEventJs(_menuItem))
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
            final String node2 = RandomUtil.randomAlphabetic(4);
            final String node3 = RandomUtil.randomAlphabetic(4);
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
    private CharSequence getEventJs(final UIMenuItem _menuItem)
    {
        final String key = RandomUtil.randomAlphabetic(8);
        this.menuItems.put(key, _menuItem);
        final StringBuilder ret = new StringBuilder();
        switch (_menuItem.getTarget()) {
            case HIDDEN:
                ret.append(getBehavior(ExecBehavior.class).getCallbackFunctionBody(CallbackParameter.converted("m",
                                "\"" + key + "\"")));
                break;
            case MODAL:
                ret.append(getBehavior(OpenModalBehavior.class).getCallbackFunctionBody(CallbackParameter.converted("m",
                                "\"" + key + "\"")));
                break;
            case POPUP:
                final PageParameters popupPageParameters = new PageParameters();
                popupPageParameters.add("m", key);
                final PopupSettings popupSettings = new PopupSettings();
                popupSettings.setHeight(_menuItem.getWindowHeight());
                popupSettings.setWidth(_menuItem.getWindowWidth());
                popupSettings.setTarget("'" + urlForListener(popupPageParameters) + "'");
                popupSettings.setWindowName("eFapsPopup");
                ret.append("(function () {").append(popupSettings.getPopupJavaScript()).append("}) ();");
                break;
            default:
                final PageParameters pageParameters = new PageParameters();
                pageParameters.add("m", key);
                ret.append("registry.byId(\"").append("mainPanel").append(
                                "\").set(\"content\", domConstruct.create(\"iframe\", {").append("\"id\": \"")
                        .append(MainPage.IFRAME_ID).append("\",\"src\": \"").append(urlForListener(pageParameters))
                        .append("\",\"style\": \"border: 0; width: 100%; height: 99%\"").append(",\"id\": \"")
                        .append(MainPage.IFRAME_ID).append("\"").append("}));");
                break;
        }
        return ret;
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
        if (StringUtils.isNotEmpty(_menuItem.getImage()) && _menuItem.getImage().endsWith(".svg")) {
            try {
                final QueryBuilder querBldr = new QueryBuilder(CIAdminUserInterface.Image);
                querBldr.addWhereAttrEqValue(CIAdminUserInterface.Image.Name, _menuItem.getImage());
                final InstanceQuery query = querBldr.getQuery();
                query.execute();
                if (query.next()) {
                    final Checkout checkout = new Checkout(query.getCurrentValue());
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();
                    checkout.execute(os);
                    final String svg = new String(os.toByteArray(), StandardCharsets.UTF_8);
                    js.append("domConstruct.create(\"span\", {\n")
                        .append("innerHTML: \"").append(StringEscapeUtils.escapeEcmaScript(svg)).append("\"\n")
                        .append("}, ").append(_node).append(");\n");
                }
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            js.append("domConstruct.create(\"span\", {\n")
                .append("class: \"circle ").append(content).append(" \"\n,")
                .append("innerHTML: \"").append(content).append("\"\n")
                .append("}, ").append(_node).append(");\n");
        }
        return js;
    }

    /**
     * Gets the behavior.
     *
     * @param _class the class
     * @return the behavior
     */
    protected AjaxEventBehavior getBehavior(final Class<? extends Behavior> _class)
    {
        final SlideInPanel panel = (SlideInPanel) getParent();
        return panel.visitChildren(AjaxMenuItem.class, (_item,
         _visit) -> {
            final List<? extends Behavior> behaviors = _item.getBehaviors(_class);
            if (CollectionUtils.isNotEmpty(behaviors)) {
                _visit.stop((AjaxEventBehavior) behaviors.get(0));
            } else {
                _visit.stop();
            }
        });
    }

    @Override
    public void onRequest()
    {
        final StringValue key = getRequestCycle().getRequest().getQueryParameters().getParameterValue("m");
        final UIMenuItem menuItem = this.menuItems.get(key.toString());
        try {
            if (StringUtils.endsWith(menuItem.getReference(), "/home")) {
                setResponsePage(new DashboardPage(getPage().getPageReference()));
            } else if (StringUtils.endsWith(menuItem.getReference(), "/connection")) {
                setResponsePage(new ConnectionPage(getPage().getPageReference()));
            } else {
                final AbstractCommand command = menuItem.getCommand();
                final PagePosition pagePosition = Target.POPUP.equals(menuItem.getTarget())
                                ? PagePosition.POPUP : PagePosition.CONTENT;
                if (command.getTargetTable() != null) {
                    if (command.getTargetStructurBrowserField() != null) {
                        final StructurBrowserPage page = new StructurBrowserPage(Model.of(new UIStructurBrowser(menuItem
                                        .getCommandUUID(), menuItem.getInstanceKey()).setPagePosition(pagePosition)));
                        setResponsePage(page);
                    } else {
                        if ("GridX".equals(Configuration.getAttribute(ConfigAttribute.TABLEDEFAULTTYPECONTENT))) {
                            final GridPage page = new GridPage(Model.of(UIGrid.get(command.getUUID(), pagePosition)));
                            setResponsePage(page);
                        } else {
                            final TablePage page = new TablePage(Model.of(new UITable(menuItem.getCommandUUID(),
                                            menuItem.getInstanceKey()).setPagePosition(pagePosition)));
                            setResponsePage(page);
                        }
                    }
                } else if (command.getTargetForm() != null  || command.getTargetSearch() != null) {
                    final UIForm uiForm = new UIForm(menuItem.getCommandUUID(), menuItem.getInstanceKey())
                                    .setPagePosition(pagePosition);
                    final FormPage page = new FormPage(Model.of(uiForm));
                    setResponsePage(page);
                }
            }
        } catch (final EFapsException e) {
            SlideIn.LOG.error("Catched", e);
        }
    }

    /**
     * Getter method for the instance variable {@link #menuItems}.
     *
     * @return value of instance variable {@link #menuItems}
     */
    public Map<String, UIMenuItem> getMenuItems()
    {
        return this.menuItems;
    }
}
