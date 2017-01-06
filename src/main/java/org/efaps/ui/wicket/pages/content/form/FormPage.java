/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.ui.wicket.pages.content.form;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.classification.ClassificationPathPanel;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.heading.HeadingPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.header.HeaderPanel;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreeTablePanel;
import org.efaps.ui.wicket.models.field.IHidden;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.models.objects.UIFieldForm;
import org.efaps.ui.wicket.models.objects.UIFieldStructurBrowser;
import org.efaps.ui.wicket.models.objects.UIFieldTable;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.Element;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
import org.efaps.ui.wicket.models.objects.UIForm.FormElement;
import org.efaps.ui.wicket.models.objects.UIHeading;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 */
public class FormPage
    extends AbstractContentPage
{

    /**
     * Reference to the css stylesheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(FormPage.class, "FormPage.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 8884911406648729094L;

    /**
     * @param _model model for the page
     * @throws EFapsException on error
     */
    public FormPage(final IModel<?> _model)
        throws EFapsException
    {
        this(_model, (ModalWindowContainer) null);
    }

    /**
     * @param _model model for the page
     * @param _modalWindow modal window
     * @throws EFapsException on error
     */
    public FormPage(final IModel<?> _model,
                    final ModalWindowContainer _modalWindow)
        throws EFapsException
    {
        super(_model, _modalWindow);
        this.addComponents();
    }

    /**
     * @param _model            model for the page
     * @param _pageReference    PageReference
     * @throws EFapsException on error
     */
    public FormPage(final IModel<?> _model,
                    final PageReference _pageReference)
        throws EFapsException
    {
        this(_model, null, _pageReference);
    }

    /**
     * @param _model            model
     * @param _modalWindow      window
     * @param _pageReference    reference
     * @throws EFapsException on error
     */
    public FormPage(final IModel<?> _model,
                    final ModalWindowContainer _modalWindow,
                    final PageReference _pageReference)
        throws EFapsException
    {
        super(_model, _modalWindow, _pageReference);
        this.addComponents();
    }

    /**
     * Method to add the components to this page.
     *
     * @throws EFapsException on error
     */
    protected void addComponents()
        throws EFapsException
    {
        final UIForm uiForm = (UIForm) super.getDefaultModelObject();

        if (!uiForm.isInitialized()) {
            uiForm.execute();
        }

        final FormContainer form = new FormContainer("form");
        add(form);
        form.add(AttributeModifier.append("class", uiForm.getMode().toString()));
        if (uiForm.isMultiPart() && (uiForm.isCreateMode() || uiForm.isEditMode())) {
            form.setMultiPart(true);
            form.setMaxSize(getApplication().getApplicationSettings().getDefaultMaximumUploadSize());
        }

        super.addComponents(form);

        final WebMarkupContainer script = new WebMarkupContainer("selectscript");
        this.add(script);
        script.setVisible(uiForm.isCreateMode() || uiForm.isEditMode() || uiForm.isSearchMode());
        FormPage.updateFormContainer(this, form, uiForm);
    }

    /**
     * Method used to update the Form Container.
     *
     * @param _page page
     * @param _form formcontainer
     * @param _uiForm model
     * @throws EFapsException on error
     */
    public static void updateFormContainer(final Page _page,
                                           final FormContainer _form,
                                           final UIForm _uiForm)
        throws EFapsException
    {

        if (!_uiForm.isInitialized()) {
            _uiForm.execute();
        }
        // the hidden cells must be marked as not added yet.
        for (final IHidden cell : _uiForm.getHidden()) {
            cell.setAdded(false);
        }
        int i = 0;
        final RepeatingView elementRepeater = new RepeatingView("elementRepeater");
        _form.add(elementRepeater);
        for (final Element element : _uiForm.getElements()) {
            if (element.getType().equals(ElementType.FORM)) {
                elementRepeater.add(new FormPanel(elementRepeater.newChildId(), _page, Model.of(_uiForm),
                                (FormElement) element.getElement(), _form));
            } else if (element.getType().equals(ElementType.HEADING)) {
                final UIHeading headingmodel = (UIHeading) element.getElement();
                elementRepeater.add(new HeadingPanel(elementRepeater.newChildId(), Model.of(headingmodel)));
            } else if (element.getType().equals(ElementType.TABLE)) {
                i++;
                final UIFieldTable fieldTable = (UIFieldTable) element.getElement();
                fieldTable.setTableId(i);
                final TablePanel table = new TablePanel(elementRepeater.newChildId(),
                                Model.of(fieldTable), _page);
                final HeaderPanel header = new HeaderPanel(elementRepeater.newChildId(), table);
                elementRepeater.add(header);
                elementRepeater.add(table);
            } else if (element.getType().equals(ElementType.CLASSIFICATION)) {
                elementRepeater.add(new ClassificationPathPanel(elementRepeater.newChildId(),
                                Model.of((UIClassification) element.getElement())));
            } else if (element.getType().equals(ElementType.STRUCBRWS)) {
                i++;
                final UIFieldStructurBrowser strBrwsr = (UIFieldStructurBrowser) element.getElement();
                strBrwsr.setTableId(i);
                final StructurBrowserTreeTablePanel strucBrws = new StructurBrowserTreeTablePanel(
                                elementRepeater.newChildId(), Model.of(strBrwsr));
                elementRepeater.add(strucBrws);
            } else if (element.getType().equals(ElementType.SUBFORM)) {
                final UIFieldForm uiFieldForm = (UIFieldForm) element.getElement();
                if (!uiFieldForm.isInitialized()) {
                    if (uiFieldForm.getInstance() == null && uiFieldForm.isCreateMode()) {
                        uiFieldForm.setInstanceKey(_uiForm.getInstanceKey());
                    }
                    uiFieldForm.execute();
                }
                final List<Element> elements = uiFieldForm.getElements();
                for (final Element subElement : elements) {
                    if (subElement.getType().equals(ElementType.FORM)) {
                        elementRepeater.add(new FormPanel(elementRepeater.newChildId(), _page,
                                        Model.of(uiFieldForm), (FormElement) subElement.getElement(), _form));
                    } else if (subElement.getType().equals(ElementType.HEADING)) {
                        final UIHeading headingmodel = (UIHeading) subElement.getElement();
                        elementRepeater.add(new HeadingPanel(elementRepeater.newChildId(), Model.of(headingmodel)));
                    } else if (subElement.getType().equals(ElementType.TABLE)) {
                        i++;
                        final UIFieldTable fieldTable = (UIFieldTable) subElement.getElement();
                        fieldTable.setTableId(i);
                        final TablePanel table = new TablePanel(elementRepeater.newChildId(),
                                        Model.of(fieldTable), _page);
                        final HeaderPanel header = new HeaderPanel(elementRepeater.newChildId(), table);
                        elementRepeater.add(header);
                        elementRepeater.add(table);
                    }
                }
            }
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(FormPage.CSS));
    }
}
