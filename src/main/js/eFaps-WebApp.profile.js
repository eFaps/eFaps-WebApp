var profile = (function(){
    return {

        releaseDir:"../resources/org/efaps/ui/wicket/behaviors/dojo",

        action: 'release',

        optimize: 'closure',

        layerOptimize: 'closure', //empty to deactivate

        cssOptimize: 'comments',

        packages:[{
            name:"dojo",
            location:"./dojo"
        },{
            name:"dojox",
            location:"./dojox"
        },{
            name:"dijit",
            location:"./dijit"
        },{
            name:"themes",
            location:"./themes"
        },{
            name:"gridx",
            location:"./gridx"
        },{
            name:"efaps",
            location:"./efaps"
        }],

        selectorEngine:"lite",

        dojoBootText:"require.boot && require.apply(null, require.boot);",

        // since this build it intended to be utilized with properly-expressed AMD modules;
        // don't insert absolute module ids into the modules
        insertAbsMids:0,

        // these are all the has feature that affect the loader and/or the bootstrap
        // the settings below are optimized for the smallest AMD loader that is configurable
        // and include dom-ready support
        staticHasFeatures:{
            // The trace & log APIs are used for debugging the loader, so we do not need them in the build.
            'dojo-trace-api': false,
            'dojo-log-api': false,
            // This causes normally private loader data to be exposed for debugging. In a release build, we do not need
            // that either.
            'dojo-publish-privates': false,

            // This application is pure AMD, so get rid of the legacy loader.
            'dojo-sync-loader': false,

            // `dojo-xhr-factory` relies on `dojo-sync-loader`, which we have removed.
            'dojo-xhr-factory': false,

            // We are not loading tests in production, so we can get rid of some test sniffing code.
            'dojo-test-sniff': false
        },

        layers:{
            "dojo/dojo":{
                 include: [
                     'dojo/dojo',
                     'dojo/loadInit',
                     'dojo/text',
                     'dojo/i18n'
                 ],
                 boot: true
            },
            "efaps/baseLayer":{
                include:[
                    "dijit/BackgroundIframe",
                    "dijit/DropDownMenu",
                    "dijit/MenuBar",
                    "dijit/MenuBarItem",
                    "dijit/MenuItem",
                    "dijit/PopupMenuBarItem",
                    "dijit/PopupMenuItem",
                    "dijit/layout/BorderContainer",
                    "dijit/layout/ContentPane",
                    "dojo/parser",
                    "dojo/ready"
                ],
                includeLocales:['es','de','en'],
                localeList:['es','de','en']
            },
            "efaps/gridxLayer":{
                include:[
                    "dojo/_base/array",
                    "dojo/_base/json",
                    "dojo/_base/lang",
                    "dojo/aspect",
                    "dojo/dom-construct",
                    "dojo/dom-geometry",
                    "dojo/dom-style",
                    "dojo/on",
                    "dojo/query",
                    "dojo/ready",
                    "dojo/store/Memory",
                    "dojo/window",
                    "dijit/DropDownMenu",
                    "dijit/MenuBar",
                    "dijit/MenuBarItem",
                    "dijit/MenuItem",
                    "dijit/PopupMenuBarItem",
                    "dijit/TooltipDialog",
                    "dijit/form/DropDownButton",
                    "dijit/form/TextBox",
                    "dijit/registry",
                    "dijit/form/NumberTextBox",
                    "efaps/GridAggregate",
                    "efaps/GridConfig",
                    "efaps/GridSort",
                    "efaps/GridQuickFilter",
                    "efaps/HeaderDialog",
                    "gridx/Grid",
                    "gridx/core/model/cache/Sync",
                    "gridx/modules/Bar",
                    "gridx/modules/ColumnResizer",
                    "gridx/modules/Filter",
                    "gridx/modules/HScroller",
                    "gridx/modules/HiddenColumns",
                    "gridx/modules/IndirectSelect",
                    "gridx/modules/Persist",
                    "gridx/modules/RowHeader",
                    "gridx/modules/SingleSort",
                    "gridx/modules/VirtualVScroller",
                    "gridx/modules/dnd/Column",
                    "gridx/modules/extendedSelect/Cell",
                    "gridx/modules/extendedSelect/Column",
                    "gridx/modules/extendedSelect/Row",
                    "gridx/modules/filter/FilterBar",
                    "gridx/modules/move/Column",
                    "gridx/support/Summary",
                    "gridx/modules/ColumnLock"
                ],
                includeLocales:['es','de','en'],
                localeList:['es','de','en']
            }
        }
    };
})();
