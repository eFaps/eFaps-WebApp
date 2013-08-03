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

import org.jbpm.process.audit.NodeInstanceLog;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UINodeInstanceLog
    implements Serializable
{

    /**
    *
    */
    private static final long serialVersionUID = 1L;

    /**
     * nodeinstancelog this ui belongs to.
     */
    private NodeInstanceLog nodeInstance;

    /**
     * @param _nodeInstanceLog nodeinstancelog this ui belongs to
     */
    public UINodeInstanceLog(final NodeInstanceLog _nodeInstanceLog)
    {
        this.nodeInstance = _nodeInstanceLog;
    }

    public Date getDate()
    {
        return this.nodeInstance.getDate();
    }

    public long getId()
    {
        return this.nodeInstance.getId();
    }

    public String getNodeId()
    {
        return this.nodeInstance.getNodeId();
    }

    public String getNodeInstanceId()
    {
        return this.nodeInstance.getNodeInstanceId();
    }

    public String getNodeName()
    {
        return this.nodeInstance.getNodeName();
    }

    public String getProcessId()
    {
        return this.nodeInstance.getProcessId();
    }

    public long getProcessInstanceId()
    {
        return this.nodeInstance.getProcessInstanceId();
    }

    public long getType()
    {
        return this.nodeInstance.getType();
    }

    /**
     * Getter method for the instance variable {@link #nodeInstance}.
     *
     * @return value of instance variable {@link #nodeInstance}
     */
    public NodeInstanceLog getNodeInstance()
    {
        return this.nodeInstance;
    }

    /**
     * Setter method for instance variable {@link #nodeInstance}.
     *
     * @param _nodeInstance value for instance variable {@link #nodeInstance}
     */
    public void setNodeInstance(final NodeInstanceLog _nodeInstance)
    {
        this.nodeInstance = _nodeInstance;
    }

    /**
     * @param _nodeInstanceLogs list of UINodeInstance the UIProcessInstance is wanted for
     * @return List of UITaskSummary
     */
    public static List<UINodeInstanceLog> getUINodeInstances(final List<NodeInstanceLog> _nodeInstanceLogs)
    {
        final List<UINodeInstanceLog> ret = new ArrayList<UINodeInstanceLog>();

        for (final NodeInstanceLog nodeLog : _nodeInstanceLogs) {
            ret.add(new UINodeInstanceLog(nodeLog));
        }
        return ret;
    }
}
