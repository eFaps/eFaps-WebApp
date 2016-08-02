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
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.IFilterable;
import org.efaps.ui.wicket.models.field.UIField;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class GridXComponent
    extends WebComponent
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    /**
     * Reference to the stylesheet.
     */
    public static final ResourceReference CSS = new CssResourceReference(AbstractDojoBehavior.class,
                    "gridx/resources/Gridx.css");

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
        final AbstractDefaultAjaxBehavior ajaxBehavior = new AbstractDefaultAjaxBehavior()
        {
            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            protected void respond(final AjaxRequestTarget _target)
            {
                final WebRequest request = (WebRequest) getPage().getRequest();
                final StringValue rowId = request.getRequestParameters().getParameterValue("rowId");
                final StringValue colId = request.getRequestParameters().getParameterValue("colId");

                try {
                    final UITable uiTable = (UITable) getDefaultModelObject();
                    final UIRow row = uiTable.getValues().get(rowId.toInt());
                    final IFilterable cell = row.getCells().get(colId.toInt());
                    System.out.println(cell);
                } catch (StringValueConversionException | EFapsException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        add(ajaxBehavior);
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
                .append("'gridx/modules/Sort',")
                .append("'gridx/modules/move/Column',")
                .append("'gridx/modules/select/Column',")
                .append("'gridx/modules/dnd/Column'")
                .append("], function(query, domGeom, win, domStyle, ready, registry, Memory, Cache, Grid, ")
                .append("VirtualVScroller, ColumnResizer,HScroller, Sort, MoveColumn, SelectColumn, DnDColumn){\n")
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
                        js.append(",").append(((UIField) uiCell).getFieldConfiguration().getName()).append(":")
                            .append("'").append(StringEscapeUtils.escapeEcmaScript(val)).append("'");
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
                        .append(" name:'").append(header.getLabel()).append("'");

                    if (header.getFieldConfig().getField().getReference() != null) {
                        js.append(", decorator: function(data, rowId, visualIndex, cell){\n")
                        .append("return '<a href=\"\" onclick=\"hrefCall(' + rowId + ',").append(j)
                            .append(");return false\">' + data + '</a>';\n")
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
                    .append("Sort,\n")
                    .append("MoveColumn,\n")
                    .append("SelectColumn,\n")
                    .append("DnDColumn,\n")
                    .append("HScroller")
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
                .append("\n");

                final CharSequence callback = getBehaviors(AbstractDefaultAjaxBehavior.class).get(0)
                                .getCallbackFunction(CallbackParameter.explicit("rowId"),
                                                CallbackParameter.explicit("colId"));

                js.append("var hrefCall=").append(callback)
                    .append("\n</script>");
            replaceComponentTagBody(_markupStream, _openTag, js);
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
