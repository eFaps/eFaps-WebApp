/*
 * Copyright 2003 - 2011 The eFaps Team
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

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
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
    public static enum UserCacheKey {
        /**
         * Key for UserAttributes used for the order of Columns.
         */
        COLUMNORDER("columnOrder"),
        /**
         * Key  for UserAttributes used for the widths of Columns.
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
     * The size of the current values list including filtereing etc. Update on filter events etc.
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
    private  String sortKey = null;

    /**
     * @param _commandUUID
     * @param _instanceKey
     * @param _openerId
     */
    public AbstractUIHeaderObject(final UUID _commandUUID,
                                  final String _instanceKey,
                                  final String _openerId)
    {
        super(_commandUUID, _instanceKey, _openerId);
    }

    /**
     * @param _parameters
     */
    public AbstractUIHeaderObject(final PageParameters _parameters)
    {
        super(_parameters);
    }

    /**
     * @param _commandUUID
     * @param _instanceKey
     */
    public AbstractUIHeaderObject(final UUID _commandUUID,
                                  final String _instanceKey)
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

    protected void setSortKeyInternal(final String _sortKey)
    {
        this.sortKey = _sortKey;
    }
}
