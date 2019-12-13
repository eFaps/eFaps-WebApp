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
package org.efaps.ui.wicket.models.objects.grid;

import java.io.File;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.RestartResponseException;
import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.DateTimeUI;
import org.efaps.admin.datamodel.ui.DateUI;
import org.efaps.admin.datamodel.ui.IUIProvider;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Table;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.user.Role;
import org.efaps.api.ci.UITableFieldProperty;
import org.efaps.api.ui.FilterBase;
import org.efaps.api.ui.IFilter;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.AbstractPrintQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.db.SelectBuilder;
import org.efaps.db.stmt.PrintStmt;
import org.efaps.db.stmt.selection.Evaluator;
import org.efaps.eql2.EQL2;
import org.efaps.eql2.IPrintStatement;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.field.JSField;
import org.efaps.ui.wicket.models.field.factories.BooleanUIFactory;
import org.efaps.ui.wicket.models.field.factories.DateTimeUIFactory;
import org.efaps.ui.wicket.models.field.factories.DateUIFactory;
import org.efaps.ui.wicket.models.field.factories.DecimalUIFactory;
import org.efaps.ui.wicket.models.field.factories.IComponentFactory;
import org.efaps.ui.wicket.models.field.factories.NumberUIFactory;
import org.efaps.ui.wicket.models.objects.AbstractUI;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.models.objects.IPageObject;
import org.efaps.ui.wicket.models.objects.IWizardElement;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIWizardObject;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
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
    implements IPageObject, IWizardElement, ICmdUIObject
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
    private final List<GridColumn> columns = new ArrayList<>();

    /** The values. */
    private final List<GridRow> values = new ArrayList<>();

    /** The filter list. */
    private final FilterList filterList = new FilterList();

    /** The call instance. */
    private Instance callInstance;

    /** The pageposition. */
    private PagePosition pagePosition;

    /** The ui wizard object. */
    private UIWizardObject uiWizardObject;

    /** The show check boxes. */
    private Boolean showCheckBoxes;

    /** The columns up to date. */
    private boolean columnsUpToDate;

    /**
     * Instantiates a new UI grid.
     *
     * @throws EFapsException the e faps exception
     */
    protected UIGrid()
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
        if (!initialized) {
            initialized = true;
            final List<Field> fields = getTable().getFields();

            boolean check4Filter = true;
            if (Context.getThreadContext().containsSessionAttribute(getCacheKey(CacheKey.DBFILTER))) {
                check4Filter = false;
                final FilterList tmpFilterList = (FilterList) Context.getThreadContext().getSessionAttribute(
                                getCacheKey(CacheKey.DBFILTER));
                for (final IFilter filter : tmpFilterList) {
                    filterList.add(filter);
                }
            }

            for (final Field field : fields) {
                if (field.hasAccess(TargetMode.VIEW, null, getCommand(), null) && !field.isNoneDisplay(
                                TargetMode.VIEW)) {
                    final GridColumn column = new GridColumn().setFieldConfig(new FieldConfiguration(field.getId()));
                    columns.add(column);
                    // before executing the esjp add the filters that are
                    // working against the database to
                    // get them filled with the defaults
                    if (check4Filter && FilterBase.DATABASE.equals(field.getFilter().getBase())) {
                        filterList.add(getFilter4Field(column.getField()));
                    }
                }
            }
            if (isStructureTree()) {
                load(getPrint(getMainQueryStmt()), Collections.emptyMap());
            } else {
                final List<Instance> instances = getInstances();
                setColumnsUpToDate(CollectionUtils.isNotEmpty(instances));
                load(instances);
            }
        }
    }

    protected String getMainQueryStmt()
        throws EFapsException
    {
        final List<Return> returns = getEventObject().executeEvents(EventType.UI_TABLE_EVALUATE,
                        ParameterValues.INSTANCE, getCallInstance(),
                        ParameterValues.CALL_INSTANCE, getCallInstance(),
                        ParameterValues.PARAMETERS, Context.getThreadContext().getParameters(),
                        ParameterValues.CLASS, this,
                        ParameterValues.OTHERS, filterList);
        String ret = null;
        if (returns.size() < 1) {
            throw new EFapsException(UIGrid.class, "getInstanceList");
        } else {
            final Object result = returns.get(0).get(ReturnValues.VALUES);
            if (result instanceof String) {
                ret =  (String) result;
            }
        }
        return ret;
    }

    protected String getChildQueryStmt(final Collection<Instance> _instances)
        throws EFapsException
    {
        final List<Return> returns = getEventObject().executeEvents(EventType.UI_TABLE_EVALUATE,
                        ParameterValues.INSTANCE, getCallInstance(),
                        ParameterValues.CALL_INSTANCE, getCallInstance(),
                        ParameterValues.REQUEST_INSTANCES, _instances,
                        ParameterValues.PARAMETERS, Context.getThreadContext().getParameters(),
                        ParameterValues.CLASS, this,
                        ParameterValues.OTHERS, filterList);
        String ret = null;
        if (returns.size() < 1) {
            throw new EFapsException(UIGrid.class, "getInstanceList");
        } else {
            final Object result = returns.get(0).get(ReturnValues.VALUES);
            if (result instanceof String) {
                ret = (String) result;
            }
        }
        return ret;
    }

    protected CharSequence getPrint(final String _queryStmt)
        throws EFapsException
    {
        final StringBuilder stmtBdlr = new StringBuilder().append(_queryStmt);
        if (!_queryStmt.contains(" select ")) {
            stmtBdlr.append(" select ");
        } else {
            stmtBdlr.append(", ");
        }
        final List<String> selects = new ArrayList<>();
        for (final GridColumn column : columns) {
            final Field field = column.getField();
            if (field.getSelect() != null) {
                selects.add(field.getSelect() + " as " + field.getName());
            } else if (field.getAttribute() != null) {
                selects.add("attribute[" + field.getAttribute() + "]  as " + field.getName());
            }
            if (field.getSelectAlternateOID() != null) {
                selects.add(field.getSelectAlternateOID() + " as " + field.getName() + "AOID");
            }
            if (field.containsProperty(UITableFieldProperty.SORT_SELECT)) {
                selects.add(field.getProperty(UITableFieldProperty.SORT_SELECT) + " as " + field.getName() + "SORT");
            } else if (field.containsProperty(UITableFieldProperty.SORT_PHRASE)) {
                // sortValue = multi.getPhrase(field.getProperty(UITableFieldProperty.SORT_PHRASE));
            } else if (field.containsProperty(UITableFieldProperty.SORT_MSG_PHRASE)) {
                // sortValue = multi.getMsgPhrase(field.getProperty(UITableFieldProperty.SORT_MSG_PHRASE));
            }
        }
        stmtBdlr.append(selects.stream().collect(Collectors.joining(", ")));
        return stmtBdlr;
    }

    protected void load(final CharSequence _printStmt, final Map<Instance, GridRow> _parents)
        throws EFapsException
    {
        final Map<Long, JSField> jsFields = new HashMap<>();
        final IPrintStatement<?> stmt = (IPrintStatement<?>) EQL2.parse(_printStmt);
        final Evaluator evaluator = PrintStmt.get(stmt).evaluate();
        final Map<Instance, GridRow> instances = new LinkedMap<>();
        while (evaluator.next()) {
            final GridRow row = new GridRow(evaluator.inst());
            instances.put(evaluator.inst(), row);
            if (_parents.isEmpty()) {
                values.add(row);
            } else {
                final Instance parentInstance = evaluator.get("ParentInstance");
                if (_parents.containsKey(parentInstance)) {
                    _parents.get(parentInstance).addChild(row);
                }
            }
            for (final GridColumn column : columns) {
                final Field field = column.getField();
                final Instance instance = evaluateFieldInstance(evaluator, field);
                final Object value = evaluator.get(field.getName());
                Object sortValue = null;
                if (field.containsProperty(UITableFieldProperty.SORT_SELECT)
                                || field.containsProperty(UITableFieldProperty.SORT_PHRASE)
                                || field.containsProperty(UITableFieldProperty.SORT_MSG_PHRASE)) {
                    sortValue =  evaluator.get(field.getName() + "SORT");
                }

                final Attribute attr = evaluator.attribute(field.getName());
                final UIValue uiValue = UIValue.get(field, attr, value)
                                .setInstance(instance)
                                .setRequestInstances(null)
                                .setCallInstance(getCallInstance());

                final GridCell cell = getCell(column, uiValue, sortValue, jsFields);
                if (column.getFieldConfig().getField().getReference() != null) {
                    cell.setInstance(instance);
                }
                row.add(cell);
            }
        }
        if (isStructureTree() && MapUtils.isNotEmpty(instances)) {
            load(getPrint(getChildQueryStmt(instances.keySet())), instances);
        }
    }

    protected Instance evaluateFieldInstance(final Evaluator _evaluator,
                                             final Field _field)
        throws EFapsException
    {
        Instance ret = _evaluator.inst();
        if (_field.getSelectAlternateOID() != null) {
            try {
                final Object alternateObj = _evaluator.get(_field.getName() + "AOID");
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
                            ParameterValues.CALL_INSTANCE, null, ParameterValues.REQUEST_INSTANCES, null,
                            ParameterValues.PARAMETERS, Context.getThreadContext()
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
     * Load.
     *
     * @param _instances the instances
     * @throws EFapsException the eFaps exception
     */
    protected void load(final List<Instance> _instance)
        throws EFapsException
    {
        if (CollectionUtils.isNotEmpty(_instance)) {
            /** The factories. */
            final Map<Long, JSField> jsFields = new HashMap<>();

            final MultiPrintQuery multi = new MultiPrintQuery(_instance);
            for (final GridColumn column : columns) {
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
            multi.executeWithoutAccessCheck();
            while (multi.next()) {
                final GridRow row = new GridRow(multi.getCurrentInstance());
                values.add(row);
                for (final GridColumn column : columns) {
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
                                    .setRequestInstances(multi.getInstanceList())
                                    .setCallInstance(getCallInstance());

                    final GridCell cell = getCell(column, uiValue, sortValue, jsFields);
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
    protected GridCell getCell(final GridColumn _column,
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
                } else if (fact instanceof DateTimeUIFactory) {
                    _column.setDataType("datetime");
                } else if (fact instanceof DecimalUIFactory || fact instanceof NumberUIFactory) {
                    _column.setDataType("number");
                } else if (fact instanceof BooleanUIFactory) {
                    _column.setDataType("enum");
                    @SuppressWarnings("unchecked")
                    final Map<String, ?> enumValues = (Map<String, ?>) _uiValue.getReadOnlyValue(TargetMode.VIEW);
                    _column.setEnumValues(enumValues.keySet());
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
        return  new GridCell().setValue(value)
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
        final List<Return> returns = getEventObject().executeEvents(EventType.UI_TABLE_EVALUATE,
                        ParameterValues.INSTANCE, getCallInstance(),
                        ParameterValues.CALL_INSTANCE, getCallInstance(),
                        ParameterValues.PARAMETERS, Context.getThreadContext().getParameters(),
                        ParameterValues.CLASS, this,
                        ParameterValues.OTHERS, filterList);
        List<Instance> ret = null;
        if (returns.size() < 1) {
            throw new EFapsException(UIGrid.class, "getInstanceList");
        } else {
            final Object result = returns.get(0).get(ReturnValues.VALUES);
            if (result instanceof List) {
                ret = (List<Instance>) result;
            }
        }
        return ret;
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
            case PICKLIST:
                ret = new ListFilter(_field.getId());
                break;
            case FREETEXT:
            case FORM:
                ret = new MapFilter(_field.getId());
                break;
            case CLASSIFICATION:
                ret = new ClassificationFilter(_field.getId());
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
    public List<GridRow> getValues()
        throws EFapsException
    {
        init();
        return Collections.unmodifiableList(values);
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
        return filterList;
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
    @Override
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
     * Checks if is structure tree.
     *
     * @return true, if is structure tree
     * @throws CacheReloadException the cache reload exception
     */
    public boolean isStructureTree()
        throws CacheReloadException
    {
        return getCommand().getTargetStructurBrowserField() != null;
    }

    /**
     * Gets the structur browser field.
     *
     * @return the structur browser field
     * @throws CacheReloadException the cache reload exception
     */
    public String getStructurBrowserField()
        throws CacheReloadException
    {
        return getCommand().getTargetStructurBrowserField();
    }

    /**
     * Gets the cmd UUID.
     *
     * @return the cmd UUID
     */
    public UUID getCmdUUID()
    {
        return cmdUUID;
    }

    /**
     * Sets the cmd UUID.
     *
     * @param _cmdUUID the new cmd UUID
     * @return the UI grid
     */
    public UIGrid setCmdUUID(final UUID _cmdUUID)
    {
        cmdUUID = _cmdUUID;
        return this;
    }

    /**
     * Gets the columns.
     *
     * @return the columns
     * @throws EFapsException on error
     */
    public List<GridColumn> getColumns()
        throws EFapsException
    {
        init();
        return Collections.unmodifiableList(columns);
    }

    /**
     * Gets the filter pick list.
     *
     * @param _header the header
     * @return the filter pick list
     */
    public List<String> getFilterPickList(final GridColumn _header)
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
        return showCheckBoxes == null ? getCommand().isTargetShowCheckBoxes() : showCheckBoxes;
    }

    /**
     * Setter method for instance variable {@link #showCheckBoxes}.
     *
     * @param _showCheckBoxes value for instance variable {@link #showCheckBoxes}
     */
    public void setShowCheckBoxes(final Boolean _showCheckBoxes)
    {
        showCheckBoxes = _showCheckBoxes;
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
            if (title != null && getCallInstance() != null) {
                final PrintQuery print = new PrintQuery(getCallInstance());
                final ValueParser parser = new ValueParser(new StringReader(title));
                final ValueList list = parser.ExpressionString();
                list.makeSelect(print);
                if (print.execute()) {
                    title = list.makeString(getCallInstance(), print, TargetMode.VIEW);
                }
                // Administration
                if (Configuration.getAttributeAsBoolean(Configuration.ConfigAttribute.SHOW_OID)
                                && Context.getThreadContext()
                                                .getPerson()
                                                .isAssigned(Role.get(UUID
                                                                .fromString("1d89358d-165a-4689-8c78-fc625d37aacd")))) {
                    title = title + " " + getCallInstance().getOid();
                }
            }
        } catch (final EFapsException | ParseException e) {
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

    @Override
    public Instance getInstance()
        throws EFapsException
    {
        return getCallInstance();
    }

    /**
     * Getter method for the instance variable {@link #callInstance}.
     *
     * @return value of instance variable {@link #callInstance}
     */
    public Instance getCallInstance()
    {
        return callInstance;
    }

    /**
     * Setter method for instance variable {@link #callInstance}.
     *
     * @param _callInstance value for instance variable {@link #callInstance}
     * @return the UI grid
     */
    public UIGrid setCallInstance(final Instance _callInstance)
    {
        callInstance = _callInstance;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #pagePosition}.
     *
     * @return value of instance variable {@link #pagePosition}
     */
    @Override
    public PagePosition getPagePosition()
    {
        return pagePosition;
    }

    /**
     * Setter method for instance variable {@link #pagePosition}.
     *
     * @param _pagePosition value for instance variable {@link #pagePosition}
     * @return the UI grid
     */
    public UIGrid setPagePosition(final PagePosition _pagePosition)
    {
        pagePosition = _pagePosition;
        return this;
    }

    /**
     * Reload.
     *
     * @throws EFapsException the e faps exception
     */
    public void reload()
        throws EFapsException
    {
        if (!initialized) {
            init();
        } else {
            values.clear();
            Context.getThreadContext().setSessionAttribute(getCacheKey(CacheKey.DBFILTER), filterList);
            load(getInstances());
        }
    }

    @Override
    public boolean isWizardCall()
    {
        return getUIWizardObject() != null;
    }

    @Override
    public UIWizardObject getUIWizardObject()
    {
        return uiWizardObject;
    }

    @Override
    public IWizardElement setUIWizardObject(final UIWizardObject _uiWizardObject)
    {
        uiWizardObject = _uiWizardObject;
        return this;
    }

    @Override
    public List<Return> executeEvents(final EventType _eventType,
                                      final Object... _objectTuples)
        throws EFapsException
    {
        final List<Return> ret;
        if (isWizardCall()) {
            ret = ((ICmdUIObject) getUIWizardObject().getWizardElement().get(0)).executeEvents(_eventType,
                            _objectTuples);
        } else {
            ret = getEventObject().executeEvents(_eventType, _objectTuples);
        }
        return ret;
    }

    /**
     * Gets the event object.
     *
     * @return the event object
     * @throws CacheReloadException the cache reload exception
     */
    public AbstractAdminObject getEventObject()
        throws CacheReloadException
    {
        return getCommand();
    }

    /**
     * Checks if is columns up to date.
     *
     * @return the columns up to date
     */
    public boolean isColumnsUpToDate()
    {
        return columnsUpToDate;
    }

    /**
     * Sets the columns up to date.
     *
     * @param _columnsUpToDate the new columns up to date
     */
    public void setColumnsUpToDate(final boolean _columnsUpToDate)
    {
        columnsUpToDate = _columnsUpToDate;
    }

    /**
     * Gets the row for id.
     *
     * @param _rowId the row id
     * @return the row for id
     * @throws EFapsException the e faps exception
     */
    public GridRow getRow4Id(final String _rowId)
        throws EFapsException
    {
        final String[] rowIds = _rowId.split("-");
        GridRow row = null;
        for (final String id : rowIds) {
            if (row == null) {
                row = getValues().get(Integer.parseInt(id));
            } else {
                row = row.getChildren().get(Integer.parseInt(id));
            }
        }
        return row;
    }

    public String getMarkupId()
    {
        return "grid";
    }

    /**
     * Gets the.
     *
     * @param _commandUUID the command UUID
     * @param _pagePosition the page position
     * @return the UI grid
     */
    public static UIGrid get(final UUID _commandUUID,
                             final PagePosition _pagePosition)
    {
        final UIGrid ret = new UIGrid();
        ret.setCmdUUID(_commandUUID).setPagePosition(_pagePosition);
        return ret;
    }

    /**
     * Prints the.
     *
     * @param _uiGrid the ui grid
     * @return the file
     */
    public static File print(final UIGrid _uiGrid)
    {
        File ret = null;
        final String clazzName = Configuration.getAttribute(ConfigAttribute.GRIDPRINTESJP);
            try {
                UIGrid.LOG.debug("Print method executed for {}", _uiGrid);
                final Class<?> clazz = Class.forName(clazzName, false, EFapsClassLoader.getInstance());
                final EventExecution event = (EventExecution) clazz.getDeclaredConstructor().newInstance();
                final Parameter param = new Parameter();
                param.put(ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
                param.put(ParameterValues.CLASS, _uiGrid);
                final Return retu = event.execute(param);
                if (retu != null) {
                    ret = (File) retu.get(ReturnValues.VALUES);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                UIGrid.LOG.error("Catched", e);
            } catch (final EFapsException e) {
                UIGrid.LOG.error("Catched", e);
            }

        return ret;
    }

    /**
     * Prints the.
     *
     * @param _instance the instance
     * @return the file
     */
    public static File checkout(final Instance _instance)
    {
        File ret = null;
        final String clazzName = Configuration.getAttribute(ConfigAttribute.GRIDCHECKOUTESJP);
        try {
            UIGrid.LOG.debug("Checkout method executed for {}", _instance);
            final Class<?> clazz = Class.forName(clazzName, false, EFapsClassLoader.getInstance());
            final EventExecution event = (EventExecution) clazz.getDeclaredConstructor().newInstance();
            final Parameter param = new Parameter();
            param.put(ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
            param.put(ParameterValues.INSTANCE, _instance);
            final Return retu = event.execute(param);
            if (retu != null) {
                ret = (File) retu.get(ReturnValues.VALUES);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            UIGrid.LOG.error("Catched", e);
        } catch (final EFapsException e) {
            UIGrid.LOG.error("Catched", e);
        }
        return ret;
    }
}
