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

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.LinkWithRangesUI;
import org.efaps.ui.wicket.components.values.DropDownField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public class LinkWithRangesUIFactory
    extends AbstractUIFactory
// CHECKSTYLE:ON
{
    /**
     * Factory Instance.
     */
    private static LinkWithRangesUIFactory FACTORY;

    /**
     * Singelton.
     */
    private LinkWithRangesUIFactory()
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
            ret = new DropDownField(_wicketId, Model.of(_abstractUIField),
                            Model.ofMap((Map<Object, Object>) _abstractUIField.getValue().getEditValue(
                                            _abstractUIField.getParent().getMode())));
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
    protected boolean applies(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return _abstractUIField.getValue().getUIProvider() instanceof LinkWithRangesUI;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected String getReadOnlyValue(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        String strValue = "";
        if (_abstractUIField.getValue().getDbValue() != null) {
            final Map<Object, Object> map = (Map<Object, Object>) _abstractUIField.getValue()
                            .getReadOnlyValue(_abstractUIField.getParent().getMode());
            strValue = String.valueOf(map.get(_abstractUIField.getValue().getDbValue()));
        }
        return strValue;
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (LinkWithRangesUIFactory.FACTORY == null) {
            LinkWithRangesUIFactory.FACTORY = new LinkWithRangesUIFactory();
        }
        return LinkWithRangesUIFactory.FACTORY;
    }
}
