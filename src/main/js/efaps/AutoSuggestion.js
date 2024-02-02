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
define("efaps/AutoSuggestion", [
    "dojo/_base/declare", // declare
    "dojo/_base/lang", // lang.clone lang.hitch
    "dojo/string", // string.substitute
    "dojo/when",
    "dijit/form/ComboBox",
    "dojo/dom-style",
    "dojo/dom-form"
], function(declare, lang, string, when, ComboBox, domStyle, domForm){

    // module:
    // efaps/AutoComplete

    return declare("efaps.AutoSuggestion", [ComboBox], {
        // summary:
        // Auto-completing text box
        baseClass: "dijitTextBox dijitComboBox eFapsAutoComplete",

        callbackUrl: "",

        minInputLength: 1,

        paramName: "p",

        indicatorId: "eFapsVeil",

        extraParameters: [],
        // overwrite the search delay default
        searchDelay: 500,

        _startSearch: function(/*String*/ text){

            if (text.length >= this.minInputLength || (this.hasDownArrow && text.length == 0)) {
                domStyle.set(this.indicatorId, "display", "");
                // summary:
                //      Starts a search for elements matching key (key=="" means to return all items),
                //      and calls _openResultList() when the search completes, to display the results.
                if(!this.dropDown){
                    var popupId = this.id + "_popup",
                        dropDownConstructor = lang.isString(this.dropDownClass) ?
                            lang.getObject(this.dropDownClass, false) : this.dropDownClass;
                    this.dropDown = new dropDownConstructor({
                        onChange: lang.hitch(this, this._selectOption),
                        id: popupId,
                        dir: this.dir,
                        textDir: this.textDir
                    });
                }

                this.inherited(arguments);

                // summary:
                //      Starts a search for elements matching text (text=="" means to return all items),
                //      and calls onSearch(...) when the search completes, to display the results.

                this._abortQuery();
                var
                    _this = this,
                    // Setup parameters to be passed to store.query().
                    // Create a new query to prevent accidentally querying for a hidden
                    // value from ComboBox's keyField
                    query = lang.clone(this.query), // #5970

                    options = {
                        start: 0,
                        count: this.pageSize,
                        callbackUrl: this.callbackUrl,
                        paramName: this.paramName,
                        ep: {}
                    },
                    qs = text, //string.substitute(this.queryExpr, [text.replace(/([\\\*\?])/g, "\\$1")]),
                    q;

                // read the extra parameters to post them also
                if (this.extraParameters.length > 0) {
                    var fieldObj = domForm.toObject(this.focusNode.form);
                    for (var i = 0; i < this.extraParameters.length; i++) {
                        var val = lang.getObject(this.extraParameters[i], false, fieldObj);
                        if (val != null) {
                            options.ep[this.extraParameters[i]] = val;
                        }
                    }
                }

                var startQuery = function(){
                        var resPromise = _this._fetchHandle = _this.store.query(query, options);
                        if(_this.disabled || _this.readOnly || (q !== _this._lastQuery)){
                            return;
                        } // avoid getting unwanted notify
                        when(resPromise, function(res){
                            _this._fetchHandle = null;
                            if(!_this.disabled && !_this.readOnly && (q === _this._lastQuery)){ // avoid getting unwanted notify
                                when(resPromise.total, function(total){
                                    res.total = total;
                                    var pageSize = _this.pageSize;
                                    if(isNaN(pageSize) || pageSize > res.total){ pageSize = res.total; }
                                    // Setup method to fetching the next page of results
                                    res.nextPage = function(direction){
                                        //  tell callback the direction of the paging so the screen
                                        //  reader knows which menu option to shout
                                        options.direction = direction = direction !== false;
                                        options.count = pageSize;
                                        if(direction){
                                            options.start += res.length;
                                            if(options.start >= res.total){
                                                options.count = 0;
                                            }
                                        }else{
                                            options.start -= pageSize;
                                            if(options.start < 0){
                                                options.count = Math.max(pageSize + options.start, 0);
                                                options.start = 0;
                                            }
                                        }
                                        if(options.count <= 0){
                                            res.length = 0;
                                            _this.onSearch(res, query, options);
                                        }else{
                                            startQuery();
                                        }
                                    };
                                    _this.onSearch(res, query, options);
                                    domStyle.set(_this.indicatorId, "display", "none");
                                });
                            }
                        }, function(err){
                            _this._fetchHandle = null;
                            domStyle.set(_this.indicatorId, "display", "none");
                            if(!_this._cancelingQuery){ // don't treat canceled query as an error
                                console.error(_this.declaredClass + ' ' + err.toString());
                            }
                        });
                    };

                lang.mixin(options, this.fetchProperties);

                // Generate query
                if(this.store._oldAPI){
                    // remove this branch for 2.0
                    q = qs;
                }else{
                    // Query on searchAttr is a regex for benefit of dojo/store/Memory,
                    // but with a toString() method to help dojo/store/JsonRest.
                    // Search string like "Co*" converted to regex like /^Co.*$/i.
                    q = this._patternToRegExp(qs);
                    q.toString = function(){ return qs; };
                }

                // set _lastQuery, *then* start the timeout
                // otherwise, if the user types and the last query returns before the timeout,
                // _lastQuery won't be set and their input gets rewritten
                this._lastQuery = query[this.searchAttr] = q;
                this._queryDeferHandle = this.defer(startQuery, this.searchDelay);
            } else {
                // no search was done but eventually the indicator is on
                domStyle.set(this.indicatorId, "display", "none");
            }
        },

        // The constructor
        constructor: function(args){
            declare.safeMixin(this,args);
        }

    });
});
