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


package org.efaps.ui.wicket.components.bpm.process;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageReference;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.bpm.task.AbstractTaskSummaryProvider;
import org.efaps.ui.wicket.components.bpm.task.ActionPanel;
import org.efaps.ui.wicket.models.objects.UITaskSummary;
import org.efaps.ui.wicket.pages.dashboard.DashboardPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ProcessTablePanel
    extends Panel
{

    /**
     * Reference to the style sheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(ProcessTablePanel.class,
                    "TaskTablePanel.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * DataProvier for this TaskTable.
     */
    private AbstractTaskSummaryProvider dataProvider;

    /**
     * @param _wicketId wicket for this component
     * @param _pageReference Reference to the calling page
     * @param _dataProvider provider for the task table
     * @throws EFapsException on error
     */
    public ProcessTablePanel(final String _wicketId,
                          final PageReference _pageReference,
                          final AbstractTaskSummaryProvider _dataProvider)
        throws EFapsException
    {
        super(_wicketId);
        this.dataProvider = _dataProvider;

        final List<IColumn<UITaskSummary, String>> columns = new ArrayList<IColumn<UITaskSummary, String>>();

        columns.add(new AbstractColumn<UITaskSummary, String>(new Model<String>(""))
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<UITaskSummary>> _cellItem,
                                     final String _componentId,
                                     final IModel<UITaskSummary> _rowModel)
            {
                _cellItem.add(new ActionPanel(_componentId, _rowModel, _pageReference));
            }

            @Override
            public String getCssClass()
            {
                return "openTask";
            }
        });

        if (this.dataProvider.showOid()) {
            columns.add(new PropertyColumn<UITaskSummary, String>(new Model<String>("ID"), "id", "id"));
            columns.add(new PropertyColumn<UITaskSummary, String>(new Model<String>("Name"), "name", "name"));
        }

        final String desc = DBProperties.getProperty(DashboardPage.class.getName() + ".TaskTable.Description");
        final String status = DBProperties.getProperty(DashboardPage.class.getName() + ".TaskTable.Status");
        final String at = DBProperties.getProperty(DashboardPage.class.getName() + ".TaskTable.ActivationTime");
        final String owner = DBProperties.getProperty(DashboardPage.class.getName() + ".TaskTable.Owner");

        columns.add(new PropertyColumn<UITaskSummary, String>(new Model<String>(desc), "description",
                        "description"));
        columns.add(new PropertyColumn<UITaskSummary, String>(new Model<String>(status), "status"));
        columns.add(new PropertyColumn<UITaskSummary, String>(new Model<String>(at), "activationTime",
                        "activationTime"));
        columns.add(new PropertyColumn<UITaskSummary, String>(new Model<String>(owner), "owner",
                        "owner"));

        add(new AjaxFallbackDefaultDataTable<UITaskSummary, String>("table", columns, this.dataProvider,
                        this.dataProvider.getRowsPerPage()));
    }


    /**
     * @return update the underlying data
     */
    public boolean updateData()
    {
        this.dataProvider.requery();
        return true;
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(ProcessTablePanel.CSS));
    }
}
