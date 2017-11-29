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

/**
 * The Enum DojoClasses.
 */
@SuppressWarnings("checkstyle:javadocvariable")
public enum DojoClasses
    implements DojoClass
{

    BackgroundIframe("dijit/BackgroundIframe", "BackgroundIframe"),
    basefx("dojo/_base/fx", "baseFx"),
    Bar("gridx/modules/Bar", "Bar"),
    BorderContainer("dijit/layout/BorderContainer", "BorderContainer"),
    ColumnLock("gridx/modules/ColumnLock", "ColumnLock"),
    ColumnResizer("gridx/modules/ColumnResizer", "ColumnResizer"),
    ContentPane("dijit/layout/ContentPane", " ContentPane"),
    ContentPaneX("dojox/layout/ContentPane", " ContentPane"),
    DateLocale("dojo/date/locale", "dateLocale"),
    DnDColumn("gridx/modules/dnd/Column", "DnDColumn"),
    DnDSource("dojo/dnd/Source", "DnDSource"),
    DropDownButton("dijit/form/DropDownButton", "DropDownButton"),
    DropDownMenu("dijit/DropDownMenu", "DropDownMenu"),
    DialogX("dojox/widget/Dialog", " Dialog"),
    Filter("gridx/modules/Filter", "Filter"),
    FilterBar("gridx/modules/filter/FilterBar", "FilterBar"),
    fx("dojo/fx", "fx"),
    Grid("gridx/Grid", "Grid"),
    GridConfig("efaps/GridConfig", "GridConfig"),
    GridQuickFilter("efaps/GridQuickFilter", "GridQuickFilter"),
    GridSort("efaps/GridSort", "GridSort"),
    GridAggregate("efaps/GridAggregate", "GridAggregate"),
    HScroller("gridx/modules/HScroller", "HScroller"),
    HeaderDialog("efaps/HeaderDialog", "HeaderDialog"),
    HiddenColumns("gridx/modules/HiddenColumns", "HiddenColumns"),
    IndirectSelect("gridx/modules/IndirectSelect", "IndirectSelect"),
    LayoutContainer("dijit/layout/LayoutContainer", "LayoutContainer"),
    Memory("dojo/store/Memory", "Memory"),
    MenuBar("dijit/MenuBar", "MenuBar"),
    MenuBarItem("dijit/MenuBarItem", "MenuBarItem"),
    MenuItem("dijit/MenuItem", "MenuItem"),
    MoveColumn("gridx/modules/move/Column", "MoveColumn"),
    NodeListDom("dojo/NodeList-dom", null),
    NodeListTraverse("dojo/NodeList-traverse", null),
    NodeListFx("dojo/NodeList-fx", null),
    Number("dojo/number", "number"),
    Persist("gridx/modules/Persist", "Persist"),
    PopupMenuBarItem("dijit/PopupMenuBarItem", "PopupMenuBarItem"),
    PopupMenuItem("dijit/PopupMenuItem", "PopupMenuItem"),
    QuickFilter("gridx/support/QuickFilter", "QuickFilter"),
    RowHeader("gridx/modules/RowHeader", "RowHeader"),
    ExtendedSelectCell("gridx/modules/extendedSelect/Cell", "SelectCell"),
    ExtendedSelectColumn("gridx/modules/extendedSelect/Column", "SelectColumn"),
    ExtendedSelectRow("gridx/modules/extendedSelect/Row", "SelectRow"),
    SelectCell("gridx/modules/select/Cell", "SelectCell"),
    SelectColumn("gridx/modules/select/Column", "SelectColumn"),
    SelectRow("gridx/modules/select/Row", "SelectRow"),
    SingleSort("gridx/modules/SingleSort", "SingleSort"),
    Summary("gridx/support/Summary", "Summary"),
    SyncCache("gridx/core/model/cache/Sync", "Cache"),
    TextBox("dijit/form/TextBox", "TextBox"),
    ToggleSplitter("dojox/layout/ToggleSplitter", "ToggleSplitter"),
    TooltipDialog("dijit/TooltipDialog", "TooltipDialog"),
    VirtualVScroller("gridx/modules/VirtualVScroller", "VirtualVScroller"),
    array("dojo/_base/array", "array"),
    aspect("dojo/aspect", "aspect"),
    baseWindow("dojo/_base/window", "baseWindow"),
    dom("dojo/dom", "dom"),
    domAttr("dojo/dom-attr", "domAttr"),
    domClass("dojo/dom-class", "domClass"),
    domConstruct("dojo/dom-construct", "domConstruct"),
    domGeom("dojo/dom-geometry", "domGeom"),
    domReady("dojo/domReady!", null),
    domStyle("dojo/dom-style", "domStyle"),
    json("dojo/_base/json", "json"),
    lang("dojo/_base/lang", "lang"),
    on("dojo/on", "on"),
    parser("dojo/parser", "parser"),
    popup("dijit/popup", "popup"),
    query("dojo/query", "query"),
    ready("dojo/ready", "ready"),
    registry("dijit/registry", "registry"),
    topic("dojo/topic", "topic"),
    win("dojo/window", "win"),

    ChartDefault("dojox/charting/axis2d/Default", "Default"),
    MouseIndicator("dojox/charting/action2d/MouseIndicator", "MouseIndicator"),
    Highlight("dojox/charting/action2d/Highlight", "Highlight"),
    Chart("dojox/charting/Chart", "Chart"),
    SelectableLegend("dojox/charting/widget/SelectableLegend", "Legend"),
    Legend("dojox/charting/widget/Legend", "Legend"),
    Tooltip("dojox/charting/action2d/Tooltip", "Tooltip"),
    StackedBars("dojox/charting/plot2d/StackedBars", "Bars"),
    ClusteredBars("dojox/charting/plot2d/ClusteredBars", "Bars"),
    Bars("dojox/charting/plot2d/Bars", "Bars"),
    StackedColumns("dojox/charting/plot2d/StackedColumns", "Columns"),
    ClusteredColumns("dojox/charting/plot2d/ClusteredColumns", "Columns"),
    Columns("dojox/charting/plot2d/Columns", "Columns"),
    Lines("dojox/charting/plot2d/Lines", "Lines"),
    StackedLines("dojox/charting/plot2d/StackedLines", "Lines"),
    Areas("dojox/charting/plot2d/Areas", "Lines"),
    StackedAreas("dojox/charting/plot2d/StackedAreas", "Lines"),
    Magnify("dojox/charting/action2d/Magnify", "Magnify"),
    PiePlot("dojox/charting/plot2d/Pie", "PiePlot"),
    MoveSlice("dojox/charting/action2d/MoveSlice", "MoveSlice"),
    Julie("dojox/charting/themes/Julie", "theme");

    /** The lib name. */
    private final String className;

    /** The para name. */
    private final String parameterName;

    /**
     * Instantiates a new dojo libs.
     *
     * @param _className the class name
     * @param _parameterName the parameter name
     */
    DojoClasses(final String _className,
                final String _parameterName)
    {
        this.className = _className;
        this.parameterName = _parameterName;
    }

    @Override
    public String getClassName()
    {
        return this.className;
    }

    @Override
    public String getParameterName()
    {
        return this.parameterName;
    }
}
