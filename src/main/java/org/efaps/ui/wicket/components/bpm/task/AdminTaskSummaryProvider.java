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

package org.efaps.ui.wicket.components.bpm.task;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.KernelSettings;
import org.efaps.admin.user.Role;
import org.efaps.bpm.BPM;
import org.efaps.bpm.task.TaskAdminstration;
import org.efaps.db.Context;
import org.efaps.ui.wicket.models.objects.UITaskSummary;
import org.efaps.util.EFapsException;
import org.kie.api.task.model.TaskSummary;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AdminTaskSummaryProvider
    extends AbstractTaskSummaryProvider
{

    /**
     * Query Defintions Enum.
     */
    public enum Query
    {
        /** Active tasks.*/
        ACTIVE,
        /** Completed tasks.*/
        COMPLETED,
        /** Error tasks.*/
        ERROR,
        /** Ready tasks.*/
        READY,
        /** Reserved tasks.*/
        RESERVED,
        /** Exited tasks.*/
        EXITED;
    }

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Current query for this provider.
     */
    private Query query;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<UITaskSummary> getUIValues()
    {
        if (this.query == null) {
            this.query = Query.ACTIVE;
        }
        final TaskAdminstration admin = BPM.getTaskAdmin();

        List<UITaskSummary> ret;
        switch (this.query) {
            case ACTIVE:
                ret = UITaskSummary.getUITaskSummary(admin.getActiveTasks());
                break;
            case COMPLETED:
                ret = UITaskSummary.getUITaskSummary(admin.getCompletedTasks());
                break;
            case READY:
                ret = UITaskSummary.getUITaskSummary(admin.getReadyTasks());
                break;
            case ERROR:
                ret = UITaskSummary.getUITaskSummary(admin.getErrorTasks());
                break;
            case RESERVED:
                ret = UITaskSummary.getUITaskSummary(admin.getReservedTasks());
                break;
            case EXITED:
                ret = UITaskSummary.getUITaskSummary(admin.getExitedTasks());
                break;
            default:
                ret = UITaskSummary.getUITaskSummary(new ArrayList<TaskSummary>());
                break;
        }
        //admin.dispose();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAttributeKey4SortProperty()
    {
        return AdminTaskSummaryProvider.class.getName() + "SortProperty";
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getUserAttributeKey4SortOrder()
    {
        return AdminTaskSummaryProvider.class.getName() + "SortOrder";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowsPerPage()
    {
        return 10;
    }

    /**
     * Getter method for the instance variable {@link #query}.
     *
     * @return value of instance variable {@link #query}
     */
    public Query getQuery()
    {
        return this.query;
    }

    /**
     * Setter method for instance variable {@link #query}.
     *
     * @param _query value for instance variable {@link #query}
     */
    public void setQuery(final Query _query)
    {
        this.query = _query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean showOid()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAdmin()
        throws EFapsException
    {
        return Context.getThreadContext().getPerson().isAssigned(Role.get(KernelSettings.USER_ROLE_ADMINISTRATION));
    }
}
