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

package org.efaps.ui.wicket.components;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.util.io.IClusterable;
import org.efaps.ui.wicket.behaviors.dojo.LazyIframeBehavior;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LazyIframe
    extends WebMarkupContainer
    implements ILinkListener
{

    /**
     * Provides the Frame Content.
     */
    public interface IFrameProvider
        extends IClusterable
    {
        /**
         * @return the page to be displayed
         */
        Page getPage();
    }


    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The link to the page.
     */
    private final IFrameProvider frameProvider;


    /**
     * @param _wicketId         wicket id of this component
     * @param _frameProvider    Provider for the frame
     */
    public LazyIframe(final String _wicketId,
                      final IFrameProvider _frameProvider,
                      final String _frameMarkupId)
    {
        super(_wicketId);
        this.frameProvider = _frameProvider;
        add(new LazyIframeBehavior(_frameMarkupId));
        setOutputMarkupId(true);
    }

    @Override
    public void onLinkClicked()
    {
        setResponsePage(this.frameProvider.getPage());
    }
}
