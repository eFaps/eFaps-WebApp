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

import java.util.UUID;

import org.efaps.admin.common.SystemConfiguration;
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

    public enum ConfigAttribute
    {
        /**  */
        RECENTCACHESIZE(false, true, "RecentCacheSize", "5"),

        /** Name of the main stylesheet for dojo. */
        DOJO_CLASS(true, true, "DojoMainStylesheet", "tundra"),
        /** position of the horizontal splitter. */
        SPLITTERPOSHORIZONTAL(true, true, "PositionOfHorizontalSplitter", "200"),
        /** position of the vertical splitter. */
        SPLITTERPOSVERTICAL(true, true, "PositionOfVerticalSplitter", "50%");

        /**
         * Stores the key for this Attribute..
         */
        private final String key;

        /**
         * The default Value.
         */
        private final String defaultvalue;

        /**
         * Can be read form the SystemConfiguration.
         */
        private final boolean system;

        /**
         * Can be read from UserAttributes.
         */
        private final boolean user;

        /**
         * @param _user             Can be read from UserAttributes
         * @param _system           Can be read form the SystemConfiguration
         * @param _key              the key for this Attribute
         * @param _devaultValue     The default Value
         */
        private ConfigAttribute(final boolean _user,
                                final boolean _system,
                                final String _key,
                                final String _devaultValue)
        {
            this.system = _system;
            this.user = _user;
            this.key = _key;
            this.defaultvalue = _devaultValue;
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

    /**
     * @return the WebApp Sytemconfiguration.
     */
    protected static SystemConfiguration getSysConfig()
    {
        // WebApp-Configuration
        return SystemConfiguration.get(UUID.fromString("50a65460-2d08-4ea8-b801-37594e93dad5"));
    }

    /**
     * @param _attribute the attribute the value is search for
     * @return the value for the configuraion
     */
    protected static String getFromConfig(final ConfigAttribute _attribute)
    {
        String ret = null;
        try {
            ret = Configuration.getSysConfig().getAttributeValue(_attribute.getKey());
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * @param _attribute attribute the value must be set
     * @param _value     value to set
     */
    public static void setAttribute(final ConfigAttribute _attribute,
                                    final String _value)
    {
        try {
            Context.getThreadContext().setUserAttribute(_attribute.getKey(), _value);
        } catch (final EFapsException e) {
            e.printStackTrace();
        }
    }
    /**
     * @param _attribute the attribute the value is search for
     * @return the value for the configuraion
     */
    public static String getAttribute(final ConfigAttribute _attribute)
    {
        String ret = null;
        try {
            // if allowed search in UserAttributes
            if (_attribute.user) {
                ret = Context.getThreadContext().getUserAttribute(_attribute.getKey());
            }
            if (ret == null && _attribute.system) {
                ret = Configuration.getFromConfig(_attribute);
            }
            if (ret == null) {
                ret = _attribute.defaultvalue;
            }

        } catch (final EFapsException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * @param _attribute the attribute the value is search for
     * @return the value for the configuraion
     */
    public static int getAttributeAsInteger(final ConfigAttribute _attribute)
    {
        return Integer.valueOf(Configuration.getAttribute(_attribute));
    }

}
