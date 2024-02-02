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
import java.util.HashSet;
import java.util.Set;

import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior.Type;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class AutoCompleteSettings
    implements Serializable
{

    /**
     * Editvalue definition.
     */
    public enum EditValue
    {
        /** Use the ID. */
        ID,
        /** Use the OID. (Default) */
        OID,
        /** Don't set the value for edit. */
        NONE;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Show the down arrow.
     */
    private boolean hasDownArrow = false;

    /**
     * Minimum input length for starting the GET request.
     */
    private int minInputLength = Configuration.getAttributeAsInteger(ConfigAttribute.AUTOC_MININPUT);

    /**
     * Max length for the Choice value.
     */
    private int maxChoiceLength = Configuration.getAttributeAsInteger(ConfigAttribute.AUTOC_MAXCHOICE);

    /**
     * Max length for the Value value.
     */
    private int maxValueLength = Configuration.getAttributeAsInteger(ConfigAttribute.AUTOC_MAXVALUE);

    /**
     * Max Result shown in the Interface. To deactivate set to -1.
     */
    private int maxResult = Configuration.getAttributeAsInteger(ConfigAttribute.AUTOC_MAXRESULT);

    /**
     * Name of the parameter.
     */
    private String paramName = Configuration.getAttribute(ConfigAttribute.AUTOC_PARAMNAME);

    /**
     * Delay in milliseconds between when user types something
     * and we start searching based on that value.
     */
    private int searchDelay =  Configuration.getAttributeAsInteger(ConfigAttribute.AUTOC_SEARCHDELAY);

    /**
     * Edit value definition.
     */
    private EditValue value4Edit = EditValue.OID;

    /**
     * Name of the extra parameters that will be send via GET.
     */
    private final Set<String> extraParameters = new HashSet<String>();

    /**
     * If required "AutoComplete" will be rendered, else "AutoSuggestion".
     */
    private boolean required = true;

    /**
     * The AutoComplete Type.
     */
    private AutoCompleteBehavior.Type autoType;

    /** The field configuration. */
    private FieldConfiguration fieldConfiguration;

    /**
     * Instantiates a new auto complete settings.
     */
    public AutoCompleteSettings()
    {
    }

    /**
     * Instantiates a new auto complete settings.
     *
     * @param _fieldConfiguration the field configuration
     */
    public AutoCompleteSettings(final FieldConfiguration _fieldConfiguration)
    {
        this.fieldConfiguration = _fieldConfiguration;
    }

    /**
     * Getter method for the instance variable {@link #hasDownArrow}.
     *
     * @return value of instance variable {@link #hasDownArrow}
     */
    public boolean isHasDownArrow()
    {
        return this.hasDownArrow;
    }

    /**
     * Setter method for instance variable {@link #hasDownArrow}.
     *
     * @param _hasDownArrow value for instance variable {@link #hasDownArrow}
     */
    public void setHasDownArrow(final boolean _hasDownArrow)
    {
        this.hasDownArrow = _hasDownArrow;
    }

    /**
     * Getter method for the instance variable {@link #minInputLength}.
     *
     * @return value of instance variable {@link #minInputLength}
     */
    public int getMinInputLength()
    {
        return this.minInputLength;
    }

    /**
     * Setter method for instance variable {@link #minInputLength}.
     *
     * @param _minInputLength value for instance variable {@link #minInputLength}
     */
    public void setMinInputLength(final int _minInputLength)
    {
        this.minInputLength = _minInputLength;
    }

    /**
     * Getter method for the instance variable {@link #searchDelay}.
     *
     * @return value of instance variable {@link #searchDelay}
     */
    public int getSearchDelay()
    {
        return this.searchDelay;
    }

    /**
     * Setter method for instance variable {@link #searchDelay}.
     *
     * @param _searchDelay value for instance variable {@link #searchDelay}
     */
    public void setSearchDelay(final int _searchDelay)
    {
        this.searchDelay = _searchDelay;
    }

    /**
     * Getter method for the instance variable {@link #parameterName}.
     *
     * @return value of instance variable {@link #parameterName}
     */
    public String getParamName()
    {
        return this.paramName;
    }

    /**
     * Setter method for instance variable {@link #parameterName}.
     *
     * @param _parameterName value for instance variable {@link #parameterName}
     */
    public void setParamName(final String _parameterName)
    {
        this.paramName = _parameterName;
    }

    /**
     * Getter method for the instance variable {@link #extraParameters}.
     *
     * @return value of instance variable {@link #extraParameters}
     */
    public Set<String> getExtraParameters()
    {
        return this.extraParameters;
    }

    /**
     * Getter method for the instance variable {@link #fieldName}.
     *
     * @return value of instance variable {@link #fieldName}
     * @throws EFapsException on error
     */
    public String getFieldName()
    {
        return getFieldConfiguration().getName();
    }

    /**
     * Getter method for the instance variable {@link #maxChoiceLength}.
     *
     * @return value of instance variable {@link #maxChoiceLength}
     */
    public int getMaxChoiceLength()
    {
        return this.maxChoiceLength;
    }

    /**
     * Setter method for instance variable {@link #maxChoiceLength}.
     *
     * @param _maxChoiceLength value for instance variable {@link #maxChoiceLength}
     */
    public void setMaxChoiceLength(final int _maxChoiceLength)
    {
        this.maxChoiceLength = _maxChoiceLength;
    }

    /**
     * Getter method for the instance variable {@link #maxValueLength}.
     *
     * @return value of instance variable {@link #maxValueLength}
     */
    public int getMaxValueLength()
    {
        return this.maxValueLength;
    }

    /**
     * Setter method for instance variable {@link #maxValueLength}.
     *
     * @param _maxValueLength value for instance variable {@link #maxValueLength}
     */
    public void setMaxValueLength(final int _maxValueLength)
    {
        this.maxValueLength = _maxValueLength;
    }

    /**
     * Getter method for the instance variable {@link #value4Edit}.
     *
     * @return value of instance variable {@link #value4Edit}
     */
    public EditValue getValue4Edit()
    {
        return this.value4Edit;
    }

    /**
     * Setter method for instance variable {@link #value4Edit}.
     *
     * @param _value4Edit value for instance variable {@link #value4Edit}
     */
    public void setValue4Edit(final EditValue _value4Edit)
    {
        this.value4Edit = _value4Edit;
    }

    /**
     * Getter method for the instance variable {@link #maxResult}.
     *
     * @return value of instance variable {@link #maxResult}
     */
    public int getMaxResult()
    {
        return this.maxResult;
    }

    /**
     * Setter method for instance variable {@link #maxResult}.
     *
     * @param _maxResult value for instance variable {@link #maxResult}
     */
    public void setMaxResult(final int _maxResult)
    {
        this.maxResult = _maxResult;
    }

    /**
     * Getter method for the instance variable {@link #required}.
     *
     * @return value of instance variable {@link #required}
     */
    public boolean isRequired()
    {
        return this.required;
    }

    /**
     * Setter method for instance variable {@link #required}.
     *
     * @param _required value for instance variable {@link #required}
     */
    public void setRequired(final boolean _required)
    {
        this.required = _required;
    }

    /**
     * Getter method for the instance variable {@link #autoType}.
     *
     * @return value of instance variable {@link #autoType}
     */
    public AutoCompleteBehavior.Type getAutoType()
    {
        if (this.autoType == null) {
            if (isRequired()) {
                setAutoType(Type.COMPLETE);
            } else {
                setAutoType(Type.SUGGESTION);
            }
        }
        return this.autoType;
    }

    /**
     * Setter method for instance variable {@link #autoType}.
     *
     * @param _autoType value for instance variable {@link #autoType}
     */
    public void setAutoType(final AutoCompleteBehavior.Type _autoType)
    {
        this.autoType = _autoType;
    }

    /**
     * Gets the field configuration.
     *
     * @return the field configuration
     */
    public FieldConfiguration getFieldConfiguration()
    {
        return this.fieldConfiguration;
    }

    /**
     * Sets the field configuration.
     *
     * @param _fieldConfiguration the new field configuration
     */
    public void setFieldConfiguration(final FieldConfiguration _fieldConfiguration)
    {
        this.fieldConfiguration = _fieldConfiguration;
    }
}
