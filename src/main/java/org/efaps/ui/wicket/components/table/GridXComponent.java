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


package org.efaps.ui.wicket.components.table;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.efaps.admin.ui.Menu;
import org.efaps.api.ui.FilterBase;
import org.efaps.api.ui.FilterType;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.IFilterable;
import org.efaps.ui.wicket.models.field.UIField;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

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
     * Instantiates a new grid component.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     */
    public GridXComponent(final String _wicketId,
                          final IModel<?> _model)
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        add(new PersistAjaxBehavior());
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(CssHeaderItem.forReference(CSS));
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        try {
            final UITable uiTable = (UITable) getDefaultModelObject();

            final StringBuilder js = new StringBuilder()
                .append("<script type=\"text/javascript\">")
                .append("require([")
                .append("'dojo/_base/lang',")
                .append("'dojo/_base/json',")
                .append("'dojo/query',")
                .append("'dojo/dom-geometry',")
                .append("'dojo/window',")
                .append("'dojo/dom-style',")
                .append("'dojo/ready',")
                .append("'dijit/registry',")
                .append("'dojo/store/Memory',")
                .append("'gridx/core/model/cache/Sync',")
                .append("'gridx/Grid',")
                .append("'gridx/modules/VirtualVScroller',")
                .append("'gridx/modules/ColumnResizer',")
                .append("'gridx/modules/HScroller',")
                .append("'gridx/modules/SingleSort',")
                .append("'gridx/modules/move/Column',")
                .append("'gridx/modules/extendedSelect/Column',")
                .append("'gridx/modules/extendedSelect/Cell',")
                .append("'gridx/modules/dnd/Column',")
                .append("'gridx/modules/HiddenColumns',")
                .append("'efaps/HeaderDialog',")
                .append("'efaps/GridConfig',")
                .append("'efaps/GridSort',")
                .append("'gridx/support/Summary',")
                .append("'gridx/support/QuickFilter',")
                .append("'gridx/modules/Bar',")
                .append("'gridx/modules/Filter',")
                .append("'gridx/modules/filter/FilterBar',")
                .append("'gridx/modules/Persist',")
                .append("'dijit/form/DropDownButton',")
                .append("'dijit/form/TextBox',")
                .append("'dijit/TooltipDialog',")
                .append("'dojo/ready'")
                .append("")

                .append("], function(lang, json, query, domGeom, win, domStyle, ready, registry, Memory, Cache, Grid, ")
                .append("VirtualVScroller, ColumnResizer,HScroller, SingleSort, MoveColumn, SelectColumn, SelectCell, ")
                .append("DnDColumn, HiddenColumns, HeaderDialog, GridConfig, GridSort, Summary, QuickFilter, Bar, Persist, Filter,FilterBar, ")
                .append("DropDownButton,TextBox,TooltipDialog,ready")
                .append("){\n")

                .append("var cp = function(_attr, _itemA, _itemB) {\n")
                .append("var strA = _itemA.hasOwnProperty(_attr + '_sort') ? _itemA[_attr + '_sort'] : _itemA[_attr];\n")
                .append("var strB = _itemB.hasOwnProperty(_attr + '_sort') ? _itemB[_attr + '_sort'] : _itemB[_attr];\n")
                .append("return strA < strB ? -1 : (strA > strB ? 1 : 0);\n")
                .append("}\n")

                .append("var store = new Memory({\n")
                .append("data: ")
                .append(GridXComponent.getDataJS(uiTable))
                .append("});\n")
                .append("var structure = [\n");

            boolean first = true;
            int j = 0;
            for (final UITableHeader header: uiTable.getHeaders()) {
                if (first) {
                    first = false;
                } else {
                    js.append(",");
                }
                js.append("{ id:'").append(header.getFieldId()).append("',")
                    .append(" field:'").append(header.getFieldName()).append("',")
                    .append(" name:'").append(header.getLabel()).append("'\n")
               //     .append("formatter: function(obj) {\n")
               //     .append("return obj.").append(header.getFieldName()).append(".v;\n")
               //     .append("},\n")
                    .append(", comparator: cp\n");
                if (header.getFieldConfig().getField().getReference() != null) {
                    js.append(", decorator: function(data, rowId, visualIndex, cell){\n")
                        .append("return '<a href=\"").append(
                                    urlFor(ILinkListener.INTERFACE, new PageParameters()))
                        .append("&rowId=").append("' + rowId + '")
                        .append("&colId=").append(j)
                        .append("\">' + data + '</a>';\n")
                        .append("}\n");
                }
                if (FilterBase.DATABASE.equals(header.getFilter().getBase())) {
                    js.append(", dialog: 'fttd_").append(header.getFieldId()).append("', headerClass:'eFapsFiltered'\n");
                } else if (FilterType.PICKLIST.equals(header.getFilter().getType())) {
                    js.append(", dataType: 'enum'\n");
                       // .append(", enumOptions: [")
                        //.append(StringUtils.join(uiTable.getFilterPickList(header), ","))
                        //.append("]");
                } else {
                   // js.append(", filterable: false\n");
                }
                js.append("}");
                j++;
            }

            js.append("];\n")
                .append("var grid = Grid({")
                .append("id: 'grid',")
                .append("cacheClass: Cache,")
                .append("store: store,")
                .append("structure: structure,\n")
                .append("barTop: [\n")
                    .append("Summary,\n")
                    .append("{pluginClass: QuickFilter, style: 'text-align: center;'}, \n")
                    .append("{pluginClass: GridConfig, style: 'text-align: right;'} \n")
                .append("],\n")
                .append("modules: [\n")
                    .append("VirtualVScroller,\n")
                    .append("ColumnResizer,\n")
                    .append("SingleSort,\n")
                    .append("MoveColumn,\n")
                    .append("SelectColumn,\n")
                    .append("SelectCell,\n")
                    .append("DnDColumn,\n")
                    .append("HeaderDialog,\n")
                    .append("Bar,\n")
                    .append("Filter,\n")
                    .append("HScroller,\n")
                    .append("HiddenColumns,\n")
                    .append("Persist\n")
                .append("],\n")
                .append("persistGet: function(_key) {");

            if (Context.getThreadContext().containsUserAttribute(uiTable.getCacheKey(UITable.UserCacheKey.GRIDX))) {
                js.append("return ").append(Context.getThreadContext().getUserAttribute(
                                uiTable.getCacheKey(UITable.UserCacheKey.GRIDX)));
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
                .append("grid.placeAt('").append(getMarkupId(true)).append("');\n")
                .append("grid.startup();\n")
                .append(" ready(function(){")
                .append(" var bar = query('.eFapsMenuBarPanel') [0];\n")
                .append("var pos = domGeom.position(bar);\n")
                .append("var vs = win.getBox();\n")
                .append("var hh = vs.h - pos.h -pos.y;\n")
                .append("console.log(hh);\n")
                .append("registry.byId('grid').resize({h:hh});")
                .append("")
                .append("});")
                .append("});")
                .append("\n")
                .append("\n</script>");

            replaceComponentTagBody(_markupStream, _openTag, js);
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onLinkClicked()
    {
        final StringValue rowId = getRequest().getRequestParameters().getParameterValue("rowId");
        final StringValue colId = getRequest().getRequestParameters().getParameterValue("colId");

        try {
            final UITable uiTable = (UITable) getDefaultModelObject();
            final UIRow row = uiTable.getValues().get(rowId.toInt());
            final IFilterable cell = row.getCells().get(colId.toInt());
            final UIField uiField = (UIField) cell;

            if (uiField.getInstanceKey() != null) {
                Menu menu = null;
                final Instance instance;
                try {
                    instance = uiField.getInstance();
                    menu = Menu.getTypeTreeMenu(instance.getType());
                } catch (final EFapsException e) {
                    throw new RestartResponseException(new ErrorPage(e));
                }
                if (menu == null) {
                    final Exception ex = new Exception("no tree menu defined for type " + instance.getType().getName());
                    throw new RestartResponseException(new ErrorPage(ex));
                }

                Page page;
                try {
                    page = new ContentContainerPage(menu.getUUID(), uiField.getInstanceKey(),
                                    getPage() instanceof StructurBrowserPage);
                } catch (final EFapsException e) {
                    page = new ErrorPage(e);
                }
                this.setResponsePage(page);
            }
        } catch (final StringValueConversionException | EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
                    final UITable uiTable = (UITable) getComponent().getDefaultModelObject();

                    Context.getThreadContext().setUserAttribute(uiTable.getCacheKey(UITable.UserCacheKey.GRIDX), value
                                .toString());
                }
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    /**
     * Gets the data JS.
     *
     * @param _uiTable the ui table
     * @return the data JS
     * @throws EFapsException on error
     */
    public static CharSequence getDataJS(final UITable _uiTable)
        throws EFapsException
    {
        final StringBuilder ret = new StringBuilder().append(" [\n");
        int i = 0;
        for (final UIRow row : _uiTable.getValues()) {
            if (i > 0) {
                ret.append(",\n");
            }
            ret.append("{ id:").append(i);
            for (final IFilterable uiCell : row.getCells()) {
                final String val = ((UIField) uiCell).getFactory().getPickListValue((AbstractUIField) uiCell);
                final String orderVal = String.valueOf(((UIField) uiCell).getFactory().getCompareValue(
                                (AbstractUIField) uiCell));

                ret.append(",").append(((UIField) uiCell).getFieldConfiguration().getName()).append(":").append("'")
                                .append(StringEscapeUtils.escapeEcmaScript(val)).append("'");

                if (val != null && !val.equals(orderVal)) {
                    ret.append(",").append(((UIField) uiCell).getFieldConfiguration().getName()).append("_sort:")
                                    .append("'").append(StringEscapeUtils.escapeEcmaScript(orderVal)).append("'");
                }
            }
            ret.append("}");
            i++;
        }
        ret.append("]\n");
        return ret;
    }
}
