/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.ui.wicket.util;

/**
 * Enum is used to store all kind of keys used to store values passed from an
 * esjp to a component or site in one place.
 *
 * @author The eFaps Team
 * @version $Id: $
 */
public enum EFapsKey {

    /** Key used for the value in the map. */
    AUTOCOMPLETE_VALUE("eFapsAutoCompleteVALUE"),
        /** Key used for the value in the map. */
    AUTOCOMPLETE_CHOICE("eFapsAutoCompleteCHOICE"),
        /** Key used for the key in the map. */
    AUTOCOMPLETE_KEY("eFapsAutoCompleteKEY"),
        /** Key used for the javascript in the map. */
    AUTOCOMPLETE_JAVASCRIPT("eFapsAutoCompleteJS"),

    /** Key used for the javascript in the map. */
    FIELDUPDATE_JAVASCRIPT("eFapsFieldUpdateJS"),
    /** Key used as name for the hidden field that makes a row unique. */
    TABLEROW_NAME("eFapsTableRowID"),
    /** key used to store instances in a request. */
    INSTANCE_CACHEKEY("eFapsInstancesInRequest");
    /**
     * The actual string used as the key.
     */
    private final String key;

    /**
     * @param _key key
     */
    private EFapsKey(final String _key)
    {
        this.key = _key;
    }

    /**
     * Getter method for the instance variable {@link #key}.
     *
     * @return value of instance variable {@link #key}
     */
    public String getKey()
    {
        return this.key;
    }
}
