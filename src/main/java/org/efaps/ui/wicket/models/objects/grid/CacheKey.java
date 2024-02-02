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
package org.efaps.ui.wicket.models.objects.grid;

/**
 * This enum holds the Values used as part of the key for the UserAttributes
 * or SessionAttribute witch belong to a TableModel.
 *
 * @author The eFaps Team
 */
public enum CacheKey
{
    /** The DB filter. */
    DBFILTER("dbFilter"),

    /**
     * Key for SessionAttribute used for the filter of a table.
     */
    GRIDX("gridx");

    /**
     * Value of the user attribute.
     */
    private final String value;

    /**
     * Constructor setting the instance variable.
     *
     * @param _value Value
     */
    CacheKey(final String _value)
    {
        this.value = _value;
    }

    /**
     * Gets the value of the user attribute.
     *
     * @return the value
     */
    public String getValue()
    {
        return this.value;
    }
}
