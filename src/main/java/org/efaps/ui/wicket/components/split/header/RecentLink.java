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


package org.efaps.ui.wicket.components.split.header;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.Link;
import org.efaps.ui.wicket.components.IRecent;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;


/**
 * Link that forwards the execution of the onCLick event to the IRecent
 * open() method.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class RecentLink
    extends Link<IRecent>
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Recent this Link will execute.
     */
    private final IRecent recent;

    /**
     * Max Length of the label.
     */
    private final int maxLength;

    /**
     * @param _wicketId wicketId for this component
     * @param _recent   IRecent that will be called
     * @param _maxLength maximum Length of the Label
     */
    public RecentLink(final String _wicketId,
                      final IRecent _recent,
                      final int _maxLength)
    {
        super(_wicketId);
        this.recent = _recent;
        this.maxLength = _maxLength > 0 ? _maxLength : 25;
    }

    /**
     * On click the event is executed in the {@link #recent}.
     * @see org.apache.wicket.markup.html.link.Link#onClick()
     */
    @Override
    public void onClick()
    {
        try {
            this.recent.open(this);
        } catch (final EFapsException e) {
            setResponsePage(new ErrorPage(e));
        }
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.markup.html.link.AbstractLink#onComponentTagBody(
     * org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
     */
    @Override
    protected void onComponentTagBody(final MarkupStream _markupStream,
                                      final ComponentTag _openTag)
    {
        super.onComponentTagBody(_markupStream, _openTag);
        try {
            replaceComponentTagBody(_markupStream, _openTag, this.recent.getLabel(this.maxLength));
        } catch (final EFapsException e) {
            setResponsePage(new ErrorPage(e));
        }
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.markup.html.link.Link#onComponentTag(org.apache.wicket.markup.ComponentTag)
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("li");
        super.onComponentTag(_tag);
    }
}
