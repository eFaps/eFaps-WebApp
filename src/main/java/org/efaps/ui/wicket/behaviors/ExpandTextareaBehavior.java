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
public class ExpandTextareaBehavior
    extends Behavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see org.apache.wicket.behavior.AbstractBehavior#onComponentTag(org.apache.wicket.Component,
     *      org.apache.wicket.markup.ComponentTag)
     * @param _component component
     * @param _tag tag
     */
    @Override
    public void onComponentTag(final Component _component,
                               final ComponentTag _tag)
    {
        super.onComponentTag(_component, _tag);
        if (_tag.getAttribute("onfocus") == null) {
            _tag.put("onfocus", "this.style.position='absolute'");
        } else {
            _tag.put("onfocus", _tag.getAttribute("onfocus") + ";this.style.position='absolute';");
        }
        if (_tag.getAttribute("onblur") == null) {
            _tag.put("onblur", "this.style.position=''");
        } else {
            _tag.put("onblur", _tag.getAttribute("onfocus") + ";this.style.position=''");
        }
    }
}
