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


package org.efaps.ui.wicket.util;

import org.efaps.admin.datamodel.IEnum;
import org.efaps.admin.dbproperty.DBProperties;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class EnumUtil
{

    /**
     * Private Constructor.
     */
    private EnumUtil()
    {
    }

    /**
     * @param _enum enum the label is wanted for
     * @return label
     */
    public static String getUILabel(final IEnum _enum)
    {
        String ret = null;
        if (_enum != null) {
            String key;
            if (_enum.getClass().isEnum()) {
                key = _enum.getClass().getName() + "." + _enum.toString();
            } else {
                key = _enum.getClass().getName();
            }
            ret = DBProperties.getProperty(key, false);
            if (ret == null) {
                ret = _enum.toString();
            }
        }
        return ret;
    }
}
