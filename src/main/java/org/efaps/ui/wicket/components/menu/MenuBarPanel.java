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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.behaviors.dojo.MenuBarBehavior;
import org.efaps.ui.wicket.behaviors.dojo.MenuBarItemBehavior;
import org.efaps.ui.wicket.components.menu.ajax.ExecItem;
import org.efaps.ui.wicket.components.menu.ajax.OpenModalItem;
import org.efaps.ui.wicket.components.menu.ajax.SearchItem;
import org.efaps.ui.wicket.components.menu.ajax.SetCompanyItem;
import org.efaps.ui.wicket.components.menu.ajax.SubmitItem;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UISearchItem;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;

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
     * Reference to the style sheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(MenuBarPanel.class, "MenuBarPanel.css");


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
        add(AttributeModifier.append("class", "eFapsMenuBarPanel"));
        if (_model == null) {
            add(new WebMarkupContainer("itemRepeater"));
        } else {
            final UIMenuItem menuItem = (UIMenuItem) super.getDefaultModelObject();

            final RepeatingView itemRepeater = new RepeatingView("itemRepeater");
            add(itemRepeater);

            for (final UIMenuItem childItem : menuItem.getChildren()) {
                if (childItem.hasChildren()) {
                    itemRepeater.add(new PopupMenuPanel(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem)));
                } else {
                    if (childItem.getReference() != null) {
                        childItem.setURL(childItem.getReference());
                        Component item = null;
                        if (childItem.getReference().equals(
                                        "/" + getSession().getApplication().getApplicationKey() + "/logout?")) {
                            item = new LogOutItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                        } else if (childItem.getReference().equals(
                                        "/" + getSession().getApplication().getApplicationKey() + "/setcompany?")) {
                            item = new SetCompanyItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                        }
                        if (item != null) {
                            item.add(new MenuBarItemBehavior());
                            itemRepeater.add(item);
                        }
                    } else {
                        Component item;
                        if (childItem.getTarget() != Target.UNKNOWN) {
                            if (childItem.getTarget() == Target.MODAL) {
                                item = new OpenModalItem(itemRepeater.newChildId(),
                                                new UIModel<UIMenuItem>(childItem));
                            } else if (childItem.getTarget() == Target.POPUP) {
                                item = new PopupItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                            } else if (childItem.getTarget() == Target.HIDDEN) {
                                item = new ExecItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                            } else {
                                item = new LinkItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                            }
                        } else {
                            if (childItem instanceof UISearchItem) {
                                item = new SearchItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                            } else if (childItem.getCommand().getTargetForm() != null
                                            || childItem.getCommand().getTargetTable() != null) {
                                item = new LinkItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                            } else if (childItem.getCommand().isSubmit()) {
                                item = new SubmitItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                            } else {
                                item = new ExecItem(itemRepeater.newChildId(), new UIModel<UIMenuItem>(childItem));
                            }
                        }
                        item.add(new MenuBarItemBehavior());
                        itemRepeater.add(item);
                    }
                }
            }
        }
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(MenuBarPanel.CSS));
    }
}
