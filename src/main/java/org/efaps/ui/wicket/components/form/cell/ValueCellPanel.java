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

package org.efaps.ui.wicket.components.form.cell;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.behaviors.PreviousValueBehavior;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.autocomplete.AutoCompleteComboBox;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.editor.EditorPanel;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.picker.AjaxPickerButton;
import org.efaps.ui.wicket.components.picker.AjaxPickerLink;
import org.efaps.ui.wicket.components.table.cell.AjaxMenuContentLink;
import org.efaps.ui.wicket.components.table.cell.ContentContainerLink;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.field.IAutoComplete;
import org.efaps.ui.wicket.models.field.IPickable;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.util.EFapsException;

/**
 * Class renders a cell in a Form.
 *
 * @author The eFaps Team
 */
public class ValueCellPanel
    extends Panel
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of the component
     * @param _model model of the component
     * @param _formmodel model of the form
     * @param _ajaxLink is this panel an ajax link
     * @throws EFapsException on error
     */
    public ValueCellPanel(final String _wicketId,
                          final IModel<UIFormCell> _model,
                          final UIForm _formmodel,
                          final boolean _ajaxLink)
        throws EFapsException
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        final UIFormCell uiFormCell = (UIFormCell) super.getDefaultModelObject();
        uiFormCell.setComponent(this);
        // if we don't have a reference or we are inside a modal window
        if (uiFormCell.getReference() == null || _formmodel.getTarget().equals(Target.MODAL)) {
            if (uiFormCell.getIcon() == null) {
                this.add(new WebComponent("icon").setVisible(false));
            } else {
                this.add(new StaticImageComponent("icon", uiFormCell.getIcon()));
            }
            // in case of create or edit for a Date or DateTime that is editable
            if ((_formmodel.isCreateMode() || _formmodel.isEditMode())
                            && ("Date".equals(uiFormCell.getTypeName())
                                            || uiFormCell.getUiClass() instanceof DateUI
                                            || "DateTime".equals(uiFormCell.getTypeName())
                                          || uiFormCell.getUiClass() instanceof DateTimeUI)
                            && uiFormCell.getDisplay().equals(Display.EDITABLE)) {

                final DateTimePanel dateField = new DateTimePanel("label", uiFormCell.getCompareValue(),
                                                       uiFormCell.getName(), uiFormCell.getCellLabel(),
                                                       "DateTime".equals(uiFormCell.getTypeName())
                                                                       || uiFormCell.getUiClass() instanceof DateTimeUI,
                                                       uiFormCell.getField().getCols());
                if (uiFormCell.isFieldUpdate()) {
                    // the update behavior must be added to the inner text field
                    final Iterator<? extends Component> iter = dateField.iterator();
                    while (iter.hasNext()) {
                        final Component comp = iter.next();
                        if (comp instanceof DateTextField) {
                            comp.add(new AjaxFieldUpdateBehavior(uiFormCell.getFieldUpdateEvent(), _model, false));
                            break;
                        }
                    }
                }
                this.add(dateField);
                this.add(new WebComponent("valuePicker").setVisible(false));
            } else if ((_formmodel.isCreateMode() || _formmodel.isEditMode())
                            && "FormatedString".equals(uiFormCell.getTypeName())
                            && uiFormCell.getDisplay().equals(Display.EDITABLE)) {
                this.add(new EditorPanel("label", _model));
                this.add(new WebComponent("valuePicker").setVisible(false));
            } else {
                final Component label;
                // check for AutoComplete field
                if (uiFormCell.isAutoComplete() && uiFormCell.getDisplay().equals(Display.EDITABLE)) {
                    label = new AutoCompleteComboBox("label", Model.of((IAutoComplete) _model.getObject()), false);
                } else {
                    label = new LabelComponent("label",
                                    new Model<String>(uiFormCell.getCellValue()));
                    if (uiFormCell.isFieldUpdate() && uiFormCell.getDisplay().equals(Display.EDITABLE)) {
                        label.add(new AjaxFieldUpdateBehavior(uiFormCell.getFieldUpdateEvent(), _model));
                        label.add(new PreviousValueBehavior().setCheck4Select(true));
                    }
                }
                this.add(label);
                // check if valuePicker must be addes
                if (uiFormCell.isValuePicker() && uiFormCell.getDisplay().equals(Display.EDITABLE)) {
                    if (uiFormCell.getPicker().isButton()) {
                        label.setVisible(false);
                        this.add(new AjaxPickerButton("valuePicker", Model.of((IPickable) _model.getObject())));
                    } else {
                        this.add(new AjaxPickerLink("valuePicker",  Model.of((IPickable) _model.getObject()), label));
                    }
                } else {
                    this.add(new WebComponent("valuePicker").setVisible(false));
                }
            }
            this.add(new WebMarkupContainer("link").setVisible(false));

        } else {
            this.add(new WebComponent("icon").setVisible(false));
            this.add(new WebComponent("label").setVisible(false));
            this.add(new WebComponent("valuePicker").setVisible(false));

            WebMarkupContainer link;
            if (_ajaxLink && uiFormCell.getTarget() != Target.POPUP) {
                link = new AjaxMenuContentLink("link", _model);
            } else {
                link = new ContentContainerLink<UIFormCell>("link", _model);
                if (uiFormCell.getTarget() == Target.POPUP) {
                    final PopupSettings popup = new PopupSettings("popup");
                    ((ContentContainerLink<?>) link).setPopupSettings(popup);
                }
            }
            if (uiFormCell.getIcon() == null) {
                link.add(new WebComponent("linkIcon").setVisible(false));
            } else {
                link.add(new StaticImageComponent("linkIcon", uiFormCell.getIcon()));
            }
            link.add(new LabelComponent("linkLabel", new Model<String>(uiFormCell.getCellValue())));
            this.add(link);
        }
    }
}
