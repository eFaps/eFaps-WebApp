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
import org.efaps.admin.datamodel.ui.NumberUI;
import org.efaps.ui.wicket.components.values.NumberField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: NumberUIFactory.java 14033 2014-09-15 21:44:25Z jan@moxter.net
 *          $
 */
// CHECKSTYLE:OFF
public final class NumberUIFactory
    extends StringUIFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static NumberUIFactory FACTORY;

    /**
     * Singelton.
     */
    private NumberUIFactory()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            switch (_uiField.getFieldConfiguration().getUIType()) {
                case DEFAULT:
                    ret = new NumberField(_wicketId, Model.of(_uiField), _uiField.getFieldConfiguration());
                    break;
                case BUTTON:
                case CHECKBOX:
                case DROPDOWN:
                case RADIO:
                case SNIPPLET:
                    ret = ((UITypeFactory) UITypeFactory.get()).getEditableComp(_wicketId, _uiField);
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getHidden(final String _wicketId,
                               final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applies(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return _abstractUIField.getValue().getUIProvider() instanceof NumberUI;
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
            strValue = String.valueOf(valueTmp);
        } else {
            strValue = valueTmp == null ? "" : String.valueOf(valueTmp);
        }
        return strValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return NumberUIFactory.class.getName();
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
        if (NumberUIFactory.FACTORY == null) {
            NumberUIFactory.FACTORY = new NumberUIFactory();
        }
        return NumberUIFactory.FACTORY;
    }
}
