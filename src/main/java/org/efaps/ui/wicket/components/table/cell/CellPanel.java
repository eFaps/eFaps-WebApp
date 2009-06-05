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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.table.cell;

import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.PageMap;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.components.AutoCompleteField;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.UITable;

/**
 * Class is used to render a cell inside a table.
 *
 * @author The eFaps Team
 * @version $Id:CellPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class CellPanel extends Panel
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor used to get a cell which only contains a check box.
     *
     * @param _wicketId wicket id for this component
     * @param _oid oid for the ceck box
     */
    public CellPanel(final String _wicketId, final String _oid)
    {
        super(_wicketId);
        add(new CheckBoxComponent("checkbox", _oid));
        add(new WebMarkupContainer("link").setVisible(false));
        add(new WebMarkupContainer("icon").setVisible(false));
        add(new WebMarkupContainer("label").setVisible(false));
    }

    /**
     * Constructor for all cases minus checkbox.
     *
     * @see #CellPanel(String, String)
     * @param _wicketId         wicket id for this component
     * @param _model            model for this component
     * @param _updateListMenu   must the list be updated
     * @param _uitable          uitable
     * @param _rowNumber        number of this row, needed for create case
     * @param _columnNumber     number of this column, needed for create case
     */
    public CellPanel(final String _wicketId, final IModel<UITableCell> _model, final boolean _updateListMenu,
                     final UITable _uitable, final int _rowNumber, final int _columnNumber)
    {
        super(_wicketId, _model);
        final UITableCell cellmodel = (UITableCell) super.getDefaultModelObject();
        // set the title of the cell
        add(new SimpleAttributeModifier("title", cellmodel.getCellTitle()));
        if (cellmodel.isAutoComplete() && _uitable.isCreateMode()) {
            final NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumIntegerDigits(2);
            nf.setMaximumIntegerDigits(2);

            final StringBuilder name = new StringBuilder().append(cellmodel.getName()).append("-eFaps").append(
                            nf.format(_rowNumber)).append(nf.format(_columnNumber)).append("\" ");

            add(new AutoCompleteField("checkbox", _model, name.toString()));
            add(new WebMarkupContainer("link").setVisible(false));
            add(new WebMarkupContainer("icon").setVisible(false));
            add(new WebMarkupContainer("label").setVisible(false));
        } else {
            // make the checkbox invisible
            add(new WebMarkupContainer("checkbox").setVisible(false));

            WebMarkupContainer celllink;
            if (cellmodel.getReference() == null) {
                celllink = new WebMarkupContainer("link");
                celllink.setVisible(false);
            } else {
                if (_updateListMenu && cellmodel.getTarget() != Target.POPUP) {
                    celllink = new AjaxLinkContainer("link", _model);
                } else {
                    if (cellmodel.isCheckOut()) {
                        celllink = new CheckOutLink("link", _model);
                    } else {
                        if (_uitable.isSearchMode() && cellmodel.getTarget() != Target.POPUP) {
                            // do we have "connectmode",then we don't want a link in a popup
                            if (_uitable.isSubmit()) {
                                celllink = new WebMarkupContainer("link");
                                celllink.setVisible(false);
                            } else {
                                celllink = new AjaxLoadInOpenerLink("link", _model);
                            }
                        } else {
                            celllink = new ContentContainerLink<UITableCell>("link", _model);
                            if (cellmodel.getTarget() == Target.POPUP) {
                                final PopupSettings popup = new PopupSettings(PageMap.forName("popup"));
                                ((ContentContainerLink<?>) celllink).setPopupSettings(popup);
                            }
                        }
                    }
                }
            }
            add(celllink);
            final String cellvalue;

            if (_uitable.isCreateMode()) {
                final Pattern tagpattern = Pattern
                                .compile("</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>");
                final StringBuilder regex = new StringBuilder().append("(?i)name\\s*=\\s*\"(?-i)").append(
                                cellmodel.getName()).append("\"");
                final NumberFormat nf = NumberFormat.getInstance();
                nf.setMinimumIntegerDigits(2);
                nf.setMaximumIntegerDigits(2);

                final String value = cellmodel.getCellValue();
                final StringBuilder bld = new StringBuilder();
                final Matcher matcher = tagpattern.matcher(value);
                int start = 0;
                while (matcher.find()) {
                    value.substring(start, matcher.start());
                    bld.append(value.substring(start, matcher.start()));
                    final String tag = matcher.group();
                    final StringBuilder name = new StringBuilder().append(" name=\"").append(cellmodel.getName())
                                                    .append("-eFaps").append(nf.format(_rowNumber))
                                                    .append(nf.format(_columnNumber)).append("\" ");
                    bld.append(tag.replaceAll(regex.toString(), name.toString()));
                    start = matcher.end();
                }
                bld.append(value.substring(start, value.length()));
                cellvalue = bld.toString();
            } else {
                cellvalue = cellmodel.getCellValue();
            }

            if (celllink.isVisible()) {
                celllink.add(new LabelComponent("linklabel", cellvalue));

                if (cellmodel.getIcon() == null) {
                    celllink.add(new WebMarkupContainer("linkicon").setVisible(false));
                } else {
                    celllink.add(new StaticImageComponent("linkicon", cellmodel.getIcon()));
                }
                add(new WebMarkupContainer("icon").setVisible(false));
                add(new WebMarkupContainer("label").setVisible(false));
            } else {
                add(new LabelComponent("label", cellvalue));
                if (cellmodel.getIcon() == null) {
                    add(new WebMarkupContainer("icon").setVisible(false));
                } else {
                    add(new StaticImageComponent("icon", cellmodel.getIcon()));
                }
            }
        }
    }
}
