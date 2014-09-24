/*
 * Copyright 2003 - 2014 The eFaps Team
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
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.date.DatePanel;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;
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
public final class DateUIFactory
    extends AbstractUIFactory
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
        Component ret = null;
        if (applies(_abstractUIField)) {
            ret = new DatePanel(_wicketId, Model.of(_abstractUIField), _abstractUIField.getFieldConfiguration(),
                            _abstractUIField.getValue().getEditValue(_abstractUIField.getParent().getMode()));
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applies(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return _abstractUIField.getValue().getUIProvider() instanceof DateUI;
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
        if (valueTmp instanceof DateTime) {
            final DateTime datetime = (DateTime) valueTmp;
            final DateTime dateTmp = datetime.withChronology(Context.getThreadContext().getChronology())
                            .withTimeAtStartOfDay();
            final DateTimeFormatter formatter = DateTimeFormat.mediumDate();
            strValue = dateTmp.toString(formatter.withLocale(Context.getThreadContext().getLocale()));
        }
        return strValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return DateUIFactory.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPickListValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return getReadOnlyValue(_uiField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return (Comparable<?>) _uiField.getValue().getReadOnlyValue(_uiField.getParent().getMode());
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
