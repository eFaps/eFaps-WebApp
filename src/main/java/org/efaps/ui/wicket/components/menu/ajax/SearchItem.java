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
package org.efaps.ui.wicket.components.menu.ajax;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class SearchItem
    extends AbstractItem
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SearchItem.class);

    /**
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     */
    public SearchItem(final String _wicketId,
                      final IModel<UIMenuItem> _model)
    {
        super(_wicketId, _model);
        add(new SearchSubmitBehavior());
    }

    /**
     * Open a modal window and submit the values.
     */
    public class SearchSubmitBehavior
        extends AbstractSubmitBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         *
         */
        public SearchSubmitBehavior()
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
            try {
                final UIMenuItem menuitem = (UIMenuItem) getComponent().getDefaultModelObject();
                final UIForm uiform = (UIForm) getPage().getDefaultModelObject();
                uiform.resetModel();
                uiform.setCommandUUID(menuitem.getCommandUUID());
                uiform.setFormUUID(uiform.getCommand().getTargetForm().getUUID());
                uiform.execute();
                getRequestCycle().setResponsePage(new FormPage(Model.of(uiform),
                                ((AbstractContentPage) getPage()).getModalWindow()));
            } catch (final EFapsException e) {
                LOG.error("Catched error", e);
                throw new RestartResponseException(new ErrorPage(e));
            }
        }
    }
}
