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
package org.efaps.ui.wicket.pages.content;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.behaviors.KeepAliveBehavior;
import org.efaps.ui.wicket.behaviors.SetMessageStatusBehavior;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.footer.FooterPanel;
import org.efaps.ui.wicket.components.heading.HeadingPanel;
import org.efaps.ui.wicket.components.help.HelpLink;
import org.efaps.ui.wicket.components.menu.MenuBarPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIHeading;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UISearchItem;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
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
     * This instance variable contains a ModalWindow passed on by the
     * Constructor.
     */
    private final ModalWindowContainer modalWindow;

    /**
     * This instance variable contains the ModalWindow from this Page.
     */
    private final ModalWindowContainer modal = new ModalWindowContainer("modal");

    /**
     * Form used in this page.
     */
    private FormContainer form;

    /**
     * Reference to the page that opened this page.
     */
    private PageReference calledByPageReference;

    /**
     * Constructor.
     *
     * @param _model        model for this page
     */
    public AbstractContentPage(final IModel<?> _model)
    {
        this(_model, null);
    }


    /**
     * Constructor.
     *
     * @param _model        model for this page
     * @param _modalWindow  modal window
     */
    public AbstractContentPage(final IModel<?> _model,
                               final ModalWindowContainer _modalWindow)
    {
        super(_model);
        this.modalWindow = _modalWindow;
    }

    /**
     * Constructor.
     *
     * @param _model                    model for this page
     * @param _modalWindow              modal window
     * @param _calledByPageReference    Refernce to the Page opening this StructurBrowserPage
     */
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
    @SuppressWarnings("unchecked")
    protected void addComponents(final FormContainer _form)
        throws EFapsException
    {
        this.form = _form;
        add(new KeepAliveBehavior());

        // set the title for the Page
        add2Page(new Label("pageTitle", DBProperties.getProperty("Logo.Version.Label")));

        add(new SetMessageStatusBehavior());

        add(this.modal);

        final AbstractUIObject uiObject = (AbstractUIObject) super.getDefaultModelObject();
        add(new HeadingPanel("titel",  Model.of(new UIHeading(uiObject.getTitle()))));

        IModel<UIMenuItem> model = null;
        if (uiObject.getMode() == TargetMode.SEARCH
                        && uiObject.getCallingCommandUUID() != null
                        && uiObject instanceof UIForm) {
            model = Model.of(new UISearchItem(uiObject.getCallingCommand()
                                            .getTargetSearch().getUUID()));
        } else if (uiObject.getCommand().getTargetMenu() != null) {
            model = Model.of(new UIMenuItem(uiObject.getCommand().getTargetMenu()
                                            .getUUID(), uiObject.getInstanceKey()));
        }
        add(new MenuBarPanel("menu", model));
        add(new HelpLink("help", Model.of(uiObject.getCommand().getId())));
        final WebMarkupContainer footerpanel;
        if (uiObject.isCreateMode() || uiObject.isEditMode() || uiObject.isSearchMode()
                        || uiObject.isOpenedByPicker()) {
            footerpanel = new FooterPanel("footer", (IModel<ICmdUIObject>) getDefaultModel(), this.modalWindow);
        } else {
            footerpanel = new WebMarkupContainer("footer");
            footerpanel.setVisible(false);
        }
        add(footerpanel);
    }


    /**
     * Getter method for the instance variable {@link #form}.
     *
     * @return value of instance variable {@link #form}
     */
    public FormContainer getForm()
    {
        return this.form;
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
     * Getter method for the instance variable {@link #modalWindow}.
     *
     * @return value of instance variable {@link #modalWindow}
     */
    public ModalWindowContainer getModalWindow()
    {
        return this.modalWindow;
    }


    /**
     * Getter method for the instance variable {@link #updateMenu}.
     *
     * @return value of instance variable {@link #updateMenu}
     */
    public boolean isUpdateMenu()
    {
        return this.calledByPageReference != null
                        && this.calledByPageReference.getPage() instanceof ContentContainerPage;
    }

    /**
     * @return the value of the markup id attribute of the indicating element
     */
    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return "eFapsVeil";
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(AbstractContentPage.CSS));
    }

}
