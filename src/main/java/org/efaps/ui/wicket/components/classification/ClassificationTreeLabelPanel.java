/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.ui.wicket.components.classification;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.models.objects.UIClassification;

/**
 * Panel used to render one leaf of the tree for Classification.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationTreeLabelPanel
    extends Panel
{
   /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model of this leaf
     */
    public ClassificationTreeLabelPanel(final String _wicketId,
                                        final IModel<Object> _model)
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) _model.getObject();
        add(new AjaxCheckBox("checkbox", _model));
        add(new LabelComponent("content", ((UIClassification) node.getUserObject()).getLabel()));
    }

    /**
     * Renders a checkbox, that stores the click on the checkbox in the model and updates parent or child nodes.
     */
    public class AjaxCheckBox
        extends WebComponent
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id for this component
         * @param _model model of this leaf
         */
        public AjaxCheckBox(final String _wicketId,
                            final IModel<Object> _model)
        {
            super(_wicketId, _model);
            add(new AjaxCheckBoxClickBehavior());
        }

        /**
         * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
         * @param _tag tag
         */
        @Override
        protected void onComponentTag(final ComponentTag _tag)
        {
            super.onComponentTag(_tag);
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) getDefaultModelObject();
            if (((UIClassification) node.getUserObject()).isSelected()) {
                _tag.put("checked", "checked");
            }
        }
    }

    /**
     * Behavior that will be executed on click.
     */
    public class AjaxCheckBoxClickBehavior
        extends AjaxEventBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _classification
         * @param event
         */
        public AjaxCheckBoxClickBehavior()
        {
            super("onClick");
        }

        /**
         * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target target for the ajaxcall
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) getComponent().getDefaultModelObject();
            final UIClassification classification = (UIClassification) node.getUserObject();
            classification.setSelected(!classification.isSelected());

            // if it was unselected all children must be unselected also
            if (!classification.isSelected() && node.getChildCount() > 0) {
                final ClassificationTree tree = getComponent().findParent(ClassificationTree.class);
                unselect(node, tree, _target);
                // if it was selected all parent must be selected
            } else if (classification.isSelected() && !node.isRoot()) {
                final ClassificationTree tree = getComponent().findParent(ClassificationTree.class);
                DefaultMutableTreeNode parent = node;
                while (!parent.isRoot()) {
                    final DefaultMutableTreeNode parentTmp = (DefaultMutableTreeNode) parent.getParent();
                    final UIClassification parentClass = (UIClassification) parentTmp.getUserObject();
                    parentClass.setSelected(true);
                    _target.addComponent(tree.getComponent(parentTmp));
                    if (!classification.isMultipleSelect()) {
                        final Enumeration<?> col = parentTmp.children();
                        while (col.hasMoreElements()) {
                            final DefaultMutableTreeNode child = (DefaultMutableTreeNode) col.nextElement();
                            if (!child.equals(parent)) {
                                unselect(child, tree, _target);
                                final UIClassification classInner = (UIClassification) child.getUserObject();
                                if (classInner.isSelected()) {
                                    classInner.setSelected(false);
                                    _target.addComponent(tree.getComponent(child));
                                }
                            }
                        }
                    }
                    parent = parentTmp;
                }
            }

        }

        /**
         * Recursive method used to uncheck all children of a given node.
         *
         * @param _node node the children must be unchecked of
         * @param _tree tree the node belongs to
         * @param _target ajax request target
         */
        private void unselect(final DefaultMutableTreeNode _node,
                              final ClassificationTree _tree,
                              final AjaxRequestTarget _target)
        {
            final Enumeration<?> enumer = _node.children();
            while (enumer.hasMoreElements()) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumer.nextElement();
                unselect(node, _tree, _target);
                final UIClassification classification = (UIClassification) node.getUserObject();
                classification.setSelected(false);
                _target.addComponent(_tree.getComponent(node));
            }
        }
    }
}
