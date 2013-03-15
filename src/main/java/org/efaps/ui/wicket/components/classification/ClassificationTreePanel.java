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

package org.efaps.ui.wicket.components.classification;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.models.objects.IFormElement;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.models.objects.UIFieldForm;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.Element;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
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
 * @version $Id$
 */
public class ClassificationTreePanel
    extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the style sheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(ClassificationPath.class,
                    "ClassificationTree.css");

    private boolean changed = false;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this component
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
        add(new Button("submitClose", new AjaxSubmitCloseLink(Button.LINKID, _model),
                        label, Button.ICON.ACCEPT.getReference()));
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
        extends AjaxLink<UIClassification>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id for this component
         * @param _model model for tihs component
         */
        public AjaxSubmitCloseLink(final String _wicketId,
                                   final IModel<UIClassification> _model)
        {
            super(_wicketId, _model);
        }

        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target ajax request target
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            _target.add(findParent(ClassificationPathPanel.class));
            _target.add(findParent(ClassificationTreePanel.class).setVisible(false));
            if (ClassificationTreePanel.this.changed) {
                final Page page = getPage();
                final UIForm uiform = (UIForm) page.getDefaultModelObject();
                final ComponentHierarchyIterator visitor = page.visitChildren(FormContainer.class);

                final FormContainer form = (FormContainer) visitor.next();

                form.removeAll();
                // remove previous added classification forms
                final Iterator<Element> iter2 = uiform.getElements().iterator();
                final Map<UUID, String> uuid2InstanceKey = new HashMap<UUID, String>();
                while (iter2.hasNext()) {
                    final IFormElement element = iter2.next().getElement();
                    if (element instanceof UIFieldForm) {
                        final String instanceKey = ((UIFieldForm) element).getInstanceKey();
                        if (instanceKey != null) {
                            final UUID classUUID = ((UIFieldForm) element).getClassificationUUID();
                            uuid2InstanceKey.put(classUUID, instanceKey);
                        }
                        iter2.remove();
                    }
                }
                try {
                    add2Elements(uiform, (UIClassification) getDefaultModelObject(), uuid2InstanceKey);
                    FormPage.updateFormContainer(page, form, uiform);
                } catch (final EFapsException e) {
                    throw new RestartResponseException(new ErrorPage(e));
                }
                _target.add(form);
            }
        }

        /**
         * Recursive method that adds the classification forms as elements to
         * the form by walking down the tree.
         *
         * @param _uiForm uiForm the elements must be added to
         * @param _parentClass the classification to be added
         * @param _uuid2InstanceKey map from uuid to instance keys
         * @throws EFapsException on error
         */
        private void add2Elements(final UIForm _uiForm,
                                  final UIClassification _parentClass,
                                  final Map<UUID, String> _uuid2InstanceKey)
            throws EFapsException
        {
            if (_parentClass.isSelected()) {
                final UIFieldForm fieldform;
                if (_uiForm.isEditMode()) {
                    if (_uuid2InstanceKey.containsKey(_parentClass.getClassificationUUID())) {
                        fieldform = new UIFieldForm(_uiForm.getCommandUUID(),
                                        _uuid2InstanceKey.get(_parentClass.getClassificationUUID()));
                    } else {
                        fieldform = new UIFieldForm(_uiForm.getCommandUUID(), _parentClass);
                        fieldform.setMode(TargetMode.CREATE);
                    }
                } else {
                    fieldform = new UIFieldForm(_uiForm.getCommandUUID(), _parentClass);
                }
                _uiForm.getElements().add(new Element(ElementType.SUBFORM, fieldform));
            }
            for (final UIClassification child : _parentClass.getChildren()) {
                add2Elements(_uiForm, child, _uuid2InstanceKey);
            }
        }
    }
}
