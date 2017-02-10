/*
 * Copyright 2003 - 2017 The eFaps Team
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
 */

package org.efaps.ui.wicket.components.classification;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class ClassificationTreePanel
    extends Panel
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the style sheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(ClassificationPath.class,
                    "ClassificationTree.css");

    /**
     * Register if model was changed or not.
     */
    private boolean changed = false;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @throws CacheReloadException on error
     */
    public ClassificationTreePanel(final String _wicketId,
                                   final IModel<UIClassification> _model)
        throws CacheReloadException
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);

        add(new ClassificationTree("tree", _model));
        final UIClassification classification = _model.getObject();
        final String label;
        if (DBProperties.hasProperty(classification.getCommandName() + ".Button.ClassTreeUpdate")) {
            label = DBProperties.getProperty(classification.getCommandName() + ".Button.ClassTreeUpdate");
        } else {
            label = DBProperties.getProperty("default.Button.ClassTreeUpdate");
        }
        add(new AjaxSubmitCloseLink("submitClose", _model, label));
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(ClassificationTreePanel.CSS));
    }

    @Override
    protected void onModelChanged()
    {
        super.onModelChanged();
        this.changed = true;
    }

    /**
     * Render a link that submits an closes the form.
     */
    public class AjaxSubmitCloseLink
        extends AjaxButton<UIClassification>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new ajax submit close link.
         *
         * @param _wicketId wicket id for this component
         * @param _model model for tihs component
         * @param _label the label
         */
        public AjaxSubmitCloseLink(final String _wicketId,
                                   final IModel<UIClassification> _model,
                                   final String _label)
        {
            super(_wicketId, _model, AjaxButton.ICON.ACCEPT.getReference(), _label);
        }

        @Override
        protected boolean getDefaultProcessing()
        {
            return false;
        }

        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target ajax request target
         */
        @Override
        public void onRequest(final AjaxRequestTarget _target)
        {
            if (ClassificationTreePanel.this.changed) {
                final Page page = getPage();
                final UIForm uiform = (UIForm) page.getDefaultModelObject();

                page.visitChildren(FormContainer.class, new IVisitor<FormContainer, Void>()
                {

                    @Override
                    public void component(final FormContainer _form,
                                          final IVisit<Void> _visit)
                    {
                        _form.removeAll();
                        try {
                            uiform.updateClassElements((UIClassification) getDefaultModelObject());
                            FormPage.updateFormContainer(page, _form, uiform);
                        } catch (final EFapsException e) {
                            throw new RestartResponseException(new ErrorPage(e));
                        }
                        _target.add(_form);
                    }
                });
            }
        }
    }
}
