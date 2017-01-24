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
import java.util.List;

import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public interface ICmdUIObject
    extends Serializable
{

    /**
     * @return the command belonging to this CommandModel
     * @throws EFapsException on error
     */
    AbstractCommand getCommand()
        throws EFapsException;

    /**
     * @return the instance key for this object
     * @throws EFapsException on error
     */
    Instance getInstance()
        throws EFapsException;

    /**
     * This method executes the Events which are related to this Model. It will
     * take the Events of the CallingCommand {@link #callingCmdUUID}, if it is
     * declared, otherwise it will take the Events of the Command
     * {@link #cmdUUID}. The Method also adds the oid {@link #instanceKey} to
     * the Context, so that it is accessible for the esjp.<br>
     * This method throws an eFpasError to provide the possibility for different
     * responses in the components.
     *
     * @param _eventType the event type
     * @param _objectTuples n tuples of ParamterValue and Object
     * @return List of Returns
     * @throws EFapsException on error
     */
    List<Return> executeEvents(EventType _eventType,
                               Object... _objectTuples)
        throws EFapsException;

    /**
     * @return the command that called this CommandModel
     * @throws EFapsException on error
     */
    default AbstractCommand getCallingCommand()
        throws EFapsException
    {
        return null;
    }

    /**
     * Checks for calling command.
     *
     * @return true, if successful
     * @throws EFapsException on error
     */
    default boolean hasCallingCommand()
        throws EFapsException
    {
        return getCallingCommand() != null;
    }

}
