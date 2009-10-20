/*
 * Copyright 2003 - 2009 The eFaps Team
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

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * @author The eFasp Team
 * @version $Id$
 */
public class UIFieldTable extends UITable implements IFormElement
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UIFieldTable.class);

    /**
     * Id of the FieldTable.
     */
    private final long id;

    /**
     * Name of the FieldTable.
     */
    private final String name;

    /**
     * Stores if the table is the first table inside a form. because the
     * first table adds some things to the page that works for all tables
     * inside the same page.
     */
    private boolean firstTable = true;
    /**
     * @param _commanduuid  uuid of the command
     * @param _instanceKey  key to the instance
     * @param _fieldTable   fieltable
     * @throws EFapsException on error
     */
    public UIFieldTable(final UUID _commanduuid, final String _instanceKey, final FieldTable _fieldTable)
        throws EFapsException
    {
        super(_commanduuid, _instanceKey);
        setTableUUID(_fieldTable.getTargetTable().getUUID());
        this.id = _fieldTable.getId();
        this.name = _fieldTable.getName();
        try {
            if (Context.getThreadContext().containsUserAttribute(getUserAttributeKey(UserAttributeKey.SORTKEY))) {
                setSortKey(Context.getThreadContext().getUserAttribute(getUserAttributeKey(UserAttributeKey.SORTKEY)));
            }
            if (Context.getThreadContext().containsUserAttribute(getUserAttributeKey(UserAttributeKey.SORTDIRECTION))) {
                setSortDirection(SortDirection.getEnum((Context.getThreadContext()
                                .getUserAttribute(getUserAttributeKey(UserAttributeKey.SORTDIRECTION)))));
            }
        } catch (final EFapsException e) {
            // we don't throw an error because this are only Usersettings
            UIFieldTable.LOG.error("error during the retrieve of UserAttributes", e);
        }
    }

    /**
     * Getter method for instance variable {@link #firstTable}.
     *
     * @return value of instance variable {@link #firstTable}
     */
    public boolean isFirstTable()
    {
        return this.firstTable;
    }

    /**
     * Setter method for instance variable {@link #firstTable}.
     *
     * @param _firstTable value for instance variable {@link #firstTable}
     */
    public void setFirstTable(final boolean _firstTable)
    {
        this.firstTable = _firstTable;
    }

    /**
     * Getter method for instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<List<Instance>> getInstanceListsOld() throws EFapsException
    {
        final List<Return> ret = FieldTable.get(this.id).executeEvents(EventType.UI_TABLE_EVALUATE,
                        ParameterValues.INSTANCE, getInstance());
        final List<List<Instance>> lists = (List<List<Instance>>) ret.get(0).get(ReturnValues.VALUES);
        return lists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected List<Instance> getInstanceList() throws EFapsException
    {
        final List<Return> ret = FieldTable.get(this.id).executeEvents(EventType.UI_TABLE_EVALUATE,
                        ParameterValues.INSTANCE, getInstance());
        final List<Instance> lists = (List<Instance>) ret.get(0).get(ReturnValues.VALUES);
        return lists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAttributeKey(final UserAttributeKey _key)
    {
        return super.getCommandUUID() + "-" + this.name + "-" + _key.getValue();
    }

    /**
     * Method to get the events that are related to this UITable.
     * @param _eventType eventype to get
     * @return List of events
     */
    @Override
    protected List<EventDefinition> getEvents(final EventType _eventType)
    {
        return Field.get(this.id).getEvents(_eventType);
    }
}
