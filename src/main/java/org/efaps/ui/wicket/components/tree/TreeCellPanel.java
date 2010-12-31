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

package org.efaps.ui.wicket.components.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.PageMap;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.behaviors.ExpandTextareaBehavior;
import org.efaps.ui.wicket.behaviors.SetSelectedRowBehavior;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.autocomplete.AutoCompleteField;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.picker.AjaxPickerLink;
import org.efaps.ui.wicket.components.table.cell.AjaxLinkContainer;
import org.efaps.ui.wicket.components.table.cell.AjaxLoadInOpenerLink;
import org.efaps.ui.wicket.components.table.cell.CheckOutLink;
import org.efaps.ui.wicket.components.table.cell.ContentContainerLink;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.cell.UIStructurBrowserTableCell;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;

/**
 * Class is used to render a cell inside a table.
 *
 * @author The eFaps Team
 * @version $Id:CellPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class TreeCellPanel
    extends Panel
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicketID for this Component
     * @param _node     treeNode the cell belongs to
     * @param _index    index of the column
     */
    public TreeCellPanel(final String _wicketId,
                         final TreeNode _node,
                         final int _index)
    {
        super(_wicketId);
        final UIStructurBrowser uiStru = (UIStructurBrowser) ((DefaultMutableTreeNode) _node).getUserObject();
        final UIStructurBrowserTableCell uiCell = uiStru.getColumnValue(_index);
        final IModel<UIStructurBrowserTableCell> cellModel = new UIModel<UIStructurBrowserTableCell>(uiCell);
        // set the title of the cell
        add(new SimpleAttributeModifier("title", uiCell.getCellTitle()));
        add(new AttributeAppender("style", true, new Model<String>("text-align:" + uiCell.getAlign()), ";"));
        if (uiCell.isHide()) {
            add(new AttributeAppender("style", true, new Model<String>("display:none"), ";"));
        }
        final WebMarkupContainer link;
        if (uiCell.getReference() == null
                        || (uiStru.isSearchMode() && uiCell.getTarget() != Target.POPUP && uiStru.isSubmit())) {
            link = new WebMarkupContainer("link")
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(final ComponentTag _tag)
                {
                    _tag.setName("span");
                    super.onComponentTag(_tag);
                }
            };
        } else {
            if ((getRequest().getPage() instanceof ContentContainerPage
                            || getRequest().getPage().getPageMapName().equals(ContentContainerPage.IFRAME_PAGEMAP_NAME))
                            && uiCell.getTarget() != Target.POPUP) {
                link = new AjaxLinkContainer("link", cellModel);
            } else {
                if (uiCell.isCheckOut()) {
                    link = new CheckOutLink("link", cellModel);
                } else {
                    if (uiStru.isSearchMode() && uiCell.getTarget() != Target.POPUP) {
                        link = new AjaxLoadInOpenerLink<UIStructurBrowserTableCell>("link", cellModel);
                    } else {
                        link = new ContentContainerLink<UIStructurBrowserTableCell>("link", cellModel);
                        if (uiCell.getTarget() == Target.POPUP) {
                            final PopupSettings popup = new PopupSettings(PageMap.forName("popup"));
                            ((ContentContainerLink<?>) link).setPopupSettings(popup);
                        }
                    }
                }
            }
        }
        add(link);

        Component label;
        if (uiCell.isAutoComplete() && uiCell.getDisplay().equals(Display.EDITABLE)) {
            label = new AutoCompleteField("label", cellModel, false);
        } else {
            label = new LabelComponent("label", cellModel);
        }
        if (uiCell.getDisplay().equals(Display.EDITABLE)) {
            label.add(new SetSelectedRowBehavior(uiCell.getName()));
            if (uiCell.isFieldUpdate()) {
                label.add(new AjaxFieldUpdateBehavior(uiCell.getFieldUpdateEvent(), cellModel));
            }
            if (uiCell.isMultiRows()) {
                label.add(new ExpandTextareaBehavior());
            }
            link.add(new WebMarkupContainer("icon").setVisible(false));
        } else {
            if (uiCell.getIcon() == null) {
                link.add(new WebMarkupContainer("icon").setVisible(false));
            } else {
                link.add(new StaticImageComponent("icon", uiCell.getIcon()));
            }
        }
        link.add(label);
        if (uiCell.isValuePicker() && uiCell.getDisplay().equals(Display.EDITABLE)) {
            this.add(new AjaxPickerLink("valuePicker", cellModel, label));
        } else {
            add(new WebMarkupContainer("valuePicker").setVisible(false));
        }
    }
}
