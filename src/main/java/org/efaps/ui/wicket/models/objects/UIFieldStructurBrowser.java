/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.models.objects;

import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field.Display;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object used for a StructurBrowser nested in a Form.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIFieldStructurBrowser
    extends UIStructurBrowser
    implements IFormElement
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UIFieldStructurBrowser.class);


    /**
     * Id of the Field this Field Structur Browser belongs to.
     */
    private final Long fieldTabelId;

    /**
     * @param _commandUUID  uuid of the calling command
     * @param _instanceKey  key to the instance
     * @param _field        Field
     * @throws EFapsException on error
     */
    public UIFieldStructurBrowser(final UUID _commandUUID,
                                  final String _instanceKey,
                                  final FieldTable _field)
        throws EFapsException
    {
        super(_commandUUID, _instanceKey);
        setTableUUID(_field.getTargetTable().getUUID());
        setBrowserFieldName(_field.getTargetStructurBrowserField());
        this.fieldTabelId = _field.getId();
        setShowCheckBoxes(_field.isTargetShowCheckBoxes());
        setForceExpanded(_field.isTargetStructurBrowserForceExpand());
    }

    /**
     * @param _commandUUID  uuid of the calling command
     * @param _instanceKey  key to the instance
     * @param _sortdirection sort direction
     * @param _field        Field
     * @throws EFapsException on error
     */
    private UIFieldStructurBrowser(final UUID _commandUUID,
                                   final String _instanceKey,
                                   final SortDirection _sortdirection,
                                   final FieldTable _field)
        throws EFapsException
    {
        super(_commandUUID, _instanceKey, false, _sortdirection);
        setTableUUID(_field.getTargetTable().getUUID());
        setBrowserFieldName(_field.getTargetStructurBrowserField());
        this.fieldTabelId = _field.getId();
        setShowCheckBoxes(_field.isTargetShowCheckBoxes());
        setForceExpanded(_field.isTargetStructurBrowserForceExpand());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UIStructurBrowser getNewStructurBrowser(final Instance _instance,
                                                      final UIStructurBrowser _parent)
        throws EFapsException
    {
        final FieldTable field = FieldTable.get(((UIFieldStructurBrowser) _parent).getFieldTabelId());
        final UIFieldStructurBrowser ret = new UIFieldStructurBrowser(_parent.getCommandUUID(), _instance == null
                        ? null : _instance.getKey(), _parent.getSortDirection(), field);
        ret.setParentBrws(this);
        ret.setLevel(getLevel() + 1);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialise()
        throws EFapsException
    {
        if (getCommand() == null) {
            super.initialise();
        } else {
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
                UIFieldStructurBrowser.LOG.error("error during the retrieve of UserAttributes", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractAdminObject getObject4Event()
    {
        return FieldTable.get(this.getFieldTabelId());
    }

    /**
     * Get the Display of the Field this StructurBrowser is bedded in.
     * @param _mode target mode the display will be evaluate for
     * @return Display
     */
    public Display getFieldDisplay(final TargetMode _mode)
    {
        return FieldTable.get(this.getFieldTabelId()).getDisplay(_mode);
    }

    /**
     * In create or edit mode this StructurBrowser is editable,
     * if the Field this StructurBroeser is bedded in is editable also.
     *
     * @return is this StructurBrowser editable.
     */
    @Override
    public boolean isEditable()
    {
        return getFieldDisplay(getMode()).equals(Display.EDITABLE) && (isCreateMode() || isEditMode());
    }

    @Override
    public String getCacheKey()
    {
        return super.getCommandUUID() + "-" + this.getFieldTabelId() + "-" + UIStructurBrowser.USERSESSIONKEY;
    }

    /**
     * Gets the id of the Field this Field Structur Browser belongs to.
     *
     * @return the id of the Field this Field Structur Browser belongs to
     */
    public Long getFieldTabelId()
    {
        return this.fieldTabelId;
    }
}
