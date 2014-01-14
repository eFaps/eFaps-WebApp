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

package org.efaps.ui.wicket.components.bpm.task;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.UUID;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.bpm.AbstractSortableProvider;
import org.efaps.ui.wicket.models.objects.UITaskSummary;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AbstractTaskSummaryProvider.java 9950 2013-08-02 22:10:34Z
 *          jan@moxter.net $
 */
public abstract class AbstractTaskSummaryProvider
    extends AbstractSortableProvider<UITaskSummary>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Iterator<UITaskSummary> iterator(final long _first,
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

        Collections.sort(getValues(), new Comparator<UITaskSummary>()
        {

            @Override
            public int compare(final UITaskSummary _task0,
                               final UITaskSummary _task1)
            {
                final UITaskSummary task0;
                final UITaskSummary task1;
                if (asc) {
                    task0 = _task0;
                    task1 = _task1;
                } else {
                    task1 = _task0;
                    task0 = _task1;
                }
                int ret = 0;
                if ("description".equals(sortprop)) {
                    ret = task0.getDescription().compareTo(task1.getDescription());
                } else if ("activationTime".equals(sortprop)) {
                    ret = task0.getActivationTime().compareTo(task1.getActivationTime());
                } else if ("name".equals(sortprop)) {
                    ret = task0.getName().compareTo(task1.getName());
                } else if ("id".equals(sortprop)) {
                    ret = task0.getId().compareTo(task1.getId());
                } else if ("status".equals(sortprop)) {
                    ret = task0.getStatus().compareTo(task1.getStatus());
                } else if ("owner".equals(sortprop)) {
                    ret = task0.getOwner().compareTo(task1.getOwner());
                }
                return ret;
            }
        });
        return getValues().subList(Long.valueOf(_first).intValue(), Long.valueOf(_first + _count).intValue())
                        .iterator();
    }

    /**
     * @see org.apache.wicket.markup.repeater.data.IDataProvider#model(java.lang.Object)
     * @param _object TaskSummary the model is wanted for
     * @return Model of TaskSummary
     */
    @Override
    public IModel<UITaskSummary> model(final UITaskSummary _object)
    {
        return Model.of(_object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultSortProperty()
    {
        return "description";
    }

    /**
     * @return true if the OID columns should be shown
     * @throws EFapsException on error
     */
    public boolean showOid()
        throws EFapsException
    {
        // Administration Role
        return Configuration.getAttributeAsBoolean(Configuration.ConfigAttribute.SHOW_OID)
                        && Context.getThreadContext().getPerson()
                                        .isAssigned(Role.get(UUID
                                                        .fromString("1d89358d-165a-4689-8c78-fc625d37aacd")));
    }
}
