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
 * Revision:        $Rev:1490 $
 * Last Changed:    $Date:2007-10-15 18:04:02 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.login;

import org.apache.wicket.Session;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.EFapsNoAuthorizationNeededInterface;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.pages.info.GatherInfoPage;

/**
 * This class renders the LoginPage for the eFaps-WebApplication.<br>
 * It is called from the #
 * {@link #onRuntimeException(org.efaps.ui.wicket.EFapsWebRequestCycle)} method,
 * in the case that noone is logged in. In case of a wrong login try, an
 * additional Message is shown to the User.
 *
 * @author The eFaps Team
 * @version $Id:LoginPage.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class LoginPage
    extends WebPage
    implements EFapsNoAuthorizationNeededInterface
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Standard Constructor showing no Message.
     */
    public LoginPage()
    {
        this(false);
    }

    /**
     * Constructor showing a "wrong Login Message" depending on the Parameter.
     *
     * @param _msg true, if "wrong Login Message: should be shown, else false
     */
    public LoginPage(final boolean _msg)
    {
        final Form<Object> form = new Form<Object>("form")
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit()
            {
                super.onSubmit();
                final EFapsSession session = (EFapsSession) getSession();
                session.login();
                if (session.isLogedIn()) {
                    getRequestCycle().setResponsePage(new GatherInfoPage());
                } else {
                    final LoginPage page = new LoginPage(true);
                    getRequestCycle().setResponsePage(page);
                }
            }
        };
        this.add(form);
        form.add(new Label("formname", new Model<String>(DBProperties.getProperty("Login.Name.Label",
                        Session.get().getLocale().getLanguage()))));

        form.add(new Label("formpwd", new Model<String>(DBProperties.getProperty("Login.Password.Label",
                        Session.get().getLocale().getLanguage()))));

        final Button button = new Button("formbutton")
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("type", "submit");
            }
        };

        form.add(button);

        button.add(new Label("formbuttonlabel", new Model<String>(DBProperties.getProperty("Login.Button.Label",
                        Session.get().getLocale().getLanguage()))));

        if (_msg) {
            this.add(new Label("msg", new Model<String>(DBProperties.getProperty("Login.Wrong.Label", Session.get()
                            .getLocale().getLanguage()))));
        } else {
            this.add(new WebMarkupContainer("msg").setVisible(false));
        }
    }


    /*
     * (non-Javadoc)
     * @see org.apache.wicket.Component#renderHead(org.apache.wicket.markup.head.IHeaderResponse)
     */
    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(CssHeaderItem.forReference(new CssResourceReference(LoginPage.class, "LoginPage.css")));
        final StringBuilder js = new StringBuilder();
        js.append("function test4top() {\n")
            .append("  if(top!=self) {\n")
            .append("    top.location = self.location;\n")
            .append("  }\n")
            .append("}\n");
        _response.render(JavaScriptHeaderItem.forScript(js, LoginPage.class.getName()));
    }
}
