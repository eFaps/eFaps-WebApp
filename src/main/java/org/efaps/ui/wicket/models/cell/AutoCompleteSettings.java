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
import java.util.HashSet;
import java.util.Set;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
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
    private int minInputLength = 1;

    /**
     * Max length for the Choice value.
     */
    private int maxChoiceLength = -1;

    /**
     * Max length for the Value value.
     */
    private int maxValueLength = -1;

    /**
     * Max Result shown in the Interface. To deactivate set to -1.
     */
    private int maxResult = 500;

    /**
     * Name of the parameter.
     */
    private String paramName = "p";

    /**
     * Delay in milliseconds between when user types something
     * and we start searching based on that value.
     */
    private int searchDelay = 500;

    /**
     * Name of the field.
     */
    private String fieldName;

    /**
     * WIdth of the rendered field.
     */
    private int width = 0;

    /**
     * Edit value definition.
     */
    private EditValue value4Edit = EditValue.OID;

    /**
     * Name of the extra parameters that will be send via GET.
     */
    private final Set<String> extraParameters = new HashSet<String>();

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
     * Setter method for instance variable {@link #fieldName}.
     *
     * @param _fieldName value for instance variable {@link #fieldName}
     */
    public void setFieldName(final String _fieldName)
    {
        this.fieldName = _fieldName;
    }

    /**
     * Setter method for instance variable {@link #width}.
     *
     * @param _width value for instance variable {@link #width}
     */
    public void setWidth(final int _width)
    {
        this.width = _width;
    }

    /**
     * Getter method for the instance variable {@link #fieldName}.
     *
     * @return value of instance variable {@link #fieldName}
     */
    public String getFieldName()
    {
        return this.fieldName;
    }

    /**
     * Getter method for the instance variable {@link #width}.
     *
     * @return value of instance variable {@link #width}
     */
    public int getWidth()
    {
        return this.width;
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
}
