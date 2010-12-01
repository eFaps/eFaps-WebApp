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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

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
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.picker.AjaxPickerLink;
import org.efaps.ui.wicket.models.objects.IFormElement;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.models.objects.UIFieldForm;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.Element;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
import org.efaps.ui.wicket.pages.classification.ClassificationPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationPathPanel
    extends Panel
{

    /**
     * Reference to the style sheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(ClassificationPathPanel.class,
                                                                              "ClassificationPathPanel.css");

    /**
     * Reference to the Icon.
     */
    public static final EFapsContentReference ICON = new EFapsContentReference(AjaxPickerLink.class, "valuepicker.png");

    /** Needed for serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Modal window used to display the page containing the classification tree.
     */
    private final ModalWindowContainer modal;

    /**
     * Must the form be updated after closing the modal window containing the classification tree.
     */
    private boolean updateForm = false;

    /**
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     */
    public ClassificationPathPanel(final String _wicketId,
                                   final IModel<UIClassification> _model)
    {
        super(_wicketId, _model);
        add(StaticHeaderContributor.forCss(ClassificationPathPanel.CSS));
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
                Page ret;
                try {
                    ret = new ClassificationPage(_model, ClassificationPathPanel.this);
                } catch (final EFapsException e) {
                    ret = new ErrorPage(e);
                }
                return ret;
            }
        });
    }

    /**
     * Getter method for instance variable {@link #modal}.
     *
     * @return value of instance variable {@link #modal}
     */
    public ModalWindowContainer getModal()
    {
        return this.modal;
    }

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
     * @param _updateForm value for instance variable {@link #updateForm}
     */
    public void setUpdateForm(final boolean _updateForm)
    {
        this.updateForm = _updateForm;
    }

    /**
     * Class renders a button to open the form containing the classifcation tree.
     */
    public class ClassTreeOpener
        extends WebComponent
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id for this component
         * @param _model model for this component
         */
        public ClassTreeOpener(final String _wicketId,
                               final IModel<UIClassification> _model)
        {
            super(_wicketId, _model);
            this.add(new AjaxOpenClassTreeBehavior());
        }

        /**
         * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
         *      org.apache.wicket.markup.ComponentTag)
         * @param _markupStream markup stream
         * @param _openTag tag
         */
        @Override
        protected void onComponentTagBody(final MarkupStream _markupStream,
                                          final ComponentTag _openTag)
        {
            super.onComponentTagBody(_markupStream, _openTag);
            final StringBuilder html = new StringBuilder();
            html.append("<img alt=\"\" src=\"").append(ClassificationPathPanel.ICON.getImageUrl()).append("\"/>");
            replaceComponentTagBody(_markupStream, _openTag, html);
        }
    }

    /**
     * Behavior used to open the form with the classification tree.
     */
    public class AjaxOpenClassTreeBehavior
        extends AjaxEventBehavior
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
         * This Method returns the JavaScript which is executed by the JSCooKMenu.
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
         * Method must be overwritten, otherwise the default would break the execution of the JavaScript.
         *
         * @return null
         */
        @Override
        protected CharSequence getPreconditionScript()
        {
            return null;
        }
    }

    /**
     * Ajax callback that is called on closing the modal window.
     */
    public class UpdateCallback
        implements ModalWindow.WindowClosedCallback
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @see org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback#onClose(
         *  org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target ajax target
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
                } catch (final EFapsException e) {
                    getRequestCycle().setResponsePage(new ErrorPage(e));
                }
                FormPage.updateFormContainer(page, form, uiform);
                _target.addComponent(form);
                // TODO this should not be done always, needed for the editor so that it is loaded correctly
                _target.appendJavascript("dojo.parser.parse(document.body)");
            }
        }

        /**
         * Recursive method that adds the classification forms as elements to the form by walking down the tree.
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
                _uiForm.getElements().add(_uiForm.new Element(ElementType.SUBFORM, fieldform));
            }
            for (final UIClassification child : _parentClass.getChildren()) {
                add2Elements(_uiForm, child, _uuid2InstanceKey);
            }
        }
    }
}
