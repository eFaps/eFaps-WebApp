define("efaps/AutoComplete", [
    "dojo/_base/declare", // declare
    "dojo/_base/lang", // lang.clone lang.hitch
    "dojo/string", // string.substitute
    "dojo/when",
    "dijit/form/FilteringSelect",
    "dojo/dom-style",
    "dojo/dom-form",
    "dojo/keys",
], function(declare, lang, string, when, FilteringSelect, domStyle, domForm, keys){

    // module:
    // efaps/AutoComplete

    return declare("efaps.AutoComplete", [FilteringSelect], {
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


        isBarcode:  function(val) {
            const re = /(^\d{8}$)|(^\d{10}$)|(^\d{13}$)/m;
            return val.match(re);
        },

        _onKey: function(/*Event*/ evt){
			// summary:
			//		Handles keyboard events
			if(evt.charCode >= 32){
				return;
			} // alphanumeric reserved for searching

			var key = evt.charCode || evt.keyCode;

			// except for cutting/pasting case - ctrl + x/v
			if(key == keys.ALT || key == keys.CTRL || key == keys.META || key == keys.SHIFT){
				return; // throw out spurious events
			}
            // detect a barcode by using regex plus ENTER
            if (key == keys.ENTER && this._lastInput && this.isBarcode(this._lastInput)) {
                return
            }

			var pw = this.dropDown;
			var highlighted = null;
			this._abortQuery();

			// _HasDropDown will do some of the work:
			//
			//	1. when drop down is not yet shown:
			//		- if user presses the down arrow key, call loadDropDown()
			//	2. when drop down is already displayed:
			//		- on ESC key, call closeDropDown()
			//		- otherwise, call dropDown.handleKey() to process the keystroke
            this.inherited(arguments);

			if(evt.altKey || evt.ctrlKey || evt.metaKey){
				return;
			} // don't process keys with modifiers  - but we want shift+TAB

			if(this._opened){
				highlighted = pw.getHighlightedOption();
			}
			switch(key){
				case keys.PAGE_DOWN:
				case keys.DOWN_ARROW:
				case keys.PAGE_UP:
				case keys.UP_ARROW:
					// Keystroke caused ComboBox_menu to move to a different item.
					// Copy new item to <input> box.
					if(this._opened){
						this._announceOption(highlighted);
					}
					evt.stopPropagation();
					evt.preventDefault();
					break;

				case keys.ENTER:
					// prevent submitting form if user presses enter. Also
					// prevent accepting the value if either Next or Previous
					// are selected
					if(highlighted){
						// only stop event on prev/next
						if(highlighted == pw.nextButton){
							this._nextSearch(1);
							// prevent submit
							evt.stopPropagation();
							evt.preventDefault();
							break;
						}else if(highlighted == pw.previousButton){
							this._nextSearch(-1);
							// prevent submit
							evt.stopPropagation();
							evt.preventDefault();
							break;
						}
						// prevent submit if ENTER was to choose an item
						evt.stopPropagation();
						evt.preventDefault();
					}else{
						// Update 'value' (ex: KY) according to currently displayed text
						this._setBlurValue(); // set value if needed
						this._setCaretPos(this.focusNode, this.focusNode.value.length); // move cursor to end and cancel highlighting
					}
				// fall through

				case keys.TAB:
					var newvalue = this.get('displayedValue');
					//	if the user had More Choices selected fall into the
					//	_onBlur handler
					if(pw && (newvalue == pw._messages["previousMessage"] || newvalue == pw._messages["nextMessage"])){
						break;
					}
					if(highlighted){
						this._selectOption(highlighted);
					}
				// fall through

				case keys.ESCAPE:
					if(this._opened){
						this._lastQuery = null; // in case results come back later
						this.closeDropDown();
					}
					break;
			}
		},


        _setBlurValue : function() {
            // if the user clicks away from the textbox OR tabs away, set the value to the textbox value
            // #4617:
            // if value is now more choices or previous choices, revert the value
            var newvalue = this.get('displayedValue');
            if (newvalue.length == 0) {
                this.valueNode.value="";
            }

            var pw = this.dropDown;
            if (pw && (newvalue == pw._messages["previousMessage"] || newvalue == pw._messages["nextMessage"])) {
                this._setValueAttr(this._lastValueReported, true);
            } else if (typeof this.item == "undefined") {
                // Update 'value' (ex: KY) according to
                // currently displayed text
                this.item = null;
                this.set('displayedValue', newvalue);
            } else {
                if (this.value != this._lastValueReported) {
                    this._handleOnChange(this.value, true);
                }
                this._refreshState();
            }
            // Remove aria-activedescendant since it may not be removed if they select with arrows then blur with mouse
            this.focusNode.removeAttribute("aria-activedescendant");
        },


        _startSearch: function(/*String*/ text){

            if (text.length >= this.minInputLength) {
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
                    // value from FilteringSelect's keyField
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
                    var fieldObj = domForm.toObject(this.valueNode.form);
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
           // _AutoCompleterMixin._onKey = myOnKey
        }

    });
});
