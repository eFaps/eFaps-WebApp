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

package org.efaps.ui.wicket.components.bpm;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.panel.Panel;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.bpm.AdminTaskSummaryProvider.Query;
import org.efaps.ui.wicket.pages.task.TaskAdminPage;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TaskAdminPanel
    extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _id
     */
    public TaskAdminPanel(final String _id,
                          final PageReference _pageReference)
        throws EFapsException
    {
        super(_id);
        add(new UpdateTableLink("activeTasksBtn", Query.ACTIVE));
        add(new UpdateTableLink("completedTasksBtn", Query.COMPLETED));
        add(new UpdateTableLink("readyTasksBtn", Query.READY));
        add(new UpdateTableLink("reservedTasksBtn", Query.RESERVED));
        add(new UpdateTableLink("errorTasksBtn", Query.ERROR));
        final TaskTablePanel taskTable = new TaskTablePanel("taskTable", _pageReference,
                        new AdminTaskSummaryProvider());
        add(taskTable);

    }

    /**
     * Link to update the table.
     */
    public static class UpdateTableLink
        extends AjaxLink<Void>
        implements IAjaxIndicatorAware
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * Query for this link.
         */
        private final Query query;

        /**
         * @param _wicketId wicket di
         * @param _query query
         */
        public UpdateTableLink(final String _wicketId,
                               final Query _query)
        {
            super(_wicketId);
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

        @Override
        public String getAjaxIndicatorMarkupId()
        {
            return "eFapsVeil";
        }

        @Override
        public void onComponentTagBody(final MarkupStream _markupStream,
                                       final ComponentTag _openTag)
        {
            final String label = DBProperties.getProperty(TaskAdminPage.class.getName() + "." + this.query);
            replaceComponentTagBody(_markupStream, _openTag, label);
        }
    }

}
