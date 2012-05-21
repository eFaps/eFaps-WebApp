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
import org.efaps.ui.wicket.behaviors.dojo.MenuBarBehavior;
import org.efaps.ui.wicket.behaviors.dojo.MenuBarItemBehavior;
import org.efaps.ui.wicket.components.menu.ajax.SetCompanyItem;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class MenuBarPanel
    extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Content reference to the an image.
     */
    public static final EFapsContentReference IMG_BLANK = new EFapsContentReference(MenuContainer.class, "blank.gif");

    /**
     * @param _wicketId wicketId of this Panel
     * @param _model model for this Panel
     * @throws EFapsException
     */
    public MenuBarPanel(final String _wicketId,
                        final IModel<?> _model)
    {
        super(_wicketId, _model);
        add(new MenuBarBehavior());
        if (_model == null) {
            add(new WebMarkupContainer("itemRepeater"));
        } else {
            final UIMenuItem menuItem = (UIMenuItem) super.getDefaultModelObject();

            final RepeatingView itemRepeater = new RepeatingView("itemRepeater");
            add(itemRepeater);

            for (final UIMenuItem childItem : menuItem.getChilds()) {
                if (childItem.hasChilds()) {
                    itemRepeater.add(new PopUpMenuPanel(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem)));
                } else {
                    if (childItem.getReference() != null) {
                        childItem.setURL(childItem.getReference());
                        Component item = null;
                        if (childItem.getReference().equals(
                                        "/" + getSession().getApplication().getApplicationKey() + "/logout?")) {
                            item = new LogOutItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                        } else if (menuItem.getReference().equals(
                                        "/" + getSession().getApplication().getApplicationKey() + "/setcompany?")) {
                            item = new SetCompanyItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                        }
                        if (item != null) {
                            item.add(new MenuBarItemBehavior());
                            itemRepeater.add(item);
                        }
                    }
                }
            }
        }
    }
}
