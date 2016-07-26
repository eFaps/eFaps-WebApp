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

package org.efaps.ui.wicket.models.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.ui.wicket.connectionregistry.RegistryManager;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class UIUser
    implements Serializable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * UserName.
     */
    private String userName;

    /**
     * @param _userName username
     */
    public UIUser(final String _userName)
    {
        this.userName = _userName;
    }

    /**
     * Getter method for the instance variable {@link #userName}.
     *
     * @return value of instance variable {@link #userName}
     */
    public String getUserName()
    {
        return this.userName;
    }

    /**
     * Setter method for instance variable {@link #userName}.
     *
     * @param _userName value for instance variable {@link #userName}
     */
    public void setUserName(final String _userName)
    {
        this.userName = _userName;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return list of currently registered Users
     */
    public static List<UIUser> getUIUser()
    {
        final List<UIUser> ret = new ArrayList<>();
        final Collection<String> users = RegistryManager.getUsers();
        for (final String user : users) {
            ret.add(new UIUser(user));
        }
        return ret;
    }

}
