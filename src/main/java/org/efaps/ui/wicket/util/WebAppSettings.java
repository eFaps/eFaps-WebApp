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
     * Integer. Duration (in seconds) of a eFapsStaticContent
     * in the Cache of the Browser, before it will be retrieved again
     * from the eFaps-DataBase. Default Value is 3600.
     */
    String CACHE_DURATION = "org.efaps.webapp.StaticContentCacheDuration";

    /**
     * String. UUID of a form that will be used in case that there is no form found for the task page.
     */
    String BPM_DEFAULTTASKFROM = "org.efaps.webapp.BPM.DefaultTaskForm";

    /**
     * Integer: Number of items in the Recent Links menu. Default Value is 5.
     */
    String RECENT_CACHE_SIZE = "org.efaps.webapp.RecentCacheSize";

    /**
     * Integer: Max Length of the Label inside the Recent Link. Default Value is 25.
     */
    String RECENT_LINKMAX = "org.efaps.webapp.RecentLinkMaxLenght";

    /**
     * String (human, windows). StyelSheet for the Classification Tree. Default
     * Value "human".
     */
    String CLASSTREE_CLASS = "org.efaps.webapp.ClassificationTreeStyleSheet";

    /**
     * Properties. Expand state for the Tree. Setting the default expand state
     * of the Tree for Classifications. e.g. Products_Class=false means that the
     * tree will not be expanded. DefaultValue is true.
     */
    String CLASSTREE_EXPAND = "org.efaps.webapp.ClassificationTreeExpandState";

    /**
     * String (tundra,claro,nihilo,soria). Name of the main stylesheet for dojo.
     * Default Value "tundra".
     */
    String DOJO_CLASS = "org.efaps.webapp.DojoMainStylesheet";

    /**
     * String (w_blue,w_silver). Name of the main stylesheet for dojo modal
     * window. Default Value "w_silver".
     */
    String DOJO_MODALCLASS = "org.efaps.webapp.DojoModalStylesheet";

    /**
     * String. Position of the horizontal splitter. Default value: 200.
     */
    String SPLITTERPOSHORIZONTAL = "org.efaps.webapp.PositionOfHorizontalSplitter";

    /**
     * String. Position of the vertical splitter. Default value: 50%.
     */
    String SPLITTERPOSVERTICAL = "org.efaps.webapp.PositionOfVerticalSplitter";

    /**
     * String (human, windows). StyleSheet for the Structur Browser Tree.
     * Default value: windows
     */
    String STRUCTREE_CLASS = "org.efaps.webapp.StructurTreeStyleSheet";

    /**
     * String (human, windows). StyleSheet for the Structur Browser Tree.
     * Default value: human
     */
    String STRUCBRWSRTREE_CLASS = "org.efaps.webapp.StructurBrowserTreeStyleSheet";

    /**
     * Boolean (true/false). Display the oid of the current object behind the title.
     */
    String SHOW_OID = "org.efaps.webapp.ShowOID";

    /**
     * String: UUID of the MainToolBar used.
     */
    String TOOLBAR = "org.efaps.webapp.MainToolBar";

    /**
     * Integer: Maximum rows of task shown in the task Table per Page. Default Value is 8.
     */
    String DASHBOARD_ASSIGNEDTASK_MAX = "org.efaps.webapp.DashBoard.AssignedTaskTable.MaximumRows";

    /**
     * Boolean: Allow the User to activate deactivate the AutoUpdate. Default true.
     */
    String DASHBOARD_ASSIGNEDTASK_AU = "org.efaps.webapp.DashBoard.AssignedTable.UserAutoUpdate";

    /**
     * Integer: Time in seconds for an AutoUpdate. To deactivate AutoUpdate set to 0. Default Value 30 Seconds.
     */
    String DASHBOARD_ASSIGNED_AUTIME = "org.efaps.webapp.DashBoard.AssignedTaskTable.AutoUpdateDuration";

    /**
     * Integer: Maximum rows of task shown in the task Table per Page. Default Value is 8.
     */
    String DASHBOARD_OWNEDTASK_MAX = "org.efaps.webapp.DashBoard.OwnedTaskTable.MaximumRows";

    /**
     * Boolean: Allow the User to activate deactivate the AutoUpdate. Default true.
     */
    String DASHBOARD_OWNEDTASK_AU = "org.efaps.webapp.DashBoard.OwnedTaskTable.UserAutoUpdate";

    /**
     * Integer: Time in seconds for an AutoUpdate. To deactivate AutoUpdate set to 0. Default Value 30 Seconds.
     */
    String DASHBOARD_OWNEDTASK_AUTIME = "org.efaps.webapp.DashBoard.OwnedTaskTable.AutoUpdateDuration";

    /**
     * DBProperties: Esjp to be used as PanelXY. panel11, panel12, panel21, panel22, panel31, panel32
     * In case that BPM is activated the BMP allways will use panel11
     *
     */
    String DASHBOARD_PANELS = "org.efaps.webapp.DashBoard.Panels";

    /**
     * Boolean (true/false): Activate the websocket/push management. Default: true
     */
    String CONMAN_ACTIVATE = " org.efaps.webapp.ConnectionManagement.activate";

    /**
     * Boolean (true/false): Activate the websocket/push management. Default: true
     */
    String WEBSOCKET_ACTIVATE = "org.efaps.webapp.WebSocket.activate";

    /**
     * Integer: Maximum rows of session shown in the Session Table per Page. Default Value is 20.
     */
    String WEBSOCKET_SESSIONTABLE_MAX = "org.efaps.webapp.WebSocket.SessionTable.MaximumRows";

    /**
     * Integer: Maximum rows of session shown in the Session Table per Page. Default Value is 20.
     */
    String WEBSOCKET_MESSAGETABLE_MAX = "org.efaps.webapp.WebSocket.MessageTable.MaximumRows";
}
