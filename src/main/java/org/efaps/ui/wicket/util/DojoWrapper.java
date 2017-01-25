/*
 * Copyright 2003 - 2017 The eFaps Team
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.collections4.comparators.ComparatorChain;

/**
 * The Class DojoWrapper.
 */
public final class DojoWrapper
{

    /**
     * Instantiates a new dojo wrapper.
     */
    private DojoWrapper()
    {

    }

    /**
     * Require.
     *
     * @param _script the script
     * @param _classes the classes
     * @return the char sequence
     */
    public static CharSequence require(final CharSequence _script,
                                       final DojoClass... _classes)
    {
        final TreeMap<Double, DojoLayer> layers = new TreeMap<>();
        for (final DojoLayer layer : DojoLayer.values()) {
            long count = 0;
            for (final DojoClass dojoClass : _classes) {
                if (layer.getDojoClasses().contains(dojoClass)) {
                    count++;
                }
            }
            if (count > 0) {
                final double weight = (double) count / (double) layer.getDojoClasses().size();
                layers.put(weight, layer);
            }
        }
        return DojoWrapper.require(_script, layers.isEmpty() ? null
                        : layers.descendingMap().values().iterator().next().getName(), _classes);
    }

    /**
     * Require.
     *
     * @param _script the script
     * @param _layer the layer
     * @param _classes the classes
     * @return the char sequence
     */
    public static CharSequence require(final CharSequence _script,
                                       final String _layer,
                                       final DojoClass... _classes)
    {
        final StringBuilder ret = new StringBuilder();
        if (_layer != null) {
            ret.append("require(['").append(_layer).append("'],function() {\n");
        }

        ret.append("require([");
        final StringBuilder paras = new StringBuilder();
        boolean first = true;
        final List<DojoClass> libs = Arrays.asList(_classes);

        final ComparatorChain<DojoClass> comparator = new ComparatorChain<>();
        comparator.addComparator(new Comparator<DojoClass>()
        {

            @Override
            public int compare(final DojoClass _arg0,
                               final DojoClass _arg1)
            {
                return _arg0.getParameterName() == null && _arg1.getParameterName() == null || _arg0
                                .getParameterName() != null && _arg1.getParameterName() != null ? 0
                                                : _arg0.getParameterName() == null ? 1 : -1;
            }
        });
        comparator.addComparator(new Comparator<DojoClass>()
        {

            @Override
            public int compare(final DojoClass _arg0,
                               final DojoClass _arg1)
            {
                return _arg0.getClassName().compareTo(_arg1.getClassName());
            }
        });
        Collections.sort(libs, comparator);
        for (final DojoClass dojoLibs : libs) {
            if (first) {
                first = false;
            } else {
                ret.append(",");
                if (dojoLibs.getParameterName() != null) {
                    paras.append(",");
                }
            }
            ret.append("\"").append(dojoLibs.getClassName()).append("\"");
            if (dojoLibs.getParameterName() != null) {
                paras.append(dojoLibs.getParameterName());
            }
        }
        if (_script == null) {
            ret.append("]);");
        } else {
            ret.append("],").append(" function(").append(paras).append(") {\n").append(_script).append("});");
        }
        if (_layer != null) {
            ret.append("});");
        }
        return ret;
    }
}
