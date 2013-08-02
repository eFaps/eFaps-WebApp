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

package org.efaps.ui.wicket.models.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jbpm.process.audit.ProcessInstanceLog;

/**
 * Wrapper class for a TaskSummary to be able to replace the values for the
 * UserInterface.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIProcessInstance
    implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Wrapped ProcessInstanceLog.
     */
    private final ProcessInstanceLog processInstance;

    /**
     * @param _processInstance ProcessInstanceLog to use
     */
    public UIProcessInstance(final ProcessInstanceLog _processInstance)
    {
        this.processInstance = _processInstance;
    }

    /**
     * @return the processID of the underlying ProcessInstanceLog
     */
    public String getProcessId()
    {
        return this.processInstance.getProcessId();
    }

    /**
     * @return the start time  of the underlying ProcessInstanceLog
     */
    public Date getStart()
    {
        return this.processInstance.getStart();
    }

    /**
     * @return the end time of the underlying ProcessInstanceLog
     */
    public Date getEnd()
    {
        return this.processInstance.getEnd();
    }

    /**
     * @return the id of the underlying ProcessInstanceLog
     */
    public Long getId()
    {
        return this.processInstance.getId();
    }

    /**
     * @return the translated Status of the underlying TaskSummary
     */
    public int getStatus()
    {
        return this.processInstance.getStatus();
    }

    /**
     * @return the translated Status of the underlying TaskSummary
     */
    public String getOutcome()
    {
        return this.processInstance.getOutcome();
    }

    /**
     * Getter method for the instance variable {@link #processInstance}.
     *
     * @return value of instance variable {@link #processInstance}
     */
    public ProcessInstanceLog getProcessInstanceLog()
    {
        return this.processInstance;
    }

    /**
     * @param _processInstanceLos list of ProcessInstanceLog the UIProcessInstance is wanted for
     * @return List of UITaskSummary
     */
    public static List<UIProcessInstance> getUITaskSummary(final List<ProcessInstanceLog> _processInstanceLogs)
    {
        final List<UIProcessInstance> ret = new ArrayList<UIProcessInstance>();

        for (final ProcessInstanceLog processLog : _processInstanceLogs) {
            ret.add(new UIProcessInstance(processLog));
        }
        return ret;
    }
}
