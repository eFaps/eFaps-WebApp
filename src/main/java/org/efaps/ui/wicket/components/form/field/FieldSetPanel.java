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
package org.efaps.ui.wicket.components.form.field;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.datagrid.SetDataGrid;
import org.efaps.ui.wicket.models.field.set.UIFieldSet;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class FieldSetPanel
    extends Panel
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new field set panel.
     *
     * @param _wicketId the _wicket id
     * @param _model the _model
     */
    public FieldSetPanel(final String _wicketId,
                         final IModel<UIFieldSet> _model)
    {
        super(_wicketId, _model);
        final UIFieldSet uiFieldSet = _model.getObject();
        final boolean hideLabel = uiFieldSet.getFieldConfiguration().isHideLabel();
        final WebMarkupContainer labelField = new WebMarkupContainer("labelField");
        final WebMarkupContainer nonLabelField = new WebMarkupContainer("nonLabelField");
        add(labelField.setVisible(!hideLabel));
        add(nonLabelField.setVisible(hideLabel));
        final WebMarkupContainer container = hideLabel ? nonLabelField : labelField;
        container.add(new SetDataGrid("field", _model));
    }
}
