/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.ui.wicket.pages.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.PageReference;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.util.EFapsException;
import org.jbpm.task.query.TaskSummary;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DashboardPage
    extends AbstractMergePage
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(DashboardPage.class, "DashboardPage.css");

    public DashboardPage(final PageReference _pageReference)
        throws EFapsException
    {
        super();
        final SystemConfiguration config = EFapsSystemConfiguration.KERNEL.get();
        final boolean active = config != null
                        ? config.getAttributeValueAsBoolean(KernelSettings.ACTIVATE_BPM) : false;
        if (active) {
            final List<IColumn<TaskSummary, String>> columns = new ArrayList<IColumn<TaskSummary, String>>();

            columns.add(new AbstractColumn<TaskSummary, String>(new Model<String>(""))
            {

                private static final long serialVersionUID = 1L;

                @Override
                public void populateItem(final Item<ICellPopulator<TaskSummary>> _cellItem,
                                         final String _componentId,
                                         final IModel<TaskSummary> _rowModel)
                {
                    _cellItem.add(new ActionPanel(_componentId, _rowModel, _pageReference));
                }

                @Override
                public String getCssClass()
                {
                    return "openTask";
                }
            });
            // Administration
            if (Configuration.getAttributeAsBoolean(Configuration.ConfigAttribute.SHOW_OID)
                            && Context.getThreadContext()
                                            .getPerson()
                                            .isAssigned(Role.get(UUID
                                                            .fromString("1d89358d-165a-4689-8c78-fc625d37aacd")))) {

                columns.add(new PropertyColumn<TaskSummary, String>(new Model<String>("ID"), "id"));
                columns.add(new PropertyColumn<TaskSummary, String>(new Model<String>("Name"), "name", "name"));
            }
            columns.add(new PropertyColumn<TaskSummary, String>(new Model<String>("Description"), "description",
                            "description"));
            columns.add(new PropertyColumn<TaskSummary, String>(new Model<String>("Status"), "status"));
            columns.add(new PropertyColumn<TaskSummary, String>(new Model<String>("Activation Time"), "activationTime"));

            add(new AjaxFallbackDefaultDataTable<TaskSummary, String>("table", columns,
                            new SortableTaskSummaryDataProvider(), 8));
        } else {
            add(new WebMarkupContainer("table").setVisible(false));
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(DashboardPage.CSS));
    }
}
