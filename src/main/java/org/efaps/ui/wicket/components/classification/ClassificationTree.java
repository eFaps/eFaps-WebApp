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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.WicketTreeModel;
import org.apache.wicket.model.IModel;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.tree.StructurBrowserTree;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * Renders the tree for selecting a clqssification.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationTree
    extends BaseTree
{
    /**
     * Key use as Attribute in the WebApp SystemConfiguration.
     */
    public static final String CONFIG_EXPAND = "ClassificationTreeExpandState";

    /**
     * ResourceReference to the StyleSheet used for this Tree.
     */
    private static final EFapsContentReference CSS =
                    new EFapsContentReference(StructurBrowserTree.class, "StructurTree.css");

    /**
     * Reference to the style sheet.
     */
    private static final EFapsContentReference TCSS = new EFapsContentReference(ClassificationPath.class,
                                                                               "ClassificationTree.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Mapping between the node from the swing tree and the child components.
     */
    private final Map<DefaultMutableTreeNode, Component> node2Component = new HashMap<DefaultMutableTreeNode,
                                                                                                    Component>();

    /**
     * @param _wicketId wicketId of this component
     * @param _model model for this component
     * @param _panel panel this tree is called from
     * @throws EFapsException on error
     */
    public ClassificationTree(final String _wicketId,
                              final IModel<UIClassification> _model,
                              final ClassificationPathPanel _panel)
        throws EFapsException
    {
        super(_wicketId, new WicketTreeModel());
        this.add(StaticHeaderContributor.forCss(ClassificationTree.CSS));
        this.add(StaticHeaderContributor.forCss(ClassificationTree.TCSS));
        final UIClassification classification = _model.getObject();
        final TreeModel model = classification.getTreeModel();
        setModelObject(model);

        //WebApp-Configuration
        final SystemConfiguration config = SystemConfiguration.get(
                            UUID.fromString("50a65460-2d08-4ea8-b801-37594e93dad5"));
        String expand = "true";
        if (config != null) {
            final Properties props = config.getAttributeValueAsProperties(ClassificationTree.CONFIG_EXPAND);
            expand = props.getProperty(Type.get(classification.getClassificationUUID()).getName(), "true");
        }
        if ("false".equalsIgnoreCase(expand)) {
            getTreeState().expandNode(model.getRoot());
            if (classification.isSelected()) {
                final Enumeration<?> nodes = ((DefaultMutableTreeNode) model.getRoot()).breadthFirstEnumeration();
                while (nodes.hasMoreElements()) {
                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
                    final UIClassification nodeClass = (UIClassification) node.getUserObject();
                    if (nodeClass.isSelected()) {
                        getTreeState().expandNode(node);
                    }
                }
            }
        } else {
            getTreeState().expandAll();
        }
        final String label;
        if (DBProperties.hasProperty(classification.getCommandName() + ".Button.ClassTreeUpdate")) {
            label = DBProperties.getProperty(classification.getCommandName() + ".Button.ClassTreeUpdate");
        } else {
            label = DBProperties.getProperty("default.Button.ClassTreeUpdate");
        }
        add(new Button("submitClose", new AjaxSubmitCloseLink(Button.LINKID, _model, _panel),
                        label, Button.ICON.ACCEPT.getReference()));
    }

    /**
     * @see org.apache.wicket.markup.html.tree.BaseTree#newNodeComponent(java.lang.String,
     *      org.apache.wicket.model.IModel)
     * @param _wicketId wicket id for the new component
     * @param _model model for the new component
     * @return new ClassificationTreeLabelPanel
     */
    @Override
    protected Component newNodeComponent(final String _wicketId,
                                         final IModel<Object> _model)
    {
        final ClassificationTreeLabelPanel comp = new ClassificationTreeLabelPanel(_wicketId, _model);
        this.node2Component.put((DefaultMutableTreeNode) _model.getObject(), comp);
        return comp;
    }

    /**
     * Get the component related to a node from the treemodel.
     *
     * @param _node node the component is wanted for
     * @return component related to the given node
     */
    protected Component getComponent(final DefaultMutableTreeNode _node)
    {
        return this.node2Component.get(_node);
    }

    /**
     * Render a link that submits an closes the form.
     */
    public class AjaxSubmitCloseLink
        extends AjaxLink<UIClassification>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * The panel this tree sits in.
         */
        private final ClassificationPathPanel classPathPanel;

        /**
         * @param _wicketId wicket id for this component
         * @param _model model for tihs component
         * @param _panel classifcation panel this link belongs to
         * @param page
         */
        public AjaxSubmitCloseLink(final String _wicketId,
                                   final IModel<UIClassification> _model,
                                   final ClassificationPathPanel _panel)
        {
            super(_wicketId, _model);
            this.classPathPanel = _panel;
        }

        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target ajax request target
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            this.classPathPanel.setUpdateForm(true);
            this.classPathPanel.getModal().close(_target);
        }
    }
}
