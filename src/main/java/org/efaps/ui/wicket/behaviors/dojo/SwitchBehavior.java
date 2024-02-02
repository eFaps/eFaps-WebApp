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

    /** The left label. */
    private String leftLabel;

    /** The right label. */
    private String rightLabel;

    /**
     * Gets the left label.
     *
     * @return the left label
     */
    public String getLeftLabel()
    {
        return this.leftLabel;
    }

    /**
     * Sets the left label.
     *
     * @param _leftLabel the left label
     * @return the switch behavior
     */
    public SwitchBehavior setLeftLabel(final String _leftLabel)
    {
        this.leftLabel = _leftLabel;
        return this;
    }

    /**
     * Gets the right label.
     *
     * @return the right label
     */
    public String getRightLabel()
    {
        return this.rightLabel;
    }

    /**
     * Sets the right label.
     *
     * @param _rightLabel the right label
     * @return the switch behavior
     */
    public SwitchBehavior setRightLabel(final String _rightLabel)
    {
        this.rightLabel = _rightLabel;
        return this;
    }

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
        if (getLeftLabel() != null) {
            _tag.put("leftLabel", getLeftLabel());
        }
        if (getRightLabel() != null) {
            _tag.put("rightLabel", getRightLabel());
        }
    }
}
