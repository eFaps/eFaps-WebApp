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


package org.efaps.ui.wicket.components.bpm.process;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.bpm.BPM;
import org.efaps.bpm.process.ProcessAdmin;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.bpm.AbstractSortableProvider;
import org.efaps.ui.wicket.models.objects.UIProcessInstanceLog;
import org.efaps.util.EFapsException;
import org.jbpm.process.audit.ProcessInstanceLog;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ProcessInstanceProvider
    extends AbstractSortableProvider<UIProcessInstanceLog>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * ProcessId used as filter.
     */
    private String processId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<UIProcessInstanceLog> getUIValues()
    {
        final ProcessAdmin admin = BPM.getProcessAdmin();
        final List<ProcessInstanceLog> instances;

        if (this.processId != null && !this.processId.isEmpty()) {
            instances = admin.getProcessInstances(this.processId);
        } else {
            instances = admin.getProcessInstances();
        }
        return  UIProcessInstanceLog.getUIProcessInstances(instances);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAttributeKey4SortProperty()
    {
        return ProcessInstanceProvider.class.getName() + "SortProperty";
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getUserAttributeKey4SortOrder()
    {
        return ProcessInstanceProvider.class.getName() + "SortOrder";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowsPerPage()
    {
        return 10;
    }

    @Override
    public Iterator<? extends UIProcessInstanceLog> iterator(final long _first,
                                                          final long _count)
    {
        final String sortprop = getSort().getProperty();
        final boolean asc = getSort().isAscending();

        try {
            Context.getThreadContext().setUserAttribute(getUserAttributeKey4SortOrder(),
                            asc ? SortOrder.ASCENDING.name() : SortOrder.DESCENDING.name());
            Context.getThreadContext().setUserAttribute(getUserAttributeKey4SortProperty(), sortprop);
        } catch (final EFapsException e) {
            // only UserAttributes ==> logging only
            AbstractSortableProvider.LOG.error("error on setting UserAttributes", e);
        }

        Collections.sort(getValues(), new Comparator<UIProcessInstanceLog>()
        {
            @Override
            public int compare(final UIProcessInstanceLog _process0,
                               final UIProcessInstanceLog _process1)
            {
                final UIProcessInstanceLog process0;
                final UIProcessInstanceLog process1;
                if (asc) {
                    process0 = _process0;
                    process1 = _process1;
                } else {
                    process1 = _process0;
                    process0 = _process1;
                }
                int ret = 0;
                if (process0 != null && process1 != null) {
                    if ("processId".equals(sortprop)) {
                        ret = process0.getProcessId().compareTo(process1.getProcessId());
                    } else if ("id".equals(sortprop)) {
                        ret = Long.valueOf(process0.getId()).compareTo(Long.valueOf(process1.getId()));
                    } else if ("start".equals(sortprop)) {
                        ret = process0.getStart().compareTo(process1.getStart());
                    } else if ("end".equals(sortprop)) {
                        ret = process0.getEnd() != null && process1.getEnd() != null
                                        ? process0.getEnd().compareTo(process1.getEnd()) : 0;
                    } else if ("status".equals(sortprop)) {
                        ret = Long.valueOf(process0.getStatus()).compareTo(Long.valueOf(process1.getStatus()));
                    } else if ("processName".equals(sortprop)) {
                        ret = String.valueOf(process0.getProcessName()).compareTo(
                                        String.valueOf(process1.getProcessName()));
                    } else if ("processVersion".equals(sortprop)) {
                        ret = String.valueOf(process0.getProcessVersion()).compareTo(
                                        String.valueOf(process1.getProcessVersion()));
                    } else if ("durationTime".equals(sortprop)) {
                        ret = Long.valueOf(process0.getDuration()).compareTo(Long.valueOf(process1.getDuration()));
                    }
                }
                return ret;
            }
        });
        return getValues().subList(Long.valueOf(_first).intValue(), Long.valueOf(_first + _count).intValue())
                        .iterator();
    }

    /**
     * @see org.apache.wicket.markup.repeater.data.IDataProvider#model(java.lang.Object)
     * @param _object UIProcessInstance the model is wanted for
     * @return Model of UIProcessInstance
     */
    @Override
    public IModel<UIProcessInstanceLog> model(final UIProcessInstanceLog _object)
    {
        return Model.of(_object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultSortProperty()
    {
        return "ProcessId";
    }


    /**
     * Getter method for the instance variable {@link #processId}.
     *
     * @return value of instance variable {@link #processId}
     */
    public String getProcessId()
    {
        return this.processId;
    }


    /**
     * Setter method for instance variable {@link #processId}.
     *
     * @param _processId value for instance variable {@link #processId}
     */
    public void setProcessId(final String _processId)
    {
        this.processId = _processId;
    }
}
