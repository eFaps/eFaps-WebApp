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
import javax.swing.tree.TreeModel;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.tree.BaseTree;
import org.apache.wicket.markup.html.tree.WicketTreeModel;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
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
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id of this component
     * @param _modal
     * @param page
     */
    public ClassificationTree(final String _wicketId, final TreeModel _model, final ModalWindowContainer _modal, final Page page)
    {
        super(_wicketId, new WicketTreeModel());
        setModelObject(_model);
        this.add(StaticHeaderContributor.forCss(ClassificationTree.CSS));

        add(new Button("submitClose", new AjaxSubmitCloseLink(Button.LINKID, _modal, page), "jaaa!", Button.ICON_ACCEPT));

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
        return new ClassificationLabelPanel(_wicketId, (UIClassification) node.getUserObject());
    }

    public class AjaxSubmitCloseLink extends AjaxLink {

        private final ModalWindowContainer modal;
        private final Page page;

        /**
         * @param _wicketId
         * @param _modal
         * @param page
         */
        public AjaxSubmitCloseLink(final String _wicketId, final ModalWindowContainer _modal, final Page page)
        {
            super(_wicketId);
            this.modal = _modal;
            this.page = page;
        }

        private static final long serialVersionUID = 1L;

        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param ajaxrequesttarget
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            this.modal.close(_target);

        }

      }
}
