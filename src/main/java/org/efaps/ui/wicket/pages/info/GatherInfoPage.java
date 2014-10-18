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

package org.efaps.ui.wicket.pages.info;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.pages.BrowserInfoForm;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.RequestCycle;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.IconHeaderItem;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class GatherInfoPage
    extends WebPage
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(GatherInfoPage.class,
                    "GatherInfoPage.css");

    /**
     * Info form.
     */
    private BrowserInfoForm browserInfoForm;

    /**
     * Constructor.
     */
    public GatherInfoPage()
    {
        final WebComponent meta = new WebComponent("meta");

        final IModel<String> urlModel = new LoadableDetachableModel<String>()
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected String load()
            {
                final CharSequence url = urlFor(GatherInfoPage.class, null);
                return url.toString();
            }
        };

        meta.add(AttributeModifier.replace("content", new AbstractReadOnlyModel<String>()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public String getObject()
            {
                return "0; url=" + urlModel.getObject();
            }

        }));
        add(meta);

        add(new Label("pageTitle", DBProperties.getProperty("Logo.Version.Label")));

        add(new Label("label", DBProperties.getProperty("gatherInfoPage.message")).setEscapeModelStrings(false));

        this.browserInfoForm = new BrowserInfoForm("postback")
        {
            private static final long serialVersionUID = 1L;

            /**
             * @see org.apache.wicket.markup.html.pages.BrowserInfoForm#afterSubmit()
             */
            @Override
            protected void afterSubmit()
            {
                RequestCycle.get().setResponsePage(WebApplication.get().getHomePage());
            }
        };
        add(this.browserInfoForm);
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(GatherInfoPage.CSS));
        _response.render(new IconHeaderItem(AbstractMergePage.FAVICON));
        _response.render(OnLoadHeaderItem.forScript(
                        String.format("Wicket.BrowserInfo.submitForm('%s')", this.browserInfoForm.getFormMarkupId())));
    }

}
