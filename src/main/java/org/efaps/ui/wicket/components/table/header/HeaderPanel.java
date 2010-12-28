/*
 * Copyright 2003 - 2010 The eFaps Team
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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.table.header;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.CssUtils;
import org.apache.wicket.util.string.JavascriptUtils;
import org.efaps.db.Context;
import org.efaps.ui.wicket.behaviors.dojo.DnDBehavior;
import org.efaps.ui.wicket.behaviors.dojo.DojoReference;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.modalwindow.UpdateParentCallback;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * This class renders the Header of a Table.
 *
 * @author The eFaps Team
 * @version $Id:TableHeaderPanel.java 1510 2007-10-18 14:35:40Z jmox $
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
     * Modal used in this header for filter etc.
     */
    private final ModalWindowContainer modal = new ModalWindowContainer("eFapsModal");


    /**
     * the table panel this header panel belongs to.
     */
    private final Component tablepanel;

    /**
     * Properties for this header.
     */
    private final String headerproperties;

    /**
     * @param _wicketId     wicketId fo rhtis component
     * @param _tablePanel   the table panel this header panel belongs to
     */
    public HeaderPanel(final String _wicketId,
                       final TablePanel _tablePanel)
    {
        super(_wicketId, _tablePanel.getDefaultModel());
        this.tablepanel = _tablePanel;
        final UITable uitable = (UITable) super.getDefaultModelObject();
        this.headerproperties = "eFapsTable" + uitable.getTableId();

        this.add(new AjaxStoreColumnWidthBehavior());
        this.add(new AjaxStoreColumnOrderBehavior());
        this.add(new AjaxReloadTableBehavior());
        this.add(new SimpleAttributeModifier("class", "eFapsTableHeader"));

        final DnDBehavior dndBehavior = DnDBehavior.getSourceBehavior(this.headerproperties);
        dndBehavior.setHorizontal(true);
        dndBehavior.setHandles(true);
        dndBehavior.setAppendJavaScript(this.headerproperties + ".storeColumnOrder(getColumnOrder("
                        + this.headerproperties + "));\n" + this.headerproperties + ".reloadTable()\n");
        this.add(dndBehavior);

        final int browserWidth = ((WebClientInfo) getRequestCycle().getClientInfo()).getProperties().getBrowserWidth();

        final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
        add(cellRepeater);
        boolean firstcell = false;
        int i = uitable.getTableId();
        if (uitable.isShowCheckBoxes()) {
            final HeaderCellPanel cell = new HeaderCellPanel(cellRepeater.newChildId(), true,
                            "eFapsTableCheckBoxCell", i);
            cell.setOutputMarkupId(true);
            cellRepeater.add(cell);
            i++;
            firstcell = true;
        }
        if (uitable.isCreateMode() || uitable.isEditMode()) {
            final HeaderCellPanel cell = new HeaderCellPanel(cellRepeater.newChildId(), false,
                            "eFapsTableRemoveRowCell", i);
            cell.setOutputMarkupId(true);
            cellRepeater.add(cell);
            i++;
            firstcell = true;
        }

        final List<String> widths = new ArrayList<String>();

        int fixed = 0;
        for (int j = 0; j < uitable.getHeaders().size(); j++) {
            final UITableHeader uiHeader = uitable.getHeaders().get(j);

            final HeaderCellPanel cell = new HeaderCellPanel(cellRepeater.newChildId(),
                            new UIModel<UITableHeader>(uiHeader), uitable);

            if (uiHeader.isFixedWidth()) {
                widths.add(".eFapsCellFixedWidth" + i + "{width: " + uiHeader.getWidth() + "px; }\n");
                if (firstcell) {
                    firstcell = false;
                    cell.add(new SimpleAttributeModifier("class",  "eFapsTableFirstCell eFapsTableHeaderCell"
                                    + " eFapsCellFixedWidth" + i));
                } else {
                    cell.add(new SimpleAttributeModifier("class",  "eFapsTableHeaderCell eFapsCellFixedWidth" + i));
                }
                fixed += uiHeader.getWidth();
            } else {
                Integer width = 0;
                if (uitable.isUserSetWidth()) {
                    width = uiHeader.getWidth();
                } else {
                    width = browserWidth / uitable.getWidthWeight() * uiHeader.getWidth();
                }
                widths.add(".eFapsCellWidth" + i + "{width: " + width.toString() + "px;}\n");
                if (firstcell) {
                    firstcell = false;
                    cell.add(new SimpleAttributeModifier("class",  "eFapsTableFirstCell eFapsTableHeaderCell"
                                    + " eFapsCellWidth" + i));
                } else {
                    cell.add(new SimpleAttributeModifier("class",  "eFapsTableHeaderCell eFapsCellWidth" + i));
                }
                cell.add(DnDBehavior.getItemBehavior(this.headerproperties));
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
                    cellRepeater.add(new Seperator(cellRepeater.newChildId(), i, this.headerproperties));
                }
            }
            i++;
        }

        add(this.modal);
        this.modal.setPageMapName("modal");
        this.modal.setWindowClosedCallback(new UpdateParentCallback(this, this.modal, false));

        this.add(new StringHeaderContributor(getWidthStyle(widths)));

        this.add(new HeaderContributor(DojoReference.getHeaderContributerforDojo()));
        this.add(StaticHeaderContributor.forJavaScript(HeaderPanel.JAVASCRIPT));
    }

    /**
     * @return the modal window
     */
    public final ModalWindowContainer getModal()
    {
        return this.modal;
    }

    /**
     * @return the javascript
     */
    private String getScript()
    {

        final StringBuilder js = new StringBuilder().append(JavascriptUtils.SCRIPT_OPEN_TAG)
            .append("  var ").append(this.headerproperties).append(" = new headerProperties();\n  ")
            .append(this.headerproperties).append(".headerID = \"").append(this.getMarkupId()).append("\";\n  ")
            .append(this.headerproperties + ".bodyID = \"").append(this.tablepanel.getMarkupId()).append("\";\n  ")
            .append(this.headerproperties + ".modelID = ")
                .append(((UITable) super.getDefaultModelObject()).getTableId()).append(";\n  ")
            .append(this.headerproperties).append(".storeColumnWidths = ")
                .append((this.getBehaviors(AjaxStoreColumnWidthBehavior.class).get(0)).getJavaScript()).append("  ")
                .append(this.headerproperties).append(".storeColumnOrder = ")
                .append((this.getBehaviors(AjaxStoreColumnOrderBehavior.class).get(0)).getJavaScript())
                .append(this.headerproperties + ".reloadTable = ")
                .append((this.getBehaviors(AjaxReloadTableBehavior.class).get(0)).getJavaScript())
                .append("  addOnResizeEvent(function (){positionTableColumns(").append(this.headerproperties)
                .append(");});\n")
            .append("  dojo.addOnLoad(function (){positionTableColumns(" + this.headerproperties + ");});\n")
            .append(JavascriptUtils.SCRIPT_CLOSE_TAG);
        return js.toString();
    }

    /**
     * @param _widths withs
     * @return style sheet
     */
    private String getWidthStyle(final List<String> _widths)
    {

        final StringBuilder ret = new StringBuilder();

        ret.append(CssUtils.INLINE_OPEN_TAG).append(".eFapsCSSId").append(
                        ((UITable) super.getDefaultModelObject()).getTableId()).append("{}\n");
        for (final String width : _widths) {
            ret.append(width);
        }
        ret.append(CssUtils.INLINE_CLOSE_TAG);
        return ret.toString();
    }

    /**
     * Add the script to the header.
     * @see org.apache.wicket.Component#onBeforeRender()
     */
    @Override
    protected void onBeforeRender()
    {
        this.add(new StringHeaderContributor(getScript()));
        super.onBeforeRender();
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
            final StringBuilder ret = new StringBuilder();
            ret.append("function(_widths){\n    ")
                .append(generateCallbackScript("wicketAjaxPost('" + getCallbackUrl(false) + "','"
                            + HeaderPanel.AjaxStoreColumnWidthBehavior.COLUMNW_PARAMETERNAME + "=' + _widths"))
                .append("\n" + "  }\n");
            return ret.toString();
        }

        /**
         * Method stores the width string in the context of the current user.
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void respond(final AjaxRequestTarget _target)
        {
            final String widths = getComponent().getRequest().getParameter(
                            HeaderPanel.AjaxStoreColumnWidthBehavior.COLUMNW_PARAMETERNAME);
            try {
                Context.getThreadContext().setUserAttribute(
                                ((UITable) getComponent().getDefaultModelObject())
                                .getCacheKey(UITable.UserCacheKey.COLUMNWIDTH), widths);
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
            final StringBuilder ret = new StringBuilder();
            ret.append("function(_columnOrder){\n    ")
                .append(generateCallbackScript("wicketAjaxPost('" + getCallbackUrl(false) + "','"
                            + HeaderPanel.AjaxStoreColumnOrderBehavior.COLUMNORDER_PARAMETERNAME + "=' + _columnOrder"))
                .append("\n" + "  }\n");
            return ret.toString();
        }

        /**
         * Method stores the width string in the context of the current user.
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void respond(final AjaxRequestTarget _target)
        {
            final String order = getComponent().getRequest().getParameter(
                            HeaderPanel.AjaxStoreColumnOrderBehavior.COLUMNORDER_PARAMETERNAME);
            ((UITable) getComponent().getDefaultModelObject()).setColumnOrder(order);
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
            ret.append("  function(){\n    ")
                .append(getCallbackScript()).append("\n  }\n");
            return ret.toString();
        }

        /**
         * Reload the table.
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void respond(final AjaxRequestTarget _target)
        {
            final TableModel model = (TableModel) getComponent().getDefaultModel();
            model.getObject().resetModel();
            try {
                if (getComponent().getPage() instanceof TablePage) {
                    getComponent().setResponsePage(new TablePage(model));
                } else {
                    final UIForm uiform = (UIForm) getComponent().getPage().getDefaultModelObject();
                    getComponent().setResponsePage(new FormPage(new FormModel(uiform)));
                }
            } catch (final EFapsException e) {
                getComponent().setResponsePage(new ErrorPage(e));
            }
        }
    }
}
