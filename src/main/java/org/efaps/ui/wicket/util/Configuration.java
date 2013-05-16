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

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Configuration
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    /**
     * Attribute enum.
     */
    public enum ConfigAttribute
    {
        /**  */
        RECENTCACHESIZE(false, true, WebAppSettings.RECENT_CACHE_SIZE, "5"),
        /**  */
        RECENT_LINKMAX(false, true, WebAppSettings.RECENT_LINKMAX, "25"),
        /**  */
        CACHE_DURATION(false, true, WebAppSettings.CACHE_DURATION, "3600"),

        /** StyelSheet for the Classification Tree. (human, windows) */
        CLASSTREE_CLASS(true, true, WebAppSettings.CLASSTREE_CLASS, "human"),
        /** Expand state for the Tree. */
        CLASSTREE_EXPAND(false, true, WebAppSettings.CLASSTREE_EXPAND, ""),
        /** Name of the main stylesheet for dojo. (tundra,claro,nihilo,soria) */
        DOJO_CLASS(true, true, WebAppSettings.DOJO_CLASS, "tundra"),
        /** Name of the main stylesheet for dojo modal window. (w_blue,w_silver) */
        DOJO_MODALCLASS(true, true, WebAppSettings.DOJO_MODALCLASS, "w_silver"),
        /** position of the horizontal splitter. */
        SPLITTERPOSHORIZONTAL(true, true, WebAppSettings.SPLITTERPOSHORIZONTAL, "200"),
        /** position of the vertical splitter. */
        SPLITTERPOSVERTICAL(true, true, WebAppSettings.SPLITTERPOSVERTICAL, "50%"),
        /** StyelSheet for the Structur Browser Tree. (human, windows) */
        STRUCTREE_CLASS(true, true, WebAppSettings.STRUCTREE_CLASS, "windows"),
        /** StyelSheet for the Structur Browser Tree. (human, windows) */
        STRUCBRWSRTREE_CLASS(true, true, WebAppSettings.STRUCBRWSRTREE_CLASS, "human"),
        /** */
        SHOW_OID(false, true, WebAppSettings.SHOW_OID, "false"),
        /** */
        TASKTABLE_MAX(false, true, WebAppSettings.TASKTABLE_MAX, "8"),
        /** MainToolBar */
        TOOLBAR(false, true, WebAppSettings.TOOLBAR, "87001cc3-c45c-44de-b8f1-776df507f268");

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
         * @param _user Can be read from UserAttributes
         * @param _system Can be read form the SystemConfiguration
         * @param _key the key for this Attribute
         * @param _devaultValue The default Value
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

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    /**
     * Private Constructor to provide Singleton.
     */
    private Configuration()
    {
    }

    /**
     * @return the WebApp Sytemconfiguration.
     * @throws CacheReloadException on error
     */
    protected static SystemConfiguration getSysConfig()
        throws CacheReloadException
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
            Configuration.LOG.warn("Catched error while reading Value from SystemConfiguration {}", _attribute, e);
        }
        return ret;
    }

    /**
     * @param _attribute attribute the value must be set
     * @param _value value to set
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


    /**
     * @param _attribute the attribute the value is search for
     * @return the value for the configuraion
     */
    public static boolean getAttributeAsBoolean(final ConfigAttribute _attribute)
    {
        return Boolean.parseBoolean(Configuration.getAttribute(_attribute));
    }

    /**
     * @param _attribute the attribute the value is search for
     * @return the value for the configuration
     * @throws EFapsException on error
     */
    public static Properties getAttributeAsProperties(final ConfigAttribute _attribute)
        throws EFapsException
    {
        final Properties ret = new Properties();
        final String value = Configuration.getAttribute(_attribute);
        if (value != null) {
            try {
                ret.load(new StringReader(value));
            } catch (final IOException e) {
                throw new EFapsException("IOException", e);
            }
        }
        return ret;
    }
}
