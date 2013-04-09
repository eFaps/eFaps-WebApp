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

import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.pages.AbstractMergePage;
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
    public TaskPage(final IModel<TaskSummary> _rowModel)
    {
        super(_rowModel);
        final Form form = new Form("form");
        add(form);
        form.add(new Button("confirm", new ConfirmLink(Button.LINKID,_rowModel),
                        "Label", Button.ICON.CANCEL.getReference()));
    }


    class ConfirmLink extends WebMarkupContainer
    {

        /**
         * @param _id
         * @param _model
         */
        public ConfirmLink(final String _id,
                           final IModel<?> _model)
        {
            super(_id, _model);
            add(new AjaxFormSubmitBehavior("onclick"){});
        }

    }

}
