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
package org.efaps.ui.wicket.models.objects.grid;

import java.util.HashMap;

import org.efaps.api.ui.IMapFilter;

/**
 * The Class MapFilter.
 *
 * @author The eFaps Team
 */
public class MapFilter
    extends HashMap<String, Object>
    implements IMapFilter
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The field id. */
    private final long fieldId;

    /**
     * Instantiates a new map filter.
     *
     * @param _fieldId the field id
     */
    public MapFilter(final long _fieldId)
    {
        this.fieldId = _fieldId;
    }

    @Override
    public long getFieldId()
    {
        return this.fieldId;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof MapFilter) {
            ret = ((MapFilter) _obj).getFieldId() == this.fieldId && super.equals(_obj);
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode() + Long.valueOf(this.fieldId).intValue();
    }
}
