/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.ui.wicket.behaviors.dojo;

import java.util.Collections;
import java.util.EnumSet;

import org.apache.wicket.Application;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IReferenceHeaderItem;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.JavaScriptLibrarySettings;
import org.efaps.admin.dbproperty.DBProperties;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class AutoCompleteHeaderItem
    extends HeaderItem
    implements IReferenceHeaderItem
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Javascript for this HeaderItem.
     */
    private final CharSequence javaScript;

    /**
     *  If true an 'AutoComplete' will be rendered, else an 'AutoSuggestion'.
     */
    private final EnumSet<AutoCompleteBehavior.Type> types;

    /**
     * @param _javaScript Javascript for the header to add
     * @param _types      enumset parameter for type setting
     */
    public AutoCompleteHeaderItem(final CharSequence _javaScript,
                                  final EnumSet<AutoCompleteBehavior.Type> _types)
    {
        this.javaScript = _javaScript;
        this.types = _types;
    }

    @Override
    public Iterable<?> getRenderTokens()
    {
        return Collections.singletonList("javascript-autocomplete-" + getJavaScript());
    }

    @Override
    public String toString()
    {
        return "AutoCompleteHeaderItem(" + getJavaScript() + ")";
    }

    @Override
    public ResourceReference getReference()
    {
        final JavaScriptLibrarySettings ajaxSettings = Application.get().getJavaScriptLibrarySettings();
        return ajaxSettings.getWicketAjaxReference();
    }

    @Override
    public void render(final Response _response)
    {
        final CharSequence js = AutoCompleteHeaderItem.writeJavaScript(getJavaScript(), this.types, true);
        JavaScriptUtils.writeJavaScript(_response, js);
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof AutoCompleteHeaderItem) {
            ret =  ((AutoCompleteHeaderItem) _obj).getJavaScript().equals(getJavaScript());
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return getJavaScript().hashCode();
    }

    /**
     * @return the script that gets executed on the DOM ready event.
     */
    public CharSequence getJavaScript()
    {
        return this.javaScript;
    }

    /**
     * Getter method for the instance variable {@link #types}.
     *
     * @return value of instance variable {@link #types}
     */
    public EnumSet<AutoCompleteBehavior.Type> getTypes()
    {
        return this.types;
    }

    /**
     * @param _javaScript   Javascript for the header to add
     * @param _types        enumset parameter for type setting
     * @return new AutoCompleteHeaderItem
     */
    public static AutoCompleteHeaderItem forScript(final CharSequence _javaScript,
                                                   final EnumSet<AutoCompleteBehavior.Type> _types)
    {
        return new AutoCompleteHeaderItem(_javaScript, _types);
    }

    /**
     * @param _charSequence     to be wrapped
     * @param _types            enumset parameter for type setting
     * @param _ready            add dojo ready
     * @return CharSequence
     */
    public static CharSequence writeJavaScript(final CharSequence _charSequence,
                                               final EnumSet<AutoCompleteBehavior.Type> _types,
                                               final boolean _ready)
    {
        final StringBuilder js = new StringBuilder();
        if (_ready) {
            js.append("require([\"dojo/ready\"]);").append("dojo.ready(function() {\n");
        }

        js.append("require([\"efaps/AjaxStore\",");

        if (_types.contains(AutoCompleteBehavior.Type.COMPLETE)) {
            js.append("\"efaps/AutoComplete\",");
        }
        if (_types.contains(AutoCompleteBehavior.Type.SUGGESTION)) {
            js.append("\"efaps/AutoSuggestion\", ");
        }
        if (_types.contains(AutoCompleteBehavior.Type.TOKEN)) {
            js.append("\"efaps/AutoTokenInput\", ");
        }

        js.append("\"dojo/on\",\"dojo/domReady!\"],")
            .append(" function(AjaxStore,");

        if (_types.contains(AutoCompleteBehavior.Type.COMPLETE)) {
            js.append(" AutoComplete,");
        }

        if (_types.contains(AutoCompleteBehavior.Type.SUGGESTION)) {
            js.append(" AutoSuggestion,");
        }

        if (_types.contains(AutoCompleteBehavior.Type.TOKEN)) {
            js.append(" AutoTokenInput,");
        }

        js.append(" on){\n")
            .append("var ph=\"")
            .append(DBProperties.getProperty(AutoCompleteBehavior.class.getName() + ".PlaceHolder"))
            .append("\";\n")
            .append("var as= new AjaxStore();\n")
            .append(_charSequence)
            .append("});\n");

        if (_ready) {
            js.append("});\n");
        }
        return js;
    }
}
