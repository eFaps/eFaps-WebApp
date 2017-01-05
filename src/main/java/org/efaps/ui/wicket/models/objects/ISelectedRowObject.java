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

import java.util.Collections;
import java.util.List;

import org.apache.wicket.util.string.StringValue;

/**
 * The Interface ISelectedRowObject.
 *
 * @author The eFaps Team
 */
public interface ISelectedRowObject
{
    /**
     * Checks for selected.
     *
     * @return true, if successful
     */
    default boolean hasSelected()
    {
        return getSelected() != null && !getSelected().isEmpty();
    };

    /**
     * Adds the selected.
     *
     * @param _values the values
     * @return the i selected row object
     */
    default ISelectedRowObject addSelected(final StringValue... _values)
    {
        Collections.addAll(getSelected(), _values);
        return this;
    };

    /**
     * Gets the selected.
     *
     * @return the selected
     */
    List<StringValue> getSelected();
}
