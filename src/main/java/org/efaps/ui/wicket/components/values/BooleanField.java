/*
 * Copyright 2003 - 2014 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.values;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.request.EFapsRequestParametersAdapter;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class BooleanField
    extends Panel
    implements IValueConverter, IFieldConfig
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Configuration for this field.
     */
    private final FieldConfiguration fieldConfiguration;

    /**
     * @param _wicketId wicket id for this component
     * @param _value value of this component
     * @param _choices choices
     * @param _fieldConfiguration configuration for this field
     */
    public BooleanField(final String _wicketId,
                        final Object _value,
                        final IModel<Map<Object, Object>> _choices,
                        final FieldConfiguration _fieldConfiguration)
    {
        super(_wicketId);
        setOutputMarkupId(true);
        this.fieldConfiguration = _fieldConfiguration;
        final RadioGroup<Boolean> radioGroup = new RadioGroup<Boolean>("radioGroup");
        if (_value == null) {
            radioGroup.setDefaultModel(new Model<Boolean>());
        } else {
            radioGroup.setDefaultModel(Model.of((Boolean) _value));
        }
        add(radioGroup);
        final Iterator<Entry<Object, Object>> iter = _choices.getObject().entrySet().iterator();

        final Entry<Object, Object> first = iter.next();
        final Radio<Boolean> radio1 = new Radio<Boolean>("choice1", Model.of((Boolean) first.getValue()));
        radio1.setLabel(Model.of((String) first.getKey()));
        radioGroup.add(radio1);
        radioGroup.add(new Label("label1", Model.of((String) first.getKey())));

        final Entry<Object, Object> second = iter.next();
        final Radio<Boolean> radio2 = new Radio<Boolean>("choice2", Model.of((Boolean) second.getValue()));
        radio2.setLabel(Model.of((String) second.getKey()));
        radioGroup.add(radio2);
        radioGroup.add(new Label("label2", Model.of((String) second.getKey())));
    }

    /**
     * Getter method for the instance variable {@link #fieldConfiguration}.
     *
     * @return value of instance variable {@link #fieldConfiguration}
     */
    public FieldConfiguration getFieldConfiguration()
    {
        return this.fieldConfiguration;
    }

    @Override
    protected void onAfterRender()
    {
        super.onAfterRender();
        final FormContainer frmContainer = findParent(FormContainer.class);
        if (frmContainer != null) {
            frmContainer.addValueConverter(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void convertValue(final EFapsRequestParametersAdapter _parameters)
        throws EFapsException
    {
        visitChildren(RadioGroup.class, new IVisitor<RadioGroup<?>, Void>() {

            @Override
            public void component(final RadioGroup<?> _group,
                                  final IVisit<Void> _visit)
            {
                try {
                    if (_group.getDefaultModelObject() == Boolean.TRUE) {
                        _parameters.addParameterValue(getFieldConfiguration().getName(), "true");
                    } else {
                        _parameters.addParameterValue(getFieldConfiguration().getName(), "false");
                    }
                } catch (final EFapsException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FieldConfiguration getFieldConfig()
    {
        return this.fieldConfiguration;
    }
}
