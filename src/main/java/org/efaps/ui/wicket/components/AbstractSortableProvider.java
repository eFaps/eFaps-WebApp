/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.components;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author The eFaps Team
 * @param <T> Sortable object
 */
public abstract class AbstractSortableProvider<T>
    extends SortableDataProvider<T, String>
{
    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractSortableProvider.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * List of TaskSummary to be displayed.
     */
    private final List<T> uiValues;

    /**
     * Constructor.
     */
    public AbstractSortableProvider()
    {
        String property = null;
        SortOrder order = null;
        try {
            property = Context.getThreadContext().getUserAttribute(getUserAttributeKey4SortProperty());
            final String orderTmp = Context.getThreadContext().getUserAttribute(getUserAttributeKey4SortOrder());
            if (orderTmp != null) {
                order = SortOrder.valueOf(orderTmp);
            }
        } catch (final EFapsException e) {
            // only UserAttributes ==> logging only
            AbstractSortableProvider.LOG.error("error on retrieving UserAttributes", e);
        }
        setSort(property == null ? getDefaultSortProperty() : property, order == null ? SortOrder.ASCENDING : order);
        this.uiValues = getUIValues();
    }

    /**
     * @see org.apache.wicket.markup.repeater.data.IDataProvider#size()
     * @return size of the list of TaskSummary
     */
    @Override
    public long size()
    {
        return this.uiValues.size();
    }


    /**
     * @return the list of values provided by this provider
     */
    protected List<T> getValues()
    {
        return this.uiValues;
    }

    /**
     * Requery the data.
     */
    public void requery()
    {
        this.uiValues.clear();
        this.uiValues.addAll(getUIValues());
    }

    /**
     * @return list of UITaskSummary.
     */
    protected abstract List<T> getUIValues();

    /**
     * @return the key used to store the sort property as a UserAttribute.
     */
    protected abstract String getUserAttributeKey4SortProperty();

    /**
     * @return the key used to store the sort order as a UserAttribute.
     */
    protected abstract String getUserAttributeKey4SortOrder();

    /**
     * @return the default sort property.
     */
    protected abstract String getDefaultSortProperty();

    /**
     * @return the number of rows presented per page
     */
    public abstract int getRowsPerPage();
}
