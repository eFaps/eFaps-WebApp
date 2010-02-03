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
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.ui.wicket.util.EFapsKey;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AutoCompleteRenderer.java 3447 2009-11-29 22:46:39Z tim.moxter
 *          $
 */
public class AutoCompleteRenderer
    extends AbstractAutoCompleteTextRenderer<Map<String, String>>
{

    /**
     * Needed for serialization.
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
        this.autoCompleteField.add(StaticHeaderContributor.forJavaScript(AjaxFieldUpdateBehavior.JS));
    }

    /**
     * @see org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteRenderer#getTextValue(java.lang.Object)
     * @param _map map from the esjp
     * @return value for the field
     */
    @Override
    protected String getTextValue(final Map<String, String> _map)
    {
        return _map.get(EFapsKey.AUTOCOMPLETE_VALUE.getKey());
    }

    /**
     * @see org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer#renderChoice(java.lang.Object,
     *      org.apache.wicket.Response, java.lang.String)
     * @param _map map from the esjp
     * @param _response Response
     * @param _criteria criteria
     */
    @Override
    protected void renderChoice(final Map<String, String> _map,
                                final Response _response,
                                final String _criteria)
    {
        final String choice = _map.get(EFapsKey.AUTOCOMPLETE_CHOICE.getKey()) != null
                        ? _map.get(EFapsKey.AUTOCOMPLETE_CHOICE.getKey())
                        : _map.get(EFapsKey.AUTOCOMPLETE_VALUE.getKey());
        _response.write(choice);
    }

    /**
     * @param _map the autocomplete item to get a custom javascript expression
     *            for
     * @return javascript to execute on selection or <code>null</code> if
     *         default behavior is intented
     */
    @Override
    protected CharSequence getOnSelectJavascriptExpression(final Map<String, String> _map)
    {
        final String key = _map.get(EFapsKey.AUTOCOMPLETE_KEY.getKey()) != null
                        ? _map.get(EFapsKey.AUTOCOMPLETE_KEY.getKey())
                        : _map.get(EFapsKey.AUTOCOMPLETE_VALUE.getKey());
        final StringBuilder js = new StringBuilder();
        js.append("wicketGet('").append(this.autoCompleteField.getMarkupId()).append("_hidden').value ='")
                        .append(key).append("';");
        for (final String keyString : _map.keySet()) {
            // if the map contains a key that is not defined in this class it is
            // assumed to be the name of a field
            if (!(EFapsKey.AUTOCOMPLETE_KEY.getKey().equals(keyString)
                            || EFapsKey.AUTOCOMPLETE_VALUE.getKey().equals(keyString)
                            || EFapsKey.AUTOCOMPLETE_CHOICE.getKey().equals(keyString)
                            || EFapsKey.AUTOCOMPLETE_JAVASCRIPT.getKey().equals(keyString))) {
                js.append("eFapsSetFieldValue('").append(this.autoCompleteField.getMarkupId()).append("','")
                    .append(keyString).append("',").append(_map.get(keyString).contains("Array(") ? "" : "'")
                    .append(_map.get(keyString)).append(_map.get(keyString).contains("Array(") ? "" : "'").append(");");
            }
        }
        if (_map.containsKey(EFapsKey.AUTOCOMPLETE_JAVASCRIPT.getKey())) {
            js.append(_map.get(EFapsKey.AUTOCOMPLETE_JAVASCRIPT.getKey()));
        }
        js.append("input");
        return js.toString();
    }
}
