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

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.api.ui.UIType;
import org.efaps.ui.wicket.components.picker.AjaxPickerButton;
import org.efaps.ui.wicket.components.values.CheckBoxField;
import org.efaps.ui.wicket.components.values.DropDownField;
import org.efaps.ui.wicket.components.values.NumberField;
import org.efaps.ui.wicket.components.values.RadioField;
import org.efaps.ui.wicket.components.values.SnippletField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.IPickable;
import org.efaps.ui.wicket.models.objects.CheckBoxOption;
import org.efaps.ui.wicket.models.objects.DropDownOption;
import org.efaps.ui.wicket.models.objects.RadioOption;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@SuppressWarnings("checkstyle:abstractclassname")
public final class UITypeFactory
    implements IComponentFactory
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
        return applies(_uiField) ? getEditableComp(_wicketId, _uiField) : null;
    }

    /**
     * Gets the editable comp.
     *
     * @param _wicketId the wicket id
     * @param _uiField the ui field
     * @return the editable comp
     * @throws EFapsException on error
     */
    protected Component getEditableComp(final String _wicketId,
                                        final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        switch (_uiField.getFieldConfiguration().getUIType()) {
            case SNIPPLET:
                if (!_uiField.getFieldConfiguration().isHideLabel()) {
                    _uiField.getFieldConfiguration().evalLabel(_uiField.getValue(), _uiField.getInstance());
                }
                final String html = String.valueOf(_uiField.getValue().getEditValue(
                                _uiField.getParent().getMode()));
                ret = new SnippletField(_wicketId, Model.of(html),  _uiField);
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
            case CHECKBOX:
                final List<CheckBoxOption> checkBoxes = CheckBoxOption.getChoices(_uiField,
                                _uiField.getValue().getEditValue(_uiField.getParent().getMode()));
                ret = new CheckBoxField(_wicketId, Model.of(_uiField), checkBoxes);
                break;
            case BUTTON:
                ret = new AjaxPickerButton(_wicketId, Model.<IPickable>of(_uiField));
                break;
            case NUMBER:
                ret = new NumberField(_wicketId, Model.of(_uiField), _uiField.getFieldConfiguration());
                break;
            default:
                break;
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
                    if (!_uiField.getFieldConfiguration().isHideLabel()) {
                        _uiField.getFieldConfiguration().evalLabel(_uiField.getValue(), _uiField.getInstance());
                    }
                    final String html = String.valueOf(_uiField.getValue().getReadOnlyValue(
                                    _uiField.getParent().getMode()));
                    ret = new SnippletField(_wicketId, Model.of(html), _uiField);
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
                    final String html = String.valueOf(_uiField.getValue().getHiddenValue(
                                    _uiField.getParent().getMode()));
                    ret = new SnippletField(_wicketId, Model.of(html), _uiField);
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    @Override
    public boolean applies(final AbstractUIField _uiField)
        throws CacheReloadException
    {
        return !UIType.DEFAULT.equals(_uiField.getFieldConfiguration().getUIType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return UITypeFactory.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPickListValue(final AbstractUIField _uiField)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return null;
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
