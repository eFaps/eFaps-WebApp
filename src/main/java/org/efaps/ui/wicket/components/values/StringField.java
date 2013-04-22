/*
 * Copyright 2003 - 2011 The eFaps Team
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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.ui.wicket.models.cell.AbstractUICellValue;
import org.efaps.ui.wicket.models.cell.CellSetValue;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StringField
    extends TextField<String>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StringField.class);

    /**
     * FieldConfiguration for this field.
     */
    private final FieldConfiguration config;

    /**
     * Was the value already converted.
     */
    private boolean converted = false;

    /**
     * value of this field.
     */
    private final AbstractUICellValue cellvalue;

    /**
     * @param _wicketId wicket id fot this component
     * @param _model    model for this componet
     * @param _config   Config
     * @throws EFapsException on error
     */
    public StringField(final String _wicketId,
                       final Model<AbstractUICellValue> _model,
                       final FieldConfiguration _config)
        throws EFapsException
    {
        super(_wicketId, Model.of((String) _model.getObject().getValue()
                        .getEditValue(_model.getObject().getParent().getMode())));
        this.config = _config;
        this.cellvalue = _model.getObject();
    }


    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("input");
        _tag.append("style", "text-align:" + this.config.getAlign(), ";");
        _tag.put("size", this.config.getSize());
        super.onComponentTag(_tag);
    }

    @Override
    public String getInputName()
    {
        String ret = "";
        try {
            ret = this.config.getName();
        } catch (final EFapsException e) {
            StringField.LOG.error("EFapsException", e);
        }
        return ret;
    }

    @Override
    protected void convertInput()
    {
        this.converted = true;
        int i = 0;
        if (this.cellvalue instanceof CellSetValue) {
            final UIFormCellSet cellset = ((CellSetValue) this.cellvalue).getCellSet();
            i = cellset.getIndex(getInputName());
        }
        final String[] value = getInputAsArray();
        setConvertedInput(value != null && value.length > 0 && value[i] != null ? trim(value[i]) : null);
    }

    @Override
    public void updateModel()
    {
        if (!this.converted) {
            convertInput();
        }
        setModelObject(getConvertedInput());
        try {
            this.cellvalue.setValue(UIValue.get(this.cellvalue.getValue().getField(), this.cellvalue.getValue()
                            .getAttribute(), getDefaultModelObject()));
        } catch (final CacheReloadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

