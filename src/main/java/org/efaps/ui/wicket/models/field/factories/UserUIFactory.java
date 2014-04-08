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
import org.efaps.admin.datamodel.ui.UserUI;
import org.efaps.admin.user.Person;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
//CHECKSTYLE:OFF
public class UserUIFactory
    extends AbstractUIFactory
//CHECKSTYLE:ON
{
    /**
     * Factory Instance.
     */
    private static UserUIFactory FACTORY = null;

    /**
     * Singelton.
     */
    private UserUIFactory()
    {
    }

    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return null;
    }

    @Override
    protected boolean applies(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return _abstractUIField.getValue().getUIProvider() instanceof UserUI;
    }

    @Override
    protected String getReadOnlyValue(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        String strValue = "";
        final Object valueTmp = _abstractUIField.getValue()
                        .getReadOnlyValue(_abstractUIField.getParent().getMode());
        if (valueTmp instanceof Person) {
            final Person person = (Person) valueTmp;
            strValue = person.getName();
        }
        return strValue;
    }
    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (UserUIFactory.FACTORY == null) {
            UserUIFactory.FACTORY = new UserUIFactory();
        }
        return UserUIFactory.FACTORY;
    }
}
