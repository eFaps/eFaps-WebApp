/*
 * Copyright 2003 - 2009 The eFaps Team
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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.form.set.YPanel;
import org.efaps.ui.wicket.components.table.row.RowPanel;
import org.efaps.ui.wicket.models.RowModel;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxAddRowPanel extends Panel
{
    /**
     * Content reference for the delete icon.
     */
    private static final EFapsContentReference ICON_ADD = new EFapsContentReference(YPanel.class, "add.png");

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId
     * @param _model
     * @param rowsRepeater
     */
    public AjaxAddRowPanel(final String _wicketId, final IModel<UITable> _model, final RepeatingView _rowsRepeater)
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        final AjaxAddRow add = new AjaxAddRow("add", _model, _rowsRepeater);
        this.add(add);
        final StaticImageComponent image = new StaticImageComponent("addIcon");
        image.setReference(AjaxAddRowPanel.ICON_ADD);
        add.add(image);

    }

    public class AjaxAddRow extends AjaxLink<UITable>
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private final RepeatingView rowsRepeater;

        /**
         * @param _wicket
         * @param _rowsRepeater
         * @param model
         */
        public AjaxAddRow(final String _wicket, final IModel<UITable> _model, final RepeatingView _rowsRepeater)
        {
            super(_wicket, _model);
            this.rowsRepeater = _rowsRepeater;
        }

        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final UITable uitable = (UITable) getDefaultModelObject();
            final UIRow uirow = uitable.getValues().get(0);
            final TablePanel tablepanel = this.findParent(TablePanel.class);
            // create the new repeater item and add it to the repeater
            final RowPanel row = new RowPanel(this.rowsRepeater.newChildId(), new RowModel(uirow), tablepanel, false);
            row.add(new SimpleAttributeModifier("class", "eFapsTableRowOdd"));
            row.setOutputMarkupId(true);
            this.rowsRepeater.add(row);
            // first execute javascript which creates a placeholder tag in
            // markup for this item
            _target.prependJavascript(String.format("var item=document.createElement('%s');item.id='%s';"
                        + "Wicket.$('%s').insertBefore(item, Wicket.$('" + AjaxAddRowPanel.this.getMarkupId() + "'));",
                         "tr", row.getMarkupId(), tablepanel.getMarkupId()));

            // notice how we set the newly created item tag's id to that of the newly created
            // Wicket component, this is what will link this markup tag to Wicket component
            // during Ajax repaint

            // all thats left is to repaint the new item via Ajax
            _target.addComponent(row);
        }
    }
}
