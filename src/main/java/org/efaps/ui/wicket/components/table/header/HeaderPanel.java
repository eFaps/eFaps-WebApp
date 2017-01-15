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

package org.efaps.ui.wicket.components.table.header;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.StringValue;
import org.efaps.db.Context;
import org.efaps.ui.wicket.behaviors.dojo.DnDBehavior;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreeTable;
import org.efaps.ui.wicket.models.objects.AbstractUIHeaderObject;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIFieldTable;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * This class renders the Header of a Table.
 *
 * @author The eFaps Team
 */
public class HeaderPanel
    extends Panel
{

    /**
     * Reference to the javascript.
     */
    public static final EFapsContentReference JAVASCRIPT = new EFapsContentReference(HeaderPanel.class,
                    "HeaderPanel.js");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Key of the Properties for this header.
     */
    private final String headerProperties;

    /**
     * StringBuilder used to add javascript.
     */
    private final StringBuilder js = new StringBuilder();

    /**
     * StyleSheet information.
     */
    private final String css;

    /**
     * The Panel the table resides in.
     */
    private final Panel tablePanel;


    /**
     * Name of the table the Header belongs to.
     */
    private String tableName;

    /**
     * @param _wicketId wicket id for this component
     * @param _panel    the Panel the table resides in
     * @throws CacheReloadException on error
     */
    public HeaderPanel(final String _wicketId,
                       final Panel _panel)
        throws CacheReloadException
    {
        this(_wicketId, _panel, _panel.getDefaultModel());
    }

    /**
     * @param _wicketId wicket id for this component
     * @param _panel    the Panel the table resides in
     * @param _model    model for this panel
     * @throws CacheReloadException on error
     */
    public HeaderPanel(final String _wicketId,
                       final Panel _panel,
                       final IModel<?> _model)
        throws CacheReloadException
    {
        super(_wicketId, _model);
        this.tablePanel = _panel;
        final AbstractUIHeaderObject uitable = (AbstractUIHeaderObject) super.getDefaultModelObject();
        if (uitable instanceof UIFieldTable) {
            this.tableName = ((UIFieldTable) _model.getObject()).getName();
        } else {
            this.tableName = uitable.getTable().getName();
        }
        final boolean  dnd = uitable.isDnD();
        this.headerProperties = "eFapsTable" + uitable.getTableId();

        this.add(new AjaxStoreColumnWidthBehavior());
        this.add(new AjaxStoreColumnOrderBehavior());
        this.add(new AjaxReloadTableBehavior());
        this.add(AttributeModifier.append("class", "eFapsTableHeader"));

        if (dnd) {
            final DnDBehavior dndBehavior = DnDBehavior.getSourceBehavior(this.headerProperties);
            dndBehavior.setAppendJavaScript(this.headerProperties + ".storeColumnOrder(getColumnOrder("
                        + this.headerProperties + "));\n" + this.headerProperties + ".reloadTable()\n");
            this.add(dndBehavior);
        }

        final int browserWidth = ((WebClientInfo) getSession().getClientInfo()).getProperties().getBrowserWidth();

        final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
        add(cellRepeater);
        boolean firstcell = false;
        int i = uitable.getTableId();
        // in case of structurbrowser and edit mode add the column for the edit symbol
        if (isStructurBrowser() && uitable.isEditMode()) {
            final HeaderCellPanel cell = new HeaderCellPanel(cellRepeater.newChildId(), false,
                            "eFapsTableHeaderCell eFapsTableCellEdit", i);
            cell.setOutputMarkupId(true);
            cellRepeater.add(cell);
            i++;
        }
        if (uitable.isShowCheckBoxes()) {
            final HeaderCellPanel cell = new HeaderCellPanel(cellRepeater.newChildId(), true,
                            "eFapsTableCheckBoxCell", i);
            cell.setOutputMarkupId(true);
            cellRepeater.add(cell);
            i++;
            firstcell = true;
        }
        // add the add/remove buttons in edit mode for normal tables
        if (uitable.isEditable() && !isStructurBrowser()) {
            final HeaderCellPanel cell = new HeaderCellPanel(cellRepeater.newChildId(), false,
                            "eFapsTableRemoveRowCell", i);
            cell.setOutputMarkupId(true);
            cellRepeater.add(cell);
            i++;
            firstcell = true;
        }

        final List<String> widthsTmp = new ArrayList<>();

        for (int j = 0; j < uitable.getHeaders().size(); j++) {
            final UITableHeader uiHeader = uitable.getHeaders().get(j);

            final HeaderCellPanel cell = new HeaderCellPanel(cellRepeater.newChildId(), Model.of(uiHeader), uitable);

            if (uiHeader.isFixedWidth()) {
                widthsTmp.add(".eFapsCellFixedWidth" + i + "{width: " + uiHeader.getFieldConfig().getWidth() + "; }\n");
                if (firstcell) {
                    firstcell = false;
                    cell.add(AttributeModifier.append("class", "eFapsTableFirstCell eFapsTableHeaderCell"
                                    + " eFapsCellFixedWidth" + i));
                } else {
                    cell.add(AttributeModifier.append("class", "eFapsTableHeaderCell eFapsCellFixedWidth" + i));
                }
                uiHeader.getWidth();
            } else {
                Integer width = 0;
                if (uitable.isUserSetWidth()) {
                    width = uiHeader.getWidth();
                } else {
                    width = browserWidth / uitable.getWidthWeight() * uiHeader.getWidth();
                }
                widthsTmp.add(".eFapsCellWidth" + i + "{width: " + width.toString() + "px;}\n");
                if (firstcell) {
                    firstcell = false;
                    cell.add(AttributeModifier.append("class", "eFapsTableFirstCell eFapsTableHeaderCell"
                                    + " eFapsCellWidth" + i));
                } else {
                    cell.add(AttributeModifier.append("class", "eFapsTableHeaderCell eFapsCellWidth" + i));
                }
                if (dnd) {
                    cell.add(DnDBehavior.getItemBehavior(this.headerProperties));
                }
            }
            cell.setOutputMarkupId(true);
            cellRepeater.add(cell);

            if (j + 1 < uitable.getHeaders().size() && !uiHeader.isFixedWidth()) {
                boolean add = false;
                for (int k = j + 1; k < uitable.getHeaders().size(); k++) {
                    if (!uitable.getHeaders().get(k).isFixedWidth()) {
                        add = true;
                        break;
                    }
                }
                if (add) {
                    final Seperator seperator = new Seperator(cellRepeater.newChildId(), i, this.headerProperties);
                    cellRepeater.add(seperator);
                    this.js.append("addMoveable(\"").append(seperator.getMarkupId())
                            .append("\", ").append(this.headerProperties).append(");");
                }
            }
            i++;
        }
        this.css = getWidthStyle(widthsTmp);
    }

    /**
     * Gets the Panel the table resides in.
     *
     * @return the Panel the table resides in
     */
    public Panel getTablePanel()
    {
        return this.tablePanel;
    }

    /**
     * @return true it it is a structur browser
     */
    private boolean isStructurBrowser()
    {
        return this.tablePanel instanceof StructurBrowserTreeTable;
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        final AbstractUIHeaderObject uitable = (AbstractUIHeaderObject) super.getDefaultModelObject();
        _response.render(CssHeaderItem.forCSS(this.css, HeaderPanel.class.getName() + "_css_" + uitable.getTableId()));
        _response.render(JavaScriptHeaderItem.forScript(getScript(),
                        HeaderPanel.class.getName() + "_js_" + uitable.getTableId()));
        _response.render(AbstractEFapsHeaderItem.forJavaScript(HeaderPanel.JAVASCRIPT));
    }

    /**
     * @return the javascript
     */
    private String getScript()
    {
        final StringBuilder jsTmp = new StringBuilder()
            .append("require([\"dojo/ready\"]);\n")
            .append("  var ").append(this.headerProperties).append(" = new headerProperties();\n  ")
            .append(this.headerProperties).append(".tableName = \"").append(this.tableName).append("\";\n  ")
            .append(this.headerProperties).append(".headerID = \"").append(this.getMarkupId()).append("\";\n  ")
            .append(this.headerProperties + ".bodyID = \"").append(this.tablePanel.getMarkupId()).append("\";\n  ")
            .append(this.headerProperties + ".modelID = ")
            .append(((AbstractUIHeaderObject) super.getDefaultModelObject()).getTableId()).append(";\n  ")
            .append(this.headerProperties).append(".storeColumnWidths = ")
            .append(this.getBehaviors(AjaxStoreColumnWidthBehavior.class).get(0).getJavaScript())
            .append("; ")
            .append(this.headerProperties).append(".storeColumnOrder = ")
            .append(this.getBehaviors(AjaxStoreColumnOrderBehavior.class).get(0).getJavaScript())
            .append("; ")
            .append(this.headerProperties + ".reloadTable = ")
            .append(this.getBehaviors(AjaxReloadTableBehavior.class).get(0).getJavaScript())
            .append("; ")
            .append("  addOnResizeEvent(function (){ positionTableColumns(")
            .append(this.headerProperties)
            .append(");});\n")
            .append("dojo.ready(function (){ positionTableColumns(").append(this.headerProperties).append(");")
            .append(this.js)
            .append("});\n");
        return jsTmp.toString();
    }

    /**
     * @param _widths withs
     * @return style sheet
     */
    private String getWidthStyle(final List<String> _widths)
    {
        final StringBuilder ret = new StringBuilder();
        ret.append(".eFapsCSSId").append(((AbstractUIHeaderObject) super.getDefaultModelObject()).getTableId())
                        .append("{}\n");
        for (final String width : _widths) {
            ret.append(width);
        }
        return ret.toString();
    }

    /**
     * Behavior to storr the width of columns in the server.
     */
    public class AjaxStoreColumnWidthBehavior
        extends AbstractDefaultAjaxBehavior
    {

        /**
         * String used as Variablename in the Javascript.
         */
        public static final String COLUMNW_PARAMETERNAME = "eFapsColumnWidths";

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @return the javascript
         */
        public String getJavaScript()
        {
            return getCallbackFunction(
                            CallbackParameter.explicit(HeaderPanel.AjaxStoreColumnWidthBehavior.COLUMNW_PARAMETERNAME))
                            .toString();
        }

        /**
         * Method stores the width string in the context of the current user.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void respond(final AjaxRequestTarget _target)
        {
            final StringValue widths = getComponent().getRequest().getRequestParameters().getParameterValue(
                            HeaderPanel.AjaxStoreColumnWidthBehavior.COLUMNW_PARAMETERNAME);
            try {
                Context.getThreadContext().setUserAttribute(
                                ((AbstractUIHeaderObject) getComponent().getDefaultModelObject())
                                                .getCacheKey(AbstractUIHeaderObject.UserCacheKey.COLUMNWIDTH),
                                widths.toString());
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
        }
    }

    /**
     * Behavior to store the coolun order.
     */
    public class AjaxStoreColumnOrderBehavior
        extends AbstractDefaultAjaxBehavior
    {

        /**
         * String used as Variablename in the Javascript.
         */
        public static final String COLUMNORDER_PARAMETERNAME = "eFapsColumnOrder";

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @return the javascript
         */
        public String getJavaScript()
        {
            return getCallbackFunction(
                         CallbackParameter.explicit(HeaderPanel.AjaxStoreColumnOrderBehavior.COLUMNORDER_PARAMETERNAME))
                           .toString();
        }

        /**
         * Method stores the width string in the context of the current user.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void respond(final AjaxRequestTarget _target)
        {
            final String order = getComponent().getRequest().getRequestParameters().getParameterValue(
                            HeaderPanel.AjaxStoreColumnOrderBehavior.COLUMNORDER_PARAMETERNAME).toString();
            ((AbstractUIHeaderObject) getComponent().getDefaultModelObject()).setColumnOrder(order);
        }
    }

    /**
     * Behavior to reload a table.
     */
    public class AjaxReloadTableBehavior
        extends AbstractDefaultAjaxBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @return javascript
         */
        public String getJavaScript()
        {
            final StringBuilder ret = new StringBuilder();
            ret.append("  function(){\n    ").append(getCallbackScript()).append("\n  }\n");
            return ret.toString();
        }

        /**
         * Reload the table.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void respond(final AjaxRequestTarget _target)
        {
            final AbstractUIObject modelObject = (AbstractUIObject) getComponent().getDefaultModelObject();
            modelObject.resetModel();
            try {
                AbstractContentPage page;
                if (getPage() instanceof TablePage) {
                    page = new TablePage(Model.of(modelObject),
                                    ((AbstractContentPage) getPage()).getModalWindow(),
                                    ((AbstractContentPage) getPage()).getCalledByPageReference());
                } else if (getPage() instanceof StructurBrowserPage) {
                    page = new StructurBrowserPage(Model.of(modelObject),
                                    ((AbstractContentPage) getPage()).getModalWindow(),
                                    ((AbstractContentPage) getPage()).getCalledByPageReference());
                } else {
                    page = new FormPage(Model.of((UIForm) getPage().getDefaultModelObject()),
                                    ((AbstractContentPage) getPage()).getModalWindow(),
                                    ((AbstractContentPage) getPage()).getCalledByPageReference());
                }
                getComponent().setResponsePage(page);

            } catch (final EFapsException e) {
                getComponent().setResponsePage(new ErrorPage(e));
            }
        }
    }
}
