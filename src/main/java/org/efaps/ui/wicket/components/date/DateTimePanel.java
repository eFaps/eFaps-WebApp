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

package org.efaps.ui.wicket.components.date;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.datetime.DateConverter;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.api.ci.UIFormFieldProperty;
import org.efaps.db.Context;
import org.efaps.ui.wicket.models.cell.CellSetValue;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to render a datefield with picker and in case that a time is wanted
 * renders fields for hours and minute.
 *
 * @author The eFaps Team
 */
public class DateTimePanel
    extends FormComponentPanel<DateTime>
    implements ILabelProvider<String>
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DateTimePanel.class);

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
     * The datepicker for the panel.
     */
    private DatePickerBehavior datePicker;

    /**
     * Field underlying this Panel.
     */
    private AbstractUIField cellvalue;

    /**
     * Was the value already converted.
     */
    private boolean converted;

    /**
     * Label of the Panel. mainly used for error indication.
     */
    private String fieldLabel;

    /**
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     * @param _fieldConf FieldConfiguration
     * @param _dateObject object containing a DateTime, if null or not DateTime
     *                       a new DateTime will be instantiated
     * @param _time must the time be rendered also
     * @throws EFapsException on error
     */
    public DateTimePanel(final String _wicketId,
                         final Model<AbstractUIField> _model,
                         final FieldConfiguration _fieldConf,
                         final Object _dateObject,
                         final boolean _time)
        throws EFapsException
    {
        this(_wicketId, _dateObject, _fieldConf.getName(), _model.getObject().getLabel(), _time,
                        _fieldConf.hasProperty(UIFormFieldProperty.COLUMNS)
                                    ? Integer.valueOf(_fieldConf.getProperty(UIFormFieldProperty.COLUMNS))
                                    : null);
        this.cellvalue = _model.getObject();
    }

    /**
     * @param _wicketId wicket id of this component
     * @param _dateObject object containing a DateTime, if null or not DateTime
     *                       a new DateTime will be instantiated
     * @param _fieldName Name of the field this DateTimePanel belongs to
     * @param _fieldLabel Label to e used in case of error
     * @param _time must the time be rendered also
     * @param _inputSize size of the input
     * @throws EFapsException on error
     */
    public DateTimePanel(final String _wicketId,
                         final Object _dateObject,
                         final String _fieldName,
                         final String _fieldLabel,
                         final boolean _time,
                         final Integer _inputSize)
        throws EFapsException
    {
        super(_wicketId, Model.<DateTime>of());
        this.datetime = _dateObject == null || !(_dateObject instanceof DateTime)
                        ? new DateTime(Context.getThreadContext().getChronology())
                        : (DateTime) _dateObject;
        this.fieldLabel = _fieldLabel;
        setLabel(Model.of(this.fieldLabel));
        this.converter = new StyleDateConverter(false) {

            private static final long serialVersionUID = 1L;

            @Override
            protected DateTimeZone getTimeZone()
            {
                DateTimeZone ret = null;
                try {
                    ret = Context.getThreadContext().getTimezone();
                } catch (final EFapsException e) {
                    DateTimePanel.LOG.error("EFapsException", e);
                } finally {
                    if (ret == null) {
                        super.getTimeZone();
                    }
                }
                return ret;
            }
        };

        this.fieldName = _fieldName;
        final DateTextField dateField = new DateTextField("date", new Model<Date>(this.datetime.toDate()),
                        this.converter)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public String getInputName()
            {
                return DateTimePanel.this.getDateFieldName();
            }

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                if (_inputSize != null) {
                    _tag.put("size", _inputSize);
                }
            }
        };
        this.add(dateField);

        this.datePicker = new DatePickerBehavior();
        dateField.add(this.datePicker);
        final WebComponent hour = new WebComponent("hours")
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                int hourTmp = DateTimePanel.this.datetime.getHourOfDay();
                if (use12HourFormat()) {
                    if (hourTmp == 0) {
                        hourTmp = 12;
                    }
                    if (hourTmp > 12) {
                        hourTmp = hourTmp - 12;
                    }
                }
                _tag.put("value", String.format("%02d", hourTmp));
                _tag.put("name", DateTimePanel.this.getHourFieldName());
                _tag.put("maxlength", 2);
            }

        };
        this.add(hour);
        hour.setVisible(_time);

        final WebComponent minutes = new WebComponent("minutes")
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("value", String.format("%02d", DateTimePanel.this.datetime.getMinuteOfHour()));
                _tag.put("name", DateTimePanel.this.getMinuteFieldName());
                _tag.put("maxlength", 2);

            }
        };
        this.add(minutes);
        minutes.setVisible(_time);

        final WebComponent ampm = new WebComponent("ampm")
        {

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
            public void onComponentTagBody(final MarkupStream _markupStream,
                                              final ComponentTag _openTag)
            {
                super.onComponentTagBody(_markupStream, _openTag);
                final StringBuilder html = new StringBuilder();
                html.append("<option ");
                final int hourTmp = DateTimePanel.this.datetime.getHourOfDay();
                if (hourTmp < 12) {
                    html.append("selected=\"true\"");
                }
                html.append(">am</option>").append("<option ");
                if (hourTmp > 11) {
                    html.append("selected=\"true\"");
                }
                html.append(">pm</option>");
                replaceComponentTagBody(_markupStream, _openTag, html);
            }
        };
        this.add(ampm);
        ampm.setVisible(_time);

        if (!use12HourFormat()) {
            ampm.setVisible(false);
        }
        this.add(new WebMarkupContainer("seperator").setVisible(_time));
    }

    /**
     * Depending on the locale am/pm is used or not.
     *
     * @return true if 12 hour format else false
     */
    protected boolean use12HourFormat()
    {
        final String pattern = DateTimeFormat.patternForStyle("-S", getLocale());
        return pattern.indexOf('a') != -1 || pattern.indexOf('h') != -1 || pattern.indexOf('K') != -1;
    }

    /**
     * Getter method for the instance variable {@link #datePicker}.
     *
     * @return value of instance variable {@link #datePicker}
     */
    public DatePickerBehavior getDatePicker()
    {
        return this.datePicker;
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
     * Method to get for the parameters returned from the form as a valid string. for a datetime
     *
     * @param _date date
     * @param _hour hour
     * @param _minute minutes
     * @param _ampm am/pm
     * @return valid string
     * @throws EFapsException on error
     */
    public List<StringValue> getDateAsString(final List<StringValue> _date,
                                             final List<StringValue> _hour,
                                             final List<StringValue> _minute,
                                             final List<StringValue> _ampm)
        throws EFapsException
    {
        final List<StringValue> ret = new ArrayList<StringValue>();
        final List<DateTime> dates = getDateList(_date, _hour, _minute, _ampm);
        for (final DateTime date : dates) {
            final DateTimeFormatter isofmt = ISODateTimeFormat.dateTime();
            ret.add(StringValue.valueOf(date.toString(isofmt)));
        }
        return ret;
    }

    /**
     * Method to get for the parameters returned from the form as a datetimes.
     *
     * @param _date date
     * @param _hour hour
     * @param _minute minutes
     * @param _ampm am/pm
     * @return valid string
     * @throws EFapsException on error
     */
    public List<DateTime> getDateList(final List<StringValue> _date,
                                      final List<StringValue> _hour,
                                      final List<StringValue> _minute,
                                      final List<StringValue> _ampm)
        throws EFapsException
    {
        final List<DateTime> ret = new ArrayList<DateTime>();
        if (_date != null) {
            Iterator<StringValue> hourIter = null;
            Iterator<StringValue> minuteIter = null;
            Iterator<StringValue> ampmIter = null;
            if (_hour != null) {
                hourIter = _hour.iterator();
            }
            if (_minute != null) {
                minuteIter = _minute.iterator();
            }
            if (_ampm != null) {
                ampmIter = _ampm.iterator();
            }

            for (final StringValue date : _date) {
                if (!date.isNull() && !date.isEmpty()) {
                    final DateTimeFormatter fmt = DateTimeFormat.forPattern(
                                    this.converter.getDatePattern(Context.getThreadContext().getLocale()))
                                    .withChronology(Context.getThreadContext().getChronology());
                    fmt.withLocale(getLocale());
                    final MutableDateTime mdt = fmt.parseMutableDateTime(date.toString());
                    if (hourIter != null) {
                        final StringValue hourStr = hourIter.next();
                        final int hour = Integer.parseInt(hourStr.toString("0"));
                        mdt.setHourOfDay(hour);
                        if (ampmIter != null) {
                            final StringValue ampmStr = ampmIter.next();
                            if ("am".equals(ampmStr.toString("am"))) {
                                if (hour == 12) {
                                    mdt.setHourOfDay(0);
                                }
                            } else {
                                if (hour != 12) {
                                    mdt.setHourOfDay(hour + 12);
                                }
                            }
                        }
                        if (minuteIter != null) {
                            final StringValue minuteStr = minuteIter.next();
                            final int minute = Integer.parseInt(minuteStr.toString("0"));
                            mdt.setMinuteOfHour(minute);
                        }
                    }
                    ret.add(mdt.toDateTime());
                }
            }
        }
        return ret;
    }


    /**
     * After rendering the datefields are added to the parent.
     */
    @Override
    protected void onAfterRender()
    {
        super.onAfterRender();
        final IDateListener container = this.findParent(IDateListener.class);
        if (container != null) {
            container.addDateComponent(this);
        }
    }

    @Override
    public void convertInput()
    {
        if (getCellvalue() != null) {
            try {
                this.converted = true;
                int i = 0;
                if (getCellvalue() instanceof CellSetValue) {
                    final UIFormCellSet cellset = ((CellSetValue) getCellvalue()).getCellSet();
                    i = cellset.getIndex(getFieldName());
                }
                final List<StringValue> dates = getRequest().getRequestParameters().getParameterValues(
                                getDateFieldName());
                final List<StringValue> hours = getRequest().getRequestParameters().getParameterValues(
                                getHourFieldName());
                final List<StringValue> minutes = getRequest().getRequestParameters().getParameterValues(
                                getMinuteFieldName());
                final List<StringValue> ampms = getRequest().getRequestParameters().getParameterValues(
                                getAmPmFieldName());
                final List<DateTime> dateTimes = getDateList(dates, hours, minutes, ampms);
                if (!dateTimes.isEmpty()) {
                    setConvertedInput(dateTimes.get(i));
                }
            } catch (final EFapsException e) {
                DateTimePanel.LOG.error("Catched error on convert input", e);
            }
        }
    }

    /**
     * Getter method for the instance variable {@link #cellvalue}.
     *
     * @return value of instance variable {@link #cellvalue}
     */
    protected AbstractUIField getCellvalue()
    {
        return this.cellvalue;
    }

    @Override
    public void updateModel()
    {
        if (!this.converted) {
            convertInput();
        }
        setModelObject(getConvertedInput());
        try {
            if (getCellvalue() != null) {
                getCellvalue().setValue(UIValue.get(getCellvalue().getValue().getField(), getCellvalue().getValue()
                            .getAttribute(), getDefaultModelObject()));
            }
        } catch (final CacheReloadException e) {
            DateTimePanel.LOG.error("Catched error on updateModel", e);
        }
    }


    /**
     * @param _date date list
     * @param _hour hour list
     * @param _minute minute lits
     * @param _ampm ampm list
     * @param _htmlTable html; the error msg will be appended to
     * @return true if validated successfully, else false
     */
    public boolean validate(final List<StringValue> _date,
                            final List<StringValue> _hour,
                            final List<StringValue> _minute,
                            final List<StringValue> _ampm,
                            final StringBuilder _htmlTable)
    {
        boolean ret = true;
        Iterator<StringValue> hourIter = null;
        Iterator<StringValue> minuteIter = null;
        Iterator<StringValue> ampmIter = null;
        if (_hour != null) {
            hourIter = _hour.iterator();
        }
        if (_minute != null) {
            minuteIter = _minute.iterator();
        }
        if (_ampm != null) {
            ampmIter = _ampm.iterator();
        }

        if (hourIter != null) {
            int i = 1;
            while (hourIter.hasNext()) {
                final StringValue hourStr = hourIter.next();
                int hour = 1;
                try {
                    hour = Integer.parseInt(hourStr.toString("0"));
                } catch (final NumberFormatException e) {
                    _htmlTable.append("<tr><td>");
                    if (_hour.size() > 1) {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.hour.nonumber.line", new Object[] { getFieldLabel(), i}));
                    } else {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                    + ".validate.hour.nonumber", new Object[] { getFieldLabel() }));
                    }
                    _htmlTable.append("</td></tr>");
                    ret = false;
                    break;
                }
                // if am/pm the value must be 1 - 12 , else 0 - 24
                if (ampmIter == null && (hour < 0 || hour > 24)) {
                    _htmlTable.append("<tr><td>");
                    if (_hour.size() > 1) {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.hour.line", new Object[] { getFieldLabel(), i, 0, 24 }));
                    } else {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.hour", new Object[] { getFieldLabel(), 0, 24 }));
                    }
                    _htmlTable.append("</td></tr>");
                    ret = false;
                    break;
                } else if (ampmIter != null && (hour < 1 || hour > 12)) {
                    _htmlTable.append("<tr><td>");
                    if (_hour.size() > 1) {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.hour.line", new Object[] { getFieldLabel(), i, 1, 12 }));
                    } else {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.hour", new Object[] { getFieldLabel(), 1, 12 }));
                    }
                    _htmlTable.append("</td></tr>");
                    ret = false;
                    break;
                }
                i++;
            }
        }

        if (minuteIter != null) {
            int i = 1;
            while (minuteIter.hasNext()) {
                final StringValue minuteStr = minuteIter.next();
                int minute = 0;
                try {
                    minute = Integer.parseInt(minuteStr.toString("0"));
                } catch (final NumberFormatException e) {
                    _htmlTable.append("<tr><td>");
                    if (_hour.size() > 1) {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.minute.nonumber.line", new Object[] { getFieldLabel(), i}));
                    } else {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                    + ".validate.minute.nonumber", new Object[] { getFieldLabel() }));
                    }
                    _htmlTable.append("</td></tr>");
                    ret = false;
                    break;
                }
                if (minute < 0 || minute > 59) {
                    _htmlTable.append("<tr><td>");
                    if (_hour.size() > 1) {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.minute.line", new Object[] { getFieldLabel(), i, 0, 59 }));
                    } else {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.minute", new Object[] { getFieldLabel(), 0, 59 }));
                    }
                    _htmlTable.append("</td></tr>");
                    ret = false;
                    break;
                }
                i++;
            }
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #fieldLabel}.
     *
     * @return value of instance variable {@link #fieldLabel}
     */
    public String getFieldLabel()
    {
        return this.fieldLabel;
    }

    @Override
    public IModel<String> getLabel()
    {
        return Model.of(getFieldLabel());
    }
}
