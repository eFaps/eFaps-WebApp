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

package org.efaps.ui.wicket.behaviors.dojo;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * This class renders the Links for the JavaScripts in the Head for Behaviors
 * using Dojo.
 *
 * @author The eFaps Team
 * @version $Id: AbstractDojoBehavior.java 7532 2012-05-19 06:31:05Z
 *          jan@moxter.net $
 */
public abstract class AbstractDojoBehavior
    extends Behavior
{

    /**
     * Reference to the stylesheet.
     */
    public static final ResourceReference CSS_TUNDRA = new CssResourceReference(AbstractDojoBehavior.class,
                    "dijit/themes/tundra/tundra.css");

    /**
     * Reference to the JavaScript.
     */
    public static final JavaScriptResourceReference JS_DOJO = new JavaScriptResourceReference(
                    AbstractDojoBehavior.class,
                    "dojo/dojo.js");

    /**
     * Reference to the JavaScript.
     */
    public static final JavaScriptResourceReference JS_EFAPSDOJO = new JavaScriptResourceReference(
                    AbstractDojoBehavior.class,
                    "dojo/eFapsDojo.js");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Render the links for the head.
     *
     * @see org.apache.wicket.behavior.AbstractBehavior#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
     * @param _component component the header will be rendered for
     * @param _response resonse to add
     */
    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        final IRequestHandler handler = new ResourceReferenceRequestHandler(AbstractDojoBehavior.JS_DOJO,
                        new PageParameters());
        final String url = RequestCycle.get().urlFor(handler).toString();
        final StringBuilder js = new StringBuilder()
            .append("djConfig.baseUrl=\"").append(url.substring(0, url.lastIndexOf("/"))).append("\";");
        _response.render(JavaScriptHeaderItem.forScript(js, AbstractDojoBehavior.class.getName()));
        _response.render(JavaScriptHeaderItem.forReference(AbstractDojoBehavior.JS_DOJO));
        _response.render(JavaScriptHeaderItem.forReference(AbstractDojoBehavior.JS_EFAPSDOJO));
        _response.render(CssHeaderItem.forReference(AbstractDojoBehavior.CSS_TUNDRA));
    }

    /**
     * All components using dojo must render the id of the component.
     *
     * @see org.apache.wicket.behavior.AbstractBehavior#beforeRender(org.apache.wicket.Component)
     * @param _component component
     */
    @Override
    public void beforeRender(final Component _component)
    {
        super.beforeRender(_component);
        _component.setOutputMarkupId(true);
    }
}
