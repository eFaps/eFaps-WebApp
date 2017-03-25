/*
 * Copyright 2003 - 2016 The eFaps Team
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

        /** */
        LOGINALERT_PROVIDER(false, true, "LoginAlert.Provider", "org.efaps.esjp.common.loginalert.AlertProvider",
                        String.class, "Class name of the AlertProvider class"),
        /** */
        LOGOUT_URL(false, true, "LogoutURL", "", String.class, "URL to redirect to on logout."),

        /**  */
        RECENTCACHESIZE(false, true, "RecentCacheSize", "5", Integer.class,
                        "Number of items in the Recent Links menu. Default Value is 5."),
        /**  */
        RECENT_LINKMAX(false, true, "RecentLinkMaxLength", "25", Integer.class,
                        "Max Length of the Label inside the Recent Link. Default Value is 25."),
        /**  */
        CACHE_DURATION(false, true, "StaticContentCacheDuration", "3600", Integer.class,
                        "Duration (in seconds) of a eFapsStaticContent\n"
                        + "* in the Cache of the Browser, before it will be retrieved again\n"
                        + "* from the eFaps-DataBase. Default Value is 3600."),
        /** */
        CLASSTREE_CLASS(true, true, "ClassificationTreeStyleSheet", "human", String.class,
                        "(human, windows). StyelSheet for the Classification Tree. Default\n"
                        + "* Value \"human\""),
        /**  */
        CLASSTREE_EXPAND(false, true, "ClassificationTreeExpandState", "", Properties.class,
                        "Expand state for the Tree. Setting the default expand state\n"
                        + "* of the Tree for Classifications. e.g. Products_Class=false means that the\n"
                        + "* tree will not be expanded. DefaultValue is true."),
        /** */
        DOJO_CLASS(true, true, "DojoMainStylesheet", "tundra", String.class,
                        " Name of the main stylesheet for dojo. (tundra,claro,nihilo,soria)"),
        /**  */
        DOJO_MODALCLASS(true, true, "DojoModalStylesheet", "w_silver", String.class,
                        "Name of the main stylesheet for dojo modal window. (w_blue,w_silver)"),
        /** */
        SPLITTERPOSHORIZONTAL(true, true, "PositionOfHorizontalSplitter", "200", String.class,
                        "Position of the horizontal splitter. "),
        /** */
        SPLITTERPOSVERTICAL(true, true, "PositionOfVerticalSplitter", "50%", String.class,
                        "Position of the vertical splitter. "),
        /**  */
        STRUCTREE_CLASS(true, true, "StructurTreeStyleSheet", "windows", String.class,
                        "StyleSheet for the Structur Browser Tree. (human, windows)"),
        /** */
        STRUCBRWSRTREE_CLASS(true, true, "StructurBrowserTreeStyleSheet", "human", String.class,
                        "StyelSheet for the Structur Browser Tree. (human, windows) "),
        /** */
        SHOW_OID(false, true, "ShowOID", "false", Boolean.class,
                        "Display the oid of the current object behind the title."),
        /** */
        BOARD_ASSIGNEDTASK_MAX(false, true, "DashBoard.AssignedTaskTable.MaximumRows", "8", Integer.class,
                        "Maximum rows of task shown in the task Table per Page. Default Value is 8."),
        /** */
        BOARD_OWNEDTASK_MAX(false, true, "DashBoard.OwnedTaskTable.MaximumRows", "8", Integer.class,
                        "Maximum rows of task shown in the task Table per Page. Default Value is 8"),
        /** */
        BOARD_ASSIGNEDTASK_AU(false, true, "DashBoard.AssignedTable.UserAutoUpdate", "true", Boolean.class,
                        "Allow the User to activate deactivate the AutoUpdate. Default true."),
        /** */
        BOARD_OWNEDTASK_AU(false, true,  "DashBoard.OwnedTaskTable.UserAutoUpdate", "true", Boolean.class,
                        "Allow the User to activate deactivate the AutoUpdate. Default true."),
        /** */
        BOARD_ASSIGNED_AUTIME(false, true, "DashBoard.AssignedTaskTable.AutoUpdateDuration", "30", Integer.class,
                        "Time in seconds for an AutoUpdate. To deactivate AutoUpdate set to 0. "
                        + "Default Value 30 Seconds."),
        /** */
        BOARD_OWNEDTASK_AUTIME(false, true, "DashBoard.OwnedTaskTable.AutoUpdateDuration", "30", Integer.class,
                        "Time in seconds for an AutoUpdate. To deactivate AutoUpdate set to 0. "
                        + "Default Value 30 Seconds."),
        /** */
        BOARD_PROVIDER(false, true, "DashBoard.Provider", "org.efaps.esjp.common.dashboard.DashboardProvider",
                        String.class,
                        "Class name of the DashboardProvider class"),
        /** MainToolBar. */
        TOOLBAR(false, true,  "MainToolBar", "87001cc3-c45c-44de-b8f1-776df507f268", String.class, ""),
        /** */
        BPM_DEFAULTTASKFROM(false, true, "BPM.DefaultTaskForm", "c34e35ad-93ed-4190-887d-5be9a2302edf",
                        String.class,
                        "UUID of a form that will be used in case that there is no form found for the task page."),
         /***/
        WEBSOCKET_ACTVATE(false, true, "WebSocket.activate", "false", Boolean.class, " Websocket activated. "),
        /**  */
        WEBSOCKET_KASP(false, true, "WebSocket.KeepAliveSendPeriod", "180", Integer.class,
                        "Websocket KeepAlive period."),
        /**  */
        WEBSOCKET_KATH(false, true, "WebSocket.KeepAliveThreshold", "300", Integer.class,
                        "Websocket KeepAlive Threshold Criteria."),
        /** */
        WEBSOCKET_MESSAGETABLE_MAX(false, true, "WebSocket.MessageTable.MaximumRows", "20", Integer.class,
                        "Maximum rows of session shown in the Session Table per Page. Default Value is 20."),
        /** */
        WEBSOCKET_SESSIONTABLE_MAX(false, true, "WebSocket.SessionTable.MaximumRows", "20", Integer.class,
                        "Maximum rows of session shown in the Session Table per Page. Default Value is 20."),
        /** */
        CONMAN_ACTVATE(false, true, "ConnectionManagement.activate", "true", Boolean.class,
                        "Activate the websocket/push management. Default: true"),
        /**  */
        AUTOC_MAXRESULT(false, true, "AutoComplete.MaxResult", "500", Integer.class,
                        "AutoComplete maximum result."),
        /** */
        AUTOC_MAXCHOICE(false, true, "AutoComplete.MaxChoiceLength", "-1", Integer.class,
                        "Maximum number of letters shown in the DropDown of the AutoComlete. Default Value is -1 "
                        + "(Deactivated)"),
        /** */
        AUTOC_MAXVALUE(false, true, "AutoComplete.MaxValueLength", "-1", Integer.class,
                        "Maximum number of letters shown in the input. Default Value is -1 (Deactivated)"),
        /** */
        AUTOC_MININPUT(false, true,  "AutoComplete.MinInputLength", "1", Integer.class,
                        "Maximum number of result shown in the DropDown of the AutoComlete. Default Value is 500."),
        /** */
        AUTOC_SEARCHDELAY(false, true, "AutoComplete.SearchDelay", "500", Integer.class,
                        "Time in ms before the AutomComplete fires the Request. Default Value is 500."),
        /** */
        AUTOC_PARAMNAME(false, true, "AutoComplete.ParameterName", "p", String.class,
                        "Name of the parameter. Defaults to \"p\"."),
        /** */
        USER_MENU(true, false, "MostUsedMenus4User", "", String.class, "Menus for the Users. "),
        /**  */
        USER_MENUMENU(false, true, "MenuCmd4UserMenu", "f84814f4-1bc5-481e-b37c-6ae782b25a00", String.class,
                        "UUID of the Menu/Command the UserMenu will be connected to."),
        /***/
        USER_MENUACT(false, true, "ActivateUserMenu", "true", Boolean.class,
                        " Activate the UserMenu Mechanism. "),
        /***/
        STORE_INMEMORYCACHE(false, false, "store.InMemoryCacheSize", "10", Integer.class,
                        " The store inmemorycache. Servlet only!!"),
        /***/
        STORE_MAXSIZEPERSESSION(false, false, "store.MaxSizePerSession(", "50", Integer.class,
                        " The store MaxSizePerSession in MB. Servlet only!!"),

        /** */
        FORMAT_DATETIME(false, true, "Format4DateTime", "MM", String.class,
                        "The format for datetime. Can be style or pattern. "),
        /** */
        FORMAT_DATE(false, true, "Format4Date", "M-", String.class,
                        "The format for date. Can be style or pattern."),
        /** */
        INDEXACCESSCMD(false, true, "IndexAccessCmd", "88c9ce19-d759-443e-b791-0e725fe58f52", String.class,
                        "UUID of the command that is used to define the access to the index search."),
        /** */
        TABLEDEFAULTTYPECONTENT(false, true, "TableDefaultType4Content", "Table", String.class,
                        "Type of table used as default content  page table"),

        /** */
        TABLEDEFAULTTYPETREE(false, true, "TableDefaultType4Tree", "Table", String.class,
                        "Type of table used as tree page table"),

        /** */
        TABLEDEFAULTTYPESEARCH(false, true, "TableDefaultType4Search", "Table", String.class,
                        "Type of table used as tree page table"),

        /** */
        TABLEDEFAULTTYPEFORM(false, true, "TableDefaultType4Form", "Table", String.class,
                        "Type of table used as table inside a form"),

        /** */
        GRIDPRINTESJP(false, true, "GridXPrinter", "org.efaps.esjp.ui.print.GridX", String.class,
                        "Class name of the esjp to be invoked for printing GridX."),
        /** */
        GRIDCHECKOUTESJP(false, true, "GridXCheckout", "org.efaps.esjp.common.file.FileCheckout", String.class,
                        "Class name of the esjp to be invoked for checkout in GridX."),

        /** */
        HELPSNIPPROV(false, true, "HelpSnipplet", "org.efaps.esjp.common.help.HelpProvider", String.class,
                        "Class name of the esjp to be invoked to create the html snipplet for Help."),

        /** Admin_Program_MarkdownTree_Menu_Action_EditMarkdown. */
        HELPEDITCMD(false, true, "HelpEditCommand", "a42e1651-a42e-42f2-9b7b-7f47599a13fd", String.class,
                        "UUID of the Command that defines the form to be opened for edit help.");

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

        /** The description. */
        private final String description;

        /**
         * Instantiates a new config attribute.
         *
         * @param _user Can be read from UserAttributes
         * @param _system Can be read form the SystemConfiguration
         * @param _key the key for this Attribute
         * @param _defaultValue The default Value
         * @param _class the class
         * @param _description the description
         */
        ConfigAttribute(final boolean _user,
                        final boolean _system,
                        final String _key,
                        final String _defaultValue,
                        final Class<?> _class,
                        final String _description)
        {
            this.system = _system;
            this.user = _user;
            this.key = Configuration.BASEKEY + _key;
            this.defaultvalue = _defaultValue;
            this.attrClass = _class;
            this.description = _description == null ? "" : _description;
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

        /**
         * Gets the description.
         *
         * @return the description
         */
        public String getDescription()
        {
            return this.description;
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
