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
import org.efaps.admin.datamodel.ui.FormatedStringUI;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * A factory for creating FormatedStringUI objects.
 */
@SuppressWarnings("checkstyle:abstractclassname")
public class FormatedStringUIFactory
    extends AbstractUIFactory
{

    /**
     * Factory Instance.
     */
    private static FormatedStringUIFactory FACTORY;

    /**
     * Singelton.
     */
    private FormatedStringUIFactory()
    {
    }

    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        return null;
    }

    @Override
    public String getKey()
    {
        return FormatedStringUIFactory.class.getName();
    }

    @Override
    public String getPickListValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return null;
    }

    @Override
    public Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return null;
    }

    @Override
    public boolean applies(final AbstractUIField _uiField)
        throws EFapsException
    {
        return _uiField.getValue().getUIProvider() instanceof FormatedStringUI;
    }

    @Override
    protected String getReadOnlyValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return null;
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (FormatedStringUIFactory.FACTORY == null) {
            FormatedStringUIFactory.FACTORY = new FormatedStringUIFactory();
        }
        return FormatedStringUIFactory.FACTORY;
    }
}
