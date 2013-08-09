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
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.values.LabelField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public class DateUIFactory
    implements IComponentFactory
// CHECKSTYLE:ON
{
    /**
     * Factory Instance.
     */
    private static DateUIFactory FACTORY;

    /**
     * Singelton.
     */
    private DateUIFactory()
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
        final Component ret = null;
        if (_abstractUIField.getValue().getUIProvider() instanceof DateUI) {
            // TODO
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getReadOnly(final String _wicketId,
                                 final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        Component ret = null;

        if (_abstractUIField.getValue().getUIProvider() instanceof DateUI) {
            String strValue = "";
            final Object valueTmp = _abstractUIField.getValue()
                            .getReadOnlyValue(_abstractUIField.getParent().getMode());
            if (valueTmp instanceof DateTime) {
                final DateTime datetime = (DateTime) valueTmp;
                final DateMidnight dateTmp = new DateMidnight(datetime.getYear(), datetime.getMonthOfYear(),
                                datetime.getDayOfMonth(), Context.getThreadContext().getChronology());
                final DateTimeFormatter formatter = DateTimeFormat.mediumDate();
                strValue = dateTmp.toString(formatter.withLocale(Context.getThreadContext().getLocale()));
            }
            ret = new LabelField(_wicketId, strValue, _abstractUIField.getFieldConfiguration().getLabel());
        }
        return ret;
    }
    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (DateUIFactory.FACTORY == null) {
            DateUIFactory.FACTORY = new DateUIFactory();
        }
        return DateUIFactory.FACTORY;
    }
}
