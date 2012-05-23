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
import org.efaps.ui.wicket.components.split.ListOnlyPanel;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PreLoaderPanel
    extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(PreLoaderPanel.class, "PreLoaderPanel.css");

    public static final String PRELOADER_CLASSNAME = "eFapsPreloader";
    public static final String CONTENT_CLASSNAME = "eFapsPreloaderContent";

    /**
     * @param _wicketId wicketId for this Panel.
     */
    public PreLoaderPanel(final String _wicketId)
    {
        super(_wicketId);
        this.add(StaticHeaderContrBehavior.forCss(ListOnlyPanel.CSS));

        add(AttributeModifier.append("class", PreLoaderPanel.PRELOADER_CLASSNAME));
        setOutputMarkupId(true);
        add(new PreloaderBehavior());

        final WebMarkupContainer content = new WebMarkupContainer("content");
        content.add(AttributeModifier.append("class", PreLoaderPanel.CONTENT_CLASSNAME));
        add(content);
        content.add(new Label("label", DBProperties.getProperty("preloader.message")).setEscapeModelStrings(false));
    }

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
                .append("require([\"dojo/ready\", \"dojo/_base/fx\"]);\n")
                .append(" dojo.ready(function() {\n")
                .append(" setTimeout(function hideLoader(){\n")
                .append(" dojo.fadeOut({\n")
                .append(" node: '").append(_component.getMarkupId(true)).append("',")
                .append(" duration:1000,\n")
                .append(" onEnd: function(n){\n")
                .append(" n.style.display = \"none\";\n")
                .append(" }")
                .append(" }).play();\n")
                .append(" }, 250);")
                .append(" });");
            _response.render(JavaScriptHeaderItem.forScript(js,
                            _component.getClass().getName() + "_" + _component.getMarkupId(true)));
        }
    }
}
