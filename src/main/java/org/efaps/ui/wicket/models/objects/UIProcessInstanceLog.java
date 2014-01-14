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

import org.apache.wicket.util.time.Duration;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.CachedMultiPrintQuery;
import org.efaps.db.Context;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.jbpm.process.audit.ProcessInstanceLog;

/**
 * Wrapper class for a TaskSummary to be able to replace the values for the
 * UserInterface.
 *
 * @author The eFaps Team
 * @version $Id: UIProcessInstanceLog.java 9959 2013-08-03 18:55:39Z
 *          jan@moxter.net $
 */
public class UIProcessInstanceLog
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
    public UIProcessInstanceLog(final ProcessInstanceLog _processInstance)
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
     * @return the start time of the underlying ProcessInstanceLog
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
     * @return the ProcessInstanceId of the underlying ProcessInstanceLog
     */
    public Long getProcessInstanceId()
    {
        return this.processInstance.getProcessInstanceId();
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
    public String getStatusStr()
    {
        String ret;
        final String base = "org.efaps.ui.wicket.components.bpm.process.ProcessInstance.";
        switch (this.processInstance.getStatus()) {
            case org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED:
                ret = DBProperties.getProperty(base + "STATE_ABORTED");
                break;
            case org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE:
                ret = DBProperties.getProperty(base + "STATE_ACTIVE");
                break;
            case org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED:
                ret = DBProperties.getProperty(base + "STATE_COMPLETED");
                break;
            case org.kie.api.runtime.process.ProcessInstance.STATE_PENDING:
                ret = DBProperties.getProperty(base + "STATE_PENDING");
                break;
            case org.kie.api.runtime.process.ProcessInstance.STATE_SUSPENDED:
                ret = DBProperties.getProperty(base + "STATE_SUSPENDED");
                break;
            default:
                ret = "";
                break;
        }
        return ret;
    }

    /**
     * @return the outcome of the underlying ProcessInstanceLog
     */
    public String getOutcome()
    {
        return this.processInstance.getOutcome();
    }

    /**
     * @return the ProcessName of the underlying ProcessInstanceLog
     */
    public String getProcessName()
    {
        return this.processInstance.getProcessName();
    }

    /**
     * @return the ProcessVersion of the underlying ProcessInstanceLog
     */
    public String getProcessVersion()
    {
        return this.processInstance.getProcessVersion();
    }

    /**
     * @return the Duration of the underlying ProcessInstanceLog
     */
    public Long getDuration()
    {
        final Long ret = this.processInstance.getDuration();
        return ret == null ? 0 : ret;
    }

    /**
     * @return the duration as formated string
     * @throws EFapsException on error
     */
    public String getDurationTime()
        throws EFapsException
    {
        return Duration.valueOf(getDuration()).toString(Context.getThreadContext().getLocale());
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
     * @param _processInstanceLogs list of ProcessInstanceLog the
     *            UIProcessInstance is wanted for
     * @return List of UITaskSummary
     */
    public static List<UIProcessInstanceLog> getUIProcessInstances(final List<ProcessInstanceLog> _processInstanceLogs)
    {
        final List<UIProcessInstanceLog> ret = new ArrayList<UIProcessInstanceLog>();

        for (final ProcessInstanceLog processLog : _processInstanceLogs) {
            ret.add(new UIProcessInstanceLog(processLog));
        }
        return ret;
    }

    /**
     * @return List of processIds
     * @throws EFapsException on error
     */
    public static List<? extends String> getProcessIds()
        throws EFapsException
    {
        final List<String> ret = new ArrayList<String>();
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.BPM);
        final CachedMultiPrintQuery multi = queryBldr.getCachedPrint("BPM");
        multi.addAttribute(CIAdminProgram.BPM.Name);
        multi.execute();
        while (multi.next()) {
            ret.add(multi.<String>getAttribute(CIAdminProgram.BPM.Name));
        }
        return ret;
    }
}
