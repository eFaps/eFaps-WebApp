/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.components.table.row;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.table.AjaxAddRemoveRowPanel;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.field.CheckBoxField;
import org.efaps.ui.wicket.components.table.field.FieldPanel;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.IFilterable;
import org.efaps.ui.wicket.models.field.IHidden;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 */
public class RowPanel
    extends Panel
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _tablePanel tablepanel this row is in
     * @param _updateListMenu must the listmenu be updated
     * @param _idx index of the current row
     * @throws EFapsException on error
     *
     */
    public RowPanel(final String _wicketId,
                    final IModel<UIRow> _model,
                    final TablePanel _tablePanel,
                    final boolean _updateListMenu,
                    final int _idx)
        throws EFapsException
    {
        super(_wicketId, _model);
        final UIRow uirow = (UIRow) super.getDefaultModelObject();

        final UITable uiTable = (UITable) _tablePanel.getDefaultModelObject();
        int i = uiTable.getTableId();

        final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
        add(cellRepeater);

        boolean firstCell = false;
        if (uiTable.isShowCheckBoxes()) {
            final CheckBoxField checkbox = new CheckBoxField(cellRepeater.newChildId(), uirow.getInstanceKey());
            checkbox.setOutputMarkupId(true);
            checkbox.add(AttributeModifier.append("class", "eFapsTableCheckBoxCell eFapsTableCellClear"));
            cellRepeater.add(checkbox);
            i++;
            firstCell = true;
        }
        if (uiTable.isEditable()) {
            final AjaxAddRemoveRowPanel remove = new AjaxAddRemoveRowPanel(cellRepeater.newChildId(),
                            Model.of(uiTable), this);
            remove.setOutputMarkupId(true);
            remove.add(AttributeModifier.append("class", "eFapsTableRemoveRowCell eFapsTableCellClear"));
            cellRepeater.add(remove);
            i++;
            firstCell = true;
        }

        for (final IFilterable filterable : uirow.getCells()) {
            Component cell = null;
            boolean fixedWidth = false;
            if (filterable instanceof AbstractUIField) {
                fixedWidth = ((AbstractUIField) filterable).getFieldConfiguration().isFixedWidth();
                cell = new FieldPanel(cellRepeater.newChildId(), Model.of((AbstractUIField) filterable));
            }
            cell.setOutputMarkupId(true);
            if (fixedWidth) {
                if (firstCell) {
                    firstCell = false;
                    cell.add(AttributeModifier.append("class", "eFapsTableFirstCell eFapsTableCell"
                                    + " eFapsCellFixedWidth" + i));
                } else {
                    cell.add(AttributeModifier.append("class", "eFapsTableCell eFapsCellFixedWidth" + i));
                }
            } else {
                if (firstCell) {
                    firstCell = false;
                    cell.add(AttributeModifier.append("class", "eFapsTableFirstCell eFapsTableCell"
                                    + " eFapsCellWidth" + i));
                } else {
                    cell.add(AttributeModifier.append("class", "eFapsTableCell eFapsCellWidth" + i));
                }
            }
            if (cellRepeater.size() < 1) {
                cell.add(AttributeModifier.append("class", "eFapsTableCellClear"));
            }
            cellRepeater.add(cell);
            i++;
        }

        final RepeatingView hiddenRepeater = new RepeatingView("hiddenRepeater");
        this.add(hiddenRepeater);
        for (final IHidden hidden : uirow.getHidden()) {
            if (!hidden.isAdded()) {
                hiddenRepeater.add(hidden.getComponent(hiddenRepeater.newChildId()));
                hidden.setAdded(true);
            }
        }
        this.add(new RowId("rowId", Model.of((AbstractInstanceObject) uirow)));
    }
}
