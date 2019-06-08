/*
 * Copyright 2003 - 2019 The eFaps Team
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
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.api.ci.UIFormFieldProperty;
import org.efaps.db.Context;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.components.values.IFieldConfig;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.field.set.UIFieldSet;
import org.efaps.ui.wicket.models.field.set.UIFieldSetValue;
import org.efaps.ui.wicket.models.objects.UITable;
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
import org.wicketstuff.datetime.DateConverter;
import org.wicketstuff.datetime.StyleDateConverter;
import org.wicketstuff.datetime.markup.html.form.DateTextField;

/**
 * Class to render a datefield with picker and in case that a time is wanted
 * renders fields for hours and minute.
 *
 * @author The eFaps Team
 */
public class DateTimePanel
    extends FormComponentPanel<DateTime>
    implements IFieldConfig
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
    private AbstractUIField uiField;

    /**
     * Was the value already converted.
     */
    private boolean converted;

    /** The field config. */
    private FieldConfiguration fieldConfig;

    /**
     * Instantiates a new date time panel.
     *
     * @param _wicketId wicket id of this component
     * @param _model the model
     * @param _fieldConf the field conf
     * @param _dateObject object containing a DateTime, if null or not DateTime
     *                       and field is required new DateTime will be instantiated
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
        super(_wicketId, Model.<DateTime>of());
        uiField = _model.getObject();
        fieldConfig = _fieldConf;
        if (_dateObject instanceof DateTime) {
            datetime = (DateTime) _dateObject;
        } else if (_fieldConf.getField().isRequired()) {
            datetime = new DateTime(Context.getThreadContext().getChronology());
        } else {
            datetime = null;
        }
        if (uiField != null) {
            setLabel(Model.of(getFieldConfig().evalLabel(uiField.getValue(), uiField.getInstance())));
        } else {
            setLabel(Model.of(getFieldConfig().getLabel()));
        }
        converter = new StyleDateConverter(false) {

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

        final DateTextField dateField = new DateTextField("date", new Model<>(datetime == null ? null : datetime.toDate()),
                        converter)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public String getInputName()
            {
                return DateTimePanel.this.getDateFieldName();
            }
        };
        this.add(dateField);

        if (getFieldConfig().hasProperty(UIFormFieldProperty.COLUMNS)) {
            add(new AttributeModifier("maxlength", getFieldConfig().getProperty(UIFormFieldProperty.COLUMNS)));
        }
        if (getFieldConfig().hasProperty(UIFormFieldProperty.WIDTH)
                        && uiField != null && !(uiField.getParent() instanceof UITable)) {
            add(new AttributeAppender("style", "width:" + getFieldConfig().getWidth(), ";"));
        }

        datePicker = new DatePickerBehavior();
        dateField.add(datePicker);

        final WebComponent hour = new WebComponent("hours")
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                int hourTmp = datetime.getHourOfDay();
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
                _tag.put("value", String.format("%02d", datetime.getMinuteOfHour()));
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
                final int hourTmp = datetime.getHourOfDay();
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

        if (getFieldConfig() != null && getFieldConfig().getField().hasEvents(EventType.UI_FIELD_UPDATE)) {
            final List<EventDefinition> events = getFieldConfig().getField().getEvents(EventType.UI_FIELD_UPDATE);
            String eventName = "change";
            for (final EventDefinition event : events) {
                eventName = event.getProperty("Event") == null ? "change" : event.getProperty("Event");
            }
            dateField.add(new AjaxFieldUpdateBehavior(eventName, Model.of(uiField), false));
            if (_time) {
                hour.add(new AjaxFieldUpdateBehavior(eventName, Model.of(uiField), false));
                minutes.add(new AjaxFieldUpdateBehavior(eventName, Model.of(uiField), false));
                if (use12HourFormat()) {
                    ampm.add(new AjaxFieldUpdateBehavior(eventName, Model.of(uiField), false));
                }
            }
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
        return datePicker;
    }

    /**
     * Getter method for instance variable {@link #fieldName}.
     *
     * @return value of instance variable {@link #fieldName}
     */
    public String getFieldName()
    {
        return getFieldConfig().getName();
    }

    /**
     * @return name for the field containing the date
     */
    public String getDateFieldName()
    {
        return getFieldName() + "_eFapsDate";
    }

    /**
     * @return name for the field containing the hours
     */
    public String getHourFieldName()
    {
        return getFieldName() + "_eFapsHour";
    }

    /**
     * @return name for the field containing the minutes
     */
    public String getMinuteFieldName()
    {
        return getFieldName() + "_eFapsMinute";
    }

    /**
     * @return name for the field containing am/pm
     */
    public String getAmPmFieldName()
    {
        return getFieldName() + "_eFapsAmPm";
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
        final List<StringValue> ret = new ArrayList<>();
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
        final List<DateTime> ret = new ArrayList<>();
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
                                    converter.getDatePattern(Context.getThreadContext().getLocale()))
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
        if (getUIField() != null) {
            try {
                converted = true;
                int i = 0;
                if (getUIField() instanceof UIFieldSetValue) {
                    final UIFieldSet cellset = ((UIFieldSetValue) getUIField()).getCellSet();
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
    protected AbstractUIField getUIField()
    {
        return uiField;
    }

    @Override
    public void updateModel()
    {
        if (!converted) {
            convertInput();
        }
        setModelObject(getConvertedInput());
        try {
            if (getUIField() != null) {
                getUIField().setValue(UIValue.get(getUIField().getValue().getField(), getUIField().getValue()
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
        final String fieldLabel = getLabel().getObject();
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
                                        + ".validate.hour.nonumber.line", new Object[] { fieldLabel, i}));
                    } else {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                    + ".validate.hour.nonumber", new Object[] { fieldLabel }));
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
                                        + ".validate.hour.line", new Object[] { fieldLabel, i, 0, 24 }));
                    } else {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.hour", new Object[] { fieldLabel, 0, 24 }));
                    }
                    _htmlTable.append("</td></tr>");
                    ret = false;
                    break;
                } else if (ampmIter != null && (hour < 1 || hour > 12)) {
                    _htmlTable.append("<tr><td>");
                    if (_hour.size() > 1) {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.hour.line", new Object[] { fieldLabel, i, 1, 12 }));
                    } else {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.hour", new Object[] { fieldLabel, 1, 12 }));
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
                                        + ".validate.minute.nonumber.line", new Object[] { fieldLabel, i}));
                    } else {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                    + ".validate.minute.nonumber", new Object[] { fieldLabel }));
                    }
                    _htmlTable.append("</td></tr>");
                    ret = false;
                    break;
                }
                if (minute < 0 || minute > 59) {
                    _htmlTable.append("<tr><td>");
                    if (_hour.size() > 1) {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.minute.line", new Object[] { fieldLabel, i, 0, 59 }));
                    } else {
                        _htmlTable.append(DBProperties.getFormatedDBProperty(DateTimePanel.class.getName()
                                        + ".validate.minute", new Object[] { fieldLabel, 0, 59 }));
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

    @Override
    public FieldConfiguration getFieldConfig()
    {
        return fieldConfig;
    }
}
