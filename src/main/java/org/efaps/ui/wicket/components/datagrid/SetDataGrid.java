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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.models.cell.CellSetRow;
import org.efaps.ui.wicket.models.cell.CellSetValue;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.cell.UISetColumnHeader;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

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
     * Reference to the style sheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(SetDataGrid.class, "DataGrid.css");

    /**
     * Content reference for the add icon.
     */
    public static final EFapsContentReference ICON_ADD = new EFapsContentReference(SetDataGrid.class, "add.png");

    /**
     * Content reference for the delete icon.
     */
    public static final EFapsContentReference ICON_DELETE = new EFapsContentReference(SetDataGrid.class, "delete.png");

    /**
     * Static variable used as the class name for the table.
     */
    public static final String STYLE_CLASS = "eFapsFieldSet";

    protected static final String AbstractUIPageObject = null;

    /**
     * @param _id
     * @param _model
     */
    public SetDataGrid(final String _wicketId,
                       final IModel<UIFormCellSet> _model)
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        add(StaticHeaderContrBehavior.forCss(FormPanel.CSS));
        if (_model.getObject().isEditMode() || _model.getObject().getParent().isCreateMode()) {
            add(new WebComponent("editHeader"));

            final WebMarkupContainer addRow = new WebMarkupContainer("addRow");
            add(addRow);
            final WebMarkupContainer addCol = new WebMarkupContainer("addCol")
            {

                @Override
                protected void onComponentTag(final ComponentTag _tag)
                {
                    super.onComponentTag(_tag);
                    _tag.put("colspan", _model.getObject().getHeaders().size() + 1);
                }
            };
            addRow.add(addCol);

            final AddLink addLink = new AddLink("addLink");
            addCol.add(addLink);
            final StaticImageComponent image = new StaticImageComponent("addIcon");
            image.setReference(SetDataGrid.ICON_ADD);
            addLink.add(image);
        } else {
            add(new WebComponent("editHeader").setVisible(false));
            add(new WebMarkupContainer("addRow").setVisible(false));
        }

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
                if (row.getParent().isEditMode() || row.getParent().getParent().isCreateMode()) {
                    final WebMarkupContainer remove = new WebMarkupContainer("remove");
                    _item.add(remove);
                    final RemoveLink removeLink = new RemoveLink("removeLink", Model.of(row));
                    remove.add(removeLink);
                    final StaticImageComponent image = new StaticImageComponent("removeIcon");
                    image.setReference(SetDataGrid.ICON_DELETE);
                    removeLink.add(image);
                    final String key;
                    if (row.getOid() == null) {
                        key = "";
                    } else {
                        key = ((AbstractUIPageObject) getPage().getDefaultModelObject()).registerOID(row.getOid());
                    }
                    remove.add(new HiddenField<String>("oid", Model.of(key))
                    {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public String getInputName()
                        {
                            return row.getParent().getName() + "_ID";
                        }
                    });

                } else {
                    _item.add(new WebMarkupContainer("remove").setVisible(false));
                }
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

    public final class AddLink
        extends AjaxLink
    {

        /**
         * @param _id
         */
        public AddLink(final String _id)
        {
            super(_id);
        }

        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final SetDataGrid grid = findParent(SetDataGrid.class);
            final UIFormCellSet cellSet = (UIFormCellSet) grid.getDefaultModelObject();
            try {
                cellSet.addNewRow();
            } catch (final CacheReloadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            _target.add(grid);
        }
    }

    public final class RemoveLink
        extends AjaxLink<CellSetRow>
    {

        private static final long serialVersionUID = 1L;

        /**
         * @param _id
         * @param _model
         */
        public RemoveLink(final String _id,
                          final Model<CellSetRow> _model)
        {
            super(_id, _model);
        }

        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final SetDataGrid grid = findParent(SetDataGrid.class);
            final UIFormCellSet cellSet = (UIFormCellSet) grid.getDefaultModelObject();
            cellSet.getRows().remove(getDefaultModelObject());
            _target.add(grid);
        }
    }
}
