/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.ui.wicket.components.table.filter;

import java.util.List;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PickerPanel extends Panel
{
    /**
     * Name of the checkbox.
     */
    public static final String CHECKBOXNAME = "eFapsFilterSelection";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final List<?> pickList;

    /**
     * @param id
     * @param model
     */
    public PickerPanel(final String id, final IModel<?> model,final UITableHeader _uitableHeader)
    {
        super(id, model);
        final UITable table = (UITable) super.getDefaultModelObject();
        this.add(new Label("checkAll", DBProperties.getProperty("FilterPage.All")));
        this.pickList = table.getFilterPickList(_uitableHeader);
        final FilterListView checksList = new FilterListView("listview", getPickList());
        this.add(checksList);
    }


    /**
     * Getter method for instance variable {@link #pickList}.
     *
     * @return value of instance variable {@link #pickList}
     */
    public List<?> getPickList()
    {
        return this.pickList;
    }


    @SuppressWarnings("unchecked")
    public class FilterListView extends ListView
    {

        private static final long serialVersionUID = 1L;

        private boolean odd = true;

        public FilterListView(final String _id, final List<?> _list)
        {
            super(_id, _list);
            setReuseItems(true);
        }

        @Override
        protected void populateItem(final ListItem _item)
        {
            final WebMarkupContainer tr = new WebMarkupContainer("listview_tr");
            _item.add(tr);

            if (this.odd) {
                tr.add(new SimpleAttributeModifier("class", "eFapsTableRowOdd"));
            } else {
                tr.add(new SimpleAttributeModifier("class", "eFapsTableRowEven"));
            }

            this.odd = !this.odd;
            tr.add(new ValueCheckBox("listview_tr_check", new Model(_item.getIndex())));
            tr.add(new Label("listview_tr_label", _item.getDefaultModelObjectAsString()));
        }
    }
    public class ValueCheckBox<T> extends FormComponent<T>
    {

        private static final long serialVersionUID = 1L;

        public ValueCheckBox(final String _id, final IModel<T> _model)
        {
            super(_id, _model);
        }

        @Override
        protected void onComponentTag(final ComponentTag tag)
        {
            super.onComponentTag(tag);
            tag.put("value", getValue());
            tag.put("name", CHECKBOXNAME);
        }
    }
}
