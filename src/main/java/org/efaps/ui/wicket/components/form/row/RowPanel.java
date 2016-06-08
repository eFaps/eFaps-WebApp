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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.form.field.FieldPanel;
import org.efaps.ui.wicket.components.form.field.FieldSetPanel;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.set.UIFieldSet;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.FormElement;
import org.efaps.ui.wicket.models.objects.UIForm.FormRow;
import org.efaps.util.EFapsException;

/**
 * TODO description!
 *
 * @author The eFaps Team
 */
public class RowPanel
    extends Panel
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _formmodel parent model of this row
     * @param _page page the RowPanel is in
     * @param _formPanel form panel this RowPanel is in
     * @param _form form this RowPanel is in
     * @param _formelementmodel element this rowpanel belongs to
     * @throws EFapsException on error
     *
     */
    public RowPanel(final String _wicketId,
                    final IModel<FormRow> _model,
                    final UIForm _formmodel,
                    final Page _page,
                    final FormPanel _formPanel,
                    final FormContainer _form,
                    final FormElement _formelementmodel)
        throws EFapsException
    {
        super(_wicketId, _model);

        final FormRow row = (FormRow) super.getDefaultModelObject();
        final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
        add(cellRepeater);
        for (final AbstractInstanceObject object : row.getValues()) {
            if (object instanceof AbstractUIField) {
                final FieldPanel field = new FieldPanel(cellRepeater.newChildId(), Model.of((AbstractUIField) object));
                field.add(AttributeModifier.replace("colspan",
                                ((AbstractUIField) object).getFieldConfiguration().getColSpan() * 2));
                if (((AbstractUIField) object).getFieldConfiguration().getRowSpan() > 0) {
                    field.add(AttributeModifier.replace("rowspan",
                                    ((AbstractUIField) object).getFieldConfiguration().getRowSpan()));
                }
                cellRepeater.add(field);
            } else if (object instanceof UIFieldSet) {
                final FieldSetPanel fieldSet = new FieldSetPanel(cellRepeater.newChildId(),
                                Model.of((UIFieldSet) object));
                cellRepeater.add(fieldSet);
            }
        }
    }
}
