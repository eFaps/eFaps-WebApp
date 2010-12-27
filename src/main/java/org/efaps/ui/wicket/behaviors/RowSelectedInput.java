/*
 * Copyright 2003 - 2010 The eFaps Team
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


package org.efaps.ui.wicket.behaviors;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;


/**
 * Class renders two hidden inputs used to commit the actual selected
 * row and column.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class RowSelectedInput
    extends WebComponent
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id for this component
     */
    public RowSelectedInput(final String _wicketId)
    {
        super(_wicketId);
    }

    /**
     * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
     * org.apache.wicket.markup.ComponentTag)
     * @param _markupStream MarkupStream
     * @param _tag          ComponentTag
     */
    @Override
    protected void onComponentTagBody(final MarkupStream _markupStream, final ComponentTag _tag)
    {
        final StringBuilder html = new StringBuilder();
        html.append("<input type=\"hidden\" name=\"").append(SetSelectedRowBehavior.INPUT_NAME).append("\"/>")
            .append("<input type=\"hidden\" name=\"").append(SetSelectedRowBehavior.INPUT_ROW).append("\"/>");
        replaceComponentTagBody(_markupStream, _tag, html);
    }

}
