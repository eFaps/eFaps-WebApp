/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.ui.wicket.behaviors.dojo;

import java.util.Collections;

import org.apache.wicket.Application;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.settings.IJavaScriptLibrarySettings;
import org.efaps.admin.dbproperty.DBProperties;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AutoCompleteHeaderItem
    extends HeaderItem
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
     * @param _javaScript Javascript for the header to add
     */
    public AutoCompleteHeaderItem(final CharSequence _javaScript)
    {
        this.javaScript = _javaScript;
    }

    /**
     * @param _javaScript Javascript for the header to add
     * @return new OnDojoReadyHeaderItem
     */
    public static AutoCompleteHeaderItem forScript(final CharSequence _javaScript)
    {
        return new AutoCompleteHeaderItem(_javaScript);
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
    public Iterable<? extends HeaderItem> getDependencies()
    {
        final IJavaScriptLibrarySettings ajaxSettings = Application.get().getJavaScriptLibrarySettings();
        final ResourceReference wicketEventReference = ajaxSettings.getWicketEventReference();
        return Collections.singletonList(JavaScriptHeaderItem.forReference(wicketEventReference));
    }

    @Override
    public void render(final Response _response)
    {
        final StringBuilder js = new StringBuilder().append("require([\"dojo/ready\"]);")
                        .append("dojo.ready(function() {\n")
                        .append("require([\"efaps/AjaxStore\",\"efaps/AutoComplete\",\"dojo/on\",\"dojo/domReady!\"],")
                        .append(" function(AjaxStore, AutoComplete, on){\n")
                        .append("var ph=\"")
                        .append(DBProperties.getProperty(AutoCompleteBehavior.class.getName() + ".PlaceHolder"))
                        .append("\";\n")
                        .append("var as= new AjaxStore();\n")
                        .append(getJavaScript())
                        .append("});\n")
                        .append("});\n");
        JavaScriptUtils.writeJavaScript(_response, js);
    }

    /**
     * @return the script that gets executed on the DOM ready event.
     */
    public CharSequence getJavaScript()
    {
        return this.javaScript;
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
}
