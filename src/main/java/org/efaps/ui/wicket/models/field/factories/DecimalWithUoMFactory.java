/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.ui.wicket.models.field.factories;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.ui.DecimalWithUoMUI;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.values.NumberWithUoMField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

//CHECKSTYLE:OFF
public class DecimalWithUoMFactory
    extends AbstractUIFactory
//CHECKSTYLE:ON
{
    /**
     * Factory Instance.
     */
    private static DecimalWithUoMFactory FACTORY;

    /**
     * Singelton.
     */
    private DecimalWithUoMFactory()
    {
    }

    @Override
    public Component getEditable(final String _wicketId, final AbstractUIField _uiField)
        throws EFapsException
    {
        return new NumberWithUoMField(_wicketId, Model.of(_uiField));
    }

    @Override
    public String getKey()
    {
        return DecimalWithUoMFactory.class.getName();
    }

    @Override
    public String getPickListValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return getReadOnlyValue(_uiField);
    }

    @Override
    public Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return getReadOnlyValue(_uiField);
    }

    @Override
    public boolean applies(final AbstractUIField _uiField)
        throws EFapsException
    {
        return _uiField.getValue().getUIProvider() instanceof DecimalWithUoMUI;
    }

    @Override
    protected String getReadOnlyValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        String strValue = "";
        final Object valueTmp = _uiField.getValue().getReadOnlyValue(_uiField.getParent().getMode());
        if (valueTmp instanceof Object[]) {
            final Object[] valueArray = (Object[]) valueTmp;
            final Object value = valueArray[0];
            if (value != null) {
                final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                                .getLocale());
                if (_uiField.getValue().getAttribute() != null) {
                    formatter.setMaximumFractionDigits(_uiField.getValue().getAttribute().getScale());
                }
                strValue = formatter.format(value);
                strValue = strValue + " " + ((UoM) valueArray[1]).getSymbol();
            }
        }
        return strValue;
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (DecimalWithUoMFactory.FACTORY == null) {
            DecimalWithUoMFactory.FACTORY = new DecimalWithUoMFactory();
        }
        return DecimalWithUoMFactory.FACTORY;
    }
}
