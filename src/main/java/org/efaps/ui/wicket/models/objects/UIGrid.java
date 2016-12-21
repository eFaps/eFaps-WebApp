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
package org.efaps.ui.wicket.models.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.RestartResponseException;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Table;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.Filter;
import org.efaps.api.ci.UITableFieldProperty;
import org.efaps.api.ui.FilterBase;
import org.efaps.api.ui.IFilter;
import org.efaps.api.ui.IFilterList;
import org.efaps.api.ui.IListFilter;
import org.efaps.api.ui.IMapFilter;
import org.efaps.api.ui.IOption;
import org.efaps.db.AbstractPrintQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.SelectBuilder;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.field.JSField;
import org.efaps.ui.wicket.models.field.factories.BooleanUIFactory;
import org.efaps.ui.wicket.models.field.factories.DateUIFactory;
import org.efaps.ui.wicket.models.field.factories.DecimalUIFactory;
import org.efaps.ui.wicket.models.field.factories.IComponentFactory;
import org.efaps.ui.wicket.models.field.factories.NumberUIFactory;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class UIGrid.
 *
 * @author The eFaps Team
 */
public class UIGrid
    extends AbstractUI
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UIGrid.class);

    /** The cmd UUID. */
    private UUID cmdUUID;

    /** The initialized. */
    private boolean initialized;

    /** The columns. */
    private final List<Column> columns = new ArrayList<>();

    /** The values. */
    private final List<Row> values = new ArrayList<>();

    /** The filter list. */
    private final FilterList filterList = new FilterList();

    /**
     * Instantiates a new UI grid.
     *
     * @throws EFapsException the e faps exception
     */
    private UIGrid()
    {
    }

    /**
     * Inits the.
     *
     * @throws EFapsException the e faps exception
     */
    private void init()
        throws EFapsException
    {
        if (!this.initialized) {
            this.initialized = true;
            final List<Field> fields = getTable().getFields();

            boolean check4Filter = true;
            if (Context.getThreadContext().containsSessionAttribute(getCacheKey(CacheKey.DBFILTER))) {
                check4Filter = false;
                final FilterList tmpFilterList = (FilterList) Context.getThreadContext().getSessionAttribute(
                                getCacheKey(CacheKey.DBFILTER));
                for (final IFilter filter : tmpFilterList) {
                    this.filterList.add(filter);
                }
            }

            for (final Field field : fields) {
                if (field.hasAccess(TargetMode.VIEW, null, getCommand(), null) && !field.isNoneDisplay(
                                TargetMode.VIEW)) {
                    final Column column = new Column().setFieldConfig(new FieldConfiguration(field.getId()));
                    this.columns.add(column);
                    // before executing the esjp add the filters that are
                    // working against the database to
                    // get them filled with the defaults
                    if (check4Filter && FilterBase.DATABASE.equals(field.getFilter().getBase())) {
                        this.filterList.add(getFilter4Field(column.getField()));
                    }
                }
            }
            load();
        }
    }

    /**
     * Load.
     *
     * @throws EFapsException the e faps exception
     */
    protected void load()
        throws EFapsException
    {
        final List<Instance> instances = getInstances();
        if (CollectionUtils.isNotEmpty(instances)) {
            /** The factories. */
            final Map<Long, JSField> jsFields = new HashMap<>();

            final Set<String> altOIDSel = new HashSet<>();
            final MultiPrintQuery multi = new MultiPrintQuery(instances);
            for (final Column column : this.columns) {
                final Field field = column.getField();
                if (field.getSelect() != null) {
                    multi.addSelect(field.getSelect());
                } else if (field.getAttribute() != null) {
                    multi.addAttribute(field.getAttribute());
                } else if (field.getPhrase() != null) {
                    multi.addPhrase(field.getName(), field.getPhrase());
                } else if (field.getMsgPhrase() != null) {
                    multi.addMsgPhrase(new SelectBuilder(getBaseSelect4MsgPhrase(field)), field.getMsgPhrase());
                }
                if (field.getSelectAlternateOID() != null) {
                    multi.addSelect(field.getSelectAlternateOID());
                    altOIDSel.add(field.getSelectAlternateOID());
                }
                if (field.containsProperty(UITableFieldProperty.SORT_SELECT)) {
                    multi.addSelect(field.getProperty(UITableFieldProperty.SORT_SELECT));
                } else if (field.containsProperty(UITableFieldProperty.SORT_PHRASE)) {
                    multi.addPhrase(field.getProperty(UITableFieldProperty.SORT_PHRASE), field.getProperty(
                                    UITableFieldProperty.SORT_PHRASE));
                } else if (field.containsProperty(UITableFieldProperty.SORT_MSG_PHRASE)) {
                    multi.addMsgPhrase(field.getProperty(UITableFieldProperty.SORT_MSG_PHRASE));
                }
            }
            multi.execute();
            while (multi.next()) {
                final Row row = new Row(multi.getCurrentInstance());
                this.values.add(row);
                for (final Column column : this.columns) {
                    final Field field = column.getField();
                    final Instance instance = evaluateFieldInstance(multi, field);
                    Object value = null;
                    Object sortValue = null;
                    Attribute attr = null;
                    if (field.getSelect() != null) {
                        value = multi.<Object>getSelect(field.getSelect());
                        attr = multi.getAttribute4Select(field.getSelect());
                    } else if (field.getAttribute() != null) {
                        value = multi.<Object>getAttribute(field.getAttribute());
                        attr = multi.getAttribute4Attribute(field.getAttribute());
                    } else if (field.getPhrase() != null) {
                        value = multi.getPhrase(field.getName());
                    } else if (field.getMsgPhrase() != null) {
                        value = multi.getMsgPhrase(new SelectBuilder(getBaseSelect4MsgPhrase(field)), field
                                        .getMsgPhrase());
                    }
                    if (field.containsProperty(UITableFieldProperty.SORT_SELECT)) {
                        sortValue = multi.getSelect(field.getProperty(UITableFieldProperty.SORT_SELECT));
                    } else if (field.containsProperty(UITableFieldProperty.SORT_PHRASE)) {
                        sortValue = multi.getPhrase(field.getProperty(UITableFieldProperty.SORT_PHRASE));
                    } else if (field.containsProperty(UITableFieldProperty.SORT_MSG_PHRASE)) {
                        sortValue = multi.getMsgPhrase(field.getProperty(UITableFieldProperty.SORT_MSG_PHRASE));
                    }

                    final UIValue uiValue = UIValue.get(field, attr, value)
                                    .setInstance(instance)
                                    .setRequestInstances(multi.getInstanceList());

                    final Cell cell = getCell(column, uiValue, sortValue, jsFields);
                    if (column.getFieldConfig().getField().getReference() != null) {
                        cell.setInstance(instance);
                    }
                    row.add(cell);
                }
            }
        }
    }

    /**
     * Gets the cell.
     *
     * @param _column the column
     * @param _uiValue the ui value
     * @param _sortValue the sort value
     * @param _fields the fields
     * @return the cell
     * @throws EFapsException the e faps exception
     */
    protected Cell getCell(final Column _column,
                           final UIValue _uiValue,
                           final Object _sortValue,
                           final Map<Long, JSField> _fields)
        throws EFapsException
    {
        JSField jsField;
        if (_fields.containsKey(_uiValue.getField().getId())) {
            jsField = _fields.get(_uiValue.getField().getId());
        } else {
            jsField = new JSField(_uiValue);
            final IComponentFactory fact = jsField.getFactory();
            if (fact == null) {
                _fields.put(_uiValue.getField().getId(), null);
                jsField = null;
            } else {
                _fields.put(_uiValue.getField().getId(), jsField);
                if (fact instanceof DateUIFactory) {
                    _column.setDataType("date");
                } else if (fact instanceof DecimalUIFactory || fact instanceof NumberUIFactory) {
                    _column.setDataType("number");
                } else if (fact instanceof BooleanUIFactory) {
                    _column.setDataType("boolean");
                }
            }
        }
        final String value;
        if (jsField == null) {
            value = "";
        } else {
            jsField.setValue(_uiValue);
            value = jsField.getFactory().getStringValue(jsField);
        }
        return  new Cell().setValue(value)
                        .setSortValue(_sortValue)
                        .setFieldConfig(_column.getFieldConfig());
    }

    /**
     * Gets the base select for msg phrase.
     *
     * @param _field Field the Base select will be evaluated for
     * @return base select
     */
    protected String getBaseSelect4MsgPhrase(final Field _field)
    {
        String ret = "";
        if (_field.getSelectAlternateOID() != null) {
            ret = StringUtils.removeEnd(_field.getSelectAlternateOID(), ".oid");
        }
        return ret;
    }

    /**
     * Gets the instances.
     *
     * @return the instances
     * @throws EFapsException the e faps exception
     */
    @SuppressWarnings("unchecked")
    protected List<Instance> getInstances()
        throws EFapsException
    {
        final List<Return> ret = getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
                        ParameterValues.INSTANCE, null,
                        ParameterValues.PARAMETERS, Context.getThreadContext().getParameters(),
                        ParameterValues.CLASS, this,
                        ParameterValues.OTHERS, this.filterList);
        List<Instance> lists = null;
        if (ret.size() < 1) {
            throw new EFapsException(UITable.class, "getInstanceList");
        } else {
            lists = (List<Instance>) ret.get(0).get(ReturnValues.VALUES);
        }
        return lists;
    }

    /**
     * Gets the filter for field.
     *
     * @param _field the field
     * @return the filter for field
     */
    protected IFilter getFilter4Field(final Field _field)
    {
        final IFilter ret;
        switch (_field.getFilter().getType()) {
            case STATUS:
            case CLASSIFICATION:
            case PICKLIST:
                ret = new ListFilter(_field.getId());
                break;
            case FREETEXT:
            case FORM:
                ret = new MapFilter(_field.getId());
                break;
            case NONE:
            default:
                ret = new IFilter()
                {
                    /** The Constant serialVersionUID. */
                    private static final long serialVersionUID = 1L;

                    @Override
                    public long getFieldId()
                    {
                        return _field.getId();
                    }
                };
                break;
        }
        return ret;
    }

    /**
     * Gets the values.
     *
     * @return the values
     * @throws EFapsException on error
     */
    public List<Row> getValues()
        throws EFapsException
    {
        init();
        return this.values;
    }

    /**
     * Gets the filter list.
     *
     * @return the filter list
     * @throws EFapsException on error
     */
    public FilterList getFilterList()
        throws EFapsException
    {
        init();
        return this.filterList;
    }

    /**
     * This is the getter method for the instance variable {@link #table}.
     *
     * @return value of instance variable {@link #table}
     * @throws CacheReloadException on error
     */
    public Table getTable()
        throws CacheReloadException
    {
        return getCommand().getTargetTable();
    }

    /**
     * Gets the command.
     *
     * @return the command
     * @throws CacheReloadException the cache reload exception
     */
    public AbstractCommand getCommand()
        throws CacheReloadException
    {
        AbstractCommand cmd = Command.get(getCmdUUID());
        if (cmd == null) {
            cmd = Menu.get(getCmdUUID());
        }
        return cmd;
    }

    /**
     * Gets the cmd UUID.
     *
     * @return the cmd UUID
     */
    public UUID getCmdUUID()
    {
        return this.cmdUUID;
    }

    /**
     * Sets the cmd UUID.
     *
     * @param _cmdUUID the new cmd UUID
     */
    public void setCmdUUID(final UUID _cmdUUID)
    {
        this.cmdUUID = _cmdUUID;
    }

    /**
     * Gets the columns.
     *
     * @return the columns
     * @throws EFapsException on error
     */
    public List<Column> getColumns()
        throws EFapsException
    {
        init();
        return this.columns;
    }

    /**
     * Gets the filter pick list.
     *
     * @param _header the header
     * @return the filter pick list
     */
    public List<String> getFilterPickList(final Column _header)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Checks if is show check boxes.
     *
     * @return true, if is show check boxes
     * @throws CacheReloadException the cache reload exception
     */
    public boolean isShowCheckBoxes()
        throws CacheReloadException
    {
        return getCommand().isTargetShowCheckBoxes();
    }

    /**
     * This method generates the Key for a UserAttribute by using the UUID of
     * the Command and the given UserAttributeKey, so that for every Table a
     * unique key for sorting etc, is created.
     *
     * @param _key UserAttributeKey the Key is wanted
     * @return String with the key
     */
    public String getCacheKey(final CacheKey _key)
    {
        return getCmdUUID() + "-" + _key.getValue();
    }

    /**
     * Evaluate field instance.
     *
     * @param _print the print
     * @param _field field the instance is wanted for
     * @return instance for the field
     * @throws EFapsException on erro
     */
    protected Instance evaluateFieldInstance(final AbstractPrintQuery _print,
                                             final Field _field)
        throws EFapsException
    {
        Instance ret = _print.getCurrentInstance();
        if (_field.getSelectAlternateOID() != null) {
            try {
                final Object alternateObj = _print.getSelect(_field.getSelectAlternateOID());
                if (alternateObj instanceof String) {
                    ret = Instance.get((String) alternateObj);
                } else if (alternateObj instanceof Instance) {
                    ret = (Instance) alternateObj;
                }
            } catch (final ClassCastException e) {
                UIGrid.LOG.error("Field '{}' has invalid SelectAlternateOID value", _field);
            }
        } else if (_field.hasEvents(EventType.UI_FIELD_ALTINST)) {
            final List<Return> retTmps = _field.executeEvents(EventType.UI_FIELD_ALTINST, ParameterValues.INSTANCE, ret,
                            ParameterValues.CALL_INSTANCE, null, ParameterValues.REQUEST_INSTANCES, _print
                                            .getInstanceList(), ParameterValues.PARAMETERS, Context.getThreadContext()
                                                            .getParameters(), ParameterValues.CLASS, this);
            for (final Return retTmp : retTmps) {
                if (retTmp.contains(ReturnValues.INSTANCE) && retTmp.get(ReturnValues.INSTANCE) != null) {
                    ret = (Instance) retTmp.get(ReturnValues.INSTANCE);
                }
            }
        }
        return ret;
    }

    /**
     * This method retrieves the Value for the Title from the eFaps Database.
     *
     * @return Value of the Title
     */
    public String getTitle()
    {
        String title = "";
        try {
            final String key = getCommand().getTargetTitle() == null
                            ? getCommand().getName() + ".Title"
                            : getCommand().getTargetTitle();
            title = DBProperties.getProperty(key);
        } catch (final Exception e) {
            throw new RestartResponseException(new ErrorPage(new EFapsException(this.getClass(), "",
                            "Error reading the Title")));
        }
        return title;
    }

    /**
     * Method used to evaluate the type for this table from the connected
     * events.
     *
     * @return type if found
     * @throws EFapsException on error
     */
    protected Type getType()
        throws EFapsException
    {
        init();
        final Type ret;
        if (getValues().isEmpty()) {
            final List<EventDefinition> events =  getCommand().getEvents(EventType.UI_TABLE_EVALUATE);
            String typeName = null;
            if (events.size() > 1) {
                throw new EFapsException(this.getClass(), "execute4NoInstance.moreThanOneEvaluate");
            } else {
                final EventDefinition event = events.get(0);
                // test for basic or abstract types
                if (event.getProperty("Type") != null) {
                    typeName = event.getProperty("Type");
                }
                // no type yet search alternatives
                if (typeName == null) {
                    for (int i = 1; i < 100; i++) {
                        final String nameTmp = "Type" + String.format("%02d", i);
                        if (event.getProperty(nameTmp) != null) {
                            typeName = event.getProperty(nameTmp);
                        } else {
                            break;
                        }
                    }
                }
            }
            ret = typeName == null ? null : Type.get(typeName);
        } else {
            ret = getValues().get(0).getInstance().getType();
        }
        return ret;
    }

    /**
     * Checks if is date filter.
     *
     * @param _filter the filter
     * @return true, if is date filter
     * @throws EFapsException the e faps exception
     */
    public boolean isDateFilter(final IFilter _filter) throws EFapsException
    {
        final boolean ret;
        final Field field = Field.get(_filter.getFieldId());
        // Explicitly set UIProvider
        if (field.getUIProvider() != null && (field.getUIProvider() instanceof DateTimeUI || field
                        .getUIProvider() instanceof DateUI)) {
            ret = true;
        } else {
            final Attribute attr = getType().getAttribute(field.getAttribute());
            if (attr == null) {
                ret = false;
            } else {
                final IUIProvider uiProvider = attr.getAttributeType().getUIProvider();
                ret = uiProvider instanceof DateTimeUI || uiProvider instanceof DateUI;
            }
        }
        return ret;
    }

    /**
     * Reload.
     *
     * @throws EFapsException the e faps exception
     */
    public void reload()
        throws EFapsException
    {
        if (!this.initialized) {
            init();
        } else {
            this.values.clear();
            Context.getThreadContext().setSessionAttribute(getCacheKey(CacheKey.DBFILTER), this.filterList);
            load();
        }
    }

    /**
     * Gets the.
     *
     * @param _commandUUID the command UUID
     * @return the UI grid
     * @throws EFapsException the e faps exception
     */
    public static UIGrid get(final UUID _commandUUID)
    {
        final UIGrid ret = new UIGrid();
        ret.setCmdUUID(_commandUUID);
        return ret;
    }

    /**
     * The Class Row.
     *
     * @author The eFaps Team
     */
    public static class Row extends ArrayList<Cell>
    {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The instance. */
        private final Instance instance;

        /**
         * Instantiates a new row.
         *
         * @param _instance the instance
         */
        public Row(final Instance _instance)
        {
            this.instance = _instance;
        }

        /**
         * Gets the single instance of Row.
         *
         * @return single instance of Row
         */
        public Instance getInstance()
        {
            return this.instance;
        }
    }


    /**
     * The Class Cell.
     *
     * @author The eFaps Team
     */
    public static class Cell
        implements Serializable
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The field config. */
        private FieldConfiguration fieldConfig;

        /** The value. */
        private Object value;

        /** The sort value. */
        private Object sortValue;

        /** The instance. */
        private Instance instance;

        /**
         * Gets the single instance of Cell.
         *
         * @return single instance of Cell
         */
        public Instance getInstance()
        {
            return this.instance;
        }

        /**
         * Sets the instance.
         *
         * @param _instance the instance
         * @return the cell
         */
        public Cell setInstance(final Instance _instance)
        {
            this.instance = _instance;
            return this;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public String getValue()
        {
            return String.valueOf(this.value);
        }

        /**
         * Sets the value.
         *
         * @param _value the value
         * @return the cell
         */
        public Cell setValue(final Object _value)
        {
            this.value = _value;
            return this;
        }

        /**
         * Gets the sort value.
         *
         * @return the sort value
         */
        public Object getSortValue()
        {
            return this.sortValue;
        }

        /**
         * Sets the sort value.
         *
         * @param _sortValue the sort value
         * @return the cell
         */
        public Cell setSortValue(final Object _sortValue)
        {
            this.sortValue = _sortValue;
            return this;
        }

        /**
         * Gets the field config.
         *
         * @return the field config
         */
        public FieldConfiguration getFieldConfig()
        {
            return this.fieldConfig;
        }

        /**
         * Sets the field config.
         *
         * @param _fieldConfig the field config
         * @return the cell
         */
        public Cell setFieldConfig(final FieldConfiguration _fieldConfig)
        {
            this.fieldConfig = _fieldConfig;
            return this;
        }
    }

    /**
     * The Class Column.
     *
     * @author The eFaps Team
     */
    public static class Column
        implements Serializable
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;
        /**
         * Id of the field this UITableHeader belongs to.
         */
        private FieldConfiguration fieldConfig;

        /** The data type. */
        private String dataType;

        /**
         * Gets the field name.
         *
         * @return the field name
         */
        public String getFieldName()
        {
            return getFieldConfig().getName();
        }

        /**
         * Gets the filter.
         *
         * @return the filter belonging to this header.
         */
        public Filter getFilter()
        {
            return getField().getFilter();
        }

        /**
         * Gets the field.
         *
         * @return the field this haeder belongs to.
         */
        public Field getField()
        {
            return getFieldConfig().getField();
        }

        /**
         * Gets the id of the field this UITableHeader belongs to.
         *
         * @return the id of the field this UITableHeader belongs to
         */
        public FieldConfiguration getFieldConfig()
        {
            return this.fieldConfig;
        }

        /**
         * Gets the label.
         *
         * @return translated label
         */
        public String getLabel()
        {
            return getFieldConfig().getLabel();
        }

        /**
         * Sets the field config.
         *
         * @param _fieldConfig the field config
         * @return the column
         */
        protected Column setFieldConfig(final FieldConfiguration _fieldConfig)
        {
            this.fieldConfig = _fieldConfig;
            return this;
        }

        /**
         * Getter method for the instance variable {@link #dataType}.
         *
         * @return value of instance variable {@link #dataType}
         */
        public String getDataType()
        {
            return this.dataType;
        }

        /**
         * Setter method for instance variable {@link #dataType}.
         *
         * @param _dataType value for instance variable {@link #dataType}
         */
        public Column setDataType(final String _dataType)
        {
            this.dataType = _dataType;
            return this;
        }
    }

    /**
     * The Class FilterList.
     *
     * @author The eFaps Team
     */
    public static class FilterList
        extends HashSet<IFilter>
        implements IFilterList
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;
    }

    /**
     * The Class ListFilter.
     *
     * @author The eFaps Team
     */
    public static class ListFilter
        extends ArrayList<IOption>
        implements IListFilter
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;
        /** The field id. */
        private final long fieldId;

        /**
         * Instantiates a new map filter.
         *
         * @param _fieldId the field id
         */
        public ListFilter(final long _fieldId)
        {
            this.fieldId = _fieldId;
        }

        @Override
        public long getFieldId()
        {
            return this.fieldId;
        }
    }

    /**
     * The Class MapFilter.
     *
     * @author The eFaps Team
     */
    public static class MapFilter
        extends HashMap<String, Object>
        implements IMapFilter
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The field id. */
        private final long fieldId;

        /**
         * Instantiates a new map filter.
         *
         * @param _fieldId the field id
         */
        public MapFilter(final long _fieldId)
        {
            this.fieldId = _fieldId;
        }

        @Override
        public long getFieldId()
        {
            return this.fieldId;
        }
    }

    /**
     * This enum holds the Values used as part of the key for the UserAttributes
     * or SessionAttribute witch belong to a TableModel.
     *
     * @author The eFaps Team
     */
    public enum CacheKey
    {
        /** The DB filter. */
        DBFILTER("dbFilter"),

        /**
         * Key for SessionAttribute used for the filter of a table.
         */
        GRIDX("gridx");

        /**
         * Value of the user attribute.
         */
        private final String value;

        /**
         * Constructor setting the instance variable.
         *
         * @param _value Value
         */
        CacheKey(final String _value)
        {
            this.value = _value;
        }

        /**
         * Gets the value of the user attribute.
         *
         * @return the value
         */
        public String getValue()
        {
            return this.value;
        }
    }
}
