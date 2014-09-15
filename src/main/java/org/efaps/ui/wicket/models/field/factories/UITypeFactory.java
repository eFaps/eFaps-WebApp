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

package org.efaps.ui.wicket.models.field.factories;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.values.DropDownField;
import org.efaps.ui.wicket.components.values.RadioField;
import org.efaps.ui.wicket.components.values.SnippletField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.objects.DropDownOption;
import org.efaps.ui.wicket.models.objects.RadioOption;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public class UITypeFactory
    implements IComponentFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static UITypeFactory FACTORY;

    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            switch (_uiField.getFieldConfiguration().getUIType()) {
                case SNIPPLET:
                    Model<String> label = null;
                    if (!_uiField.getFieldConfiguration().isHideLabel()) {
                        label = Model.of(_uiField.getFieldConfiguration().getLabel());
                    }
                    final String html = String.valueOf(_uiField.getValue().getEditValue(
                                    _uiField.getParent().getMode()));
                    ret = new SnippletField(_wicketId, Model.of(html), label);
                    break;
                case DROPDOWN:
                    final List<DropDownOption> choices = DropDownOption.getChoices(_uiField.getValue()
                                    .getEditValue(_uiField.getParent().getMode()));
                    ret = new DropDownField(_wicketId, Model.of(_uiField), choices);
                    break;
                case RADIO:
                    final List<RadioOption> radios = RadioOption.getChoices(_uiField.getValue()
                                    .getEditValue(_uiField.getParent().getMode()));
                    ret = new RadioField(_wicketId, Model.of(_uiField), radios);
                    break;

                default:
                    break;
            }
        }
        return ret;
    }

    @Override
    public Component getReadOnly(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {

            switch (_uiField.getFieldConfiguration().getUIType()) {
                case SNIPPLET:
                    Model<String> label = null;
                    if (!_uiField.getFieldConfiguration().isHideLabel()) {
                        label = Model.of(_uiField.getFieldConfiguration().getLabel());
                    }
                    final String html = String.valueOf(_uiField.getValue().getReadOnlyValue(
                                    _uiField.getParent().getMode()));
                    ret = new SnippletField(_wicketId, Model.of(html), label);
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getHidden(final String _wicketId,
                               final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            switch (_uiField.getFieldConfiguration().getUIType()) {
                case SNIPPLET:
                    final String html = String.valueOf(_uiField.getValue().getReadOnlyValue(
                                    _uiField.getParent().getMode()));
                    ret = new SnippletField(_wicketId, Model.of(html), null);
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    protected boolean applies(final AbstractUIField _uiField)
        throws CacheReloadException
    {
        return  _uiField.getValue().getUIProvider() == null
                        && _uiField.getFieldConfiguration().getUIType() != null;
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (UITypeFactory.FACTORY == null) {
            UITypeFactory.FACTORY = new UITypeFactory();
        }
        return UITypeFactory.FACTORY;
    }
}
