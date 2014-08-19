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
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Menu;
import org.efaps.ui.wicket.components.links.MenuContentAjaxLink;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public class HRefFactory
    implements IComponentFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static HRefFactory FACTORY;

    /**
     * Singelton.
     */
    private HRefFactory()
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
        // never ever do a link when it is editable
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getReadOnly(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (TargetMode.VIEW.equals(_uiField.getParent().getMode())
                        && _uiField.getFieldConfiguration().getField().getReference() != null
                        && _uiField.getInstanceKey() != null) {
            final Menu menu = Menu.getTypeTreeMenu(_uiField.getInstance().getType());
            if (menu != null && menu.hasAccess(_uiField.getParent().getMode(), _uiField.getInstance())
                            && (!((AbstractUIPageObject) _uiField.getParent()).getAccessMap().containsKey(
                                            _uiField.getInstance())
                            || ((AbstractUIPageObject) _uiField.getParent()).getAccessMap().containsKey(
                                            _uiField.getInstance())
                                            && ((AbstractUIPageObject) _uiField.getParent()).getAccessMap().get(
                                                            _uiField.getInstance()))) {
                ret = new MenuContentAjaxLink(_wicketId, Model.of(_uiField));
            }
        }
        return ret;
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (HRefFactory.FACTORY == null) {
            HRefFactory.FACTORY = new HRefFactory();
        }
        return HRefFactory.FACTORY;
    }
}
