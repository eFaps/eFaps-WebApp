/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.util;

/**
 * Enum is used to store all kind of keys used to store values passed from an
 * esjp to a component or site in one place.
 *
 * @author The eFaps Team
 */
public enum EFapsKey {

    /** Key used to deactivate the escape functionalities in AutoComplete. */
    AUTOCOMPLETE_DEACTIVATEESCAPE("eFapsAutoCompleteDeactivateEscape4HTMLandJS"),
    /** Key used for the value in the map. */
    AUTOCOMPLETE_VALUE("eFapsAutoCompleteVALUE"),
    /** Key used for the value in the map. */
    AUTOCOMPLETE_CHOICE("eFapsAutoCompleteCHOICE"),
    /** Key used for the key in the map. */
    AUTOCOMPLETE_KEY("eFapsAutoCompleteKEY"),
    /** Key used for the javascript in the map. */
    AUTOCOMPLETE_JAVASCRIPT("eFapsAutoCompleteJS"),
    /** Used in conjunction with the fieldName as postfix to create a name for the map of an chart image.*/
    CHARTMAPPOSTFIX("eFapsChartMap"),
    /** Key used to deactivate the escape functionalities in Picker. */
    PICKER_DEACTIVATEESCAPE("eFapsPickerDeactivateEscape4HTMLandJS"),
    /** Key used for the value in the map. */
    PICKER_VALUE("eFapsPickerValue4Field"),
    /** Key used for the javascript in the map. */
    PICKER_JAVASCRIPT("eFapsPickerJavaScript"),

    /** Key used for the javascript in the map. */
    FIELDUPDATE_JAVASCRIPT("eFapsFieldUpdateJS"),
    /** Key used to configure the update mechanism for each map. */
    FIELDUPDATE_USEID("eFapsFieldUseId"),
    /** Key used to configure the update mechanism for each map. */
    FIELDUPDATE_USEIDX("eFapsFieldUseIndex"),

    /** Key used as name for the hidden field that contains the Level of a node. */
    STRUCBRWSR_LEVEL("eFapsStructurBrowserNodeLevel"),
    /** Key used as name for the hidden field that contains if the node can contain children. */
    STRUCBRWSR_ALLOWSCHILDS("eFapsStructurBrowserNodeAllowsChilds"),

    /** Key used as name for the hidden field that makes a row unique. */
    TABLEROW_NAME("eFapsTRID"),
    /** key used to store instances in a request. */
    INSTANCE_CACHEKEY("eFapsInstancesInRequest"),

    /** Key used as name for the input that selects a row. */
    SELECTEDROW_NAME("selectedRow"),
    /** Key used as name for the input that marks all selected. */
    SELECTEALL_NAME("selecteAll");
    /**
     * The actual string used as the key.
     */
    private final String key;

    /**
     * @param _key key
     */
    EFapsKey(final String _key)
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
