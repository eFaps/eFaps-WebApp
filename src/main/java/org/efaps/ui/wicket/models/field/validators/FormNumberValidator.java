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
package org.efaps.ui.wicket.models.field.validators;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.FormValidatorAdapter;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.datamodel.ui.NumberUI;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.models.field.AbstractUIField;

/**
 * The Class NumberValidator.
 *
 * @author The eFaps Team
 */
public class FormNumberValidator
    extends FormValidatorAdapter
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new number validator.
     *
     * @param _uiField the ui field
     */
    public FormNumberValidator(final AbstractUIField _uiField)
    {
        super(new IFormValidator()
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public FormComponent<?>[] getDependentFormComponents()
            {
                return null;
            }

            @Override
            public void validate(final Form<?> _form)
            {
                final List<StringValue> list = RequestCycle.get().getRequest().getRequestParameters()
                                .getParameterValues(_uiField.getFieldConfiguration().getName());
                boolean hasValue = false;
                if (CollectionUtils.isNotEmpty(list)) {
                    for (final StringValue value : list) {
                        if (!value.isEmpty()) {
                            final String message = new NumberUI().validateValue(UIValue.get(null, null, value
                                            .toString()));
                            if (message != null) {
                                _form.error(_uiField.getFieldConfiguration().getLabel() + ": " + message);
                            }
                            hasValue = true;
                        }
                    }
                }
                if (!hasValue && _uiField.getFieldConfiguration().getField().isRequired()) {
                    _form.error(DBProperties.getFormatedDBProperty(FormNumberValidator.class.getName()
                                    + ".FieldRequired", (Object) _uiField.getFieldConfiguration().getLabel()));
                }
            }
        });
    }
}
