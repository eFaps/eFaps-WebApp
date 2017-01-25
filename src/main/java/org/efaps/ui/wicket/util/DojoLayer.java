/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Enum DojoLayer.
 *
 * @author The eFaps Team
 */
public enum DojoLayer
{

    /** The GridXLayer. */
    GridXLayer("efaps/gridxLayer",  DojoClasses.array, DojoClasses.lang, DojoClasses.json, DojoClasses.aspect,
                    DojoClasses.query, DojoClasses.domConstruct, DojoClasses.win, DojoClasses.domStyle,
                    DojoClasses.ready, DojoClasses.on, DojoClasses.registry, DojoClasses.Memory,
                    DojoClasses.Grid, DojoClasses.VirtualVScroller, DojoClasses.ColumnResizer,
                    DojoClasses.HScroller, DojoClasses.SingleSort, DojoClasses.SyncCache,
                    DojoClasses.HeaderDialog, DojoClasses.MoveColumn, DojoClasses.SelectColumn,
                    DojoClasses.SelectCell, DojoClasses.DnDColumn, DojoClasses.HiddenColumns,
                    DojoClasses.GridConfig, DojoClasses.GridSort, DojoClasses.Summary,
                    DojoClasses.GridQuickFilter, DojoClasses.Bar, DojoClasses.Persist, DojoClasses.Filter,
                    DojoClasses.FilterBar, DojoClasses.DropDownButton, DojoClasses.TextBox, DojoClasses.TooltipDialog,
                    DojoClasses.ready, DojoClasses.domGeom, DojoClasses.ColumnLock, DojoClasses.MenuBar,
                    DojoClasses.IndirectSelect, DojoClasses.RowHeader, DojoClasses.SelectRow,
                    DojoClasses.MenuBar, DojoClasses.DropDownMenu, DojoClasses.MenuItem,
                    DojoClasses.PopupMenuBarItem, DojoClasses.MenuBarItem),

    /** The Base layer. */
    BaseLayer("efaps/baseLayer", DojoClasses.BackgroundIframe, DojoClasses.DropDownMenu, DojoClasses.MenuBar,
                    DojoClasses.MenuBarItem, DojoClasses.MenuItem, DojoClasses.PopupMenuBarItem,
                    DojoClasses.PopupMenuItem, DojoClasses.BorderContainer, DojoClasses.ContentPane,
                    DojoClasses.parser, DojoClasses.ready);

    /** The name. */
    private final String name;

    /** The classes. */
    private final Set<DojoClass> dojoClasses;

    /**
     * Instantiates a new dojo layer.
     *
     * @param _name the name
     * @param _classes the classes
     */
    DojoLayer(final String _name,
              final DojoClass... _classes)
    {
        this.name = _name;
        this.dojoClasses = Arrays.stream(_classes).collect(Collectors.toSet());
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

    /**
     * Getter method for the instance variable {@link #dojoClasses}.
     *
     * @return value of instance variable {@link #dojoClasses}
     */
    public Set<DojoClass> getDojoClasses()
    {
        return this.dojoClasses;
    }
}
