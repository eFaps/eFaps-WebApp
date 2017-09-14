/*
 * Copyright 2003 - 2017 The eFaps Team
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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * The Class SwitchBehavior.
 */
public class SwitchBehavior
    extends AbstractDojoBehavior
{

    /**
     * Reference to the stylesheet.
     */
    public static final ResourceReference CSS = new CssResourceReference(AbstractDojoBehavior.class,
                    "dojox/mobile/themes/ios7/ios7.css");

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The input name. */
    private String inputName;

    /** The on. */
    private boolean on;

    /**
     * Checks if is on.
     *
     * @return the on
     */
    public boolean isOn()
    {
        return this.on;
    }

    /**
     * Sets the on.
     *
     * @param _on the new on
     * @return the switch behavior
     */
    public SwitchBehavior setOn(final boolean _on)
    {
        this.on = _on;
        return this;
    }

    /**
     * Gets the input name.
     *
     * @return the input name
     */
    public String getInputName()
    {
        return this.inputName;
    }

    /**
     * Sets the input name.
     *
     * @param _inputName the new input name
     * @return the switch behavior
     */
    public SwitchBehavior setInputName(final String _inputName)
    {
        this.inputName = _inputName;
        return this;
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        _response.render(CssHeaderItem.forReference(CSS));
    }

    @Override
    public void onComponentTag(final Component _component,
                               final ComponentTag _tag)
    {
        super.onComponentTag(_component, _tag);
        _tag.put("data-dojo-type", "dojox/mobile/Switch");
        _tag.put("name", getInputName());
        _tag.put("value", isOn() ? "on" : "off");
    }
}
