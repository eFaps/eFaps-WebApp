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

package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.wicket.IClusterable;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.Table;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
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
    extends UIAbstractPageObject
{

    /**
     * This enum holds the Values used as part of the key for the UserAttributes
     * witch belong to a TableModel.
     */
    public static enum UserAttributeKey {
        /**
         * Key used for the order of Columns.
         */
        COLUMNORDER("columnOrder"),
                /**
         * Key used for the widths of Columns.
         */
        COLUMNWIDTH("columnWidths"),
                /**
         * Key used for the sort direction.
         */
        SORTDIRECTION("sortDirection"),
                /**
         * Key used for the Column.
         */
        SORTKEY("sortKey");

        /**
         * Value of the user attribute.
         */
        private final String value;

        /**
         * Constructor setting the instance variable.
         *
         * @param _value Value
         */
        private UserAttributeKey(final String _value)
        {
            this.value = _value;
        }

        /**
         * @return the value
         */
        public String getValue()
        {
            return this.value;
        }
    }

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
    private final Map<UITableHeader, Filter> filters = new HashMap<UITableHeader, Filter>();

    /**
     * Map is used to store the filters in relation to a field name. It is used
     * temporarily when the table is (due to a database based filter) requeried,
     * to be able to set the filters to the headers by copying it from the old
     * header to the new one.
     *
     * @see #getInstanceListsOld()
     * @see #execute4InstanceOld()
     */
    private final Map<String, Filter> filterTempCache = new HashMap<String, Filter>();
    /**
     * The instance Array holds the Label for the Columns.
     */
    private final List<UITableHeader> headers = new ArrayList<UITableHeader>();

    /**
     * This instance variable sores if the Table should show CheckBodes.
     */
    private boolean showCheckBoxes;

    /**
     * The instance variable stores the string of the sort direction.
     *
     * @see #getSortDirection
     * @see #setSortDirection
     */
    private SortDirection sortDirection = SortDirection.NONE;

    /**
     * The instance variable stores the string of the sort key.
     *
     * @see #getSortKey
     * @see #setSortKey
     */
    private String sortKey = null;

    /**
     * This instance variable stores the Id of the table. This int is used to
     * distinguish tables in case that there are more than one table on one
     * page.
     */
    private int tableId = 1;

    /**
     * The instance variable stores the UUID for the table which must be shown.
     *
     * @see #getTable
     */
    private UUID tableUUID;

    /**
     * This instance variable stores if the Widths of the Columns are set by
     * UserAttributes.
     */
    private boolean userWidths = false;

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
     * This instance variable stores the total weight of the widths of the
     * Cells. (Sum of all widths)
     */
    private int widthWeight;

    /**
     * Constructor setting the parameters.
     *
     * @param _parameters PageParameters
     * @throws EFapsException on error
     */
    public UITable(final PageParameters _parameters)
        throws EFapsException
    {
        super(_parameters);
        initialise();
    }

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
            this.showCheckBoxes = false;
        } else {
            // set target table
            if (command.getTargetTable() != null) {
                this.tableUUID = command.getTargetTable().getUUID();
                // add the filter here, if it is a required filter that must be
                // applied against the database
                for (final Field field : command.getTargetTable().getFields()) {
                    if (field.isFilterRequired() && !field.isFilterMemoryBased()) {
                        this.filters.put(new UITableHeader(field, SortDirection.NONE, null), new Filter());
                    }
                }
            }
            // set default sort
            if (command.getTargetTableSortKey() != null) {
                this.sortKey = command.getTargetTableSortKey();
                this.sortDirection = command.getTargetTableSortDirection();
            }

            this.showCheckBoxes = command.isTargetShowCheckBoxes();
            // get the User specific Attributes if exist overwrite the defaults
            try {
                if (Context.getThreadContext().containsUserAttribute(
                                getUserAttributeKey(UITable.UserAttributeKey.SORTKEY))) {
                    this.sortKey = Context.getThreadContext().getUserAttribute(
                                    getUserAttributeKey(UITable.UserAttributeKey.SORTKEY));
                }
                if (Context.getThreadContext().containsUserAttribute(
                                getUserAttributeKey(UITable.UserAttributeKey.SORTDIRECTION))) {
                    this.sortDirection = SortDirection.getEnum((Context.getThreadContext()
                                    .getUserAttribute(getUserAttributeKey(UITable.UserAttributeKey.SORTDIRECTION))));
                }
            } catch (final EFapsException e) {
                // we don't throw an error because this are only Usersettings
                UITable.LOG.error("error during the retrieve of UserAttributes", e);
            }
        }
    }

    /**
     * Method to get the List of Instances for the table.
     *
     * @return List with List of instances
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    protected List<List<Instance>> getInstanceListsOld()
        throws EFapsException
    {
        // get the filters that must be applied against the database
        final Map<String, Map<String, String>> dataBasefilters = new HashMap<String, Map<String, String>>();
        final Iterator<Entry<UITableHeader, Filter>> iter = this.filters.entrySet().iterator();
        this.filterTempCache.clear();
        while (iter.hasNext()) {
            final Entry<UITableHeader, Filter> entry = iter.next();
            if (!entry.getKey().isFilterMemoryBased()) {
                final Map<String, String> map = entry.getValue().getMap4esjp();
                dataBasefilters.put(entry.getKey().getFieldName(), map);
            }
            this.filterTempCache.put(entry.getKey().getFieldName(), entry.getValue());
            iter.remove();
        }

        final List<Return> ret = getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
                        ParameterValues.INSTANCE, getInstance(),
                        ParameterValues.OTHERS, dataBasefilters);
        final List<List<Instance>> lists = (List<List<Instance>>) ret.get(0).get(ReturnValues.VALUES);
        return lists;
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
        final Map<String, Map<String, String>> dataBasefilters = new HashMap<String, Map<String, String>>();
        final Iterator<Entry<UITableHeader, Filter>> iter = this.filters.entrySet().iterator();
        this.filterTempCache.clear();
        while (iter.hasNext()) {
            final Entry<UITableHeader, Filter> entry = iter.next();
            if (!entry.getKey().isFilterMemoryBased()) {
                final Map<String, String> map = entry.getValue().getMap4esjp();
                dataBasefilters.put(entry.getKey().getFieldName(), map);
            }
            this.filterTempCache.put(entry.getKey().getFieldName(), entry.getValue());
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
     * TODO to be removed. Temporary method to decide which method of selct must
     * be used
     *
     * @return true if the new way
     */
    private boolean isNewWay()
    {
        boolean ret = false;
        final Table form = Table.get(this.tableUUID);
        for (final Field field : form.getFields()) {
            if (field.getAttribute() != null || field.getSelect() != null || field.getPhrase() != null
                            || field.getSelectAlternateOID() != null) {
                ret = true;
                break;
            }
        }
        return ret;
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
                if (isNewWay()) {
                    execute4Instance();
                } else {
                    UITable.LOG.error("invalid execute of instances for a table: " + getCallingCommand().toString());
                    execute4InstanceOld();
                }
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        super.setInitialized(true);
    }

    /**
     * @throws EFapsException on error
     */
    private void execute4Instance()
        throws EFapsException
    {
        // first get list of object ids
        final List<Instance> instances = getInstanceList();

        // evaluate for all expressions in the table
        final MultiPrintQuery query = new MultiPrintQuery(instances);
        final List<Integer> userWidthList = getUserWidths();

        final List<Field> fields = getUserSortedColumns();
        int i = 0;
        Type type;
        if (instances.size() > 0) {
            type = instances.get(0).getType();
        } else {
            type = getTypeFromEvent();
        }
        for (final Field field : fields) {
            if (field.hasAccess(getMode(), getInstance())
                            && !field.isNoneDisplay(getMode()) && !field.isHiddenDisplay(getMode())) {
                Attribute attr = null;
                if (instances.size() > 0) {
                    if (field.getSelect() != null) {
                        query.addSelect(field.getSelect());
                    } else if (field.getAttribute() != null) {
                        query.addAttribute(field.getAttribute());
                    } else if (field.getPhrase() != null) {
                        query.addPhrase(field.getName(), field.getPhrase());
                    } else if (field.getExpression() != null) {
                        query.addExpression(field.getName(), field.getExpression());
                    }
                    if (field.getSelectAlternateOID() != null) {
                        query.addSelect(field.getSelectAlternateOID());
                    }
                }
                if (field.getAttribute() != null && type != null) {
                    attr = type.getAttribute(field.getAttribute());
                }
                SortDirection sortdirection = SortDirection.NONE;
                if (field.getName().equals(this.sortKey)) {
                    sortdirection = getSortDirection();
                }
                if (field.getFilterAttributes() != null) {
                    if (field.getFilterAttributes().contains(",")) {
                        if (field.getFilterAttributes().contains("/")) {
                            attr = Attribute.get(field.getFilterAttributes().split(",")[0]);
                        } else {
                            attr = type.getAttribute(field.getFilterAttributes().split(",")[0]);
                        }
                    } else {
                        if (field.getFilterAttributes().contains("/")) {
                            attr = Attribute.get(field.getFilterAttributes());
                        } else {
                            attr = type.getAttribute(field.getFilterAttributes());
                        }
                    }
                }
                final UITableHeader uiTableHeader = new UITableHeader(field, sortdirection, attr);
                if (this.filterTempCache.containsKey(uiTableHeader.getFieldName())
                                && this.filterTempCache.get(uiTableHeader.getFieldName()).getUiTableHeader() != null) {
                    this.filters.put(uiTableHeader, this.filterTempCache.get(uiTableHeader.getFieldName()));
                    uiTableHeader.setFilterApplied(true);
                } else if (uiTableHeader.isFilterRequired()) {
                    this.filters.put(uiTableHeader, new Filter(uiTableHeader));
                }
                this.headers.add(uiTableHeader);
                if (!field.isFixedWidth()) {
                    if (userWidthList != null && userWidthList.size() > i) {
                        if (isShowCheckBoxes()) {
                            uiTableHeader.setWidth(userWidthList.get(i + 1));
                        } else {
                            uiTableHeader.setWidth(userWidthList.get(i));
                        }
                    }
                    this.widthWeight += field.getWidth();
                }
                i++;
            }
        }
        query.execute();

        executeRowResult(query, fields);

        if (this.sortKey != null) {
            sort();
        }
    }

    /**
     * @param _query Query
     * @param _fields Fields
     * @throws EFapsException on error
     */
    private void executeRowResult(final MultiPrintQuery _query,
                                  final List<Field> _fields)
        throws EFapsException
    {
        boolean first = true;
        while (_query.next()) {
            Instance instance = _query.getCurrentInstance();
            final UIRow row = new UIRow(this, instance.getKey());

            String strValue = "";
            if (isEditMode() && first) {
                this.emptyRow = new UIRow(this);
            }
            for (final Field field : _fields) {
                if (field.getSelectAlternateOID() != null) {
                    instance = Instance.get(_query.<String> getSelect(field.getSelectAlternateOID()));
                } else {
                    instance = _query.getCurrentInstance();
                }
                if (field.hasAccess(getMode(), instance) && !field.isNoneDisplay(getMode())) {
                    Object value = null;
                    Attribute attr = null;
                    if (field.getSelect() != null) {
                        value = _query.<Object> getSelect(field.getSelect());
                        attr = _query.getAttribute4Select(field.getSelect());
                    } else if (field.getAttribute() != null) {
                        value = _query.<Object> getAttribute(field.getAttribute());
                        attr = _query.getAttribute4Attribute(field.getAttribute());
                    }  else if (field.getPhrase() != null) {
                        value = _query.getPhrase(field.getName());
                    }

                    final FieldValue fieldvalue = new FieldValue(field, attr, value, instance, getInstance());
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
                        final FieldValue fldVal = new FieldValue(field, attr, null, null, null);
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
            if (field.hasAccess(getMode(), getInstance())
                            && !field.isNoneDisplay(getMode()) && !field.isHiddenDisplay(getMode())) {
                SortDirection sortdirection = SortDirection.NONE;
                if (field.getName().equals(this.sortKey)) {
                    sortdirection = getSortDirection();
                }
                final UITableHeader headermodel = new UITableHeader(field, sortdirection, null);
                headermodel.setSortable(false);
                headermodel.setFilter(false);
                this.headers.add(headermodel);
                if (!field.isFixedWidth()) {
                    if (userWidthList != null) {
                        if (isShowCheckBoxes()) {
                            headermodel.setWidth(userWidthList.get(i + 1));
                        } else {
                            headermodel.setWidth(userWidthList.get(i));
                        }
                    }
                    this.widthWeight += field.getWidth();
                }
                i++;
            }
        }
        final Type type = getTypeFromEvent();
        final UIRow row = new UIRow(this);
        Attribute attr = null;

        for (final Field field : fields) {
            if (field.hasAccess(getMode(), getInstance()) && !field.isNoneDisplay(getMode())) {
                attr = null;
                // TODO to be removed!!
                if (field.getExpression() != null) {
                    attr = type.getAttribute(field.getExpression());
                }
                if (field.getAttribute() != null) {
                    attr = type.getAttribute(field.getAttribute());
                }
                final FieldValue fieldvalue = new FieldValue(field, attr, null, null, getInstance());
                String htmlValue;
                String htmlTitle = null;
                boolean hidden = false;
                if (isCreateMode() && field.isEditableDisplay(getMode())) {
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

        if (this.sortKey != null) {
            sort();
        }
    }

    /**
     * This method executes the TableModel, that means this method has to be
     * called so that this model contains actual data from the eFaps-DataBase.
     * The method works in conjunction with
     * {@link #executeRowResult(Map, ListQuery)}.
     *
     * @throws EFapsException on error
     */
    private void execute4InstanceOld()
        throws EFapsException
    {
        // first get list of object ids
        final List<List<Instance>> lists = getInstanceListsOld();

        final List<Instance> instances = new ArrayList<Instance>();
        final Map<Instance, List<Instance>> instMapper = new HashMap<Instance, List<Instance>>();
        for (final List<Instance> oneList : lists) {
            final Instance inst = oneList.get(oneList.size() - 1);
            instances.add(inst);
            instMapper.put(inst, oneList);
        }

        // evaluate for all expressions in the table
        final ListQuery query = new ListQuery(instances);
        final List<Integer> userWidthList = getUserWidths();

        final List<Field> fields = getUserSortedColumns();
        int i = 0;
        Type type;
        if (instances.size() > 0) {
            type = instances.get(0).getType();
        } else {
            type = getTypeFromEvent();
        }
        for (final Field field : fields) {
            if (field.hasAccess(getMode(), getInstance()) && !field.isNoneDisplay(getMode())
                            && !field.isHiddenDisplay(getMode())) {
                Attribute attr = null;
                if (field.getExpression() != null) {
                    query.addSelect(field.getExpression());
                    if (type != null) {
                        attr = type.getAttribute(field.getExpression());
                    }
                }
                if (field.getAlternateOID() != null) {
                    query.addSelect(field.getAlternateOID());
                }
                SortDirection sortdirection = SortDirection.NONE;
                if (field.getName().equals(this.sortKey)) {
                    sortdirection = getSortDirection();
                }

                final UITableHeader uiTableHeader = new UITableHeader(field, sortdirection, attr);
                if (this.filterTempCache.containsKey(uiTableHeader.getFieldName())
                                && this.filterTempCache.get(uiTableHeader.getFieldName()).getUiTableHeader() != null) {
                    this.filters.put(uiTableHeader, this.filterTempCache.get(uiTableHeader.getFieldName()));
                    uiTableHeader.setFilterApplied(true);
                } else if (uiTableHeader.isFilterRequired()) {
                    this.filters.put(uiTableHeader, new Filter(uiTableHeader));
                }
                this.headers.add(uiTableHeader);
                if (!field.isFixedWidth()) {
                    if (userWidthList != null) {
                        if (isShowCheckBoxes()) {
                            uiTableHeader.setWidth(userWidthList.get(i + 1));
                        } else {
                            uiTableHeader.setWidth(userWidthList.get(i));
                        }
                    }
                    this.widthWeight += field.getWidth();
                }
                i++;
            }
        }
        query.execute();

        executeRowResultOld(instMapper, query, fields);

        if (this.sortKey != null) {
            sort();
        }
    }

    /**
     * This method works together with {@link #execute4InstanceOld()} to fill
     * this Model with Data.
     *
     * @param _instMapper Map of instances
     * @param _query Query with results
     * @param _fields List of the Fields
     * @throws EFapsException on error
     */
    private void executeRowResultOld(final Map<Instance, List<Instance>> _instMapper,
                                     final ListQuery _query,
                                     final List<Field> _fields)
        throws EFapsException
    {

        while (_query.next()) {
            // get all found oids (typically more than one if it is an expand)
            Instance instance = _query.getInstance();
            final StringBuilder instanceKeys = new StringBuilder();
            boolean first = true;
            if (_instMapper.get(instance) != null) {
                final List<Instance> list = _instMapper.get(instance);
                final Instance inst = list.get(list.size() - 1);
                if (!instance.getKey().equals(inst.getKey())) {
                    instance = inst;
                }
                for (final Instance oneInstance : list) {
                    if (first) {
                        first = false;
                    } else {
                        instanceKeys.append("|");
                    }
                    instanceKeys.append(oneInstance.getKey());
                }
            }
            final UIRow row = new UIRow(this, instance.getKey());

            String strValue = "";
            for (final Field field : _fields) {
                if (field.hasAccess(getMode(), getInstance()) && !field.isNoneDisplay(getMode())) {
                    Object value = null;
                    Attribute attr = null;
                    if (field.getExpression() != null) {
                        value = _query.get(field.getExpression());
                        attr = _query.getAttribute(field.getExpression());
                    }

                    if (field.getAlternateOID() != null) {
                        instance = Instance.get((String) _query.get(field.getAlternateOID()));
                    }

                    final FieldValue fieldvalue = new FieldValue(field, attr, value, instance, getInstance());
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
                }
            }
            this.values.add(row);
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
     */
    public void addFilterList(final UITableHeader _uitableHeader,
                              final Set<?> _list)
    {
        final Filter filter = new Filter(_uitableHeader, _list);
        this.filters.put(_uitableHeader, filter);
        _uitableHeader.setFilterApplied(true);
    }

    /**
     * Add a range to the filters of this UiTable.
     *
     * @param _uitableHeader UitableHeader this filter belongs to
     * @param _from from value
     * @param _to to value
     */
    public void addFilterRange(final UITableHeader _uitableHeader,
                               final String _from,
                               final String _to)
    {
        final Filter filter = new Filter(_uitableHeader, _from, _to);
        this.filters.put(_uitableHeader, filter);
        _uitableHeader.setFilterApplied(true);
    }

    /**
     * Method to get a Filter from the list of filters belonging to this
     * UITable.
     *
     * @param _uitableHeader UitableHeader this filter belongs to
     * @return filter
     */
    public Filter getFilter(final UITableHeader _uitableHeader)
    {
        return this.filters.get(_uitableHeader);
    }

    /**
     * Get the List of values for a PICKERLIST.
     *
     * @param _uitableHeader UitableHeader this filter belongs to
     * @return List of Values
     */
    public List<?> getFilterPickList(final UITableHeader _uitableHeader)
    {
        final List<String> ret = new ArrayList<String>();
        for (final UIRow rowmodel : this.values) {
            final List<UITableCell> cells = rowmodel.getValues();
            for (final UITableCell cell : cells) {
                if (cell.getFieldId() == _uitableHeader.getFieldId()) {
                    final String value = cell.getCellValue();
                    if (!ret.contains(value)) {
                        ret.add(value);
                    }
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * This is the getter method for the instance variable {@link #headers}.
     *
     * @return value of instance variable {@link #headers}
     */
    public List<UITableHeader> getHeaders()
    {
        return this.headers;
    }

    /**
     * This is the getter method for the instance variable
     * {@link #sortDirection}.
     *
     * @return value of instance variable {@link #sortDirection}
     * @see #sortDirection
     * @see #setSortDirection
     */
    public SortDirection getSortDirection()
    {
        return this.sortDirection;
    }

    /**
     * Method to set he sort direction.
     *
     * @param _sortdirection sort direction to set
     */
    public void setSortDirection(final SortDirection _sortdirection)
    {
        this.sortDirection = _sortdirection;
        try {
            Context.getThreadContext().setUserAttribute(getUserAttributeKey(UITable.UserAttributeKey.SORTDIRECTION),
                            _sortdirection.getValue());
        } catch (final EFapsException e) {
            // we don't throw an error because this are only Usersettings
            UITable.LOG.error("error during the retrieve of UserAttributes", e);
        }
    }

    /**
     * This is the getter method for the instance variable {@link #sortKey}.
     *
     * @return value of instance variable {@link #sortKey}
     * @see #sortKey
     * @see #setSortKey
     */
    public String getSortKey()
    {
        return this.sortKey;
    }

    /**
     * This is the setter method for the instance variable {@link #sortKey}.
     *
     * @param _sortKey new value for instance variable {@link #sortKey}
     * @see #sortKey
     * @see #getSortKey
     */
    public void setSortKey(final String _sortKey)
    {
        this.sortKey = _sortKey;
        try {
            Context.getThreadContext().setUserAttribute(getUserAttributeKey(UITable.UserAttributeKey.SORTKEY),
                            _sortKey);
        } catch (final EFapsException e) {
            // we don't throw an error because this are only Usersettings
            UITable.LOG.error("error during the retrieve of UserAttributes", e);
        }

    }

    /**
     * This is the getter method for the instance variable {@link #table}.
     *
     * @return value of instance variable {@link #table}
     * @see #table
     */
    public Table getTable()
    {
        return Table.get(this.tableUUID);
    }

    /**
     * This is the getter method for the instance variable {@link #tableId}.
     *
     * @return value of instance variable {@link #tableId}
     */
    public int getTableId()
    {
        return this.tableId * 100;
    }

    /**
     * This is the setter method for the instance variable {@link #tableId}.
     *
     * @param _tableId the tableId to set
     */
    public void setTableId(final int _tableId)
    {
        this.tableId = _tableId;
    }

    /**
     * This method generates the Key for a UserAttribute by using the UUID of
     * the Command and the given UserAttributeKey, so that for every Table a
     * unique key for sorting etc, is created.
     *
     * @param _key UserAttributeKey the Key is wanted
     * @return String with the key
     */
    public String getUserAttributeKey(final UserAttributeKey _key)
    {
        return super.getCommandUUID() + "-" + _key.getValue();
    }

    /**
     * This method looks if for this TableModel a UserAttribute for the sorting
     * of the Columns exist. If they exist the Fields will be sorted as defined
     * by the User. If no definition of the User exist the Original default
     * sorting of the columns will be used. In the Case that the Definition of
     * the Table was altered Field which are not sorted yet will be sorted in at
     * the last position.
     *
     * @return List of fields
     */
    private List<Field> getUserSortedColumns()
    {
        final List<Field> fields = getTable().getFields();
        List<Field> ret = new ArrayList<Field>();
        try {
            if (Context.getThreadContext().containsUserAttribute(
                            getUserAttributeKey(UITable.UserAttributeKey.COLUMNORDER))) {

                final String columnOrder = Context.getThreadContext().getUserAttribute(
                                getUserAttributeKey(UITable.UserAttributeKey.COLUMNORDER));

                final StringTokenizer tokens = new StringTokenizer(columnOrder, ";");
                while (tokens.hasMoreTokens()) {
                    final String fieldname = tokens.nextToken();
                    for (int i = 0; i < fields.size(); i++) {
                        if (fieldname.equals(fields.get(i).getName())) {
                            ret.add(fields.get(i));
                            fields.remove(i);
                        }
                    }
                }
                if (!fields.isEmpty()) {
                    for (final Field field : fields) {
                        ret.add(field);
                    }
                }
            } else {
                ret = fields;
            }
        } catch (final EFapsException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * This method retieves the UserAttribute for the ColumnWidths and evaluates
     * the string.
     *
     * @return List with the values of the columns in Pixel
     */
    private List<Integer> getUserWidths()
    {
        List<Integer> ret = null;
        try {
            if (Context.getThreadContext().containsUserAttribute(
                            getUserAttributeKey(UITable.UserAttributeKey.COLUMNWIDTH))) {
                this.userWidths = true;
                final String widths = Context.getThreadContext().getUserAttribute(
                                getUserAttributeKey(UITable.UserAttributeKey.COLUMNWIDTH));

                final StringTokenizer tokens = new StringTokenizer(widths, ";");

                ret = new ArrayList<Integer>();

                while (tokens.hasMoreTokens()) {
                    final String token = tokens.nextToken();
                    for (int i = 0; i < token.length(); i++) {
                        if (!Character.isDigit(token.charAt(i))) {
                            final int width = Integer.parseInt(token.substring(0, i));
                            ret.add(width);
                            break;
                        }
                    }
                }
            }
        } catch (final NumberFormatException e) {
            // we don't throw an error because this are only Usersettings
            UITable.LOG.error("error during the retrieve of UserAttributes in getUserWidths()", e);
        } catch (final EFapsException e) {
            // we don't throw an error because this are only Usersettings
            UITable.LOG.error("error during the retrieve of UserAttributes in getUserWidths()", e);
        }
        return ret;
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
                for (final Filter filter : this.filters.values()) {
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
        return ret;
    }

    /**
     * This is the getter method for the instance variable {@link #widthWeight}.
     *
     * @return value of instance variable {@link #widthWeight}
     */
    public int getWidthWeight()
    {
        return this.widthWeight;
    }

    /**
     * Are the values of the Rows filtered or not.
     *
     * @return true if filtered, else false
     */
    public boolean isFiltered()
    {
        return !this.filters.isEmpty();
    }

    /**
     * @return <i>true</i> if the check boxes must be shown, other <i>false</i>
     *         is returned.
     * @see #showCheckBoxes
     */
    public boolean isShowCheckBoxes()
    {
        boolean ret;
        if (super.isSubmit() && !isCreateMode()) {
            ret = true;
        } else {
            ret = this.showCheckBoxes;
        }
        return ret;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #showCheckBoxes}.
     *
     * @param _showCheckBoxes the showCheckBoxes to set
     */
    public void setShowCheckBoxes(final boolean _showCheckBoxes)
    {
        this.showCheckBoxes = _showCheckBoxes;
    }

    /**
     * This is the getter method for the instance variable {@link #userWidths}.
     *
     * @return value of instance variable {@link #userWidths}
     */
    public boolean isUserSetWidth()
    {
        return this.userWidths;
    }

    /**
     * Method to remove a filter from the filters.
     *
     * @param _uiTableHeader UITableHeader the filter is removed for
     */
    public void removeFilter(final UITableHeader _uiTableHeader)
    {
        this.filters.remove(_uiTableHeader);
        _uiTableHeader.setFilterApplied(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetModel()
    {
        super.setInitialized(false);
        this.values.clear();
        this.headers.clear();
    }

    /**
     * Method to set the order of the columns.
     *
     * @param _markupsIds ids of the columns as a string with ; separated
     */
    public void setColumnOrder(final String _markupsIds)
    {
        final StringTokenizer tokens = new StringTokenizer(_markupsIds, ";");
        final StringBuilder columnOrder = new StringBuilder();
        while (tokens.hasMoreTokens()) {
            final String markupId = tokens.nextToken();
            for (final UITableHeader header : this.headers) {
                if (markupId.equals(header.getMarkupId())) {
                    columnOrder.append(header.getFieldName()).append(";");
                    break;
                }
            }
        }
        try {
            Context.getThreadContext().setUserAttribute(getUserAttributeKey(UITable.UserAttributeKey.COLUMNORDER),
                            columnOrder.toString());
        } catch (final EFapsException e) {
            // we don't throw an error because this are only Usersettings
            UITable.LOG.error("error during the setting of UserAttributes", e);
        }
    }

    /**
     * This is the setter method for the instance variable {@link #tableUUID}.
     *
     * @param _tableUUID the tableUUID to set
     */
    protected void setTableUUID(final UUID _tableUUID)
    {
        this.tableUUID = _tableUUID;
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
     */
    public void sort()
    {
        if (getSortKey() != null && getSortKey().length() > 0) {
            int sortKeyTmp = 0;
            for (int i = 0; i < getTable().getFields().size(); i++) {
                final Field field = getTable().getFields().get(i);
                if (field.getName().equals(getSortKey())) {
                    sortKeyTmp = i;
                    break;
                }
            }
            final int index = sortKeyTmp;
            Collections.sort(this.values, new Comparator<UIRow>() {

                public int compare(final UIRow _rowModel1,
                                   final UIRow _rowModel2)
                                            {

                    final UITableCell cellModel1 = _rowModel1.getValues().get(index);
                    final FieldValue fValue1 = new FieldValue(getTable().getFields().get(index), cellModel1
                                    .getUiClass(), cellModel1.getCompareValue() != null ? cellModel1.getCompareValue()
                                    : cellModel1.getCellValue());

                    final UITableCell cellModel2 = _rowModel2.getValues().get(index);
                    final FieldValue fValue2 = new FieldValue(getTable().getFields().get(index), cellModel2
                                    .getUiClass(), cellModel2.getCompareValue() != null ? cellModel2.getCompareValue()
                                    : cellModel2.getCellValue());

                    return fValue1.compareTo(fValue2);
                }
            });
            if (getSortDirection() == SortDirection.DESCENDING) {
                Collections.reverse(this.values);
            }
        }
    }

    /**
     * Class represents one filter applied to this UITable.
     */
    public class Filter
        implements IClusterable
    {

        /**
         * Key to the value for "from" in the nap for the esjp.
         */
        public static final String FROM = "from";

        /**
         * Key to the value for "to" in the nap for the esjp.
         */
        public static final String TO = "to";

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * UITableHeader this filter belohngs to.
         */
        private final UITableHeader uiTableHeader;

        /**
         * Set of value for the filter. Only used for filter using a PICKERLIST.
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
         * Constructor is used for a database based filter in case that it is
         * required.
         */
        public Filter()
        {
            this.uiTableHeader = null;
        }

        /**
         * Constructor is used in case that a filter is required, during loading
         * the date first time for a memory base filter.
         *
         * @param _uitableHeader UITableHeader this filter lies in
         * @throws EFapsException on error
         */
        public Filter(final UITableHeader _uitableHeader)
            throws EFapsException
        {
            this.uiTableHeader = _uitableHeader;
            if (_uitableHeader.getFilterDefault() != null) {
                if (_uitableHeader.getFilterType().equals(FilterType.DATE)) {
                    final String filter = _uitableHeader.getFilterDefault();
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
         */
        public Filter(final UITableHeader _uitableHeader,
                      final String _from,
                      final String _to)
        {
            this.uiTableHeader = _uitableHeader;
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
        public Filter(final UITableHeader _uitableHeader,
                      final Set<?> _filterList)
        {
            this.uiTableHeader = _uitableHeader;
            this.filterList = _filterList;
        }

        /**
         * Getter method for instance variable {@link #uiTableHeader}.
         *
         * @return value of instance variable {@link #uiTableHeader}
         */
        public UITableHeader getUiTableHeader()
        {
            return this.uiTableHeader;
        }

        /**
         * Method to get the map that must be passed for this filter to the
         * esjp.
         *
         * @return Map
         */
        public Map<String, String> getMap4esjp()
        {
            final Map<String, String> ret = new HashMap<String, String>();
            if (this.filterList == null) {
                ret.put(UITable.Filter.FROM, this.from);
                ret.put(UITable.Filter.TO, this.to);

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
            if (this.uiTableHeader.isFilterMemoryBased()) {
                final List<UITableCell> cells = _uiRow.getValues();
                for (final UITableCell cell : cells) {
                    if (cell.getFieldId() == this.uiTableHeader.getFieldId()) {
                        if (this.filterList != null) {
                            final String value = cell.getCellValue();
                            if (!this.filterList.contains(value)) {
                                ret = true;
                            }
                        } else if (this.uiTableHeader.getFilterType().equals(FilterType.DATE)) {
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
    }
}
