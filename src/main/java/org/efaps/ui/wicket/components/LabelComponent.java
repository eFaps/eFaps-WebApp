/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.ui.wicket.components;

import java.util.List;
import java.util.Map.Entry;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.value.IValueMap;
import org.efaps.admin.datamodel.ui.UIInterface;

/**
 * This class is a Label for webform and webtable. It is needed to replace
 * the eFapsTempTag and add the events.
 *
 * @author The eFaps Team
 * @version $Id:LabelComponent.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class LabelComponent
    extends WebComponent
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor that makes a model from the value to use the standard
     * constructor.
     *
     * @param _wicketId wicketId of this Component
     * @param _value value of this Component
     */
    public LabelComponent(final String _wicketId,
                          final String _value)
    {
        this(_wicketId, new Model<String>(_value));
    }

    /**
     * Standard constructor.
     *
     * @param _wicketId wicketId of this Component
     * @param _model model of this Component
     */
    public LabelComponent(final String _wicketId,
                          final IModel<?> _model)
    {
        super(_wicketId, _model);
        setRenderBodyOnly(true);
    }

    /**
     * Must be overwritten so that no replacing of html tags is done.
     *
     * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
     *      org.apache.wicket.markup.ComponentTag)
     *
     * @param _markupStream MarkupStream
     * @param _openTag Tag
     */
    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                      final ComponentTag _openTag)
    {
        setEscapeModelStrings(false);
        String value = getDefaultModelObjectAsString(getDefaultModelObject());
        // if the value contains the EFAPSTMPTAG all tags from this component
        // will be moved to the subcomponent
        if (value != null && value.contains(UIInterface.EFAPSTMPTAG)) {
            final StringBuilder tagBldr = new StringBuilder();
            final List<? extends Behavior> behaviors = getBehaviors();
            final ComponentTag tmpTag = new ComponentTag(_openTag);
            for (final Behavior behavior : behaviors) {
                behavior.onComponentTag(this, tmpTag);
            }
            final IValueMap map = tmpTag.getAttributes();
            for (final Entry<String, Object> entry : map.entrySet()) {
                final String key = entry.getKey();
                if (!"wicket:id".equals(key)) {
                    tagBldr.append(" ").append(key).append("=\"").append(entry.getValue()).append("\" ");
                }
            }
            // if no id is given add the id here
            if (!map.containsKey("id")) {
                tagBldr.append(" id=\"").append(getMarkupId()).append("\" ");
            }
            value = value.replace(UIInterface.EFAPSTMPTAG, tagBldr);
        }
        super.replaceComponentTagBody(_markupStream, _openTag, value);
    }

    /**
     * Method is overwritten to prevent the annoing warnings from Component to come up.
     * The warning come up due to the reason that this component moves the tags from parent
     * element to its child.
     */
    @Override
    protected void onRender()
    {
        final IMarkupFragment markup = getMarkup();
        if (markup == null) {
            throw new MarkupException("Markup not found. Component: " + toString());
        }

        final MarkupStream markupStream = new MarkupStream(markup);

        // Get mutable copy of next tag
        final ComponentTag openTag = markupStream.getTag();
        final ComponentTag tag = openTag.mutable();

        // Call any tag handler
        onComponentTag(tag);

        // If we're an openclose tag
        if (!tag.isOpenClose() && !tag.isOpen()) {
            // We were something other than <tag> or <tag/>
            markupStream.throwMarkupException("Method renderComponent called on bad markup element: " + tag);
        }

        if (tag.isOpenClose() && openTag.isOpen()) {
            markupStream.throwMarkupException("You can not modify a open tag to open-close: " + tag);
        }

        markupStream.next();

        getMarkupSourcingStrategy().onComponentTagBody(this, markupStream, tag);
    }
}
