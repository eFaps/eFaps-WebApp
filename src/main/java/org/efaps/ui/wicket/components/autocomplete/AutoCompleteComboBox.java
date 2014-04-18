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
import org.efaps.ui.wicket.behaviors.SetSelectedRowBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior.AutoCompleteField;
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
    extends TextField<UITableCell> implements AutoCompleteField
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
     * @param _string
     * @param _model
     * @param _b
     */
    public AutoCompleteComboBox(final String _wicketId,
                                final IModel<?> _model,
                                final boolean _selectRow)
    {
        super(_wicketId, Model.<UITableCell>of(_model));
        final UITableCell uiAbstractCell = (UITableCell) getDefaultModelObject();
        final String fieldName = uiAbstractCell.getName();

        this.add(new AutoCompleteBehavior());

        if (_selectRow) {
            this.add(new SetSelectedRowBehavior(fieldName));
        }
    }
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("input");
        final UITableCell uiAbstractCell = (UITableCell) getDefaultModelObject();
        super.onComponentTag(_tag);

//        if (this.cols > 0) {
//            _tag.put("size", this.cols);
//        }
        if (uiAbstractCell.getParent().isEditMode() || uiAbstractCell.getParent().isCreateMode()) {
            _tag.put("value", uiAbstractCell.getCellTitle());
        }
        _tag.append("class", "eFapsAutoComplete", " ");
    }

    @Override
    public String getFieldName()
    {
        final UITableCell uiAbstractCell = (UITableCell) getDefaultModelObject();
        return uiAbstractCell.getName();
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
        }
        return retList.iterator();
    }
}
