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

import org.efaps.api.ui.IOption;

/**
 *
 * @author The eFaps Team
 */
public abstract class AbstractOption
    implements Serializable, IOption
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The label. */
    private String label;

    /** The value. */
    private String value;

    /** The selected. */
    private boolean selected = false;

    /**
     * Instantiates a new abstract option.
     *
     * @param _value the value
     * @param _label the label
     */
    public AbstractOption(final String _value,
                          final String _label)

    {
        this.label = _label;
        this.value = _value;
    }

    @Override
    public String getLabel()
    {
        return this.label;
    }

    public void setLabel(final String _label)
    {
        this.label = _label;
    }

    @Override
    public String getValue()
    {
        return this.value;
    }

    public void setValue(final String _value)
    {
        this.value = _value;
    }

    /**
     * Getter method for the instance variable {@link #selected}.
     *
     * @return value of instance variable {@link #selected}
     */
    @Override
    public boolean isSelected()
    {
        return this.selected;
    }

    /**
     * Setter method for instance variable {@link #selected}.
     *
     * @param _selected value for instance variable {@link #selected}
     */
    public void setSelected(final boolean _selected)
    {
        this.selected = _selected;
    }
}
