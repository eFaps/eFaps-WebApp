/*
 * Copyright 2003 - 2016 The eFaps Team
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


package org.efaps.ui.wicket.behaviors.dojo;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IReferenceHeaderItem;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.ResourceReference;
import org.efaps.ui.wicket.util.DojoClass;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class RequireHeaderItem
    extends HeaderItem
    implements IReferenceHeaderItem
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The classes. */
    private final Set<DojoClass> dojoClasses = new HashSet<>();

    /**
     * @param _dojoClasses
     */
    public RequireHeaderItem(final DojoClass... _dojoClasses)
    {
        if (ArrayUtils.isNotEmpty(_dojoClasses)) {
            CollectionUtils.addAll(this.dojoClasses, _dojoClasses);
        }
    }

    @Override
    public ResourceReference getReference()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<?> getRenderTokens()
    {
        return getDojoClasses();
    }

    @Override
    public void render(final Response _response)
    {
    }

    @Override
    public boolean equals(final Object _obj)
    {
        boolean ret;
        if (_obj instanceof RequireHeaderItem) {
            ret = ((RequireHeaderItem) _obj).getDojoClasses().equals(getDojoClasses());
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return getDojoClasses().hashCode();
    }

    /**
     * Gets the dojo classes.
     *
     * @return the dojo classes
     */
    public Set<DojoClass> getDojoClasses()
    {
        return this.dojoClasses;
    }

    @Override
    public String toString()
    {
        return "RequireHeaderItem(" + getDojoClasses() + ")";
    }

    /**
     * @param _javaScript Javascript for the header to add
     * @return new OnDojoReadyHeaderItem
     */
    public static RequireHeaderItem forClasses(final DojoClass... _dojoClasses)
    {
        return new RequireHeaderItem(_dojoClasses);
    }
}
