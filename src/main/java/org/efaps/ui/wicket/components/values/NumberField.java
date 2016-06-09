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

package org.efaps.ui.wicket.components.values;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.BigDecimalConverter;
import org.apache.wicket.util.convert.converter.DoubleConverter;
import org.apache.wicket.util.convert.converter.IntegerConverter;
import org.apache.wicket.util.convert.converter.LongConverter;
import org.apache.wicket.util.value.IValueMap;
import org.efaps.admin.datamodel.IAttributeType;
import org.efaps.admin.datamodel.attributetype.DecimalType;
import org.efaps.admin.datamodel.attributetype.IntegerType;
import org.efaps.admin.datamodel.attributetype.LongType;
import org.efaps.admin.datamodel.attributetype.RealType;
import org.efaps.admin.datamodel.ui.DecimalUI;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.api.ci.UIFormFieldProperty;
import org.efaps.db.Context;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.field.set.UIFieldSet;
import org.efaps.ui.wicket.models.field.set.UIFieldSetValue;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
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
     * Any step should be set.
     */
    private boolean any;

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
        setRequired(_config.getField().isRequired());
        if (getUIField().getValue().getAttribute() != null) {
            final IAttributeType attrType = getUIField().getValue().getAttribute().getAttributeType()
                            .getDbAttrType();
            if (attrType instanceof DecimalType) {
                this.any = true;
            }
        } else if (getFieldConfig().getField().getUIProvider() != null) {
            final IUIProvider uiprovider = getFieldConfig().getField().getUIProvider();
            if (uiprovider instanceof DecimalUI) {
                this.any = true;
            }
        }
    }

    @Override
    public void convertInput()
    {
        this.converted = true;
        int i = 0;
        if (getUIField() instanceof UIFieldSetValue) {
            final UIFieldSet cellset = ((UIFieldSetValue) getUIField()).getCellSet();
            i = cellset.getIndex(getInputName());
        }
        final String[] value = getInputAsArray();
        try {
            if (value != null && value.length > 0 && value[i] != null) {
                IConverter<? extends Number> converter = LongConverter.INSTANCE;
                if (getUIField().getValue().getAttribute() != null) {
                    final IAttributeType attrType = getUIField().getValue().getAttribute().getAttributeType()
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
                } else if (getFieldConfig().getField().getUIProvider() != null) {
                    final IUIProvider uiprovider = getFieldConfig().getField().getUIProvider();
                    if (uiprovider instanceof DecimalUI) {
                        converter = new BigDecimalConverter();
                    }
                }
                setConvertedInput(converter.convertToObject(value[i], Context.getThreadContext().getLocale()));
            }
        } catch (final ConversionException e) {
            error(newValidationError(e).getErrorMessage(new ErrorMessageResource()));
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
            getUIField().setValue(UIValue.get(getUIField().getValue().getField(), getUIField().getValue()
                            .getAttribute(), getDefaultModelObject()));
        } catch (final CacheReloadException e) {
            NumberField.LOG.error("Catched error on updateModel", e);
        }
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        // must rempove teh html5 tags if not set explecitely
        final IValueMap attributes = _tag.getAttributes();
        if (getFieldConfig().getField().containsProperty(UIFormFieldProperty.NUMBER_MINIMUM)) {
            attributes.put("min", getFieldConfig().getField().getProperty(UIFormFieldProperty.NUMBER_MINIMUM));
        } else {
            attributes.remove("min");
        }
        if (getFieldConfig().getField().containsProperty(UIFormFieldProperty.NUMBER_MAXIMUM)) {
            attributes.put("max", getFieldConfig().getField().getProperty(UIFormFieldProperty.NUMBER_MAXIMUM));
        } else {
            attributes.remove("max");
        }
        if (getFieldConfig().getField().containsProperty(UIFormFieldProperty.NUMBER_STEP)) {
            attributes.put("step", getFieldConfig().getField().getProperty(UIFormFieldProperty.NUMBER_STEP));
        } else if (this.any) {
            attributes.put("step", "any");
        } else {
            attributes.remove("step");
        }
    }

    @Override
    protected String[] getInputTypes()
    {
        String[] ret;
        switch (getFieldConfig().getUIType()) {
            case NUMBER:
                ret = new String[] { "number" };
                break;
            case DEFAULT:
                if (this.any) {
                    ret = new String[] { "text" };
                } else {
                    ret = new String[] { "number" };
                }
                break;
            default:
                ret =  new String[] { "text" };
                break;
        }
        return ret;
    }
}
