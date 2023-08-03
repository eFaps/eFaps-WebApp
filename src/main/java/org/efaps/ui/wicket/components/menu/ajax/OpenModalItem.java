/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.ui.wicket.components.menu.ajax;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestParameters;
import org.efaps.ui.wicket.components.modalwindow.LegacyModalWindow;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowAjaxPageCreator;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.IPageObject;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.util.ParameterUtil;

/**
 * Class is used as a link inside the JSCookMenu that opens a modal window.
 *
 * @author The eFaps Team
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
            super("click");
        }

        /**
         * Show the modal window.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final ModalWindowContainer modal;
            final PagePosition pagePosition;
            if (getPage() instanceof MainPage) {
                modal = ((MainPage) getPage()).getModal();
                pagePosition = PagePosition.CONTENTMODAL;
            } else {
                modal = ((AbstractContentPage) getPage()).getModal();
                if (getPage().getDefaultModelObject() instanceof IPageObject) {
                    switch (((IPageObject) getPage().getDefaultModelObject()).getPagePosition()) {
                        case TREE:
                            pagePosition = PagePosition.TREEMODAL;
                            break;
                        case CONTENT:
                        default:
                            pagePosition = PagePosition.CONTENTMODAL;
                            break;
                    }
                } else {
                    pagePosition = PagePosition.TREEMODAL;
                }
            }
            modal.reset();
            final ModalWindowAjaxPageCreator pageCreator = new ModalWindowAjaxPageCreator((UIMenuItem) super
                            .getComponent().getDefaultModelObject(), modal, pagePosition);
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
         *
         */
        public SubmitAndOpenModalBehavior()
        {
            super("click");
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
            final PagePosition pagePosition;
            if (getPage() instanceof MainPage) {
                modal = ((MainPage) getPage()).getModal();
                pagePosition = PagePosition.CONTENTMODAL;
            } else {
                modal = ((AbstractContentPage) getPage()).getModal();
                if (getPage().getDefaultModelObject() instanceof IPageObject) {
                    switch (((IPageObject) getPage().getDefaultModelObject()).getPagePosition()) {
                        case TREE:
                            pagePosition = PagePosition.TREEMODAL;
                            break;
                        case CONTENT:
                        default:
                            pagePosition = PagePosition.CONTENTMODAL;
                            break;
                    }
                } else {
                    pagePosition = PagePosition.TREEMODAL;
                }
            }
            if (check) {
                modal.reset();
                final ModalWindowAjaxPageCreator pageCreator = new ModalWindowAjaxPageCreator((UIMenuItem) super
                                .getComponent().getDefaultModelObject(), modal, pagePosition);
                modal.setPageCreator(pageCreator);
                modal.setInitialHeight(((UIMenuItem) getDefaultModelObject()).getWindowHeight());
                modal.setInitialWidth(((UIMenuItem) getDefaultModelObject()).getWindowWidth());
                modal.show(_target);
            } else {
                modal.setPageCreator(new LegacyModalWindow.PageCreator()
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public Page createPage()
                    {
                        return new DialogPage(modal.getPage().getPageReference(),
                                        "SubmitSelectedRows.fail" + uiMenuItem.getSubmitSelectedRows(), false, false);
                    }
                });
                modal.setInitialHeight(150);
                modal.setInitialWidth(350);
                modal.show(_target);
            }
        }
    }
}
