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


package org.efaps.ui.wicket.components.autocomplete;

import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AutoCompleteFieldBehavior
    extends AutoCompleteBehavior<Map<String, String>>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * AutoCompleteField this behavior belong to.
     */
    private final AutoCompleteField autoCompleteField;

    /**
     * @param _autoCompleteField    AutoCompleteField
     * @param _renderer             IAutoCompleteRenderer
     * @param _settings             AutoCompleteSettings
     */
    public AutoCompleteFieldBehavior(final AutoCompleteField _autoCompleteField,
                                     final IAutoCompleteRenderer<Map<String, String>> _renderer,
                                     final AutoCompleteSettings _settings)
    {
        super(_renderer, _settings);
        this.autoCompleteField = _autoCompleteField;
    }

    /**
     * (non-Javadoc).
     * @see org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior#getChoices(java.lang.String)
     * @param _input Input
     * @return Iterator
     */
    @Override
    protected Iterator<Map<String, String>> getChoices(final String _input)
    {
        return this.autoCompleteField.getChoices(_input);
    }

    /**
     * get the INit script.
     * @return Script
     */
    public String getInitScript()
    {
        final String id = getComponent().getMarkupId();
        final String initJS = String.format("new Wicket.AutoComplete('%s','%s',%s,%s);", id,
                                                  getCallbackUrl(), constructSettingsJS(), "null");
        return initJS;
    }
}
