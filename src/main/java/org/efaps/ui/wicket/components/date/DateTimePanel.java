/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.ui.wicket.components.date;

import java.util.Date;

import org.apache.wicket.datetime.DateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Class to render a datefield with picker and in case that a time is wanted
 * renders fields for hours and minute.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DateTimePanel extends Panel
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Name of the field this DateTimePanel belongs to.
     */
    private final String fieldName;

    /**
     * DateConverter needed to enable date formating related to the locale.
     */
    private final DateConverter converter;

    /**
     * DateTime of this DateTimePanel.
     */
    private final DateTime datetime;

    /**
     * @param _wicketId     wicket id of this componnet
     * @param _dateObject   object containing a DateTime, if null or not DateTime
     *                      a new DateTime will be instantiated
     * @param _converter    DateConverter needed to enable date formating related
     *                      to the locale
     * @param _fieldName    Name of the field this DateTimePanel belongs to
     * @param _time must    the time be rendered also
     */
    public DateTimePanel(final String _wicketId, final Object _dateObject, final DateConverter _converter,
                         final String _fieldName, final boolean _time)
    {
        super(_wicketId);
        this.datetime = ((_dateObject == null || !(_dateObject instanceof DateTime)) ? new DateTime()
                        : (DateTime) _dateObject);

        this.converter = _converter;

        this.fieldName = _fieldName;
        final DateTextField dateField = new DateTextField("date", new Model<Date>(this.datetime.toDate()), _converter) {

            private static final long serialVersionUID = 1L;

            @Override
            public String getInputName()
            {
                return DateTimePanel.this.getDateFieldName();
            }
        };
        this.add(dateField);

        dateField.add(new DatePicker() {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean enableMonthYearSelection()
            {
                return true;
            }
        });

        final WebComponent hour = new WebComponent("hours") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("value", DateTimePanel.this.datetime.getHourOfDay() % (use12HourFormat() ? 12 : 24));
                _tag.put("name", DateTimePanel.this.getHourFieldName());
                _tag.put("maxlength", 2);
            }

        };
        this.add(hour);
        hour.setVisible(_time);

        final WebComponent minutes = new WebComponent("minutes") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("value", DateTimePanel.this.datetime.getMinuteOfHour());
                _tag.put("name", DateTimePanel.this.getMinuteFieldName());
                _tag.put("maxlength", 2);

            }
        };
        this.add(minutes);
        minutes.setVisible(_time);

        final WebComponent ampm = new WebComponent("ampm") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("name", DateTimePanel.this.getAmPmFieldName());
            }

            /**
             * set an am or pm option
             */
            @Override
            protected void onComponentTagBody(final MarkupStream _markupStream, final ComponentTag _openTag)
            {
                super.onComponentTagBody(_markupStream, _openTag);
                final StringBuilder html = new StringBuilder();
                html.append("<option ").append(
                                DateTimePanel.this.datetime.getHourOfDay() > 11 ? "" : "selected=\"true\"").append(
                                ">am</option>").append("<option ").append(
                                DateTimePanel.this.datetime.getHourOfDay() > 11 ? "selected=\"true\"" : "").append(
                                ">pm</option>");
                replaceComponentTagBody(_markupStream, _openTag, html);
            }

        };
        this.add(ampm);
        ampm.setVisible(_time);

        if (!use12HourFormat()) {
            ampm.setVisible(false);
        }
        this.add(new WebComponent("seperator").setVisible(_time));
    }

    /**
     * Depending on the locale am/pm is used or not.
     * @return true if 12 hour format else false
     */
    protected boolean use12HourFormat()
    {
        final String pattern = DateTimeFormat.patternForStyle("-S", getLocale());
        return pattern.indexOf('a') != -1 || pattern.indexOf('h') != -1 || pattern.indexOf('K') != -1;
    }

    /**
     * Getter method for instance variable {@link #fieldName}.
     *
     * @return value of instance variable {@link #fieldName}
     */
    public String getFieldName()
    {
        return this.fieldName;
    }


    /**
     * @return name for the field containing the date
     */
    public String getDateFieldName()
    {
        return this.fieldName + "_eFapsDate";
    }

    /**
     * @return name for the field containing the hours
     */
    public String getHourFieldName()
    {
        return this.fieldName + "_eFapsHour";
    }

    /**
     * @return name for the field containing the minutes
     */
    public String getMinuteFieldName()
    {
        return this.fieldName + "_eFapsMinute";
    }

    /**
     * @return name for the field containing am/pm
     */
    public String getAmPmFieldName()
    {
        return this.fieldName + "_eFapsAmPm";
    }

    /**
     * Method to get for the parameters returned from the form as a valid string.
     * for a datetime
     *
     * @param _date     date
     * @param _hour     hour
     * @param _minute   minutes
     * @param _ampm     am/pm
     * @return  valid string
     */
    public String getDateAsString(final String[] _date, final String[] _hour, final String[] _minute,
                                  final String[] _ampm)
    {
        String ret = null;
        if (_date != null && _date[0].length() > 0) {
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(this.converter.getDatePattern());
            fmt.withLocale(getLocale());
            final MutableDateTime mdt = fmt.parseMutableDateTime(_date[0]);

            if (_hour != null && _hour[0].length() > 0) {
                final int hour = Integer.parseInt(_hour[0]);
                if (use12HourFormat()) {
                    if ("pm".equals(_ampm[0])) {
                        mdt.setHourOfDay(hour + 12);
                    }
                } else {
                    mdt.setHourOfDay(hour);
                }
                if (_minute != null && _minute[0].length() > 0) {
                    final int minute = Integer.parseInt(_minute[0]);
                    mdt.setMinuteOfHour(minute);
                }
            }
            final DateTimeFormatter isofmt = ISODateTimeFormat.dateTime();
            ret = mdt.toString(isofmt);
        }
        return ret;
    }
}
