/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.ui.wicket.components.menu;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.behaviors.dojo.DropDownMenuBehavior;
import org.efaps.ui.wicket.behaviors.dojo.MenuItemBehavior;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DropDownMenuPanel
    extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicketId of this Panel
     * @param _model model for this Panel
     */
    public DropDownMenuPanel(final String _wicketId,
                             final IModel<?> _model)
    {
        super(_wicketId, _model);
        add(new DropDownMenuBehavior());
        final UIMenuItem menuItem = (UIMenuItem) super.getDefaultModelObject();

        final RepeatingView itemRepeater = new RepeatingView("itemRepeater");
        add(itemRepeater);

        for (final UIMenuItem childItem : menuItem.getChilds()) {
            if (childItem.hasChilds()) {
                itemRepeater.add(new PopUpMenuPanel(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem),
                                false));
            } else {
                final Component item;
                if (childItem.getTarget() != Target.UNKNOWN) {
                    if (childItem.getTarget() == Target.MODAL) {
                        item = new AjaxOpenModalComponent(itemRepeater.newChildId(),
                                        new UIModel<UIMenuItem>(childItem),
                                        childItem.getCommand().isSubmit() ? null : null);
                    } else {
                        item = new LinkItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                    }
                } else {
                    item = new WebMarkupContainer(itemRepeater.newChildId());
                }
                item.add(new MenuItemBehavior());
                itemRepeater.add(item);
            }
        }
    }
}
