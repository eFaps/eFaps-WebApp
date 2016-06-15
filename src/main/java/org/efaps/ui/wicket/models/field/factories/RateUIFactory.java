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
package org.efaps.ui.wicket.models.field.factories;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.RateUI;
import org.efaps.admin.datamodel.ui.RateUI.Value;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.values.HiddenRateField;
import org.efaps.ui.wicket.components.values.RateField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * A factory for creating RateUI objects.
 */
@SuppressWarnings("checkstyle:abstractclassname")
public final class RateUIFactory
    extends AbstractUIFactory
{

    /**
     * Factory Instance.
     */
    private static RateUIFactory FACTORY;

    /**
     * Singelton.
     */
    private RateUIFactory()
    {
    }

    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            final RateUI.Value value = (Value) _uiField.getValue().getHiddenValue(_uiField.getParent().getMode());
            _uiField.getFieldConfiguration().evalLabel(_uiField.getValue(), _uiField.getInstance());
            ret = new RateField(_wicketId, Model.of(value), _uiField.getFieldConfiguration());
        }
        return ret;
    }

    @Override
    public Component getHidden(final String _wicketId,
                               final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            final RateUI.Value value = (Value) _uiField.getValue().getHiddenValue(_uiField.getParent().getMode());
            ret = new HiddenRateField(_wicketId, Model.of(value), _uiField.getFieldConfiguration());
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applies(final AbstractUIField _uiField)
        throws EFapsException
    {
        return _uiField.getValue().getUIProvider() instanceof RateUI;
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
            strValue = formatter.format(valueTmp);
        } else if (valueTmp instanceof RateUI.Value) {
            final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                            .getLocale());
            strValue = formatter.format(((RateUI.Value) valueTmp).getRate());
        }
        return strValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return RateUIFactory.class.getName();
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
        if (RateUIFactory.FACTORY == null) {
            RateUIFactory.FACTORY = new RateUIFactory();
        }
        return RateUIFactory.FACTORY;
    }
}
