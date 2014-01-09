/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.ui.wicket.util;

import java.util.List;

import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.string.StringValue;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class ParameterUtil
{

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
     * @param _parameters IRequestParameters
     * @param _parameterName name of the Paramaters
     * @return always StringArray, if parameter does not exist an empty
     *         StringArray
     */
    public static String[] stringValues2Array(final List<StringValue> values)
    {
        final String[] ret;
        if (values != null) {
            ret = new String[values.size()];
            int i = 0;
            for (final StringValue value : values) {
                ret[i] = value.toString();
                i++;
            }
        } else {
            ret = new String[0];
        }
        return ret;
    }
}
