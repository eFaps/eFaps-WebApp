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

import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.BigDecimalConverter;
import org.apache.wicket.util.convert.converter.DoubleConverter;
import org.apache.wicket.util.convert.converter.IntegerConverter;
import org.apache.wicket.util.convert.converter.LongConverter;
import org.efaps.admin.datamodel.IAttributeType;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.datamodel.attributetype.IntegerType;
import org.efaps.admin.datamodel.attributetype.LongType;
import org.efaps.admin.datamodel.attributetype.RealType;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.db.Context;
import org.efaps.ui.wicket.models.cell.CellSetValue;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.field.AbstractUIField;
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
public class NumberField
    extends AbstractField<Number>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(NumberField.class);

    /**
     * Was the value already converted.
     */
    private boolean converted = false;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this componet
     * @param _config Config
     * @throws EFapsException on error
     */
    public NumberField(final String _wicketId,
                       final Model<AbstractUIField> _model,
                       final FieldConfiguration _config)
        throws EFapsException
    {
        super(_wicketId, _model, _config);
    }

    @Override
    protected void convertInput()
    {
        this.converted = true;
        int i = 0;
        if (getCellvalue() instanceof CellSetValue) {
            final UIFormCellSet cellset = ((CellSetValue) getCellvalue()).getCellSet();
            i = cellset.getIndex(getInputName());
        }
        final String[] value = getInputAsArray();
        try {
            IConverter<? extends Number> converter = LongConverter.INSTANCE;
            if (value != null && value.length > 0 && value[i] != null
                            && getCellvalue().getValue().getAttribute() != null) {
                final IAttributeType attrType = getCellvalue().getValue().getAttribute().getAttributeType()
                                .getDbAttrType();
                if (attrType instanceof LongType) {
                    converter = LongConverter.INSTANCE;
                } else if (attrType instanceof IntegerType) {
                    converter = IntegerConverter.INSTANCE;
                } else if (attrType instanceof RealType) {
                    converter = DoubleConverter.INSTANCE;
                } else if (attrType instanceof DecimalType) {
                    converter = new BigDecimalConverter();
                }
            }
            setConvertedInput(converter.convertToObject(value[i], Context.getThreadContext().getLocale()));
        } catch (final ConversionException e) {
            error(newValidationError(e));
        } catch (final CacheReloadException e) {
            NumberField.LOG.error("Catched error on convertInput", e);
        } catch (final EFapsException e) {
            NumberField.LOG.error("Catched error on convertInput", e);
        }
    }

    @Override
    public void updateModel()
    {
        if (!this.converted) {
            convertInput();
        }
        setModelObject(getConvertedInput());
        try {
            getCellvalue().setValue(UIValue.get(getCellvalue().getValue().getField(), getCellvalue().getValue()
                            .getAttribute(), getDefaultModelObject()));
        } catch (final CacheReloadException e) {
            NumberField.LOG.error("Catched error on updateModel", e);
        }
    }
}
