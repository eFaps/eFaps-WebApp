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
package org.efaps.ui.wicket.components.menu;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.behaviors.dojo.DropDownMenuBehavior;
import org.efaps.ui.wicket.behaviors.dojo.MenuItemBehavior;
import org.efaps.ui.wicket.components.menu.ajax.ExecItem;
import org.efaps.ui.wicket.components.menu.ajax.OpenModalItem;
import org.efaps.ui.wicket.components.menu.ajax.SearchItem;
import org.efaps.ui.wicket.components.menu.ajax.SubmitItem;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UISearchItem;
import org.efaps.ui.wicket.util.IdGenerator;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
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
     * @param _idGenerator helper for ids
     * @throws CacheReloadException on error
     */
    public DropDownMenuPanel(final String _wicketId,
                             final IModel<?> _model,
                             final IdGenerator _idGenerator)
        throws CacheReloadException
    {
        super(_wicketId, _model);
        add(new DropDownMenuBehavior());
        final UIMenuItem menuItem = (UIMenuItem) super.getDefaultModelObject();

        final RepeatingView itemRepeater = new RepeatingView("itemRepeater");
        add(itemRepeater);

        for (final UIMenuItem childItem : menuItem.getChildren()) {
            if (childItem.hasChildren()) {
                itemRepeater.add(new PopupMenuPanel(_idGenerator.newChildId(), Model.of(childItem),
                                _idGenerator, false));
            } else {
                Component item = null;
                if (childItem.getReference() != null) {
                    childItem.setURL(childItem.getReference());
                    if (childItem.getReference().endsWith("/taskadmin")) {
                        item = new TaskAdminItem(_idGenerator.newChildId(), Model.of(childItem));
                    } else if (childItem.getReference().endsWith("/connection")) {
                        item = new ConnectionItem(_idGenerator.newChildId(), Model.of(childItem));
                    }
                }

                if (item == null) {
                    if (childItem.getTarget() != Target.UNKNOWN) {
                        if (childItem.getTarget() == Target.MODAL) {
                            item = new OpenModalItem(_idGenerator.newChildId(), Model.of(childItem));
                        } else if (childItem.getTarget() == Target.POPUP) {
                            item = new PopupItem(_idGenerator.newChildId(), Model.of(childItem));
                        } else if (childItem.getTarget() == Target.HIDDEN) {
                            item = new ExecItem(_idGenerator.newChildId(), Model.of(childItem));
                        } else {
                            item = new LinkItem(_idGenerator.newChildId(), Model.of(childItem));
                        }
                    } else {
                        if (childItem instanceof UISearchItem) {
                            item = new SearchItem(_idGenerator.newChildId(), Model.of(childItem));
                        } else if (childItem.getCommand().getTargetForm() != null
                                        || childItem.getCommand().getTargetTable() != null) {
                            item = new LinkItem(_idGenerator.newChildId(), Model.of(childItem));
                        } else if (childItem.getCommand().isSubmit()) {
                            item = new SubmitItem(_idGenerator.newChildId(), Model.of(childItem));
                        } else {
                            item = new ExecItem(_idGenerator.newChildId(), Model.of(childItem));
                        }
                    }
                }
                if (item != null) {
                    item.add(new MenuItemBehavior());
                    itemRepeater.add(item);
                }
            }
        }
    }
}
