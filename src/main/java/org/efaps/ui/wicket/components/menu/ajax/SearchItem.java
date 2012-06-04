/*
 * Copyright 2003 - 2011 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.ui.wicket.components.menu.ajax;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.heading.HeadingPanel;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SearchItem
    extends AbstractItem
{
    /**
    *
    */
   private static final long serialVersionUID = 1L;

    /**
     * @param _id
     * @param _model
     */
    public SearchItem(final String _id,
                      final IModel<UIMenuItem> _model)
    {
        super(_id, _model);
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
            super("onClick");
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
                FormContainer form = null;
                HeadingPanel heading = null;
                boolean break1 = false;
                boolean break2 = false;

                final Iterator<? extends Component> iter = getPage().iterator();
                while (iter.hasNext()) {
                    final Component comp = iter.next();
                    if (comp instanceof FormContainer) {
                        _target.add(comp);
                        form = (FormContainer) comp;
                        break1 = true;
                    }
                    if (comp instanceof HeadingPanel) {
                        _target.add(comp);
                        heading = (HeadingPanel) comp;
                        break2 = true;
                    }
                    if (break1 && break2) {
                        break;
                    }
                }
                heading.removeAll();
                form.removeAll();

                final UIMenuItem menuitem = (UIMenuItem) getComponent().getDefaultModelObject();

                final UIForm uiform = (UIForm) getPage().getDefaultModelObject();
                uiform.resetModel();
                uiform.setCommandUUID(menuitem.getCommandUUID());
                uiform.setFormUUID(uiform.getCommand().getTargetForm().getUUID());
                uiform.execute();
                heading.addComponents(uiform.getTitle());
                FormPage.updateFormContainer(getPage(), form, uiform);
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
        }
    }
}
