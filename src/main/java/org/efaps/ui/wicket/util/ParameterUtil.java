/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.string.StringValue;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class ParameterUtil
{
    /**
     * Instantiates a new parameter util.
     */
    private ParameterUtil()
    {
    }

    /**
     * Get an array for the StringValues.
     *
     * @param _parameters IRequestParameters
     * @return always StringArray, if parameter does not exist an empty
     *         StringArray
     */
    public static Map<String, String[]> parameter2Map(final IRequestParameters _parameters)
    {
        final Map<String, String[]> ret = new HashMap<>();
        for (final String name : _parameters.getParameterNames()) {
            ret.put(name, ParameterUtil.parameter2Array(_parameters, name));
        }
        return ret;
    }

    /**
     * Get an array for the StringValues.
     *
     * @param _parameters IRequestParameters
     * @param _parameterName name of the Paramaters
     * @return always StringArray, if parameter does not exist an empty
     *         StringArray
     */
    public static String[] parameter2Array(final IRequestParameters _parameters,
                                           final String _parameterName)
    {
        final List<StringValue> values = _parameters.getParameterValues(_parameterName);
        return ParameterUtil.stringValues2Array(values);
    }

    /**
     * Get an array for the StringValues.
     *
     * @param _values the values
     * @return always StringArray, if parameter does not exist an empty
     *         StringArray
     */
    public static String[] stringValues2Array(final List<StringValue> _values)
    {
        final String[] ret;
        if (_values != null) {
            ret = new String[_values.size()];
            int i = 0;
            for (final StringValue value : _values) {
                ret[i] = value.toString();
                i++;
            }
        } else {
            ret = new String[0];
        }
        return ret;
    }
}
