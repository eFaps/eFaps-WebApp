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
     * @param _element elemtn the script is added to
     * @return get the Javascript
     */
    public String getJavaScript(final String _element)
    {
        final StringBuilder ret = new StringBuilder()
            .append(" var f=").append(_element).append(".form;\n")
            .append(" f.").append(SetSelectedRowBehavior.INPUT_NAME).append(".value='").append(this.name).append("';")
            .append(" var c=f.elements[").append(_element).append(".name];")
            .append(" if(typeof(c.length)=='undefined'){")
            .append(" f.").append(SetSelectedRowBehavior.INPUT_ROW).append(".value=0;")
            .append(" }else{")
            .append(" for (var i = 0; i < c.length; i++) {")
            .append(" if (c[i]==").append(_element).append(") {")
            .append(" f.").append(SetSelectedRowBehavior.INPUT_ROW).append(".value=i;")
            .append(" break;}}}\n");
        return ret.toString();
    }

}
