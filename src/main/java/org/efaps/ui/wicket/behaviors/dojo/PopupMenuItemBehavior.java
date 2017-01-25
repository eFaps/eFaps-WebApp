/*
 * Copyright 2003 - 2016 The eFaps Team
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

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.efaps.ui.wicket.util.DojoClasses;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class PopupMenuItemBehavior
    extends AbstractDojoBehavior
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The tag of the related component must be set, so that a dojo
     * BorderContainer will be rendered.
     *
     * @param _component component this Behavior belongs to
     * @param _tag Tag to write to
     */
    @Override
    public void onComponentTag(final Component _component,
                               final ComponentTag _tag)
    {
        super.onComponentTag(_component, _tag);
        _tag.put("data-dojo-type", "dijit/PopupMenuItem");
    }

    /**
     * Render the links for the head.
     *
     * @param _component component the header will be rendered for
     * @param _response resonse to add
     */
    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        _response.render(RequireHeaderItem.forClasses(DojoClasses.PopupMenuItem, DojoClasses.parser));
    }
}
