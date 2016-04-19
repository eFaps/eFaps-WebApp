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


package org.efaps.ui.wicket.components.autocomplete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.api.ui.IOption;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.behaviors.SetSelectedRowBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior.AutoCompleteField;
import org.efaps.ui.wicket.models.field.AutoCompleteSettings.EditValue;
import org.efaps.ui.wicket.models.field.IAutoComplete;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class AutoCompleteComboBox
    extends TextField<String>
    implements AutoCompleteField
{
    /**
     * Logger.
     */
    private static final Logger LOG =  LoggerFactory.getLogger(AutoCompleteField.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * the cell this automcoplete belongs to.
     */
    private final IAutoComplete autoComplete;

    /**
     * @param _wicketId     wicket id for this component
     * @param _model        model for this component
     * @param _selectRow    add selected row behavior
     */
    public AutoCompleteComboBox(final String _wicketId,
                                final IModel<IAutoComplete> _model,
                                final boolean _selectRow)
    {
        super(_wicketId, Model.of(""));
        this.autoComplete = _model.getObject();
        try {
            setLabel(Model.of(this.autoComplete.getLabel()));
        } catch (final EFapsException e) {
            AutoCompleteComboBox.LOG.error("Error in setting of label)", e);
        }
        final AutoCompleteBehavior acBehavior = new AutoCompleteBehavior(this.autoComplete.getAutoCompleteSetting());
        this.add(acBehavior);

        if (_selectRow) {
            this.add(new SetSelectedRowBehavior(this.autoComplete.getAutoCompleteSetting().getFieldName()));
        }
        if (this.autoComplete.isFieldUpdate()) {
            final AjaxFieldUpdateBehavior fieldUpdate = new AjaxFieldUpdateBehavior("domready",
                            Model.of(_model.getObject()), false);
            fieldUpdate.setDojoCall(true);
            this.add(fieldUpdate);
            acBehavior.addFieldUpdate(fieldUpdate);
        }
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("input");
        super.onComponentTag(_tag);
        _tag.put("value", "");
    }

    /**
     * Method to get the values from the esjp.
     *
     * @see org.apache.wicket.extensions.ajax.markup.html.autocomplete
     *      .AutoCompleteTextField#getChoices(java.lang.String)
     * @param _input input from the webform
     * @return iterator
     */
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Map<String, String>> getChoices(final String _input)
    {
        final List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
        try {
            final AbstractUIPageObject pageObject = (AbstractUIPageObject) getPage().getDefaultModelObject();
            final Map<String, String> uiID2Oid = pageObject == null ? null : pageObject.getUiID2Oid();
            final List<Return> returns = this.autoComplete.getAutoCompletion(_input, uiID2Oid);
            for (final Return aReturn : returns) {
                final Object ob = aReturn.get(ReturnValues.VALUES);
                if (ob instanceof List) {
                    retList.addAll((Collection<? extends Map<String, String>>) ob);
                }
            }
        } catch (final EFapsException e) {
            AutoCompleteComboBox.LOG.error("Error in getChoice()", e);
        }
        return retList.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getItemValue()
    {
        String ret = null;
        try {
            if (this.autoComplete != null && this.autoComplete.getParent().isEditMode()
                            && !EditValue.NONE.equals(this.autoComplete.getAutoCompleteSetting().getValue4Edit())) {
                final Instance instance = this.autoComplete.getInstance();
                if (instance != null && instance.isValid()) {
                    switch (this.autoComplete.getAutoCompleteSetting().getValue4Edit()) {
                        case OID:
                            ret = instance.getOid();
                            break;
                        case ID:
                            ret = String.valueOf(instance.getId());
                            break;
                        default:
                            break;
                    }
                }
            } else if (this.autoComplete != null
                            && (this.autoComplete.getParent().isEditMode()
                                            || this.autoComplete.getParent().isCreateMode())
                            && !this.autoComplete.getAutoCompleteSetting().isRequired()) {
                ret = this.autoComplete.getAutoCompleteValue();
            }
        } catch (final EFapsException e) {
            AutoCompleteComboBox.LOG.error("Error in getItemValue()", e);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getItemLabel()
    {
        String ret = null;
        if ((this.autoComplete.getParent().isEditMode() || this.autoComplete.getParent().isCreateMode())
                        && !EditValue.NONE.equals(this.autoComplete.getAutoCompleteSetting().getValue4Edit())) {
            try {
                ret = this.autoComplete.getAutoCompleteValue();
            } catch (final EFapsException e) {
                AutoCompleteComboBox.LOG.error("Error in getItemValue()", e);
            }
        }
        return ret;
    }

    @Override
    public String getInputName()
    {
        return this.autoComplete.getAutoCompleteSetting().getFieldName();
    }

    @Override
    public List<IOption> getTokens()
    {
        final List<IOption> ret = new ArrayList<>();
        if ((this.autoComplete.getParent().isEditMode() || this.autoComplete.getParent().isCreateMode())
                        && !EditValue.NONE.equals(this.autoComplete.getAutoCompleteSetting().getValue4Edit())) {
            try {
                ret.addAll(this.autoComplete.getTokens());
            } catch (final EFapsException e) {
                AutoCompleteComboBox.LOG.error("Error in getItemValue()", e);
            }
        }
        return ret;
    }
}
