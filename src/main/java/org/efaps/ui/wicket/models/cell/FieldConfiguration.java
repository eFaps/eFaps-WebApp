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


package org.efaps.ui.wicket.models.cell;

import java.io.Serializable;

import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.field.Field;
import org.efaps.ui.wicket.models.UIType;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldConfiguration
    implements Serializable
{

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
        throws EFapsException
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
     * @return the size of the field
     */
    public int getSize()
    {
        return getField().getCols();
    }
    /**
     * @return the label of this field
     */
    public String getLabel()
    {
        String ret;
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
     * @return must the label been hidden
     */
    public boolean isHideLabel()
    {
        return getField().isHideLabel();
    }

    /**
     * @param _abstractUIField field object this labelconfig belongs to
     * @return the label for the UserInterface
     * @throws CacheReloadException on error
     */
    public String getLabel(final AbstractUIField _abstractUIField)
        throws CacheReloadException
    {
        String key;
        if (getField().getLabel() == null) {
            if (_abstractUIField.getValue() != null && _abstractUIField.getValue().getAttribute() != null) {
                key = _abstractUIField.getValue().getAttribute().getLabelKey();
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
     * @return the UIType
     */
    public UIType getUIType()
    {
        UIType ret;
        final String uiTypeStr = getField().getProperty("UIType");
        if (EnumUtils.isValidEnum(UIType.class, uiTypeStr)) {
            ret = UIType.valueOf(uiTypeStr);
        } else {
            ret = UIType.DEFAULT;
        }
        return ret;
    }
}
