define([
    'dojo/_base/declare',
    'dojo/_base/array',
    'dojo/_base/sniff',
    'dojo/dom',
    'dojo/dom-construct',
    'dojo/dom-style',
    'dojo/number',
    'dojo/string',
    'gridx/core/_Module',
    'dojo/i18n!./nls/eFaps'
], function(declare, array, has, dom, domConstruct, domStyle, number, string, _Module, eFapsNLS){

return declare(_Module, {

    name: 'gridAggregate',

    forced: ['body'],

    nls: eFapsNLS,

    preload: function(args){

    },

    load: function(args, deferStartup){
        var t = this,
            g = t.grid;
        t._init();
        t.loaded.callback();
        t.aspect(g.model, 'onSizeChange', 'refresh');
        t.aspect(g.model, '_onParentSizeChange', 'refresh');
        t.refresh();
    },

    destroy: function(){
        this.inherited(arguments);
    },


    refresh: function() {
         var t = this,
             g = t.grid,
             m = g.model;

         var selCol = [];
         array.forEach(g._columns, function(_col) {
             if (_col.aggregate) {
                 selCol.push({
                     field: _col.field,
                     domId: _col._domId,
                     sum: 0
                 });
             }
         });
         var scanCallback = function(_rows, _start, _parentId) {
                if(!_rows.length){
                    return false;
                }
                array.forEach(_rows, function(_row) {
                   array.forEach(selCol, function(_col) {
                       var val = 0;
                       if (typeof _row.rawData[_col.field] == "number") {
                           val = _row.rawData[_col.field];
                       } else if (typeof _row.rawData[_col.field + '_sort'] == "number") {
                           val = _row.rawData[_col.field + '_sort'];
                       }
                       _col.sum = _col.sum + val;
                    });
                });
            };
        m.scan({
            start: 0,
            pageSize: t.pageSize
        }, scanCallback).then(function(){
            array.forEach(selCol, function(_col) {
                var num = number.format(_col.sum);
                var txt = string.substitute(t.nls.aggregateMsg, { aggregate: t.nls.aggregateSUM, value: num });
                dom.byId(_col.domId + "-aggr").innerHTML= txt;
            });
        });
    },

    //Private---------------------------------------------------------
    _init: function() {
        var t = this,
            g = t.grid,
            nodeName = 'gridxAggrNode',
            node = t[nodeName] = domConstruct.create('div', {
                'class': "gridAggregate"
            }),
            inner = t.innerNode = domConstruct.create('div', {
                'class': 'gridAggregateInner',
                role: 'row'
            });
        t.grid.vLayout.register(t, nodeName, 'footerNode', 3);

        node.appendChild(t.innerNode);

        var sb = ['<table role="presentation" border="0" cellpadding="0" cellspacing="0"><tr>'];
        array.forEach(g._columns, function(col){
            sb.push('<td id="', col._domId + "-aggr",
                    '" role="columnsum" aria-readonly="true" tabindex="-1" colid="', col.id,
                    '" class="gridxCell ',
                    '" style="width:', col.width, ';min-width:', col.width, ';',
                    '">', '&nbsp;', '</td>');
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
        t.aspect(g.header, 'onRender', '_onRender');
    },

    _onHScroll: function(left) {
        if((has('webkit') || has('ie') < 8) && !this.grid.isLeftToRight()){
            left = this.innerNode.scrollWidth - this.innerNode.offsetWidth - left;
        }
        this.innerNode.scrollLeft = this._scrollLeft = left;
    },

    _onResize: function(_colId) {
        var t = this,
            g = t.grid,
            col = g._columnsById[_colId];
        if (col) {
            var headerNode = g.header.getHeaderNode(_colId),
                headerNodeStyle = headerNode.style,
                sumNode = dom.byId(col._domId + "-aggr");

            sumNode.style.width = headerNodeStyle.width;
            sumNode.style.minWidth = headerNodeStyle.minWidth;
            sumNode.style.maxWidth = headerNodeStyle.maxWidth;
        }
    },

    _onRender: function() {
         var t = this,
             g = t.grid,
             h = g.header;

         array.forEach(g._columns, function(_col) {
             var headerNode = h.getHeaderNode(_col.id),
                  sumNode = dom.byId(_col._domId + "-aggr");
             sumNode.style.width = headerNode.style.width;
             sumNode.style.minWidth = headerNode.style.minWidth;
             sumNode.style.maxWidth = headerNode.style.maxWidth;
         });
    }
});
});
