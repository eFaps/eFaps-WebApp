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
 * Revision:        $Rev:1490 $
 * Last Changed:    $Date:2007-10-15 18:04:02 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.main;

import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.time.Duration;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.message.MessageStatusHolder;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.SetMessageStatusBehavior;
import org.efaps.ui.wicket.behaviors.ShowFileCallBackBehavior;
import org.efaps.ui.wicket.components.ChildCallBackHeaderContributer;
import org.efaps.ui.wicket.components.menu.MenuContainer;
import org.efaps.ui.wicket.components.menu.StandardLink;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.empty.EmptyPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.util.EFapsException;

/**
 * This Page is the MainPage for eFaps and also the Homepage as set in
 * {@link #org.efaps.ui.wicket.EFapsApplication.getHomePage()}.<br>
 * It contains the MainMenu and two iFrames. One for the Content and one hidden
 * to provide the possibility to set a response into the hidden FRame.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class MainPage
    extends AbstractMergePage
{
    /**
     * this static variable contains the id for the htmlFrame.
     */
    public static final String IFRAME_WICKETID = "content";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(MainPage.class, "MainPage.css");

    /**
     * Reference to a JavaScript used for this Page.
     */
    private static final EFapsContentReference FRAMEJS = new EFapsContentReference(MainPage.class, "SetFrameHeight.js");

    /**
     * Key to the parameter for storing the height and width of the browser
     * window.
     */
    private static String HEIGTH_PARAMETERNAME = "eFapsWindowHeight";

    /**
     * Key to the parameter for storing the height and width of the browser
     * window.
     */
    private static String WIDTH_PARAMETERNAME = "eFapsWindowWidth";


    /**
     * The MainPage has a ModalWindow that can be called from the childPages.
     */
    private final ModalWindowContainer modal = new ModalWindowContainer("modal");

    /**
     * Event that is fired on resize.
     */
    private final ResizeEventBehavior resize;

    /**
     * Constructor adding all Components to this Page.
     */
    public MainPage()
    {
        super();
        // call the client info to force the reload script to be executed on the
        // beginning of a session,
        // if an ajax call would be done as first an error occurs
        ((WebClientInfo) Session.get().getClientInfo()).getProperties();
        // add the file call back used to open a file in the session and the
        // main page
        final ShowFileCallBackBehavior fileCall = new ShowFileCallBackBehavior();
        this.add(fileCall);
        ((EFapsSession) getSession()).setFileCallBack(fileCall);

        // we need to add a JavaScript Function to resize the iFrame
        // don't merge it to keep the sequence
        add(StaticHeaderContrBehavior.forJavaScript(MainPage.FRAMEJS, true));

        // set the title for the Page
        add(new Label("pageTitle", DBProperties.getProperty("Logo.Version.Label")));
        add(this.modal);


        this.add(StaticHeaderContrBehavior.forCss(MainPage.CSS));
        this.add(new ChildCallBackHeaderContributer());

        this.resize = new ResizeEventBehavior();
        add(this.resize);
        final WebMarkupContainer logo = new WebMarkupContainer("logo");
        this.add(logo);
        final Label welcome = new Label("welcome", DBProperties.getProperty("Logo.Welcome.Label")) {

            private static final long serialVersionUID = 1L;

            @Override
            public void renderHead(final IHeaderResponse _response)
            {
                super.renderHead(_response);
                final CharSequence resizeScript = MainPage.this.resize.getCallbackScript();
                _response.render(JavaScriptHeaderItem.forScript(resizeScript, MainPage.class.getName()));
            }
        };
        logo.add(welcome);


        try {
            final Context context = Context.getThreadContext();
            logo.add(new Label("firstname", context.getPerson().getFirstName()));
            logo.add(new Label("lastname", context.getPerson().getLastName()));
            final String companyName = context.getCompany() == null ? "" : context.getCompany().getName();
            logo.add(new Label("company", companyName));
            logo.add(new AttributeModifier("class",
                            new Model<String>("eFapsLogo " + companyName.replaceAll("\\W", ""))));
            final long usrId = context.getPersonId();
            // Admin_Common_SystemMessageAlert
            final StandardLink alert = new StandardLink("useralert",
                            new UIModel<UIMenuItem>(new UIMenuItem(SetMessageStatusBehavior.getCmdUUD())))
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(final ComponentTag _tag)
                {
                    super.onComponentTag(_tag);
                }

                @Override
                public void onComponentTagBody(final MarkupStream _markupStream,
                                                  final ComponentTag _openTag)
                {
                    super.onComponentTagBody(_markupStream, _openTag);
                    replaceComponentTagBody(_markupStream, _openTag,
                                    SetMessageStatusBehavior.getLabel(MessageStatusHolder.getUnReadCount(usrId),
                                                    MessageStatusHolder.getReadCount(usrId)));
                }
            };
            this.add(alert);
            if (MessageStatusHolder.hasUnreadMsg(usrId)) {
                alert.add(new AttributeModifier("class", new Model<String>("unread")));
            } else if (!MessageStatusHolder.hasReadMsg(usrId)) {
                alert.add(new AttributeModifier("style", new Model<String>("display:none")));
            }

        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }

        // add the MainToolBar to the Page
        final MenuContainer menu = new MenuContainer("menu", new UIModel<UIMenuItem>(new UIMenuItem(UUID
                        .fromString("87001cc3-c45c-44de-b8f1-776df507f268"))));
        this.add(menu);

        this.add(new Label("version", DBProperties.getProperty("Logo.Version.Label")));

        this.add(new InlineFrame(MainPage.IFRAME_WICKETID, EmptyPage.class));

        this.add(new InlineFrame("hidden", EmptyPage.class));
    }

    /**
     * Method to get the ModalWindow of this Page.
     *
     * @return modal window
     */
    public final ModalWindowContainer getModal()
    {
        return this.modal;
    }

    /**
     * Event that is fired on the resize of the client browser window.
     */
    public class ResizeEventBehavior
        extends AjaxEventBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public ResizeEventBehavior()
        {
            super("onResize");
        }

        /**
         * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getCallbackScript()
         * @return script
         */
        @Override
        public CharSequence getCallbackScript()
        {
            final StringBuilder js = new StringBuilder()
                .append("function eFapsPostSize(").append(MainPage.WIDTH_PARAMETERNAME).append(",")
                .append(MainPage.HEIGTH_PARAMETERNAME).append(") {\n")
                .append(getCallbackFunctionBody(MainPage.WIDTH_PARAMETERNAME, MainPage.HEIGTH_PARAMETERNAME))
                .append("}\n")
                .append("window.onresize = ")
                .append("function(){\n").append("eFapsSetIFrameHeight();\n")
                .append("eFapsPostSize(window.innerWidth, window.innerHeight);\n")
                .append("}\n")
                .append("Wicket.Event.add(window, \"domready\", function(event) { eFapsSetIFrameHeight();")
                .append("eFapsPostSize(window.innerWidth, window.innerHeight);}); \n");
            return js.toString();
        }

        /**
         * Overwritten to be deactivated.
         *
         * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getPreconditionScript()
         * @return null
         */
        @Override
        protected CharSequence getPreconditionScript()
        {
            return null;
        }

        @Override
        protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
        {
            super.updateAjaxAttributes(_attributes);
            _attributes.setThrottlingSettings(new ThrottlingSettings("mainThrottel", Duration.seconds(2)));
            _attributes.setMethod(Method.POST);
        }

        /**
         * On event the actual size of the browser window is stored in the
         * requestcycle.
         *
         * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final StringValue width = getComponent().getRequest().getRequestParameters()
                            .getParameterValue(MainPage.WIDTH_PARAMETERNAME);
            final StringValue height = getComponent().getRequest().getRequestParameters()
                            .getParameterValue(MainPage.HEIGTH_PARAMETERNAME);
            if (height != null) {
                final WebClientInfo asd = (WebClientInfo) Session.get().getClientInfo();
                asd.getProperties().setBrowserWidth(Integer.parseInt(width.toString()));
                asd.getProperties().setBrowserHeight(Integer.parseInt(height.toString()));
            }
        }
    }
}
