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

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface WebAppSettings
{
    /**
     * Basic key.
     */
    String BASEKEY = "org.efaps.webapp.";

    /**
     * Most used Menu for the User.
     */
    String USER_MENU = WebAppSettings.BASEKEY + "MostUsedMenus4User";

    /**
     * Most used Menu for the User.
     */
    String USER_MENUMENU = WebAppSettings.BASEKEY + "MenuCmd4UserMenu";

    /**
     * Most used Menu for the User.
     */
    String USER_MENUACT = WebAppSettings.BASEKEY + "ActivateUserMenu";

    /**
     * Integer. Duration (in seconds) of a eFapsStaticContent
     * in the Cache of the Browser, before it will be retrieved again
     * from the eFaps-DataBase. Default Value is 3600.
     */
    String CACHE_DURATION = WebAppSettings.BASEKEY + "StaticContentCacheDuration";

    /**
     * String. UUID of a form that will be used in case that there is no form found for the task page.
     */
    String BPM_DEFAULTTASKFROM = WebAppSettings.BASEKEY + "BPM.DefaultTaskForm";

    /**
     * Integer: Number of items in the Recent Links menu. Default Value is 5.
     */
    String RECENT_CACHE_SIZE = WebAppSettings.BASEKEY + "RecentCacheSize";

    /**
     * Integer: Max Length of the Label inside the Recent Link. Default Value is 25.
     */
    String RECENT_LINKMAX = WebAppSettings.BASEKEY + "RecentLinkMaxLenght";

    /**
     * String (human, windows). StyelSheet for the Classification Tree. Default
     * Value "human".
     */
    String CLASSTREE_CLASS = WebAppSettings.BASEKEY + "ClassificationTreeStyleSheet";

    /**
     * Properties. Expand state for the Tree. Setting the default expand state
     * of the Tree for Classifications. e.g. Products_Class=false means that the
     * tree will not be expanded. DefaultValue is true.
     */
    String CLASSTREE_EXPAND = WebAppSettings.BASEKEY + "ClassificationTreeExpandState";

    /**
     * String (tundra,claro,nihilo,soria). Name of the main stylesheet for dojo.
     * Default Value "tundra".
     */
    String DOJO_CLASS = WebAppSettings.BASEKEY + "DojoMainStylesheet";

    /**
     * String (w_blue,w_silver). Name of the main stylesheet for dojo modal
     * window. Default Value "w_silver".
     */
    String DOJO_MODALCLASS = WebAppSettings.BASEKEY + "DojoModalStylesheet";

    /**
     * String. Position of the horizontal splitter. Default value: 200.
     */
    String SPLITTERPOSHORIZONTAL = WebAppSettings.BASEKEY + "PositionOfHorizontalSplitter";

    /**
     * String. Position of the vertical splitter. Default value: 50%.
     */
    String SPLITTERPOSVERTICAL = WebAppSettings.BASEKEY + "PositionOfVerticalSplitter";

    /**
     * String (human, windows). StyleSheet for the Structur Browser Tree.
     * Default value: windows
     */
    String STRUCTREE_CLASS = WebAppSettings.BASEKEY + "StructurTreeStyleSheet";

    /**
     * String (human, windows). StyleSheet for the Structur Browser Tree.
     * Default value: human
     */
    String STRUCBRWSRTREE_CLASS = WebAppSettings.BASEKEY + "StructurBrowserTreeStyleSheet";

    /**
     * Boolean (true/false). Display the oid of the current object behind the title.
     */
    String SHOW_OID = WebAppSettings.BASEKEY + "ShowOID";

    /**
     * String: UUID of the MainToolBar used.
     */
    String TOOLBAR = WebAppSettings.BASEKEY + "MainToolBar";

    /**
     * Integer: Maximum rows of task shown in the task Table per Page. Default Value is 8.
     */
    String DASHBOARD_ASSIGNEDTASK_MAX = WebAppSettings.BASEKEY + "DashBoard.AssignedTaskTable.MaximumRows";

    /**
     * Boolean: Allow the User to activate deactivate the AutoUpdate. Default true.
     */
    String DASHBOARD_ASSIGNEDTASK_AU = WebAppSettings.BASEKEY + "DashBoard.AssignedTable.UserAutoUpdate";

    /**
     * Integer: Time in seconds for an AutoUpdate. To deactivate AutoUpdate set to 0. Default Value 30 Seconds.
     */
    String DASHBOARD_ASSIGNED_AUTIME = WebAppSettings.BASEKEY + "DashBoard.AssignedTaskTable.AutoUpdateDuration";

    /**
     * Integer: Maximum rows of task shown in the task Table per Page. Default Value is 8.
     */
    String DASHBOARD_OWNEDTASK_MAX = WebAppSettings.BASEKEY + "DashBoard.OwnedTaskTable.MaximumRows";

    /**
     * Boolean: Allow the User to activate deactivate the AutoUpdate. Default true.
     */
    String DASHBOARD_OWNEDTASK_AU = WebAppSettings.BASEKEY + "DashBoard.OwnedTaskTable.UserAutoUpdate";

    /**
     * Integer: Time in seconds for an AutoUpdate. To deactivate AutoUpdate set to 0. Default Value 30 Seconds.
     */
    String DASHBOARD_OWNEDTASK_AUTIME = WebAppSettings.BASEKEY + "DashBoard.OwnedTaskTable.AutoUpdateDuration";

    /**
     * DBProperties: Esjp to be used as PanelXY. panel11, panel12, panel21, panel22, panel31, panel32
     * In case that BPM is activated the BMP allways will use panel11
     *
     */
    String DASHBOARD_PANELS = WebAppSettings.BASEKEY + "DashBoard.Panels";

    /**
     * Boolean (true/false): Activate the websocket/push management. Default: true
     */
    String CONMAN_ACTIVATE = WebAppSettings.BASEKEY + "ConnectionManagement.activate";

    /**
     * Boolean (true/false): Activate the websocket/push management. Default: true
     */
    String WEBSOCKET_ACTIVATE = WebAppSettings.BASEKEY + "WebSocket.activate";

    /**
     * Integer: Maximum rows of session shown in the Session Table per Page. Default Value is 20.
     */
    String WEBSOCKET_SESSIONTABLE_MAX = WebAppSettings.BASEKEY + "WebSocket.SessionTable.MaximumRows";

    /**
     * Integer: Maximum rows of session shown in the Session Table per Page. Default Value is 20.
     */
    String WEBSOCKET_MESSAGETABLE_MAX = WebAppSettings.BASEKEY + "WebSocket.MessageTable.MaximumRows";

    /**
     * Integer: Activate the websocket/push management. Default: true
     */
    String WEBSOCKET_KASP = WebAppSettings.BASEKEY + "WebSocket.KeepAliveSendPeriod";

    /**
     * Integer: Activate the websocket/push management. Default: true
     */
    String WEBSOCKET_KATH = WebAppSettings.BASEKEY + "WebSocket.KeepAliveThreshold";

    /**
     * Integer: Maximum number of result shown in the DropDown of the AutoComlete. Default Value is 500.
     */
    String AUTOC_MAXRESULT = WebAppSettings.BASEKEY + "AutoComplete.MaxResult";

    /**
     * Integer: Time in ms before the AutomComplete fires the Request. Default Value is 500.
     */
    String AUTOC_SEARCHDELAY = WebAppSettings.BASEKEY + "AutoComplete.SearchDelay";

    /**
     * Integer: Maximum number of letters shown in the DropDown of the AutoComlete. Default Value is -1 (Deactivated).
     */
    String AUTOC_MAXCHOICE = WebAppSettings.BASEKEY + "AutoComplete.MaxChoiceLength";

    /**
     * Integer: Maximum number of letters shown in the input. Default Value is -1 (Deactivated).
     */
    String AUTOC_MAXVALUE = WebAppSettings.BASEKEY + "AutoComplete.XaxValueLength";

    /**
     * Integer: Maximum number of result shown in the DropDown of the AutoComlete. Default Value is 500.
     */
    String AUTOC_MININPUT = WebAppSettings.BASEKEY + "AutoComplete.MinInputLength";
    /**
     * String:Name of the parameter. Defaults to "p".
     */
    String AUTOC_PARAMNAME = WebAppSettings.BASEKEY + "AutoComplete.ParameterName";

}
