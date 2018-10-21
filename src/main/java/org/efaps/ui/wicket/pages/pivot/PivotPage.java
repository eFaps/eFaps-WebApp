/*
 * Copyright 2003 - 2018 The eFaps Team
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
package org.efaps.ui.wicket.pages.pivot;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;

public class PivotPage
    extends WebPage
{

    private static final long serialVersionUID = 1L;

    private final static JavaScriptResourceReference WDR_JS = new JavaScriptResourceReference(PivotPage.class,
                    "webdatarocks.js");
    private final static JavaScriptResourceReference WDRTB_JS = new JavaScriptResourceReference(PivotPage.class,
                    "webdatarocks.toolbar.js");
    private final static CssResourceReference WDR_CSS = new CssResourceReference(PivotPage.class, "webdatarocks.css");

    private final static PackageResourceReference LOG_ES = new PackageResourceReference(PivotPage.class, "loc/es.json");

    public PivotPage() {
        add(new WebMarkupContainer("wdr") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTagBody(final MarkupStream _markupStream, final ComponentTag _openTag)
            {
                final IRequestHandler handler = new ResourceReferenceRequestHandler(LOG_ES, getPageParameters());
                final CharSequence locUrl = RequestCycle.get().urlFor(handler);
                final StringBuilder js = new StringBuilder()
                                .append("var pivot = new WebDataRocks({")
                                .append("container : \"#wdr-component\",")
                                .append("toolbar : true,")
                                .append("report : {")
                                .append("dataSource : {")
                                .append("filename : \"https://cdn.webdatarocks.com/data/data.csv\"")
                                .append("}")
                                .append("},")
                                .append("global: {")
                                .append("localization: \"").append(locUrl).append("\"")
                                .append("}")
                                .append("});");
                replaceComponentTagBody(_markupStream, _openTag, js);
            }
        });
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(JavaScriptHeaderItem.forReference(WDR_JS));
        _response.render(JavaScriptHeaderItem.forReference(WDRTB_JS));
        _response.render(CssHeaderItem.forReference(WDR_CSS));
    }
}
