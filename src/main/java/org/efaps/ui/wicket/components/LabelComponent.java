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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components;

import java.util.List;
import java.util.Map.Entry;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.value.IValueMap;

import org.efaps.admin.datamodel.ui.UIInterface;

/**
 * This class is a Label for webform and webtable. It is needed because the
 * standard Label from wicket replaces tags from a String. So that the String
 * will be shown and not be interpreted as HtmlTag.
 *
 * @author jmox
 * @version $Id:LabelComponent.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class LabelComponent extends WebComponent
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Standard constructor.
     *
     * @param _wicketId wicketId of this Component
     * @param _model model of this Component
     */
    public LabelComponent(final String _wicketId, final IModel<String> _model)
    {
        super(_wicketId, _model);
        setRenderBodyOnly(true);
    }

    /**
     * Constructor that makes a model from the value to use the standard
     * constructor.
     *
     * @param _wicketId wicketId of this Component
     * @param _value value of this Component
     */
    public LabelComponent(final String _wicketId, final String _value)
    {
        this(_wicketId, new Model<String>(_value));
    }

    /**
     * Must be overwritten so that now replacing of html tags is done.
     *
     * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
     *      org.apache.wicket.markup.ComponentTag)
     *
     * @param _markupStream MarkupStream
     * @param _openTag Tag
     */
    @Override
    protected void onComponentTagBody(final MarkupStream _markupStream, final ComponentTag _openTag)
    {
        String value = (String) super.getDefaultModelObject();
        // if the value contains the EFAPSTMPTAG all tags from this component
        // will be moved to the subcomponent
        if (value != null && value.contains(UIInterface.EFAPSTMPTAG)) {
            final StringBuilder tagBldr = new StringBuilder();
            final List<IBehavior> behaviors = getBehaviors();
            final ComponentTag tmpTag = new ComponentTag(_openTag);
            for (final IBehavior behavior : behaviors) {
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


}
