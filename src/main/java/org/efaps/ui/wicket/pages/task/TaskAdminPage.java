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

package org.efaps.ui.wicket.pages.task;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.efaps.ui.wicket.components.task.AdminTaskSummaryProvider;
import org.efaps.ui.wicket.components.task.AdminTaskSummaryProvider.Query;
import org.efaps.ui.wicket.components.task.TaskTablePanel;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TaskAdminPage
    extends AbstractMergePage
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TaskAdminPage.class);

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(TaskPage.class, "TaskPage.css");

    /**
     * @param _pageReference
     */
    public TaskAdminPage(final PageReference _pageReference)
        throws EFapsException
    {
        add(new UpdateTableLink("activeTasksBtn", Query.ACTIVE));
        add(new UpdateTableLink("completedTasksBtn", Query.COMPLETED));
        add(new UpdateTableLink("readyTasksBtn", Query.READY));
        add(new UpdateTableLink("reservedTasksBtn", Query.RESERVED));
        add(new UpdateTableLink("errorTasksBtn", Query.ERROR));
        final TaskTablePanel taskTable = new TaskTablePanel("taskTable", _pageReference,
                        new AdminTaskSummaryProvider());
        add(taskTable);
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(TaskAdminPage.CSS));
    }

    /*
     * (non-Javadoc)
     * @see org.apache.wicket.Page#onDetach()
     */
    @Override
    protected void onDetach()
    {
        super.onDetach();
    }

    public static class UpdateTableLink
        extends IndicatingAjaxLink<Void>
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private final Query query;

        /**
         * @param _id
         */
        public UpdateTableLink(final String _id,
                               final Query _query)
        {
            super(_id);
            this.query = _query;
        }

        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final AjaxFallbackDefaultDataTable<?, ?> table = (AjaxFallbackDefaultDataTable<?, ?>) getPage()
                            .visitChildren(AjaxFallbackDefaultDataTable.class).next();
            final AdminTaskSummaryProvider provider = (AdminTaskSummaryProvider) table.getDataProvider();
            provider.setQuery(this.query);
            provider.requery();
            _target.add(table);
        }

    }

}
