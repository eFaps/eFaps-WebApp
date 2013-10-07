/*
 * Copyright 2003 - 2013 The eFaps Team
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
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.time.Duration;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.message.MessageStatusHolder;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.SetMessageStatusBehavior;
import org.efaps.ui.wicket.behaviors.dojo.BorderContainerBehavior;
import org.efaps.ui.wicket.behaviors.dojo.BorderContainerBehavior.Design;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior.Region;
import org.efaps.ui.wicket.behaviors.dojo.MessageListenerBehavior;
import org.efaps.ui.wicket.behaviors.dojo.RequireBehavior;
import org.efaps.ui.wicket.components.LazyIframe;
import org.efaps.ui.wicket.components.menu.LinkItem;
import org.efaps.ui.wicket.components.menu.MenuBarPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.preloader.PreLoaderPanel;
import org.efaps.ui.wicket.models.PushMsg;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UIUserSession;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.dashboard.DashboardPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Id of the Iframe.
     */
    public static final String IFRAME_ID = "eFapsContentFrame";


    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MainPage.class);

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(MainPage.class, "MainPage.css");

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
     * Socket listening Component.
     */
    private Component socketMsg;

    /**
     * Constructor adding all Components to this Page.
     * @throws CacheReloadException on error
     */
    public MainPage()
        throws CacheReloadException
    {
        super();
        // add the debug bar for administration role, in case of an erro only log it
        Component debug = null;
        try {
         // Administration
            final Role role = Role.get(UUID.fromString("1d89358d-165a-4689-8c78-fc625d37aacd"));
            if (role != null && Context.getThreadContext().getPerson().isAssigned(role)) {
                debug = new DebugBar("debug");
            }
        } catch (final CacheReloadException e) {
            MainPage.LOG.error("Error on retrieving Role assignment.", e);
        } catch (final EFapsException e) {
            MainPage.LOG.error("Error on retrieving Role assignment.", e);
        } finally {
            if (debug == null) {
                debug = new WebComponent("debug").setVisible(false);
            }
            add(debug);
        }

        // call the client info to force the reload script to be executed on the
        // beginning of a session,
        // if an ajax call would be done as first an error occurs
        ((WebClientInfo) Session.get().getClientInfo()).getProperties();

        add(new RequireBehavior("dojo/dom", "dojo/_base/window"));
        add(new PreLoaderPanel("preloader"));

        final WebMarkupContainer borderPanel = new WebMarkupContainer("borderPanel");
        this.add(borderPanel);
        borderPanel.add(new BorderContainerBehavior(Design.HEADLINE, false));

        final LazyIframe mainPanel = new LazyIframe("mainPanel", new IPageLink()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public Page getPage()
            {
                Page error = null;
                WebPage page = null;
                try {
                    page = new DashboardPage(getPageReference());
                } catch (final EFapsException e) {
                    error = new ErrorPage(e);
                }
                return error == null ? page : error;
            }

            @Override
            public Class<? extends Page> getPageIdentity()
            {
                return AbstractContentPage.class;
            }
        });

        borderPanel.add(mainPanel);
        mainPanel.add(new ContentPaneBehavior(Region.CENTER, false));

        final WebMarkupContainer headerPanel = new WebMarkupContainer("headerPanel");
        borderPanel.add(headerPanel);
        headerPanel.add(new ContentPaneBehavior(Region.TOP, false));

        headerPanel.add(new MenuBarPanel("menubar", new UIModel<UIMenuItem>(new UIMenuItem(UUID
                      .fromString(Configuration.getAttribute(ConfigAttribute.TOOLBAR))))));

        // set the title for the Page
        add(new Label("pageTitle", DBProperties.getProperty("Logo.Version.Label")));
        add(this.modal);
        add(new ResizeEventBehavior());

        final WebMarkupContainer logo = new WebMarkupContainer("logo");
        headerPanel.add(logo);
        final Label welcome = new Label("welcome", DBProperties.getProperty("Logo.Welcome.Label"));
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
            final LinkItem alert = new LinkItem("useralert",
                            new UIModel<UIMenuItem>(new UIMenuItem(SetMessageStatusBehavior.getCmdUUD())))
            {

                private static final long serialVersionUID = 1L;

                @Override
                public void onComponentTagBody(final MarkupStream _markupStream,
                                                  final ComponentTag _openTag)
                {
                    try {
                        replaceComponentTagBody(_markupStream, _openTag,
                                        SetMessageStatusBehavior.getLabel(MessageStatusHolder.getUnReadCount(usrId),
                                                        MessageStatusHolder.getReadCount(usrId)));
                    } catch (final CacheReloadException e) {
                        MainPage.LOG.error("Cannot replace Component tag");
                    }
                }
            };
            add(alert);
            alert.add(new MessageListenerBehavior());
            alert.add(new AttributeModifier("class", new Model<String>("eFapsUserMsg")));
            if (!MessageStatusHolder.hasReadMsg(usrId)) {
                alert.add(new AttributeModifier("style", new Model<String>("display:none")));
            }
            final WebMarkupContainer socketMsgContainer = new WebMarkupContainer("socketMsgContainer");
            add(socketMsgContainer);
            if (Configuration.getAttributeAsBoolean(ConfigAttribute.WEBSOCKET_ACTVATE)) {
                socketMsgContainer.setOutputMarkupPlaceholderTag(true);
                this.socketMsg = new Label("socketMsg", "none yet");
                this.socketMsg.setOutputMarkupPlaceholderTag(true);
                this.socketMsg.add(new WebSocketBehavior()
                {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onConnect(final ConnectedMessage _message)
                    {
                        EFapsSession.get().getConnectionRegistry()
                                        .addMsgConnection(_message.getSessionId(), _message.getPageId());
                    }
                });
                socketMsgContainer.add(this.socketMsg);

                final AjaxLink<Void> close = new AjaxLink<Void>("socketMsgClose") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(final AjaxRequestTarget _target)
                    {
                        final MarkupContainer msgContainer = MainPage.this.socketMsg.getParent();
                        msgContainer.add(new AttributeModifier("style", new Model<String>("display:none")));
                        _target.add(msgContainer);
                    }
                };
                socketMsgContainer.add(close);
            } else {
                socketMsgContainer.setVisible(false);
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(MainPage.CSS));
        final StringBuilder js = new StringBuilder();
        js.append("  if(top!=self) {\n")
            .append("    top.location = self.location;\n")
            .append("  }\n");
        _response.render(JavaScriptHeaderItem.forScript(js, MainPage.class.getName()));
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

    @Override
    public void onEvent(final IEvent<?> _event)
    {
        if (_event.getPayload() instanceof WebSocketPushPayload) {
            final WebSocketPushPayload wsEvent = (WebSocketPushPayload) _event.getPayload();
            if (wsEvent != null) {
                final IWebSocketPushMessage msg = wsEvent.getMessage();
                if (msg instanceof PushMsg) {
                    this.socketMsg.setDefaultModelObject(wsEvent.getMessage().toString());
                    final MarkupContainer msgContainer = this.socketMsg.getParent();
                    msgContainer.add(new AttributeModifier("style", new Model<String>("display:block")));
                    wsEvent.getHandler().add(msgContainer);
                } else if (msg instanceof UIUserSession) {
                    final String sessId = ((UIUserSession) msg).getSessionId();
                    if (sessId.endsWith(getSession().getId())) {
                        getSession().invalidate();
                    }
                }
            }
        }
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
                .append("window.onresize = function(event) {\n")
                .append("var ").append(MainPage.WIDTH_PARAMETERNAME).append("=window.innerWidth;\n")
                .append("var ").append(MainPage.HEIGTH_PARAMETERNAME).append("=window.innerHeight;\n")
                .append(getCallbackFunctionBody(CallbackParameter.explicit(MainPage.WIDTH_PARAMETERNAME),
                                CallbackParameter.explicit(MainPage.HEIGTH_PARAMETERNAME)))
                .append("}\n");
            return js.toString();
        }

        @Override
        protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
        {
            super.updateAjaxAttributes(_attributes);
            _attributes.setThrottlingSettings(new ThrottlingSettings("mainThrottel", Duration.seconds(2), true));
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
            if (height.toString() != null) {
                final WebClientInfo asd = (WebClientInfo) Session.get().getClientInfo();
                asd.getProperties().setBrowserWidth(Integer.parseInt(width.toString()));
                asd.getProperties().setBrowserHeight(Integer.parseInt(height.toString()));
            }
        }

        @Override
        public void renderHead(final Component _component,
                               final IHeaderResponse _response)
        {
            _response.render(JavaScriptHeaderItem.forScript(getCallbackScript(), ResizeEventBehavior.class.getName()));
        }
    }
}
