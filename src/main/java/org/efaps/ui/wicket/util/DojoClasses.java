package org.efaps.ui.wicket.util;

/**
 * The Enum DojoClasses.
 */
public enum DojoClasses
    implements DojoClass
{
    array("dojo/_base/array", "array"),
    lang("dojo/_base/lang", "lang"),
    json("dojo/_base/json","json"),
    fx("dojo/_base/fx","fx"),
    parser("dojo/parser", "parser"),
    aspect("dojo/aspect","aspect"),
    query("dojo/query","query"),
    domGeom("dojo/dom-geometry","domGeom"),
    domConstruct("dojo/dom-construct","domConstruct"),
    win("dojo/window","win"),
    domStyle("dojo/dom-style","domStyle"),
    ready("dojo/ready","ready"),
    on("dojo/on","on"),
    registry("dijit/registry","registry"),
    Memory("dojo/store/Memory","Memory"),
    SyncCache("gridx/core/model/cache/Sync","Cache"),
    Grid("gridx/Grid","Grid"),
    VirtualVScroller("gridx/modules/VirtualVScroller","VirtualVScroller"),
    ColumnResizer("gridx/modules/ColumnResizer","ColumnResizer"),
    HScroller("gridx/modules/HScroller","HScroller"),
    SingleSort("gridx/modules/SingleSort","SingleSort"),
    MoveColumn("gridx/modules/move/Column","MoveColumn"),
    SelectColumn("gridx/modules/extendedSelect/Column","SelectColumn"),
    SelectCell("gridx/modules/extendedSelect/Cell","SelectCell"),
    SelectRow("gridx/modules/extendedSelect/Row","SelectRow"),
    DnDColumn("gridx/modules/dnd/Column","DnDColumn"),
    HiddenColumns("gridx/modules/HiddenColumns","HiddenColumns"),
    IndirectSelect("gridx/modules/IndirectSelect","IndirectSelect"),
    RowHeader("gridx/modules/RowHeader","RowHeader"),
    HeaderDialog("efaps/HeaderDialog","HeaderDialog"),
    GridConfig("efaps/GridConfig","GridConfig"),
    GridSort("efaps/GridSort","GridSort"),
    Summary("gridx/support/Summary","Summary"),
    QuickFilter("gridx/support/QuickFilter","QuickFilter"),
    Bar("gridx/modules/Bar","Bar"),
    Filter("gridx/modules/Filter","Filter"),
    FilterBar("gridx/modules/filter/FilterBar","FilterBar"),
    Persist("gridx/modules/Persist","Persist"),
    DropDownButton ("dijit/form/DropDownButton","DropDownButton"),
    TextBox("dijit/form/TextBox","TextBox"),
    TooltipDialog("dijit/TooltipDialog","TooltipDialog"),
    MenuBar("dijit/MenuBar","MenuBar"),
    PopupMenuBarItem("dijit/PopupMenuBarItem","PopupMenuBarItem"),
    PopupMenuItem("dijit/PopupMenuItem","PopupMenuItem"),
    MenuItem("dijit/MenuItem","MenuItem"),
    DropDownMenu("dijit/DropDownMenu","DropDownMenu"),
    MenuBarItem("dijit/MenuBarItem", "MenuBarItem"),
    ToggleSplitter("dojox/layout/ToggleSplitter", "ToggleSplitter"),
    ContentPane("dijit/layout/ContentPane"," ContentPane"),
    ContentPaneX("dojox/layout/ContentPane"," ContentPane"),
    BorderContainer("dijit/layout/BorderContainer", "BorderContainer");

    /** The lib name. */
    private final String className;

    /** The para name. */
    private final String parameterName;

    /**
     * Instantiates a new dojo libs.
     *
     * @param _libName the lib name
     * @param _paraName the para name
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
