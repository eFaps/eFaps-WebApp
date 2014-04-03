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


package org.efaps.ui.wicket.models.task;

import java.io.Serializable;
import java.util.UUID;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.user.Person;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DelegatePerson
    implements Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * UUID of the Role.
     */
    private final UUID uuid;

    /**
     * Name of the Person.
     */
    private final String name;

    /**
     * firstName of the Person.
     */
    private final String firstName;

    /**
     * lastName of the Person.
     */
    private final String lastName;

    /**
     * @param _person Person the delegate belongs to
     */
    public DelegatePerson(final Person _person)
    {
        this(_person.getUUID(), _person.getName(), _person.getFirstName(), _person.getLastName());
    }

    /**
     * @param _uuid UUDI
     * @param _name name
     * @param _firstName fistName
     * @param _lastname lastName
     */
    public DelegatePerson(final UUID _uuid,
                          final String _name,
                          final String _firstName,
                          final String _lastname)
    {
        this.uuid = _uuid;
        this.name = _name;
        this.firstName = _firstName;
        this.lastName = _lastname;
    }

    /**
     * Private Constructor for model reasons
     */
    private DelegatePerson()
    {
        this.uuid = null;
        this.name = null;
        this.firstName = null;
        this.lastName = null;
    }

    /**
     * Getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Getter method for the instance variable {@link #uuid}.
     *
     * @return value of instance variable {@link #uuid}
     */
    public UUID getUuid()
    {
        return this.uuid;
    }

    /**
     * Getter method for the instance variable {@link #firstName}.
     *
     * @return value of instance variable {@link #firstName}
     */
    public String getFirstName()
    {
        return this.firstName;
    }

    /**
     * Getter method for the instance variable {@link #lastName}.
     *
     * @return value of instance variable {@link #lastName}
     */
    public String getLastName()
    {
        return this.lastName;
    }

    /**
     * @return an empty defautl model.
     */
    public static IModel<DelegatePerson> getModel()
    {
        return Model.of(new DelegatePerson());
    }
}
