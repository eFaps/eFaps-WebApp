define("efaps/AjaxStore", [
    "dojo/_base/xhr",
    "dojo/_base/lang",
    "dojo/json",
    "dojo/Deferred",
    "dojo/_base/declare",
    "dojo/store/util/QueryResults" /*=====, "./api/Store" =====*/
], function(xhr, lang, json, Deferred, declare, QueryResults /*=====, Store =====*/){

// No base class, but for purposes of documentation, the base class is dojo/store/api/Store
var base = null;
/*===== base = Store; =====*/

/*=====
var __HeaderOptions = {
        // headers: Object?
        //      Additional headers to send along with the request.
    },
    __PutDirectives = declare(Store.PutDirectives, __HeaderOptions),
    __QueryOptions = declare(Store.QueryOptions, __HeaderOptions);
=====*/

return declare("efaps.AjaxStore", base, {
    // summary:
    //      This is a basic store for RESTful communicating with a server through JSON
    //      formatted data. It implements dojo/store/api/Store.

    constructor: function(options){
        // summary:
        //      This is a basic store for RESTful communicating with a server through JSON
        //      formatted data.
        // options: dojo/store/JsonRest
        //      This provides any configuration information that will be mixed into the store
        this.headers = {};
        declare.safeMixin(this, options);
    },

    // target: String
    //      The target base URL to use for all requests to the server. This string will be
    //      prepended to the id to generate the URL (relative or absolute) for requests
    //      sent to the server
    target: "",

    // idProperty: String
    //      Indicates the property to use as the identity property. The values of this
    //      property should be unique.
    idProperty: "id",

    // sortParam: String
    //      The query parameter to used for holding sort information. If this is omitted, than
    //      the sort information is included in a functional query token to avoid colliding
    //      with the set of name/value pairs.

    get: function(id, options){

    },

    // accepts: String
    //      Defines the Accept header to use on HTTP requests
    accepts: "application/javascript, application/json",

    getIdentity: function(object){
        // summary:
        //      Returns an object's identity
        // object: Object
        //      The object to get the identity from
        // returns: Number
        return object[this.idProperty];
    },

    put: function(object, options){

    },

    add: function(object, options){

    },

    remove: function(id, options){

    },

    query: function(query, options){
        // summary:
        //      Queries the store for objects. This will trigger a GET request to the server, with the
        //      query added as a query string.
        // query: Object
        //      The query to use for retrieving objects from the store.
        // options: __QueryOptions?
        //      The optional arguments to apply to the resultset.
        // returns: dojo/store/api/Store.QueryResults
        //      The results of the query, extended with iterative methods.
        options = options || {};
        var self = this,
        paramName = options.paramName,
        attrs = {
            u: options.callbackUrl,
            ep: {},
            wr: false,
            dt: 'html',
            sh: [ function(attributes, jqXHR, resp, textStatus) {
                var xmlDoc = Wicket.Xml.parse(resp);
                var root = xmlDoc.getElementsByTagName("ajax-response")[0];
                var res;
                for (var c = 0; c < root.childNodes.length; ++c) {
                    var node = root.childNodes[c];
                    if (node.tagName === "json") {
                        res = json.parse(node.textContent, false);
                    }
                }
                self.deferred.resolve(res);
            } ]
        };
        lang.setObject(paramName, query.name.toString(), options.ep);

        var eps = [],epv,epn;
        for (epn in options.ep) {
            epv = options.ep[epn];
            if (epv instanceof Array) {
                for (var i = 0; i < epv.length; i++) {
                    eps.push({name:epn,value: epv[i]});
                }
            } else {
                eps.push({name:epn,value: epv});
            }
        }
        attrs.ep = eps;

        this.deferred = new Deferred();
        Wicket.Ajax.ajax(attrs);
        var results= this.deferred.promise;

        results.total = results.then(function(){
            return 3;
        });
        return QueryResults(results);
    }});
});
