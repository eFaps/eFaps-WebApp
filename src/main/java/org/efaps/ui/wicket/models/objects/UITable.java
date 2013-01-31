/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.util.io.IClusterable;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.Filter;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.ui.wicket.models.cell.UIHiddenCell;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.UITableHeader.FilterType;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.DateTimeUtil;
import org.efaps.util.EFapsException;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO description!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UITable
    extends AbstractUIHeaderObject
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UITable.class);

    /**
     * Serial Id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Map contains the applied filters to this table.
     */
    private final Map<String, TableFilter> filters = new HashMap<String, TableFilter>();

    /**
     * Map is used to store the filters in relation to a field name. It is used
     * temporarily when the table is (due to a database based filter) requeried,
     * to be able to set the filters to the headers by copying it from the old
     * header to the new one.
     *
     * @see #getInstanceListsOld()
     * @see #execute4InstanceOld()
     */
    private final Map<String, TableFilter> filterTempCache = new HashMap<String, TableFilter>();

    /**
     * All evaluated rows of this table are stored in this list.
     *
     * @see #getValues
     */
    private final List<UIRow> values = new ArrayList<UIRow>();

    /**
     * Thie Row is used in case of edit to create new empty rows.
     */
    private UIRow emptyRow;

    /**
     * Constructor setting the uuid and Key of the instance.
     *
     * @param _commandUUID UUID of the Command
     * @param _instanceKey Key of the instance
     * @throws EFapsException on error
     */
    public UITable(final UUID _commandUUID,
                   final String _instanceKey)
        throws EFapsException
    {
        super(_commandUUID, _instanceKey);
        initialise();
    }

    /**
     * Constructor setting the uuid and Key of the instance.
     *
     * @param _commandUUID UUID of the Command
     * @param _instanceKey Key of the instance
     * @param _openerId id of the opener
     * @throws EFapsException on error
     */
    public UITable(final UUID _commandUUID,
                   final String _instanceKey,
                   final String _openerId)
        throws EFapsException
    {
        super(_commandUUID, _instanceKey, _openerId);
        initialise();
    }

    /**
     * Method that initializes the TableModel.
     *
     * @throws EFapsException on error
     */
    private void initialise()
        throws EFapsException
    {

        final AbstractCommand command = getCommand();
        if (command == null) {
            setShowCheckBoxes(false);
        } else {
            // set target table
            if (command.getTargetTable() != null) {
                setTableUUID(command.getTargetTable().getUUID());
                if (Context.getThreadContext().containsSessionAttribute(getCacheKey(UITable.UserCacheKey.FILTER))) {
                    @SuppressWarnings("unchecked")
                    final Map<String, TableFilter> sessfilter = (Map<String, TableFilter>) Context.getThreadContext()
                        .getSessionAttribute(getCacheKey(UITable.UserCacheKey.FILTER));
                    for (final Field field : command.getTargetTable().getFields()) {
                        if (sessfilter.containsKey(field.getName())) {
                            final TableFilter filter = sessfilter.get(field.getName());
                            filter.setHeaderFieldId(field.getId());
                            this.filters.put(field.getName(), filter);
                        }
                    }
                } else {
                    // add the filter here, if it is a required filter that must be
                    // applied against the database
                    for (final Field field : command.getTargetTable().getFields()) {
                        if (field.getFilter().isRequired()
                                        && field.getFilter().getBase().equals(Filter.Base.DATABASE)) {
                            this.filters.put(field.getName(), new TableFilter());
                        }
                    }
                }
            }
            // set default sort
            if (command.getTargetTableSortKey() != null) {
                setSortKeyInternal(command.getTargetTableSortKey());
                setSortDirection(command.getTargetTableSortDirection());
            }

            setShowCheckBoxes(command.isTargetShowCheckBoxes());
            // get the User specific Attributes if exist overwrite the defaults
            try {
                if (Context.getThreadContext().containsUserAttribute(
                                getCacheKey(UITable.UserCacheKey.SORTKEY))) {
                    setSortKeyInternal(Context.getThreadContext().getUserAttribute(
                                    getCacheKey(UITable.UserCacheKey.SORTKEY)));
                }
                if (Context.getThreadContext().containsUserAttribute(
                                getCacheKey(UITable.UserCacheKey.SORTDIRECTION))) {
                    setSortDirection(SortDirection.getEnum(Context.getThreadContext()
                                    .getUserAttribute(getCacheKey(UITable.UserCacheKey.SORTDIRECTION))));
                }
            } catch (final EFapsException e) {
                // we don't throw an error because this are only Usersettings
                UITable.LOG.error("error during the retrieve of UserAttributes", e);
            }
        }
    }

    /**
     * Method to get the list of instance.
     *
     * @return List of instances
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    protected List<Instance> getInstanceList()
        throws EFapsException
    {
        // get the filters that must be applied against the database
        final Map<String, Map<String, Object>> dataBasefilters = new HashMap<String, Map<String, Object>>();
        final Iterator<Entry<String, TableFilter>> iter = this.filters.entrySet().iterator();
        this.filterTempCache.clear();
        while (iter.hasNext()) {
            final Entry<String, TableFilter> entry = iter.next();
            if (entry.getValue().getUiTableHeader() == null
                           || (entry.getValue().getUiTableHeader() != null
                           && entry.getValue().getUiTableHeader().getFilter().getBase().equals(Filter.Base.DATABASE))) {
                final Map<String, Object> map = entry.getValue().getMap4esjp();
                dataBasefilters.put(entry.getKey(), map);
            }
            this.filterTempCache.put(entry.getKey(), entry.getValue());
            iter.remove();
        }

        final List<Return> ret = getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
                        ParameterValues.INSTANCE, getInstance(),
                        ParameterValues.OTHERS, dataBasefilters);
        List<Instance> lists = null;
        if (ret.size() < 1) {
            throw new EFapsException(UITable.class, "getInstanceList");
        } else {
            lists = (List<Instance>) ret.get(0).get(ReturnValues.VALUES);
        }
        return lists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute()
    {
        try {
            if (isCreateMode()) {
                execute4NoInstance();
            } else {
                final List<Instance> instances = getInstanceList();
                if (instances.isEmpty() && isEditMode()) {
                    execute4NoInstance();
                } else {
                    execute4Instance(instances);
                }
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        super.setInitialized(true);
    }

    /**
     * @param _instances list of instances the table is executed for
     * @throws EFapsException on error
     */
    private void execute4Instance(final List<Instance> _instances)
        throws EFapsException
    {
        final Set<String>altOIDSel = new HashSet<String>();

        // evaluate for all expressions in the table
        final MultiPrintQuery multi = new MultiPrintQuery(_instances);
        final List<Integer> userWidthList = getUserWidths();

        final List<Field> fields = getUserSortedColumns();
        int i = 0;
        Type type;
        if (_instances.size() > 0) {
            type = _instances.get(0).getType();
        } else {
            type = getTypeFromEvent();
        }

        for (final Field field : fields) {
            if (field.hasAccess(getMode(), getInstance(), getCommand())
                            && !field.isNoneDisplay(getMode()) && !field.isHiddenDisplay(getMode())) {
                Attribute attr = null;
                if (_instances.size() > 0) {
                    if (field.getSelect() != null) {
                        multi.addSelect(field.getSelect());
                    } else if (field.getAttribute() != null) {
                        multi.addAttribute(field.getAttribute());
                    } else if (field.getPhrase() != null) {
                        multi.addPhrase(field.getName(), field.getPhrase());
                    }
                    if (field.getSelectAlternateOID() != null) {
                        multi.addSelect(field.getSelectAlternateOID());
                        altOIDSel.add(field.getSelectAlternateOID());
                    }
                }
                if (field.getAttribute() != null && type != null) {
                    attr = type.getAttribute(field.getAttribute());
                }
                SortDirection sortdirection = SortDirection.NONE;
                if (field.getName().equals(getSortKey())) {
                    sortdirection = getSortDirection();
                }
                if (field.getFilter().getAttributes() != null) {
                    if (field.getFilter().getAttributes().contains(",")) {
                        if (field.getFilter().getAttributes().contains("/")) {
                            attr = Attribute.get(field.getFilter().getAttributes().split(",")[0]);
                        } else {
                            attr = type.getAttribute(field.getFilter().getAttributes().split(",")[0]);
                        }
                    } else {
                        if (field.getFilter().getAttributes().contains("/")) {
                            attr = Attribute.get(field.getFilter().getAttributes());
                        } else {
                            attr = type.getAttribute(field.getFilter().getAttributes());
                        }
                    }
                }
                final UITableHeader uiTableHeader = new UITableHeader(field, sortdirection, attr);
                if (this.filterTempCache.containsKey(uiTableHeader.getFieldName())) {
                    this.filters.put(uiTableHeader.getFieldName(),
                                    this.filterTempCache.get(uiTableHeader.getFieldName()));
                    uiTableHeader.setFilterApplied(true);
                } else if (uiTableHeader.getFilter().isRequired()) {
                    this.filters.put(uiTableHeader.getFieldName(), new TableFilter(uiTableHeader));
                }
                getHeaders().add(uiTableHeader);
                if (!field.isFixedWidth()) {
                    if (userWidthList != null && userWidthList.size() > i) {
                        if (isShowCheckBoxes() && userWidthList.size() > i + 1) {
                            uiTableHeader.setWidth(userWidthList.get(i + 1));
                        } else {
                            uiTableHeader.setWidth(userWidthList.get(i));
                        }
                    }
                    setWidthWeight(getWidthWeight() + field.getWidth());
                }
                i++;
            }
        }
        multi.execute();

        if (!altOIDSel.isEmpty()) {
            final List<Instance> inst = new ArrayList<Instance>();
            for (final String sel : altOIDSel) {
                inst.addAll(multi.getInstances4Select(sel));
            }
            checkAccessToInstances(inst);
        }
        executeRowResult(multi, fields);

        if (getSortKey() != null) {
            sort();
        }
    }

    /**
     * @param _multi Query
     * @param _fields Fields
     * @throws EFapsException on error
     */
    private void executeRowResult(final MultiPrintQuery _multi,
                                  final List<Field> _fields)
        throws EFapsException
    {
        boolean first = true;
        while (_multi.next()) {
            Instance instance = _multi.getCurrentInstance();
            final UIRow row = new UIRow(this, instance.getKey());

            String strValue = "";
            if (isEditMode() && first) {
                this.emptyRow = new UIRow(this);
            }
            for (final Field field : _fields) {
                if (field.getSelectAlternateOID() != null) {
                    instance = Instance.get(_multi.<String> getSelect(field.getSelectAlternateOID()));
                } else {
                    instance = _multi.getCurrentInstance();
                }
                if (field.hasAccess(getMode(), instance, getCommand()) && !field.isNoneDisplay(getMode())) {
                    Object value = null;
                    Attribute attr = null;
                    if (field.getSelect() != null) {
                        value = _multi.<Object> getSelect(field.getSelect());
                        attr = _multi.getAttribute4Select(field.getSelect());
                    } else if (field.getAttribute() != null) {
                        value = _multi.<Object> getAttribute(field.getAttribute());
                        attr = _multi.getAttribute4Attribute(field.getAttribute());
                    }  else if (field.getPhrase() != null) {
                        value = _multi.getPhrase(field.getName());
                    }
                    final FieldValue fieldvalue = new FieldValue(field, attr, value, instance, getInstance(),
                                    new ArrayList<Instance>(_multi.getInstanceList()), this);
                    String htmlTitle = null;
                    boolean hidden = false;
                    if (isPrintMode()) {
                        strValue = fieldvalue.getStringValue(getMode());
                    } else {
                        if ((isCreateMode() || isEditMode()) && field.isEditableDisplay(getMode())) {
                            strValue = fieldvalue.getEditHtml(getMode());
                            htmlTitle = fieldvalue.getStringValue(getMode());
                        } else if (field.isHiddenDisplay(getMode())) {
                            strValue = fieldvalue.getHiddenHtml(getMode());
                            hidden = true;
                        } else {
                            strValue = fieldvalue.getReadOnlyHtml(getMode());
                            htmlTitle = fieldvalue.getStringValue(getMode());
                        }
                    }

                    if (strValue == null) {
                        strValue = "";
                    }
                    String icon = field.getIcon();
                    if (field.isShowTypeIcon()) {
                        final Image image = Image.getTypeIcon(instance.getType());
                        if (image != null) {
                            icon = image.getUrl();
                        }
                    }
                    if (hidden) {
                        row.addHidden(new UIHiddenCell(this, fieldvalue, null, strValue));
                    } else {
                        row.add(new UITableCell(this, fieldvalue, instance, strValue, htmlTitle, icon));
                    }
                    // in case of edit mode an empty version of the first row
                    // isw stored, and can be used
                    // to create new rows
                    if (isEditMode() && first) {
                        final FieldValue fldVal = new FieldValue(field, attr);
                        final String cellvalue;
                        final String cellTitle;
                        if (field.isEditableDisplay(getMode())) {
                            cellvalue = fldVal.getEditHtml(getMode());
                            cellTitle = fldVal.getStringValue(getMode());
                        } else if (field.isHiddenDisplay(getMode())) {
                            cellvalue = fldVal.getHiddenHtml(getMode());
                            cellTitle = "";
                        } else {
                            cellvalue = fldVal.getReadOnlyHtml(getMode());
                            cellTitle = fldVal.getStringValue(getMode());
                        }

                        if (hidden) {
                            this.emptyRow.addHidden(new UIHiddenCell(this, fldVal, null, cellvalue));
                        } else {
                            this.emptyRow.add(new UITableCell(this, fldVal, null, cellvalue, cellTitle, icon));
                        }
                    }
                }
            }
            this.values.add(row);
            first = false;
        }
    }

    /**
     * Executes this model for the case that no instance is given. Currently
     * only create!
     *
     * @throws EFapsException on error
     */
    private void execute4NoInstance()
        throws EFapsException
    {
        final List<Field> fields = getUserSortedColumns();
        final List<Integer> userWidthList = getUserWidths();
        int i = 1;
        for (final Field field : fields) {
            if (field.hasAccess(getMode(), getInstance(), getCommand())
                            && !field.isNoneDisplay(getMode()) && !field.isHiddenDisplay(getMode())) {
                SortDirection sortdirection = SortDirection.NONE;
                if (field.getName().equals(getSortKey())) {
                    sortdirection = getSortDirection();
                }
                final UITableHeader headermodel = new UITableHeader(field, sortdirection, null);
                headermodel.setSortable(false);
                headermodel.setFilter(false);
                getHeaders().add(headermodel);
                if (!field.isFixedWidth()) {
                    if (userWidthList != null
                                    && ((isShowCheckBoxes() && (i + 1) < userWidthList.size())
                                                    || (!isShowCheckBoxes() && i < userWidthList.size()))) {
                        if (isShowCheckBoxes()) {
                            headermodel.setWidth(userWidthList.get(i + 1));
                        } else {
                            headermodel.setWidth(userWidthList.get(i));
                        }
                    }
                    setWidthWeight(getWidthWeight() + field.getWidth());
                }
                i++;
            }
        }
        final Type type = getTypeFromEvent();
        final UIRow row = new UIRow(this);
        Attribute attr = null;

        for (final Field field : fields) {
            if (field.hasAccess(getMode(), getInstance(), getCommand()) && !field.isNoneDisplay(getMode())) {
                attr = null;
                if (field.getAttribute() != null && type != null) {
                    attr = type.getAttribute(field.getAttribute());
                }
                final FieldValue fieldvalue = new FieldValue(field, attr, null, null, getInstance(), null, this);
                String htmlValue;
                String htmlTitle = null;
                boolean hidden = false;
                if ((isCreateMode() || isEditMode()) && field.isEditableDisplay(getMode())) {
                    htmlValue = fieldvalue.getEditHtml(getMode());
                    htmlTitle = fieldvalue.getStringValue(getMode());
                } else if (field.isHiddenDisplay(getMode())) {
                    htmlValue = fieldvalue.getHiddenHtml(getMode());
                    hidden = true;
                } else {
                    htmlValue = fieldvalue.getReadOnlyHtml(getMode());
                    htmlTitle = fieldvalue.getStringValue(getMode());
                }
                if (htmlValue == null) {
                    htmlValue = "";
                }
                if (hidden) {
                    row.addHidden(new UIHiddenCell(this, fieldvalue, null, htmlValue));
                } else {
                    final UITableCell cell = new UITableCell(this, fieldvalue, null, htmlValue, htmlTitle, null);
                    row.add(cell);
                }
            }
        }
        this.values.add(row);

        if (getSortKey() != null) {
            sort();
        }
    }



    /**
     * Method used to evaluate the type for this table from the connected
     * events.
     *
     * @return type if found
     * @throws EFapsException on error
     */
    private Type getTypeFromEvent()
        throws EFapsException
    {
        final List<EventDefinition> events = getEvents(EventType.UI_TABLE_EVALUATE);
        String typeName = null;
        if (events.size() > 1) {
            throw new EFapsException(this.getClass(), "execute4NoInstance.moreThanOneEvaluate");
        } else {
            final EventDefinition event = events.get(0);
            //TODO remove expand
            if (event.getProperty("Expand") != null) {
                final String tmp = event.getProperty("Expand");
                typeName = tmp.substring(0, tmp.indexOf("\\"));
            } else if (event.getProperty("Types") != null) {
                typeName = event.getProperty("Types").split(";")[0];
            }
        }
        return Type.get(typeName);
    }

    /**
     * Getter method for the instance variable {@link #emptyRow}.
     *
     * @return value of instance variable {@link #emptyRow}
     */
    public UIRow getEmptyRow()
    {
        return this.emptyRow;
    }

    /**
     * Add a filterlist to the filters of this UiTable.
     *
     * @param _uitableHeader UitableHeader this filter belongs to
     * @param _list lsi of value to filter
     *
     */
    public void addFilterList(final UITableHeader _uitableHeader,
                              final Set<?> _list)
    {
        final TableFilter filter = new TableFilter(_uitableHeader, _list);
        this.filters.put(_uitableHeader.getFieldName(), filter);
        final UITableHeader orig = getHeader4Id(_uitableHeader.getFieldId());
        if (orig != null) {
            orig.setFilterApplied(true);
        }
        storeFilters();
    }

    /**
     * Add a range to the filters of this UiTable.
     *
     * @param _uitableHeader UitableHeader this filter belongs to
     * @param _from from value
     * @param _to to value
     * @throws EFapsException on error
     *
     */
    public void addFilterRange(final UITableHeader _uitableHeader,
                               final String _from,
                               final String _to)
        throws EFapsException
    {
        final TableFilter filter = new TableFilter(_uitableHeader, _from, _to);
        this.filters.put(_uitableHeader.getFieldName(), filter);
        final UITableHeader orig = getHeader4Id(_uitableHeader.getFieldId());
        if (orig != null) {
            orig.setFilterApplied(true);
        }
        storeFilters();
    }

    /**
     * Add a classification based filters of this UiTable.
     *
     * @param _uitableHeader    UitableHeader this filter belongs to
     * @param _uiClassification classification based filters
     * @throws EFapsException on error
     *
     */
    public void addFilterClassifcation(final UITableHeader _uitableHeader,
                                       final UIClassification _uiClassification)
        throws EFapsException
    {
        final TableFilter filter = new TableFilter(_uitableHeader, _uiClassification);
        this.filters.put(_uitableHeader.getFieldName(), filter);
        final UITableHeader orig = getHeader4Id(_uitableHeader.getFieldId());
        if (orig != null) {
            orig.setFilterApplied(true);
        }
        storeFilters();
    }

    /**
     * Method to get a Filter from the list of filters belonging to this
     * UITable.
     *
     * @param _uitableHeader UitableHeader this filter belongs to
     * @return filter
     * @throws EFapsException on error
     */
    public TableFilter getFilter(final UITableHeader _uitableHeader)
        throws EFapsException
    {
        TableFilter ret = this.filters.get(_uitableHeader.getFieldName());
        if (ret != null && ret.getUiTableHeader() == null) {
            ret = new TableFilter(_uitableHeader);
            this.filters.put(_uitableHeader.getFieldName(), ret);
        }
        return ret;
    }

    /**
     * Get the List of values for a PICKERLIST.
     *
     * @param _uitableHeader UitableHeader this filter belongs to
     * @return List of Values
     */
    public List<String> getFilterPickList(final UITableHeader _uitableHeader)
    {
        final List<String> ret = new ArrayList<String>();
        for (final UIRow rowmodel : this.values) {
            final List<UITableCell> cells = rowmodel.getValues();
            for (final UITableCell cell : cells) {
                if (cell.getFieldId() == _uitableHeader.getFieldId()) {
                    final String value = cell.getCellTitle();
                    if (!ret.contains(value)) {
                        ret.add(value);
                    }
                    break;
                }
            }
        }
        Collections.sort(ret);
        return ret;
    }

    /**
     * Store the Filter in the Session.
     */
    private void storeFilters()
    {
        final Map<String, TableFilter> sessFilter = new HashMap<String, TableFilter>();
        for (final Entry<String, TableFilter> entry : this.filters.entrySet()) {
            sessFilter.put(entry.getKey(), entry.getValue());
        }
        try {
            Context.getThreadContext().setSessionAttribute(getCacheKey(UITable.UserCacheKey.FILTER), sessFilter);
        } catch (final EFapsException e) {
            UITable.LOG.error("Error storing Filtermap for Table called by Command with UUID: {}", getCommandUUID(), e);
        }
    }

    /**
     * This is the getter method for the instance variable {@link #values}.
     *
     * @return value of instance variable {@link #values}
     * @throws EFapsException
     * @see #values
     * @see #setValues
     */
    public List<UIRow> getValues()
    {
        List<UIRow> ret = new ArrayList<UIRow>();
        if (isFiltered()) {
            for (final UIRow row : this.values) {
                boolean filtered = false;
                for (final TableFilter filter : this.filters.values()) {
                    filtered = filter.filterRow(row);
                    if (filtered) {
                        break;
                    }
                }
                if (!filtered) {
                    ret.add(row);
                }
            }
        } else {
            ret = this.values;
        }
        setSize(ret.size());
        return ret;
    }

    /**
     * Are the values of the Rows filtered or not.
     *
     * @return true if filtered, else false
     */
    @Override
    public boolean isFiltered()
    {
        return !this.filters.isEmpty();
    }

    /**
     * Method to remove a filter from the filters.
     *
     * @param _uiTableHeader UITableHeader the filter is removed for
     */
    public void removeFilter(final UITableHeader _uiTableHeader)
    {
        this.filters.remove(_uiTableHeader.getFieldName());
        final UITableHeader orig = getHeader4Id(_uiTableHeader.getFieldId());
        if (orig != null) {
            orig.setFilterApplied(false);
        }
        storeFilters();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetModel()
    {
        super.setInitialized(false);
        this.values.clear();
        getHeaders().clear();
        getHiddenCells().clear();
    }

    /**
     * Method to get the events that are related to this UITable.
     *
     * @param _eventType eventype to get
     * @return List of events
     */
    protected List<EventDefinition> getEvents(final EventType _eventType)
    {
        return this.getCommand().getEvents(_eventType);
    }

    /**
     * The instance method sorts the table values depending on the sort key in
     * {@link #sortKey} and the sort direction in {@link #sortDirection}.
     * @throws EFapsException on error
     */
    @Override
    public void sort()
        throws EFapsException
    {
        if (getSortKey() != null && getSortKey().length() > 0) {
            int sortKeyTmp = 0;
            final List<Field> fields = getUserSortedColumns();
            for (int i = 0; i < fields.size(); i++) {
                final Field field = fields.get(i);
                if (field.hasAccess(getMode(), getInstance(), getCommand())
                                && !field.isNoneDisplay(getMode()) && !field.isHiddenDisplay(getMode())) {
                    if (field.getName().equals(getSortKey())) {
                        break;
                    }
                    sortKeyTmp++;
                }
            }
            if (sortKeyTmp < getTable().getFields().size()) {
                final int index = sortKeyTmp;
                Collections.sort(this.values, new Comparator<UIRow>()
                {

                    public int compare(final UIRow _rowModel1,
                                       final UIRow _rowModel2)
                    {

                        final UITableCell cellModel1 = _rowModel1.getValues().get(index);
                        final FieldValue fValue1 = new FieldValue(getTable().getFields().get(index), cellModel1
                                        .getUiClass(), cellModel1.getCompareValue() != null ? cellModel1
                                        .getCompareValue()
                                        : cellModel1.getCellValue());

                        final UITableCell cellModel2 = _rowModel2.getValues().get(index);
                        final FieldValue fValue2 = new FieldValue(getTable().getFields().get(index), cellModel2
                                        .getUiClass(), cellModel2.getCompareValue() != null ? cellModel2
                                        .getCompareValue()
                                        : cellModel2.getCellValue());

                        return fValue1.compareTo(fValue2);
                    }
                });
                if (getSortDirection() == SortDirection.DESCENDING) {
                    Collections.reverse(this.values);
                }
            }
        }
    }

    /**
     * Class represents one filter applied to this UITable.
     */
    public class TableFilter
        implements IClusterable
    {
        /**
         * Key to the value for "from" in the map for the esjp.
         */
        public static final String FROM = "from";

        /**
         * Key to the value for "to" in the map for the esjp.
         */
        public static final String TO = "to";

        /**
         * Key to the value for "to" in the map for the esjp.
         */
        public static final String LIST = "list";

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Set of value for the filter. Only used for filter using a PICKERLIST or CLASSIFICATION.
         */
        private Set<?> filterList;

        /**
         * Value of the from Field from the website.
         */
        private String from;

        /**
         * Value of the to Field from the website.
         */
        private String to;

        /**
         * DateTime value for {@link #from} in case that the filter is for a
         * date field.
         */
        private DateTime dateFrom;

        /**
         * DateTime value for {@link #to} in case that the filter is for a date
         * field.
         */
        private DateTime dateTo;

        /**
         * Id of the field this header belongs to.
         */
        private long headerFieldId;


        /**
         * Type of the Filter.
         */
        private final FilterType filterType;

        /**
         * Constructor is used for a database based filter in case that it is
         * required.
         */
        public TableFilter()
        {
            this.headerFieldId = 0;
            this.filterType = null;
        }

        /**
         * Constructor is used in case that a filter is required, during loading
         * the date first time for a memory base filter.
         *
         * @param _uitableHeader UITableHeader this filter lies in
         * @throws EFapsException on error
         */
        public TableFilter(final UITableHeader _uitableHeader)
            throws EFapsException
        {
            this.headerFieldId = _uitableHeader.getFieldId();
            this.filterType =  _uitableHeader.getFilterType();
            if (_uitableHeader.getFilter().getDefaultValue() != null) {
                if (_uitableHeader.getFilterType().equals(FilterType.DATE)) {
                    final String filter = _uitableHeader.getFilter().getDefaultValue();
                    final String[] parts = filter.split(":");
                    final String range = parts[0];
                    final int sub = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                    if ("today".equalsIgnoreCase(range)) {
                        final DateMidnight tmp = DateTimeUtil.translateFromUI(new DateTime()).toDateMidnight();
                        this.dateFrom = tmp.toDateTime().minusDays(sub);
                        this.dateTo = this.dateFrom.plusDays(1).plusSeconds(1);
                    } else if ("week".equalsIgnoreCase(range)) {
                        DateMidnight tmp = DateTimeUtil.translateFromUI(new DateTime()).toDateMidnight();
                        tmp = tmp.minusDays(tmp.getDayOfWeek() - 1);
                        this.dateFrom = tmp.toDateTime().minusWeeks(sub);
                        this.dateTo = tmp.toDateTime().plusWeeks(1);
                    } else if ("month".equalsIgnoreCase(range)) {
                        DateMidnight tmp = DateTimeUtil.translateFromUI(new DateTime()).toDateMidnight();
                        tmp = tmp.minusDays(tmp.getDayOfMonth() - 1);
                        this.dateFrom = tmp.toDateTime().minusMonths(sub);
                        this.dateTo = tmp.toDateTime().plusMonths(1);
                    } else if ("year".equalsIgnoreCase(range)) {
                        DateMidnight tmp = DateTimeUtil.translateFromUI(new DateTime()).toDateMidnight();
                        tmp = tmp.minusDays(tmp.getDayOfYear() - 1);
                        this.dateFrom = tmp.toDateTime().minusYears(sub);
                        this.dateTo = tmp.toDateTime().plusYears(1);
                    }
                }
            }
        }

        /**
         * Standard Constructor for a Filter containing a range.
         *
         * @param _uitableHeader UITableHeader this filter lies in
         * @param _from value for from
         * @param _to value for to
         * @throws EFapsException on error
         */
        public TableFilter(final UITableHeader _uitableHeader,
                           final String _from,
                           final String _to)
            throws EFapsException
        {
            this.headerFieldId = _uitableHeader.getFieldId();
            this.filterType = _uitableHeader.getFilterType();
            this.from = _from;
            this.to = _to;
            if (_uitableHeader.getFilterType().equals(FilterType.DATE)) {
                this.dateFrom = DateTimeUtil.translateFromUI(_from);
                this.dateTo = DateTimeUtil.translateFromUI(_to);
                this.dateTo = this.dateTo == null ? null : this.dateTo.plusDays(1);
            }
        }

        /**
         * Standard Constructor for a Filter using a PICKLIST.
         *
         * @param _uitableHeader UITableHeader this filter lies in
         * @param _filterList set of values for the filter
         */
        public TableFilter(final UITableHeader _uitableHeader,
                           final Set<?> _filterList)
        {
            this.headerFieldId = _uitableHeader.getFieldId();
            this.filterType = _uitableHeader.getFilterType();
            this.filterList = _filterList;
        }

        /**
         * Standard Constructor for a Filter using a CLASSICIATION.
         *
         * @param _uitableHeader UITableHeader this filter lies in
         * @param _uiClassification set of values for the filter
         */
        public TableFilter(final UITableHeader _uitableHeader,
                           final UIClassification _uiClassification)
        {
            this.headerFieldId = _uitableHeader.getFieldId();
            this.filterType = _uitableHeader.getFilterType();
            final Set<UUID> list = new HashSet<UUID>();
            if (_uiClassification.isSelected()) {
                list.add(_uiClassification.getClassificationUUID());
            }
            for (final UIClassification uiClass : _uiClassification.getDescendants()) {
                if (uiClass.isSelected()) {
                    list.add(uiClass.getClassificationUUID());
                }
            }
            this.filterList = list;
        }

        /**
         * Getter method for instance variable {@link #uiTableHeader}.
         *
         * @return value of instance variable {@link #uiTableHeader}
         */
        public UITableHeader getUiTableHeader()
        {
            return getHeader4Id(this.headerFieldId);
        }

        /**
         * Method to get the map that must be passed for this filter to the
         * esjp.
         *
         * @return Map
         */
        public Map<String, Object> getMap4esjp()
        {
            final Map<String, Object> ret = new HashMap<String, Object>();
            if (this.filterList == null) {
                ret.put(UITable.TableFilter.FROM, this.from);
                ret.put(UITable.TableFilter.TO, this.to);
            } else {
                ret.put(UITable.TableFilter.LIST, this.filterList);
            }
            return ret;
        }

        /**
         * Method is used for memory based filters to filter one row.
         *
         * @param _uiRow UIRow to filter
         * @return false if the row must be shown to the user, true if the row
         *         must be filtered
         */
        public boolean filterRow(final UIRow _uiRow)
        {
            boolean ret = false;
            if (this.headerFieldId > 0
                            && Field.get(this.headerFieldId).getFilter().getBase().equals(Filter.Base.MEMORY)) {
                final List<UITableCell> cells = _uiRow.getValues();
                for (final UITableCell cell : cells) {
                    if (cell.getFieldId() == this.headerFieldId) {
                        if (this.filterList != null) {
                            final String value = cell.getCellTitle();
                            if (!this.filterList.contains(value)) {
                                ret = true;
                            }
                        } else if (this.filterType.equals(FilterType.DATE)) {
                            if (this.dateFrom == null || this.dateTo == null) {
                                ret = true;
                            } else {
                                final Interval interval = new Interval(this.dateFrom, this.dateTo);
                                final DateTime value = (DateTime) cell.getCompareValue();
                                if (!(interval.contains(value) || value.isEqual(this.dateFrom)
                                                || value.isEqual(this.dateTo))) {
                                    ret = true;
                                }
                            }
                        }
                        break;
                    }
                }
            }
            return ret;
        }

        /**
         * Getter method for instance variable {@link #dateFrom}.
         *
         * @return value of instance variable {@link #dateFrom}
         */
        public DateTime getDateFrom()
        {
            return this.dateFrom;
        }

        /**
         * Getter method for instance variable {@link #dateTo}.
         *
         * @return value of instance variable {@link #dateTo}
         */
        public DateTime getDateTo()
        {
            return this.dateTo;
        }

        /**
         * Setter method for instance variable {@link #headerFieldId}.
         *
         * @param _headerFieldId value for instance variable {@link #headerFieldId}
         */

        protected void setHeaderFieldId(final long _headerFieldId)
        {
            this.headerFieldId = _headerFieldId;
        }
    }
}
