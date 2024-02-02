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
package org.efaps.ui.wicket.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Session;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class AbstractInstanceObject
    implements Serializable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * key to the instance.
     */
    private String instanceKey;

    /**
     * The id this row got for presentation in the userinterface.
     */
    private String userinterfaceId;

    /**
     * Instantiates a new abstract instance object.
     */
    public AbstractInstanceObject()
    {
    }

    /**
     * Instantiates a new abstract instance object.
     *
     * @param _instanceKey the instance key
     */
    public AbstractInstanceObject(final String _instanceKey)
    {
        this.instanceKey = _instanceKey;
    }

    /**
     * Getter method for instance variable {@link #instanceKey}.
     *
     * @return value of instance variable {@link #instanceKey}
     */
    public String getInstanceKey()
    {
        return this.instanceKey;
    }

    /**
     * @see org.efaps.ui.wicket.models.AbstractInstanceObject#getInstance()
     * @return Instance of the object
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public Instance getInstance()
        throws EFapsException
    {
        Instance ret = null;
        Map<String, Instance> map = (Map<String, Instance>) Session.get().getAttribute(
                        EFapsKey.INSTANCE_CACHEKEY.getKey());
        if (map == null) {
            map = new HashMap<>();
            Session.get().setAttribute(EFapsKey.INSTANCE_CACHEKEY.getKey(), (Serializable) map);
        }
        if (map.containsKey(this.instanceKey)) {
            ret = map.get(this.instanceKey);
        } else {
            if (hasInstanceManager()) {
                ret = getInstanceFromManager();
            } else if ((this.instanceKey != null)
                            && (this.instanceKey.length() > 0)) {
                ret = Instance.get(getInstanceKey());
            }
            map.put(this.instanceKey, ret);
        }
        return ret;
    }

    /**
     * Gets the instance4 create.
     *
     * @param _type the type
     * @return the instance4 create
     */
    public static Instance getInstance4Create(final Type _type)
    {
        return Instance.get(_type, 0);
    }

    /**
     * Setter method for instance variable {@link #instanceKey}.
     *
     * @param _instanceKey value for instance variable {@link #instanceKey}
     */
    public void setInstanceKey(final String _instanceKey)
    {
        this.instanceKey = _instanceKey;
    }

    /**
     * Gets the instance from manager.
     *
     * @return the instance from manager
     * @throws EFapsException on error
     */
    public abstract Instance getInstanceFromManager()
        throws EFapsException;

    /**
     * Checks for instance manager.
     *
     * @return true, if successful
     * @throws EFapsException on error
     */
    public abstract boolean hasInstanceManager()
        throws EFapsException;

    /**
     * Getter method for the instance variable {@link #userinterfaceId}.
     *
     * @return value of instance variable {@link #userinterfaceId}
     */
    public String getUserinterfaceId()
    {
        return this.userinterfaceId;
    }

    /**
     * Setter method for instance variable {@link #userinterfaceId}.
     *
     * @param _userinterfaceId value for instance variable
     *            {@link #userinterfaceId}
     */

    public void setUserinterfaceId(final String _userinterfaceId)
    {
        this.userinterfaceId = _userinterfaceId;
    }

}
