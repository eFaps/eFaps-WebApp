/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.models.field;

import java.io.Serializable;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.Table;
import org.efaps.admin.ui.field.Field;
import org.efaps.api.ci.UIFormFieldProperty;
import org.efaps.api.ui.UIType;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class FieldConfiguration
    implements Serializable
{

    /**
     * Logger.
     */
    private static final Logger LOG =  LoggerFactory.getLogger(FieldConfiguration.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Id of the field the configuration belongs to.
     */
    private final long fieldId;

    /**
     * Label for the related field.
     */
    private String label;

    /**
     * @param _fieldId id of the field
     */
    public FieldConfiguration(final long _fieldId)
    {
        this.fieldId = _fieldId;
    }

    /**
     * @return the name of this configuration
     * @throws EFapsException on error
     */
    public String getName()
    {
        return getField().getName();
    }

    /**
     * @return the field this configuration belongs to
     */
    public Field getField()
    {
        return Field.get(this.fieldId);
    }

    /**
     * @return the alignment of the field
     */
    public String getAlign()
    {
        return getField().getAlign();
    }

    /**
     * @return the alignment of the field
     */
    public int getRowSpan()
    {
        return getField().getRowSpan();
    }

    /**
     * @return the alignment of the field
     */
    public int getColSpan()
    {
        return getField().containsProperty(UIFormFieldProperty.COL_SPAN.value())
                        ? Integer.valueOf(getField().getProperty(UIFormFieldProperty.COL_SPAN.value())) : 1;
    }

    /**
     * @return the size of the field
     */
    public String getWidth()
    {
        String ret = "";
        if (hasProperty(UIFormFieldProperty.WIDTH.value())) {
            final String widthTmp = getProperty(UIFormFieldProperty.WIDTH.value());
            if (StringUtils.isNumeric(widthTmp)) {
                ret = widthTmp + "ch";
            } else {
                ret = widthTmp;
            }
        }
        return ret;
    }

    /**
     * Gets the width weight.
     *
     * @return the width weight
     */
    public int getWidthWeight()
    {
        int ret = 1;
        if (!isFixedWidth() && hasProperty(UIFormFieldProperty.WIDTH.value())) {
            ret = Integer.valueOf(getProperty(UIFormFieldProperty.WIDTH.value()));
        }
        return ret;
    }

    /**
     * Checks for property.
     *
     * @param _enum the _enum
     * @return true, if successful
     */
    public boolean hasProperty(final String _enum)
    {
        return getField().containsProperty(_enum);
    }

    /**
     * Gets the property.
     *
     * @param _enum the _enum
     * @return the property
     */
    public String getProperty(final String _enum)
    {
        return getField().getProperty(_enum);
    }

    /**
     * @return the size of the field
     */
    public boolean isFixedWidth()
    {
        return hasProperty(UIFormFieldProperty.WIDTH.value())
                        && !StringUtils.isNumeric(getField().getProperty(UIFormFieldProperty.WIDTH.value()));
    }

    /**
     * @return the rows of the field
     */
    public int getRows()
    {
        return getField().getRows();
    }

    /**
     * @return the label of this field
     */
    public String getLabel()
    {
        final String ret;
        if (this.label == null) {
            if (getField().getLabel() == null) {
                ret = "";
            } else {
                ret =  DBProperties.getProperty(getField().getLabel());
            }
        } else {
            ret = this.label;
        }
        return ret;
    }

    /**
     * Evaluate the label.
     *
     * @param _uiValue the _ui value
     * @param _fieldInst the _field inst
     * @return the label
     * @throws CacheReloadException the cache reload exception
     */
    public String evalLabel(final UIValue _uiValue,
                            final Instance _fieldInst)
        throws CacheReloadException
    {
        final String key;
        if (getField().getLabel() == null) {
            if (_uiValue != null && _uiValue.getAttribute() != null) {
                if (_fieldInst != null && _fieldInst.isValid()
                                && _fieldInst.getType()
                                                .getAttribute(_uiValue.getAttribute().getName()) != null) {
                    key = _fieldInst.getType().getAttribute(_uiValue.getAttribute().getName()).getLabelKey();
                } else if (_uiValue.getInstance() != null
                                && _uiValue.getInstance().getType()
                                                .getAttribute(_uiValue.getAttribute().getName()) != null) {
                    key = _uiValue.getInstance().getType().getAttribute(_uiValue.getAttribute().getName())
                                    .getLabelKey();
                } else {
                    key = _uiValue.getAttribute().getLabelKey();
                }
            } else {
                key = FieldConfiguration.class.getName() + ".NoLabel";
            }
        } else {
            key = getField().getLabel();
        }
        this.label = DBProperties.getProperty(key);
        return this.label;
    }

    /**
     * @return must the label been hidden
     */
    public boolean isHideLabel()
    {
        return getField().isHideLabel();
    }

    /**
     * @return the UIType
     */
    public UIType getUIType()
    {
        final UIType ret;
        final String uiTypeStr = getProperty(UIFormFieldProperty.UI_TYPE.value());
        if (EnumUtils.isValidEnum(UIType.class, uiTypeStr)) {
            ret = UIType.valueOf(uiTypeStr);
        } else {
            ret = UIType.DEFAULT;
        }
        return ret;
    }

    /**
     * Sets the label for the related field.
     *
     * @param _label the new label for the related field
     */
    public void setLabel(final String _label)
    {
        this.label = _label;
    }

    /**
     * Checks if is table.
     *
     * @return true, if is table
     */
    public boolean isTableField()
    {
        boolean ret = false;
        try {
            ret = getField().getCollection() instanceof Table;
        } catch (final CacheReloadException e) {
            LOG.error("CacheReloadException", e);
        }
        return ret;
    }

    /**
     * Gets the field config.
     *
     * @return the field config
     */
    public static FieldConfiguration getSimFieldConfig(final String _fieldName)
    {
        final Field field = new Field(0, "", _fieldName);
        final FieldConfiguration ret = new FieldConfiguration(0)
        {
            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public Field getField()
            {
                return field;
            }
        };
        return ret;
    }
}
