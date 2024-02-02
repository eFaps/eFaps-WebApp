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
package org.efaps.ui.wicket.components.table.field;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.Model;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class CheckBoxField
    extends FieldPanel
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The oid. */
    private final String oid;

    /**
     * Instantiates a new check box field.
     *
     * @param _wicketId the _wicket id
     * @param _oid the _oid
     */
    public CheckBoxField(final String _wicketId,
                         final String _oid)
    {
        super(_wicketId);
        setDefaultModel(Model.of());
        this.oid = _oid;

        final Component field = new CheckBox("field", Model.of(Boolean.FALSE))
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                _tag.getAttributes().put("type", "checkbox");
                _tag.getAttributes().put("name", "selectedRow");
                _tag.getAttributes().put("value", CheckBoxField.this.oid);
                _tag.setName("input");
            }
        };
        add(field);
    }
}
