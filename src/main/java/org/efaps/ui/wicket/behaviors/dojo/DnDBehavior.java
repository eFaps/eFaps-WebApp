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

package org.efaps.ui.wicket.behaviors.dojo;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

/**
 * This class renders the drag and drop ability from the DojoToolKit to a component.<br>
 * It is used for all tags which can be part of the Dojo-dnd. The handles, items and the Source.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DnDBehavior
    extends AbstractDojoBehavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Enum used to set the Type of Drag and Drop this Behavior should render.
     */
    public enum BehaviorType
    {
        /** Render a item. */
        ITEM,
        /** Render a source. */
        SOURCE;
    }

    /**
     * This instance variable stores what kind should be rendered.
     */
    private final BehaviorType type;

    /**
     * this instance variable stores a javascript which will be
     * executed after the drag and drop. It is only used in
     * case of BehaviorType.SOURCE
     */
    private String appendJavaScript;

    /**
     * Type.
     */
    private CharSequence dndType = "eFapsdnd";

    /**
     * Constructor setting the Type of the DnDBehavior. Instead of
     * using this constructor it can be used on e of the
     * static methods. <li>
     * {@link #getHandleBehavior()}</li> <li>{@link #getItemBehavior()}</li> <li>
     * {@link #getSourceBehavior()}</li>
     *
     * @param _type BehaviorType of this DnDBehavior
     */
    public DnDBehavior(final BehaviorType _type)
    {
        this.type = _type;
    }

    /**
     * Constructor setting the type and dendtype.
     *
     * @param _type BehaviorType of this DnDBehavior
     * @param _dndType dndType
     */
    public DnDBehavior(final BehaviorType _type,
                       final String _dndType)
    {
        this.type = _type;
        this.dndType = _dndType;
    }

    /**
     * The tag of the component must be altered, so that the dojo dnd will be rendered.
     *
     * @see org.apache.wicket.behavior.AbstractBehavior#onComponentTag(org.apache.wicket.Component,
     *      org.apache.wicket.markup.ComponentTag)
     * @param _component Component
     * @param _tag tag to edit
     */
    @Override
    public void onComponentTag(final Component _component,
                               final ComponentTag _tag)
    {
        super.onComponentTag(_component, _tag);

        if (this.type == DnDBehavior.BehaviorType.ITEM) {
            String value = "dojoDndItem ";
            if (_tag.getAttribute("class") != null) {
                value += _tag.getAttribute("class");
            }
            _tag.put("dndType", this.dndType);
            _tag.put("class", value);
        }
    }

    /**
     * Add the javascriupt to the head of the webpage.
     *
     * @see org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior#renderHead(
     * org.apache.wicket.markup.html.IHeaderResponse)
     *
     * @param _component component the header will be rendered for
     * @param _response rseponse
     */
    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        _response.render(JavaScriptHeaderItem.forScript("require([\"dojo/dnd/Source\",\"dojo/parser\"]);",
                        DnDBehavior.class.toString()));
       if (this.type == DnDBehavior.BehaviorType.SOURCE) {
            final StringBuilder js = new StringBuilder()
                .append("require([\"dojo/aspect\",\"dojo/dom\",\"dojo/dnd/Source\"], ")
                .append("function(aspect,dom){\n")
                .append("var nSrc = new dojo.dnd.Source(dom.byId('").append(_component.getMarkupId(true)).append("'),")
                .append("{ accept: ['" + this.dndType + "']});")
                .append(" aspect.after(nSrc,\"onDrop\", function(){\n")
                .append(this.appendJavaScript)
                .append("\n});")
                .append("});");
            _response.render(OnDojoReadyHeaderItem.forScript(js.toString()));
        }
    }

    /**
     * This is the getter method for the instance variable {@link #appendJavaScript}.
     *
     * @return value of instance variable {@link #appendJavaScript}
     */
    public String getAppendJavaScript()
    {
        return this.appendJavaScript;
    }

    /**
     * This is the setter method for the instance variable {@link #appendJavaScript}.
     *
     * @param _appendJavaScript the appendJavaScript to set
     */
    public void setAppendJavaScript(final String _appendJavaScript)
    {
        this.appendJavaScript = _appendJavaScript;
    }

    /**
     * This is the getter method for the instance variable {@link #dndType}.
     *
     * @return value of instance variable {@link #dndType}
     */
    public CharSequence getDndType()
    {
        return this.dndType;
    }

    /**
     * This is the setter method for the instance variable {@link #dndType}.
     *
     * @param _dndType the dndType to set
     */
    public void setDndType(final CharSequence _dndType)
    {
        this.dndType = _dndType;
    }

    /**
     * Static Method to get DnDBehavior with Source behavior.
     *
     * @return DnDBehavior with Source behavior.
     */
    public static DnDBehavior getSourceBehavior()
    {
        return new DnDBehavior(DnDBehavior.BehaviorType.SOURCE);
    }

    /**
     * Static Method to get DnDBehavior with Source behavior.
     *
     * @param _dndType dndtype to set
     * @return DnDBehavior with Source behavior.
     */
    public static DnDBehavior getSourceBehavior(final String _dndType)
    {
        return new DnDBehavior(DnDBehavior.BehaviorType.SOURCE, _dndType);
    }

    /**
     * Static Method to get DnDBehavior with item behavior.
     *
     * @param _dndType dndtype to set
     * @return DnDBehavior with item behavior.
     */
    public static DnDBehavior getItemBehavior(final String _dndType)
    {
        return new DnDBehavior(DnDBehavior.BehaviorType.ITEM, _dndType);
    }

    /**
     * Static Method to get DnDBehavior with item behavior.
     *
     * @return DnDBehavior with item behavior.
     */
    public static DnDBehavior getItemBehavior()
    {
        return new DnDBehavior(DnDBehavior.BehaviorType.ITEM);
    }
}
