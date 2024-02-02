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
package org.efaps.ui.wicket.components.gridx.behaviors;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.ui.Menu;
import org.efaps.ui.wicket.models.objects.grid.GridCell;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;

/**
 * The Class OpenInOpenerBehavior.
 *
 * @author The eFaps Team
 */
public class OpenInOpenerBehavior
    extends AjaxEventBehavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public OpenInOpenerBehavior()
    {
        super("click");
    }

    @Override
    protected void onEvent(final AjaxRequestTarget _target)
    {
        final StringValue rowId = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("rowId");
        final StringValue colId = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("colId");

        final UIGrid uiGrid = (UIGrid) getComponent().getPage().getDefaultModelObject();
        try {
            final List<GridCell> row = uiGrid.getValues().get(rowId.toInt());
            final GridCell cell = row.get(colId.toInt());

            if (cell.getInstance() != null) {
                Menu menu = null;
                try {
                    menu = Menu.getTypeTreeMenu(cell.getInstance().getType());
                } catch (final EFapsException e) {
                    throw new RestartResponseException(new ErrorPage(e));
                }
                if (menu == null) {
                    final Exception ex = new Exception("no tree menu defined for type "
                                    + cell.getInstance() == null ? "??"
                                                    : cell.getInstance().getType().getName());
                    throw new RestartResponseException(new ErrorPage(ex));
                }

                final Page page = new ContentContainerPage(menu.getUUID(), cell.getInstance().getKey(), false);
                final CharSequence url = RequestCycle.get().urlFor(new RenderPageRequestHandler(new PageProvider(page)));
                final StringBuilder js = new StringBuilder()
                                .append("opener.top.dijit.registry.byId(\"").append("mainPanel")
                                .append("\").set(\"content\", dojo.create(\"iframe\",{")
                                .append("\"id\": \"").append(MainPage.IFRAME_ID)
                                .append("\",\"src\": \"./wicket/").append(url)
                                .append("\",\"style\": \"border: 0; width: 100%; height: 99%\"")
                                .append("}));");
                _target.appendJavaScript(js);
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }
}
