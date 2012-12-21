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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.RestartResponseException;
import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.field.Field;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.PrintQuery;
import org.efaps.ui.wicket.models.cell.UIHiddenCell;
import org.efaps.ui.wicket.models.cell.UIStructurBrowserTableCell;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;
import org.efaps.util.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to provide the Model for the StructurBrowser for eFpas. <br>
 * It is used in tow different cases. In one case it is a TreeTable, where the
 * Table will be provided with additional information in columns. In the other
 * case a Tree only.<br>
 * The concept of this class is to provide a Model which connects through the
 * eFaps-kernel to the eFaps-DataBase and turn it to a Standard TreeModel from
 * <code>javax.swing.tree</code>. which will be used from the Component to
 * render the Tree and the Table. This leads to a very similar behavior of the
 * WebApp GUI to a swing GUI. <br>
 * This model works asyncron. That means only the actually in the GUI rendered
 * Nodes (and Columns) will be retrieved from the eFaps-DataBase. The next level
 * in a tree will be retrieved on the expand of a TreeNode. To achieve this and
 * to be able to render expand-links for every node it will only be checked if
 * it is a potential parent (if it has children). In the case of expanding this
 * Node the children will be retrieved and rendered.<br>
 * To access the eFaps-Database a esjp is used, which will be used in five
 * different cases. To distinguish the use of the esjp some extra Parameters
 * will be passed to the esjp when calling it.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIStructurBrowser
    extends AbstractUIHeaderObject
{
    /**
     * Enum is used to set for this UIStructurBrowser which status of execution
     * it is in.
     */
    public enum ExecutionStatus {
        /** Method addChildren is executed. */
        ADDCHILDREN,
        /** Method addChildren is executed. */
        ALLOWSCHILDREN,
        /** Method is executed to check if the in case of edit the creation of items is allowed. */
        ALLOWSITEM,
        /** Method checkForChildren is executed. */
        CHECKFORCHILDREN,
        /** Method is creating a new folder. */
        CHECKHIDECOLUMN4ROW,
        /**
         * Method is called after the insert etc. of a new node in edit mode to
         * get a JavaScript that will be appended to the AjaxTarget. In this
         * Step the values for the StructurBrowsers also can be altered.
         * @see {@link UIStructurBrowser.#setValuesFromUI(Map, DefaultMutableTreeNode)setValuesFromUI}
         */
        GETJAVASCRIPT4TARGET,
        /** Method execute is executed. */
        EXECUTE,
        /**
         * Executed on removal of a node as an listener and does not
         * effect directly the tree, but allows to manipulate it.
         */
        NODE_REMOVE,
        /**
         * Executed on insert of an new item node as an listener and does not
         * effect directly the tree, but allows to manipulate it.
         */
        NODE_INSERTITEM,
        /**
         * Executed on insert of an item as child as an listener and does not
         * effect directly the tree, but allows to manipulate it.
         */
        NODE_INSERTCHILDITEM,
        /**
         * Executed on insert of an folder as child as an listener and does not
         * effect directly the tree, but allows to manipulate it.
         */
        NODE_INSERTCHILDFOLDER,
        /** Method sort is executed. */
        SORT;
    }

    /**
     * Static part of the key to get the Information stored in the session
     * in relation to this StruturBrowser.
     */
    public static final String USERSESSIONKEY = "eFapsUIStructurBrowser";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UIStructurBrowser.class);


    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     *  This instance variable holds if this StructurBrowserModel can have
     *  children at all.
     */
    private boolean allowChildren;

    /**
     *  This instance variable holds if this StructurBrowserModel can have
     *  children of type items, will onlye be evaluated if allowChils is true.
     */
    private boolean allowItems;

    /**
     * This instance variable holds if this StructurBrowserModel is a
     * parent, this is needed because, first it will be only determined if a
     * node is a potential parent, and later on the children will be retrieved
     * from the eFaps-DataBase.
     *
     * @see #isParent()
     */
    private boolean parent;

    /**
     * This instance variable holds the children of this StructurBrowserModel.
     */
    private final List<UIStructurBrowser> children = new ArrayList<UIStructurBrowser>();

    /**
     * Holds the columns in case of a TableTree.
     */
    private final List<UIStructurBrowserTableCell> columns = new ArrayList<UIStructurBrowserTableCell>();

    /**
     * Holds the hidden columns in case of a TableTree.
     */
    private final List<UIHiddenCell> hidden = new ArrayList<UIHiddenCell>();

    /**
     * Holds the label of the Node which will be presented in the GUI.
     *
     * @see #toString()
     * @see #getLabel()
     * @see #setLabel(String)
     * @see #requeryLabel()
     */
    private String label;

    /**
     * Holds the Name of the Field the StructurBrowser should be in, in case of
     * a TableTree.
     */
    private String browserFieldName;

    /**
     * This instance variable holds, if this StructurBrowserModel is the Root of
     * a tree.
     */
    private final boolean root;

    /**
     * Holds the Value for the Label as it is difined in the DBProperties.
     */
    private String valueLabel;

    /**
     * Contains the url for the Image that will be presented in GUI.
     */
    private String image;

    /**
     * this instrance variable is used as a <b>TriState</b>, to determine if the
     * Model should show the direction of this Model as Child in comparisment to
     * the parent.<br>
     * The tristate is used as follows: <li><b>null</b>: no direction will be
     * shown</li> <li><b>true</b>: an arrow showing downwards, will be rendered</li>
     * <li><b>false</b>: an arrow showing upwards, will be rendered</li>
     */
    private Boolean direction = null;

    /**
     * Stores the actual execution status.
     */
    private ExecutionStatus executionStatus;

    /**
     * Is this model expanded.
     */
    private boolean expanded;

    /**
     * The index of the column containing the browser.
     */
    private int browserFieldIndex;

    /**
     * If true the tree is always expanded and the inks for expand and
     * collapse will not work.
     */
    private boolean forceExpanded = false;

    /**
     * Level of the current instance.
     */
    private int level = 0;

    /**
     * The parent UIStructurBrowser. Used to be able to climb up in the hirachy.
     */
    private UIStructurBrowser parentBrws = null;

    /**
     * Set of expanded UIStructurBrowser. Used on ly for the root node to be able
     * to initialize correctly the Tree for the UserInterface.
     */
    private final Set<UIStructurBrowser> expandedBrowsers = new HashSet<UIStructurBrowser>();

    /**
     * Standard constructor, if called this StructurBrowserModel will be defined
     * as root.
     *
     * @param _commandUUID UUID of the calling command
     * @param _instanceKey oid
     * @throws EFapsException on error
     *
     */
    public UIStructurBrowser(final UUID _commandUUID,
                             final String _instanceKey)
        throws EFapsException
    {
        this(_commandUUID, _instanceKey, true, SortDirection.ASCENDING);
    }

    /**
     * Internal constructor, it is used to set that this StructurBrowserModel is
     * not a root.
     *
     * @param _commandUUID UUID of the command
     * @param _instanceKey OID
     * @param _root is this STrtucturbrowser the root
     * @param _sortdirection sort direction
     * @throws EFapsException on error
     */
    protected UIStructurBrowser(final UUID _commandUUID,
                                final String _instanceKey,
                                final boolean _root,
                                final SortDirection _sortdirection)
        throws EFapsException
    {
        super(_commandUUID, _instanceKey);
        this.root = _root;
        if (isRoot()) {
            this.allowChildren = true;
        }
        setSortDirectionInternal(_sortdirection);
        initialise();
    }

    /**
     * Internal method to call a constructor, it is used to set that this
     * StructurBrowserModel is not a root.
     *
     * @param _instance     Instance
     * @param _strucBrwsr   StructurBrowser the values will be copied from
     * @return UIStructurBrowser
     * @throws EFapsException on error
     */
    protected UIStructurBrowser getNewStructurBrowser(final Instance _instance,
                                                      final UIStructurBrowser _strucBrwsr)
        throws EFapsException
    {
        final UUID uuid;
        if (_strucBrwsr.getTable() == null) {
            uuid = Menu.getTypeTreeMenu(_instance.getType()).getUUID();
        } else {
            uuid = _strucBrwsr.getCommandUUID();
        }
        final UIStructurBrowser ret = new UIStructurBrowser(uuid, _instance == null ? null : _instance.getKey(), false,
                        _strucBrwsr.getSortDirection());
        ret.setParentBrws(this);
        ret.setLevel(getLevel() + 1);
        return ret;
    }

    /**
     * Method used to initialize this StructurBrowserModel.
     *
     * @throws EFapsException on error
     */
    protected void initialise()
        throws EFapsException
    {
        final AbstractCommand command = getCommand();
        if ((command != null) && (command.getTargetTable() != null)) {
            setTableUUID(command.getTargetTable().getUUID());
            this.browserFieldName = command.getTargetStructurBrowserField();
            setShowCheckBoxes(command.isTargetShowCheckBoxes());
        } else if (getInstance() != null) {
            final String tmplabel = Menu.getTypeTreeMenu(getInstance().getType()).getLabel();
            this.valueLabel = DBProperties.getProperty(tmplabel);
        }

        // set default sort
        if (command.getTargetTableSortKey() != null) {
            setSortKeyInternal(command.getTargetTableSortKey());
            setSortDirection(command.getTargetTableSortDirection());
        }

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
            UIStructurBrowser.LOG.error("error during the retrieve of UserAttributes", e);
        }
    }

    /**
     * This method should be called to actually execute this
     * StructurBrowserModel, that means to retrieve the values from the
     * eFaps-DataBase, create the TreeModel etc. This method actually calls
     * depending if we have a Tree or a TreeTabel the Methodes
     * {@link #executeTree(List)} or {@link #executeTreeTable(List)}
     *
     * @see #executeTree(List)
     * @see #executeTreeTable(List)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void execute()
    {
        setExecutionStatus(UIStructurBrowser.ExecutionStatus.EXECUTE);
        List<Return> ret;
        try {
            if (getTableUUID() == null) {
                final Map<Instance, Boolean> map = new LinkedHashMap<Instance, Boolean>();
                map.put(getInstance(), null);
                executeTree(map, false);
            } else {
                final Map<Instance, Boolean> map = new LinkedHashMap<Instance, Boolean>();
                if (!isCreateMode()) {
                    ret = getObject4Event().executeEvents(EventType.UI_TABLE_EVALUATE, ParameterValues.CLASS, this,
                                ParameterValues.INSTANCE, getInstance());
                    map.putAll((Map<Instance, Boolean>) ret.get(0).get(ReturnValues.VALUES));
                }
                executeTreeTable(map, false);
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * This method is called in case of a Tree from the {@link #execute()}method
     * to fill this StructurBrowserModel with live.
     *
     * @param _map      List of Object
     * @param _expand   inside an expand
     */
    protected void executeTree(final Map<Instance, Boolean> _map,
                               final boolean _expand)
    {
        try {
            final List<Instance> instances = new ArrayList<Instance>();
            for (final Instance inst : _map.keySet()) {
                instances.add(inst);
            }
            final ValueParser parser = new ValueParser(new StringReader(this.valueLabel));
            final ValueList valuelist = parser.ExpressionString();
            final MultiPrintQuery print = new MultiPrintQuery(instances);
            valuelist.makeSelect(print);
            print.execute();
            while (print.next()) {
                Object value = null;
                final Instance instance = print.getCurrentInstance();
                value = valuelist.makeString(getInstance(), print, getMode());
                final UIStructurBrowser child = getNewStructurBrowser(instance, this);
                this.children.add(child);
                child.setDirection(_map.get(instance));
                child.setLabel(value.toString());
                child.setAllowChildren(checkForAllowChildren(instance));
                if (isAllowChildren()) {
                    child.setParent(checkForChildren(instance));
                }
                child.setImage(Image.getTypeIcon(instance.getType()) != null ? Image.getTypeIcon(instance.getType())
                                .getUrl() : null);
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        } catch (final ParseException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        sortModel();
        expand(_expand);
        super.setInitialized(true);
    }

    /**
     * This method is called in case of a TreeTable from the {@link #execute()}
     * method to fill this StructurBrowserModel with live.
     *
     * @param _map      List of Objects
     * @param _expand   inside an expand
     */
    protected void executeTreeTable(final Map<Instance, Boolean> _map,
                                    final boolean _expand)
    {
        try {
            final List<Instance> instances = new ArrayList<Instance>();
            for (final Instance inst : _map.keySet()) {
                instances.add(inst);
            }
            final List<Integer> userWidthList = getUserWidths();
            // evaluate for all expressions in the table
            final MultiPrintQuery multi = new MultiPrintQuery(instances);
            Type type = instances.isEmpty() ? null : instances.get(0).getType();
            int i = 0;
            for (final Field field : getUserSortedColumns()) {
                Attribute attr = null;
                if (field.hasAccess(getMode(), getInstance())
                                && !field.isNoneDisplay(getMode()) && !field.isHiddenDisplay(getMode())) {
                    if (_map.size() > 0) {
                        if (field.getSelect() != null) {
                            multi.addSelect(field.getSelect());
                        } else if (field.getAttribute() != null) {
                            multi.addAttribute(field.getAttribute());
                        } else if (field.getPhrase() != null) {
                            multi.addPhrase(field.getName(), field.getPhrase());
                        }
                        if (field.getSelectAlternateOID() != null) {
                            multi.addSelect(field.getSelectAlternateOID());
                        }
                    }
                    if (field.getAttribute() != null && type != null) {
                        attr = type.getAttribute(field.getAttribute());
                    }
                    if (isRoot()) {
                        SortDirection sortdirection = SortDirection.NONE;
                        if (field.getName().equals(getSortKey())) {
                            sortdirection = getSortDirection();
                        }
                        final UITableHeader uiTableHeader = new UITableHeader(field, sortdirection, attr);
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
                    }
                    i++;
                }
            }
            if (!multi.execute()) {
                type = getTypeFromEvent();
            }
            Attribute attr = null;
            while (multi.next()) {
                Instance instance = multi.getCurrentInstance();
                final UIStructurBrowser child = getNewStructurBrowser(instance, this);
                child.setDirection(_map.get(instance));
                for (final Field field : getUserSortedColumns()) {
                    if (field.hasAccess(getMode(), getInstance()) && !field.isNoneDisplay(getMode())) {
                        Object value = null;
                        attr = null;
                        //the previous field might have set the different instance
                        if (field.getSelectAlternateOID() == null) {
                            instance = multi.getCurrentInstance();
                        } else {
                            instance = Instance.get(multi.<String>getSelect(field.getSelectAlternateOID()));
                        }
                        if (field.getSelect() != null) {
                            value = multi.getSelect(field.getSelect());
                            attr = multi.getAttribute4Select(field.getSelect());
                        } else if (field.getAttribute() != null) {
                            value = multi.getAttribute(field.getAttribute());
                            attr = multi.getAttribute4Attribute(field.getAttribute());
                        } else if (field.getPhrase() != null) {
                            value = multi.getPhrase(field.getName());
                        }
                        final FieldValue fieldvalue = new FieldValue(field, attr, value, instance, getInstance(),
                                        new ArrayList<Instance>(multi.getInstanceList()));
                        String strValue;
                        String htmlTitle;
                        boolean isHidden = false;
                        if ((isCreateMode() || isEditMode()) && field.isEditableDisplay(getMode())) {
                            strValue = fieldvalue.getEditHtml(getMode());
                            htmlTitle = fieldvalue.getStringValue(getMode());
                        } else if (field.isHiddenDisplay(getMode())) {
                            strValue = fieldvalue.getHiddenHtml(getMode());
                            htmlTitle = "";
                            isHidden = true;
                        } else {
                            strValue = fieldvalue.getReadOnlyHtml(getMode());
                            htmlTitle = fieldvalue.getStringValue(getMode());
                        }
                        if (strValue == null) {
                            strValue = "";
                        }
                        if (htmlTitle == null) {
                            htmlTitle = "";
                        }
                        String icon = field.getIcon();
                        if (field.isShowTypeIcon()) {
                            final Image cellIcon = Image.getTypeIcon(instance.getType());
                            if (cellIcon != null) {
                                icon = cellIcon.getUrl();
                            }
                        }
                        if (isHidden) {
                            child.getHidden().add(new UIHiddenCell(this, fieldvalue, null, strValue));
                        } else {
                            final UIStructurBrowserTableCell cell = new UIStructurBrowserTableCell(child, fieldvalue,
                                            instance, strValue, htmlTitle, icon);

                            if (field.getName().equals(this.browserFieldName)) {
                                child.setLabel(strValue);
                                child.setAllowChildren(checkForAllowChildren(instance));
                                if (child.isAllowChildren()) {
                                    child.setAllowItems(checkForAllowItems(instance));
                                    child.setParent(checkForChildren(instance));
                                }
                                child.setImage(Image.getTypeIcon(instance.getType()) != null ? Image.getTypeIcon(
                                                instance.getType()).getUrl() : null);
                                cell.setBrowserField(true);
                                child.browserFieldIndex = child.getColumns().size();
                            }
                            child.getColumns().add(cell);
                        }
                    }
                }
                this.children.add(child);
                child.checkHideColumn4Row();
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        sortModel();
        expand(_expand);
        super.setInitialized(true);
    }

    /**
     * Getter method for the instance variable {@link #hidden}.
     *
     * @return value of instance variable {@link #hidden}
     */
    public List<UIHiddenCell> getHidden()
    {
        return this.hidden;
    }

    /**
     * Expand the tree with the information from the Session.
     * @param _expand is this inside an expand
     */
    @SuppressWarnings("unchecked")
    protected void expand(final boolean _expand)
    {
        try {
            // only if the element was opened the first time e.g. reload etc.
            if ((isRoot() || _expand)
                       && (Context.getThreadContext().containsSessionAttribute(getCacheKey()) || this.forceExpanded)) {
                final Map<String, Boolean> sessMap = (Map<String, Boolean>) Context
                                .getThreadContext().getSessionAttribute(getCacheKey());
                for (final UIStructurBrowser uiChild : this.children) {
                    if (isForceExpanded() || sessMap == null || sessMap.containsKey(uiChild.getInstanceKey())) {
                        final Boolean expandedTmp = sessMap == null || isForceExpanded()
                                                ? true : sessMap.get(uiChild.getInstanceKey());
                        if (expandedTmp != null && expandedTmp && uiChild.isParent()) {
                            uiChild.setExecutionStatus(UIStructurBrowser.ExecutionStatus.ADDCHILDREN);
                            final List<Return> ret = getObject4Event().executeEvents(EventType.UI_TABLE_EVALUATE,
                                                ParameterValues.INSTANCE, uiChild.getInstance(),
                                                ParameterValues.CLASS, uiChild);
                            final Map<Instance, Boolean> map = (Map<Instance, Boolean>) ret.get(0).get(
                                            ReturnValues.VALUES);
                            uiChild.setExpanded(true);
                            uiChild.add2ExpandedBrowsers(uiChild);
                            if (uiChild.getTableUUID() == null) {
                                uiChild.executeTree(map, true);
                            } else {
                                uiChild.executeTreeTable(map, true);
                            }
                        }
                    }
                }
            }
        } catch (final EFapsException e) {
            UIStructurBrowser.LOG.error("Error retreiving Session info for StruturBrowser from Command with UUID: {}",
                            getCommandUUID(), e);
        }
    }

    /**
     * @return set of expanded browsers
     */
    public Set<UIStructurBrowser> getExpandedBrowsers()
    {
        final Set<UIStructurBrowser> ret;
        if (isRoot()) {
            ret = this.expandedBrowsers;
        } else {
            ret = getParentBrws().getExpandedBrowsers();
        }
        return ret;
    }

    /**
     * @param _structBrowser structurbrowser to be added to the expanded
     */
    protected void add2ExpandedBrowsers(final UIStructurBrowser _structBrowser)
    {
        if (isRoot()) {
            this.expandedBrowsers.add(_structBrowser);
        } else {
            getParentBrws().add2ExpandedBrowsers(_structBrowser);
        }
    }

    /**
     * Method to sort the data of this model. It calls an esjp for sorting.
     */
    protected void sortModel()
    {
        setExecutionStatus(UIStructurBrowser.ExecutionStatus.SORT);
        try {
            getObject4Event().executeEvents(EventType.UI_TABLE_EVALUATE, ParameterValues.CLASS, this);

            if (getSortDirection() == SortDirection.DESCENDING) {
                Collections.reverse(this.children);
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * Method to sort this model and all child models.
     */
    @Override
    public void sort()
    {
        sortModel();
        for (final UIStructurBrowser child : this.children) {
            child.sort();
        }
    }

    /**
     * Getter method for the instance variable {@link #forceExpanded}.
     *
     * @return value of instance variable {@link #forceExpanded}
     */
    public boolean isForceExpanded()
    {
        return this.forceExpanded;
    }

    /**
     * Setter method for instance variable {@link #forceExpanded}.
     *
     * @param _forceExpanded value for instance variable {@link #forceExpanded}
     */
    protected void setForceExpanded(final boolean _forceExpanded)
    {
        this.forceExpanded = _forceExpanded;
    }

    /**
     * Getter method for the instance variable {@link #allowChildren}.
     *
     * @return value of instance variable {@link #allowChildren}
     */
    public boolean isAllowChildren()
    {
        return this.allowChildren;
    }


    /**
     * Setter method for instance variable {@link #allowChildren}.
     *
     * @param _allowChildren value for instance variable {@link #allowChildren}
     */

    public void setAllowChildren(final boolean _allowChildren)
    {
        this.allowChildren = _allowChildren;
    }

    /**
     * Has this StructurBrowserModel childs.
     *
     * @return true if has children, else false
     */
    public boolean hasChildren()
    {
        return !this.children.isEmpty();
    }

    /**
     * Getter method for instance variable {@link #children}.
     *
     * @return value of instance variable {@link #children}
     */
    public List<UIStructurBrowser> getChildren()
    {
        return this.children;
    }

    /**
     * Getter method for the instance variable {@link #allowItems}.
     *
     * @return value of instance variable {@link #allowItems}
     */
    public boolean isAllowItems()
    {
        return this.allowItems;
    }

    /**
     * Setter method for instance variable {@link #allowItems}.
     *
     * @param _allowItems value for instance variable {@link #allowItems}
     */
    public void setAllowItems(final boolean _allowItems)
    {
        this.allowItems = _allowItems;
    }

    /**
     * Method used to evaluate the type for this table from the connected
     * events.
     *
     * @return type if found
     * @throws EFapsException on error
     */
    protected Type getTypeFromEvent()
        throws EFapsException
    {
        final List<EventDefinition> events =  getObject4Event().getEvents(EventType.UI_TABLE_EVALUATE);
        String typeName = null;
        if (events.size() > 1) {
            throw new EFapsException(this.getClass(), "execute4NoInstance.moreThanOneEvaluate");
        } else {
            final EventDefinition event = events.get(0);
            if (event.getProperty("Types") != null) {
                typeName = event.getProperty("Types").split(";")[0];
            }
        }
        return Type.get(typeName);
    }

    /**
     * Method is called from the StructurBrowser in edit mode before rendering
     * the columns for row to be able to hide the columns for different rows by
     * setting the cell model to hide.
     */
    public void checkHideColumn4Row()
    {
        setExecutionStatus(UIStructurBrowser.ExecutionStatus.CHECKHIDECOLUMN4ROW);
        try {
            getObject4Event().executeEvents(EventType.UI_TABLE_EVALUATE,
                            ParameterValues.INSTANCE, getInstance(),
                            ParameterValues.CLASS, this);
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * This method should be called to add children to a Node in the Tree.<br>
     * e.g. in a standard implementation the children would be added to the Tree
     * on the expand-Event of the tree. The children a retrieved from an esjp
     * with the EventType UI_TABLE_EVALUATE.
     */
    @SuppressWarnings("unchecked")
    public void addChildren()
    {
        setExecutionStatus(UIStructurBrowser.ExecutionStatus.ADDCHILDREN);
        List<Return> ret;
        try {
            ret = getObject4Event().executeEvents(EventType.UI_TABLE_EVALUATE, ParameterValues.INSTANCE, getInstance(),
                            ParameterValues.CLASS, this);
            final Map<Instance, Boolean> map = (Map<Instance, Boolean>) ret.get(0).get(ReturnValues.VALUES);

            if (getTableUUID() == null) {
                executeTree(map, false);
            } else {
                executeTreeTable(map, false);
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * This method is used to check if a node has potential children.
     *
     * @param _instance Instance of a Node to be checked
     * @return true if this Node has children, else false
     */
    protected boolean checkForAllowChildren(final Instance _instance)
    {
        setExecutionStatus(UIStructurBrowser.ExecutionStatus.ALLOWSCHILDREN);
        try {
            final List<Return> ret = getObject4Event().executeEvents(EventType.UI_TABLE_EVALUATE,
                            ParameterValues.INSTANCE, _instance,
                            ParameterValues.CLASS, this);
            return ret.isEmpty() ? false : ret.get(0).get(ReturnValues.TRUE) != null;
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * This method is used to execute a listener with a specific event.
     * @param _status status to be executed
     * @param _uiID2Oid UI Id 2 Oid mapping
     */
    public void executeListener(final ExecutionStatus _status,
                                final Map<String, String> _uiID2Oid)
    {
        setExecutionStatus(_status);
        try {
            getObject4Event().executeEvents(EventType.UI_TABLE_EVALUATE,
                            ParameterValues.INSTANCE, getInstance(),
                            ParameterValues.CLASS, this,
                            ParameterValues.OIDMAP4UI,  _uiID2Oid);
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * This method is used to check if a node has potential children.
     *
     * @param _instance Instance of a Node to be checked
     * @return true if this Node has children, else false
     */
    public boolean checkForAllowItems(final Instance _instance)
    {
        setExecutionStatus(UIStructurBrowser.ExecutionStatus.ALLOWSITEM);
        try {
            final List<Return> ret = getObject4Event().executeEvents(EventType.UI_TABLE_EVALUATE,
                            ParameterValues.INSTANCE, _instance,
                            ParameterValues.CLASS, this);
            return ret.isEmpty() ? false : ret.get(0) == null ? false : ret.get(0).get(ReturnValues.TRUE) != null;
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * This method is used to check if a node has potential children.
     *
     * @param _instance Instance of a Node to be checked
     * @return true if this Node has children, else false
     */
    protected boolean checkForChildren(final Instance _instance)
    {
        setExecutionStatus(UIStructurBrowser.ExecutionStatus.CHECKFORCHILDREN);
        try {
            final List<Return> ret = getObject4Event().executeEvents(EventType.UI_TABLE_EVALUATE,
                            ParameterValues.INSTANCE, _instance,
                            ParameterValues.CLASS, this);
            return ret.isEmpty() ? false : ret.get(0).get(ReturnValues.TRUE) != null;
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * Method is called from the StructurBrowser in edit mode from
     * {@link #setValuesFromUI(Map, DefaultMutableTreeNode)} to get
     * additional JavaScript to be appended to the AjaxTarget.
     * @param _parameters   Parameter as send from the UserInterface
     * @return JavaScript for the UserInterface
     */
    protected String getJavaScript4Target(final Map<String, String[]> _parameters)
    {
        String ret;
        setExecutionStatus(UIStructurBrowser.ExecutionStatus.GETJAVASCRIPT4TARGET);
        try {
            final List<Return> retList = getObject4Event().executeEvents(EventType.UI_TABLE_EVALUATE,
                                ParameterValues.INSTANCE, getInstance(),
                                ParameterValues.CLASS, this,
                                ParameterValues.PARAMETERS, _parameters);
            ret = (String) retList.get(0).get(ReturnValues.SNIPLETT);
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        return ret;
    }

    /**
     * This is the getter method for the instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public boolean isParent()
    {
        return this.parent;
    }

    /**
     * This is the setter method for the instance variable {@link #parent}.
     *
     * @param _parent the parent to set
     */
    public void setParent(final boolean _parent)
    {
        this.parent = _parent;
    }

    /**
     * Method to reset the Model.
     *
     * @see org.efaps.ui.wicket.models.AbstractModel#resetModel()
     */
    @Override
    public void resetModel()
    {
        super.setInitialized(false);
        getHeaders().clear();
        this.children.clear();
    }

    /**
     * @param _parameters   Parameter as send from the UserInterface
     * @param _node         Node the _parameters were send from
     * @throws EFapsException on error
     * @return JavaScript for the UserInterface
     */
    public String setValuesFromUI(final Map<String, String[]> _parameters,
                                  final DefaultMutableTreeNode _node)
        throws EFapsException
    {
        final Enumeration<?> preOrdEnum = ((DefaultMutableTreeNode) _node.getRoot()).preorderEnumeration();
        int i = 0;
        while (preOrdEnum.hasMoreElements()) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) preOrdEnum.nextElement();
            if (!node.isRoot()) {
                final UIStructurBrowser uiObject = (UIStructurBrowser) node.getUserObject();
                if (uiObject != null) {
                    for (final UIStructurBrowserTableCell cell : uiObject.getColumns()) {
                        final String[] values = _parameters.get(cell.getName());
                        if (cell.isAutoComplete()) {
                            final String[] autoValues = _parameters.get(cell.getName() + "AutoComplete");
                            cell.setCellTitle(autoValues[i]);
                            cell.setInstanceKey(values[i]);
                        } else {
                            if (values != null) {
                                cell.setValueFromUI(values[i]);
                            }
                        }
                    }
                    i++;
                }
            }
        }
        return getJavaScript4Target(_parameters);
    }

    /**
     * Get the Value of a Column identified by the index of the Column.
     *
     * @param _index index of the Column
     * @return String with the Value of the Column
     */
    public UIStructurBrowserTableCell getColumnValue(final int _index)
    {
        return this.columns.isEmpty() ? null : this.columns.get(_index);
    }

    /**
     * This is the setter method for the instance variable {@link #label}.
     *
     * @param _label the label to set
     */
    private void setLabel(final String _label)
    {
        this.label = _label;
    }

    /**
     * This is the getter method for the instance variable {@link #columns}.
     *
     * @return value of instance variable {@link #columns}
     */
    public List<UIStructurBrowserTableCell> getColumns()
    {
        return this.columns;
    }

    /**
     * Setter method for instance variable {@link #browserFieldName}.
     *
     * @param _browserFieldName value for instance variable {@link #browserFieldName}
     */

    protected void setBrowserFieldName(final String _browserFieldName)
    {
        this.browserFieldName = _browserFieldName;
    }

    /**
     * Setter method for instance variable {@link #browserFieldIndex}.
     *
     * @param _browserFieldIndex value for instance variable {@link #browserFieldIndex}
     */

    protected void setBrowserFieldIndex(final int _browserFieldIndex)
    {
        this.browserFieldIndex = _browserFieldIndex;
    }

    /**
     * Getter method for the instance variable {@link #browserFieldIndex}.
     *
     * @return value of instance variable {@link #browserFieldIndex}
     */
    public int getBrowserFieldIndex()
    {
        return this.browserFieldIndex;
    }

    /**
     * This is the getter method for the instance variable
     * {@link #browserFieldName}.
     *
     * @return value of instance variable {@link #browserFieldName}
     */
    public String getBrowserFieldName()
    {
        return this.browserFieldName;
    }

    /**
     * This is the getter method for the instance variable {@link #image}.
     *
     * @return value of instance variable {@link #image}
     */

    public String getImage()
    {
        return this.image;
    }

    /**
     * This is the setter method for the instance variable {@link #image}.
     *
     * @param _url the url of the image to set
     */
    private void setImage(final String _url)
    {
        if (_url != null) {
            this.image = RequestHandler.replaceMacrosInUrl(_url);
        }
    }

    /**
     * This is the getter method for the instance variable {@link #direction}.
     *
     * @return value of instance variable {@link #direction}
     */
    public Boolean getDirection()
    {
        return this.direction;
    }

    /**
     * This is the setter method for the instance variable {@link #direction}.
     *
     * @param _direction the direction to set
     */
    public void setDirection(final Boolean _direction)
    {
        this.direction = _direction;
    }

    /**
     * Getter method for instance variable {@link #executionStatus}.
     *
     * @return value of instance variable {@link #executionStatus}
     */
    public ExecutionStatus getExecutionStatus()
    {
        return this.executionStatus;
    }

    /**
     * This method is updating the Label, by querying the eFaps-DataBase.
     */
    public void requeryLabel()
    {
        try {
            final ValueParser parser = new ValueParser(new StringReader(this.valueLabel));
            final ValueList valList = parser.ExpressionString();
            final PrintQuery print = new PrintQuery(getInstance());
            valList.makeSelect(print);
            if (print.execute()) {
                setLabel(valList.makeString(getInstance(), print, getMode()).toString());
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        } catch (final ParseException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * Getter method for instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     */
    public String getLabel()
    {
        return this.label;
    }



    /**
     * Setter method for instance variable {@link #sortDirection} and for all
     * children also.
     *
     * @param _sortDirection value for instance variable {@link #sortDirection}
     */
    @Override
    public void setSortDirection(final SortDirection _sortDirection)
    {
        super.setSortDirection(_sortDirection);
        for (final UIStructurBrowser child : this.children) {
            child.setSortDirectionInternal(_sortDirection);
        }
    }

    /**
     * Getter method for instance variable {@link #expanded}.
     *
     * @return value of instance variable {@link #expanded}
     */
    public boolean isExpanded()
    {
        return this.expanded;
    }

    /**
     * Setter method for instance variable {@link #expanded}.
     *
     * @param _expanded value for instance variable {@link #expanded}
     */
    public void setExpanded(final boolean _expanded)
    {
        this.expanded = _expanded;
        storeInSession();
    }

    /**
     * This method generates the Key for a UserAttribute by using the UUID of
     * the Command and the given static part, so that for every StruturBrowser a
     * unique key for expand etc, is created.
     *
     * @return String with the key
     */
    public String getCacheKey()
    {
        return super.getCommandUUID() + "-" + UIStructurBrowser.USERSESSIONKEY;
    }

    /**
     * Store the Information in the Session.
     */
    @SuppressWarnings("unchecked")
    private void storeInSession()
    {
        try {
            if (!getMode().equals(TargetMode.CREATE)) {
                final Map<String, Boolean> sessMap;
                if (Context.getThreadContext().containsSessionAttribute(getCacheKey())) {
                    sessMap = (Map<String, Boolean>) Context.getThreadContext().getSessionAttribute(getCacheKey());
                } else {
                    sessMap = new HashMap<String, Boolean>();
                }
                sessMap.put(getInstanceKey(), isExpanded());
                Context.getThreadContext().setSessionAttribute(getCacheKey(), sessMap);
            }
        } catch (final EFapsException e) {
            UIStructurBrowser.LOG.error("Error storing Session info for StruturBrowser called by Command with UUID: {}",
                            getCommandUUID(), e);
        }
    }

    /**
     * Setter method for instance variable {@link #executionStatus}.
     *
     * @param _executionStatus value for instance variable {@link #executionStatus}
     */
    protected void setExecutionStatus(final ExecutionStatus _executionStatus)
    {
        this.executionStatus = _executionStatus;
    }

    /**
     * Get the Admin Object that contains the events that must be executed.
     *
     * @return the Admin Object that contains the events to be executed
     */
    protected AbstractAdminObject getObject4Event()
    {
        return this.getCommand();
    }

    /**
     * Getter method for the instance variable {@link #root}.
     *
     * @return value of instance variable {@link #root}
     */
    public boolean isRoot()
    {
        return this.root;
    }

    /**
     * Getter method for the instance variable {@link #parentBrws}.
     *
     * @return value of instance variable {@link #parentBrws}
     */
    public UIStructurBrowser getParentBrws()
    {
        return this.parentBrws;
    }

    /**
     * Setter method for instance variable {@link #parentBrws}.
     *
     * @param _parentBrws value for instance variable {@link #parentBrws}
     */
    protected void setParentBrws(final UIStructurBrowser _parentBrws)
    {
        this.parentBrws = _parentBrws;
    }

    /**
     * Getter method for the instance variable {@link #level}.
     *
     * @return value of instance variable {@link #level}
     */
    public int getLevel()
    {
        return this.level;
    }

    /**
     * Setter method for instance variable {@link #level}.
     *
     * @param _level value for instance variable {@link #level}
     */
    protected void setLevel(final int _level)
    {
        this.level = _level;
    }

    /**
     * (non-Javadoc).
     *
     * @see org.apache.wicket.model.Model#toString()
     * @return label
     */
    @Override
    public String toString()
    {
        return this.label;
    }
}
