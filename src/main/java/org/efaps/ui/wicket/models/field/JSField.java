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
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.AbstractUIModeObject;
import org.efaps.util.EFapsException;

/**
 * The Class JSField.
 */
public class JSField
    extends AbstractUIField
{

    /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new JS field.
     *
     * @param _parent the parent
     * @param _instanceKey the instance key
     * @param _value the value
     * @throws EFapsException the e faps exception
     */
    public JSField(final UIValue _value)
        throws EFapsException
    {
        super(new AbstractUIModeObject(null) {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Instance getInstanceFromManager()
                throws EFapsException
            {
                return null;
            }

            @Override
            public boolean hasInstanceManager()
                throws EFapsException
            {
                return false;
            }

        }.setMode(TargetMode.VIEW), null, _value);
    }
}

