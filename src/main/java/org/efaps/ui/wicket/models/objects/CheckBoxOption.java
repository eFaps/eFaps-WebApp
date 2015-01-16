/*
 * Copyright 2003 - 20141 The eFaps Team
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

package org.efaps.ui.wicket.models.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IEnum;
import org.efaps.admin.datamodel.ui.BitEnumUI;
import org.efaps.admin.datamodel.ui.BooleanUI;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.ui.IOption;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.util.EnumUtil;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CheckBoxOption
    extends AbstractOption
    implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CheckBoxOption(final String _value,
                          final String _label)
    {
        this(_value, _label, false);
    }

    public CheckBoxOption(final String _value,
                          final String _label,
                          final boolean _selected)
    {
        super(_value, _label);
        setSelected(_selected);
    }

    public CheckBoxOption(final IEnum _enum)
    {
        super(Integer.valueOf(_enum.getInt()).toString(), EnumUtil.getUILabel(_enum));
    }

    /**
     * @param _field field this option belongs to
     * @param _choices choiced to be rendered
     * @return new List of choices
     * @throws EFapsException on error
     */
    public static List<CheckBoxOption> getChoices(final AbstractUIField _field,
                                                  final List<Object> _choices)
        throws EFapsException
    {
        final List<CheckBoxOption> ret = new ArrayList<CheckBoxOption>();
        if (_choices == null) {
            final Attribute attr = _field.getValue().getAttribute();
            final IUIProvider uiProvider;
            if (attr == null) {
                uiProvider = _field.getFieldConfiguration().getField().getUIProvider();
            } else {
                uiProvider = attr.getAttributeType().getUIProvider();
            }
            if (uiProvider instanceof BitEnumUI) {
                try {
                    final Class<?> clazz = Class.forName(attr.getClassName(), false, EFapsClassLoader.getInstance());
                    final Object[] consts = clazz.getEnumConstants();
                    for (final Object obj : consts) {
                        final IEnum enumConst = (IEnum) obj;
                        ret.add(new CheckBoxOption(enumConst));
                    }
                } catch (final ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if (uiProvider instanceof BooleanUI) {
                ret.add(new CheckBoxOption("true", DBProperties.getProperty(_field.getFieldConfiguration().getField()
                                .getLabel())));
            }
        } else {
            for (final Object obj : _choices) {
                if (obj instanceof IEnum) {
                    final IEnum enumConst = (IEnum) obj;
                    ret.add(new CheckBoxOption(enumConst));
                } else if (obj instanceof IOption) {
                    final IOption option = (IOption) obj;
                    ret.add(new CheckBoxOption(String.valueOf(option.getValue()), option.getLabel(), option
                                    .isSelected()));
                }
            }
        }
        return ret;
    }

    /**
     * @param _field field this option belongs to
     * @param _object object used for rendering
     * @return new List of choices
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public static List<CheckBoxOption> getChoices(final AbstractUIField _field,
                                                  final Object _object)
        throws EFapsException
    {
        List<CheckBoxOption> ret = new ArrayList<>();
        if (_object instanceof List) {
            ret = getChoices(_field, (List<Object>) _object);
        }
        return ret;
    }
}
