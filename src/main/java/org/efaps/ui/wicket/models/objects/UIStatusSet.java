/*
 * Copyright 2003 - 2013 The eFaps Team
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.util.io.IClusterable;
import org.efaps.admin.datamodel.Status;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIStatusSet
    implements IClusterable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Key that groups the given Status.
     */
    private final String key;

    /**
     * ids of the Status belonging to this StatusSet.
     */
    private final List<Long> ids = new ArrayList<Long>();

    /**
     * Labels of the Status.
     */
    private final Set<String> labels = new TreeSet<String>();


    /**
     * @param _status Status to be added
     */
    public UIStatusSet(final Status _status)
    {
        this.key = _status.getKey();
        this.ids.add(_status.getId());
        this.labels.add(_status.getLabel());
    }

    /**
     * @param _status Status to add
     */
    private void addStatus(final Status _status)
    {
        this.ids.add(_status.getId());
        this.labels.add(_status.getLabel());
    }

    /**
     * @return the first id of the ids or Zero
     */
    public Long getSelectedId()
    {
        return this.ids.isEmpty() ? Long.valueOf(0) : this.ids.iterator().next();
    }

    /**
     * @return the label for this StatusSet
     */
    public String getLabel()
    {
        final StringBuilder ret = new StringBuilder();
        for (final String label : this.labels) {
            if (ret.length() > 0) {
                ret.append(", ");
            }
            ret.append(label);
        }
        return ret.toString();
    }

    /**
     * @return the list for ids
     */
    public Collection<? extends Object> getIds()
    {
        return this.ids;
    }


    /**
     * Getter method for the instance variable {@link #key}.
     *
     * @return value of instance variable {@link #key}
     */
    public String getKey()
    {
        return this.key;
    }

    /**
     * @param _statusList List of Status the List of UIStatusSet is wanted for
     * @return List of UIStatusSet
     */
    public static List<UIStatusSet> getUIStatusSet4List(final List<Status> _statusList)
    {
        final List<UIStatusSet> ret = new ArrayList<UIStatusSet>();
        final Map<String, UIStatusSet> values = new HashMap<String, UIStatusSet>();
        for (final Status status : _statusList) {
            if (values.containsKey(status.getKey())) {
                values.get(status.getKey()).addStatus(status);
            } else {
                values.put(status.getKey(), new UIStatusSet(status));
            }
        }
        ret.addAll(values.values());
        return ret;
    }

}
