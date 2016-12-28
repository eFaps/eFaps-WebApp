/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.ui.wicket.components.preloader;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class PreLoaderPanel
    extends Panel
{
    /**
     * Reference to the StyleSheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(PreLoaderPanel.class,
                    "PreLoaderPanel.css");

    /** The Constant PRELOADER_CLASSNAME. */
    public static final String PRELOADER_CLASSNAME = "eFapsPreloader";

    /** The Constant CONTENT_CLASSNAME. */
    public static final String CONTENT_CLASSNAME = "eFapsPreloaderContent";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicketId for this Panel.
     */
    public PreLoaderPanel(final String _wicketId)
    {
        super(_wicketId);
        add(AttributeModifier.append("class", PreLoaderPanel.PRELOADER_CLASSNAME));
        setOutputMarkupId(true);
        add(new PreloaderBehavior());

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.add(AttributeModifier.append("class", PreLoaderPanel.CONTENT_CLASSNAME));
        add(content);
        content.add(new Label("label", DBProperties.getProperty("PreLoader.message")).setEscapeModelStrings(false));
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */
    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(PreLoaderPanel.CSS));
    }

    /**
     * The Class PreloaderBehavior.
     */
    public final class PreloaderBehavior
        extends AbstractDojoBehavior
    {

        private static final long serialVersionUID = 1L;

        /**
         * Render the links for the head.
         *
         * @param _component component the header will be rendered for
         * @param _response resonse to add
         */
        @Override
        public void renderHead(final Component _component,
                               final IHeaderResponse _response)
        {
            super.renderHead(_component, _response);
            final StringBuilder js = new StringBuilder()
                .append(" ready(function() {\n")
                .append(" setTimeout(function hideLoader(){\n")
                .append(" fx.fadeOut({\n")
                .append(" node: '").append(_component.getMarkupId(true)).append("',")
                .append(" duration:1000,\n")
                .append(" onEnd: function(n){\n")
                .append(" n.style.display = \"none\";\n")
                .append(" }")
                .append(" }).play();\n")
                .append(" }, 250);});");
            _response.render(JavaScriptHeaderItem.forScript(DojoWrapper.require(js, DojoClasses.ready, DojoClasses.fx),
                            _component.getClass().getName() + "_" + _component.getMarkupId(true)));
        }
    }
}
