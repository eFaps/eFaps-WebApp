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

package org.efaps.ui.wicket.components.datagrid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.models.cell.CellSetRow;
import org.efaps.ui.wicket.models.cell.CellSetValue;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.cell.UISetColumnHeader;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SetDataGrid
    extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _id
     * @param _model
     */
    public SetDataGrid(final String _wicketId,
                       final IModel<UIFormCellSet> _model)
    {
        super(_wicketId, _model);


        final RefreshingView<UISetColumnHeader> headerRepeater = new RefreshingView<UISetColumnHeader>(
                        "headerRepeater", Model.ofList(_model.getObject().getHeaders()))
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Iterator<IModel<UISetColumnHeader>> getItemModels()
            {
                final List<IModel<UISetColumnHeader>> ret = new ArrayList<IModel<UISetColumnHeader>>();
                final List<?> rows = (List<?>) getDefaultModelObject();
                for (final Object row : rows) {
                    ret.add(new Model<UISetColumnHeader>((UISetColumnHeader) row));
                }
                return ret.iterator();
            }

            @Override
            protected void populateItem(final Item<UISetColumnHeader> _item)
            {
                final UISetColumnHeader header = (UISetColumnHeader) _item.getDefaultModelObject();
                _item.add(new Label("header", header.getLabel()));
            }
        };
        add(headerRepeater);


        final RefreshingView<CellSetRow> rowRepeater = new RefreshingView<CellSetRow>(
                        "rowRepeater", Model.ofList(_model.getObject().getRows()))
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Iterator<IModel<CellSetRow>> getItemModels()
            {
                final List<IModel<CellSetRow>> ret = new ArrayList<IModel<CellSetRow>>();
                final List<?> rows = (List<?>) getDefaultModelObject();
                for (final Object row : rows) {
                    ret.add(new Model<CellSetRow>((CellSetRow) row));
                }
                return ret.iterator();
            }

            @Override
            protected void populateItem(final Item<CellSetRow> _item)
            {
                final CellSetRow row = (CellSetRow) _item.getDefaultModelObject();
                _item.add(new RowRepeater("columnRepeater", Model.ofList(row.getValues())));
            }
        };
        add(rowRepeater);
    }

    public final class RowRepeater
        extends RefreshingView<CellSetValue>
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _id
         * @param _model
         */
        public RowRepeater(final String _id,
                           final IModel<?> _model)
        {
            super(_id, _model);
        }

        /*
         * (non-Javadoc)
         * @see org.apache.wicket.markup.repeater.RefreshingView#getItemModels()
         */
        @Override
        protected Iterator<IModel<CellSetValue>> getItemModels()
        {
            final List<IModel<CellSetValue>> ret = new ArrayList<IModel<CellSetValue>>();
            final List<?> values = (List<?>) getDefaultModelObject();
            for (final Object value : values) {
                ret.add(new Model<CellSetValue>((CellSetValue) value));
            }
            return ret.iterator();
        }

        /*
         * (non-Javadoc)
         * @see
         * org.apache.wicket.markup.repeater.RefreshingView#populateItem(org
         * .apache.wicket.markup.repeater.Item)
         */
        @Override
        protected void populateItem(final Item<CellSetValue> _item)
        {
            final CellSetValue value = _item.getModelObject();
            try {
                _item.add(value.getComponent("value"));
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
