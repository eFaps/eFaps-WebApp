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
import org.efaps.ui.wicket.models.objects.UIProcessInstance;
import org.efaps.util.EFapsException;
import org.jbpm.process.audit.ProcessInstanceLog;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ProcessInstanceProvider
    extends AbstractSortableProvider<UIProcessInstance>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<UIProcessInstance> getUIValues()
    {
        final ProcessAdmin admin = BPM.getProcessAdmin();
        final List<ProcessInstanceLog> instances = admin.findProcessInstances();
        return  UIProcessInstance.getUITaskSummary(instances);
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
    public Iterator<? extends UIProcessInstance> iterator(final long _first,
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

        Collections.sort(getValues(), new Comparator<UIProcessInstance>()
        {
            @Override
            public int compare(final UIProcessInstance _process0,
                               final UIProcessInstance _process1)
            {
                final UIProcessInstance process0;
                final UIProcessInstance process1;
                if (asc) {
                    process0 = _process0;
                    process1 = _process1;
                } else {
                    process1 = _process0;
                    process0 = _process1;
                }
                int ret = 0;
                if ("processId".equals(sortprop)) {
                    ret = process0.getProcessId().compareTo(process1.getProcessId());
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
    public IModel<UIProcessInstance> model(final UIProcessInstance _object)
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
}
