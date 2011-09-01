/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.ui.wicket.components;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

/**
 * The Class AbstractParentMarkupContainer is used for repeaters.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractParentMarkupContainer
    extends WebMarkupContainer
{

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new abstract parent markup container.
     *
     * @param _wicketId the wicket id of this component
     */
    public AbstractParentMarkupContainer(final String _wicketId)
    {
        super(_wicketId);
    }

    /**
     * Instantiates a new abstract parent markup container.
     *
     * @param _wicketId the wicket id of this component
     * @param _model the model for this component
     */
    public AbstractParentMarkupContainer(final String _wicketId,
                                         final IModel<?> _model)
    {
        super(_wicketId, _model);
    }

    /**
     * @see org.apache.wicket.Component#getMarkupId()
     * @return the id
     */
    @Override
    public String getMarkupId()
    {
        return getId();
    }

    /**
     * @see org.apache.wicket.MarkupContainer#onRender(org.apache.wicket.markup.MarkupStream)
     * @param _markupStream the markup stream
     */
    @Override
    protected void onRender(final MarkupStream _markupStream)
    {
        final int markupStart = _markupStream.getCurrentIndex();

        // Get mutable copy of next tag
        final ComponentTag openTag = _markupStream.getTag();
        final ComponentTag tag = openTag.mutable();

        // Call any tag handler
        onComponentTag(tag);

        // Render open tag
        if (!getRenderBodyOnly()) {
            renderComponentTag(tag);
        }
        _markupStream.next();

        // Render the body only if open-body-close. Do not render if
        // open-close.
        if (tag.isOpen()) {
            // Render the body
            onComponentTagBody(_markupStream, tag);
        }
        _markupStream.setCurrentIndex(markupStart);

        final Iterator<?> childs = this.iterator();
        while (childs.hasNext()) {
            _markupStream.setCurrentIndex(markupStart);

            final Component child = (Component) childs.next();

            child.render(getMarkupStream());
        }

        _markupStream.setCurrentIndex(markupStart);
        _markupStream.next();
        // Render close tag

        if (tag.isOpen()) {
            if (openTag.isOpen()) {
                // Get the close tag from the stream
                ComponentTag closeTag = _markupStream.getTag();

                // If the open tag had its id changed
                if (tag.getNameChanged()) {
                    // change the id of the close tag
                    closeTag = closeTag.mutable();
                    closeTag.setName(tag.getName());
                }

                // Render the close tag
                renderComponentTag(closeTag);
                _markupStream.next();
            }
        }
    }
}
