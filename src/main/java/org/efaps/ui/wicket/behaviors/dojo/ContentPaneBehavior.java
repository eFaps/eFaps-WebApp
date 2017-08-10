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
 * This class turns a Component into a Dojo-ContentPane.
 *
 * @author The eFaps Team
 */
public class ContentPaneBehavior
    extends AbstractDojoBehavior
{

    /**
     * Enum is used when this ContentPaneBehavior is used as a child inside a
     * BorderContainer. The BorderContainer is widget is partitioned into up to
     * five regions: left (or leading), right (or trailing), top, and bottom
     * with a mandatory center to fill in any remaining space. Each edge region
     * may have an optional splitter user interface for manual resizing.
     */
    public enum Region
    {
        /** center region. */
        CENTER("center"),
        /** top region. */
        TOP("top"),
        /** bottom region. */
        BOTTOM("bottom"),
        /** leading region. */
        LEADING("leading"),
        /** trailing region. */
        TRAILING("trailing"),
        /** left region. */
        LEFT("left"),
        /** right region. */
        RIGHT("right");

        /**
         * Stores the key of the Region.
         */
        private final String key;

        /**
         * Private Constructor.
         *
         * @param _key Key
         */
        Region(final String _key)
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
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Region of this ContenPane.
     */
    private final Region region;

    /**
     * Width of this ContenPane.
     */
    private String width;

    /**
     * Height of this ContenPane.
     */
    private final String height;

    /**
     * Sould a splitter be added.
     */
    private final boolean splitter;

    /** The splitter state. */
    private final String splitterState;

    /** The js executeable. */
    private boolean jsExecuteable;

    /** The layout container. */
    private boolean layoutContainer;

    /**
     * Constructor.
     *
     * @param _region region of this ContentPaneBehavior
     * @param _splitter should a splitter be rendered
     *
     */
    public ContentPaneBehavior(final Region _region,
                               final boolean _splitter)
    {
        this(_region, _splitter, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param _region region of this ContentPaneBehavior
     * @param _splitter should a splitter be rendered
     * @param _width width of this ContentPaneBehavior
     * @param _height height of this ContentPaneBehavior
     * @param _splitterState the splitter state
     */
    public ContentPaneBehavior(final Region _region,
                               final boolean _splitter,
                               final String _width,
                               final String _height,
                               final String _splitterState)
    {
        super();
        this.region = _region;
        this.width = _width;
        this.height = _height;
        this.splitter = _splitter;
        this.splitterState = _splitterState;
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
        if (isLayoutContainer()) {
            _tag.put("data-dojo-type", "dijit/layout/LayoutContainer");
        } else {
            _tag.put("data-dojo-type", isJsExecuteable() ? "dojox/layout/ContentPane" : "dijit/layout/ContentPane");
        }
        if (this.region != null) {
            _tag.append("data-dojo-props", "region: '" + this.region.getKey() + "'", ",");
        }

        if (this.splitter) {
            _tag.append("data-dojo-props", "splitter: true", ",");
        }
        if (this.splitterState != null) {
            _tag.append("data-dojo-props", "toggleSplitterState: \"" + this.splitterState + "\"", ",");
        }

        if (this.width != null) {
            _tag.append("style", "width: " + this.width, ";");
        }
        if (this.height != null) {
            _tag.append("style", "height: " + this.height, ";");
        }
    }

    /**
     * Getter method for instance variable {@link #width}.
     *
     * @return value of instance variable {@link #width}
     */
    public String getWidth()
    {
        return this.width;
    }

    /**
     * Setter method for instance variable {@link #width}.
     *
     * @param _width value for instance variable {@link #width}
     * @return the content pane behavior
     */
    public ContentPaneBehavior setWidth(final String _width)
    {
        this.width = _width;
        return this;
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
        _response.render(RequireHeaderItem.forClasses(
                        isJsExecuteable() ? DojoClasses.ContentPaneX : DojoClasses.ContentPane,
                                        DojoClasses.LayoutContainer, DojoClasses.parser));
    }

    /**
     * Getter method for the instance variable {@link #jsExecuteable}.
     *
     * @return value of instance variable {@link #jsExecuteable}
     */
    public boolean isJsExecuteable()
    {
        return this.jsExecuteable;
    }

    /**
     * Setter method for instance variable {@link #jsExecuteable}.
     *
     * @param _jsExecuteable value for instance variable {@link #jsExecuteable}
     * @return the content pane behavior
     */
    public ContentPaneBehavior setJsExecuteable(final boolean _jsExecuteable)
    {
        this.jsExecuteable = _jsExecuteable;
        return this;
    }


    /**
     * Getter method for the instance variable {@link #layoutContainer}.
     *
     * @return value of instance variable {@link #layoutContainer}
     */
    public boolean isLayoutContainer()
    {
        return this.layoutContainer;
    }

    /**
     * Setter method for instance variable {@link #layoutContainer}.
     *
     * @param _layoutContainer value for instance variable {@link #layoutContainer}
     */
    public ContentPaneBehavior setLayoutContainer(final boolean _layoutContainer)
    {
        this.layoutContainer = _layoutContainer;
        return this;
    }
}
