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
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.TypeUI;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public final class TypeUIFactory
    extends AbstractUIFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static TypeUIFactory FACTORY;

    /**
     * Singelton.
     */
    private TypeUIFactory()
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
           //there is no default editable field for TypeUI, teherfor give the chance to UIType
           ret = UITypeFactory.get().getEditable(_wicketId, _uiField);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getHidden(final String _wicketId,
                               final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
           //there is no default hidden field for TypeUI, teherfor give the chance to UIType
           ret = UITypeFactory.get().getEditable(_wicketId, _uiField);
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
        return _uiField.getValue().getUIProvider() instanceof TypeUI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getReadOnlyValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        Object ret = _uiField.getValue().getReadOnlyValue(_uiField.getParent().getMode());
        if (ret instanceof Type) {
            ret = ((Type) ret).getLabel();
        }
        return ret == null ? "" : String.valueOf(ret);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return TypeUIFactory.class.getName();
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
        return getReadOnlyValue(_uiField);
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (TypeUIFactory.FACTORY == null) {
            TypeUIFactory.FACTORY = new TypeUIFactory();
        }
        return TypeUIFactory.FACTORY;
    }
}
