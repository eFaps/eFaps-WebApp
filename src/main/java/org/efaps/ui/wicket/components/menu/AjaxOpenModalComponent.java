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

package org.efaps.ui.wicket.components.menu;

import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowAjaxPageCreator;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.main.MainPage;

/**
 * Class is used as a link inside the JSCookMenu that opens a modal window.
 *
 * @author The eFaps Team
 * @version $Id:AjaxOpenModalComponent.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxOpenModalComponent
    extends AbstractMenuItemAjaxComponent
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
    public AjaxOpenModalComponent(final String _wicketId,
                                  final IModel<UIMenuItem> _model,
                                  final FormContainer _form)
    {
        super(_wicketId, _model);
        if (_form == null) {
            add(new AjaxOpenModalBehavior());
        } else {
            add(new SubmitAndOpenModalBehavior(_form));
        }
    }

    /**
     * This Method returns the JavaScript which is executed by the JSCooKMenu.
     *
     * @return String with the JavaScript
     */
    @Override
    public String getJavaScript()
    {
        final String ret;
        if (super.getBehaviors().get(0) instanceof AjaxOpenModalBehavior) {
            ret = ((AjaxOpenModalBehavior) super.getBehaviors().get(0)).getJavaScript();
        } else {
            ret = ((SubmitAndOpenModalBehavior) super.getBehaviors().get(0)).getJavaScript();
        }
        return ret;
    }

    /**
     * /** Class is used to execute the opening of the modal window.
     *
     *
     */
    public class AjaxOpenModalBehavior
        extends AjaxEventBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public AjaxOpenModalBehavior()
        {
            super("onclick");
        }

        /**
         * This Method returns the JavaScript which is executed by the JSCooKMenu.
         *
         * @return String with the JavaScript
         */
        public String getJavaScript()
        {
            final String script = super.getCallbackScript().toString();
            return "javascript:" + script.replace("'", "\"");
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

        /**
         * Method must be overwritten, otherwise the default would break the execution of the JavaScript.
         *
         * @return null
         */
        @Override
        protected CharSequence getPreconditionScript()
        {
            return null;
        }
    }

    /**
     * Open a modal window and submit the values.
     */
    public class SubmitAndOpenModalBehavior
        extends AjaxFormSubmitBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Form to be submitted.
         */
        private final Form<?> form;

        /**
         * @param _form form
         */
        public SubmitAndOpenModalBehavior(final Form<?> _form)
        {
            super(_form, "onClick");
            this.form = _form;
        }

        /**
         * This Method returns the JavaScript which is executed by the JSCooKMenu.
         *
         * @return String with the JavaScript
         */
        public String getJavaScript()
        {
            String script = super.getEventHandler().toString();
            script = "javascript:" + script.replace("'", "\"");
            script = script.replace("wicketSubmitFormById", "wicketSubmitForm");
            final String formStr;
            if (ContentContainerPage.IFRAME_PAGEMAP_NAME.equals(getComponent().getPage().getPageMapName())) {
                formStr = "top.frames[0].frames[0].document.getElementById(\"" + this.form.getMarkupId() + "\")";
            } else {
                formStr = "top.frames[0].document.getElementById(\"" + this.form.getMarkupId() + "\")";
            }
            script = script.replace("\"" + this.form.getMarkupId() + "\"", formStr);
            return script;
        }

        /**
         * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#getPreconditionScript()
         * @return null
         */
        @Override
        protected CharSequence getPreconditionScript()
        {
            // we have to override the original Script, because it breaks the
            // eval in the eFapsScript
            return null;
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

            final Map<?, ?> para = this.form.getRequest().getParameterMap();

            boolean check = false;
            if (uiMenuItem.getSubmitSelectedRows() > -1) {
                final String[] oids = (String[]) para.get("selectedRow");
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
                        return new DialogPage(modal, "SubmitSelectedRows.fail" + uiMenuItem.getSubmitSelectedRows(),
                                        false, null);
                    }
                });
                modal.setInitialHeight(150);
                modal.setInitialWidth(350);
                modal.show(_target);
            }
        }

        /**
         * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onError(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onError(final AjaxRequestTarget _target)
        {
            // nothing must be done
        }
    }
}
