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
