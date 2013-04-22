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

package org.efaps.ui.wicket.pages.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.PageCreator;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UITaskObject;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.pages.task.TaskPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.jbpm.task.query.TaskSummary;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UserPage
    extends AbstractMergePage
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(UserPage.class, "UserPage.css");

    public UserPage(final PageReference _pageReference)
        throws EFapsException
    {
        super();

        final List<IColumn<TaskSummary, String>> columns = new ArrayList<IColumn<TaskSummary, String>>();

        columns.add(new AbstractColumn<TaskSummary, String>(new Model<String>("Actions"))
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<TaskSummary>> _cellItem,
                                     final String _componentId,
                                     final IModel<TaskSummary> _rowModel)
            {
                _cellItem.add(new AjaxLink<Void>(_componentId)
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
                                    page = new TaskPage(UITaskObject.getModelForTask(_rowModel.getObject()),
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
                });
            }
        });
        // only admin
        columns.add(new PropertyColumn<TaskSummary, String>(new Model<String>("ID"), "id"));
        columns.add(new PropertyColumn<TaskSummary, String>(new Model<String>("Name"), "name",
                        "name"));

        columns.add(new PropertyColumn<TaskSummary, String>(new Model<String>("Description"), "description",
                        "description"));
        columns.add(new PropertyColumn<TaskSummary, String>(new Model<String>("Status"), "status"));
        columns.add(new PropertyColumn<TaskSummary, String>(new Model<String>("Activation Time"), "activationTime"));

        final SystemConfiguration config = EFapsSystemConfiguration.KERNEL.get();

        final boolean active = config != null
                        ? config.getAttributeValueAsBoolean(KernelSettings.ActivateBPM) : false;
        if (active) {
            add(new AjaxFallbackDefaultDataTable<TaskSummary, String>("table", columns,
                            new SortableTaskSummaryDataProvider(), 8));
        } else {
            add(new WebMarkupContainer("table").setVisible(false));
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(UserPage.CSS));
    }
}
