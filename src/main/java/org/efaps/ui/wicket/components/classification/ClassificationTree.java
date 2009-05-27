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

package org.efaps.ui.wicket.components.classification;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.WicketTreeModel;
import org.apache.wicket.model.IModel;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.tree.StructurBrowserTree;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationTree extends BaseTree
{
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
     * @param _wicketId     wicketId of this component
     * @param _model        model for this component
     * @param _panel        panel this tree is called from
     */
    public ClassificationTree(final String _wicketId, final IModel<UIClassification> _model,
                              final ClassificationPathPanel _panel)
    {
        super(_wicketId, new WicketTreeModel());
        this.add(StaticHeaderContributor.forCss(ClassificationTree.CSS));
        this.add(StaticHeaderContributor.forCss(ClassificationTree.TCSS));
        final UIClassification classification = _model.getObject();
        setModelObject(classification.getTreeModel());

        final String label;
        if (DBProperties.hasProperty(classification.getCommandName() + ".Button.ClassTreeUpdate")) {
            label = DBProperties.getProperty(classification.getCommandName() + ".Button.ClassTreeUpdate");
        } else {
            label = DBProperties.getProperty("default.Button.ClassTreeUpdate");
        }
        add(new Button("submitClose", new AjaxSubmitCloseLink(Button.LINKID, _model, _panel),
                       label, Button.ICON_ACCEPT));
    }

    /**
     * @see org.apache.wicket.markup.html.tree.BaseTree#newNodeComponent(java.lang.String, org.apache.wicket.model.IModel)
     * @param arg0
     * @param arg1
     * @return
     */
    @Override
    protected Component newNodeComponent(final String _wicketId, final IModel<Object> _model)
    {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) _model.getObject();
        return new ClassificationTreeLabelPanel(_wicketId, (UIClassification) node.getUserObject());
    }

    public class AjaxSubmitCloseLink extends AjaxLink<UIClassification> {

        private final ClassificationPathPanel classPathPanel;

        /**
         * @param _wicketId
         * @param _model
         * @param _modal
         * @param page
         */
        public AjaxSubmitCloseLink(final String _wicketId, final IModel<UIClassification> _model,
                                   final ClassificationPathPanel _panel)
        {
            super(_wicketId, _model);
            this.classPathPanel = _panel;
        }

        private static final long serialVersionUID = 1L;

        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param ajaxrequesttarget
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            this.classPathPanel.setUpdateForm(true);
            this.classPathPanel.getModal().close(_target);
        }
    }
}
