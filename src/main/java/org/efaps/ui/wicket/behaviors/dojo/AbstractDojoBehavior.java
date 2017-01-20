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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.WicketEventJQueryResourceReference;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.IReferenceHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.Strings;
import org.efaps.db.Context;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class renders the Links for the JavaScripts in the Head for Behaviors
 * using Dojo.
 *
 * @author The eFaps Team
 * @version $Id$
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
     * Reference to the stylesheet.
     */
    public static final ResourceReference CSS_CLARO = new CssResourceReference(AbstractDojoBehavior.class,
                    "dijit/themes/claro/claro.css");

    /**
     * Reference to the stylesheet.
     */
    public static final ResourceReference CSS_NIHILO = new CssResourceReference(AbstractDojoBehavior.class,
                    "dijit/themes/nihilo/nihilo.css");

    /**
     * Reference to the stylesheet.
     */
    public static final ResourceReference CSS_SORIA = new CssResourceReference(AbstractDojoBehavior.class,
                    "dijit/themes/soria/soria.css");

    /**
     * Reference to the JavaScript.
     */
    public static final JavaScriptResourceReference JS_DOJO = new JavaScriptResourceReference(
                    AbstractDojoBehavior.class,
                    "dojo/dojo.js");

    /**
     * Logger.
     */
    private static final Logger LOG =  LoggerFactory.getLogger(AbstractDojoBehavior.class);

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
        _response.render(new PriorityHeaderItem(new DojoHeaderItem(AbstractDojoBehavior.JS_DOJO,
                        AbstractDojoBehavior.class.getName())));
        final String clazz = Configuration.getAttribute(ConfigAttribute.DOJO_CLASS);

        ResourceReference reference = null;
        if ("tundra".equals(clazz)) {
            reference = AbstractDojoBehavior.CSS_TUNDRA;
        } else if ("claro".equals(clazz)) {
            reference = AbstractDojoBehavior.CSS_CLARO;
        } else if ("nihilo".equals(clazz)) {
            reference = AbstractDojoBehavior.CSS_NIHILO;
        } else if ("soria".equals(clazz)) {
            reference = AbstractDojoBehavior.CSS_TUNDRA;
        }
        if (reference != null) {
            _response.render(CssHeaderItem.forReference(reference));
        }
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

    /**
     * Class to add Dojo script tags.
     */
    public static final class DojoHeaderItem
        extends HeaderItem
        implements IReferenceHeaderItem
    {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * Resource refrecne with the actual script.
         */
        private final ResourceReference reference;

        /**
         * Id this script will be identified by.
         */
        private final String id;

        /**
         * @param _reference the ResourceReference3
         * @param _id       id of this script
         */
        public DojoHeaderItem(final ResourceReference _reference,
                              final String _id)
        {
            this.id = _id;
            this.reference = _reference;
        }

        /**
         * @return the reference for the item.
         */
        @Override
        public ResourceReference getReference()
        {
            return this.reference;
        }

        /**
         * @return The tokens this {@code HeaderItem} can be identified by. If
         *         any of the tokens has already been rendered, this
         *         {@code HeaderItem} will not be rendered.
         */
        @Override
        public Iterable<?> getRenderTokens()
        {
            final String url = Strings.stripJSessionId(getUrl());
            final List<String> ret;
            if (Strings.isEmpty(this.id)) {
                ret = Collections.singletonList("javascript-" + url);
            } else {
                ret = Arrays.asList("javascript-" + this.id, "javascript-" + url);
            }
            return ret;
        }

        /**
         * Renders the {@code HeaderItem} to the response.
         *
         * @param _response Response
         */
        @Override
        public void render(final Response _response)
        {
            final String url = getUrl();
            _response.write("<script type=\"text/javascript\"> ");
            _response.write(" var dojoConfig = {");
            _response.write("baseUrl:\"");
            _response.write(url.substring(0, url.lastIndexOf("/")));
            _response.write("\",");
            _response.write("async:true,");

            if (WebApplication.get().getDebugSettings().isAjaxDebugModeEnabled()) {
                _response.write("isDebug:true,");
            }
            _response.write("locale:\"");
            try {
                _response.write(Context.getThreadContext().getLocale().toLanguageTag());
            } catch (final EFapsException e) {
                AbstractDojoBehavior.LOG.warn("Could not set correct locale for Dojo");
            }
            _response.write("\",");
            _response.write("parseOnLoad: true");
            _response.write(" };");
            _response.write("</script>");
            _response.write("<script type=\"text/javascript\"");
            if (this.id != null) {
                _response.write(" id=\"" + this.id + "\" ");
            }
            _response.write(" src=\"");
            _response.write(url);
            _response.write("\"></script>");
            _response.write("\n");
        }

        /**
         * @return the url for this resource
         */
        private String getUrl()
        {
            final IRequestHandler handler = new ResourceReferenceRequestHandler(getReference(),
                            new PageParameters());
            return RequestCycle.get().urlFor(handler).toString();
        }

        @Override
        public List<HeaderItem> getDependencies()
        {
            final List<HeaderItem> ret = new ArrayList<>();
            ret.addAll(WicketEventJQueryResourceReference.get().getDependencies());
            return ret;
        }
    }
}
