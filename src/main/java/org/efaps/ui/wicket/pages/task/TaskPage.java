/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.ui.wicket.pages.task;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.efaps.bpm.Bpm;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.jbpm.task.query.TaskSummary;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TaskPage
    extends AbstractMergePage
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _rowModel
     */
    public TaskPage(final IModel<TaskSummary> _rowModel,
                    final PageReference _pageReference)
    {
        super(_rowModel);
        final Form form = new Form("form");
        add(form);
        form.add(new Button("aprove", new DecisionLink(Button.LINKID, _rowModel, _pageReference, true),
                        "Label1", Button.ICON.ACCEPT.getReference()));

        form.add(new Button("reject", new DecisionLink(Button.LINKID, _rowModel, _pageReference, false),
                        "Label2", Button.ICON.CANCEL.getReference()));

    }

    public class DecisionLink
        extends WebMarkupContainer
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _id
         * @param _model
         */
        public DecisionLink(final String _id,
                            final IModel<TaskSummary> _model,
                            final PageReference _pageReference,
                            final boolean _decision)
        {
            super(_id, _model);
            add(new AjaxFormSubmitBehavior("onclick")
            {

                /**
                 *
                 */
                private static final long serialVersionUID = 1L;

                /*
                 * (non-Javadoc)
                 * @see
                 * org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onEvent
                 * (org.apache.wicket.ajax.AjaxRequestTarget)
                 */
                @Override
                protected void onEvent(final AjaxRequestTarget _target)
                {
                    final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                    modal.close(_target);

                    final Map<String, Object> values = new HashMap<String, Object>();

                    Bpm.executeTask((TaskSummary) getComponent().getDefaultModelObject(), _decision, values);
                }
            });
        }
    }
}
