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


package org.efaps.ui.wicket.models.cell;

import java.io.File;
import java.util.List;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.field.FieldChart;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIFormCellChart
    extends UIFormCell
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The image file for this cell.
     */
    private File image;

    /**
     * The html snipplet returned form the esjp.
     */
    private String snipplet;

    /**
     * Constructor.
     *
     * @param _parent   parent ui object
     * @param _field    FieldValue of the Cell
     * @param _instance Instance of the field
     * @param _label Label for the Cell
     * @throws EFapsException on error
     */
    public UIFormCellChart(final AbstractUIObject _parent,
                           final FieldChart _field,
                           final Instance _instance,
                           final String _label)
        throws EFapsException
    {
        super(_parent, new FieldValue(_field, null, null, _instance, null), _instance, null, null, null, _label, null);
    }

    /**
     * Initialize the object.
     * @throws EFapsException on error
     */
    public void initialize()
        throws EFapsException
    {
        final List<Return> rets = executeEvents(EventType.UI_FIELD_VALUE, null, null);
        if (rets != null && !rets.isEmpty()) {
            for (final Return ret : rets) {
                if (ret.contains(ReturnValues.VALUES)) {
                    this.image = (File) ret.get(ReturnValues.VALUES);
                }
                if (ret.contains(ReturnValues.SNIPLETT)) {
                    this.snipplet = (String) ret.get(ReturnValues.SNIPLETT);
                }
            }
        }
    }

    /**
     * @return has this cell a snipplet
     */
    public boolean hasSnipllet()
    {
        return this.snipplet != null && !this.snipplet.isEmpty();
    }

    /**
     * @return contains the snipplet a map for the image
     */
    public boolean hasMap()
    {
        return hasSnipllet() && this.snipplet.contains(EFapsKey.CHARTMAPPOSTFIX.getKey());
    }
    /**
     * Getter method for the instance variable {@link #image}.
     *
     * @return value of instance variable {@link #image}
     */
    public File getImage()
    {
        return this.image;
    }

    /**
     * Getter method for the instance variable {@link #snipplet}.
     *
     * @return value of instance variable {@link #snipplet}
     */
    public String getSnipplet()
    {
        return this.snipplet;
    }
}
