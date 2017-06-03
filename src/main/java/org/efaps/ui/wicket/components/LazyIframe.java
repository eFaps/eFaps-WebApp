/*
 * Copyright 2003 - 2017 The eFaps Team
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

import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.io.IClusterable;
import org.efaps.ui.wicket.behaviors.dojo.LazyIframeBehavior;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class LazyIframe
    extends WebMarkupContainer
    implements IRequestListener
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The link to the page.
     */
    private final IFrameProvider frameProvider;

    /**
     * Instantiates a new lazy iframe.
     *
     * @param _wicketId         wicket id of this component
     * @param _frameProvider    Provider for the frame
     * @param _frameMarkupId the frame markup id
     */
    public LazyIframe(final String _wicketId,
                      final IFrameProvider _frameProvider,
                      final String _frameMarkupId)
    {
        this(_wicketId, _frameProvider, _frameMarkupId, true);
    }

    /**
     * Instantiates a new lazy iframe.
     *
     * @param _wicketId the wicket id
     * @param _frameProvider the frame provider
     * @param _frameMarkupId the frame markup id
     * @param _autoLoad the auto load
     */
    public LazyIframe(final String _wicketId,
                      final IFrameProvider _frameProvider,
                      final String _frameMarkupId,
                      final boolean _autoLoad)
    {
        super(_wicketId);
        this.frameProvider = _frameProvider;
        if (_autoLoad) {
            add(new LazyIframeBehavior(_frameMarkupId));
        }
        setOutputMarkupId(true);
    }

    @Override
    public void onRequest()
    {
        setResponsePage(this.frameProvider.getPage(this));
    }

    /**
     * Provides the Frame Content.
     */
    public interface IFrameProvider
        extends IClusterable
    {
        /**
         * Gets the page.
         *
         * @param _component the component
         * @return the page to be displayed
         */
        Page getPage(Component _component);
    }
}
