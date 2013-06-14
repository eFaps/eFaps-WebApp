/*
 * Copyright 2003 - 2012 The eFaps Team
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
import java.util.Set;

import org.apache.commons.collections.SetUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITable.TableFilter;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PickerPanel
    extends Panel
{
    /**
     * Name of the checkbox.
     */
    public static final String CHECKBOXNAME = "eFapsFilterSelection";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * List of Picker positions.
     */
    private final List<String> pickList;

    /**
     * Selected values.
     */
    private final Set<?> selected;

    /**
     * @param _wicketId         wicket id for this component
     * @param _model            model
     * @param _uitableHeader    table header this picker belongs to
     */
    public PickerPanel(final String _wicketId,
                       final IModel<?> _model,
                       final UITableHeader _uitableHeader)
        throws EFapsException
    {
        super(_wicketId, _model);
        final UITable table = (UITable) super.getDefaultModelObject();
        this.add(new Label("checkAll", DBProperties.getProperty("FilterPage.All")));
        this.pickList = table.getFilterPickList(_uitableHeader);
        final TableFilter filter = table.getFilter(_uitableHeader);
        if (filter != null) {
            this.selected = filter.getFilterList();
        } else {
            this.selected = SetUtils.EMPTY_SET;
        }
        final FilterListView checksList = new FilterListView("listview", getPickList());
        this.add(checksList);
    }

    /**
     * Getter method for instance variable {@link #pickList}.
     *
     * @return value of instance variable {@link #pickList}
     */
    public List<String> getPickList()
    {
        return this.pickList;
    }

    /**
     * List for Filter.
     */
    public class FilterListView
        extends ListView<String>
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wixcket id of the component
         * @param _list     list to be used
         */
        public FilterListView(final String _wicketId,
                              final List<String> _list)
        {
            super(_wicketId, _list);
            setReuseItems(true);
        }

        @Override
        protected ListItem<String> newItem(final int _index,
                                           final IModel<String> _itemModel)
        {
            final ListItem<String> ret = super.newItem(_index, _itemModel);
            if (_index % 2 == 0) {
                ret.add(AttributeModifier.append("class", "eFapsTableRowOdd"));
            } else {
                ret.add(AttributeModifier.append("class", "eFapsTableRowEven"));
            }
            return ret;
        }

        @Override
        protected void populateItem(final ListItem<String> _item)
        {
            final String label = _item.getDefaultModelObjectAsString();
            _item.add(new ValueCheckBox<Integer>("listview_tr_check", new Model<Integer>(_item.getIndex()),
                            PickerPanel.this.selected.contains(label)));
            _item.add(new Label("listview_tr_label", label));
        }
    }

    /**
     * CheckBox Component.
     */
    public class ValueCheckBox<T>
        extends FormComponent<T>
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Checked or not.
         */
        private final boolean checked;

        /**
         * @param _wicketId wicket id of this component
         * @param _model    model for this component
         * @param _checked  checked or not
         */
        public ValueCheckBox(final String _wicketId,
                             final IModel<T> _model,
                             final boolean _checked)
        {
            super(_wicketId, _model);
            this.checked = _checked;
        }

        @Override
        protected void onComponentTag(final ComponentTag _tag)
        {
            super.onComponentTag(_tag);
            _tag.put("value", getValue());
            _tag.put("name", PickerPanel.CHECKBOXNAME);
            if (this.checked) {
                _tag.put("checked", "checked");
            }
        }
    }
}
