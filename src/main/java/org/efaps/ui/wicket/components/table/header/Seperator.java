/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

/**
 * This class renders a Span wich can be used to resize columns in a Header. It is placed between the cells in the
 * Header.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Seperator
    extends WebComponent
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Id of the output.
     */
    private final int outputId;

    /**
     * @param _wicketId wicket id
     * @param _outputid output id
     * @param _propId   propId
     */
    public Seperator(final String _wicketId,
                     final int _outputid,
                     final String _propId)
    {
        super(_wicketId);
        this.outputId = _outputid;
        add(AttributeModifier.append("class", "eFapsTableHeaderSeperator"));
        setMarkupId(this.outputId + "eFapsHeaderSeperator");
    }

    /*
     * (non-Javadoc)
     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        _tag.setName("span");
    }
}
