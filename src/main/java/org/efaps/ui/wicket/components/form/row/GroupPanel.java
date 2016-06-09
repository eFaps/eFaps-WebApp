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

package org.efaps.ui.wicket.components.form.row;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.form.command.CommandCellPanel;
import org.efaps.ui.wicket.components.form.field.FieldPanel;
import org.efaps.ui.wicket.components.form.field.FieldSetPanel;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.IUIElement;
import org.efaps.ui.wicket.models.field.UICmdField;
import org.efaps.ui.wicket.models.field.UIGroup;
import org.efaps.ui.wicket.models.field.set.UIFieldSet;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class GroupPanel
    extends Panel
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new group panel.
     *
     * @param _wicketId the _wicket id
     * @param _model the _model
     * @param _uiForm the _ui form
     * @param _formContainer the _form container
     * @throws EFapsException the e faps exception
     */
    public GroupPanel(final String _wicketId,
                      final IModel<UIGroup> _model,
                      final UIForm _uiForm,
                      final FormContainer _formContainer)
        throws EFapsException
    {
        super(_wicketId, _model);
        final UIGroup group = (UIGroup) super.getDefaultModelObject();
        final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
        add(cellRepeater);
        for (final IUIElement object : group.getFields()) {
            if (object instanceof UICmdField) {
                final CommandCellPanel fieldSet = new CommandCellPanel(cellRepeater.newChildId(),
                                Model.of((UICmdField) object), _uiForm, _formContainer);
                cellRepeater.add(fieldSet);
            } else if (object instanceof AbstractUIField) {
                final FieldPanel field = new FieldPanel(cellRepeater.newChildId(), Model.of((AbstractUIField) object));
                cellRepeater.add(field);
            } else if (object instanceof UIFieldSet) {
                final FieldSetPanel fieldSet = new FieldSetPanel(cellRepeater.newChildId(),
                                Model.of((UIFieldSet) object));
                cellRepeater.add(fieldSet);
            }
        }
    }
}
