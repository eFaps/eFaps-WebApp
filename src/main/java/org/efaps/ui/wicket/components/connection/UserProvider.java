/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.ui.wicket.components.connection;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.bpm.AbstractSortableProvider;
import org.efaps.ui.wicket.models.objects.UIUser;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class UserProvider
    extends AbstractSortableProvider<UIUser>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Iterator<UIUser> iterator(final long _first,
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

        Collections.sort(getValues(), new Comparator<UIUser>()
        {

            @Override
            public int compare(final UIUser _user0,
                               final UIUser _user1)
            {
                final UIUser user0;
                final UIUser user1;
                if (asc) {
                    user0 = _user0;
                    user1 = _user1;
                } else {
                    user1 = _user0;
                    user0 = _user1;
                }
                int ret = 0;
                if ("userName".equals(sortprop)) {
                    ret = user0.getUserName().compareTo(user1.getUserName());
                }
                return ret;
            }
        });
        return getValues().subList(Long.valueOf(_first).intValue(), Long.valueOf(_first + _count).intValue())
                        .iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IModel<UIUser> model(final UIUser _object)
    {
        return Model.of(_object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<UIUser> getUIValues()
    {
        return UIUser.getUIUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAttributeKey4SortProperty()
    {
        return UserProvider.class.getName() + "SortProperty";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAttributeKey4SortOrder()
    {
        return UserProvider.class.getName() + "SortOrder";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultSortProperty()
    {
        return "userName";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowsPerPage()
    {
        return Configuration.getAttributeAsInteger(Configuration.ConfigAttribute.WEBSOCKET_MESSAGETABLE_MAX);
    }
}
