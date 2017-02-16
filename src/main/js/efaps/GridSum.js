define([
    'dojo/_base/declare',
    'dojo/_base/array',
    'dojo/_base/sniff',
    'dojo/dom',
    'dojo/dom-construct',
    'dojo/dom-style',
    'gridx/core/_Module'
], function(declare, array, has, dom, domConstruct, domStyle, _Module){

return declare(_Module, {

    name: 'gridSum',

    forced: ['body'],

    preload: function(args){
      console.log('dddddddddddddddddddddd');
    },

    load: function(args, deferStartup){
        var t = this;
        t._init();
        t.loaded.callback();
    },

    destroy: function(){
        this.inherited(arguments);
    },

    //Private---------------------------------------------------------
    _init: function() {
        var t = this,
            g = t.grid,
            nodeName = 'gridxSumNode',
            node = t[nodeName] = domConstruct.create('div', {
                'class': "gridSum"
            }),
            inner = t.innerNode = domConstruct.create('div', {
                'class': 'gridSumInner',
                role: 'row'
            });
        t.grid.vLayout.register(t, nodeName, 'footerNode', 3);

        node.appendChild(t.innerNode);

        var sb = ['<table role="presentation" border="0" cellpadding="0" cellspacing="0"><tr>'];
        array.forEach(g._columns, function(col){
            sb.push('<td id="', col._domId + "-sum",
                    '" role="columnsum" aria-readonly="true" tabindex="-1" colid="', col.id,
                    '" class="gridxCell ',
                    '" style="width:', col.width, ';min-width:', col.width, ';',
                    '">',
                    'sum',
                    '</td>');
        });
        sb.push('</tr></table>');
        t.innerNode.innerHTML = sb.join('');

        t.aspect(g, 'onHScroll', '_onHScroll');

        if(g.columnResizer){
            t.aspect(g.columnResizer, 'onResize', function(_colId, _newWidth, _oldWidth ) {
                this._onResize(_colId);
                if(g.hScrollerNode.style.display == 'none'){
                    t._onHScroll(0);
                }
            });
        }
        if (g.indirectSelect) {
            domStyle.set(node, {
                    "margin-left": "20px",
                    "margin-right": "0px"
            });
        }
    },

    _onHScroll: function(left){
        if((has('webkit') || has('ie') < 8) && !this.grid.isLeftToRight()){
            left = this.innerNode.scrollWidth - this.innerNode.offsetWidth - left;
        }
        this.innerNode.scrollLeft = this._scrollLeft = left;
    },

    _onResize: function(_colId){
        var t = this,
            g = t.grid,
            col = g._columnsById[_colId];
        if (col) {
            var headerNode = g.header.getHeaderNode(_colId),
                headerNodeStyle = headerNode.style,
                sumNode = dom.byId(col._domId + "-sum");

            sumNode.style.width = headerNodeStyle.width;
            sumNode.style.minWidth = headerNodeStyle.minWidth;
            sumNode.style.maxWidth = headerNodeStyle.maxWidth;
        }
    }
});
});
