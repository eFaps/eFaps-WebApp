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
define([
    "dojo/_base/declare",
    "dojo/_base/event",
    "dojo/parser",
    "dojo/dom",
    "dojo/aspect",
    "dijit/registry",
    "dojo/dom-construct",
    "dojo/dom-class",
    "dojo/keys",
    "dijit/popup",
    "gridx/core/_Module",
    "gridx/modules/HeaderRegions"
], function(declare, event, parser, dom, aspect, registry, domConstruct, domClass, keys, popup, _Module){

/*=====
    var HeaderMenu = declare(_Module, {
        // summary:
        //        module name: headerMenu.
        //        Add a dropdown menu button on header cell.
        // description:
        //        Add a dropdown menu button on the header of any column that has a "menu" defined in structure.
        //        The "menu" is a dijit/Menu widget or its ID. It can provide a "bindGrid" function with signature of
        //        function(grid, column), so that some initialization work can be done when this menu is bound to grid.
    });

    HeaderMenu.__ColumnDefinition = declare([], {
        // menu: String|dijit.Menu
        //        Any dijit.Menu widget or its ID.
        menu: null
    });

    return HeaderMenu;
=====*/

    return declare(_Module, {
        name: 'headerDialog',

        forced: ['headerRegions'],

        preload: function(){
            var t = this,
                grid = t.grid;
            grid.headerRegions.add(function(col){
                var dialog = col.dialog;
                if(dialog){
                    var btn = domConstruct.create('div', {
                        className: 'gridxHeaderMenuBtn',
                        tabIndex: -1,
                        innerHTML: '<span class="gridxHeaderMenuBtnInner">&#9662;</span>&nbsp;'
                    });
                   // domClass.add(dialog.domNode, 'gridxHeaderMenu');
                    t.connect(btn, 'onkeydown', function(e){
                        if(e.keyCode == keys.ENTER){
                            event.stop(e);
                            popup.open({
                                popup: registry.byId(dialog),
                                around: btn
                            });
                        }
                    });
                    t.connect(btn, 'click', function(e){
                        event.stop(e);
                        popup.open({
                            popup: registry.byId(dialog),
                            orient: [ "below-alt", "above-alt", "below","above" ],
                            around: btn.parentNode,
                            style: "min-width:150px"
                        });
                    });
                    if(dialog.bindGrid){
                        dialog.bindGrid(grid, col);
                    }
                    //handle dialog close
                   // t.aspect(dialog, 'onClose', function(e){
                     //   grid.headerRegions._doFocus(e);
                    //});
                    return btn;
                }
            }, 0, 1);
        }
    });
});
