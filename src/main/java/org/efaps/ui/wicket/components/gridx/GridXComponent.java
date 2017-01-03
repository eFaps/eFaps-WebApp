/*
 * Copyright 2003 - 2016 The eFaps Team
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


package org.efaps.ui.wicket.components.gridx;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.AbstractMenu;
import org.efaps.admin.ui.Menu;
import org.efaps.api.ui.FilterBase;
import org.efaps.api.ui.HRef;
import org.efaps.db.Context;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.gridx.behaviors.CallUpdateTreeMenuBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.CheckoutBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.OpenModalBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.PrintBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.ReloadBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.SubmitBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.SubmitModalBehavior;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UIGrid.Cell;
import org.efaps.ui.wicket.models.objects.UIGrid.Column;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.ui.wicket.util.DojoClass;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class GridXComponent
    extends WebComponent
    implements ILinkListener
{

    /**
     * Reference to the stylesheet.
     */
    public static final ResourceReference CSS = new CssResourceReference(AbstractDojoBehavior.class,
                    "gridx/resources/claro/Gridx.css");

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GridXComponent.class);

    /**
     * Instantiates a new grid component.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     */
    public GridXComponent(final String _wicketId,
                          final IModel<UIGrid> _model)
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        add(new PersistAjaxBehavior());
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(CssHeaderItem.forReference(GridXComponent.CSS));
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        try {
            final UIGrid uiGrid = (UIGrid) getDefaultModelObject();

            final Set<DojoClass> dojoClasses = new HashSet<>();

            Collections.addAll(dojoClasses, DojoClasses.array, DojoClasses.lang, DojoClasses.json, DojoClasses.aspect,
                            DojoClasses.query, DojoClasses.domConstruct, DojoClasses.win, DojoClasses.domStyle,
                            DojoClasses.ready, DojoClasses.on, DojoClasses.registry, DojoClasses.Memory,
                            DojoClasses.Grid, DojoClasses.VirtualVScroller, DojoClasses.ColumnResizer,
                            DojoClasses.HScroller, DojoClasses.SingleSort, DojoClasses.SyncCache,
                            DojoClasses.HeaderDialog, DojoClasses.MoveColumn, DojoClasses.SelectColumn,
                            DojoClasses.SelectCell, DojoClasses.DnDColumn, DojoClasses.HiddenColumns,
                            DojoClasses.GridConfig, DojoClasses.GridSort, DojoClasses.Summary, DojoClasses.QuickFilter,
                            DojoClasses.Bar, DojoClasses.Persist, DojoClasses.Filter, DojoClasses.FilterBar,
                            DojoClasses.DropDownButton, DojoClasses.TextBox, DojoClasses.TooltipDialog,
                            DojoClasses.ready, DojoClasses.domGeom, DojoClasses.ColumnLock);

            final StringBuilder js = new StringBuilder()
                .append("var cp = function(_attr, _itemA, _itemB) {\n")
                .append("var strA = _itemA.hasOwnProperty(_attr + '_sort')")
                    .append(" ? _itemA[_attr + '_sort'] : _itemA[_attr];\n")
                .append("var strB = _itemB.hasOwnProperty(_attr + '_sort') ")
                    .append("? _itemB[_attr + '_sort'] : _itemB[_attr];\n")
                .append("return strA < strB ? -1 : (strA > strB ? 1 : 0);\n")
                .append("}\n")
                .append("var store = new Memory({\n")
                .append("data: ")
                .append(GridXComponent.getDataJS(uiGrid))
                .append("});\n")
                .append("var structure = [\n");

            boolean first = true;
            int j = 0;
            final Set<Long> checkOutCols = new HashSet<>();
            for (final Column column : uiGrid.getColumns()) {
                if (first) {
                    first = false;
                } else {
                    js.append(",");
                }
                js.append("{ id:'").append(column.getField().getId()).append("',")
                    .append(" field:'").append(column.getFieldName()).append("',")
                    .append(" name:'").append(column.getLabel()).append("'\n");

                if (!"left".equals(column.getField().getAlign())) {
                    js.append(", style:'text-align:right'");
                }
                js.append(", comparator: cp\n");
                if (column.getFieldConfig().getField().getReference() != null
                                && StringUtils.containsIgnoreCase(column.getFieldConfig().getField().getReference(),
                                                HRef.CHECKOUT.toString())) {
                    final StaticImageComponent icon = new StaticImageComponent("icon",
                                    column.getFieldConfig().getField().getIcon());
                    js.append(", decorator: function(data, rowId, visualIndex, cell){\n")
                        .append("return '<span class=\"eFapsCheckout\"><img src=\"").append(icon.getUrl())
                            .append("\"></img>' + data + '</span>';\n")
                        .append("}\n");
                    checkOutCols.add(column.getFieldConfig().getField().getId());
                } else if (column.getFieldConfig().getField().getReference() != null) {
                    js.append(", decorator: function(data, rowId, visualIndex, cell){\n")
                        .append("return '<a href=\"").append(
                                urlFor(ILinkListener.INTERFACE, new PageParameters()))
                        .append("&rowId=").append("' + rowId + '")
                        .append("&colId=").append(j)
                        .append("\">' + data + '</a>';\n")
                        .append("}\n");
                }
                if (FilterBase.DATABASE.equals(column.getFilter().getBase())) {
                    js.append(", dialog: 'fttd_").append(column.getField().getId())
                        .append("', headerClass:'eFapsFiltered'\n");
                }
                if (column.getDataType() != null) {
                    js.append(", dataType: '").append(column.getDataType()).append("'\n");
                    switch (column.getDataType()) {
                        case "date":
                            Configuration.getAttribute(Configuration.ConfigAttribute.FORMAT_DATE);

                            js.append(", dateParser: function(_value){ \n")
                                .append(" var pattern = /(\\d\\d?)\\/(\\d{2})\\/(\\d{4})/;\n")
                                .append("pattern.test(_value);\n")
                                .append("var d = new Date();\n")
                                .append("d.setDate(parseInt(RegExp.$1));\n")
                                .append("d.setMonth(parseInt(RegExp.$2) - 1);\n")
                                .append("d.setFullYear(parseInt(RegExp.$3));\n")
                                .append("console.log(d);\n")
                                .append("return d;\n")
                                .append("}\n");
                            break;
                        default:
                            break;
                    }
                }
                js.append("}");
                j++;
            }

            js.append("];\n")
                .append("").append(getMenu(dojoClasses))
                .append("var grid = Grid({")
                .append("id: 'grid',")
                .append("cacheClass: Cache,")
                .append("store: store,")
                .append("structure: structure,\n")
                .append("barTop: [\n");

            if (dojoClasses.contains(DojoClasses.MenuBar)) {
                js.append("{plugin: pMenuBar, style: 'text-align: left;'}, \n");
            }

            js.append("{pluginClass: QuickFilter, style: 'text-align: center;'}, \n")
                    .append("{ pluginClass: \"efaps/GridConfig\", style: 'text-align: right;', printItems: [")
                    .append(getPrintMenuItems()).append("],\n")
                    .append("reload : ").append(getBehavior(ReloadBehavior.class).getCallbackFunction())
                    .append("} \n")
                    .append("],\n")
                .append("barBottom: [\n")
                    .append("{pluginClass: Summary, style: 'text-align: right;'}\n")
                    .append("],\n")
                .append("modules: [\n")
                    .append("VirtualVScroller,\n")
                    .append("ColumnLock,\n")
                    .append("ColumnResizer,\n")
                    .append("SingleSort,\n")
                    .append("MoveColumn,\n")
                    .append("SelectColumn,\n")
                    .append("SelectCell,\n")
                    .append("DnDColumn,\n")
                    .append("HeaderDialog,\n")
                    .append("Bar,\n")
                    .append("Filter,\n")
                    .append("FilterBar,\n")
                    .append("HScroller,\n")
                    .append("HiddenColumns,\n")
                    .append("Persist\n");

            if (uiGrid.isShowCheckBoxes()) {

                Collections.addAll(dojoClasses, DojoClasses.IndirectSelect, DojoClasses.RowHeader,
                                DojoClasses.SelectRow);

                js.append(", IndirectSelect,\n")
                    .append("SelectRow,\n")
                    .append("RowHeader,\n");
            }

            js.append("],\n")
                .append("persistGet: function(_key) {");

            if (Context.getThreadContext().containsUserAttribute(uiGrid.getCacheKey(UIGrid.CacheKey.GRIDX))) {
                js.append("return ").append(Context.getThreadContext().getUserAttribute(
                                uiGrid.getCacheKey(UIGrid.CacheKey.GRIDX)));
            }

            js.append("},\n")
                .append("persistPut: function(_key, _value, _options) {\n")
                .append("var value;")
                .append("if(_value && lang.isObject(_value)){\n")
                .append("value = json.toJson(_value);\n")
                .append("}else{\n")
                .append("value = {expires: -1};\n")
                .append("}\n")

                .append(getBehaviors(PersistAjaxBehavior.class).get(0).getCallbackFunctionBody(
                                CallbackParameter.explicit("value")))
                .append("},\n")
                .append("modelExtensions: [\n")
                    .append("GridSort\n")
                    .append("]\n")
                .append("});")
                .append("grid.placeAt('").append(getMarkupId(true)).append("');\n");

            for (final Long checkOutCol : checkOutCols) {
                js.append("aspect.after(grid.body,'onAfterRow', function(_row){\n")
                    .append("var rowId = _row.id;\n")
                    .append("var cell =  _row.cell('").append(checkOutCol).append("', false);\n")
                    .append("var colId = cell.column.index();\n")
                    .append("query(\".eFapsCheckout\", cell.node()).on(\"click\", function(e) {\n")
                    .append(getBehavior(CheckoutBehavior.class).getCallbackFunctionBody(
                                CallbackParameter.explicit("rowId"), CallbackParameter.explicit("colId")))
                    .append("});\n")
                .append("},true);\n");
            }

            js.append("grid.startup();\n")
                .append("ready(function(){")
                .append("var bar = query('.eFapsFrameTitle') [0];\n")
                .append("var pos = domGeom.position(bar);\n")
                .append("var vs = win.getBox();\n")
                .append("var hh = vs.h - pos.h -pos.y;\n")
                .append("registry.byId('grid').resize({h:hh});")
                .append("});")
                .append("on(window, 'resize', function() {\n")
                .append("var bar = query('.eFapsFrameTitle') [0];\n")
                .append("var pos = domGeom.position(bar);\n")
                .append("var vs = win.getBox();\n")
                .append("var hh = vs.h - pos.h -pos.y;\n")
                .append("registry.byId('grid').resize({h:hh});")
                .append("});\n");
            if (uiGrid.isShowCheckBoxes()) {
                js.append("aspect.after(grid.select.row, 'onSelectionChange', function (_defferd) {\n")
                    .append("query(\"input[name='selectedRow']\").forEach(domConstruct.destroy);\n")
                    .append("array.forEach(registry.byId('grid').select.row.getSelected(), function (_item) {\n")
                    .append("domConstruct.create('input', {\n")
                    .append("type: 'hidden',\n")
                    .append("name:'selectedRow',\n")
                    .append("value: _item\n")
                    .append("},\n")
                    .append("'").append(findParent(Form.class).getMarkupId(true)).append("');\n")
                    .append("});\n")
                    .append("});\n")
                    .append("grid.prevSelected = [];\n")
                    .append("var ftb = function () {\n")
                    .append("registry.byId('grid').prevSelected = registry.byId('grid').select.row.getSelected();\n")
                    .append("};\n")
                    .append("var fta = function () {\n")
                    .append("array.forEach(registry.byId('grid').prevSelected, function (_item) {\n")
                    .append("registry.byId('grid').select.row.selectById(_item);\n")
                    .append("});\n")
                    .append("};\n")
                    .append("aspect.before(grid.filter, 'setFilter', ftb);\n")
                    .append("aspect.before(grid.filter, 'clearFilter', ftb);\n")
                    .append("aspect.after(grid.filter, 'setFilter', fta);\n")
                    .append("aspect.after(grid.filter, 'clearFilter', fta);\n");
            }

            final StringBuilder html = new StringBuilder().append("<script type=\"text/javascript\">")
                            .append(DojoWrapper.require(js, "efaps/gridxLayer",
                                            dojoClasses.toArray(new DojoClass[dojoClasses.size()])))
                            .append("\n</script>");

            replaceComponentTagBody(_markupStream, _openTag, html);
        } catch (final EFapsException e) {
            GridXComponent.LOG.error("Catched error", e);
        }
    }

    /**
     * Gets the prints the menu items.
     *
     * @return the prints the menu items
     */
    protected CharSequence getPrintMenuItems()
    {
        final StringBuilder ret = new StringBuilder();
        final PrintBehavior printBehavior = (PrintBehavior) getBehavior(PrintBehavior.class);
        final String[] mimes = new String[] {"PDF", "XLS"};
        for (final String mime : mimes) {
            if (ret.length() > 0) {
                ret.append(",");
            }
            ret.append("new MenuItem({\n")
                .append("label: \"").append(mime).append("\",\n")
                .append("iconClass:\"eFapsMenuIcon eFapsMenuIcon").append(mime.toUpperCase()).append("\",\n")
                .append("onClick: function(event) {\n")
                    .append("var g = registry.byId('grid');\n")
                    .append("var sr = g.select.row.getSelected();\n")
                    .append("if (sr.length == 0) {\n")
                    .append("array.forEach(g.rows(), function (_item, _id) {\n")
                    .append("sr[_id] = _item.id;\n")
                    .append("});\n")
                    .append("}\n")
                    .append("var cm = [];\n")
                    .append("array.forEach(g.columns(), function (_item, _id) {\n")
                    .append("cm[_id] = _item.id;\n")
                    .append("});\n")
                    .append(printBehavior.getCallbackFunctionBody(
                            CallbackParameter.resolved("MIME", "\"" + mime + "\""), CallbackParameter.explicit("sr"),
                            CallbackParameter.explicit("cm")))
                    .append("}\n")
                .append("})\n");
        }
        return ret;
    }

    /**
     * Gets the menu.
     *
     * @param _dojoClasses the dojo classes
     * @return the menu
     * @throws EFapsException on error
     */
    protected CharSequence getMenu(final Set<DojoClass> _dojoClasses)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        final UIGrid uiGrid = (UIGrid) getDefaultModelObject();
        if (uiGrid.getCommand().getTargetMenu() != null) {
            Collections.addAll(_dojoClasses, DojoClasses.MenuBar, DojoClasses.DropDownMenu, DojoClasses.MenuItem,
                            DojoClasses.PopupMenuBarItem, DojoClasses.MenuBarItem);

            ret.append("var pMenuBar = new MenuBar({});\n");
            for (final AbstractCommand child : uiGrid.getCommand().getTargetMenu().getCommands()) {
                if (child instanceof AbstractMenu) {
                    ret.append(getSubMenu((AbstractMenu) child, "pMenuBar"));
                } else {
                    ret.append("pMenuBar.addChild(").append(getMenuItem(child, true)).append(");\n");
                }
            }
        }
        return ret;
    }

    /**
     * Gets the sub menu.
     *
     * @param _menu the menu
     * @param _parent the parent
     * @return the sub menu
     */
    protected CharSequence getSubMenu(final AbstractMenu _menu,
                                      final String _parent)
    {
        final String var = RandomStringUtils.randomAlphabetic(4);
        final StringBuilder js = new StringBuilder();
        js.append("var ").append(var).append(" = new DropDownMenu({});\n");

        for (final AbstractCommand child : _menu.getCommands()) {
            if (child instanceof AbstractMenu) {
                js.append(getSubMenu((AbstractMenu) child, var));
            } else {
                js.append(var).append(".addChild(")
                    .append(getMenuItem(child, false))
                    .append(");\n");
            }
        }
        js.append(_parent).append(".addChild(new PopupMenuBarItem({\n")
            .append("label: \"").append(StringEscapeUtils.escapeEcmaScript(_menu.getLabelProperty())).append("\",\n")
            .append(" popup: ").append(var).append("\n")
            .append(" }));\n");
        return js;
    }

    /**
     * Gets the menu item.
     *
     * @param _cmd the cmd
     * @param _menuBar the menu bar
     * @return the menu item
     */
    protected CharSequence getMenuItem(final AbstractCommand _cmd,
                                       final boolean _menuBar)
    {
        final UIGrid uiGrid = (UIGrid) getDefaultModelObject();
        final String rid = uiGrid.getRandom4ID(_cmd.getId());
        final StringBuilder js = new StringBuilder();
        if (_menuBar) {
            js.append("new MenuBarItem({\n");
        } else {
            js.append("new MenuItem({\n");
        }
        js.append(" label: \"")
                .append(StringEscapeUtils.escapeEcmaScript(_cmd.getLabelProperty())).append("\",\n")
                .append("onClick: function(event) {\n")
                .append("var rid = \"").append(rid).append("\";\n");

        if (Target.MODAL.equals(_cmd.getTarget())) {
            if (_cmd.isSubmit()) {
                js.append(getBehavior(SubmitModalBehavior.class).getCallbackFunctionBody(
                                CallbackParameter.explicit("rid")));
            } else {
                js.append(getBehavior(OpenModalBehavior.class).getCallbackFunctionBody(
                                CallbackParameter.explicit("rid")));
            }
        } else if (_cmd.isSubmit()) {
            js.append(getBehavior(SubmitBehavior.class).getCallbackFunctionBody(CallbackParameter.explicit("rid")));
        }
        js.append("}\n").append("})\n");
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
        final GridXPanel panel = (GridXPanel) getParent();
        return panel.visitChildren(MenuItem.class, new IVisitor<MenuItem, AjaxEventBehavior>()
        {

            @Override
            public void component(final MenuItem _item,
                                  final IVisit<AjaxEventBehavior> _visit)
            {
                final List<? extends Behavior> behaviors = _item.getBehaviors(_class);
                if (CollectionUtils.isNotEmpty(behaviors)) {
                    _visit.stop((AjaxEventBehavior) behaviors.get(0));
                } else {
                    _visit.stop();
                }
            }
        });
    }

    @Override
    public void onLinkClicked()
    {
        final StringValue rowId = getRequest().getRequestParameters().getParameterValue("rowId");
        final StringValue colId = getRequest().getRequestParameters().getParameterValue("colId");

        try {
            final UIGrid uiGrid = (UIGrid) getDefaultModelObject();
            final List<Cell> row = uiGrid.getValues().get(rowId.toInt());
            final Cell cell = row.get(colId.toInt());

            if (cell.getInstance() != null) {
                Menu menu = null;
                try {
                    menu = Menu.getTypeTreeMenu(cell.getInstance().getType());
                } catch (final EFapsException e) {
                    throw new RestartResponseException(new ErrorPage(e));
                }
                if (menu == null) {
                    final Exception ex = new Exception("no tree menu defined for type "
                                    + cell.getInstance() == null ? "??"
                                                    : cell.getInstance().getType().getName());
                    throw new RestartResponseException(new ErrorPage(ex));
                }

                Page page;
                try {
                    switch (uiGrid.getPagePosition()) {
                        case TREE:
                            if (menu.getTargetTable() != null) {
                                if (menu.getTargetStructurBrowserField() == null) {
                                    if ("GridX".equals(Configuration.getAttribute(
                                                    ConfigAttribute.TABLEDEFAULTTYPETREE))) {
                                        page = new GridPage(Model.of(UIGrid.get(menu.getUUID(), PagePosition.TREE)
                                                        .setCallInstance(cell.getInstance())));
                                    } else {
                                        page = new TablePage(menu.getUUID(), cell.getInstance().getOid());
                                    }
                                } else {
                                    page = new StructurBrowserPage(menu.getUUID(), cell.getInstance().getOid());
                                }
                            } else {
                                page = new FormPage(menu.getUUID(), cell.getInstance().getOid());
                            }
                            page.add(new CallUpdateTreeMenuBehavior(cell.getInstance()));
                            break;
                        case CONTENT:
                        default:
                            page = new ContentContainerPage(menu.getUUID(), cell.getInstance().getKey(), false);
                            break;
                    }
                } catch (final EFapsException e) {
                    page = new ErrorPage(e);
                }
                this.setResponsePage(page);
            }
        } catch (final StringValueConversionException | EFapsException e) {
            GridXComponent.LOG.error("Catched error", e);
        }
    }

    /**
     * Gets the data JS.
     *
     * @param _uiGrid the ui grid
     * @return the data JS
     * @throws EFapsException on error
     */
    public static CharSequence getDataJS(final UIGrid _uiGrid)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder().append(" [\n");
        int i = 0;
        for (final List<Cell> row : _uiGrid.getValues()) {
            if (i > 0) {
                ret.append(",\n");
            }
            ret.append("{ id:").append(i);
            for (final Cell cell : row) {

                ret.append(",").append(cell.getFieldConfig().getName()).append(":").append("'")
                    .append(StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeHtml4(cell.getValue())))
                    .append("'");

                final Comparable<?> orderObject = (Comparable<?>) cell.getSortValue();
                if (orderObject != null) {
                    final String orderVal;
                    final boolean ps;
                    if (orderObject instanceof Number) {
                        orderVal = ((Number) orderObject).toString();
                        ps = false;
                    } else {
                        orderVal = String.valueOf(orderObject);
                        ps = true;
                    }
                    if (cell.getValue() != null && !cell.getValue().equals(orderVal)) {
                        ret.append(",").append(cell.getFieldConfig().getName()).append("_sort:")
                                        .append(ps ? "'" : "")
                                        .append(StringEscapeUtils.escapeEcmaScript(orderVal))
                                        .append(ps ? "'" : "");
                    }
                }
            }
            ret.append("}");
            i++;
        }
        ret.append("]\n");
        return ret;
    }

    /**
     * Gets the javascript.
     *
     * @param _uiGrid the ui grid
     * @return the javascript
     * @throws EFapsException on error
     */
    public static CharSequence getDataReloadJS(final UIGrid _uiGrid)
        throws EFapsException
    {
        final StringBuilder js = new StringBuilder()
                        .append("var grid = registry.byId('grid');\n").append("var items= ")
                        .append(GridXComponent.getDataJS(_uiGrid)).append("grid.model.clearCache();\n")
                        .append("grid.model.store.setData(items);\n")
                        .append("grid.body.refresh();\n");
        return DojoWrapper.require(js, DojoClasses.registry);
    }

    /**
     * The Class AjaxBehavior.
     *
     * @author The eFaps Team
     */
    public static class PersistAjaxBehavior
        extends AbstractDefaultAjaxBehavior
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        protected void respond(final AjaxRequestTarget _target)
        {
            try {
                final StringValue value = getComponent().getRequest().getRequestParameters().getParameterValue(
                                "value");
                if (!value.isEmpty()) {
                    final UIGrid uiGrid = (UIGrid) getComponent().getDefaultModelObject();
                    Context.getThreadContext().setUserAttribute(uiGrid.getCacheKey(UIGrid.CacheKey.GRIDX), value
                                .toString());
                }
            } catch (final EFapsException e) {
                GridXComponent.LOG.error("Catched error", e);
            }
        }
    }
}
