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

package org.efaps.ui.wicket.components.bpm.task;

import java.util.List;

import org.efaps.bpm.BPM;
import org.efaps.ui.wicket.models.objects.UITaskSummary;
import org.efaps.ui.wicket.util.Configuration;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AssignedTaskSummaryProvider
    extends AbstractTaskSummaryProvider
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<UITaskSummary> getUITaskSummary()
    {
        return UITaskSummary.getUITaskSummary(BPM.getTasksAssignedAsPotentialOwner());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAttributeKey4SortProperty()
    {
        return AssignedTaskSummaryProvider.class.getName() + "SortProperty";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAttributeKey4SortOrder()
    {
        return AssignedTaskSummaryProvider.class.getName() + "SortOrder";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowsPerPage()
    {
        return Configuration.getAttributeAsInteger(Configuration.ConfigAttribute.BOARD_ASSIGNEDTASK_MAX);
    }
}
