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
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.models.objects.IWizardElement;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class renders the Footer under a WebForm or WebTable.<br>
 * It provides also the necessary links to initialize the necessary actions of
 * the Footer like submit, cancel and so on.
 *
 * @author The eFaps Team
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
     * @throws EFapsException on error
     */
    public FooterPanel(final String _wicketId,
                       final IModel<ICmdUIObject> _model,
                       final ModalWindowContainer _modalWindow)
        throws EFapsException
    {
        super(_wicketId, _model);
        this.modalWindow = _modalWindow;

        final ICmdUIObject cmdUIObject = (ICmdUIObject) super.getDefaultModelObject();

        // if we want a SucessDialog we add it here, it will be opened after
        // closing the window
        if ("true".equals(cmdUIObject.getCommand().getProperty("SuccessDialog"))) {
            FooterPanel.this.modalWindow.setWindowClosedCallback(new WindowClosedCallback()
            {

                private static final long serialVersionUID = 1L;

                @Override
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

                            @Override
                            public Page createPage()
                            {
                                Page ret = null;

                                try {
                                    ret = new DialogPage(((AbstractContentPage) getPage()).getPageReference(),
                                                    cmdUIObject.getCommand().getName()
                                                                    + ".Success", false, false);
                                } catch (final EFapsException e) {
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
        if (cmdUIObject.getCommand().getTargetCommand() != null) {
            label = DBProperties.getProperty(cmdUIObject.getCommand().getTargetCommand().getName() + ".Label");
        } else {
            switch (cmdUIObject.getCommand().getTargetMode()) {
                case CREATE:
                    label = getLabel(cmdUIObject, "Create");
                    break;
                case EDIT:
                    label = getLabel(cmdUIObject, "Edit");
                    break;
                case SEARCH:
                    label = getLabel(cmdUIObject, "Search");
                    break;
                case CONNECT:
                    label = getLabel(cmdUIObject, "Connect");
                    break;
                default:
                    break;
            }
        }
        boolean prevAdded = false;
        // star tof a new wisard call
        if (cmdUIObject.getCommand().getTargetCommand() != null) {
            add(new AjaxSubmitCloseButton("createeditsearch", _model, Button.ICON.NEXT.getReference(), label));
        } else if (cmdUIObject instanceof IWizardElement && ((IWizardElement) cmdUIObject).isWizardCall()) {
            // this is searchmode: on first call show form, on second show table
            final IWizardElement first = ((IWizardElement) cmdUIObject).getUIWizardObject().getWizardElement().get(0);
            if (TargetMode.SEARCH.equals(((ICmdUIObject) first).getCallingCommand().getTargetMode())) {
                add(new WebMarkupContainer("createeditsearch").setVisible(false));
            } else {
                add(new AjaxSubmitCloseButton("createeditsearch", _model, Button.ICON.ACCEPT.getReference(),
                                getLabel(cmdUIObject, "Connect")));
                if (cmdUIObject instanceof UIGrid) {
                    ((UIGrid) cmdUIObject).setShowCheckBoxes(true);
                }
            }
        } else if ((cmdUIObject.getCommand().isSubmit() && cmdUIObject instanceof UITable) || !TargetMode.SEARCH.equals(
                        cmdUIObject.getCommand().getTargetMode())) {
            add(new AjaxSubmitCloseButton("createeditsearch", _model, Button.ICON.ACCEPT.getReference(), label));
        } else if (TargetMode.SEARCH.equals(cmdUIObject.getCommand().getTargetMode())
                        && cmdUIObject.getCommand().getTargetForm() != null) {
            add(new AjaxSearchSubmitButton("createeditsearch", _model, Button.ICON.NEXT.getReference(), label));
        } else {
            closelabelkey = "Close";
            label = getLabel(cmdUIObject, "Revise");
            add(new AjaxReviseButton("createeditsearch", _model, Button.ICON.PREVIOUS.getReference(), label));
            prevAdded = true;
        }

        if (_modalWindow == null) {
            add(new ClosePopUpButton("cancel", _model, Button.ICON.CANCEL.getReference(), getLabel(cmdUIObject,
                            closelabelkey)));
        } else {
            add(new AjaxCancelButton("cancel", _model, Button.ICON.CANCEL.getReference(), getLabel(cmdUIObject,
                            closelabelkey)));
        }

        if (cmdUIObject instanceof IWizardElement && ((IWizardElement) cmdUIObject).isWizardCall() && cmdUIObject
                        .getCommand().isTargetCmdRevise() && !prevAdded) {
            label = getLabel(cmdUIObject, "Revise");
            add(new AjaxReviseButton("prev", _model, Button.ICON.PREVIOUS.getReference(), label));
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
     * @param _cmdObject the cmd object
     * @param _keytype what Label should be searched
     * @return if found DBProperty of the CommandAbstract, else a Default
     * @throws EFapsException on error
     */
    private String getLabel(final ICmdUIObject _cmdObject,
                            final String _keytype)
        throws EFapsException
    {
        final String ret;
        if (DBProperties.hasProperty(_cmdObject.getCommand().getName()  + ".Button." + _keytype)) {
            ret = DBProperties.getProperty(_cmdObject.getCommand().getName() + ".Button." + _keytype);
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
