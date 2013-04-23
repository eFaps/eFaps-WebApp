/*
 * Copyright 2003 - 2013 The eFaps Team
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
     * Integer: Maximum rows of task shown in the task Table per Page. Default Value is 8.
     */
    String TASKTABLE_MAX = "org.efaps.webapp.TaskTable";

}
