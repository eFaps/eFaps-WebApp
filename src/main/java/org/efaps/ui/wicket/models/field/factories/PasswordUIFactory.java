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

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.PasswordUI;
import org.efaps.ui.wicket.components.values.PasswordField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@SuppressWarnings("checkstyle:abstractclassname")
public final class PasswordUIFactory
    extends AbstractUIFactory
{

    /**
     * Factory Instance.
     */
    private static PasswordUIFactory FACTORY;

    /**
     * Singelton.
     */
    protected PasswordUIFactory()
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
            ret = new PasswordField(_wicketId, Model.of(_uiField), _uiField.getFieldConfiguration());
        }
        return ret;
    }

    @Override
    public Component getHidden(final String _wicketId,
                               final AbstractUIField _uiField)
        throws EFapsException
    {
        // does not have a hidden
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applies(final AbstractUIField _uiField)
        throws EFapsException
    {
        return _uiField.getValue().getUIProvider() instanceof PasswordUI && _uiField.editable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getReadOnlyValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        // does not have an readonly!
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return PasswordUIFactory.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPickListValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return null;
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (PasswordUIFactory.FACTORY == null) {
            PasswordUIFactory.FACTORY = new PasswordUIFactory();
        }
        return PasswordUIFactory.FACTORY;
    }
}
