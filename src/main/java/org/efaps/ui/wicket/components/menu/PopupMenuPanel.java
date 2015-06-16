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

package org.efaps.ui.wicket.components.menu;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.behaviors.dojo.PopupMenuBarItemBehavior;
import org.efaps.ui.wicket.behaviors.dojo.PopupMenuItemBehavior;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.util.IdGenerator;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PopupMenuPanel
    extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Panel inside a menubar.
     */
    private final boolean menuBarItem;

    /**
     * @param _wicketId wicketId of this Panel
     * @param _model model for this Panel
     * @param _idGenerator helper for ids
     * @throws CacheReloadException on error
     */
    public PopupMenuPanel(final String _wicketId,
                          final IModel<?> _model,
                          final IdGenerator _idGenerator)
        throws CacheReloadException
    {
        this(_wicketId, _model, _idGenerator, true);
    }

    /**
     * @param _wicketId wicketId of this Panel
     * @param _model model for this Panel
     * @param _idGenerator helper for ids
     * @param _isMenuBarItem part of the main menu bar or not
     * @throws CacheReloadException on error
     */
    public PopupMenuPanel(final String _wicketId,
                          final IModel<?> _model,
                          final IdGenerator _idGenerator,
                          final boolean _isMenuBarItem)
        throws CacheReloadException
    {
        super(_wicketId, _model);
        this.menuBarItem = _isMenuBarItem;
        final UIMenuItem menuItem = (UIMenuItem) super.getDefaultModelObject();

        if (_isMenuBarItem) {
            add(new PopupMenuBarItemBehavior());
        } else {
            add(new PopupMenuItemBehavior());
        }
        add(new WebComponent("label", Model.of(menuItem))
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTagBody(final MarkupStream _markupStream,
                                           final ComponentTag _openTag)
            {
                super.onComponentTagBody(_markupStream, _openTag);
                final UIMenuItem uiItem = (UIMenuItem) getDefaultModelObject();
                final StringBuilder html = new StringBuilder();
                if (PopupMenuPanel.this.menuBarItem && uiItem.getImage() != null) {
                    html.append("<img src=\"/..").append(uiItem.getImage()).append("\" class=\"eFapsMenuImage\"/>");
                } else if (!PopupMenuPanel.this.menuBarItem) {
                    if (uiItem.getImage() == null) {
                        html.append("<div class=\"eFapsMenuImagePlaceHolder\">").append("&nbsp;</div>");
                    } else {
                        html.append("<img src=\"/..").append(uiItem.getImage()).append("\" class=\"eFapsMenuImage\"/>");
                    }
                }
                html.append("<span class=\"eFapsMenuLabel\">").append(uiItem.getLabel()).append("</span>");
                replaceComponentTagBody(_markupStream, _openTag, html);
            }
        });
        add(new DropDownMenuPanel("menu", new UIModel<UIMenuItem>(menuItem), _idGenerator));
    }
}
