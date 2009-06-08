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

package org.efaps.ui.wicket.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SetSelectedRowBehavior extends AbstractBehavior
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see org.apache.wicket.behavior.AbstractBehavior#onComponentTag(org.apache.wicket.Component, org.apache.wicket.markup.ComponentTag)
     * @param _component
     * @param _tag
     */
    @Override
    public void onComponentTag(final Component _component, final ComponentTag _tag)
    {
        super.onComponentTag(_component, _tag);
        _tag.put("onfocus", getJavaScript());
    }

    private String getJavaScript() {
        final StringBuilder ret = new StringBuilder();
        ret.append(" var p=0;")
            .append(" var c=document.getElementsByName(this.name);")
            .append(" for (var i = 0; i < c.length; i++) {")
            .append(" if (c[i]==this) {")
            .append(" p = i; break;}}")
            .append(" var s=document.getElementsByName('eFapsRowSelected');")
            .append(" for (var i = 0; i < c.length; i++) {")
            .append(" s[i].value='';")
            .append(" }")
            .append(" s[p].value='selected';");
        return ret.toString();
    }

}
