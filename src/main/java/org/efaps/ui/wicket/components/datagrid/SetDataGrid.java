/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.components.datagrid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.values.IValueConverter;
import org.efaps.ui.wicket.models.field.set.UIFieldSet;
import org.efaps.ui.wicket.models.field.set.UIFieldSetColHeader;
import org.efaps.ui.wicket.models.field.set.UIFieldSetRow;
import org.efaps.ui.wicket.models.field.set.UIFieldSetValue;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class SetDataGrid
    extends GenericPanel<UIFieldSet>
    implements ILabelProvider<String>
{
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

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SetDataGrid.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id for this component
     * @param _model    model for this component
     */
    public SetDataGrid(final String _wicketId,
                       final IModel<UIFieldSet> _model)
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        if (_model.getObject().isEditMode() || _model.getObject().getParent().isCreateMode()) {
            add(new WebComponent("editHeader"));

            final WebMarkupContainer addRow = new WebMarkupContainer("addRow");
            add(addRow);
            final WebMarkupContainer addCol = new WebMarkupContainer("addCol")
            {

                private static final long serialVersionUID = 1L;

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

        final RefreshingView<UIFieldSetColHeader> headerRepeater = new RefreshingView<UIFieldSetColHeader>(
                        "headerRepeater", Model.ofList(_model.getObject().getHeaders()))
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Iterator<IModel<UIFieldSetColHeader>> getItemModels()
            {
                final List<IModel<UIFieldSetColHeader>> ret = new ArrayList<>();
                final List<?> rows = (List<?>) getDefaultModelObject();
                for (final Object row : rows) {
                    ret.add(new Model<>((UIFieldSetColHeader) row));
                }
                return ret.iterator();
            }

            @Override
            protected void populateItem(final Item<UIFieldSetColHeader> _item)
            {
                final UIFieldSetColHeader header = (UIFieldSetColHeader) _item.getDefaultModelObject();
                _item.add(new Label("header", header.getLabel()));
            }
        };
        add(headerRepeater);

        final RefreshingView<UIFieldSetRow> rowRepeater = new RefreshingView<UIFieldSetRow>(
                        "rowRepeater", Model.ofList(_model.getObject().getRows()))
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Iterator<IModel<UIFieldSetRow>> getItemModels()
            {
                final List<IModel<UIFieldSetRow>> ret = new ArrayList<>();
                final List<?> rows = (List<?>) getDefaultModelObject();
                for (final Object row : rows) {
                    ret.add(new Model<>((UIFieldSetRow) row));
                }
                return ret.iterator();
            }

            @Override
            protected void populateItem(final Item<UIFieldSetRow> _item)
            {

                final UIFieldSetRow row = (UIFieldSetRow) _item.getDefaultModelObject();
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
                            return row.getParent().getFieldConfiguration().getName() + "_ID";
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

    @Override
    protected void onAfterRender()
    {
        super.onAfterRender();
        final UIFieldSet cellSet = (UIFieldSet) getDefaultModelObject();
        cellSet.resetIndex();
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(SetDataGrid.CSS).setSortWeight(10));
    }

    @Override
    public IModel<String> getLabel()
    {
        String label = "unknown";
        try {
            label = ((UIFieldSet) getDefaultModelObject()).getLabel();
        } catch (final EFapsException e) {
            SetDataGrid.LOG.error("Catched", e);
        }
        return Model.of(label);
    }

    /**
     * Repeater fo ra Row.
     */
    public final class RowRepeater
        extends RefreshingView<UIFieldSetValue>
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id
         * @param _model    model
         */
        public RowRepeater(final String _wicketId,
                           final IModel<?> _model)
        {
            super(_wicketId, _model);
        }

        @Override
        protected Iterator<IModel<UIFieldSetValue>> getItemModels()
        {
            final List<IModel<UIFieldSetValue>> ret = new ArrayList<>();
            final List<?> values = (List<?>) getDefaultModelObject();
            for (final Object value : values) {
                ret.add(new Model<>((UIFieldSetValue) value));
            }
            return ret.iterator();
        }

        @Override
        protected void populateItem(final Item<UIFieldSetValue> _item)
        {
            final UIFieldSetValue value = _item.getModelObject();
            try {
                _item.add(value.getComponent("value"));
            } catch (final EFapsException e) {
                SetDataGrid.LOG.error("Catched", e);
            }
        }
    }

    /**
     * Link to add a row.
     */
    public final class AddLink
        extends AjaxSubmitLink
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id
         */
        public AddLink(final String _wicketId)
        {
            super(_wicketId);
            setDefaultFormProcessing(false);
        }

        @Override
        protected void onSubmit(final AjaxRequestTarget _target)
        {
            final SetDataGrid grid = findParent(SetDataGrid.class);
            final UIFieldSet cellSet = (UIFieldSet) grid.getDefaultModelObject();

            grid.visitChildren(new IVisitor<Component, Void>()
            {

                @Override
                public void component(final Component _component,
                                      final IVisit<Void> _visit)
                {
                    if (_component instanceof IValueConverter) {
                        final FormContainer frmContainer = findParent(FormContainer.class);
                        frmContainer.removeValueConverter((IValueConverter) _component);
                    }
                    if (_component instanceof IFormModelUpdateListener) {
                        ((IFormModelUpdateListener) _component).updateModel();
                    }
                }
            });

            try {
                cellSet.addNewRow();
            } catch (final EFapsException e) {
                SetDataGrid.LOG.error("CacheReloadException", e);
            }
            _target.add(grid);
        }
    }

    /**
     * Linkt to remove a row.
     */
    public final class RemoveLink
        extends AjaxLink<UIFieldSetRow>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id
         * @param _model    model
         */
        public RemoveLink(final String _wicketId,
                          final Model<UIFieldSetRow> _model)
        {
            super(_wicketId, _model);
        }

        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final SetDataGrid grid = findParent(SetDataGrid.class);
            final UIFieldSet cellSet = (UIFieldSet) grid.getDefaultModelObject();
            final RefreshingView<?> repeater = findParent(RefreshingView.class);
            repeater.visitChildren(new IVisitor<Component, Void>()
            {

                @Override
                public void component(final Component _component,
                                      final IVisit<Void> _visit)
                {
                    if (_component instanceof IValueConverter) {
                        final FormContainer frmContainer = findParent(FormContainer.class);
                        frmContainer.removeValueConverter((IValueConverter) _component);
                    }
                }
            });
            cellSet.getRows().remove(getDefaultModelObject());
            _target.add(grid);
        }
    }
}
