package org.efaps.ui.wicket.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections4.comparators.ComparatorChain;

/**
 * The Class DojoWrapper.
 */
public final class DojoWrapper
{

    /**
     * Require.
     *
     * @param _parameter the parameter
     * @param _script the script
     * @param _classes the classes
     * @return the char sequence
     */
    public static CharSequence require(final CharSequence _script,
                                       final DojoClass... _classes)
    {
        final StringBuilder ret = new StringBuilder().append("require([");
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
                                                : (_arg0.getParameterName() == null ? 1 : -1);
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
        ret.append("],").append(" function(").append(paras).append(") {\n").append(_script).append("});");
        return ret;
    }
}
