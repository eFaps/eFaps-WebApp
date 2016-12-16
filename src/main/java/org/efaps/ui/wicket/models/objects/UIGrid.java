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
import java.util.Collection;
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
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Table;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.Filter;
import org.efaps.api.ci.UITableFieldProperty;
import org.efaps.api.ui.FilterBase;
import org.efaps.api.ui.IFilter;
import org.efaps.api.ui.IFilterList;
import org.efaps.db.AbstractPrintQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.SelectBuilder;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.field.JSField;
import org.efaps.ui.wicket.models.field.factories.IComponentFactory;
import org.efaps.ui.wicket.models.objects.AbstractUIHeaderObject.UserCacheKey;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Instantiates a new UI grid.
     */
    private UIGrid()
    {
    }

    private void init()
        throws EFapsException
    {
        if (!this.initialized) {
            this.initialized = true;
            final List<Field> fields = getTable().getFields();
            for (final Field field : fields) {
                if (field.hasAccess(TargetMode.VIEW, null, getCommand(), null) && !field.isNoneDisplay(
                                TargetMode.VIEW)) {
                    final Column column = new Column().setFieldConfig(new FieldConfiguration(field.getId()));
                    this.columns.add(column);
                }
            }
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
    }

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

    protected List<Instance> getInstances()
        throws EFapsException
    {
        final FilterList filterList = new FilterList();
        for (final Column column : getColumns()) {
            if (FilterBase.DATABASE.equals(column.getField().getFilter().getBase())) {
                filterList.getFilters().add(getFilter4Field(column.getField()));
            }
        }
        final List<Return> ret = getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
                        ParameterValues.INSTANCE, null,
                        ParameterValues.PARAMETERS, Context.getThreadContext().getParameters(),
                        ParameterValues.CLASS, this,
                        ParameterValues.OTHERS, filterList);
        List<Instance> lists = null;
        if (ret.size() < 1) {
            throw new EFapsException(UITable.class, "getInstanceList");
        } else {
            lists = (List<Instance>) ret.get(0).get(ReturnValues.VALUES);
        }
        return lists;
    }

    protected IFilter getFilter4Field(final Field _field)
    {
        return new IFilter()
        {
            @Override
            public Long getFieldID()
            {
                return _field.getId();
            }
        };
    }


    public static class FilterList
        implements IFilterList
    {
        List<IFilter> filters = new ArrayList<>();

        @Override
        public Collection<IFilter> getFilters()
        {
            return this.filters;
        }
    }

    public List<Row> getValues()
        throws EFapsException
    {
        init();
        return this.values;
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

    public AbstractCommand getCommand()
        throws CacheReloadException
    {
        return Command.get(getCmdUUID());
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
     */
    public List<Column> getColumns()
        throws EFapsException
    {
        init();
        return this.columns;
    }

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
    public String getCacheKey(final UserCacheKey _key)
    {
        return getCmdUUID() + "-" + _key.getValue();
    }

    /**
     * @param _multi multiprint
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
     * @throws Exception
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
        } // CHECKSTYLE:ON
        return title;
    }

    /**
     * Gets the.
     *
     * @param _commandUUID the command UUID
     * @return the UI grid
     */
    public static UIGrid get(final UUID _commandUUID)
    {
        final UIGrid ret = new UIGrid();
        ret.setCmdUUID(_commandUUID);
        return ret;
    }

    public static class Row extends ArrayList<Cell>
    {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        private final Instance instance;

        public Row(final Instance _Instance)
        {
            this.instance = _Instance;
        }

        public Instance getInstance()
        {
            return this.instance;
        }
    }


    public static class Cell
        implements Serializable
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        private FieldConfiguration fieldConfig;

        private Object value;

        private Object sortValue;

        private Instance instance;


        public Instance getInstance()
        {
            return this.instance;
        }


        public Cell setInstance(final Instance instance)
        {
            this.instance = instance;
            return this;
        }

        public String getValue()
        {
            return String.valueOf(this.value);
        }

        public Cell setValue(final Object _value)
        {
            this.value = _value;
            return this;
        }

        public Object getSortValue()
        {
            return this.sortValue;
        }

        public Cell setSortValue(final Object _sortValue)
        {
            this.sortValue = _sortValue;
            return this;
        }


        public FieldConfiguration getFieldConfig()
        {
            return this.fieldConfig;
        }


        public Cell setFieldConfig(final FieldConfiguration _fieldConfig)
        {
            this.fieldConfig = _fieldConfig;
            return this;
        }
    }

    public static class Column
        implements Serializable
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        /**
         * Id of the field this UITableHeader belongs to.
         */
        private FieldConfiguration fieldConfig;

        public String getFieldName()
        {
            return getFieldConfig().getName();
        }

        /**
         * @return the filter belonging to this header.
         */
        public Filter getFilter()
        {
            return getField().getFilter();
        }

        /**
         * @return the field this haeder belongs to.
         */
        public Field getField()
        {
            return getFieldConfig().getField();
        }

        public FieldConfiguration getFieldConfig()
        {
            return this.fieldConfig;
        }

        /**
         * @return translated label
         */
        public String getLabel()
        {
            return getFieldConfig().getLabel();
        }

        protected Column setFieldConfig(final FieldConfiguration _fieldConfig)
        {
            this.fieldConfig = _fieldConfig;
            return this;
        }

    }
}
