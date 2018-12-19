/*
 * Copyright 2003 - 2018 The eFaps Team
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

import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.ui.Table;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.IFormElement;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class UIFieldGrid.
 *
 * @author The eFaps Team
 */
public final class UIFieldGrid
    extends UIGrid
    implements IFormElement
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The field id. */
    private long fieldId;

    /**
     * Instantiates a new UI field grid.
     */
    private UIFieldGrid()
    {
    }

    /**
     * Gets the.
     *
     * @param _commandUUID the command UUID
     * @param _pagePosition the page position
     * @param _callInstance the call instance
     * @param _fieldTable the field table
     * @return the UI grid
     */
    public static UIFieldGrid get(final UUID _commandUUID,
                                  final PagePosition _pagePosition,
                                  final Instance _callInstance,
                                  final FieldTable _fieldTable)
    {
        final UIFieldGrid ret = new UIFieldGrid();
        ret.setFieldId(_fieldTable.getId()).setCmdUUID(_commandUUID).setPagePosition(_pagePosition).setCallInstance(
                        _callInstance);
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #fieldId}.
     *
     * @return value of instance variable {@link #fieldId}
     */
    public long getFieldId()
    {
        return this.fieldId;
    }

    /**
     * Getter method for the instance variable {@link #fieldId}.
     *
     * @return value of instance variable {@link #fieldId}
     */
    public FieldTable getFieldTable()
    {
        return FieldTable.get(this.fieldId);
    }

    /**
     * Setter method for instance variable {@link #fieldId}.
     *
     * @param _fieldId value for instance variable {@link #fieldId}
     * @return the UI field grid
     */
    public UIFieldGrid setFieldId(final long _fieldId)
    {
        this.fieldId = _fieldId;
        return this;
    }

    @Override
    public Table getTable()
        throws CacheReloadException
    {
        return getFieldTable().getTargetTable();
    }

    @Override
    public String getCacheKey(final CacheKey _key)
    {
        return getCmdUUID() + "-" + getFieldTable().getName() + "-" + _key.getValue();
    }

    @Override
    public AbstractAdminObject getEventObject()
        throws CacheReloadException
    {
        return getFieldTable();
    }

    @Override
    public boolean isStructureTree()
        throws CacheReloadException
    {
        return getFieldTable().getTargetStructurBrowserField() != null;
    }

    @Override
    public String getStructurBrowserField()
        throws CacheReloadException
    {
        return getFieldTable().getTargetStructurBrowserField();
    }

    @Override
    public boolean isShowCheckBoxes()
        throws CacheReloadException
    {
        return getFieldTable().isTargetShowCheckBoxes();
    }
}
