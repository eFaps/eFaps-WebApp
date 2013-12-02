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

package org.efaps.ui.wicket.models.field.factories;

import org.apache.wicket.Component;
import org.efaps.ui.wicket.components.values.LabelField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractUIFactory
    implements IComponentFactory
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Component getReadOnly(final String _wicketId,
                                 final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_abstractUIField)) {
            ret = new LabelField(_wicketId, getReadOnlyValue(_abstractUIField), _abstractUIField
                            .getFieldConfiguration().getLabel(_abstractUIField));
        }
        return ret;
    }

    /**
     * @param _abstractUIField AbstractUIField the component is wanted for
     * @return true if this factory applies to the given Field, else false
     * @throws EFapsException on error
     */
    protected abstract boolean applies(final AbstractUIField _abstractUIField)
        throws EFapsException;

    /**
     * @param _abstractUIField  AbstractUIField the component is wanted for
     * @return String value used for a LabelField
     * @throws EFapsException on error
     */
    protected abstract String getReadOnlyValue(final AbstractUIField _abstractUIField)
        throws EFapsException;
}
