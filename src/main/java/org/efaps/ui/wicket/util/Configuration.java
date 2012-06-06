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


package org.efaps.ui.wicket.util;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Configuration
{
    public enum UserAttribute
    {
        /** position of the horizontal splitter. */
        SPLITTERPOSHORIZONTAL("positionOfHorizontalSplitter"),
        /** position of the vertical splitter. */
        SPLITTERPOSVERTICAL("positionOfVerticalSplitter");

        /**
         * Stores the key of the Region.
         */
        private final String key;

        /**
         * Private Constructor.
         *
         * @param _key Key
         */
        private UserAttribute(final String _key)
        {
            this.key = _key;
        }

        /**
         * Getter method for instance variable {@link #key}.
         *
         * @return value of instance variable {@link #key}
         */
        public String getKey()
        {
            return this.key;
        }
    }

    public static void setUserAttribute(final UserAttribute _userAttribute,
                                        final String _value)
    {
        try {
            Context.getThreadContext().setUserAttribute(_userAttribute.getKey(), _value);
        } catch (final EFapsException e) {
            e.printStackTrace();
        }
    }

    public static String getUserAttribute(final UserAttribute _userAttribute)
    {
        String ret = null;
        try {
            ret = Context.getThreadContext().getUserAttribute(_userAttribute.getKey());
        } catch (final EFapsException e) {
            e.printStackTrace();
        }
        return ret;
    }

}
