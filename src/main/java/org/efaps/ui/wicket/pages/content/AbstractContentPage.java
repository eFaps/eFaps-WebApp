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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.Opener;
import org.efaps.ui.wicket.behaviors.SetMessageStatusBehavior;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.footer.FooterPanel;
import org.efaps.ui.wicket.components.heading.HeadingPanel;
import org.efaps.ui.wicket.components.menu.MenuBarPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UISearchItem;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.util.EFapsException;

/**
 * Abstract Class that renders the Content<br/>
 * It adds the Menu, Header and Footer to the Page.
 *
 * @author The eFaps Team
 * @version $Id:AbstractContentPage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public abstract class AbstractContentPage
    extends AbstractMergePage
    implements IAjaxIndicatorAware
{

    /**
     * Reference to the StyleSheet of this Page stored in the eFaps-DataBase.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(AbstractContentPage.class,
                    "AbstractContentPage.css");
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = -2374207555009145191L;

    /**
     * Variable contains the key to the MenuTree.
     */
    private String menuTreeKey;

    /**
     * This instance variable contains a ModalWindow passed on by the
     * Constructor.
     */
    private final ModalWindowContainer modalWindow;

    /**
     * This instance variable contains the ModalWindow from this Page.
     */
    private final ModalWindowContainer modal = new ModalWindowContainer("modal");
    private boolean updateMenu = false;

    private PageReference calledByPageReference;





    /**
     * Constructor.
     *
     * @param _model model for this page
     */
    public AbstractContentPage(final IModel<?> _model,
                               final boolean _updateMenu)
    {
        this(_model, null, _updateMenu);
    }

    /**
     * Constructor.
     *
     * @param _model model for this page
     * @param _modalWindow modal window
     */
    public AbstractContentPage(final IModel<?> _model,
                               final ModalWindowContainer _modalWindow,
                               final boolean _updateMenu)
    {
        super(_model);
        this.modalWindow = _modalWindow;
        this.updateMenu = _updateMenu;
    }


    public AbstractContentPage(final IModel<?> _model,
                               final ModalWindowContainer _modalWindow,
                               final PageReference _calledByPageReference)
    {
        super(_model);
        this.modalWindow = _modalWindow;
        this.calledByPageReference = _calledByPageReference;
    }


    /**
     * Getter method for the instance variable {@link #calledByPageReference}.
     *
     * @return value of instance variable {@link #calledByPageReference}
     */
    public PageReference getCalledByPageReference()
    {
        return this.calledByPageReference;
    }


    /**
     * Setter method for instance variable {@link #calledByPageReference}.
     *
     * @param _calledByPageReference value for instance variable {@link #calledByPageReference}
     */

    public void setCalledByPageReference(final PageReference _calledByPageReference)
    {
        this.calledByPageReference = _calledByPageReference;
    }

    /**
     * Method that adds the Components to the Page.
     *
     * @param _form FormContainer
     * @throws EFapsException on error
     */
    protected void addComponents(final FormContainer _form)
        throws EFapsException
    {
        // set the title for the Page
        add(new Label("pageTitle", DBProperties.getProperty("Logo.Version.Label")));

        add(StaticHeaderContrBehavior.forCss(AbstractContentPage.CSS));
        add(new SetMessageStatusBehavior());

        add(this.modal);

        final AbstractUIObject uiObject = (AbstractUIObject) super.getDefaultModelObject();
        add(new HeadingPanel("titel", uiObject.getTitle()));

        UIModel<UIMenuItem> model = null;
        if (uiObject.getMode() == TargetMode.SEARCH
                        && uiObject.getCallingCommandUUID() != null) {
            model = new UIModel<UIMenuItem>(new UISearchItem(uiObject.getCallingCommand()
                                            .getTargetSearch().getUUID()));
        } else if (uiObject.getCommand().getTargetMenu() != null) {
            model = new UIModel<UIMenuItem>(new UIMenuItem(uiObject.getCommand().getTargetMenu()
                                            .getUUID(), uiObject.getInstanceKey()));
        }
        add(new MenuBarPanel("menu", model));
        WebMarkupContainer exLink;
        if (((AbstractUIPageObject) super.getDefaultModelObject()).getHelpTarget() != null) {
            final PopupSettings set = new PopupSettings(PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS
                            | PopupSettings.MENU_BAR | PopupSettings.LOCATION_BAR | PopupSettings.STATUS_BAR
                            | PopupSettings.TOOL_BAR);
            exLink = new ExternalLink("help",
                            "/servlet/help/" + ((AbstractUIPageObject) super.getDefaultModelObject()).getHelpTarget(),
                            DBProperties.getProperty("org.efaps.ui.wicket.pages.content.AbstractContentPage.HelpLink"))
                            .setPopupSettings(set).setContextRelative(true);

            exLink.add(AttributeModifier.append("class", "eFapsHelpLink"));
            if (true) {
                exLink.add(AttributeModifier.append("class", " eFapsHelpMainLink"));
            }
        } else {
            exLink = new WebMarkupContainer("help");
            exLink.setVisible(false);
        }
        add(exLink);
        WebMarkupContainer footerpanel;
        if (uiObject.isCreateMode() || uiObject.isEditMode() || uiObject.isSearchMode() || uiObject.isPicker()) {
            footerpanel = new FooterPanel("footer", getDefaultModel(), this.modalWindow, _form);
        } else {
            footerpanel = new WebMarkupContainer("footer");
            footerpanel.setVisible(false);
        }
        add(footerpanel);
    }

    /**
     * This is the getter method for the instance variable {@link #modal}.
     *
     * @return value of instance variable {@link #modal}
     */
    public ModalWindowContainer getModal()
    {
        return this.modal;
    }

    /**
     * Getter method for the instance variable {@link #updateMenu}.
     *
     * @return value of instance variable {@link #updateMenu}
     */
    public boolean isUpdateMenu()
    {
        return this.updateMenu;
    }

    /**
     * This is the getter method for the instance variable {@link #menuTreeKey}.
     *
     * @return value of instance variable {@link #menuTreeKey}
     */

    public String getMenuTreeKey()
    {
        if (this.menuTreeKey == null) {
            this.menuTreeKey = ((AbstractUIObject) getDefaultModelObject()).getMenuTreeKey();
        }
        return this.menuTreeKey;
    }

    /**
     * This is the setter method for the instance variable {@link #menuTreeKey}.
     *
     * @param _menuTreeKey the listMenuName to set
     * @return this
     */
    public AbstractContentPage setMenuTreeKey(final String _menuTreeKey)
    {
        this.menuTreeKey = _menuTreeKey;
        return this;
    }

    /**
     * After the page is rendered, it is checked if the a opener exists, If it
     * exists and it is marked for remove it is removed from the session.
     *
     */
    @Override
    protected void onAfterRender()
    {
        super.onAfterRender();
        if (((AbstractUIObject) getDefaultModelObject()).getOpenerId() != null) {
            final String openerId = ((AbstractUIObject) getDefaultModelObject()).getOpenerId();
            final Opener opener = ((EFapsSession) getSession()).getOpener(openerId);
            if (opener != null && opener.isMarked4Remove()) {
                ((EFapsSession) getSession()).removeOpener(openerId);
            }
        }
    }

    /**
     * @return the value of the markup id attribute of the indicating element
     */
    public String getAjaxIndicatorMarkupId()
    {
        return "eFapsVeil";
    }


    /* (non-Javadoc)
     * @see org.apache.wicket.Component#onEvent(org.apache.wicket.event.IEvent)
     */
    @Override
    public void onEvent(final IEvent<?> _event)
    {
        // TODO Auto-generated method stub
        super.onEvent(_event);
    }
}
