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

package org.efaps.ui.wicket.models.field;

import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.user.AbstractUserObject;
import org.efaps.ui.wicket.models.objects.AbstractUIModeObject;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class UIField
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
    public UIField(final AbstractUIModeObject _parent,
                   final String _instanceKey,
                   final UIValue _value)
        throws EFapsException
    {
        super(_parent, _instanceKey, _value);
        if (getValue() != null && getValue().getObject() != null
                        && getValue().getObject() instanceof AbstractUserObject) {
            setInstanceKey(((AbstractUserObject) getValue().getObject()).getInstance().getKey());
        }
    }
}
