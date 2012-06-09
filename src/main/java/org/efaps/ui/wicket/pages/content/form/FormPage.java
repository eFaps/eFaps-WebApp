/*
 * Copyright 2003 - 2012 The eFaps Team
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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content.form;

import java.util.List;
import java.util.UUID;

import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.classification.ClassificationPathPanel;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.heading.HeadingPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.header.HeaderPanel;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreeTablePanel;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.cell.UIHiddenCell;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.models.objects.UIFieldForm;
import org.efaps.ui.wicket.models.objects.UIFieldStructurBrowser;
import org.efaps.ui.wicket.models.objects.UIFieldTable;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.Element;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
import org.efaps.ui.wicket.models.objects.UIForm.FormElement;
import org.efaps.ui.wicket.models.objects.UIHeading;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id:FormPage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class FormPage
    extends AbstractContentPage
{

    /**
     * Reference to the css stylesheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(FormPage.class, "FormPage.css");

    /**
     * @param _model model for the page
     * @throws EFapsException on error
     */
    public FormPage(final IModel<?> _model,
                    final boolean _updateMenu)
        throws EFapsException
    {
        this(_model, null, _updateMenu);
    }

    /**
     * @param _commandUUID UUID of the command
     * @param _oid oid of the instance
     * @throws EFapsException on error
     */
    public FormPage(final UUID _commandUUID,
                    final String _oid,
                    final boolean _updateMenu)
        throws EFapsException
    {
        this(_commandUUID, _oid, (ModalWindowContainer) null, _updateMenu);
    }

    /**
     * @param _commandUUID UUID of the command
     * @param _oid oid of the instance
     * @param _modalWindow modal window of this page
     * @throws EFapsException on error
     */
    public FormPage(final UUID _commandUUID,
                    final String _oid,
                    final ModalWindowContainer _modalWindow,
                    final boolean _updateMenu)
        throws EFapsException
    {
        super(new FormModel(new UIForm(_commandUUID, _oid)), _modalWindow, _updateMenu);
        this.addComponents();
    }

    /**
     * @param _model model for the page
     * @param _modalWindow modal window
     * @throws EFapsException on error
     */
    public FormPage(final IModel<?> _model,
                    final ModalWindowContainer _modalWindow,
                    final boolean _updateMenu)
        throws EFapsException
    {
        super(_model, _modalWindow, _updateMenu);
        this.addComponents();
    }





    /**
     * @param _pageMap pagemap to be used
     * @param _commandUUID UUID of the command
     * @param _oid oid of the instance
     * @param _openerId id of the opener
     * @throws EFapsException on error
     */
    public FormPage(final UUID _commandUUID,
                    final String _oid,
                    final String _openerId)
        throws EFapsException
    {
        super(new FormModel(new UIForm(_commandUUID, _oid, _openerId)), null, false);
        this.addComponents();
    }

    /**
     * @param _model model for the page
     * @throws EFapsException on error
     */
    public FormPage(final IModel<?> _model,
                    final PageReference _pageReference)
        throws EFapsException
    {
        this(_model, null, _pageReference);
    }


    /**
     * @param _commandUUID
     * @param _instanceKey
     * @param _pageReference
     */
    public FormPage(final UUID _commandUUID,
                    final String _instanceKey,
                    final PageReference _pageReference)
        throws EFapsException
    {
        this(new FormModel(new UIForm(_commandUUID, _instanceKey)), null, _pageReference);
    }

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

        if (uiForm.isFileUpload() && (uiForm.isCreateMode() || uiForm.isEditMode())) {
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
        // in case of classification the different parts of the form a loaded
        // via ajax, that leads
        // to problems on parsing the dojo elements (EditorPanel) to prevent
        // this the dojo
        // scripts are loaded by default. Thats not the optimum, but normally
        // the scripts are
        // already in the cache of the browser
        // TODO Is there a better way?
        if (_uiForm.isClassified() && (_uiForm.isEditMode() || _uiForm.isCreateMode())) {
            // EditorPanel.prepare(_page);
        }
        // the hidden cells must be marked as not added yet.
        for (final UIHiddenCell cell : _uiForm.getHiddenCells()) {
            cell.setAdded(false);
        }
        int i = 0;
        final RepeatingView elementRepeater = new RepeatingView("elementRepeater");
        _form.add(elementRepeater);
        for (final Element element : _uiForm.getElements()) {
            if (element.getType().equals(ElementType.FORM)) {
                elementRepeater.add(new FormPanel(elementRepeater.newChildId(), _page, new FormModel(_uiForm),
                                (FormElement) element.getElement(), _form));
            } else if (element.getType().equals(ElementType.HEADING)) {
                final UIHeading headingmodel = (UIHeading) element.getElement();
                elementRepeater.add(new HeadingPanel(elementRepeater.newChildId(), headingmodel.getLabel(),
                                headingmodel.getLevel()));
            } else if (element.getType().equals(ElementType.TABLE)) {
                i++;
                final UIFieldTable fieldTable = (UIFieldTable) element.getElement();
                fieldTable.setTableId(i);
                final TablePanel table = new TablePanel(elementRepeater.newChildId(),
                                new TableModel(fieldTable), _page);
                final HeaderPanel header = new HeaderPanel(elementRepeater.newChildId(), table);
                elementRepeater.add(header);
                elementRepeater.add(table);
            } else if (element.getType().equals(ElementType.CLASSIFICATION)) {
                elementRepeater.add(new ClassificationPathPanel(elementRepeater.newChildId(),
                                new UIModel<UIClassification>((UIClassification) element.getElement())));
            } else if (element.getType().equals(ElementType.STRUCBRWS)) {
                final StructurBrowserTreeTablePanel strucBrws = new StructurBrowserTreeTablePanel(
                                elementRepeater.newChildId(), new UIModel<UIStructurBrowser>(
                                                (UIFieldStructurBrowser) element.getElement()), true);
                elementRepeater.add(strucBrws);
            } else if (element.getType().equals(ElementType.SUBFORM)) {
                final UIFieldForm uiFieldForm = (UIFieldForm) element.getElement();
                if (!uiFieldForm.isInitialized()) {
                    uiFieldForm.execute();
                }
                final List<Element> elements = uiFieldForm.getElements();
                for (final Element subElement : elements) {
                    if (subElement.getType().equals(ElementType.FORM)) {
                        elementRepeater.add(new FormPanel(elementRepeater.newChildId(), _page,
                                        new FormModel(uiFieldForm), (FormElement) subElement.getElement(), _form));
                    } else if (subElement.getType().equals(ElementType.HEADING)) {
                        final UIHeading headingmodel = (UIHeading) subElement.getElement();
                        elementRepeater.add(new HeadingPanel(elementRepeater.newChildId(), headingmodel.getLabel(),
                                        headingmodel.getLevel()));
                    } else if (subElement.getType().equals(ElementType.TABLE)) {
                        i++;
                        final UIFieldTable fieldTable = (UIFieldTable) subElement.getElement();
                        fieldTable.setTableId(i);
                        final TablePanel table = new TablePanel(elementRepeater.newChildId(),
                                        new TableModel(fieldTable), _page);
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
