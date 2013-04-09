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


package org.efaps.ui.wicket.pages.user;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.bpm.Bpm;
import org.jbpm.task.query.TaskSummary;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SortableTaskSummaryDataProvider extends SortableDataProvider<TaskSummary, String>
{

    private final List<TaskSummary> summaries;

    /**
     * constructor
     */
    public SortableTaskSummaryDataProvider()
    {
        // set default sort
        setSort("firstName", SortOrder.ASCENDING);
        this.summaries = Bpm.getTasksAssignedAsPotentialOwner();
    }

    @Override
    public Iterator<TaskSummary> iterator(final long first,
                                          final long count)
    {
        return this.summaries.subList(Long.valueOf(first).intValue(), Long.valueOf(first + count).intValue())
                        .iterator();
    }

    /**
     * @see org.apache.wicket.markup.repeater.data.IDataProvider#size()
     */
    @Override
    public long size()
    {
        return this.summaries.size();
    }

    /**
     * @see org.apache.wicket.markup.repeater.data.IDataProvider#model(java.lang.Object)
     */
    @Override
    public IModel<TaskSummary> model(final TaskSummary object)
    {
        return Model.of(object);
    }
}
