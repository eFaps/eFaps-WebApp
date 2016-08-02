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
            final StringBuilder js = new StringBuilder()
                .append("<script type=\"text/javascript\">")
                .append("require([")
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
                .append("'gridx/modules/select/Column',")
                .append("'gridx/modules/dnd/Column',")
                .append("'gridx/core/model/extensions/FormatSort'")
                .append("], function(query, domGeom, win, domStyle, ready, registry, Memory, Cache, Grid, ")
                .append("VirtualVScroller, ColumnResizer,HScroller, SingleSort, MoveColumn, SelectColumn, DnDColumn, FormatSort){\n")
                .append("var store = new Memory({\n")
                .append("data: [\n");

            int i = 0;
            final UITable uiTable = (UITable) getDefaultModelObject();

            for (final UIRow row : uiTable.getValues()) {
                if (i > 0) {
                    js.append(",\n");
                }
                js.append("{ id:").append(i);
                for (final IFilterable uiCell: row.getCells()) {
                    final String val = ((UIField) uiCell).getFactory().getPickListValue((AbstractUIField) uiCell);
                    final String orderVal = String.valueOf(((UIField) uiCell).getFactory().getCompareValue((AbstractUIField) uiCell));

                    js.append(",").append(((UIField) uiCell).getFieldConfiguration().getName()).append(":")
                        .append("{ val:")
                        .append("'").append(StringEscapeUtils.escapeEcmaScript(val)).append("', orderVal:")
                        .append("'").append(StringEscapeUtils.escapeEcmaScript(orderVal)).append("'")
                        .append("}");
                }
                js.append("}");
                i++;
            }

            js.append("]\n")
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
                    .append(" name:'").append(header.getLabel()).append("',\n")
                    .append("formatter: function(obj) {\n")
                    .append("return obj.").append(header.getFieldName()).append(".val;\n")
                    .append("},\n")
                    .append("comparator: function(item1, item2) {\n")
                    .append(" return item1.orderVal > item2.orderVal;\n")
                    .append("    }\n");

                if (header.getFieldConfig().getField().getReference() != null) {
                    js.append(", decorator: function(data, rowId, visualIndex, cell){\n")
                        .append("return '<a href=\"").append(
                                    urlFor(ILinkListener.INTERFACE, new PageParameters()))
                        .append("&rowId=").append("' + rowId + '")
                        .append("&colId=").append(j)
                        .append("\">' + data + '</a>';\n")
                        .append("}\n");
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
                .append("modules: [\n")
                    .append("VirtualVScroller,\n")
                    .append("ColumnResizer,\n")
                    .append("SingleSort,\n")
                    .append("MoveColumn,\n")
                    .append("SelectColumn,\n")
                    .append("DnDColumn,\n")
                    .append("HScroller")
                .append("],\n")
                .append("modelExtensions: [\n")
                    .append("FormatSort\n")
                    .append("]\n")
                .append("});")
                .append("grid.placeAt('").append(getMarkupId(true)).append("');")
                .append("grid.startup();")

                .append(" ready(function(){")
                .append(" var bar = query('.eFapsMenuBarPanel') [0];\n")
                .append("var pos = domGeom.position(bar);\n")
                .append("var vs = win.getBox();\n")
                .append("var hh = vs.h - pos.h -pos.y;\n")
                .append("console.log(hh);\n")
                .append("registry.byId('grid').resize({h:hh});")
                .append("")
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
        } catch (StringValueConversionException | EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
