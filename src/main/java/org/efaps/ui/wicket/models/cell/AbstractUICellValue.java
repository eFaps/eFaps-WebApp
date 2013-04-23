/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.ui.wicket.models.cell;

import java.util.List;

import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractUICellValue
    extends AbstractUIField
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;


   /**
     * @param _instanceKey key to the instance
     * @param _parent parent object
     * @param _value value
     * @throws EFapsException on error
     */
    public AbstractUICellValue(final String _instanceKey,
                               final AbstractUIObject _parent,
                               final UIValue _value)
        throws EFapsException
    {
        super(_instanceKey, _parent, _value);
    }

    /**
     * @see org.efaps.ui.wicket.models.AbstractInstanceObject#getInstanceFromManager()
     * @return Instance
     * @throws EFapsException on error
     */
    @Override
    public Instance getInstanceFromManager()
        throws EFapsException
    {
        Instance ret = null;
        if (getParent() != null) {
            final AbstractCommand cmd = getParent().getCommand();
            final List<Return> rets = cmd.executeEvents(EventType.UI_INSTANCEMANAGER, ParameterValues.OTHERS,
                            getInstanceKey(), ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
            ret = (Instance) rets.get(0).get(ReturnValues.VALUES);
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    @Override
    public AbstractUIObject getParent()
    {
        return (AbstractUIObject) super.getParent();
    }
}
