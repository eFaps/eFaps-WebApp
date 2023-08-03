/*
 * Copyright 2003 - 2014 The eFaps Team
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

import org.apache.wicket.util.io.IClusterable;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.field.FieldHeading;

/**
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIHeading
    implements IFormElement, IClusterable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * instance variable to store the level of the Heading.
     */
    private final int level;

    /**
     * Label.
     */
    private final String label;

    /**
     * Boolean used as a tristate.
     */
    private final Boolean collapsed;

    /**
     * Name.
     */
    private final String name;


    /**
     * @param _label label to use
     */
    public UIHeading(final String _label)
    {
        this.label = _label;
        this.level = 0;
        this.collapsed = null;
        this.name = null;
    }

    /**
     * @param _heading Heading
     */
    public UIHeading(final FieldHeading _heading)
    {
        this.label = DBProperties.getProperty(_heading.getLabel());
        this.level = _heading.getLevel();
        this.name = _heading.getName();
        if (_heading.getProperty("Collapsed") == null) {
            this.collapsed = null;
        } else {
            this.collapsed = Boolean.valueOf(_heading.getProperty("Collapsed"));
        }
    }

    /**
     * This is the getter method for the instance variable {@link #level}.
     *
     * @return value of instance variable {@link #level}
     */
    public int getLevel()
    {
        return this.level;
    }

    /**
     * This is the getter method for the instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * @return is the heading collapsible
     */
    public boolean isCollapsible()
    {
        return this.collapsed != null;
    }

    /**
     * Getter method for the instance variable {@link #collapsed}.
     *
     * @return value of instance variable {@link #collapsed}
     */
    public Boolean getCollapsed()
    {
        return this.collapsed;
    }

    /**
     * Getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName()
    {
        return this.name;
    }
}
