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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.footer;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class renders the Footer under a WebForm or WebTable.<br>
 * It provides also the necessary links to initialize the necessary actions of
 * the Footer like submit, cancel and so on.
 *
 * @author The eFaps Team
 * @version $Id:FooterPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class FooterPanel
    extends Panel
{

    /**
     * Reference to the stylesheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(FooterPanel.class, "FooterPanel.css");

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FooterPanel.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = -1722339596237748160L;

    /**
     * This instance variable stores the ModalWindowContainer the Page and with
     * it this footer was opened in, to have acess to it, for actions like
     * closing the ModalWindow.
     */
    private final ModalWindowContainer modalWindow;

    /**
     * Stores if the update was successful.
     */
    private boolean success;

    /**
     * Constructor for the FooterPanel.
     *
     * @param _wicketId wicket id of the Component
     * @param _model Model of the Component
     * @param _modalWindow ModalWindowContainer containing this FooterPanel
     * @param _form FormContainer of the Page (needed to submit the Form)
     * @throws CacheReloadException on error
     */
    public FooterPanel(final String _wicketId,
                       final IModel<?> _model,
                       final ModalWindowContainer _modalWindow,
                       final FormContainer _form)
        throws CacheReloadException
    {
        super(_wicketId, _model);
        this.modalWindow = _modalWindow;

        final AbstractUIPageObject uiObject = (AbstractUIPageObject) super.getDefaultModelObject();

        // if we want a SucessDialog we add it here, it will be opened after
        // closing the window
        if ("true".equals(uiObject.getCommand().getProperty("SuccessDialog"))) {
            FooterPanel.this.modalWindow.setWindowClosedCallback(new WindowClosedCallback()
            {

                private static final long serialVersionUID = 1L;

                public void onClose(final AjaxRequestTarget _target)
                {
                    if (FooterPanel.this.success) {
                        FooterPanel.this.modalWindow.setResizable(false);
                        FooterPanel.this.modalWindow.setInitialWidth(20);
                        FooterPanel.this.modalWindow.setInitialHeight(12);
                        FooterPanel.this.modalWindow.setWidthUnit("em");
                        FooterPanel.this.modalWindow.setHeightUnit("em");

                        FooterPanel.this.modalWindow.setPageCreator(new ModalWindow.PageCreator()
                        {

                            private static final long serialVersionUID = 1L;

                            public Page createPage()
                            {
                                Page ret = null;

                                try {
                                    ret = new DialogPage(((AbstractContentPage) getPage()).getPageReference(),
                                                    uiObject.getCommand().getName()
                                                                    + ".Success", false, false);
                                } catch (final CacheReloadException e) {
                                    FooterPanel.LOG.error("Error on instanciation of page", e);
                                }

                                return ret;
                            }
                        });

                        FooterPanel.this.modalWindow.show(_target);
                        FooterPanel.this.success = false;
                    }
                }
            });
        }

        String label = null;
        String closelabelkey = "Cancel";
        if (uiObject.hasTargetCmd()) {
            label = DBProperties.getProperty(uiObject.getTargetCmd().getName() + ".Label");
        } else if (uiObject.isCreateMode()) {
            label = getLabel(uiObject.getCommand().getName(), "Create");
        } else if (uiObject.isEditMode()) {
            label = getLabel(uiObject.getCommand().getName(), "Edit");
        } else if (uiObject.isSubmit() && uiObject instanceof UITable) {
            label = getLabel(uiObject.getCommand().getName(), "Connect");
        } else if (uiObject.isSearchMode()) {
            label = getLabel(uiObject.getCommand().getName(), "Search");
        } else if (uiObject.isPicker()) {
            label = getLabel(uiObject.getCommand().getName(), "Picker");
        }

        if (uiObject.hasTargetCmd()) {
            final Button button = new Button("createeditsearch",
                            new AjaxSubmitCloseLink(Button.LINKID, uiObject, _form),
                            label, Button.ICON.NEXT.getReference());
            this.add(button);
        } else if ((uiObject.isSubmit() && uiObject instanceof UITable) || !uiObject.isSearchMode()) {
            final Button button = new Button("createeditsearch",
                            new AjaxSubmitCloseLink(Button.LINKID, uiObject, _form),
                            label, Button.ICON.ACCEPT.getReference());
            this.add(button);
        } else if (uiObject.isSearchMode() && uiObject instanceof UIForm) {
            final Button button = new Button("createeditsearch", new SearchSubmitLink(Button.LINKID, _model, _form),
                            label, Button.ICON.NEXT.getReference());
            this.add(button);
        } else {
            closelabelkey = "Close";
            label = getLabel(uiObject.getCommand().getName(), "Revise");
            final Button button = new Button("createeditsearch", new AjaxReviseLink(Button.LINKID, uiObject),
                            label, Button.ICON.PREVIOUS.getReference());
            add(button);
        }

        if (_modalWindow == null) {
            add(new Button("cancel", new ClosePopUpLink(Button.LINKID, uiObject), getLabel(uiObject.getCommand()
                            .getName(), closelabelkey), Button.ICON.CANCEL.getReference()));
        } else {
            add(new Button("cancel", new AjaxCancelLink(Button.LINKID), getLabel(uiObject.getCommand().getName(),
                            closelabelkey), Button.ICON.CANCEL.getReference()));
        }

        if (uiObject.isPartOfWizardCall() && uiObject.isRenderRevise()) {
            label = getLabel(uiObject.getCommand().getName(), "Revise");
            final Button prev = new Button("prev", new AjaxReviseLink(Button.LINKID, uiObject),
                            label, Button.ICON.PREVIOUS.getReference());
            this.add(prev);
        } else {
            add(new WebMarkupContainer("prev").setVisible(false));
        }
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */
    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(FooterPanel.CSS));
    }

    /**
     * Method that searches a DBProperty for the Label.
     *
     * @param _cmdName Name of the CommandAbstract the Label should be searched
     *            for
     * @param _keytype what Label should be searched
     * @return if found DBProperty of the CommandAbstract, else a Default
     */
    private String getLabel(final String _cmdName,
                            final String _keytype)
    {
        String ret;
        if (DBProperties.hasProperty(_cmdName + ".Button." + _keytype)) {
            ret = DBProperties.getProperty(_cmdName + ".Button." + _keytype);
        } else {
            ret = DBProperties.getProperty("default.Button." + _keytype);
        }
        return ret;
    }

    /**
     * This is the getter method for the instance variable {@link #modalWindow}.
     *
     * @return value of instance variable {@link #modalWindow}
     */
    public ModalWindowContainer getModalWindow()
    {
        return this.modalWindow;
    }

    /**
     * This is the getter method for the instance variable {@link #success}.
     *
     * @return value of instance variable {@link #success}
     */
    public boolean isSuccess()
    {
        return this.success;
    }

    /**
     * This is the setter method for the instance variable {@link #success}.
     *
     * @param _success the success to set
     */
    public void setSuccess(final boolean _success)
    {
        this.success = _success;
    }
}
