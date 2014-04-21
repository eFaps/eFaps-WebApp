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

package org.efaps.ui.wicket.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SetSelectedRowBehavior
    extends Behavior
{
    /**
     * Name of the hidden input for the field name.
     */
    public static final String INPUT_NAME = "eFapsRSN";

    /**
     * Name of the hidden input for the row number.
     */
    public static final String INPUT_ROW = "eFapsRSR";

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Name of the field this behavior belong to.
     */
    private final String name;

    /**
     * @param _name name
     */
    public SetSelectedRowBehavior(final String _name)
    {
        this.name = _name;
    }

    /**
     * @see org.apache.wicket.behavior.AbstractBehavior#onComponentTag(org.apache.wicket.Component,
     * org.apache.wicket.markup.ComponentTag)
     * @param _component component
     * @param _tag       tag
     */
    @Override
    public void onComponentTag(final Component _component,
                               final ComponentTag _tag)
    {
        super.onComponentTag(_component, _tag);
        _tag.put("onfocus", getJavaScript("this"));
    }

    /**
     * @return get the Javascript
     */
    public String getJavaScript(final String _element)
    {
        final StringBuilder ret = new StringBuilder()
            .append(" var f=").append(_element).append(".form;\n")
            .append(" f.").append(SetSelectedRowBehavior.INPUT_NAME).append(".value='").append(this.name).append("';\n")
            .append(" c=f.elements[").append(_element).append(".name];\n")
            .append(" if(typeof(c.length)=='undefined'){\n")
            .append(" f.").append(SetSelectedRowBehavior.INPUT_ROW).append(".value=0;\n")
            .append(" }else{\n")
            .append(" for (var i = 0; i < c.length; i++) {\n")
            .append(" if (c[i]==").append(_element).append(") {\n")
            .append(" f.").append(SetSelectedRowBehavior.INPUT_ROW).append(".value=i;\n")
            .append(" break;}}}\n");
        return ret.toString();
    }

}
