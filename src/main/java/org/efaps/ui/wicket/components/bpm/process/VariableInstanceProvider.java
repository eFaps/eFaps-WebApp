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

import java.util.ArrayList;
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
import org.efaps.ui.wicket.models.objects.UIVariableInstanceLog;
import org.efaps.util.EFapsException;
import org.jbpm.process.audit.VariableInstanceLog;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class VariableInstanceProvider
    extends AbstractSortableProvider<UIVariableInstanceLog>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * ProcessId used as filter.
     */
    private Long processInstanceId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<UIVariableInstanceLog> getUIValues()
    {
        final ProcessAdmin admin = BPM.getProcessAdmin();
        final List<VariableInstanceLog> instances;

        if (this.processInstanceId != null) {
            instances = admin.getVariableInstances(this.processInstanceId);
        } else {
            instances = new ArrayList<VariableInstanceLog>();
        }
        return UIVariableInstanceLog.getUIVariableInstances(instances);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAttributeKey4SortProperty()
    {
        return VariableInstanceProvider.class.getName() + "SortProperty";
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getUserAttributeKey4SortOrder()
    {
        return VariableInstanceProvider.class.getName() + "SortOrder";
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
    public Iterator<? extends UIVariableInstanceLog> iterator(final long _first,
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

        Collections.sort(getValues(), new Comparator<UIVariableInstanceLog>()
        {
            @Override
            public int compare(final UIVariableInstanceLog _var0,
                               final UIVariableInstanceLog _var1)
            {
                final UIVariableInstanceLog var0;
                final UIVariableInstanceLog var1;
                if (asc) {
                    var0 = _var0;
                    var1 = _var1;
                } else {
                    var1 = _var0;
                    var0 = _var1;
                }
                int ret = 0;
                if (var0 != null && var1 != null) {
                    if ("id".equals(sortprop)) {
                        ret = Long.valueOf(var0.getId()).compareTo( Long.valueOf(var1.getId()));
                    } else if ("variableId".equals(sortprop)) {
                        ret = var0.getVariableId().compareTo(var1.getVariableId());
                    } else if ("value".equals(sortprop)) {
                        ret = var0.getValue().compareTo(var1.getValue());
                    } else if ("date".equals(sortprop)) {
                        ret = var0.getDate().compareTo( var1.getDate());
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
     * @param _object UINodeInstance the model is wanted for
     * @return Model of UINodeInstance
     */
    @Override
    public IModel<UIVariableInstanceLog> model(final UIVariableInstanceLog _object)
    {
        return Model.of(_object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultSortProperty()
    {
        return "id";
    }


    /**
     * Getter method for the instance variable {@link #processInstanceId}.
     *
     * @return value of instance variable {@link #processInstanceId}
     */
    public Long getProcessInstanceId()
    {
        return this.processInstanceId;
    }


    /**
     * Setter method for instance variable {@link #processInstanceId}.
     *
     * @param _processInstanceId value for instance variable {@link #processInstanceId}
     */
    public void setProcessInstanceId(final Long _processInstanceId)
    {
        this.processInstanceId = _processInstanceId;
    }
}
