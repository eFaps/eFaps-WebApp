/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.ui.wicket.components.autocomplete;

import java.util.Map;

import org.apache.wicket.Response;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AutoCompleteRenderer extends AbstractAutoCompleteTextRenderer<Map<String, String>>
{
    /**
     * Key used for the value in the map.
     */
    public static String VALUE = "eFapsAutoCompleteVALUE";

    /**
     * Key used for the choice in the map.
     */
    public static String CHOICE = "eFapsAutoCompleteCHOICE";

    /**
     * Key used for the key in the map.
     */
    public static String KEY = "eFapsAutoCompleteKEY";

    /**
     *  Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * AutoCompleteField this renderer is used for.
     */
    private final AutoCompleteField autoCompleteField;

    /**
     * @param _autoCompleteField AutoCompleteField
     */
    public AutoCompleteRenderer(final AutoCompleteField _autoCompleteField)
    {
       this.autoCompleteField = _autoCompleteField;
    }

    /**
     * @see org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteRenderer#getTextValue(java.lang.Object)
     * @param _map  map from the esjp
     * @return value for the field
     */
    @Override
    protected String getTextValue(final Map<String, String> _map)
    {
        return _map.get(AutoCompleteRenderer.VALUE);
    }

    /**
     * @see org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer#renderChoice(java.lang.Object, org.apache.wicket.Response, java.lang.String)
     * @param _map          map from the esjp
     * @param _response     Response
     * @param _criteria     criteria
     */
    @Override
    protected void renderChoice(final Map<String, String> _map, final Response _response, final String _criteria)
    {
        final String choice = _map.get(AutoCompleteRenderer.CHOICE) != null
                                    ? _map.get(AutoCompleteRenderer.CHOICE)
                                    : _map.get(AutoCompleteRenderer.VALUE);
        _response.write(choice);
    }

    /**
     * @param _map  the autocomplete item to get a custom javascript expression for
     * @return javascript to execute on selection or <code>null</code> if default behavior is intented
     */
    @Override
    protected CharSequence getOnSelectJavascriptExpression(final Map<String, String> _map)
    {
        final String key = _map.get(AutoCompleteRenderer.KEY) != null
                                ? _map.get(AutoCompleteRenderer.KEY)
                                : _map.get(AutoCompleteRenderer.VALUE);
        final StringBuilder js = new StringBuilder();
        js.append("wicketGet('").append(this.autoCompleteField.getMarkupId()).append("_hidden').value ='")
            .append(key).append("';");
        boolean isUpdater = false;
        final StringBuilder updater = new StringBuilder();
        for (final String keyString : _map.keySet()) {
            if (!(AutoCompleteRenderer.KEY.equals(keyString) || AutoCompleteRenderer.VALUE.equals(keyString)
                            || AutoCompleteRenderer.CHOICE.equals(keyString))) {
                updater.append("document.getElementsByName('").append(keyString).append("')[pos].value ='")
                    .append(_map.get(keyString)).append("';");
                isUpdater = true;
            }
        }
        if (isUpdater) {
            js.append("var pos = 0; var eFapsFields = document.getElementsByName('")
                .append(this.autoCompleteField.getFieldName()).append("');")
                .append(" for (var i = 0; i < eFapsFields.length; i++) {")
                .append(" if (eFapsFields[i].id=='").append(this.autoCompleteField.getMarkupId()).append("_hidden'){")
            .append(" pos = i; break;}}");
            js.append(updater);
        }
        js.append("input");
        return js.toString();
    }
}
