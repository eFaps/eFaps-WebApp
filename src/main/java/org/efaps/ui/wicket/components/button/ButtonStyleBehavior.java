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

package org.efaps.ui.wicket.components.button;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class ButtonStyleBehavior
    extends Behavior
{

    /**
     * Reference to the StyleSheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(ButtonStyleBehavior.class,
                                    "ButtonStyleBehavior.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        _response.render(CssHeaderItem.forUrl(ButtonStyleBehavior.CSS.getStaticContentUrl()));
    }

    @Override
    public void onComponentTag(final Component _component,
                               final ComponentTag _tag)
    {
        super.onComponentTag(_component, _tag);
        if (_component instanceof WebMarkupContainer) {
            _tag.put("class", "eFapsButton");
        }
        if (_component instanceof Label) {
            _tag.put("class", "eFapsButtonLabel");
        }
    }
}
