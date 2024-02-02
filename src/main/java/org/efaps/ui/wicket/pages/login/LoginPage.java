/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.pages.login;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Session;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.ui.wicket.EFapsNoAuthorizationNeededInterface;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.pages.info.GatherInfoPage;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class renders the LoginPage for the eFaps-WebApplication.<br>
 * It is called from the #
 * {@link #onRuntimeException(org.efaps.ui.wicket.EFapsWebRequestCycle)} method,
 * in the case that noone is logged in. In case of a wrong login try, an
 * additional Message is shown to the User.
 *
 * @author The eFaps Team
 */
public class LoginPage
    extends WebPage
    implements EFapsNoAuthorizationNeededInterface
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(LoginPage.class);

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
        final TransparentWebMarkupContainer body = new TransparentWebMarkupContainer("body");
        this.add(body);
        try {
            body.add(AttributeModifier.append("class", AppAccessHandler.getApplicationKey()));
        } catch (final EFapsException e) {
            LoginPage.LOG.error("Could not set class attribute for body", e);
        }
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
        body.add(form);
        form.add(new Label("formname", new Model<>(DBProperties.getProperty("Login.Name.Label",
                        Session.get().getLocale().getLanguage()))));

        form.add(new Label("formpwd", new Model<>(DBProperties.getProperty("Login.Password.Label",
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

        button.add(new Label("formbuttonlabel", new Model<>(DBProperties.getProperty("Login.Button.Label",
                        Session.get().getLocale().getLanguage()))));

        if (_msg) {
            this.add(new Label("msg", new Model<>(DBProperties.getProperty("Login.Wrong.Label", Session.get()
                            .getLocale().getLanguage()))));
        } else {
            this.add(new WebMarkupContainer("msg").setVisible(false));
        }
        setStatelessHint(true);
        if (EFapsSession.get().isLogedIn()) {
            getRequestCycle().setResponsePage(new GatherInfoPage());
        }
    }

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
