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

package org.efaps.ui.wicket.components.bpm;

import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.PageCreator;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.admin.ui.Menu;
import org.efaps.bpm.BPM;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UITaskObject;
import org.efaps.ui.wicket.models.objects.UITaskSummary;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.pages.task.TaskPage;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

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
     * @param _wicketID component id
     * @param _model model for contact
     * @param _pageReference reference to the calling page
     */
    public ActionPanel(final String _wicketID,
                       final IModel<UITaskSummary> _model,
                       final PageReference _pageReference)
    {
        super(_wicketID, _model);
        final AjaxLink<UITaskSummary> select = new AjaxLink<UITaskSummary>("select", _model)
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

        final Link<UITaskSummary> open = new Link<UITaskSummary>("open", _model)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick()
            {
                final UITaskSummary uiTaskSummary = _model.getObject();
                final Object values = BPM.getTaskData(uiTaskSummary.getTaskSummary());
                Instance inst = null;
                if (values instanceof Map) {
                    final String oid = (String) ((Map<?, ?>) values).get("OID");
                    inst = Instance.get(oid);
                }

                if (inst != null && inst.isValid()) {

                    Menu menu = null;
                    try {

                        menu = Menu.getTypeTreeMenu(inst.getType());
                    } catch (final EFapsException e) {
                        throw new RestartResponseException(new ErrorPage(e));
                    }
                    if (menu == null) {
                        final Exception ex = new Exception("no tree menu defined for type " + inst.getType().getName());
                        throw new RestartResponseException(new ErrorPage(ex));
                    }

                    Page page;
                    try {
                        page = new ContentContainerPage(menu.getUUID(), inst.getOid(),
                                        getPage() instanceof StructurBrowserPage);

                    } catch (final EFapsException e) {
                        page = new ErrorPage(e);
                    }
                    this.setResponsePage(page);
                }
            }
        };
        add(open);
    }
}
