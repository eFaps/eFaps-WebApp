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

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.form.valuepicker.ValuePicker;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.models.objects.UIFieldForm;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.Element;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
import org.efaps.ui.wicket.pages.classification.ClassificationPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationPathPanel extends Panel
{
    public static final EFapsContentReference ICON = new EFapsContentReference(ValuePicker.class, "valuepicker.png");

    /** Needed for serialization. */
    private static final long serialVersionUID = 1L;

    private final ModalWindowContainer modal;

    private boolean updateForm = false;

    /**
     * Getter method for instance variable {@link #updateForm}.
     *
     * @return value of instance variable {@link #updateForm}
     */
    public boolean isUpdateForm()
    {
        return this.updateForm;
    }

    /**
     * Setter method for instance variable {@link #updateForm}.
     *
     * @param updateForm value for instance variable {@link #updateForm}
     */
    public void setUpdateForm(final boolean updateForm)
    {
        this.updateForm = updateForm;
    }

    /**
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     */
    public ClassificationPathPanel(final String _wicketId, final IModel<UIClassification> _model)
    {
        super(_wicketId, _model);
        this.add(new ClassificationPath("path", _model));
        if (_model.getObject().getMode().equals(TargetMode.EDIT)
                        || _model.getObject().getMode().equals(TargetMode.CREATE)) {
            this.add(new ClassTreeOpener("button", _model));
        } else {
            this.add(new WebMarkupContainer("button").setVisible(false));
        }

        this.modal = new ModalWindowContainer("modal");
        add(this.modal);
        this.modal.reset();
        this.modal.setPageMapName("modal-2");
        this.modal.setWindowClosedCallback(new UpdateCallback());
        // it must be used a Page Creator, because only a modal window using a
        // page creator can be moved over the whole srceen
        this.modal.setPageCreator(new ModalWindowContainer.PageCreator()
        {
            /** Needed for serialization */
            private static final long serialVersionUID = 1L;

            public Page createPage()
            {
                return new ClassificationPage(_model, ClassificationPathPanel.this);
            }
        });
    }

    /**
     * @return
     */
    public ModalWindowContainer getModal()
    {
        return this.modal;
    }

    public class ClassTreeOpener extends WebComponent
    {

        /**
         * @param id
         */
        public ClassTreeOpener(final String _wicketId, final IModel<UIClassification> _model)
        {
            super(_wicketId, _model);
            this.add(new AjaxOpenClassTreeBehavior());
        }

        /**
         * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
         *      org.apache.wicket.markup.ComponentTag)
         * @param _markupStream
         * @param _openTag
         */
        @Override
        protected void onComponentTagBody(final MarkupStream _markupStream, final ComponentTag _openTag)
        {
            super.onComponentTagBody(_markupStream, _openTag);
            final StringBuilder html = new StringBuilder();
            html.append("<img alt=\"\" src=\"").append(ICON.getImageUrl()).append("\"/>");
            replaceComponentTagBody(_markupStream, _openTag, html);
        }

    }

    public class AjaxOpenClassTreeBehavior extends AjaxEventBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public AjaxOpenClassTreeBehavior()
        {
            super("onclick");
        }

        /**
         * This Method returns the JavaScript which is executed by the
         * JSCooKMenu.
         *
         * @return String with the JavaScript
         */
        public String getJavaScript()
        {
            final String script = super.getCallbackScript().toString();
            return "javascript:" + script.replace("'", "\"");
        }

        /**
         * Show the modal window.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            ClassificationPathPanel.this.updateForm = false;
            ClassificationPathPanel.this.modal.show(_target);
        }

        /**
         * Method must be overwritten, otherwise the default would break the
         * execution of the JavaScript.
         *
         * @return null
         */
        @Override
        protected CharSequence getPreconditionScript()
        {
            return null;
        }
    }

    public class UpdateCallback implements ModalWindow.WindowClosedCallback {

        /**
         * @see org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback#onClose(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target
         */
        public void onClose(final AjaxRequestTarget _target)
        {
            if (ClassificationPathPanel.this.updateForm) {
                final Page page = getPage();
                final UIForm uiform = (UIForm) page.getDefaultModelObject();
                final Iterator<? extends Component> iter = page.iterator();
                FormContainer form = null;
                while (iter.hasNext()) {
                    final Component comp = iter.next();
                    if (comp instanceof FormContainer) {
                        form = (FormContainer) comp;
                        break;
                    }
                }
                form.removeAll();
                // remove previous added classification forms
                final Iterator<Element> iter2 = uiform.getElements().iterator();
                while (iter2.hasNext()) {
                    if (iter2.next().getElement() instanceof UIFieldForm) {
                        iter2.remove();
                    }
                }
                add2Elements(uiform, (UIClassification) getDefaultModelObject());
                FormPage.updateFormContainer(page, form, uiform);
                _target.addComponent(form);
            }
        }

        /**
         * Recursive method that adds the classification forms as elements to the
         * form by walking down the tree.
         *
         * @param _uiForm
         * @param _parentClass
         */
        private void add2Elements(final UIForm _uiForm, final UIClassification _parentClass) {
            if (_parentClass.isSelected()) {
                final UIFieldForm fieldform = new UIFieldForm(_uiForm.getCommandUUID(), _parentClass);
                _uiForm.getElements().add(_uiForm.new Element(ElementType.SUBFORM, fieldform));
            }
            for (final UIClassification child : _parentClass.getChildren()) {
                add2Elements(_uiForm, child);
            }

        }
    }
}
