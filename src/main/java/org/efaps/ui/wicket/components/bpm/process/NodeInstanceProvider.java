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
import org.efaps.ui.wicket.models.objects.UINodeInstanceLog;
import org.efaps.util.EFapsException;
import org.jbpm.process.audit.NodeInstanceLog;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class NodeInstanceProvider
    extends AbstractSortableProvider<UINodeInstanceLog>
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
    protected List<UINodeInstanceLog> getUIValues()
    {
        final ProcessAdmin admin = BPM.getProcessAdmin();
        final List<NodeInstanceLog> instances;

        if (this.processInstanceId != null) {
            instances = admin.getNodeInstances(this.processInstanceId);
        } else {
            instances = new ArrayList<NodeInstanceLog>();
        }
        return UINodeInstanceLog.getUINodeInstances(instances);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAttributeKey4SortProperty()
    {
        return NodeInstanceProvider.class.getName() + "SortProperty";
    }

    /**
     * {@inheritDoc}
     */

    @Override
    protected String getUserAttributeKey4SortOrder()
    {
        return NodeInstanceProvider.class.getName() + "SortOrder";
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
    public Iterator<? extends UINodeInstanceLog> iterator(final long _first,
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

        Collections.sort(getValues(), new Comparator<UINodeInstanceLog>()
        {
            @Override
            public int compare(final UINodeInstanceLog _node0,
                               final UINodeInstanceLog _node1)
            {
                final UINodeInstanceLog node0;
                final UINodeInstanceLog node1;
                if (asc) {
                    node0 = _node0;
                    node1 = _node1;
                } else {
                    node1 = _node0;
                    node0 = _node1;
                }
                int ret = 0;
                if (node0 != null && node1 != null) {
                    if ("id".equals(sortprop)) {
                        ret = Long.valueOf(node0.getId()).compareTo( Long.valueOf(node1.getId()));
                    } else if ("nodeId".equals(sortprop)) {
                        ret = node0.getNodeId().compareTo(node1.getNodeId());
                    } else if ("nodeInstanceId".equals(sortprop)) {
                        ret = node0.getNodeInstanceId().compareTo(node1.getNodeInstanceId());
                    } else if ("nodeName".equals(sortprop)) {
                        ret = node0.getNodeName().compareTo(node1.getNodeName());
                    } else if ("type".equals(sortprop)) {
                        ret = Long.valueOf(node0.getType()).compareTo( Long.valueOf(node1.getType()));
                    } else if ("date".equals(sortprop)) {
                        ret = node0.getDate().compareTo(node1.getDate());
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
    public IModel<UINodeInstanceLog> model(final UINodeInstanceLog _object)
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
