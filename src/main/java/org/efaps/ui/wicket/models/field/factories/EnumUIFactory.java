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

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.IEnum;
import org.efaps.admin.datamodel.ui.EnumUI;
import org.efaps.ui.wicket.components.values.RadioField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.set.UIFieldSetValue;
import org.efaps.ui.wicket.util.EnumUtil;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public final class EnumUIFactory
    extends AbstractUIFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static EnumUIFactory FACTORY;

    /**
     * Singelton.
     */
    private EnumUIFactory()
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
            ret = new RadioField(_wicketId, Model.of(_uiField), _uiField.getValue().getEditValue(
                            _uiField.getParent().getMode()),
                            _uiField.getFieldConfiguration(),
                            _uiField instanceof UIFieldSetValue);
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
        return _uiField.getValue().getUIProvider() instanceof EnumUI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getReadOnlyValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        final Object valueTmp = _uiField.getValue()
                        .getReadOnlyValue(_uiField.getParent().getMode());
        final String ret = valueTmp == null ? null : EnumUtil.getUILabel((IEnum) valueTmp);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return EnumUIFactory.class.getName();
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
        if (EnumUIFactory.FACTORY == null) {
            EnumUIFactory.FACTORY = new EnumUIFactory();
        }
        return EnumUIFactory.FACTORY;
    }
}
