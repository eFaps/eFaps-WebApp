/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.ui.wicket.models.cell;

import java.util.UUID;

import org.apache.wicket.IClusterable;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.field.FieldPicker;
import org.efaps.ui.wicket.components.modalwindow.ICmdUIObject;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIPicker
    implements IClusterable, ICmdUIObject

{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * UUID of the command used for this PickerObject.
     */
    private final UUID cmdUUID;

    /**
     * Label of this Picker.
     */
    private final String label;

    /**
     * The parent UIOBject
     */
    private final UITableCell parent;

    /**
     * @param _field    fieldPicker this UIObject belongs to
     * @param _parent   parent field this fieldpicker belongs to
     */
    public UIPicker(final FieldPicker _field,
                    final UITableCell _parent)
    {
        this.cmdUUID = _field.getCommand().getUUID();
        this.label = _field.getCommand().getLabelProperty();
        this.parent = _parent;
    }

    /**
     * Getter method for the instance variable {@link #cmdUUID}.
     *
     * @return value of instance variable {@link #cmdUUID}
     */
    public UUID getCmdUUID()
    {
        return this.cmdUUID;
    }

    /**
     * @return the command underlying this picker
     */
    public Command getCommand()
    {
        return Command.get(this.cmdUUID);
    }

    /**
     * Getter method for the instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * @return the height of the window to be opened
     */
    public int getWindowHeight()
    {
        return getCommand().getWindowHeight();
    }

    /**
     * @return the width of the window to be opened
     */
    public int getWindowWidth()
    {
        return getCommand().getWindowWidth();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInstanceKey()
    {
        return this.parent.getInstanceKey();
    }
}
