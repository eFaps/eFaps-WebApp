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
package org.efaps.ui.wicket.components.tree;

import java.util.Iterator;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree.State;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.nested.BranchItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.table.field.CheckBoxField;
import org.efaps.ui.wicket.components.table.field.FieldPanel;
import org.efaps.ui.wicket.components.table.row.RowId;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.IHidden;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class SubElement
    extends Panel
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubElement.class);

    /**
     * SubTree element.
     */
    private NestedTree<UIStructurBrowser> tree;

    /**
     * @param _wicketId wicket id for this component
     * @param _tree Nested tree element
     * @param _model model for this component
     */
    public SubElement(final String _wicketId,
                      final NestedTree<UIStructurBrowser> _tree,
                      final IModel<UIStructurBrowser> _model)
    {
        super(_wicketId, _model);

        if (_tree == null) {
            throw new IllegalArgumentException("argument [tree] cannot be null");
        }
        this.tree = _tree;

        final RefreshingView<UIStructurBrowser> branches = new RefreshingView<UIStructurBrowser>("branches")
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Iterator<IModel<UIStructurBrowser>> getItemModels()
            {
                return new ModelIterator();
            }

            @Override
            protected Item<UIStructurBrowser> newItem(final String _wicketId,
                                                      final int _index,
                                                      final IModel<UIStructurBrowser> _model)
            {
                return newBranchItem(_wicketId, _index, _model);
            }

            @Override
            protected void populateItem(final Item<UIStructurBrowser> _item)
            {
                try {
                    populateBranch(_item);
                } catch (final EFapsException e) {
                    SubElement.LOG.error("EFapsException", e);
                }
            }
        };
        branches.setItemReuseStrategy(new IItemReuseStrategy()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public <S> Iterator<Item<S>> getItems(final IItemFactory<S> _factory,
                                                  final Iterator<IModel<S>> _newModels,
                                                  final Iterator<Item<S>> _existingItems)
            {
                return SubElement.this.tree.getItemReuseStrategy().getItems(_factory, _newModels, _existingItems);
            }
        });
        add(branches);
    }

    /**
     * @return the model for this element
     */
    @SuppressWarnings("unchecked")
    public IModel<UIStructurBrowser> getModel()
    {
        return (IModel<UIStructurBrowser>) getDefaultModel();
    }

    /**
     * @return the model object of this element
     */
    public UIStructurBrowser getModelObject()
    {
        return getModel().getObject();
    }

    /**
     * @param _wicketId wicket id for the branch item
     * @param _index    index for the item
     * @param _model    model for the item
     * @return new BrancheItem
     */
    protected BranchItem<UIStructurBrowser> newBranchItem(final String _wicketId,
                                                          final int _index,
                                                          final IModel<UIStructurBrowser> _model)
    {
        return new BranchItem<UIStructurBrowser>(_wicketId, _index, _model);
    }

    /**
     * @param _item item to be used for population
     * @throws EFapsException on error
     */
    protected void populateBranch(final Item<UIStructurBrowser> _item)
        throws EFapsException
    {
        final IModel<UIStructurBrowser> model = _item.getModel();
        final UIStructurBrowser strucBrws = model.getObject();
        final RepeatingView cellsBeforeRepeater = new RepeatingView("cellsBeforeRepeater");
        _item.add(cellsBeforeRepeater);
        final RepeatingView cellsAfterRepeater = new RepeatingView("cellsAfterRepeater");
        _item.add(cellsAfterRepeater);

        int i = strucBrws.getTableId();
        if (strucBrws.isEditMode()) {
            final Label cell = new Label(cellsBeforeRepeater.newChildId(), "");
            cell.add(AttributeModifier.append("class", "eFapsTableCellClear"));
            cell.add(AttributeModifier.append("class", "eFapsSTBRWtmp"));
            cell.add(AttributeModifier.append("class", "eFapsTableCell eFapsTableCellEdit eFapsCellFixedWidth" + i));
            cell.setOutputMarkupId(true);
            cellsBeforeRepeater.add(cell);
            i++;
        }
        if (strucBrws.isShowCheckBoxes()) {
            final CheckBoxField checkbox = new CheckBoxField(cellsBeforeRepeater.newChildId(),
                            strucBrws.getInstanceKey());
            if (i == strucBrws.getTableId()) {
                checkbox.add(AttributeModifier.append("class", "eFapsTableCellClear"));
            }
            checkbox.add(AttributeModifier.append("class", "eFapsSTBRWtmp"));
            checkbox.add(AttributeModifier.append("class", "eFapsTableCheckBoxCell eFapsCellFixedWidth" + i));
            checkbox.setOutputMarkupId(true);
            cellsBeforeRepeater.add(checkbox);
            i++;
        }
        boolean firstCell = true;
        boolean before = true;
        for (final AbstractUIField uiField : strucBrws.getColumns()) {
            final Component cell;
            if (strucBrws.isBrowserField(uiField)) {
                before = false;
                cell = SubElement.this.tree.newNodeComponent("node", model);
                _item.add(cell);
            } else {
                final RepeatingView repeater;
                if (before) {
                    repeater = cellsBeforeRepeater;
                } else {
                    repeater = cellsAfterRepeater;
                }
                cell = new FieldPanel(repeater.newChildId(), Model.of(uiField));
                cell.setOutputMarkupId(true);
                repeater.add(cell);
            }
            if (i == strucBrws.getTableId()) {
                cell.add(AttributeModifier.append("class", "eFapsTableCellClear"));
            }

            if (uiField.getFieldConfiguration().isFixedWidth()) {
                if (firstCell) {
                    firstCell = false;
                    cell.add(AttributeModifier.append("class", "eFapsTableFirstCell eFapsTableCell"
                                    + " eFapsCellFixedWidth" + i));
                } else {
                    cell.add(AttributeModifier.append("class", "eFapsTableCell eFapsCellFixedWidth" + i));
                }
            } else {
                if (firstCell) {
                    firstCell = false;
                    cell.add(AttributeModifier.append("class", "eFapsTableFirstCell eFapsTableCell"
                                    + " eFapsCellWidth" + i));
                } else {
                    cell.add(AttributeModifier.append("class", "eFapsTableCell eFapsCellWidth" + i));
                }
            }
            i++;
            cell.add(AttributeModifier.append("class", "eFapsSTBRWtmp"));
        }

        _item.add(SubElement.this.tree.newSubtree("subtree", model));

        final RepeatingView hiddenRepeater = new RepeatingView("hiddenRepeater");
        _item.add(hiddenRepeater);
        for (final IHidden cell : strucBrws.getHidden()) {
            hiddenRepeater.add(cell.getComponent(hiddenRepeater.newChildId()));
        }

        _item.add(new RowId("rowId", Model.of((AbstractInstanceObject) strucBrws)));
    }

    @Override
    public boolean isVisible()
    {
        boolean ret = true;
        final UIStructurBrowser t = getModel().getObject();
        if (t != null) {
            // roots always visible
            ret = this.tree.getState(t) == State.EXPANDED;
        }
        return ret;
    }

    /**
     * Iterator implementation for the SubElement.
     */
    private final class ModelIterator
        implements Iterator<IModel<UIStructurBrowser>>
    {
        /**
         * children.
         */
        private Iterator<? extends UIStructurBrowser> children;

        /**
         * Constructor.
         */
        ModelIterator()
        {
            final UIStructurBrowser t = getModel().getObject();
            if (t == null) {
                this.children = SubElement.this.tree.getProvider().getRoots();
            } else {
                this.children = SubElement.this.tree.getProvider().getChildren(t);
            }
        }

        /**
         * not implemented.
         */
        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * has this iterator a further element.
         * @return true if next element
         */
        @Override
        public boolean hasNext()
        {
            return this.children.hasNext();
        }

        /**
         * Get the next element.
         * @return Model
         */
        @Override
        public IModel<UIStructurBrowser> next()
        {
            return SubElement.this.tree.getProvider().model(this.children.next());
        }
    }
}
