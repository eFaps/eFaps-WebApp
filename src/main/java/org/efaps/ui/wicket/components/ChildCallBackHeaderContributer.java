/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.ui.wicket.components;

import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.util.string.JavascriptUtils;

/**
 * Header Contributer that adds a javascript to the header needed
 * to executed a method for a child in the parent frame.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ChildCallBackHeaderContributer
    extends StringHeaderContributor
{
    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Constant javaScript.
     */
    private static final String JAVASCRIPT =
                    JavascriptUtils.SCRIPT_OPEN_TAG
                        + "function childCallBack(_call){\n"
                        + "  _call = _call.replace(/^javascript:/, \"\");\n"
                        + "  eval(_call);\n"
                        + "}\n"
                        + JavascriptUtils.SCRIPT_CLOSE_TAG;

    /**
     * Instantiates a new child call back header contributer.
     */
    public ChildCallBackHeaderContributer()
    {
        super(ChildCallBackHeaderContributer.JAVASCRIPT);
    }
}
