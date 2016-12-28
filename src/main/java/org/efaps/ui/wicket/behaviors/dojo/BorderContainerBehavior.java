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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.efaps.ui.wicket.util.DojoClasses;

/**
 * Class renders a dojo border. It can be used to render a slipt between the
 * children of this container. BorderContainer operates in a choice of two
 * layout modes: <br>
 * the design attribute may be set to "headline" (by default) or "sidebar". With
 * the "headline" layout, the top and bottom sections extend the entire width of
 * the box and the remaining regions are placed in the middle. With the
 * "sidebar" layout, the side panels take priority, extending the full height of
 * the box.
 *
 * @author The eFaps Team
 */
public class BorderContainerBehavior
    extends AbstractDojoBehavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the stylesheet.
     */
    public static final ResourceReference CSS_SPLITTER = new CssResourceReference(BorderContainerBehavior.class,
                    "dojox/layout/resources/ToggleSplitter.css");


    /**
     * Enum for the different Designs of a dojo BorderContainer.
     */
    public enum Design
    {
        /** "headline" design. */
        HEADLINE("headline"),
        /** "sidebar" design. */
        SIDEBAR("sidebar");

        /**
         * Stores the key of the Design.
         */
        private final String key;

        /**
         * Private Constructor.
         *
         * @param _key Key
         */
        private Design(final String _key)
        {
            this.key = _key;
        }

        /**
         * Getter method for instance variable {@link #key}.
         *
         * @return value of instance variable {@link #key}
         */
        public String getKey()
        {
            return this.key;
        }
    }

    /**
     * Stores the design of this BorderBehavior.
     */
    private final Design design;

    /**
     * Will the Container use a toggle splitter.
     */
    private final boolean toggleSplitter;

    /**
     * Constructor.
     *
     * @param _design           Design for this BorderBehavior.
     * @param _toggleSplitter   Will the Container use a toggle splitter.
     */
    public BorderContainerBehavior(final Design _design,
                                   final boolean _toggleSplitter)
    {
        this.design = _design;
        this.toggleSplitter = _toggleSplitter;
    }

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
        _tag.put("data-dojo-type", "dijit/layout/BorderContainer");
        _tag.append("data-dojo-props", "design: '" + this.design.key + "'", ",");
        _tag.append("class", "tundra eFapsBorderContainer", " ");
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
        new StringBuilder()
            .append("require([\"dijit/layout/BorderContainer\", \"dojo/parser\"");
        if (this.toggleSplitter) {
            _response.render(RequireHeaderItem.forClasses(DojoClasses.ToggleSplitter));
            _response.render(CssHeaderItem.forReference(BorderContainerBehavior.CSS_SPLITTER));
        }
        _response.render(RequireHeaderItem.forClasses(DojoClasses.BorderContainer, DojoClasses.parser));
    }
}
