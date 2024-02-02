/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.models.field.factories;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.IEnum;
import org.efaps.admin.datamodel.ui.BitEnumUI;
import org.efaps.ui.wicket.components.values.CheckBoxField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.util.EnumUtil;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: BitEnumUIFactory.java 11692 2014-01-14 19:54:16Z jan@moxter.net
 *          $
 */
// CHECKSTYLE:OFF
public final class BitEnumUIFactory
    extends AbstractUIFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static BitEnumUIFactory FACTORY;

    /**
     * Singelton.
     */
    private BitEnumUIFactory()
    {
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_abstractUIField)) {
            ret = new CheckBoxField(_wicketId, Model.of(_abstractUIField),
                            (List<Object>) _abstractUIField.getValue().getEditValue(
                                            _abstractUIField.getParent().getMode()),
                            _abstractUIField.getFieldConfiguration());
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
        return _abstractUIField.getValue().getUIProvider() instanceof BitEnumUI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getReadOnlyValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        final StringBuilder bldr = new StringBuilder();
        final Object valueTmp = _uiField.getValue().getReadOnlyValue(_uiField.getParent().getMode());
        if (valueTmp instanceof List) {
            boolean first = true;
            for (final Object obj : (List<?>) valueTmp) {
                if (first) {
                    first = false;
                } else {
                    bldr.append(", ");
                }
                bldr.append(EnumUtil.getUILabel((IEnum) obj));
            }
        }
        final String ret = bldr.toString();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return BitEnumUIFactory.class.getName();
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
    public Comparable<String> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return getReadOnlyValue(_uiField);
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (BitEnumUIFactory.FACTORY == null) {
            BitEnumUIFactory.FACTORY = new BitEnumUIFactory();
        }
        return BitEnumUIFactory.FACTORY;
    }
}
