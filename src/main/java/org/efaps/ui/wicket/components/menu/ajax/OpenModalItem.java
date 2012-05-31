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

package org.efaps.ui.wicket.components.menu.ajax;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowAjaxPageCreator;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.util.ParameterUtil;

/**
 * Class is used as a link inside the JSCookMenu that opens a modal window.
 *
 * @author The eFaps Team
 * @version $Id:AjaxOpenModalComponent.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class OpenModalItem
    extends AbstractItem
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     * @param _form form in case of a submit
     */
    public OpenModalItem(final String _wicketId,
                         final IModel<UIMenuItem> _model)
    {
        super(_wicketId, _model);
        if (_model.getObject().isSubmit()) {
            add(new SubmitAndOpenModalBehavior());
        } else {
            add(new OpenModalBehavior());
        }
    }

    /**
     * /** Class is used to execute the opening of the modal window.
     *
     *
     */
    public class OpenModalBehavior
        extends AbstractItemBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public OpenModalBehavior()
        {
            super("onclick");
        }

        /**
         * Show the modal window.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            ModalWindowContainer modal;
            if (getPage() instanceof MainPage) {
                modal = ((MainPage) getPage()).getModal();
            } else {
                modal = ((AbstractContentPage) getPage()).getModal();
            }
            modal.reset();
            final ModalWindowAjaxPageCreator pageCreator = new ModalWindowAjaxPageCreator((UIMenuItem) super
                            .getComponent().getDefaultModelObject(), modal);
            modal.setPageCreator(pageCreator);
            modal.setInitialHeight(((UIMenuItem) getDefaultModelObject()).getWindowHeight());
            modal.setInitialWidth(((UIMenuItem) getDefaultModelObject()).getWindowWidth());
            modal.show(_target);
        }
    }

    /**
     * Open a modal window and submit the values.
     */
    public class SubmitAndOpenModalBehavior
        extends AbstractSubmitBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _form form
         */
        public SubmitAndOpenModalBehavior()
        {
            super( "onClick");
        }

        /**
         * Open the modal window.
         *
         * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onSubmit(final AjaxRequestTarget _target)
        {
            final UIMenuItem uiMenuItem = (UIMenuItem) super.getComponent().getDefaultModelObject();

            final IRequestParameters para = getRequest().getRequestParameters();
            boolean check = false;
            if (uiMenuItem.getSubmitSelectedRows() > -1) {
                final String[] oids = ParameterUtil.parameter2Array(para, "selectedRow");
                if (uiMenuItem.getSubmitSelectedRows() > 0) {
                    check = oids == null ? false : oids.length == uiMenuItem.getSubmitSelectedRows();
                } else {
                    check = oids == null ? false : oids.length > 0;
                }
            } else {
                check = true;
            }
            final ModalWindowContainer modal;
            if (getPage() instanceof MainPage) {
                modal = ((MainPage) getPage()).getModal();
            } else {
                modal = ((AbstractContentPage) getPage()).getModal();
            }
            if (check) {
                modal.reset();
                final ModalWindowAjaxPageCreator pageCreator = new ModalWindowAjaxPageCreator((UIMenuItem) super
                                .getComponent().getDefaultModelObject(), modal);
                modal.setPageCreator(pageCreator);
                modal.setInitialHeight(((UIMenuItem) getDefaultModelObject()).getWindowHeight());
                modal.setInitialWidth(((UIMenuItem) getDefaultModelObject()).getWindowWidth());
                modal.show(_target);
            } else {
                modal.setPageCreator(new ModalWindow.PageCreator()
                {
                    private static final long serialVersionUID = 1L;

                    public Page createPage()
                    {
                        return new DialogPage(modal.getPage().getPageReference(),
                                        "SubmitSelectedRows.fail" + uiMenuItem.getSubmitSelectedRows(),
                                        false, null);
                    }
                });
                modal.setInitialHeight(150);
                modal.setInitialWidth(350);
                modal.show(_target);
            }
        }
    }
}
