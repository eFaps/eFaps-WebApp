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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.Table;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractUIHeaderObject
    extends AbstractUIPageObject
{

    /**
     * This enum holds the Values used as part of the key for the UserAttributes
     * or SessionAttribute witch belong to a TableModel.
     */
    public static enum UserCacheKey
    {
        /**
         * Key for UserAttributes used for the order of Columns.
         */
        COLUMNORDER("columnOrder"),
        /**
         * Key for UserAttributes used for the widths of Columns.
         */
        COLUMNWIDTH("columnWidths"),
        /**
         * Key for UserAttributes used for the sort direction.
         */
        SORTDIRECTION("sortDirection"),
        /**
         * Key for UserAttributes used for the Column.
         */
        SORTKEY("sortKey"),
        /**
         * Key for SessionAttribute used for the filter of a table.
         */
        FILTER("filter");

        /**
         * Value of the user attribute.
         */
        private final String value;

        /**
         * Constructor setting the instance variable.
         *
         * @param _value Value
         */
        private UserCacheKey(final String _value)
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
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractUIHeaderObject.class);

    /**
     * The instance Array holds the Label for the Columns.
     */
    private final List<UITableHeader> headers = new ArrayList<UITableHeader>();

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
     * This instance variable sores if the Table should show CheckBodes.
     */
    private boolean showCheckBoxes = false;

    /**
     * This instance variable stores if the Widths of the Columns are set by
     * UserAttributes.
     */
    private boolean userWidths = false;

    /**
     * This instance variable stores the total weight of the widths of the
     * Cells. (Sum of all widths)
     */
    private int widthWeight;

    /**
     * The size of the current values list including filtereing etc. Update on
     * filter events etc.
     */
    private int size;

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
     * @param _commandUUID UUID of the Command
     * @param _instanceKey key to the instance
     * @param _openerId id of the opener
     * @throws CacheReloadException on error
     */
    public AbstractUIHeaderObject(final UUID _commandUUID,
                                  final String _instanceKey,
                                  final String _openerId)
        throws CacheReloadException
    {
        super(_commandUUID, _instanceKey, _openerId);
    }

    /**
     * @param _commandUUID UUID of the Command
     * @param _instanceKey key to the instance
     * @throws CacheReloadException on error
     */
    public AbstractUIHeaderObject(final UUID _commandUUID,
                                  final String _instanceKey)
        throws CacheReloadException
    {
        super(_commandUUID, _instanceKey);
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
     * Getter method for the instance variable {@link #tableUUID}.
     *
     * @return value of instance variable {@link #tableUUID}
     */
    protected UUID getTableUUID()
    {
        return this.tableUUID;
    }

    /**
     * Setter method for instance variable {@link #tableUUID}.
     *
     * @param _tableUUID value for instance variable {@link #tableUUID}
     */

    protected void setTableUUID(final UUID _tableUUID)
    {
        this.tableUUID = _tableUUID;
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
        return getTableUUID() == null ? null : Table.get(getTableUUID());
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
     * In create or edit mode this Table is editable.
     *
     * @return is this Table editable.
     */
    public boolean isEditable()
    {
        return isCreateMode() || isEditMode();
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
     * @param _fieldId id of the
     * @return UITableHeader header
     */
    public UITableHeader getHeader4Id(final long _fieldId)
    {
        UITableHeader ret = null;
        for (final UITableHeader header : this.headers) {
            if (header.getFieldId() == _fieldId) {
                ret = header;
                break;
            }
        }
        return ret;
    }

    /**
     * Are the values of the Rows filtered or not.
     *
     * @return true if filtered, else false
     */
    public boolean isFiltered()
    {
        return false;
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
     * @param _userWidth must the user with be set
     */
    protected void setUserWidth(final boolean _userWidth)
    {
        this.userWidths = _userWidth;
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
     * Setter method for instance variable {@link #widthWeight}.
     *
     * @param _widthWeight value for instance variable {@link #widthWeight}
     */
    protected void setWidthWeight(final int _widthWeight)
    {
        this.widthWeight = _widthWeight;
    }

    /**
     * This method retieves the UserAttribute for the ColumnWidths and evaluates
     * the string.
     *
     * @return List with the values of the columns in Pixel
     */
    protected List<Integer> getUserWidths()
    {
        List<Integer> ret = null;
        try {
            if (Context.getThreadContext().containsUserAttribute(
                            getCacheKey(UITable.UserCacheKey.COLUMNWIDTH))) {
                setUserWidth(true);
                final String widths = Context.getThreadContext().getUserAttribute(
                                getCacheKey(UITable.UserCacheKey.COLUMNWIDTH));

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
            AbstractUIHeaderObject.LOG.error("error during the retrieve of UserAttributes in getUserWidths()", e);
        } catch (final EFapsException e) {
            // we don't throw an error because this are only Usersettings
            AbstractUIHeaderObject.LOG.error("error during the retrieve of UserAttributes in getUserWidths()", e);
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #sortDirection}.
     *
     * @return value of instance variable {@link #sortDirection}
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
            Context.getThreadContext().setUserAttribute(getCacheKey(UserCacheKey.SORTDIRECTION),
                            _sortdirection.getValue());
        } catch (final EFapsException e) {
            // we don't throw an error because this are only Usersettings
            AbstractUIHeaderObject.LOG.error("error during the retrieve of UserAttributes", e);
        }
    }

    /**
     * @param _sortdirection sortdirection
     */
    public void setSortDirectionInternal(final SortDirection _sortdirection)
    {
        this.sortDirection = _sortdirection;
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
        return super.getCommandUUID() + "-" + _key.getValue();
    }

    /**
     * Getter method for the instance variable {@link #size}.
     *
     * @return value of instance variable {@link #size}
     */
    public int getSize()
    {
        return this.size;
    }

    /**
     * Setter method for instance variable {@link #size}.
     *
     * @param _size value for instance variable {@link #size}
     */
    protected void setSize(final int _size)
    {
        this.size = _size;
    }

    /**
     * Getter method for the instance variable {@link #sortKey}.
     *
     * @return value of instance variable {@link #sortKey}
     */
    public String getSortKey()
    {
        return this.sortKey;
    }

    /**
     * Setter method for instance variable {@link #sortKey}.
     *
     * @param _sortKey value for instance variable {@link #sortKey}
     */
    public void setSortKey(final String _sortKey)
    {
        setSortKeyInternal(_sortKey);
        try {
            Context.getThreadContext().setUserAttribute(getCacheKey(UITable.UserCacheKey.SORTKEY),
                            _sortKey);
        } catch (final EFapsException e) {
            // we don't throw an error because this are only Usersettings
            AbstractUIHeaderObject.LOG.error("error during the retrieve of UserAttributes", e);
        }
    }

    /**
     * @param _sortKey sort key
     */
    protected void setSortKeyInternal(final String _sortKey)
    {
        this.sortKey = _sortKey;
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
            for (final UITableHeader header : getHeaders()) {
                if (markupId.equals(header.getMarkupId())) {
                    columnOrder.append(header.getFieldName()).append(";");
                    break;
                }
            }
        }
        try {
            Context.getThreadContext().setUserAttribute(getCacheKey(UITable.UserCacheKey.COLUMNORDER),
                            columnOrder.toString());
        } catch (final EFapsException e) {
            // we don't throw an error because this are only Usersettings
            AbstractUIHeaderObject.LOG.error("error during the setting of UserAttributes", e);
        }
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
    protected List<Field> getUserSortedColumns()
    {
        List<Field> ret = new ArrayList<Field>();
        try {
            final List<Field> fields = getTable().getFields();
            if (Context.getThreadContext().containsUserAttribute(
                            getCacheKey(UITable.UserCacheKey.COLUMNORDER))) {

                final String columnOrder = Context.getThreadContext().getUserAttribute(
                                getCacheKey(UITable.UserCacheKey.COLUMNORDER));

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
            AbstractUIHeaderObject.LOG.debug("Error on sorting columns");
        }
        return ret;
    }

    /**
     * Sort the UIObject;
     *
     * @throws EFapsException on error
     */
    public abstract void sort()
        throws EFapsException;
}
