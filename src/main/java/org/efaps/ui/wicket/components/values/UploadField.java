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

package org.efaps.ui.wicket.components.values;

import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;

/**
 * Field for uploading a file.
 *
 * @author The eFaps Team
 */
public class UploadField
    extends FormComponentPanel<AbstractUIField>
    implements IFieldConfig
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The field config. */
    private final FieldConfiguration fieldConfig;

    /**
     * Instantiates a new upload field.
     *
     * @param _wicketId the wicket id
     */
    public UploadField(final String _wicketId,
                       final IModel<AbstractUIField> _model)
    {
        super(_wicketId, _model);
        final Form<Void> form = new Form<>("form");
        add(form);
        final FileUploadField upload = new FileUploadField("upload"){

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public String getInputName()
            {
                return getFieldConfig().getName();
            }
        } ;
        form.add(upload);
        form.add(new UploadProgressBar("progress", form, upload));
        this.fieldConfig = _model.getObject().getFieldConfiguration();
    }

    @Override
    public FieldConfiguration getFieldConfig()
    {
        return this.fieldConfig;
    }

    @Override
    public IModel<String> getLabel()
    {
        return Model.<String>of(this.fieldConfig.getLabel());
    }
}
