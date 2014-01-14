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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.connection;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageReference;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.bpm.AbstractSortableProvider;
import org.efaps.ui.wicket.models.objects.UIUser;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: MessageTablePanel.java 10378 2013-10-06 01:57:57Z
 *          jan@moxter.net $
 */
public class MessageTablePanel
    extends Panel
{
    /**
     * Reference to the style sheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(AbstractSortableProvider.class,
                    "BPM.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * DataProvider for this TaskTable.
     */
    private final UserProvider dataProvider;

    /**
     * @param _wicketId wicket for this component
     * @param _pageReference Reference to the calling page
     * @param _dataProvider provider for the task table
     * @throws EFapsException on error
     */
    public MessageTablePanel(final String _wicketId,
                             final PageReference _pageReference,
                             final UserProvider _dataProvider)
        throws EFapsException
    {
        super(_wicketId);
        this.dataProvider = _dataProvider;

        final List<IColumn<UIUser, String>> columns = new ArrayList<IColumn<UIUser, String>>();

        columns.add(new AbstractColumn<UIUser, String>(new Model<String>(""))
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<UIUser>> _cellItem,
                                     final String _componentId,
                                     final IModel<UIUser> _rowModel)
            {
                _cellItem.add(new CheckBoxPanel(_componentId, _rowModel));
            }
        });

        final String userName = DBProperties.getProperty(MessageTablePanel.class.getName() + ".UserName");

        columns.add(new PropertyColumn<UIUser, String>(new Model<String>(userName), "userName", "userName"));

        add(new AjaxFallbackDefaultDataTable<UIUser, String>("table", columns, this.dataProvider,
                        this.dataProvider.getRowsPerPage()));
    }

    /**
     * @return update the underlying data
     */
    public boolean updateData()
    {
        this.dataProvider.requery();
        return true;
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(MessageTablePanel.CSS));
    }

    /**
     * Panel for the checkbox column.
     */
    public static class CheckBoxPanel
        extends Panel
        implements IMarkupResourceStreamProvider
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicketid for this component
         * @param _rowModel model for the row
         */
        public CheckBoxPanel(final String _wicketId,
                             final IModel<UIUser> _rowModel)
        {
            super(_wicketId, _rowModel);
            add(new CheckBox("checkbox", Model.of(false)));
        }

        @Override
        public IResourceStream getMarkupResourceStream(final MarkupContainer _container,
                                                       final Class<?> _containerClass)
        {
            return new StringResourceStream(
                            "<wicket:panel><input wicket:id=\"checkbox\" type=\"checkbox\"/></wicket:panel>");
        }
    }
}
