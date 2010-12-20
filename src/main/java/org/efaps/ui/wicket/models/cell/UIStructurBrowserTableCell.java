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

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIStructurBrowserTableCell
    extends UITableCell
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Is this cell the browserField.
     */
    private boolean browserField = false;


    /**
     * @param _parent           parent uiObject
     * @param _fieldValue       FieldValue
     * @param _instance         instance
     * @param _cellvalue        value fo the cell
     * @param _cellTitle        title for the cell, if null will be set to _cellvalue
     * @param _icon             icon of the cell
     *
     * @throws EFapsException on error
     */
    public UIStructurBrowserTableCell(final AbstractUIObject _parent,
                                      final FieldValue _fieldValue,
                                      final Instance _instance,
                                      final String _cellvalue,
                                      final String _cellTitle,
                                      final String _icon)
        throws EFapsException
    {
        super(_parent, _fieldValue, _instance, _cellvalue, _cellTitle, _icon);
    }

    /**
     * Getter method for the instance variable {@link #browserField}.
     *
     * @return value of instance variable {@link #browserField}
     */
    public boolean isBrowserField()
    {
        return this.browserField;
    }

    /**
     * Setter method for instance variable {@link #browserField}.
     *
     * @param _browserField value for instance variable {@link #browserField}
     */

    public void setBrowserField(final boolean _browserField)
    {
        this.browserField = _browserField;
    }
}
