define([
    'dojo/_base/declare',
    'dojo/dom-class',
    'gridx/support/QuickFilter'
], function(declare, domClass, QuickFilter){

return declare([QuickFilter], {
    postCreate: function(){
        var t = this,
            dn = t.domNode,
            g = t.grid,
            tb = t.textBox;
        this.connect(this.textBox, 'onInput', '_onInput');
        if (g.persist){
            var d = g.persist.registerAndLoad('quickFilter', function(){
                return tb.get('value');
            });
            if (undefined != d && d.length > 0) {
                tb.set('value', d);
                domClass.toggle(dn, 'gridxQuickFilterActive', tb.get('value'));
            }
        }
    }
});
});
