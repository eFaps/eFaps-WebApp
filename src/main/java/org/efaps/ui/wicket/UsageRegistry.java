/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.wicket.MetaDataKey;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UsageRegistry
{

    public static final String SEP4UUID = ":";

    public static final String SEP4VAL = ";";

    public static MetaDataKey<HashMap<String, Integer>> USAGEMAP = new MetaDataKey<HashMap<String, Integer>>()
    {

        private static final long serialVersionUID = 1L;
    };

    /**
     * @param _uuid
     */
    public static void register(final String _key)
    {
        if (Configuration.getAttributeAsBoolean(ConfigAttribute.USER_MENUACT)) {
            final Map<String, Integer> data = getRegister();
            if (data.containsKey(_key)) {
                data.put(_key, data.get(_key) + 1);
            } else {
                data.put(_key, 1);
            }
        }
    }

    private static Map<String, Integer> getRegister()
    {
        HashMap<String, Integer> ret = EFapsSession.get().getMetaData(USAGEMAP);
        if (ret == null) {
            init();
            ret = EFapsSession.get().getMetaData(USAGEMAP);
        }
        return ret;
    }

    private static void init()
    {
        final HashMap<String, Integer> map = new HashMap<String, Integer>();
        final String valuestr = Configuration.getAttribute(ConfigAttribute.USER_MENU);
        if (valuestr != null && !valuestr.isEmpty()) {
            final String[] valueArr = valuestr.split(SEP4VAL);
            for (int i = 0; i < valueArr.length; i = i + 2) {
                if (i + 1 < valueArr.length) {
                    map.put(valueArr[i], Integer.valueOf(valueArr[i + 1]));
                }
            }
        }
        EFapsSession.get().setMetaData(USAGEMAP, map);
    }

    /**
     *
     */
    public static void store()
    {
        if (Configuration.getAttributeAsBoolean(ConfigAttribute.USER_MENUACT)) {
            final Map<String, Integer> map = getRegister();
            if (!map.isEmpty()) {
                final Map<Integer, Set<String>> sortedMap = new TreeMap<>(Collections.reverseOrder());
                for (final Entry<String, Integer> entry : map.entrySet()) {
                    Set<String> set;
                    if (sortedMap.containsKey(entry.getValue())) {
                        set = sortedMap.get(entry.getValue());
                    } else {
                        set = new HashSet<String>();
                    }
                    set.add(entry.getKey());
                    sortedMap.put(entry.getValue(), set);
                }
                final StringBuilder val = new StringBuilder();
                int i = 0;
                for (final Entry<Integer, Set<String>> entry : sortedMap.entrySet()) {
                    for (final String key : entry.getValue()) {
                        if (val.length() > 0) {
                            val.append(SEP4VAL);
                        }
                        val.append(key).append(SEP4VAL).append(entry.getKey());
                        i++;
                        if (i > 10) {
                            break;
                        }
                    }
                    if (i > 10) {
                        break;
                    }
                }
                Configuration.setAttribute(ConfigAttribute.USER_MENU, val.toString());
            }
        }
    }

    public static List<String> getKeyList()
    {
        final List<String> ret = new ArrayList<>();
        final Map<String, Integer> reg = getRegister();
        final Map<Integer, Set<String>> sortedMap = new TreeMap<>(Collections.reverseOrder());
        for (final Entry<String, Integer> entry : reg.entrySet()) {
            Set<String> set;
            if (sortedMap.containsKey(entry.getValue())) {
                set = sortedMap.get(entry.getValue());
            } else {
                set = new HashSet<String>();
            }
            set.add(entry.getKey());
            sortedMap.put(entry.getValue(), set);
        }
        for (final Entry<Integer, Set<String>> entry : sortedMap.entrySet()) {
            for (final String key : entry.getValue()) {
                ret.add(key);
            }
        }
        return ret;
    }
}
