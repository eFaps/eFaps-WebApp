/*
 * Copyright 2003 - 2012 The eFaps Team
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

import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.util.EFapsException;


/**
 * UIObjects that implement this interface will be executed in a
 * hirachy from the main object.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface IEventUIObject
{
    /**
     * @param _eventType    type of the event to be executed
     * @param _objectTuples tuple of objects passed to the esjp
     * @return List of Returns
     * @throws EFapsException on error
     */
    List<Return> executeEvents(EventType _eventType,
                               Object... _objectTuples)
        throws EFapsException;
}
