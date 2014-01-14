/*
 * Copyright 2003 - 2014 The eFaps Team
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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.PageCreator;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.company.CompanyPage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.main.MainPage;

/**
 * Class is used as a link to set the company.
 *
 * @author The eFasp Team
 * @version $Id:AjaxOpenModalComponent.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class SetCompanyItem
    extends AbstractItem
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Must the main page be reloaded or not.
     */
    private boolean reload = false;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     */
    public SetCompanyItem(final String _wicketId,
                              final IModel<UIMenuItem> _model)
    {
        super(_wicketId, _model);
        add(new OpenSetCompanyPageBehavior());
    }

    /**
     * Setter method for instance variable {@link #reload}.
     *
     * @param _reload value for instance variable {@link #reload}
     */
    public void setReload(final boolean _reload)
    {
        this.reload = _reload;
    }

    /**
     * /** Class is used to execute the opening of the modal window.
     *
     *
     */
    public class OpenSetCompanyPageBehavior
        extends AbstractItemBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public OpenSetCompanyPageBehavior()
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
            final ModalWindowContainer modal;
            if (getPage() instanceof MainPage) {
                modal = ((MainPage) getPage()).getModal();
            } else {
                modal = ((AbstractContentPage) getPage()).getModal();
            }
            modal.reset();
            modal.setInitialHeight(((UIMenuItem) getDefaultModelObject()).getWindowHeight());
            modal.setInitialWidth(((UIMenuItem) getDefaultModelObject()).getWindowWidth());

            modal.setWindowClosedCallback(new ModalWindow.WindowClosedCallback()
            {

                private static final long serialVersionUID = 1L;

                public void onClose(final AjaxRequestTarget _target)
                {
                    if (SetCompanyItem.this.reload) {
                        getRequestCycle().setResponsePage(getPage().getApplication().getHomePage());
                    }
                }

            });

            final PageCreator pageCreator = new ModalWindow.PageCreator()
            {

                private static final long serialVersionUID = 1L;

                public Page createPage()
                {
                    return new CompanyPage(getPage().getPageReference());
                }
            };
            modal.setPageCreator(pageCreator);
            modal.show(_target);
        }
    }
}
