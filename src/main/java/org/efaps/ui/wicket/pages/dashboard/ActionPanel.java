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

package org.efaps.ui.wicket.pages.dashboard;

import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.PageCreator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UITaskObject;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.pages.task.TaskPage;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.jbpm.task.query.TaskSummary;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ActionPanel
    extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param id component id
     * @param model model for contact
     */
    public ActionPanel(final String id,
                       final IModel<TaskSummary> _model,
                       final PageReference _pageReference)
    {
        super(id, _model);
        final AjaxLink<TaskSummary> select = new AjaxLink<TaskSummary>("select", _model)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget _target)
            {
                final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                modal.setPageCreator(new PageCreator()
                {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Page createPage()
                    {
                        Page page = null;
                        try {
                            page = new TaskPage(UITaskObject.getModelForTask(_model.getObject()),
                                            _pageReference);
                        } catch (final CacheReloadException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (final EFapsException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        return page;
                    }
                });
                modal.show(_target);
            }
        };
        add(select);
    }
}
