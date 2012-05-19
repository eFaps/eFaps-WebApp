/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.ui.wicket.components.table.row;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.table.AjaxAddRemoveRowPanel;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.cell.CellPanel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.cell.UIHiddenCell;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class RowPanel
    extends Panel
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId         wicket id for this component
     * @param _model            model for this component
     * @param _tablePanel       tablepanel this row is in
     * @param _updateListMenu   must the listmenu be updated
     * @param _idx              index of the current row
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
            final CellPanel cellpanel = new CellPanel(cellRepeater.newChildId(), uirow.getInstanceKey());
            cellpanel.setOutputMarkupId(true);
            cellpanel.add(AttributeModifier.append("class", "eFapsTableCheckBoxCell"));
            cellRepeater.add(cellpanel);
            i++;
            firstCell = true;
        }
        if (uiTable.isEditable()) {
            final AjaxAddRemoveRowPanel remove = new AjaxAddRemoveRowPanel(cellRepeater.newChildId(),
                                                                           new TableModel(uiTable), this);
            remove.setOutputMarkupId(true);
            remove.add(AttributeModifier.append("class", "eFapsTableRemoveRowCell"));
            cellRepeater.add(remove);
            i++;
            firstCell = true;
        }

        final Map<String, Component> name2comp = new HashMap<String, Component>();
        for (final UITableCell uiCell : uirow.getValues()) {
            final Panel cell;
            if (uiTable.isEditable() && uiCell.getDisplay().equals(Display.EDITABLE)
                            && (uiCell.getUiClass() instanceof DateUI || uiCell.getUiClass() instanceof DateTimeUI)) {
                cell = new DateTimePanel("label", uiCell.getCompareValue(), uiCell.getName(),
                                uiCell.getUiClass() instanceof DateTimeUI,
                                uiCell.getField().getCols());
            } else {
                cell = new CellPanel(cellRepeater.newChildId(), new UIModel<UITableCell>(uiCell),
                                                      _updateListMenu, uiTable, _idx);
            }
            cell.setOutputMarkupId(true);
            if (uiCell.isFixedWidth()) {
                if (firstCell) {
                    firstCell = false;
                    cell.add(AttributeModifier.append("class",  "eFapsTableFirstCell eFapsTableCell"
                                    + " eFapsCellFixedWidth" + i));
                } else {
                    cell.add(AttributeModifier.append("class",  "eFapsTableCell eFapsCellFixedWidth" + i));
                }
            } else {
                if (firstCell) {
                    firstCell = false;
                    cell.add(AttributeModifier.append("class",  "eFapsTableFirstCell eFapsTableCell"
                                    + " eFapsCellWidth" + i));
                } else {
                    cell.add(AttributeModifier.append("class",  "eFapsTableCell eFapsCellWidth" + i));
                }
            }
            cellRepeater.add(cell);
            i++;
            name2comp.put(uiCell.getName(), cell);
        }

        final RepeatingView hiddenRepeater = new RepeatingView("hiddenRepeater");
        this.add(hiddenRepeater);
        for (final UIHiddenCell cell : uirow.getHidden()) {
            hiddenRepeater.add(new LabelComponent(hiddenRepeater.newChildId(), cell.getCellValue()));
        }

        final WebComponent rowId = new WebComponent("rowId") {

            /**
             * Needed for serialization.
             */
            private static final long serialVersionUID = 1L;

            /**
             * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
             */
            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                final AbstractUIPageObject uiObject = (AbstractUIPageObject) getPage().getDefaultModelObject();
                uirow.setUserinterfaceId(uiObject.getNewRandom());

                try {
                    uiObject.getUiID2Oid().put(uirow.getUserinterfaceId(), uirow.getInstance() == null
                                                                            ? null : uirow.getInstance().getOid());
                } catch (final EFapsException e) {
                    throw new RestartResponseException(new ErrorPage(e));
                }
                _tag.put("name", EFapsKey.TABLEROW_NAME.getKey());
                _tag.put("value", uirow.getUserinterfaceId());
                _tag.put("type" , "hidden");
            }
        };
        this.add(rowId);
    }
}
