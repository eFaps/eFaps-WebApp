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


package org.efaps.ui.wicket.components.gridx;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.github.openjson.JSONTokener;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.AbstractMenu;
import org.efaps.admin.ui.Menu;
import org.efaps.api.ci.UITableFieldProperty;
import org.efaps.api.ui.FilterBase;
import org.efaps.api.ui.HRef;
import org.efaps.db.Context;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.gridx.behaviors.CheckoutBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.OpenInOpenerBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.OpenModalBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.PrintBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.ReloadBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.SubmitBehavior;
import org.efaps.ui.wicket.components.gridx.behaviors.SubmitModalBehavior;
import org.efaps.ui.wicket.components.menutree.CallUpdateTreeMenuBehavior;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIFieldGrid;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UIGrid.Cell;
import org.efaps.ui.wicket.models.objects.UIGrid.Column;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.ui.wicket.util.DateUtil;
import org.efaps.ui.wicket.util.DojoClass;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsException;
import org.efaps.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class GridXComponent
    extends WebComponent
    implements IRequestListener
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
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(CssHeaderItem.forReference(GridXComponent.CSS));
    }

    @Override
    @SuppressWarnings("checkstyle:MethodLength")
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        try {
            final UIGrid uiGrid = (UIGrid) getDefaultModelObject();
            final boolean isField = uiGrid instanceof UIFieldGrid;

            final Set<DojoClass> dojoClasses = new HashSet<>();

            Collections.addAll(dojoClasses, DojoClasses.array, DojoClasses.lang, DojoClasses.json, DojoClasses.aspect,
                            DojoClasses.query, DojoClasses.domConstruct, DojoClasses.win, DojoClasses.domStyle,
                            DojoClasses.ready, DojoClasses.on, DojoClasses.registry, DojoClasses.Memory,
                            DojoClasses.Grid, DojoClasses.VirtualVScroller, DojoClasses.ColumnResizer,
                            DojoClasses.HScroller, DojoClasses.SingleSort, DojoClasses.SyncCache,
                            DojoClasses.HeaderDialog, DojoClasses.MoveColumn, DojoClasses.SelectColumn,
                            DojoClasses.SelectCell, DojoClasses.DnDColumn, DojoClasses.HiddenColumns,
                            DojoClasses.GridConfig, DojoClasses.GridSort, DojoClasses.Summary,
                            DojoClasses.GridQuickFilter, DojoClasses.GridAggregate,
                            DojoClasses.Bar, DojoClasses.Persist, DojoClasses.Filter, DojoClasses.FilterBar,
                            DojoClasses.DropDownButton, DojoClasses.TextBox, DojoClasses.TooltipDialog,
                            DojoClasses.domGeom, DojoClasses.ColumnLock, DojoClasses.DateLocale);

            final DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(
                            Context.getThreadContext().getLocale());
            final char decSep = format.getDecimalFormatSymbols().getDecimalSeparator();
            final char grpSep = format.getDecimalFormatSymbols().getGroupingSeparator();

            final StringBuilder js = new StringBuilder()
                .append("var cpn = function(_attr, _itemA, _itemB) {\n")
                .append("    var valA = _itemA.hasOwnProperty(_attr + '_sort')")
                    .append(" ? _itemA[_attr + '_sort'] : parseFloat(String(_itemA[_attr]).replace('")
                    .append(grpSep).append("',''").append(").replace('").append(decSep).append("','.'").append("));\n")
                .append("    var valB = _itemB.hasOwnProperty(_attr + '_sort') ")
                    .append(" ? _itemB[_attr + '_sort'] : parseFloat(String(_itemB[_attr]).replace('")
                    .append(grpSep).append("',''").append(").replace('").append(decSep).append("','.'").append("));\n")
                .append("    return valA < valB ? -1 : (valA > valB ? 1 : 0);\n")
                .append("}\n")
                .append("var cp = function(_attr, _itemA, _itemB) {\n")
                .append("    var strA = _itemA.hasOwnProperty(_attr + '_sort')")
                    .append(" ? _itemA[_attr + '_sort'] : _itemA[_attr];\n")
                .append("    var strB = _itemB.hasOwnProperty(_attr + '_sort') ")
                    .append("? _itemB[_attr + '_sort'] : _itemB[_attr];\n")
                .append("    return strA < strB ? -1 : (strA > strB ? 1 : 0);\n")
                .append("}\n")
                .append("var dp = function(_value){ \n")
                    .append("return dateLocale.parse(_value, { datePattern: \"").append(DateUtil.getDatePattern())
                        .append("\", selector: \"date\" });")
                    .append("}\n")
                .append("var dtp = function(_value){ \n")
                    .append("return dateLocale.parse(_value, { datePattern: \"").append(DateUtil.getDateTimePattern())
                        .append("\", selector: \"date\", am: \"AM\", pm: \"PM\" });")
                .append("}\n")
                .append("var cpd = function(_attr, _itemA, _itemB) {\n")
                    .append("    return dp(_itemA[_attr]) - dp(_itemB[_attr]);\n")
                    .append("}\n")
                .append("var cpdt = function(_attr, _itemA, _itemB) {\n")
                    .append("    return dtp(_itemA[_attr]) - dtp(_itemB[_attr]);\n")
                    .append("}\n")
                .append("var store = new Memory({\n")
                .append("data: ")
                .append(GridXComponent.getDataJS(uiGrid))
                .append("});\n")
                .append("var structure = [\n");

            boolean first = true;
            boolean aggregate = false;
            int j = 0;
            final Set<Long> checkOutCols = new HashSet<>();
            final Set<Long> linkCols = new HashSet<>();
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
                if (column.getFieldConfig().getField().getReference() != null
                                && StringUtils.containsIgnoreCase(column.getFieldConfig().getField().getReference(),
                                                HRef.CHECKOUT.toString())) {
                    final StaticImageComponent icon = new StaticImageComponent("icon",
                                    new EFapsContentReference(column.getFieldConfig().getField().getIcon()));
                    js.append(", decorator: function(data, rowId, visualIndex, cell){\n")
                        .append("return '<span class=\"eFapsCheckout\"><img src=\"").append(icon.getUrl())
                            .append("\"></img>' + data + '</span>';\n")
                        .append("}\n");
                    checkOutCols.add(column.getFieldConfig().getField().getId());
                } else if (column.getFieldConfig().getField().getReference() != null) {
                    switch (uiGrid.getPagePosition()) {
                        case POPUP:
                            linkCols.add(column.getFieldConfig().getField().getId());
                            js.append(", decorator: function(data, rowId, visualIndex, cell){\n")
                                .append("return '<a href=\"#\">' + data + '</a>';\n")
                                .append("}\n");
                            break;
                        case CONTENT:
                        case TREE:
                            js.append(", decorator: function(data, rowId, visualIndex, cell){\n")
                                .append("return '<a href=\"").append(
                                    urlForListener(new PageParameters()))
                                .append("&rowId=").append("' + rowId + '")
                                .append("&colId=").append(j)
                                .append("\">' + data + '</a>';\n")
                                .append("}\n");
                            break;
                        case CONTENTMODAL:
                        case TREEMODAL:
                        default:
                            break;
                    }
                }
                if (FilterBase.DATABASE.equals(column.getFilter().getBase())) {
                    js.append(", dialog: 'fttd_").append(column.getField().getId())
                        .append("', headerClass:'eFapsFiltered'\n");
                }
                if (column.getDataType() != null) {
                    js.append(", dataType: '").append(column.getDataType()).append("'\n");
                    switch (column.getDataType()) {
                        case "date":
                            js.append(", dateParser: dp,\n")
                                .append("comparator: cpd\n");
                            break;
                        case "datetime":
                            js.append(", datetimeParser: dtp,\n")
                                .append("comparator: cpdt\n");
                            break;
                        case "number":
                            js.append(", comparator: cpn\n");
                            break;
                        case "enum":
                            js.append(", enumOptions: ['").append(StringUtils.join(column.getEnumValues(), "','"))
                                .append("']").append(", comparator: cp\n");
                            break;
                        default:
                            js.append(", comparator: cp\n");
                            break;
                    }
                } else {
                    js.append(", comparator: cp\n");
                }

                if (column.getField().containsProperty(UITableFieldProperty.AGGREGATE)) {
                    aggregate = true;
                    js.append(", aggregate: '")
                        .append(column.getField().getProperty(UITableFieldProperty.AGGREGATE))
                        .append("'");
                }
                js.append("}");
                j++;
            }

            js.append("];\n")
                .append("").append(getMenu(dojoClasses))
                .append("var grid = Grid({")
                .append("id: '").append(getGridId()).append("',")
                .append("cacheClass: Cache,")
                .append("store: store,")
                .append("structure: structure,\n");

            if (isField) {
                js.append("autoHeight: true,\n");
            } else {
                js.append("barTop: [\n");

                if (dojoClasses.contains(DojoClasses.MenuBar)) {
                    js.append("{plugin: pMenuBar, style: 'text-align: left;'}, \n");
                }

                js.append("{pluginClass: GridQuickFilter, style: 'text-align: center;'}, \n")
                    .append("{ pluginClass: \"efaps/GridConfig\", style: 'text-align: right;', printItems: [")
                    .append(getPrintMenuItems(dojoClasses)).append("],\n")
                    .append("reload : ").append(getBehavior(ReloadBehavior.class).getCallbackFunction())
                    .append("} \n")
                    .append("],\n")
                    .append("barBottom: [\n")
                    .append("{pluginClass: Summary, style: 'text-align: right;'}\n")
                    .append("],\n");
            }

            js.append("modules: [")
                .append("VirtualVScroller, ColumnLock, ColumnResizer, SingleSort, MoveColumn, SelectColumn, ")
                .append("SelectCell, DnDColumn, HeaderDialog, Bar, HScroller, HiddenColumns, Persist");

            if (!isField) {
                js.append(", Filter, FilterBar");
            }

            if (aggregate) {
                js.append(", GridAggregate");
            }

            if (uiGrid.isShowCheckBoxes()) {
                Collections.addAll(dojoClasses, DojoClasses.IndirectSelect, DojoClasses.RowHeader,
                                DojoClasses.ExtendedSelectRow);
                js.append(", IndirectSelect, SelectRow, RowHeader");
            }

            js.append("],\n")
                .append(getPersistenceScript(uiGrid))
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

            for (final Long linkCol : linkCols) {
                js.append("aspect.after(grid.body,'onAfterRow', function(_row){\n")
                    .append("var rowId = _row.id;\n")
                    .append("var cell =  _row.cell('").append(linkCol).append("', false);\n")
                    .append("var colId = cell.column.index();\n")
                    .append("query(\"a\", cell.node()).on(\"click\", function(e) {\n")
                    .append(getBehavior(OpenInOpenerBehavior.class).getCallbackFunctionBody(
                                CallbackParameter.explicit("rowId"), CallbackParameter.explicit("colId")))
                    .append("});\n")
                    .append("},true);\n");
            }

            js.append("grid.startup();\n")
                .append("grid.comparators = {\n")
                .append("date : cpd,\n")
                .append("numeric : cpn,\n")
                .append("datetime : cpdt,\n")
                .append("string : cp\n")
                .append("}\n");

            if (!isField) {
                js.append("var rg = function() {\n")
                    .append("var bar = query('.eFapsFrameTitle') [0];\n")
                    .append("var pos = domGeom.position(bar);\n")
                    .append("var vs = win.getBox();\n")
                    .append("var hh = vs.h - pos.h - pos.y;\n")
                    .append("var ft = query('.eFapsFooter')[0];\n")
                    .append("if (typeof ft != 'undefined') {\n")
                    .append("var ftPos = domGeom.position(ft);\n")
                    .append("hh = hh - ftPos.h;\n")
                    .append("\n")
                    .append("}\n")
                    .append("registry.byId('").append(getGridId()).append("').resize({h:hh});")
                    .append("}\n")
                    .append("ready(function(){")
                    .append("rg();\n")
                    .append("});")
                    .append("on(window, 'resize', function() {\n")
                    .append("rg();\n")
                    .append("});\n");
            }

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
                    .append("registry.byId('").append(getGridId())
                        .append("').prevSelected = registry.byId('").append(getGridId())
                        .append("').select.row.getSelected();\n")
                    .append("};\n")
                    .append("var fta = function () {\n")
                    .append("array.forEach(registry.byId('").append(getGridId())
                        .append("').prevSelected, function (_item) {\n")
                    .append("registry.byId('").append(getGridId()).append("').select.row.selectById(_item);\n")
                    .append("});\n")
                    .append("};\n")
                    .append("aspect.before(grid.filter, 'setFilter', ftb);\n")
                    .append("aspect.before(grid.filter, 'clearFilter', ftb);\n")
                    .append("aspect.after(grid.filter, 'setFilter', fta);\n")
                    .append("aspect.after(grid.filter, 'clearFilter', fta);\n");
            }

            final StringBuilder html = new StringBuilder().append("<script type=\"text/javascript\">")
                            .append(DojoWrapper.require(js, dojoClasses.toArray(new DojoClass[dojoClasses.size()])))
                            .append("\n</script>");

            replaceComponentTagBody(_markupStream, _openTag, html);
        } catch (final EFapsException e) {
            GridXComponent.LOG.error("Catched error", e);
        }
    }

    /**
     * Gets the persistence script.
     *
     * @param _uiGrid the ui grid
     * @return the persistence script
     * @throws EFapsException the e faps exception
     */
    protected CharSequence getPersistenceScript(final UIGrid _uiGrid)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("persistGet: function(_key) {");
        if (Context.getThreadContext().containsUserAttribute(_uiGrid.getCacheKey(UIGrid.CacheKey.GRIDX))) {
            final Set<Long> colIds = _uiGrid.getColumns()
                            .stream()
                            .map(col -> col.getField().getId())
                            .collect(Collectors.toSet());
            boolean add = true;
            final JSONObject json = new JSONObject(new JSONTokener(Context.getThreadContext().getUserAttribute(
                            _uiGrid.getCacheKey(UIGrid.CacheKey.GRIDX))));
            final JSONArray columnsArray = json.getJSONArray("column");
            for (int i = 0; i < columnsArray.length(); i++) {
                final JSONObject colObj = (JSONObject) columnsArray.get(i);
                final Long colid = colObj.getLong("id");
                if (!colIds.contains(colid)) {
                    add = false;
                    break;
                }
            }
            if (add && !json.isNull("filterBar")) {
                final JSONObject filterBar =  json.getJSONObject("filterBar");
                if (!filterBar.isNull("conditions")) {
                    final JSONArray conditionsArray = filterBar.getJSONArray("conditions");
                    for (int i = 0; i < conditionsArray.length(); i++) {
                        final JSONObject colObj = (JSONObject) conditionsArray.get(i);
                        if (colObj.has("colId")) {
                            final Long colid = colObj.optLong("colId");
                            if (colid > 0 && !colIds.contains(colid)) {
                                add = false;
                                break;
                            }
                        }
                    }
                }
            }
            if (add) {
                ret.append("return ").append(json.toString());
            }
        }
        ret.append("},\n")
            .append("persistPut: function(_key, _value, _options) {\n")
            .append("  var value;")
            .append("  if(_value && lang.isObject(_value)){\n")
            .append("    value = json.toJson(_value);\n")
            .append("  }else{\n")
            .append("    value = {expires: -1};\n")
            .append("  }\n")
            .append("top.eFaps.persistUserAttr('")
                .append(_uiGrid.getCacheKey(UIGrid.CacheKey.GRIDX)).append("', value);")
            .append("},\n");
        return ret;
    }

    /**
     * Gets the prints the menu items.
     *
     * @param _dojoClasses the dojo classes
     * @return the prints the menu items
     */
    protected CharSequence getPrintMenuItems(final Set<DojoClass> _dojoClasses)
    {
        final StringBuilder ret = new StringBuilder();
        Collections.addAll(_dojoClasses, DojoClasses.MenuItem);
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
        if (!(uiGrid instanceof UIFieldGrid) && uiGrid.getCommand().getTargetMenu() != null) {
            Collections.addAll(_dojoClasses, DojoClasses.MenuBar, DojoClasses.DropDownMenu, DojoClasses.MenuItem,
                            DojoClasses.PopupMenuBarItem, DojoClasses.MenuBarItem);

            ret.append("var pMenuBar = new MenuBar({});\n");
            for (final AbstractCommand child : uiGrid.getCommand().getTargetMenu().getCommands()) {
                if (child.hasAccess(uiGrid.getCommand().getTargetMode(), uiGrid.getInstance())) {
                    if (child instanceof AbstractMenu) {
                        ret.append(getSubMenu((AbstractMenu) child, "pMenuBar"));
                    } else {
                        ret.append("pMenuBar.addChild(").append(getMenuItem(child, true)).append(");\n");
                    }
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
     * @throws EFapsException on error
     */
    protected CharSequence getSubMenu(final AbstractMenu _menu,
                                      final String _parent)
        throws EFapsException
    {
        final String var = RandomUtil.randomAlphabetic(4);
        final StringBuilder js = new StringBuilder();
        js.append("var ").append(var).append(" = new DropDownMenu({});\n");
        final UIGrid uiGrid = (UIGrid) getDefaultModelObject();
        for (final AbstractCommand child : _menu.getCommands()) {
            if (child.hasAccess(uiGrid.getCommand().getTargetMode(), uiGrid.getInstance())) {
                if (child instanceof AbstractMenu) {
                    js.append(getSubMenu((AbstractMenu) child, var));
                } else {
                    js.append(var).append(".addChild(")
                        .append(getMenuItem(child, false))
                        .append(");\n");
                }
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
        return panel.visitChildren(MenuItem.class, (_item,
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
                                        final UITable uiTable = new UITable(menu.getUUID(), cell.getInstance().getOid())
                                                        .setPagePosition(PagePosition.TREE);
                                        page = new TablePage(Model.of(uiTable));
                                    }
                                } else {
                                    page = new StructurBrowserPage(menu.getUUID(), cell.getInstance().getOid());
                                }
                            } else {
                                final UIForm uiForm = new UIForm(menu.getUUID(), cell.getInstance().getOid())
                                                .setPagePosition(PagePosition.TREE);
                                page = new FormPage(Model.of(uiForm));
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
     * Gets the grid id.
     *
     * @return the grid id
     * @throws EFapsException on error
     */
    protected String getGridId()
        throws EFapsException
    {
        return GridXComponent.getGridId((UIGrid) getDefaultModelObject());
    }

    /**
     * Gets the grid id.
     *
     * @param _uiGrid the ui grid
     * @return the grid id
     * @throws EFapsException on error
     */
    public static String getGridId(final UIGrid _uiGrid)
        throws EFapsException
    {
        return _uiGrid instanceof UIFieldGrid ? "grid_" + ((UIFieldGrid) _uiGrid).getFieldTable().getName() : "grid";
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
            .append("var grid = registry.byId('grid');\n")
            .append("var items = ").append(GridXComponent.getDataJS(_uiGrid));

        final StringBuilder dialogJs = new StringBuilder();
        if (!_uiGrid.isColumnsUpToDate()) {
            // lazy setting of data type when first time data
            _uiGrid.setColumnsUpToDate(true);
            js.append("array.forEach(grid.structure, function(entry){\n");
            for (final Column column : _uiGrid.getColumns()) {
                if (column.getDataType() != null) {
                    js.append("if ('").append(column.getField().getId()).append("'== entry.id) {\n")
                        .append("entry.dataType='").append(column.getDataType()).append("';\n")
                        .append("entry.comparator = grid.comparators.").append(column.getDataType()).append(";\n")
                        .append("}\n");
                }
                if (_uiGrid.getFilterList().stream()
                                .filter(filter -> filter.getFieldId() == column.getField().getId())
                                .findFirst().isPresent()) {
                    // to prevent jumping of the modal filter dialog, close and open it
                    final String varName = RandomUtil.randomAlphabetic(4);
                    dialogJs.append("var ").append(varName)
                        .append(" = registry.byId('").append("fttd_" + column.getField().getId()).append("');\n")
                        .append("if (").append(varName).append(" && !(").append(varName)
                            .append(".domNode.offsetHeight == 0 && ")
                            .append(varName).append(".domNode.offsetWidth == 0)) {\n")
                        .append(varName).append(".onBlur();\n")
                        .append("var nl = query(\".gridxHeaderMenuBtn\", dom.byId('grid-")
                            .append(column.getField().getId()).append("'));\n")
                        .append("nl[0].click();\n")
                        .append("}\n");
                }
            }
            js.append("});\n")
                .append("grid.setColumns(grid.structure);\n")
                .append(dialogJs);
        }
        js.append("grid.model.clearCache();\n")
            .append("grid.model.store.setData(items);\n")
            .append("grid.body.refresh();\n");
        return DojoWrapper.require(js, DojoClasses.registry, DojoClasses.array, DojoClasses.dom, DojoClasses.query);
    }
}
