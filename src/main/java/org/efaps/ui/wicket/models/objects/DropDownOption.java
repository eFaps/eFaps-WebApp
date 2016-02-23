/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.ui.wicket.models.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.efaps.api.ui.IOption;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class DropDownOption
    extends AbstractOption
    implements Serializable
{

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new drop down option.
     *
     * @param _value the value
     * @param _label the label
     */
    public DropDownOption(final String _value,
                          final String _label)
    {
        super(_value, _label);
    }

    /**
     * Instantiates a new drop down option.
     *
     * @param _value the value
     * @param _label the label
     * @param _selected the selected
     */
    public DropDownOption(final String _value,
                          final String _label,
                          final boolean _selected)
    {
        super(_value, _label);
        setSelected(_selected);
    }

    /**
     * Gets the choices.
     *
     * @param _object the object
     * @return the choices
     */
    public static List<DropDownOption> getChoices(final Object _object)
    {
        final List<DropDownOption> ret = new ArrayList<>();
        if (_object instanceof List) {
            for (final Object obj : (List<?>) _object) {
                if (obj instanceof IOption) {
                    final IOption option = (IOption) obj;

                    ret.add(new DropDownOption(String.valueOf(option.getValue()), option.getLabel(), option
                                    .isSelected()));
                }
            }
        }
        return ret;
    }
}
