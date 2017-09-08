/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.pages.main;

import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.IRequestListener;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.time.Duration;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.user.Role;
import org.efaps.api.ui.IEsjpSnipplet;
import org.efaps.api.ui.ILoginAlertProvider;
import org.efaps.db.Context;
import org.efaps.message.MessageStatusHolder;
import org.efaps.ui.wicket.behaviors.SetMessageStatusBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.behaviors.dojo.BorderContainerBehavior;
import org.efaps.ui.wicket.behaviors.dojo.BorderContainerBehavior.Design;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior.Region;
import org.efaps.ui.wicket.behaviors.dojo.MessageListenerBehavior;
import org.efaps.ui.wicket.behaviors.dojo.RequireBehavior;
import org.efaps.ui.wicket.components.LazyIframe;
import org.efaps.ui.wicket.components.LazyIframe.IFrameProvider;
import org.efaps.ui.wicket.components.help.ShowHelpBehavior;
import org.efaps.ui.wicket.components.menu.LinkItem;
import org.efaps.ui.wicket.components.menu.MenuBarPanel;
import org.efaps.ui.wicket.components.menu.SlideInPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.preloader.PreLoaderPanel;
import org.efaps.ui.wicket.components.search.SearchPanel;
import org.efaps.ui.wicket.connectionregistry.RegistryManager;
import org.efaps.ui.wicket.models.PushMsg;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UIUserSession;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.dashboard.DashboardPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.error.UnexpectedErrorPage;
import org.efaps.ui.wicket.pages.preferences.PreferencesPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsBaseException;
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
            final Role role = Role.get(KernelSettings.USER_ROLE_ADMINISTRATION);
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
        add(new OpenWindowOnLoadBehavior());
        add(new ShowHelpBehavior());
        // set the title for the Page
        add2Page(new Label("pageTitle", DBProperties.getProperty("Logo.Version.Label")));

        add(modal);
        add(new ResizeEventBehavior());

        try {
            // only add the search if it is activated in the kernel
            if (EFapsSystemConfiguration.get().getAttributeValueAsBoolean(KernelSettings.INDEXACTIVATE)) {
                final SearchPanel search = new SearchPanel("search");
                add(search);
            } else {
                add(new WebMarkupContainer("search").setVisible(false));
            }
        } catch (final EFapsException e1) {
            MainPage.LOG.error("Error on retrieving setting for index", e1);
        }

        final boolean slidein = Configuration.getAttribute(ConfigAttribute.MAINMENU).equalsIgnoreCase("slidein");

        final WebMarkupContainer borderPanel = new WebMarkupContainer("borderPanel");
        this.add(borderPanel);
        borderPanel.add(new BorderContainerBehavior(slidein ? Design.SIDEBAR : Design.HEADLINE, false));

        final LazyIframe mainPanel = new LazyIframe("mainPanel", new IFrameProvider()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public Page getPage(final Component _component)
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
        }, MainPage.IFRAME_ID);

        borderPanel.add(mainPanel);
        mainPanel.add(new ContentPaneBehavior(Region.CENTER, slidein));
        try {
            final Context context = Context.getThreadContext();
            if (slidein) {
                borderPanel.add(new WebMarkupContainer("headerPanel").setVisible(false));

                final WebMarkupContainer slideinPane = new WebMarkupContainer("slideinPane");
                slideinPane.add(new ContentPaneBehavior(Region.LEADING, false)
                                .setLayoutContainer(true)
                                .setWidth("200px"));
                borderPanel.add(slideinPane);

                final WebMarkupContainer slideinHeaderPanel = new WebMarkupContainer("slideinHeaderPane");
                slideinHeaderPanel.add(new ContentPaneBehavior(Region.TOP, false));
                slideinPane.add(slideinHeaderPanel);

                final WebMarkupContainer slideinContentPane = new WebMarkupContainer("slideinContentPane");
                slideinContentPane.add(new ContentPaneBehavior(Region.CENTER, false));
                slideinPane.add(slideinContentPane);

                slideinContentPane.add(new SlideInPanel("slideInPanel", Model.of(new UIMenuItem(UUID
                                .fromString(Configuration.getAttribute(ConfigAttribute.TOOLBAR))))));

                final WebMarkupContainer slideinFooterPane = new WebMarkupContainer("slideinFooterPane");
                slideinFooterPane.add(new ContentPaneBehavior(Region.BOTTOM, false));
                slideinPane.add(slideinFooterPane);

            } else {
                borderPanel.add(new WebMarkupContainer("slideinPane").setVisible(false));
                final WebMarkupContainer headerPanel = new WebMarkupContainer("headerPanel");
                borderPanel.add(headerPanel);
                headerPanel.add(new ContentPaneBehavior(Region.TOP, false));

                headerPanel.add(new MenuBarPanel("menubar", Model.of(new UIMenuItem(UUID
                          .fromString(Configuration.getAttribute(ConfigAttribute.TOOLBAR))))));
                final WebMarkupContainer logo = new WebMarkupContainer("logo");
                headerPanel.add(logo);
                final Label welcome = new Label("welcome", DBProperties.getProperty("Logo.Welcome.Label"));
                logo.add(welcome);

                final Label userNameLabel = new Label("userName", String.format("%s %s",
                    context.getPerson().getFirstName(), context.getPerson().getLastName()));
                userNameLabel.setMarkupId("eFapsUserName");
                userNameLabel.add(new LoadPreferenceBehavior());
                logo.add(userNameLabel);

                final String companyName = context.getCompany() == null ? "" : context.getCompany().getName();
                logo.add(new Label("company", companyName));
                logo.add(new AttributeModifier("class",
                                new Model<>("eFapsLogo " + companyName.replaceAll("\\W", ""))));
            }

            final long usrId = context.getPersonId();
            // Admin_Common_SystemMessageAlert
            final LinkItem alert = new LinkItem("useralert",
                            Model.of(new UIMenuItem(SetMessageStatusBehavior.getCmdUUD())))
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
            alert.add(new AttributeModifier("class", new Model<>("eFapsUserMsg")));
            if (!MessageStatusHolder.hasReadMsg(usrId)) {
                alert.add(new AttributeModifier("style", new Model<>("display:none")));
            }
            final WebMarkupContainer socketMsgContainer = new WebMarkupContainer("socketMsgContainer");
            add(socketMsgContainer);
            if (Configuration.getAttributeAsBoolean(ConfigAttribute.WEBSOCKET_ACTVATE)) {
                socketMsgContainer.setOutputMarkupPlaceholderTag(true);
                socketMsg = new Label("socketMsg", "none yet").setEscapeModelStrings(false);
                socketMsg.setOutputMarkupPlaceholderTag(true);
                socketMsg.add(new WebSocketBehavior()
                {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onConnect(final ConnectedMessage _message)
                    {
                        RegistryManager.addMsgConnection(_message.getSessionId(), _message.getKey());
                    }
                });
                socketMsgContainer.add(socketMsg);

                final AjaxLink<Void> close = new AjaxLink<Void>("socketMsgClose") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(final AjaxRequestTarget _target)
                    {
                        final MarkupContainer msgContainer = socketMsg.getParent();
                        msgContainer.add(new AttributeModifier("style", new Model<>("display:none")));
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
        js.append("  if (top.location != location) {\n")
            .append("    top.location.href = document.location.href;\n")
            .append("  }\n");
        _response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forScript(js, MainPage.class.getName())));
    }

    /**
     * Method to get the ModalWindow of this Page.
     *
     * @return modal window
     */
    public final ModalWindowContainer getModal()
    {
        return modal;
    }

    @Override
    public void onEvent(final IEvent<?> _event)
    {
        if (_event.getPayload() instanceof WebSocketPushPayload) {
            final WebSocketPushPayload wsEvent = (WebSocketPushPayload) _event.getPayload();
            if (wsEvent != null) {
                final IWebSocketPushMessage msg = wsEvent.getMessage();
                if (msg instanceof PushMsg) {
                    socketMsg.setDefaultModelObject(wsEvent.getMessage().toString());
                    final MarkupContainer msgContainer = socketMsg.getParent();
                    msgContainer.add(new AttributeModifier("style", new Model<>("display:block")));
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
            super("resize");
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
                if (!width.toString().isEmpty()) {
                    asd.getProperties().setBrowserWidth(Integer.parseInt(width.toString()));
                }
                if (!height.toString().isEmpty()) {
                    asd.getProperties().setBrowserHeight(Integer.parseInt(height.toString()));
                }
            }
        }

        @Override
        public void renderHead(final Component _component,
                               final IHeaderResponse _response)
        {
            _response.render(JavaScriptHeaderItem.forScript(getCallbackScript(), ResizeEventBehavior.class.getName()));
        }
    }

    /**
     * The Class OpenWindowOnLoadBehavior.
     *
     * @author The eFaps Team
     */
    public class OpenWindowOnLoadBehavior
        extends AbstractDefaultAjaxBehavior
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The esjp. */
        private IEsjpSnipplet esjpSnipplet;

        /** The provider. */
        private ILoginAlertProvider provider;

        @Override
        protected void onBind()
        {
            super.onBind();
            final String providerClass = Configuration.getAttribute(ConfigAttribute.LOGINALERT_PROVIDER);
            if (providerClass != null) {
                final Class<?> clazz;
                try {
                    clazz = Class.forName(providerClass, false, EFapsClassLoader.getInstance());
                    provider = (ILoginAlertProvider) clazz.newInstance();
                    esjpSnipplet = provider.getEsjpSnipplet("LoginAlert");
                } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    MainPage.LOG.error("Could not find/instantiate Provider Class", e);
                }
            }
        }

        @Override
        protected void respond(final AjaxRequestTarget _target)
        {
            final ModalWindowContainer modalTmp = getModal();
            modalTmp.setPageCreator(new ModalWindow.PageCreator()
            {

                private static final long serialVersionUID = 1L;

                @Override
                public Page createPage()
                {
                    Page page;
                    try {
                        page = new AlertPage(Model.of(esjpSnipplet.getHtmlSnipplet()
                                        .toString()));
                    } catch (final EFapsBaseException e) {
                        MainPage.LOG.error("Catched error.", e);
                        page = new UnexpectedErrorPage();
                    }
                    return page;
                }
            });
            modalTmp.setWindowClosedCallback(new ModalWindow.WindowClosedCallback()
            {

                /** The Constant serialVersionUID. */
                private static final long serialVersionUID = 1L;

                @Override
                public void onClose(final AjaxRequestTarget _target)
                {
                    provider.onClose();
                }
            });

            modalTmp.show(_target);
        }

        @Override
        public void renderHead(final Component _component,
                               final IHeaderResponse _response)
        {
            try {
                if (esjpSnipplet != null && esjpSnipplet.isVisible()) {
                    _response.render(OnDomReadyHeaderItem.forScript(getCallbackScript()));
                }
            } catch (final EFapsBaseException e) {
                MainPage.LOG.error("Catched error.", e);
            }
        }
    }

    /**
     * The Class LoadFormBehavior.
     *
     */
    public static class LoadPreferenceBehavior
        extends AbstractDojoBehavior
        implements IRequestListener
    {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public void renderHead(final Component _component,
                               final IHeaderResponse _response)
        {
            super.renderHead(_component, _response);
            final StringBuilder js = new StringBuilder()
                .append("ready(function() {\n")
                .append("var myTooltipDialog = new TooltipDialog({\n")
                .append("  style: 'width: 300px;',\n")
                .append("  href: '").append(_component.urlForListener(this, new PageParameters())).append("',\n")
                .append("});\n")
                .append(" on(dom.byId(\"").append(_component.getMarkupId(true)).append("\"), \"click\", function(evt){")
                .append("popup.open({\n")
                .append("  popup: myTooltipDialog,\n")
                .append("  orient: [\"below-centered\", \"above-centered\"],\n")
                .append("  around: dom.byId('eFapsUserName')\n")
                .append("});\n")
                .append("});")
                .append("});");

            _response.render(JavaScriptHeaderItem.forScript(DojoWrapper.require(js, DojoClasses.ready,
                            DojoClasses.registry, DojoClasses.dom, DojoClasses.domConstruct, DojoClasses.on,
                            DojoClasses.TooltipDialog, DojoClasses.popup),
                            _component.getMarkupId() + "-Script"));
        }

        @Override
        public void onRequest()
        {
            RequestCycle.get().setResponsePage(new PreferencesPage());
        }
    }
}
