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

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.BooleanUI;
import org.efaps.ui.wicket.components.values.BooleanField;
import org.efaps.ui.wicket.components.values.CheckBoxField;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.cell.FieldConfiguration.UIType;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public class BooleanUIFactory
    extends AbstractUIFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static BooleanUIFactory FACTORY;

    /**
     * Singelton.
     */
    private BooleanUIFactory()
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
            final FieldConfiguration config = _abstractUIField.getFieldConfiguration();
            final UIType uiType = config.getUIType();
            if (uiType.equals(UIType.CHECKBOX)) {
                ret = new CheckBoxField(_wicketId, Model.of(_abstractUIField), null, config);
            } else {
                final IModel<Map<Object, Object>> model = Model.ofMap((Map<Object, Object>) _abstractUIField.getValue()
                                .getEditValue(_abstractUIField.getParent().getMode()));
                final Serializable value = _abstractUIField.getValue().getDbValue();
                ret = new BooleanField(_wicketId, value, model,
                                _abstractUIField.getFieldConfiguration(),
                                _abstractUIField.getFieldConfiguration().getLabel(_abstractUIField));
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean applies(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return _abstractUIField.getValue().getUIProvider() instanceof BooleanUI;
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
            for (final Entry<Object, Object> entry : map.entrySet()) {
                if (entry.getValue().equals(_abstractUIField.getValue().getDbValue())) {
                    strValue = (String) entry.getKey();
                    break;
                }
            }
        }
        return strValue;
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (BooleanUIFactory.FACTORY == null) {
            BooleanUIFactory.FACTORY = new BooleanUIFactory();
        }
        return BooleanUIFactory.FACTORY;
    }
}
