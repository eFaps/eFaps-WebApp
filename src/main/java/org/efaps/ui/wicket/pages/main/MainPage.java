/*
 * Copyright 2003 - 2009 The eFaps Team
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
import org.apache.wicket.PageMap;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.AjaxCallThrottlingDecorator;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.JavascriptUtils;
import org.apache.wicket.util.time.Duration;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.message.MessageStatusHolder;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.ShowFileCallBackBehavior;
import org.efaps.ui.wicket.components.ChildCallBackHeaderContributer;
import org.efaps.ui.wicket.components.menu.MenuContainer;
import org.efaps.ui.wicket.components.menu.StandardLink;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.MenuItemModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.empty.EmptyPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
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
public class MainPage extends AbstractMergePage
{
    /**
     * this static variable contains the Key for the PageMap for the IFrame.
     */
    public static final String IFRAME_PAGEMAP_NAME = "MainPageIFramePageMap";

    /**
     * this static variable contains the id for the htmlFrame.
     */
    public static final String IFRAME_WICKETID = "content";

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
    private static String HEIGTHWIDTH_PARAMETERNAME = "eFapsWindowHeightWidth";

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
        // call the client info to force the relaod script to be executed on the beginning of a session,
        // if an ajax call would be doen as fisrt an error occurs
        Session.get().getClientInfo();
        // add the file call back used to open a file in the session and the main page
        final ShowFileCallBackBehavior fileCall = new ShowFileCallBackBehavior();
        this.add(fileCall);
        ((EFapsSession) getSession()).setFileCallBack(fileCall);

        // we need to add a JavaScript Function to resize the iFrame
        // don't merge it to keep the sequence
        this.add(StaticHeaderContributor.forJavaScript(MainPage.FRAMEJS, true));

        // set the title for the Page
        this.add(new StringHeaderContributor("<title>" + DBProperties.getProperty("Logo.Version.Label") + "</title>"));

        add(this.modal);
        this.modal.setPageMapName("modal");

        this.add(StaticHeaderContributor.forCss(MainPage.CSS));
        this.add(new ChildCallBackHeaderContributer());

        this.resize = new ResizeEventBehavior();

        final WebMarkupContainer logo = new WebMarkupContainer("logo");
        this.add(logo);
        final Label welcome = new Label("welcome", DBProperties.getProperty("Logo.Welcome.Label"));
        logo.add(welcome);
        welcome.add(this.resize);
        welcome.add(new HeaderContributor(new IHeaderContributor() {

            private static final long serialVersionUID = 1L;

            public void renderHead(final IHeaderResponse _response)
            {
                final CharSequence resizeScript = MainPage.this.resize.getCallbackScript();
                _response.renderString(JavascriptUtils.SCRIPT_OPEN_TAG + " window.onresize = " +  resizeScript + "; \n"
                                + "  window.onload = eFapsSetIFrameHeight; \n" + JavascriptUtils.SCRIPT_CLOSE_TAG);
            }
        }));

        try {
            final Context context = Context.getThreadContext();
            logo.add(new Label("firstname", context.getPerson().getFirstName()));
            logo.add(new Label("lastname", context.getPerson().getLastName()));
            final String companyName = context.getCompany() == null ? "" : context.getCompany().getName();
            logo.add(new Label("company", companyName));
            logo.add(new AttributeModifier("class", true,
                            new Model<String>("eFapsLogo " + companyName.replace(" " , ""))));

            //Admin_Common_SystemMessageAlert
            final StandardLink alert = new StandardLink("useralert",
                           new MenuItemModel(new UIMenuItem(UUID.fromString("5a6f2d4a-df81-4211-b7ed-18ae83608c81")))) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onRender(final MarkupStream _markupStream)
                {
                    renderComponent(_markupStream);
                }

                @Override
                protected void onComponentTag(final ComponentTag _tag)
                {
                    super.onComponentTag(_tag);
                }

                @Override
                protected void onComponentTagBody(final MarkupStream _markupStream,
                                                  final ComponentTag _openTag)
                {
                    super.onComponentTagBody(_markupStream, _openTag);
                    replaceComponentTagBody(_markupStream, _openTag, ((UIMenuItem) getDefaultModelObject()).getLabel());
                }


            };
            this.add(alert);
            if (MessageStatusHolder.hasUnreadMsg(context.getPerson().getId())) {
                alert.add(new AttributeModifier("class", true, new Model<String>("unread")));
            } else if (!MessageStatusHolder.hasReadMsg(context.getPerson().getId())) {
                alert.add(new AttributeModifier("style", true, new Model<String>("display:none")));
            }

        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }


        // add the MainToolBar to the Page
        final MenuContainer menu = new MenuContainer("menu", new MenuItemModel(new UIMenuItem(UUID
                        .fromString("87001cc3-c45c-44de-b8f1-776df507f268"))));
        this.add(menu);

        this.add(new Label("version", DBProperties.getProperty("Logo.Version.Label")));


        this.add(new InlineFrame(MainPage.IFRAME_WICKETID,
                                 PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME), EmptyPage.class));

        this.add(new InlineFrame("hidden", getPageMap(), EmptyPage.class));
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
    public class ResizeEventBehavior extends AjaxEventBehavior
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
            final StringBuilder ret = new StringBuilder();
            ret.append("function(){").append("eFapsSetIFrameHeight();").append(
                            generateCallbackScript("wicketAjaxPost('" + getCallbackUrl(false) + "','"
                                            + MainPage.HEIGTHWIDTH_PARAMETERNAME + "='"
                                            + "+window.innerWidth+\";\"+window.innerHeight ")).append("}\n");
            return ret.toString();
        }

        /** Overwritten to be deactivated.
         * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getPreconditionScript()
         * @return null
         */
        @Override
        protected CharSequence getPreconditionScript()
        {
            return null;
        }

        /**
         * Decorator for the call, so that the event is only fired one a
         * second.
         * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getAjaxCallDecorator()
         * @return CallDecorator
         */
        @Override
        protected IAjaxCallDecorator getAjaxCallDecorator()
        {
            return new AjaxCallThrottlingDecorator(getComponent().getMarkupId(), Duration.milliseconds(500));
        }

        /**
         * On event the actual size of the browser window is stored in the requestcycle.
         * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final String size = getComponent().getRequest().getParameter(MainPage.HEIGTHWIDTH_PARAMETERNAME);

            if (size != null) {
                final String[] sizes = size.split(";");
                final WebClientInfo asd = (WebClientInfo) getRequestCycle().getClientInfo();
                asd.getProperties().setBrowserWidth(Integer.parseInt(sizes[0]));
                asd.getProperties().setBrowserHeight(Integer.parseInt(sizes[1]));
            }
        }
    }
}
