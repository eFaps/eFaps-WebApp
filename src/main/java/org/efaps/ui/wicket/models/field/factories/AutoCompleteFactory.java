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
import org.efaps.ui.wicket.components.autocomplete.AutoCompleteComboBox;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.IAutoComplete;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
//CHECKSTYLE:OFF
public class AutoCompleteFactory
    implements IComponentFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static AutoCompleteFactory FACTORY;

    /**
     * Singelton.
     */
    private AutoCompleteFactory()
    {
    }


    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            ret = new AutoCompleteComboBox(_wicketId, Model.of((IAutoComplete) _uiField), false)
                        .setRequired(_uiField.getFieldConfiguration().getField().isRequired());
        }
        return ret;
    }

    @Override
    public Component getReadOnly(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        // does not have an readonly!
        return null;
    }

    @Override
    public Component getHidden(final String _wicketId,
                               final AbstractUIField _uiField)
        throws EFapsException
    {
     // does not have an hidden!
        return null;
    }

    @Override
    public String getKey()
    {
        return AutoCompleteFactory.class.getName();
    }

    @Override
    public String getPickListValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        // does not have an comparevalue because it must not be sortable
        return null;
    }

    @Override
    public boolean applies(final AbstractUIField _uiField)
        throws EFapsException
    {
        return _uiField.isAutoComplete();
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (AutoCompleteFactory.FACTORY == null) {
            AutoCompleteFactory.FACTORY = new AutoCompleteFactory();
        }
        return AutoCompleteFactory.FACTORY;
    }
}
