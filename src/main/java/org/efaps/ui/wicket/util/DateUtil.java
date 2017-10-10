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

import org.apache.wicket.datetime.StyleDateConverter;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Singelton class for date functions.
 *
 * @author The eFaps Team
 */
public final class DateUtil
{
    /**
     * Singelton.
     */
    private DateUtil()
    {
    }

    /**
     * Convert a date from a parameter into a <code>DateTime</code>.
     *
     * @param _value value to be converted
     * @return DateTime
     * @throws EFapsException on error
     */
    public static DateTime getDateFromParameter(final String _value)
        throws EFapsException
    {
        final StyleDateConverter styledate = new StyleDateConverter(false);
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(styledate.getDatePattern(Context.getThreadContext()
                        .getLocale()));
        fmt.withLocale(Context.getThreadContext().getLocale());
        final DateTime dt = fmt.parseDateTime(_value);
        return dt;
    }

    /**
     * Convert a <code>DateTime</code> to a String for parameter.
     *
     * @param _value value to be converted
     * @return DateTime
     * @throws EFapsException on error
     */
    public static String getDate4Parameter(final DateTime _value)
        throws EFapsException
    {
        final StyleDateConverter styledate = new StyleDateConverter(false);
        final DateTimeFormatter fmt = DateTimeFormat.forPattern(styledate.getDatePattern(Context.getThreadContext()
                        .getLocale()));
        fmt.withLocale(Context.getThreadContext().getLocale());
        return _value.toString(fmt);
    }

    /**
     * Gets the date time formatter.
     *
     * @return the date time formatter
     * @throws EFapsException the e faps exception
     */
    public static DateTimeFormatter getDateFormatter()
        throws EFapsException
    {
        final String formatStr = Configuration.getAttribute(Configuration.ConfigAttribute.FORMAT_DATE);
        final DateTimeFormatter ret;
        if (formatStr.matches("^[S,M,L,F,-]{2}$")) {
            ret = DateTimeFormat.forStyle(formatStr);
        } else {
            ret = DateTimeFormat.forPattern(formatStr);
        }
        return ret.withLocale(Context.getThreadContext().getLocale());
    }

    /**
     * Gets the date pattern.
     *
     * @return the date pattern
     * @throws EFapsException the e faps exception
     */
    public static String getDatePattern()
        throws EFapsException
    {
        final String formatStr = Configuration.getAttribute(Configuration.ConfigAttribute.FORMAT_DATE);
        return formatStr.matches("^[S,M,L,F,-]{2}$")
                        ? DateTimeFormat.patternForStyle(formatStr, Context.getThreadContext().getLocale())
                        : formatStr;
    }

    /**
     * Gets the date time formatter.
     *
     * @return the date time formatter
     * @throws EFapsException the e faps exception
     */
    public static DateTimeFormatter getDateTimeFormatter()
        throws EFapsException
    {
        final String formatStr = Configuration.getAttribute(Configuration.ConfigAttribute.FORMAT_DATETIME);
        final DateTimeFormatter ret;
        if (formatStr.matches("^[S,M,L,F,-]{2}$")) {
            ret = DateTimeFormat.forStyle(formatStr);
        } else {
            ret = DateTimeFormat.forPattern(formatStr);
        }
        return ret.withLocale(Context.getThreadContext().getLocale());
    }

    /**
     * Gets the date pattern.
     *
     * @return the date pattern
     * @throws EFapsException the e faps exception
     */
    public static String getDateTimePattern()
        throws EFapsException
    {
        final String formatStr = Configuration.getAttribute(Configuration.ConfigAttribute.FORMAT_DATETIME);
        return formatStr.matches("^[S,M,L,F,-]{2}$")
                        ? DateTimeFormat.patternForStyle(formatStr, Context.getThreadContext().getLocale())
                        : formatStr;
    }
}
