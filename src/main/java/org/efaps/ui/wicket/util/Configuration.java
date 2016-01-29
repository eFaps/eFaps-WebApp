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

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.db.Context;
import org.efaps.ui.wicket.EFapsApplication;
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
     * Basic key.
     */
    private static String BASEKEY = "org.efaps.webapp.";

    /**
     * Attribute enum.
     */
    public enum ConfigAttribute
    {
        /**  */
        RECENTCACHESIZE(false, true, WebAppSettings.RECENT_CACHE_SIZE, "5", Integer.class),
        /**  */
        RECENT_LINKMAX(false, true, WebAppSettings.RECENT_LINKMAX, "25", Integer.class),
        /**  */
        CACHE_DURATION(false, true, WebAppSettings.CACHE_DURATION, "3600", Integer.class),
        /** StyelSheet for the Classification Tree. (human, windows) */
        CLASSTREE_CLASS(true, true, WebAppSettings.CLASSTREE_CLASS, "human", String.class),
        /** Expand state for the Tree. */
        CLASSTREE_EXPAND(false, true, WebAppSettings.CLASSTREE_EXPAND, "", Properties.class),
        /** Name of the main stylesheet for dojo. (tundra,claro,nihilo,soria) */
        DOJO_CLASS(true, true, WebAppSettings.DOJO_CLASS, "tundra", String.class),
        /** Name of the main stylesheet for dojo modal window. (w_blue,w_silver) */
        DOJO_MODALCLASS(true, true, WebAppSettings.DOJO_MODALCLASS, "w_silver", String.class),
        /** position of the horizontal splitter. */
        SPLITTERPOSHORIZONTAL(true, true, WebAppSettings.SPLITTERPOSHORIZONTAL, "200", String.class),
        /** position of the vertical splitter. */
        SPLITTERPOSVERTICAL(true, true, WebAppSettings.SPLITTERPOSVERTICAL, "50%", String.class),
        /** StyelSheet for the Structur Browser Tree. (human, windows) */
        STRUCTREE_CLASS(true, true, WebAppSettings.STRUCTREE_CLASS, "windows", String.class),
        /** StyelSheet for the Structur Browser Tree. (human, windows) */
        STRUCBRWSRTREE_CLASS(true, true, WebAppSettings.STRUCBRWSRTREE_CLASS, "human", String.class),
        /** */
        SHOW_OID(false, true, WebAppSettings.SHOW_OID, "false", Boolean.class),
        /** */
        BOARD_ASSIGNEDTASK_MAX(false, true, WebAppSettings.DASHBOARD_ASSIGNEDTASK_MAX, "8", Integer.class),
        /** */
        BOARD_OWNEDTASK_MAX(false, true, WebAppSettings.DASHBOARD_OWNEDTASK_MAX, "8", Integer.class),
        /** */
        BOARD_ASSIGNEDTASK_AU(false, true, WebAppSettings.DASHBOARD_ASSIGNEDTASK_AU, "true", Boolean.class),
        /** */
        BOARD_OWNEDTASK_AU(false, true, WebAppSettings.DASHBOARD_OWNEDTASK_AU, "true", Boolean.class),
        /** */
        BOARD_ASSIGNED_AUTIME(false, true, WebAppSettings.DASHBOARD_ASSIGNED_AUTIME, "30", Integer.class),
        /** */
        BOARD_OWNEDTASK_AUTIME(false, true, WebAppSettings.DASHBOARD_OWNEDTASK_AUTIME, "30", Integer.class),
        /** */
        BOARD_PROVIDER(false, true, WebAppSettings.DASHBOARD_PROVIDER,
                        "org.efaps.esjp.common.dashboard.DashboardProvider", String.class),
        /** MainToolBar. */
        TOOLBAR(false, true, WebAppSettings.TOOLBAR, "87001cc3-c45c-44de-b8f1-776df507f268", String.class),
        /** Default form for Task to prevent errors. */
        BPM_DEFAULTTASKFROM(false, true, WebAppSettings.BPM_DEFAULTTASKFROM, "c34e35ad-93ed-4190-887d-5be9a2302edf",
                        String.class),
         /** Websocket activated. */
        WEBSOCKET_ACTVATE(false, true, WebAppSettings.WEBSOCKET_ACTIVATE, "true", Boolean.class),
        /** Websocket KeepAlive period. */
        WEBSOCKET_KASP(false, true, WebAppSettings.WEBSOCKET_KASP, "180", Integer.class),
        /** Websocket KeepAlive Threshold Criteria. */
        WEBSOCKET_KATH(false, true, WebAppSettings.WEBSOCKET_KATH, "300", Integer.class),
        /** */
        WEBSOCKET_MESSAGETABLE_MAX(false, true, WebAppSettings.WEBSOCKET_MESSAGETABLE_MAX, "20", Integer.class),
        /** */
        WEBSOCKET_SESSIONTABLE_MAX(false, true, WebAppSettings.WEBSOCKET_SESSIONTABLE_MAX, "20", Integer.class),
        /** Websocket activated. */
        CONMAN_ACTVATE(false, true, WebAppSettings.CONMAN_ACTIVATE, "true", Boolean.class),
        /** AutoComplete maximum result. */
        AUTOC_MAXRESULT(false, true, WebAppSettings.AUTOC_MAXRESULT, "500", Integer.class),
        /** AutoComplete maximum choice length. */
        AUTOC_MAXCHOICE(false, true, WebAppSettings.AUTOC_MAXCHOICE, "-1", Integer.class),
        /** AutoComplete maximum value length. */
        AUTOC_MAXVALUE(false, true, WebAppSettings.AUTOC_MAXVALUE, "-1", Integer.class),
        /** AutoComplete minimum input length. */
        AUTOC_MININPUT(false, true, WebAppSettings.AUTOC_MININPUT, "1", Integer.class),
        /** AutoComplete search delay. */
        AUTOC_SEARCHDELAY(false, true, WebAppSettings.AUTOC_SEARCHDELAY, "500", Integer.class),
        /** AutoComplete search delay. */
        AUTOC_PARAMNAME(false, true, WebAppSettings.AUTOC_PARAMNAME, "p", String.class),
        /** Menus for the Users. */
        USER_MENU(true, false, WebAppSettings.USER_MENU, "", String.class),
        /** UUID of the Menu/Command the UserMenu will be connected to. */
        USER_MENUMENU(false, true, WebAppSettings.USER_MENUMENU, "f84814f4-1bc5-481e-b37c-6ae782b25a00", String.class),
        /** Activate the UserMenu Mechanism. */
        USER_MENUACT(false, true, WebAppSettings.USER_MENUACT, "true", Boolean.class),
        /** The store inmemorycache. Servlet only!!*/
        STORE_INMEMORYCACHE(false, false, BASEKEY + "store.InMemoryCacheSize", "10", Integer.class);

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

        /** The type class. */
        private final Class<?> attrClass;

        /**
         * Instantiates a new config attribute.
         *
         * @param _user Can be read from UserAttributes
         * @param _system Can be read form the SystemConfiguration
         * @param _key the key for this Attribute
         * @param _devaultValue The default Value
         * @param _class the class
         */
        ConfigAttribute(final boolean _user,
                        final boolean _system,
                        final String _key,
                        final String _devaultValue,
                        final Class<?> _class)
        {
            this.system = _system;
            this.user = _user;
            this.key = _key;
            this.defaultvalue = _devaultValue;
            this.attrClass = _class;
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

        /**
         * Gets the default Value.
         *
         * @return the default Value
         */
        public String getDefaultvalue()
        {
            return this.defaultvalue;
        }

        /**
         * Checks if is can be read form the SystemConfiguration.
         *
         * @return the can be read form the SystemConfiguration
         */
        public boolean isSystem()
        {
            return this.system;
        }

        /**
         * Checks if is can be read from UserAttributes.
         *
         * @return the can be read from UserAttributes
         */
        public boolean isUser()
        {
            return this.user;
        }

        /**
         * Gets the type class.
         *
         * @return the type class
         */
        public Class<?> getAttrClass()
        {
            return this.attrClass;
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
    public static SystemConfiguration getSysConfig()
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
            // if still null check for
            if (ret == null) {
                ret = EFapsApplication.get().getInitParameter(_attribute.getKey());
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
