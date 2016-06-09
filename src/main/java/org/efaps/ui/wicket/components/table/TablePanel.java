/*
 * Copyright 2003 - 2014 The eFaps Team
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

import java.util.Iterator;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.behaviors.RowSelectedInput;
import org.efaps.ui.wicket.components.table.row.RowPanel;
import org.efaps.ui.wicket.models.objects.UIFieldTable;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class renders a table.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TablePanel
    extends Panel
{

    /**
     * Reference to the style sheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(TablePanel.class, "TablePanel.css");

    /**
     * Logger for this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(TablePanel.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id of this component
     * @param _uitable model for this component
     * @param _page page this component is in
     * @throws EFapsException on error
     */
    public TablePanel(final String _wicketId,
                      final IModel<UITable> _uitable,
                      final Page _page)
        throws EFapsException
    {
        super(_wicketId, _uitable);

        final UITable uiTable = (UITable) super.getDefaultModelObject();

        if (!uiTable.isInitialized()) {
            uiTable.execute();
        }
        setOutputMarkupId(true);
        this.add(AttributeModifier.append("class", "eFapsTableBody"));

        final RepeatingView rowsRepeater = new RepeatingView("rowRepeater");
        add(rowsRepeater);

        if (uiTable.getValues().isEmpty()) {
            String text;
            if (uiTable.isFiltered()) {
                text = DBProperties.getProperty("WebTable.NoDataWithFilter");
            } else {
                text = DBProperties.getProperty("WebTable.NoData");
            }
            final Label nodata = new Label(rowsRepeater.newChildId(), text);
            nodata.add(AttributeModifier.append("class", "eFapsTableNoData"));
            rowsRepeater.add(nodata);
        } else {
            boolean odd = true;
            int i = 0;
            for (final Iterator<UIRow> rowIter = uiTable.getValues().iterator(); rowIter.hasNext(); odd = !odd) {
                i++;
                final RowPanel row = new RowPanel(rowsRepeater.newChildId(), Model.of(rowIter.next()), this,
                                ((AbstractContentPage) _page).isUpdateMenu(), i);
                row.setOutputMarkupId(true);
                if (odd) {
                    row.add(AttributeModifier.append("class", "eFapsTableRowOdd"));
                } else {
                    row.add(AttributeModifier.append("class", "eFapsTableRowEven"));
                }
                rowsRepeater.add(row);
            }
        }
        if (uiTable.isEditable()) {
            rowsRepeater.add(new AjaxAddRemoveRowPanel(rowsRepeater.newChildId(), _uitable, rowsRepeater));
            if (uiTable instanceof UIFieldTable) {
                if (((UIFieldTable) uiTable).isFirstTable()) {
                    this.add(new RowSelectedInput("selected"));
                } else {
                    this.add(new WebComponent("selected").setVisible(false));
                }
            } else {
                this.add(new RowSelectedInput("selected"));
            }
        } else {
            this.add(new WebComponent("selected").setVisible(false));
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(TablePanel.CSS));
    }
}
