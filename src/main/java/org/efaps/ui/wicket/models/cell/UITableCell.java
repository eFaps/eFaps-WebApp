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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.models.cell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldPicker;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * This class represents the model wich is used for rendering the components
 * of one cell inside a Table.It uses a
 * {@link org.efaps.admin.ui.field.Field} as the base for the data.
 *
 * @author The eFaps Team
 * @version $Id:CellModel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class UITableCell
    extends AbstractUICell
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * instance variable storing the reference of the field.
     */
    private String reference;

    /**
     * Variable storing the value as it was retrieved from the eFaps-Database
     * by a query. The value is used for
     * comparisons and to be able to access the original value.
     */
    private final Object compareValue;

    /**
     * instance variable storing the icon of the field.
     */
    private final String icon;

    /**
     * instance variable storing if the cell is fixed width.
     */
    private final boolean fixedWidth;

    /**
     * Title for the cell. Will be e.g rendered as title in a div.
     */
    private String cellTitle;

    /**
     * Stores if the field has an esjp used for the update of other fields.
     */
    private final boolean fieldUpdate;

    /**
     * Stores the update event for the field update. Default is "onchange";
     */
    private String fieldUpdateEvent;

    /**
     * Is the field multiRow. Meaning that it has more than one row.
     */
    private final boolean multiRows;

    /**
     * The align of this cell.
     */
    private final String align;

    /**
     * Picker related to this field.
     */
    private final UIPicker picker;

    /**
     * Show numbering in the cell.
     */
    private final boolean showNumbering;

    /**
     * Settings for the AutoComplete.
     */
    private AutoCompleteSettings autoCompleteSetting;

    /**
     * Constructor.
     *
     * @param _parent parent ui object
     * @param _fieldValue FieldValue
     * @param _cellvalue Value for the cell
     * @param _cellTitle title for the cell, if null will be set to _cellvalue
     * @param _icon icon of the cell
     * @param _instance Instance
     * @throws EFapsException on error
     */
    public UITableCell(final AbstractUIObject _parent,
                       final FieldValue _fieldValue,
                       final Instance _instance,
                       final String _cellvalue,
                       final String _cellTitle,
                       final String _icon)
        throws EFapsException
    {
        super(_parent, _fieldValue, _instance == null ? null : _instance.getKey(), _cellvalue);
        this.showNumbering = _fieldValue.getField().isShowNumbering();
        this.compareValue = _fieldValue.getObject4Compare();
        this.fixedWidth = _fieldValue.getField().isFixedWidth();
        this.align = _fieldValue.getField().getAlign();
        this.cellTitle = _cellTitle == null ? _cellvalue : _cellTitle;
        this.icon = _icon;
        this.multiRows = _fieldValue.getField().getRows() > 1;

        if (_fieldValue.getField().hasEvents(EventType.UI_FIELD_AUTOCOMPLETE)) {
            this.autoCompleteSetting = new AutoCompleteSettings();
            this.autoCompleteSetting.setFieldName(getName());
            final List<EventDefinition> events = _fieldValue.getField().getEvents(EventType.UI_FIELD_AUTOCOMPLETE);
            for (final EventDefinition event : events) {
                this.autoCompleteSetting.setMinInputLength(event.getProperty("MinInputLength") == null
                                ? 1 : Integer.valueOf(event.getProperty("MinInputLength")));
                this.autoCompleteSetting.setMaxChoiceLength(event.getProperty("MaxChoiceLength") == null
                                ? -1 : Integer.valueOf(event.getProperty("MaxChoiceLength")));
                this.autoCompleteSetting.setMaxValueLength(event.getProperty("MaxValueLength") == null
                                ? -1 : Integer.valueOf(event.getProperty("MaxValueLength")));
                final String ep = event.getProperty("ExtraParameter");
                if (ep != null) {
                    this.autoCompleteSetting.getExtraParameters().add(ep);
                    for (int i = 1; i < 100; i++) {
                        final String keyTmp = "ExtraParameter" + String.format("%02d", i);
                        final String epTmp = event.getProperty(keyTmp);
                        if (epTmp == null) {
                            break;
                        } else {
                            this.autoCompleteSetting.getExtraParameters().add(epTmp);
                        }
                    }
                }

            }
        }
        this.fieldUpdate = _fieldValue.getField().hasEvents(EventType.UI_FIELD_UPDATE);
        if (this.fieldUpdate) {
            final List<EventDefinition> events = _fieldValue.getField().getEvents(EventType.UI_FIELD_UPDATE);
            for (final EventDefinition event : events) {
                this.fieldUpdateEvent = event.getProperty("Event") == null ? "onchange" : event.getProperty("Event");
            }
        }
        // check if the user has access to the typemenu, if not set the reference to null
        if (_fieldValue.getField().getReference() != null) {
            if (getInstanceKey() != null) {
                final Menu menu = Menu.getTypeTreeMenu(_instance.getType());
                if (menu != null && menu.hasAccess(getParent().getMode(), getInstance())
                            && (!((AbstractUIPageObject) _parent).getAccessMap().containsKey(getInstance())
                                || (((AbstractUIPageObject) _parent).getAccessMap().containsKey(getInstance())
                                && ((AbstractUIPageObject) _parent).getAccessMap().get(getInstance())))) {
                    this.reference = _fieldValue.getField().getReference();
                } else if (_fieldValue.getField().getReference().contains("/servlet/checkout")) {
                    this.reference = _fieldValue.getField().getReference();
                }
            }
        }
        this.picker = evaluatePicker(_fieldValue);
    }

    /**
     * Method to evaluate the picker.
     * @param _fieldValue FieldValue
     * @return UIPIcker
     * @throws CacheReloadException on error during access to command
     */
    private UIPicker evaluatePicker(final FieldValue _fieldValue)
        throws CacheReloadException
    {
        UIPicker ret = null;
        if (_fieldValue.getField() instanceof FieldPicker) {
            ret = new UIPicker((FieldPicker) _fieldValue.getField(), this);
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #align}.
     *
     * @return value of instance variable {@link #align}
     */
    public String getAlign()
    {
        return this.align;
    }

    /**
     * This is the getter method for the instance variable {@link #reference}.
     *
     * @return value of instance variable {@link #reference}
     */

    public String getReference()
    {
        return this.reference;
    }

    /**
     * This is the setter method for the instance variable {@link #reference}.
     *
     * @param _reference the reference to set
     */
    public void setReference(final String _reference)
    {
        this.reference = _reference;
    }

    /**
     * This is the getter method for the instance variable {@link #compareValue} .
     *
     * @return value of instance variable {@link #compareValue}
     */
    public Object getCompareValue()
    {
        return this.compareValue;
    }

    /**
     * This is the getter method for the instance variable {@link #cellTitle}.
     *
     * @return value of instance variable {@link #cellTitle}
     */
    public String getCellTitle()
    {
        return this.cellTitle == null ? "" : this.cellTitle;
    }

    /**
     * Setter method for instance variable {@link #cellTitle}.
     *
     * @param _cellTitle value for instance variable {@link #cellTitle}
     */

    public void setCellTitle(final String _cellTitle)
    {
        this.cellTitle = _cellTitle;
    }

    /**
     * This is the getter method for the instance variable {@link #icon}.
     *
     * @return value of instance variable {@link #icon}
     */

    public String getIcon()
    {
        return this.icon;
    }

    /**
     * This is the getter method for the instance variable {@link #fixedWidth}.
     *
     * @return value of instance variable {@link #fixedWidth}
     */
    public boolean isFixedWidth()
    {
        return this.fixedWidth;
    }

    /**
     * This method returns if the field is a link which makes a checkout.
     *
     * @return true if it is a checkout
     */
    public boolean isCheckOut()
    {
        return this.reference.contains("/servlet/checkout");
    }

    /**
     * @return the multiRows
     */
    public boolean isMultiRows()
    {
        return this.multiRows;
    }

    /**
     * Getter method for instance variable {@link #showNumbering}.
     *
     * @return value of instance variable {@link #showNumbering}
     */
    public boolean isShowNumbering()
    {
        return this.showNumbering;
    }

    /**
     * Method to execute the events.
     *
     * @param _eventType    type of the event to be executed
     * @param _others       object to be passed to the executed event with ParameterValues.OTHERS
     * @param _uiID2Oid     mapping of session userinterface ids to oids
     * @return List of Returns
     * @throws EFapsException on error
     */
    public List<Return> executeEvents(final EventType _eventType,
                                      final Object _others,
                                      final Map<String, String> _uiID2Oid)
        throws EFapsException
    {
        List<Return> ret = new ArrayList<Return>();
        final Field field = getField();
        if (field.hasEvents(_eventType)) {
            final Context context = Context.getThreadContext();
            final String[] contextoid = { getInstanceKey() };
            context.getParameters().put("oid", contextoid);
            ret = field.executeEvents(_eventType,
                            ParameterValues.INSTANCE, getInstance(),
                            ParameterValues.OTHERS, _others,
                            ParameterValues.PARAMETERS, context.getParameters(),
                            ParameterValues.CLASS, this,
                            ParameterValues.OIDMAP4UI, _uiID2Oid);
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #autoComplete}.
     *
     * @return value of instance variable {@link #autoComplete}
     */
    public boolean isAutoComplete()
    {
        return this.autoCompleteSetting != null;
    }

    /**
     * Method to get the auto completion event.
     *
     * @param _others object to be passed to the executed event
     * @param _uiID2Oid mapping of UserInterface Id and OID
     * @return List of Returns
     * @throws EFapsException on error
     */
    public List<Return> getAutoCompletion(final Object _others,
                                          final Map<String, String> _uiID2Oid)
        throws EFapsException
    {
        return executeEvents(EventType.UI_FIELD_AUTOCOMPLETE, _others, _uiID2Oid);
    }

    /**
     * Getter method for instance variable {@link #fieldUpdate}.
     *
     * @return value of instance variable {@link #fieldUpdate}
     */
    public boolean isFieldUpdate()
    {
        return this.fieldUpdate;
    }

    /**
     * Getter method for instance variable {@link #fieldUpdateEvent}.
     *
     * @return value of instance variable {@link #fieldUpdateEvent}
     */
    public String getFieldUpdateEvent()
    {
        return this.fieldUpdateEvent;
    }

    /**
     * Method to get the field update event.
     *
     * @param _others   object to be passed to the executed event
     * @param _uiID2Oid mapping of UserInterface Id and OID
     * @return List of Returns
     * @throws EFapsException on error
     */
    public List<Return> getFieldUpdate(final Object _others,
                                       final Map<String, String> _uiID2Oid)
        throws EFapsException
    {
        return executeEvents(EventType.UI_FIELD_UPDATE, _others, _uiID2Oid);
    }

    /**
     * Getter method for instance variable {@link #valuePicker}.
     *
     * @return value of instance variable {@link #valuePicker}
     */
    public boolean isValuePicker()
    {
        return this.picker != null;
    }

    /**
     * Getter method for instance variable {@link #picker}.
     *
     * @return value of instance variable {@link #picker}
     */
    public UIPicker getPicker()
    {
        return this.picker;
    }

    /**
     * Getter method for the instance variable {@link #autoCompleteSetting}.
     *
     * @return value of instance variable {@link #autoCompleteSetting}
     */
    public AutoCompleteSettings getAutoCompleteSetting()
    {
        return this.autoCompleteSetting;
    }

    /**
     * Setter method for instance variable {@link #autoCompleteSetting}.
     *
     * @param _autoCompleteSetting value for instance variable {@link #autoCompleteSetting}
     */
    public void setAutoCompleteSetting(final AutoCompleteSettings _autoCompleteSetting)
    {
        this.autoCompleteSetting = _autoCompleteSetting;
    }
}
