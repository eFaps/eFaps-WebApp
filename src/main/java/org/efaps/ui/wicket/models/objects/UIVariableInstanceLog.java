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

package org.efaps.ui.wicket.models.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jbpm.process.audit.VariableInstanceLog;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIVariableInstanceLog
    implements Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The underlying Variable Instance log.
     */
    private VariableInstanceLog variableInstanceLog;

    public UIVariableInstanceLog(final VariableInstanceLog _variableInstanceLog)
    {
        this.variableInstanceLog = _variableInstanceLog;
    }

    /**
     * @return the date of the underlying Variable Instance log.
     */
    public Date getDate()
    {
        return this.variableInstanceLog.getDate();
    }

    /**
     * @return the id of the underlying Variable Instance log.
     */
    public Long getId()
    {
        return this.variableInstanceLog.getId();
    }

    /**
     * @return the Process Instance Id of the underlying Variable Instance log.
     */
    public Long getProcessInstanceId()
    {
        return this.variableInstanceLog.getProcessInstanceId();
    }

    /**
     * @return the process id of the underlying Variable Instance log.
     */
    public String getProcessId()
    {
        return this.variableInstanceLog.getProcessId();
    }

    /**
     * @return the value of the underlying Variable Instance log.
     */
    public String getValue()
    {
        return this.variableInstanceLog.getValue();
    }

    /**
     * @return the variable id of the underlying Variable Instance log.
     */
    public String getVariableId()
    {
        return this.variableInstanceLog.getVariableId();
    }

    /**
     * @return the variable instance id of the underlying Variable Instance log.
     */
    public String getVariableInstanceId()
    {
        return this.variableInstanceLog.getVariableInstanceId();
    }

    /**
     * Getter method for the instance variable {@link #variableInstanceLog}.
     *
     * @return value of instance variable {@link #variableInstanceLog}
     */
    public VariableInstanceLog getVariableInstanceLog()
    {
        return this.variableInstanceLog;
    }

    /**
     * Setter method for instance variable {@link #variableInstanceLog}.
     *
     * @param _variableInstanceLog value for instance variable
     *            {@link #variableInstanceLog}
     */
    public void setVariableInstanceLog(final VariableInstanceLog _variableInstanceLog)
    {
        this.variableInstanceLog = _variableInstanceLog;
    }

    /**
     * @param _processInstanceLogs list of ProcessInstanceLog the
     *            UIProcessInstance is wanted for
     * @return List of UITaskSummary
     */
    public static List<UIVariableInstanceLog> getUIVariableInstances(
                                                                     final List<VariableInstanceLog> _varaiableInstanceLogs)
    {
        final List<UIVariableInstanceLog> ret = new ArrayList<UIVariableInstanceLog>();

        for (final VariableInstanceLog variableLog : _varaiableInstanceLogs) {
            ret.add(new UIVariableInstanceLog(variableLog));
        }
        return ret;
    }
}
