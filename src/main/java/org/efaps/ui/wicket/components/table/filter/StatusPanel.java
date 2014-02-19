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

package org.efaps.ui.wicket.components.table.filter;

import java.util.Collections;
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
import org.efaps.ui.wicket.models.objects.UIStatusSet;
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
public class StatusPanel
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
    private List<UIStatusSet> uiStatusList = Collections.<UIStatusSet>emptyList();

    /**
     * Selected values.
     */
    private Set<?> selected = SetUtils.EMPTY_SET;

    /**
     * @param _wicketId         wicket id for this component
     * @param _model            model
     * @throws EFapsException on error
     */
    public StatusPanel(final String _wicketId,
                       final IModel<UITableHeader> _model)
        throws EFapsException
    {
        super(_wicketId, _model);
        final UITableHeader tableHeader = (UITableHeader) super.getDefaultModelObject();
        if (tableHeader.getUiHeaderObject() instanceof UITable) {
            final UITable table = (UITable) tableHeader.getUiHeaderObject();
            this.add(new Label("checkAll", DBProperties.getProperty("FilterPage.All")));
            this.uiStatusList = UIStatusSet.getUIStatusSet4List(table.getStatusFilterList(tableHeader));
            final TableFilter filter = table.getFilter(tableHeader);
            if (filter != null) {
                this.selected = filter.getFilterList();
            }
        }
        final FilterListView checksList = new FilterListView("listview", getStatusSets());
        this.add(checksList);
    }

    /**
     * Getter method for instance variable {@link #uiStatusList}.
     *
     * @return value of instance variable {@link #uiStatusList}
     */
    public List<UIStatusSet> getStatusSets()
    {
        return  this.uiStatusList;
    }

    /**
     * List for Filter.
     */
    public class FilterListView
        extends ListView<UIStatusSet>
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
                              final List<UIStatusSet> _list)
        {
            super(_wicketId, _list);
            setReuseItems(true);
        }

        @Override
        protected ListItem<UIStatusSet> newItem(final int _index,
                                           final IModel<UIStatusSet> _itemModel)
        {
            final ListItem<UIStatusSet> ret = super.newItem(_index, _itemModel);
            if (_index % 2 == 0) {
                ret.add(AttributeModifier.append("class", "eFapsTableRowOdd"));
            } else {
                ret.add(AttributeModifier.append("class", "eFapsTableRowEven"));
            }
            return ret;
        }

        @Override
        protected void populateItem(final ListItem<UIStatusSet> _item)
        {
            final UIStatusSet uiStatusSet = (UIStatusSet) _item.getDefaultModelObject();
            _item.add(new ValueCheckBox<Integer>("listview_tr_check", new Model<Integer>(_item.getIndex()),
                            StatusPanel.this.selected.contains(uiStatusSet.getSelectedId())));
            _item.add(new Label("listview_tr_label", uiStatusSet.getLabel()));
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
            _tag.put("name", StatusPanel.CHECKBOXNAME);
            if (this.checked) {
                _tag.put("checked", "checked");
            }
        }
    }
}
