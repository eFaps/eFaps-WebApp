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
package org.efaps.ui.wicket.models.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IEnum;
import org.efaps.admin.datamodel.ui.EnumUI;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.ui.IOption;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.util.EnumUtil;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class RadioOption
    extends AbstractOption
    implements Serializable
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(RadioOption.class);

    /**
     * @param _value
     * @param _label
     */
    public RadioOption(final String _value,
                       final String _label)
    {
        super(_value, _label);

    }

    public RadioOption(final IEnum _enum)
    {
        super(Integer.valueOf(_enum.getInt()).toString(), EnumUtil.getUILabel(_enum));
    }

    public RadioOption(final String _value,
                       final String _label,
                       final boolean _selected)
    {
        super(_value, _label);
        setSelected(_selected);
    }


    /**
     * @param _choices
     */
    public static List<RadioOption> getChoices(final AbstractUIField _field)
        throws EFapsException
    {
        final List<RadioOption> ret = new ArrayList<>();
        final Attribute attr = _field.getValue().getAttribute();
        final IUIProvider uiProvider = attr.getAttributeType().getUIProvider();
        if (uiProvider instanceof EnumUI) {
            try {
                final Class<?> clazz = Class.forName(attr.getClassName(), false, EFapsClassLoader.getInstance());
                final Object[] consts = clazz.getEnumConstants();
                for (final Object obj : consts) {
                    final IEnum enumConst = (IEnum) obj;
                    ret.add(new RadioOption(enumConst));
                }
            } catch (final ClassNotFoundException e) {
                LOG.error("Catched", e);
            }
        }
        return ret;
    }

    public static List<RadioOption> getChoices(final Object _object)
    {
        final List<RadioOption> ret = new ArrayList<>();
        if (_object instanceof List) {
            for (final Object obj : (List<?>) _object) {
                if (obj instanceof IOption) {
                    final IOption option = (IOption) obj;
                    ret.add(new RadioOption(String.valueOf(option.getValue()), option.getLabel(), option
                                    .isSelected()));
                }
            }
        }
        return ret;
    }
}
