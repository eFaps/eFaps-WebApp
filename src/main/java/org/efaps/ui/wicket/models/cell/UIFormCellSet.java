/*
 * Copyright 2003 - 2009 The eFaps Team
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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment.
 *
 * @author The eFaswp Team
 * @version $Id$
 */
public class UIFormCellSet
    extends UIFormCell
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * IS this UIFormCellSet inedit mode.
     */
    private final boolean editMode;

    /**
     * index number for a new row.
     */
    private int newCount = 0;

    /**
     * Mapping of x-coordinate to y-coordinate 2 value.
     */
    private final Map<Integer, Map<Integer, String>> xy2value = new TreeMap<Integer, Map<Integer, String>>();

    /**
     * Mapping of x-coordinate to a FormCell Object used as a definition.
     */
    private final Map<Integer, UIFormCell> x2definition = new TreeMap<Integer, UIFormCell>();


    /**
     * Mapping y-coordinate to an instance.
     */
    private final Map<Integer, Instance> y2Instance = new HashMap<Integer, Instance>();


    /**
     * @param _parent       parent object
     * @param _fieldValue   FieldValue
     * @param _instance     instance
     * @param _value        value
     * @param _icon         icon
     * @param _label        Label
     * @param _edit         edit mode or not
     * @throws EFapsException   on error
     */
    public UIFormCellSet(final AbstractUIObject _parent,
                         final FieldValue _fieldValue,
                         final Instance _instance,
                         final String _value,
                         final String _icon,
                         final String _label,
                         final boolean _edit)
        throws EFapsException
    {
        super(_parent, _fieldValue, _instance, _value, null, _icon, _label, "");
        this.editMode = _edit;
    }

    /**
     * @return new index number for the y-coordinate
     */
    public int getNewCount()
    {
        return this.newCount++;
    }

    /**
     * @return is this edit mode
     */
    public boolean isEditMode()
    {
        return this.editMode;
    }


    /**
     * @param _xCoord   x-coordinate
     * @param _yCoord   y-coordinate
     * @param _value    Value
     */
    public void add(final int _xCoord,
                    final int _yCoord,
                    final String _value)
    {
        Map<Integer, String> xmap = this.xy2value.get(_xCoord);
        if (xmap == null) {
            xmap = new TreeMap<Integer, String>();
            this.xy2value.put(_xCoord, xmap);
        }
        xmap.put(_yCoord, _value);
    }

    /**
     * @return size of y
     */
    public int getYsize()
    {
        final Map<Integer, String> xmap = this.xy2value.get(0);
        int ret = 0;
        if (xmap != null) {
            ret = xmap.size();
        }
        return ret;
    }

    /**
     * @return size of x
     */
    public int getXsize()
    {
        return this.xy2value.size();
    }

    /**
     * @param _xCoord   x-coordinate
     * @param _yCoord   y-coordinate
     * @return value
     */
    public String getXYValue(final int _xCoord,
                             final int _yCoord)
    {
        String ret = null;
        final Map<Integer, String> xmap = this.xy2value.get(_xCoord);
        if (xmap != null) {
            ret = xmap.get(_yCoord);
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #x2definition}.
     *
     * @return value of instance variable {@link #x2definition}
     */
    public Map<Integer, UIFormCell> getX2definition()
    {
        return this.x2definition;
    }

    /**
     * @param _xCoord       x-coordinate
     * @param _uiFormCell   UIFormCell used as definition
     * @throws CacheReloadException on error
     */
    public void addDefiniton(final int _xCoord,
                             final UIFormCell _uiFormCell)
        throws CacheReloadException
    {
        if ("LinkWithRanges".equals(_uiFormCell.getTypeName())) {
            _uiFormCell.setAutoComplete(false);
        }
        this.x2definition.put(_xCoord, _uiFormCell);
    }

    /**
     *
     * @param _xCoord  x-coordinate the definition is wanted fot
     * @return  UIFormCell used as defintion
     */
    public UIFormCell getDefinition(final int _xCoord)
    {
        return this.x2definition.get(_xCoord);
    }



    /**
     * Ad an instance to an y-coordinate.
     * @param _yCoord       y-coordinate
     * @param _instance     Instance
     */
    public void addInstance(final int _yCoord,
                            final Instance _instance)
    {
        this.y2Instance.put(_yCoord, _instance);
    }

    /**
     * @param _yCoord       y-coordinate
     * @return Instance
     */
    public Instance getInstance(final int _yCoord)
    {
        return this.y2Instance.get(_yCoord);
    }
}
