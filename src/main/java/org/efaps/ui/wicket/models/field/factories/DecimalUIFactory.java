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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.DecimalUI;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.values.NumberField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public class DecimalUIFactory
extends StringUIFactory
// CHECKSTYLE:ON
{
    /**
     * Factory Instance.
     */
    private static DecimalUIFactory FACTORY;

    /**
     * Singelton.
     */
    private DecimalUIFactory()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_abstractUIField)) {
            ret = new NumberField(_wicketId, Model.of(_abstractUIField), _abstractUIField.getFieldConfiguration());
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean applies(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return _abstractUIField.getValue().getUIProvider() instanceof DecimalUI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getReadOnlyValue(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        String strValue = "";
        final Object valueTmp = _abstractUIField.getValue()
                        .getReadOnlyValue(_abstractUIField.getParent().getMode());
        if (valueTmp instanceof Number) {
            final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                            .getLocale());
            if (_abstractUIField.getValue().getAttribute() != null) {
                formatter.setMaximumFractionDigits(_abstractUIField.getValue().getAttribute().getScale());
            }
            strValue = formatter.format(valueTmp);
        }
        return strValue;
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (DecimalUIFactory.FACTORY == null) {
            DecimalUIFactory.FACTORY = new DecimalUIFactory();
        }
        return DecimalUIFactory.FACTORY;
    }
}
