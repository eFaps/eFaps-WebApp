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
package org.efaps.ui.wicket.models.field.factories;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.RateUI;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.values.HiddenField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * A factory for creating RateUI objects.
 */
@SuppressWarnings("checkstyle:abstractclassname")
public final class RateUIFactory
    extends AbstractUIFactory
{

    /**
     * Factory Instance.
     */
    private static RateUIFactory FACTORY;

    /**
     * Singelton.
     */
    private RateUIFactory()
    {
    }

    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        final Component ret = null;
        if (applies(_uiField)) {

        }
        return ret;
    }

    @Override
    public Component getHidden(final String _wicketId,
                               final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            ret = new AbstractRepeater(_wicketId) {

                /** The Constant serialVersionUID. */
                private static final long serialVersionUID = 1L;

                @Override
                protected Iterator<? extends Component> renderIterator()
                {
                    List<HiddenField> comps = null;
                    try {
                        final Object value = _uiField.getValue().getHiddenValue(_uiField.getParent().getMode());
                        System.out.println(value);
                        comps = Arrays.asList(new HiddenField(_wicketId, Model.of(_uiField), _uiField
                                        .getFieldConfiguration()), new HiddenField(_wicketId, Model.of(_uiField),
                                                        _uiField.getFieldConfiguration()));
                    } catch (final EFapsException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return comps == null ? IteratorUtils.emptyListIterator(): comps.iterator();
                }

                @Override
                protected void onPopulate()
                {
                }
            };
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applies(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return _abstractUIField.getValue().getUIProvider() instanceof RateUI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getReadOnlyValue(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        String strValue = "";
        final Object valueTmp = _abstractUIField.getValue()
                        .getReadOnlyValue(_abstractUIField.getParent().getMode());
        if (valueTmp instanceof Number) {
            final DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Context.getThreadContext()
                            .getLocale());
            strValue = formatter.format(valueTmp);
        }
        return strValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return RateUIFactory.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPickListValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return getReadOnlyValue(_uiField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return (Comparable<?>) _uiField.getValue().getReadOnlyValue(_uiField.getParent().getMode());
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (RateUIFactory.FACTORY == null) {
            RateUIFactory.FACTORY = new RateUIFactory();
        }
        return RateUIFactory.FACTORY;
    }
}
