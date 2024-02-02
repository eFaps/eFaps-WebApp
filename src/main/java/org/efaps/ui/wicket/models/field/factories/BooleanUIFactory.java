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

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.BooleanUI;
import org.efaps.api.ui.UIType;
import org.efaps.ui.wicket.components.values.BooleanField;
import org.efaps.ui.wicket.components.values.CheckBoxField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.field.set.UIFieldSetValue;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
// CHECKSTYLE:OFF
public final class BooleanUIFactory
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
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            final FieldConfiguration config = _uiField.getFieldConfiguration();
            final UIType uiType = config.getUIType();
            if (uiType.equals(UIType.CHECKBOX)) {
                ret = new CheckBoxField(_wicketId, Model.of(_uiField), null, config);
            } else {
                final IModel<Map<Object, Object>> model = Model.ofMap((Map<Object, Object>) _uiField.getValue()
                                .getEditValue(_uiField.getParent().getMode()));
                final Serializable value = _uiField.getValue().getDbValue();
                ret = new BooleanField(_wicketId, value, model, _uiField.getFieldConfiguration(), _uiField.getLabel(),
                                _uiField instanceof UIFieldSetValue);
            }
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
        return _uiField.getValue().getUIProvider() instanceof BooleanUI;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected String getReadOnlyValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        String strValue = "";
        if (_uiField.getValue().getDbValue() != null) {
            final Map<Object, Object> map = (Map<Object, Object>) _uiField.getValue()
                            .getReadOnlyValue(_uiField.getParent().getMode());
            for (final Entry<Object, Object> entry : map.entrySet()) {
                if (entry.getValue().equals(_uiField.getValue().getDbValue())) {
                    strValue = (String) entry.getKey();
                    break;
                }
            }
        }
        return strValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return BooleanUIFactory.class.getName();
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

    @Override
    public Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return BooleanUtils.toBooleanDefaultIfNull((Boolean) _uiField.getValue().getDbValue(), false);
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
