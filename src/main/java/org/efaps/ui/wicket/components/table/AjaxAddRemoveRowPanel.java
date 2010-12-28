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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.table;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.table.row.RowPanel;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIFieldTable;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * Panel renders the add, insert and remove buttons for tables on insert.
 *
 * @author The eFaps Team
 * @version $Id$
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
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Script needed for the ajax call.
     */
    private CharSequence script;

    /**
     * Do be able to have more than one table in a form that can add new rows,
     * it is necessary to have unique function names.
     */
    private String functionName;

    /**
     * Constructor for ajax add link. Called from the table.
     *
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _rowsRepeater repeater
     */
    public AjaxAddRemoveRowPanel(final String _wicketId,
                                 final IModel<UITable> _model,
                                 final RepeatingView _rowsRepeater)
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        final AjaxAddRow link = new AjaxAddRow("addLink", _model, _rowsRepeater);
        this.add(link);
        final StaticImageComponent image = new StaticImageComponent("addIcon");
        image.setReference(AjaxAddRemoveRowPanel.ICON_ADD);
        link.add(image);

        add(new WebMarkupContainer("delLink").setVisible(false));

        add(new WebComponent("script") {

            /**
             * Needed for serialization.
             */
            private static final long serialVersionUID = 1L;

            /**
             * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
             *      org.apache.wicket.markup.ComponentTag)
             * @param _markupstream
             * @param _tag
             */
            @Override
            protected void onComponentTagBody(final MarkupStream _markupstream,
                                              final ComponentTag _tag)
            {
                super.onComponentTagBody(_markupstream, _tag);
                final StringBuilder js = new StringBuilder();
                js.append("<script type=\"text/javascript\">")
                    .append("function ").append(AjaxAddRemoveRowPanel.this.functionName)
                    .append("(_count, _successHandler, _rowId) {")
                    .append(AjaxAddRemoveRowPanel.this.script)
                    .append("}</script>");
                replaceComponentTagBody(_markupstream, _tag, js);
            }
        });
    }

    /**
     * Constructor called from the rowpanel for each row.
     *
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _rowPanel rowpanel that must be removed
     */
    public AjaxAddRemoveRowPanel(final String _wicketId,
                                 final IModel<UITable> _model,
                                 final RowPanel _rowPanel)
    {
        super(_wicketId, _model);

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

        add(new WebComponent("script").setVisible(false));
    }

    /**
     * Class renders an ajax link that adds a row to the table.
     */
    public class AjaxAddRow
        extends WebMarkupContainer
    {

        /**
         *Needed for serialization.
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
            add(new AjaxEventBehavior("onclick") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent(final AjaxRequestTarget _target)
                {
                    final String newRows = getComponent().getRequest().getParameter("eFapsNewRows");
                    final String rowId = getComponent().getRequest().getParameter("eFapsRowId");


                    final int count = Integer.parseInt(newRows);
                    final UITable uitable = (UITable) getDefaultModelObject();
                    final UIRow uirow = uitable.getEmptyRow() != null
                                                    ? uitable.getEmptyRow() : uitable.getValues().get(0);
                    final TablePanel tablepanel = getComponent().findParent(TablePanel.class);

                    for (int i = 0; i < count; i++) {
                        // create the new repeater item and add it to the
                        // repeater
                        final RowPanel row = new RowPanel(AjaxAddRemoveRowPanel.AjaxAddRow.this.rowsRep.newChildId(),
                                         new UIModel<UIRow>(uirow), tablepanel, false);
                        row.add(new SimpleAttributeModifier("class", "eFapsTableRowOdd"));
                        row.setOutputMarkupId(true);
                        AjaxAddRemoveRowPanel.AjaxAddRow.this.rowsRep.add(row);
                        // first execute javascript which creates a placeholder
                        // tag in markup for this item
                        final StringBuilder js = new StringBuilder();
                        js.append("var item=document.createElement('").append("tr").append("');")
                            .append("item.id='").append(row.getMarkupId()).append("';")
                            .append("Wicket.$('").append(tablepanel.getMarkupId())
                            .append("').insertBefore(item, Wicket.$('")
                            .append(rowId != null && rowId.length() > 0 && !rowId.equalsIgnoreCase("null")
                                            ? rowId
                                            : AjaxAddRemoveRowPanel.this.getMarkupId()).append("'));");
                        _target.prependJavascript(js.toString());
                        // notice how we set the newly created item tag's id to
                        // that of the newly created
                        // Wicket component, this is what will link this markup
                        // tag to Wicket component
                        // during Ajax repaint

                        // all thats left is to repaint the new item via Ajax
                        _target.addComponent(row);
                    }
                }

                /**
                 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getCallbackScript()
                 * @return
                 */
                @Override
                protected CharSequence getCallbackScript()
                {
                    final String name;
                    if (getComponent().getDefaultModelObject() instanceof UIFieldTable) {
                        name = ((UIFieldTable) getComponent().getDefaultModelObject()).getName();
                    } else {
                        name = ((UITable) getComponent().getDefaultModelObject()).getTable().getName();
                    }
                    AjaxAddRemoveRowPanel.this.functionName = "addNewRows_" + name;
                    AjaxAddRemoveRowPanel.this.script = "var w = wicketAjaxGet('" + getCallbackUrl(false)
                                    + "&eFapsNewRows=' + _count + '&eFapsRowId=' + _rowId,_successHandler,null,null)";
                    return AjaxAddRemoveRowPanel.this.functionName + "(1, null, null)";
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
            js.append("var e = document.getElementById('").append(this.rowPanel.getMarkupId()).append("');")
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
         * @param _model    model for this component
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
            final String name;
            if (getDefaultModelObject() instanceof UIFieldTable) {
                name = ((UIFieldTable) getDefaultModelObject()).getName();
            } else {
                name = ((UITable) getDefaultModelObject()).getTable().getName();
            }
            final StringBuilder js = new StringBuilder();
            js.append("addNewRows_").append(name).append("(1, null, '")
                .append(this.rowPanel.getMarkupId()).append("');");
            _tag.put("onclick", js);
        }
    }
}
