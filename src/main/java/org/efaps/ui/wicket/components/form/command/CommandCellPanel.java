/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.components.form.command;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.autocomplete.AutoCompleteComboBox;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.models.field.IAutoComplete;
import org.efaps.ui.wicket.models.field.UICmdField;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class CommandCellPanel
    extends Panel
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _formmodel model of the form containing this component
     * @param _form form containing this component
     * @throws EFapsException on error
     */
    public CommandCellPanel(final String _wicketId,
                            final IModel<UICmdField> _model,
                            final UIForm _formmodel,
                            final FormContainer _form)
        throws EFapsException
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        final UICmdField uiField = (UICmdField) getDefaultModelObject();
        final Component auto;
        boolean add2Auto = false;

        if (uiField.isAutoComplete() && (_formmodel.isCreateMode() || _formmodel.isCreateMode() || _formmodel
                        .isSearchMode())) {
            auto = new AutoCompleteComboBox("autocomplete", Model.of((IAutoComplete) _model.getObject()), false);
            add2Auto = true;
        } else {
            auto = new WebComponent("autocomplete").setVisible(false);
        }
        add(auto);
        final WebMarkupContainer command = new WebMarkupContainer("command");
        add(command);
        if (uiField.isRenderButton()) {
            command.setVisible(false);
            EFapsContentReference reference = null;
            if (uiField.getButtonIcon() != null) {
                reference = AjaxButton.ICON.valueOf(uiField.getButtonIcon()).getReference();
            }
            add(new AjaxExecuteLink("execute", _model, reference, uiField.getLabel()));
        } else {
            add(new WebComponent("execute").setVisible(false));

            final Component targetBottom = new LabelComponent("targetBottom", "").setVisible(false);
            command.setOutputMarkupId(true);
            command.add(targetBottom);

            final AjaxCmdBehavior cmdBehavior = new AjaxCmdBehavior(_form, targetBottom);
            final EsjpAjaxComponent esjpComp = new EsjpAjaxComponent("renderedExecute", _model);
            esjpComp.add(cmdBehavior);
            command.add(esjpComp);

            if (add2Auto) {
                ((AutoCompleteComboBox) auto).add(cmdBehavior);
            }
        }
    }
}
