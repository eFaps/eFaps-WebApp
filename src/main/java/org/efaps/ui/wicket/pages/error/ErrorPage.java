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
package org.efaps.ui.wicket.pages.error;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.Model;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.datetime.markup.html.basic.DateLabel;

/**
 * This Page is the ErrorPage for the eFaps-Webapplication.<br>
 * It renders a Page that shows the EFapsException in a user friendly way.
 *
 * @author The eFaps Team
 */
public class ErrorPage
    extends WebPage
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ErrorPage.class);

    /**
     * reference to the StyleSheet of this Page stored in the eFaps-DataBase.
     */
    private static final EFapsContentReference CSS =
                    new EFapsContentReference(ErrorPage.class, "ErrorPage.css");

    /**
     * Constructor adding all Components.
     *
     * @param _exception Excpetion that was thrown
     */
    public ErrorPage(final Exception _exception)
    {
        super();

        ErrorPage.LOG.error("ErrorPage was called", _exception);

        String errorMessage = _exception.getMessage();
        String errorAction = "";
        String errorKey = "";
        String errorId = "";
        String errorAdvanced = "";

        add(DateLabel.forDateStyle("date", Model.of(new Date()), "FF"));

        if (_exception instanceof EFapsException) {
            final EFapsException eFapsException = (EFapsException) _exception;
            errorKey = eFapsException.getClassName().getName() + "." + eFapsException.getId();
            errorId = DBProperties.getProperty(errorKey + ".Id");
            errorMessage = DBProperties.getProperty(errorKey + ".Message");
            errorAction = DBProperties.getProperty(errorKey + ".Action");
            if (eFapsException.getArgs() != null) {
                errorMessage = MessageFormat.format(errorMessage, eFapsException.getArgs());
            }
        } else {
            if (errorMessage == null) {
                errorMessage = _exception.toString();
            }
        }

        final StackTraceElement[] traceElements = _exception.getStackTrace();
        for (final StackTraceElement traceElement : traceElements) {
            errorAdvanced += traceElement.toString() + "\n";
        }

     // set the title for the Page
        add(new Label("pageTitle", DBProperties.getProperty("ErrorPage.Titel")));

        add(new Label("dateLabel", DBProperties.getProperty("ErrorPage.Date.Label")));

        add(new Label("errorIDLabel", DBProperties.getProperty("ErrorPage.Id.Label")));
        add(new Label("errorID", errorId));

        add(new Label("errorMsgLabel", DBProperties.getProperty("ErrorPage.Message.Label")));
        add(new MultiLineLabel("errorMsg", errorMessage));

        final WebMarkupContainer advanced = new WebMarkupContainer("advanced");

        final AjaxLink<Object> ajaxlink = new AjaxLink<Object>("openclose")
        {

            private static final long serialVersionUID = 1L;

            private boolean expanded = false;

            @Override
            public void onClick(final AjaxRequestTarget _target)
            {
                this.expanded = !this.expanded;
                final String text;
                if (this.expanded) {
                    text = "less";
                } else {
                    text = "more";
                }
                advanced.setVisible(this.expanded);

                final Label label = new Label("opencloseLabel", text);

                label.setOutputMarkupId(true);

                replace(label);

                _target.add(label);
                _target.add(advanced);

            }
        };
        this.add(ajaxlink);

        ajaxlink.add(new Label("opencloseLabel", "more").setOutputMarkupId(true));

        try {
            if (!(errorAdvanced.length() > 0)
                            && Context.getThreadContext().getPerson()
                                            .isAssigned(Role.get(KernelSettings.USER_ROLE_ADMINISTRATION))) {
                ajaxlink.setVisible(false);
            }
        } catch (final EFapsException e) {
            ErrorPage.LOG.error("Catched Exception", _exception);
        }

        this.add(advanced);
        advanced.setVisible(false);
        advanced.setOutputMarkupPlaceholderTag(true);

        advanced.add(new MultiLineLabel("advancedMsg", errorAdvanced));

        add(new Label("errorActLabel", DBProperties
                        .getProperty("ErrorPage.Action.Label")));
        add(new Label("errorAct", errorAction));

    }

    @Override
    public boolean isErrorPage()
    {
        return true;
    }

    @Override
    public boolean isVersioned()
    {
        return false;
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(ErrorPage.CSS));
    }
}
