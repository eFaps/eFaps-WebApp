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
package org.efaps.ui.wicket.components.values;

import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class IconLabelField
    extends Panel
    implements ILabelProvider<String>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new icon label field.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @param _label the label
     * @param _icon the icon
     * @throws EFapsException on error
     */
    public IconLabelField(final String _wicketId,
                          final IModel<AbstractUIField> _model,
                          final String _label,
                          final String _icon)
        throws EFapsException
    {
        super(_wicketId, _model);
        add(new StaticImageComponent("icon",  new EFapsContentReference(_icon)));
        add(new LabelField("labelField", Model.of(_label), _model.getObject()));
    }

    @Override
    public IModel<String> getLabel()
    {
        final IModel<String> ret = visitChildren(LabelField.class, new IVisitor<LabelField, IModel<String>>()
        {

            @Override
            public void component(final LabelField _field,
                                  final IVisit<IModel<String>> _visit)
            {
                _visit.stop(_field.getLabel());
            }
        });
        return ret;
    }
}
