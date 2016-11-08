define([
    "dojo/_base/declare",
    "dojo/_base/array",
    "dijit/_WidgetBase",
    "dijit/_TemplatedMixin",
    "dijit/_WidgetsInTemplateMixin",
    "dijit/CheckedMenuItem",
    "dojo/text!./templates/GridConfig.html",
    "dojo/i18n!./nls/eFaps"
], function(declare, array, _WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin, CheckedMenuItem, template, eFapsNLS){

    return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin], {
        templateString: template,

        buttonLabel: eFapsNLS.gridConfigButtonLabel,

        grid: null,

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
                tm = t.menu,
                hC = g.hiddenColumns;

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
        },

        check: function(_colId) {
            var t = this,
                items = t.menu.getChildren();
            array.forEach(items, function(item){
                if (_colId == item.colId) {
                    item._setCheckedAttr(true);
                }
            });
        },

        uncheck: function(_colId) {
            var t = this,
                items = t.menu.getChildren();
            array.forEach(items, function(item){
                if (_colId == item.colId) {
                    item._setCheckedAttr(false);
                }
            });
        }
    });
});
