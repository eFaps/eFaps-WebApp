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
import org.efaps.db.Instance;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.behaviors.SetSelectedRowBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior.AutoCompleteField;
import org.efaps.ui.wicket.models.cell.AutoCompleteSettings.EditValue;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AutoCompleteComboBox
    extends TextField<UITableCell>
    implements AutoCompleteField
{

    /**
     * Logger.
     */
    private static final Logger LOG =  LoggerFactory.getLogger(AutoCompleteField.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId     wicket id for this component
     * @param _model        model for this component
     * @param _selectRow    add selected row behavior
     */
    public AutoCompleteComboBox(final String _wicketId,
                                final IModel<?> _model,
                                final boolean _selectRow)
    {
        super(_wicketId, Model.<UITableCell>of(_model));
        final UITableCell uiAbstractCell = (UITableCell) getDefaultModelObject();
        final String fieldName = uiAbstractCell.getName();

        final AutoCompleteBehavior autocomplete = new AutoCompleteBehavior(uiAbstractCell.getAutoCompleteSetting());
        this.add(autocomplete);

        if (_selectRow) {
            this.add(new SetSelectedRowBehavior(fieldName));
        }
        if (uiAbstractCell.isFieldUpdate()) {
            final AjaxFieldUpdateBehavior fieldUpdate = new AjaxFieldUpdateBehavior("domready", _model);
            fieldUpdate.setDojoCall(true);
            this.add(fieldUpdate);
            autocomplete.addFieldUpdate(fieldUpdate);
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
        final UITableCell uiObject = (UITableCell) getDefaultModelObject();
        final List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
        try {
            final AbstractUIPageObject pageObject = (AbstractUIPageObject) (getPage().getDefaultModelObject());
            final Map<String, String> uiID2Oid = pageObject == null ? null : pageObject.getUiID2Oid();
            final List<Return> returns = uiObject.getAutoCompletion(_input, uiID2Oid);
            for (final Return aReturn : returns) {
                final Object ob = aReturn.get(ReturnValues.VALUES);
                if (ob instanceof List) {
                    retList.addAll((Collection<? extends Map<String, String>>) ob);
                }
            }
        } catch (final EFapsException e) {
            AutoCompleteComboBox.LOG.error("Error in getChoice()", e);
        }  catch (final Exception e) {
            // not the prettiest way, but we must be sure to return the list iterator
            AutoCompleteComboBox.LOG.error("Unexcpected error:", e);
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
            final UITableCell uiAbstractCell = (UITableCell) getDefaultModelObject();
            if (uiAbstractCell != null && uiAbstractCell.getParent().isEditMode()
                            && !EditValue.NONE.equals(uiAbstractCell.getAutoCompleteSetting().getValue4Edit())) {
                final Instance instance = uiAbstractCell.getInstance();
                if (instance != null && instance.isValid()) {
                    switch (uiAbstractCell.getAutoCompleteSetting().getValue4Edit()) {
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
        final UITableCell uiAbstractCell = (UITableCell) getDefaultModelObject();
        if ((uiAbstractCell.getParent().isEditMode() || uiAbstractCell.getParent().isCreateMode())
                        && !EditValue.NONE.equals(uiAbstractCell.getAutoCompleteSetting().getValue4Edit())) {
            ret = uiAbstractCell.getCellValue();
        }
        return ret;
    }
}
