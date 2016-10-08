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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.table.row.RowPanel;
import org.efaps.ui.wicket.models.objects.AbstractUIHeaderObject;
import org.efaps.ui.wicket.models.objects.UIFieldTable;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * Panel renders the add, insert and remove buttons for tables on insert.
 *
 * @author The eFaps Team
 */
public class AjaxAddRemoveRowPanel
    extends Panel
{

    /**
     * Content reference for the delete icon.
     */
    private static final EFapsContentReference ICON_ADD = new EFapsContentReference(AjaxAddRemoveRowPanel.class,
                    "add.png");
    /**
     * Content reference for the delete icon.
     */
    private static final EFapsContentReference ICON_DELETE = new EFapsContentReference(AjaxAddRemoveRowPanel.class,
                    "delete.png");

    /**
     * Prefix for the function name.
     */
    private static final String FUNCTION_PREFIX = "addNewRows_";

    /**
     * Name of a parameter.
     */
    private static final String FUNCTION_ROWCOUNT = "eFapsNewRowsCount";

    /**
     * Name of a parameter.
     */
    private static final String FUNCTION_ROWDID = "eFapsRowId";

    /**
     * Name of a parameter.
     */
    private static final String FUNCTION_EXPARA = "eFapsExtraParameter";

    /**
     * Name of a parameter.
     */
    private static final String FUNCTION_SUCCESSHANDLER = "eFapsSuccessHandler";

    /**
     * Suffix for the variable to hold the sucess handler.
     */
    private static final String VAR_SUFFIX = "_successHandler";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Do be able to have more than one table in a form that can add new rows,
     * it is necessary to have unique function names.
     */
    private final String tableName;

    /**
     * Constructor for only a ajax add link which is placed below the table.
     *
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _rowsRepeater repeater
     * @throws CacheReloadException on error
     */
    public AjaxAddRemoveRowPanel(final String _wicketId,
                                 final IModel<UITable> _model,
                                 final RepeatingView _rowsRepeater)
        throws CacheReloadException
    {
        super(_wicketId, _model);
        if (_model.getObject() instanceof UIFieldTable) {
            this.tableName = ((UIFieldTable) _model.getObject()).getName();
        } else {
            this.tableName = _model.getObject().getTable().getName();
        }
        setOutputMarkupId(true);
        final AjaxAddRow link = new AjaxAddRow("addLink", _model, _rowsRepeater);
        this.add(link);
        final StaticImageComponent image = new StaticImageComponent("addIcon");
        image.setReference(AjaxAddRemoveRowPanel.ICON_ADD);
        link.add(image);
        add(new WebMarkupContainer("delLink").setVisible(false));
    }

    /**
     * Constructor for a insert and remove link which is placed in the table for
     * each row.
     *
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _rowPanel rowpanel that must be removed
     * @throws CacheReloadException on error
     */
    public AjaxAddRemoveRowPanel(final String _wicketId,
                                 final IModel<UITable> _model,
                                 final RowPanel _rowPanel)
        throws CacheReloadException
    {
        super(_wicketId, _model);
        if (_model.getObject() instanceof UIFieldTable) {
            this.tableName = ((UIFieldTable) _model.getObject()).getName();
        } else {
            this.tableName = _model.getObject().getTable().getName();
        }
        final InsertRow insertlink = new InsertRow("addLink", _model, _rowPanel);
        this.add(insertlink);
        final StaticImageComponent insertImage = new StaticImageComponent("addIcon");
        insertImage.setReference(AjaxAddRemoveRowPanel.ICON_ADD);
        insertlink.add(insertImage);

        final RemoveRow delLink = new RemoveRow("delLink", _rowPanel);
        this.add(delLink);
        final StaticImageComponent delImage = new StaticImageComponent("delIcon");
        delImage.setReference(AjaxAddRemoveRowPanel.ICON_DELETE);
        delLink.add(delImage);
    }

    /**
     * Class renders an ajax link that adds a row to the table.
     */
    public class AjaxAddRow
        extends WebMarkupContainer
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * RepeatingView.
         */
        private final RepeatingView rowsRep;

        /**
         * @param _wicketId wicket id for this component
         * @param _model model for this component
         * @param _rowsRepeater row repeater
         *
         */
        public AjaxAddRow(final String _wicketId,
                          final IModel<UITable> _model,
                          final RepeatingView _rowsRepeater)
        {
            super(_wicketId, _model);
            this.rowsRep = _rowsRepeater;
            add(new AbstractDefaultAjaxBehavior()
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void respond(final AjaxRequestTarget _target)
                {
                    final StringValue newRows = getComponent().getRequest().getRequestParameters()
                                    .getParameterValue(AjaxAddRemoveRowPanel.FUNCTION_ROWCOUNT);
                    final StringValue rowId = getComponent().getRequest().getRequestParameters()
                                    .getParameterValue(AjaxAddRemoveRowPanel.FUNCTION_ROWDID);
                    final StringValue sucesshandler = getComponent().getRequest().getRequestParameters()
                                    .getParameterValue(AjaxAddRemoveRowPanel.FUNCTION_SUCCESSHANDLER);
                    final int count = Integer.parseInt(newRows.toString());
                    final UITable uitable = (UITable) getDefaultModelObject();
                    try {
                        final UIRow uirow = uitable.getEmptyRow() != null
                                        ? uitable.getEmptyRow() : uitable.getValues().get(0);
                        final TablePanel tablepanel = getComponent().findParent(TablePanel.class);

                        for (int i = 0; i < count; i++) {
                            // create the new repeater item and add it to the repeater
                            final RowPanel row = new RowPanel(
                                            AjaxAddRemoveRowPanel.AjaxAddRow.this.rowsRep.newChildId(),
                                            Model.of(uirow), tablepanel, false, 0);
                            row.add(AttributeModifier.append("class", "eFapsTableRowOdd"));
                            row.setOutputMarkupId(true);
                            AjaxAddRemoveRowPanel.AjaxAddRow.this.rowsRep.add(row);
                            // first execute javascript which creates a
                            // placeholder tag in markup for this item
                            final StringBuilder js = new StringBuilder();
                            js.append("var item=document.createElement('").append("tr").append("');")
                                .append("item.id='").append(row.getMarkupId()).append("';")
                                .append("Wicket.$('").append(tablepanel.getMarkupId())
                                .append("').insertBefore(item, Wicket.$('")
                                .append(rowId != null && rowId.toString().length() > 0
                                                && !rowId.toString().equalsIgnoreCase("null")
                                                            ? rowId : AjaxAddRemoveRowPanel.this.getMarkupId())
                                            .append("'));");
                            _target.prependJavaScript(js.toString());
                            _target.add(row);

                        }
                    } catch (final EFapsException e) {
                        TablePanel.LOG.error("error in adding row", e);
                    }
                    if (!sucesshandler.isNull() && !sucesshandler.isEmpty()
                                    && "true".equalsIgnoreCase(sucesshandler.toString())) {
                        _target.appendJavaScript(AjaxAddRemoveRowPanel.this.tableName
                                        + AjaxAddRemoveRowPanel.VAR_SUFFIX + "();");
                    }
                    final int tableid = ((AbstractUIHeaderObject) _model.getObject()).getTableId();
                    _target.appendJavaScript("positionTableColumns(eFapsTable" + tableid + ")");
                }

                /**
                 * The script must be added to the head but not on domready like
                 * it is done out of the box.
                 *
                 * @param _component Component this behavior belongs to
                 * @param _response the reponse the script is written to
                 */
                @Override
                public void renderHead(final Component _component,
                                       final IHeaderResponse _response)
                {
                    if (_component.isEnabledInHierarchy()) {
                        final CharSequence js = getCallbackScript(_component);

                        final AjaxRequestTarget target = _component.getRequestCycle().find(AjaxRequestTarget.class);
                        if (target == null) {
                            _response.render(JavaScriptHeaderItem.forScript(js.toString(),
                                            AjaxAddRemoveRowPanel.this.tableName));
                        } else {
                            target.appendJavaScript(js);
                        }
                    }
                }

                @Override
                protected void onComponentTag(final ComponentTag _tag)
                {
                    super.onComponentTag(_tag);
                    _tag.put("onclick", AjaxAddRemoveRowPanel.FUNCTION_PREFIX
                                    + AjaxAddRemoveRowPanel.this.tableName + "(1, null, null)");
                }

                @Override
                public CharSequence getCallbackScript(final Component _component)
                {
                    final StringBuilder js = new StringBuilder()
                        .append("var ").append(AjaxAddRemoveRowPanel.this.tableName)
                            .append(AjaxAddRemoveRowPanel.VAR_SUFFIX).append(";\n")
                        .append("var ").append(AjaxAddRemoveRowPanel.FUNCTION_PREFIX)
                            .append(AjaxAddRemoveRowPanel.this.tableName).append("=function(_nrc,_sh,_rId, _ep) {\n")
                        .append("var call=").append(getCallbackFunction(
                                CallbackParameter.explicit(AjaxAddRemoveRowPanel.FUNCTION_ROWCOUNT),
                                CallbackParameter.explicit(AjaxAddRemoveRowPanel.FUNCTION_SUCCESSHANDLER),
                                CallbackParameter.explicit(AjaxAddRemoveRowPanel.FUNCTION_ROWDID),
                                CallbackParameter.explicit(AjaxAddRemoveRowPanel.FUNCTION_EXPARA)))
                        .append(AjaxAddRemoveRowPanel.this.tableName).append(AjaxAddRemoveRowPanel.VAR_SUFFIX)
                            .append("=function() {\n")
                            .append("require([\"dojo/topic\"], function(topic){\n")
                            .append("topic.publish(\"eFaps/addRowBeforeScript/")
                                .append(AjaxAddRemoveRowPanel.this.tableName).append("\");\n")
                            .append("});\n")
                            .append("if (jQuery.isFunction(_sh)) {\n")
                                .append("_sh();\n")
                            .append("}\n")
                            .append("require([\"dojo/topic\"], function(topic){\n")
                                .append("topic.publish(\"eFaps/addRow/")
                                    .append(AjaxAddRemoveRowPanel.this.tableName).append("\");\n")
                            .append("});\n")
                        .append("}\n")
                        .append("call(_nrc,true,_rId, _ep);\n")
                        .append("}");
                    return js;
                }
            });
        }
    }

    /**
     * Class renders a component containing a script to remove a row from a
     * table.
     */
    public class RemoveRow
        extends WebMarkupContainer
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Rowpanel that must be removed.
         */
        private final RowPanel rowPanel;

        /**
         * @param _wicketId wicket id for this component
         * @param _rowPanel Rowpanel that must be removed
         */
        public RemoveRow(final String _wicketId,
                         final RowPanel _rowPanel)
        {
            super(_wicketId);
            this.rowPanel = _rowPanel;
        }

        /**
         * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
         * @param _tag tag
         */
        @Override
        protected void onComponentTag(final ComponentTag _tag)
        {
            super.onComponentTag(_tag);
            final StringBuilder js = new StringBuilder();
            js.append("var e = Wicket.$('").append(this.rowPanel.getMarkupId()).append("');")
                    .append("var p = e.parentNode;")
                    .append("p.removeChild(e);");
            _tag.put("onclick", js);
        }
    }

    /**
     * Render an insert button.
     *
     */
    public class InsertRow
        extends WebMarkupContainer
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Rowpanel the new must be added before.
         */
        private final RowPanel rowPanel;

        /**
         * @param _wicketId wicket ID of this component
         * @param _model model for this component
         * @param _rowPanel rowpnale this component belongs to
         */
        public InsertRow(final String _wicketId,
                         final IModel<UITable> _model,
                         final RowPanel _rowPanel)
        {
            super(_wicketId, _model);
            this.rowPanel = _rowPanel;
        }

        /**
         * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
         * @param _tag tag
         */
        @Override
        protected void onComponentTag(final ComponentTag _tag)
        {
            super.onComponentTag(_tag);
            final StringBuilder js = new StringBuilder();
            js.append(AjaxAddRemoveRowPanel.FUNCTION_PREFIX).append(AjaxAddRemoveRowPanel.this.tableName)
                .append("(1, null, '").append(this.rowPanel.getMarkupId()).append("');");
            _tag.put("onclick", js);
        }
    }
}
