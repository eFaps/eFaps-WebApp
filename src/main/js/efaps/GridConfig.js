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
    "dojo/_base/array",
    "dojo/dom-class",
    "dojo/dom-geometry",
    "dojo/on",
    "dijit/_WidgetBase",
    "dijit/_TemplatedMixin",
    "dijit/_WidgetsInTemplateMixin",
    "dijit/form/Button",
    "dijit/form/ToggleButton",
    "dijit/form/NumberSpinner",
    "dijit/CheckedMenuItem",
    "dijit/MenuItem",
    "dijit/Toolbar",
    "dojo/text!./templates/GridConfig.html",
    "dojo/i18n!./nls/eFaps"
], function(declare, array, domClass, domGeom, on, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, Button, ToggleButton,
        NumberSpinner, CheckedMenuItem, MenuItem, Toolbar, template, eFapsNLS) {

    return declare("efaps.GridConfig", [Toolbar, _WidgetsInTemplateMixin], {
        templateString: template,

        baseClass: "dijitToolbar gridConfig",
        //Labels
        nls: eFapsNLS,
        grid: null,
        printItems: [],
        reload: null,

        startup: function(){
            var t = this;
            t.inherited(arguments);
            t.refresh();
        },

        postCreate: function(){
            var t = this,
                m = t.grid.model,
                hC = t.grid.hiddenColumns;

            t.domNode.setAttribute('tabIndex', t.grid.domNode.getAttribute('tabIndex'));
            t.connect(hC, 'add', 'uncheck');
            t.connect(hC, 'remove', 'check');
        },

        refresh: function() {
            var t = this, g = t.grid,
                dn = t.domNode,
                tm = t.columnMenu,
                hC = g.hiddenColumns,
                wB = t.wrapButton,
                pM = t.printMenu,
                rB = t.reloadButton,
                lB = t.lockColumnButton,
                uB = t.unlockColumnButton;

            array.forEach(g.structure, function(col){
                 var item = new CheckedMenuItem({
                     colId: col.id,
                     label: col.name,
                     checked: true,
                     disabled: col.hasOwnProperty('dialog'),
                     onClick: function(e){
                         this.checked ? hC.remove(col.id) : hC.add(col.id);
                     }
                 });
                 this.addChild(item);
             }, tm);

            if(g.persist){
                var d = g.persist.registerAndLoad('wrapButton', function(){
                    return wB.checked;
                });
                if (d != null && d != wB.checked) {
                    wB.setChecked(d);
                    domClass.toggle(wB.domNode, "dijitDisabled", !wB.checked);
                    domClass.toggle(g.bodyNode, "eFapsNoWrap", !wB.checked);
                    g.resize();
                }
                if (g.columnLock.count > 0) {
                    dijit.byId('integerspinner').set('value', g.columnLock.count);
                }
            }

            on(wB, "click", function(evt){
                domClass.toggle(wB.domNode, "dijitDisabled", !wB.checked);
                domClass.toggle(g.bodyNode, "eFapsNoWrap", !wB.checked);
                g.resize();
            });

            on(lB, "click", function(evt){
                var c = dijit.byId('integerspinner').get('value');
                g.columnLock.lock(c);
            });

            on(uB, "click", function(evt){
                g.columnLock.unlock();
            });

            array.forEach(t.printItems, function(_item){
                 pM.addChild(_item);
            }, pM);

            rB.onClick = t.reload;
        },

        check: function(_colId) {
            var t = this,
                items = t.columnMenu.getChildren();
            array.forEach(items, function(item){
                if (_colId == item.colId) {
                    item._setCheckedAttr(true);
                }
            });
        },

        uncheck: function(_colId) {
            var t = this,
                items = t.columnMenu.getChildren();
            array.forEach(items, function(item){
                if (_colId == item.colId) {
                    item._setCheckedAttr(false);
                }
            });
        }
    });
});
