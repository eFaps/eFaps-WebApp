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
package org.efaps.ui.wicket.components.connection;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.AbstractSortableProvider;
import org.efaps.ui.wicket.models.objects.UIUserSession;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 */
public class SessionProvider
    extends AbstractSortableProvider<UIUserSession>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Iterator<UIUserSession> iterator(final long _first,
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

        Collections.sort(getValues(), (_session0, _session1) -> {
            final UIUserSession session0;
            final UIUserSession session1;
            if (asc) {
                session0 = _session0;
                session1 = _session1;
            } else {
                session1 = _session0;
                session0 = _session1;
            }
            int ret = 0;
            if ("userName".equals(sortprop)) {
                ret = session0.getUserName().compareTo(session1.getUserName());
            } else if ("sessionId".equals(sortprop)) {
                ret = session0.getSessionId().compareTo(session1.getSessionId());
            } else if ("lastActivity".equals(sortprop)) {
                ret = session0.getLastActivity().compareTo(session1.getLastActivity());
            }
            return ret;
        });
        return getValues().subList(Long.valueOf(_first).intValue(), Long.valueOf(_first + _count).intValue())
                        .iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IModel<UIUserSession> model(final UIUserSession _object)
    {
        return Model.of(_object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<UIUserSession> getUIValues()
    {
        return UIUserSession.getUIUserSessions();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAttributeKey4SortProperty()
    {
        return SessionProvider.class.getName() + "SortProperty";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAttributeKey4SortOrder()
    {
        return SessionProvider.class.getName() + "SortOrder";
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
        return Configuration.getAttributeAsInteger(Configuration.ConfigAttribute.WEBSOCKET_SESSIONTABLE_MAX);
    }
}
