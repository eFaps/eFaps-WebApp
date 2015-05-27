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
import org.efaps.admin.datamodel.ui.StringUI;
import org.efaps.ui.wicket.components.values.HiddenField;
import org.efaps.ui.wicket.components.values.StringField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: StringUIFactory.java 14076 2014-09-22 19:22:26Z jan@moxter.net
 *          $
 */
// CHECKSTYLE:OFF
public class StringUIFactory
    extends AbstractUIFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static StringUIFactory FACTORY;

    /**
     * Singelton.
     */
    protected StringUIFactory()
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
                    ret = new StringField(_wicketId, Model.of(_uiField), _uiField.getFieldConfiguration());
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

    @Override
    public Component getHidden(final String _wicketId,
                               final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            ret = new HiddenField(_wicketId, Model.of(_uiField), _uiField.getFieldConfiguration());
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
        return _uiField.getValue().getUIProvider() instanceof StringUI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getReadOnlyValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        final Object obj = _uiField.getValue().getReadOnlyValue(_uiField.getParent().getMode());
        return obj == null ? "" : String.valueOf(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return StringUIFactory.class.getName();
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
        if (StringUIFactory.FACTORY == null) {
            StringUIFactory.FACTORY = new StringUIFactory();
        }
        return StringUIFactory.FACTORY;
    }
}
