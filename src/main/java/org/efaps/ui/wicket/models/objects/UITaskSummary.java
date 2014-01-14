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
import java.util.UUID;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.user.Person;
import org.efaps.util.EFapsException;
import org.kie.api.task.model.TaskSummary;


/**
 * Wrapper class for a TaskSummary to be able to replace the values for the
 * UserInterface.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UITaskSummary
    implements Serializable
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Wrapped TaskSummary.
     */
    private final TaskSummary taskSummary;

    /**
     * @param _taskSummary taskSumary to use
     */
    public UITaskSummary(final TaskSummary _taskSummary)
    {
        this.taskSummary = _taskSummary;
    }

    /**
     * @return the description of the underlying TaskSummary
     */
    public String getDescription()
    {
        return this.taskSummary.getDescription();
    }

    /**
     * @return the description of the underlying TaskSummary
     */
    public String getProcessId()
    {
        return this.taskSummary.getProcessId();
    }

    /**
     * @return the description of the underlying TaskSummary
     */
    public Long getProcessInstanceId()
    {
        return this.taskSummary.getProcessInstanceId();
    }

    /**
     * @return the activation time  of the underlying TaskSummary
     */
    public Date getActivationTime()
    {
        return this.taskSummary.getActivationTime();
    }

    /**
     * @return the name of the underlying TaskSummary
     */
    public String getName()
    {
        return this.taskSummary.getName();
    }

    /**
     * @return the id of the underlying TaskSummary
     */
    public Long getId()
    {
        return this.taskSummary.getId();
    }

    /**
     * @return the translated Status of the underlying TaskSummary
     */
    public String getStatus()
    {
        return DBProperties.getProperty(UITaskSummary.class.getName() + ".Status."
                        + this.taskSummary.getStatus().toString());
    }

    /**
     * Getter method for the instance variable {@link #taskSummary}.
     *
     * @return value of instance variable {@link #taskSummary}
     */
    public TaskSummary getTaskSummary()
    {
        return this.taskSummary;
    }

    /**
     * @return the String representation of the Person of the underlying TaskSummary
     */
    public String getOwner()
    {
        Person person = null;
        try {
            if (this.taskSummary.getActualOwner() != null) {
                person = Person.get(UUID.fromString(this.taskSummary.getActualOwner().getId()));
            }
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return person == null ? "" : person.getFirstName() + " " + person.getLastName();
    }

    /**
     * @param _taskSummaries list of TaskSummary the UITaskSummary is wanted for
     * @return List of UITaskSummary
     */
    public static List<UITaskSummary> getUITaskSummary(final List<TaskSummary> _taskSummaries)
    {
        final List<UITaskSummary> ret = new ArrayList<UITaskSummary>();

        for (final TaskSummary taskSum : _taskSummaries) {
            ret.add(new UITaskSummary(taskSum));
        }
        return ret;
    }
}
