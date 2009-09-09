/*
    Copyright (c) 2004-2009, The Dojo Foundation All Rights Reserved.
    Available via Academic Free License >= 2.1 OR the modified BSD license.
    see: http://dojotoolkit.org/license for details
*/

/*
    This is a compiled version of Dojo, built for deployment and not for
    development. To get an editable version, please visit:

        http://dojotoolkit.org

    for documentation and information on getting the source.
*/

if(!dojo._hasResource["dojo.i18n"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.i18n"] = true;
dojo.provide("dojo.i18n");

/*=====
dojo.i18n = {
    // summary: Utility classes to enable loading of resources for internationalization (i18n)
};
=====*/

dojo.i18n.getLocalization = function(/*String*/packageName, /*String*/bundleName, /*String?*/locale){
    //  summary:
    //      Returns an Object containing the localization for a given resource
    //      bundle in a package, matching the specified locale.
    //  description:
    //      Returns a hash containing name/value pairs in its prototypesuch
    //      that values can be easily overridden.  Throws an exception if the
    //      bundle is not found.  Bundle must have already been loaded by
    //      `dojo.requireLocalization()` or by a build optimization step.  NOTE:
    //      try not to call this method as part of an object property
    //      definition (`var foo = { bar: dojo.i18n.getLocalization() }`).  In
    //      some loading situations, the bundle may not be available in time
    //      for the object definition.  Instead, call this method inside a
    //      function that is run after all modules load or the page loads (like
    //      in `dojo.addOnLoad()`), or in a widget lifecycle method.
    //  packageName:
    //      package which is associated with this resource
    //  bundleName:
    //      the base filename of the resource bundle (without the ".js" suffix)
    //  locale:
    //      the variant to load (optional).  By default, the locale defined by
    //      the host environment: dojo.locale

    locale = dojo.i18n.normalizeLocale(locale);

    // look for nearest locale match
    var elements = locale.split('-');
    var module = [packageName,"nls",bundleName].join('.');
    var bundle = dojo._loadedModules[module];
    if(bundle){
        var localization;
        for(var i = elements.length; i > 0; i--){
            var loc = elements.slice(0, i).join('_');
            if(bundle[loc]){
                localization = bundle[loc];
                break;
            }
        }
        if(!localization){
            localization = bundle.ROOT;
        }

        // make a singleton prototype so that the caller won't accidentally change the values globally
        if(localization){
            var clazz = function(){};
            clazz.prototype = localization;
            return new clazz(); // Object
        }
    }

    throw new Error("Bundle not found: " + bundleName + " in " + packageName+" , locale=" + locale);
};

dojo.i18n.normalizeLocale = function(/*String?*/locale){
    //  summary:
    //      Returns canonical form of locale, as used by Dojo.
    //
    //  description:
    //      All variants are case-insensitive and are separated by '-' as specified in [RFC 3066](http://www.ietf.org/rfc/rfc3066.txt).
    //      If no locale is specified, the dojo.locale is returned.  dojo.locale is defined by
    //      the user agent's locale unless overridden by djConfig.

    var result = locale ? locale.toLowerCase() : dojo.locale;
    if(result == "root"){
        result = "ROOT";
    }
    return result; // String
};

dojo.i18n._requireLocalization = function(/*String*/moduleName, /*String*/bundleName, /*String?*/locale, /*String?*/availableFlatLocales){
    //  summary:
    //      See dojo.requireLocalization()
    //  description:
    //      Called by the bootstrap, but factored out so that it is only
    //      included in the build when needed.

    var targetLocale = dojo.i18n.normalizeLocale(locale);
    var bundlePackage = [moduleName, "nls", bundleName].join(".");
    // NOTE:
    //      When loading these resources, the packaging does not match what is
    //      on disk.  This is an implementation detail, as this is just a
    //      private data structure to hold the loaded resources.  e.g.
    //      `tests/hello/nls/en-us/salutations.js` is loaded as the object
    //      `tests.hello.nls.salutations.en_us={...}` The structure on disk is
    //      intended to be most convenient for developers and translators, but
    //      in memory it is more logical and efficient to store in a different
    //      order.  Locales cannot use dashes, since the resulting path will
    //      not evaluate as valid JS, so we translate them to underscores.

    //Find the best-match locale to load if we have available flat locales.
    var bestLocale = "";
    if(availableFlatLocales){
        var flatLocales = availableFlatLocales.split(",");
        for(var i = 0; i < flatLocales.length; i++){
            //Locale must match from start of string.
            //Using ["indexOf"] so customBase builds do not see
            //this as a dojo._base.array dependency.
            if(targetLocale["indexOf"](flatLocales[i]) == 0){
                if(flatLocales[i].length > bestLocale.length){
                    bestLocale = flatLocales[i];
                }
            }
        }
        if(!bestLocale){
            bestLocale = "ROOT";
        }
    }

    //See if the desired locale is already loaded.
    var tempLocale = availableFlatLocales ? bestLocale : targetLocale;
    var bundle = dojo._loadedModules[bundlePackage];
    var localizedBundle = null;
    if(bundle){
        if(dojo.config.localizationComplete && bundle._built){return;}
        var jsLoc = tempLocale.replace(/-/g, '_');
        var translationPackage = bundlePackage+"."+jsLoc;
        localizedBundle = dojo._loadedModules[translationPackage];
    }

    if(!localizedBundle){
        bundle = dojo["provide"](bundlePackage);
        var syms = dojo._getModuleSymbols(moduleName);
        var modpath = syms.concat("nls").join("/");
        var parent;

        dojo.i18n._searchLocalePath(tempLocale, availableFlatLocales, function(loc){
            var jsLoc = loc.replace(/-/g, '_');
            var translationPackage = bundlePackage + "." + jsLoc;
            var loaded = false;
            if(!dojo._loadedModules[translationPackage]){
                // Mark loaded whether it's found or not, so that further load attempts will not be made
                dojo["provide"](translationPackage);
                var module = [modpath];
                if(loc != "ROOT"){module.push(loc);}
                module.push(bundleName);
                var filespec = module.join("/") + '.js';
                loaded = dojo._loadPath(filespec, null, function(hash){
                    // Use singleton with prototype to point to parent bundle, then mix-in result from loadPath
                    var clazz = function(){};
                    clazz.prototype = parent;
                    bundle[jsLoc] = new clazz();
                    for(var j in hash){ bundle[jsLoc][j] = hash[j]; }
                });
            }else{
                loaded = true;
            }
            if(loaded && bundle[jsLoc]){
                parent = bundle[jsLoc];
            }else{
                bundle[jsLoc] = parent;
            }

            if(availableFlatLocales){
                //Stop the locale path searching if we know the availableFlatLocales, since
                //the first call to this function will load the only bundle that is needed.
                return true;
            }
        });
    }

    //Save the best locale bundle as the target locale bundle when we know the
    //the available bundles.
    if(availableFlatLocales && targetLocale != bestLocale){
        bundle[targetLocale.replace(/-/g, '_')] = bundle[bestLocale.replace(/-/g, '_')];
    }
};

(function(){
    // If other locales are used, dojo.requireLocalization should load them as
    // well, by default.
    //
    // Override dojo.requireLocalization to do load the default bundle, then
    // iterate through the extraLocale list and load those translations as
    // well, unless a particular locale was requested.

    var extra = dojo.config.extraLocale;
    if(extra){
        if(!extra instanceof Array){
            extra = [extra];
        }

        var req = dojo.i18n._requireLocalization;
        dojo.i18n._requireLocalization = function(m, b, locale, availableFlatLocales){
            req(m,b,locale, availableFlatLocales);
            if(locale){return;}
            for(var i=0; i<extra.length; i++){
                req(m,b,extra[i], availableFlatLocales);
            }
        };
    }
})();

dojo.i18n._searchLocalePath = function(/*String*/locale, /*Boolean*/down, /*Function*/searchFunc){
    //  summary:
    //      A helper method to assist in searching for locale-based resources.
    //      Will iterate through the variants of a particular locale, either up
    //      or down, executing a callback function.  For example, "en-us" and
    //      true will try "en-us" followed by "en" and finally "ROOT".

    locale = dojo.i18n.normalizeLocale(locale);

    var elements = locale.split('-');
    var searchlist = [];
    for(var i = elements.length; i > 0; i--){
        searchlist.push(elements.slice(0, i).join('-'));
    }
    searchlist.push(false);
    if(down){searchlist.reverse();}

    for(var j = searchlist.length - 1; j >= 0; j--){
        var loc = searchlist[j] || "ROOT";
        var stop = searchFunc(loc);
        if(stop){ break; }
    }
};

dojo.i18n._preloadLocalizations = function(/*String*/bundlePrefix, /*Array*/localesGenerated){
    //  summary:
    //      Load built, flattened resource bundles, if available for all
    //      locales used in the page. Only called by built layer files.

    function preload(locale){
        locale = dojo.i18n.normalizeLocale(locale);
        dojo.i18n._searchLocalePath(locale, true, function(loc){
            for(var i=0; i<localesGenerated.length;i++){
                if(localesGenerated[i] == loc){
                    dojo["require"](bundlePrefix+"_"+loc);
                    return true; // Boolean
                }
            }
            return false; // Boolean
        });
    }
    preload();
    var extra = dojo.config.extraLocale||[];
    for(var i=0; i<extra.length; i++){
        preload(extra[i]);
    }
};

}

if(!dojo._hasResource["dijit._base.focus"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.focus"] = true;
dojo.provide("dijit._base.focus");

// summary:
//      These functions are used to query or set the focus and selection.
//
//      Also, they trace when widgets become actived/deactivated,
//      so that the widget can fire _onFocus/_onBlur events.
//      "Active" here means something similar to "focused", but
//      "focus" isn't quite the right word because we keep track of
//      a whole stack of "active" widgets.  Example:  Combobutton --> Menu -->
//      MenuItem.   The onBlur event for Combobutton doesn't fire due to focusing
//      on the Menu or a MenuItem, since they are considered part of the
//      Combobutton widget.  It only happens when focus is shifted
//      somewhere completely different.

dojo.mixin(dijit,
{
    // _curFocus: DomNode
    //      Currently focused item on screen
    _curFocus: null,

    // _prevFocus: DomNode
    //      Previously focused item on screen
    _prevFocus: null,

    isCollapsed: function(){
        // summary: tests whether the current selection is empty
        var _document = dojo.doc;
        if(_document.selection){ // IE
            var s=_document.selection;
            if(s.type=='Text'){
                return !s.createRange().htmlText.length; // Boolean
            }else{ //Control range
                return !s.createRange().length; // Boolean
            }
        }else{
            var _window = dojo.global;
            var selection = _window.getSelection();
            if(dojo.isString(selection)){ // Safari
                return !selection; // Boolean
            }else{ // Mozilla/W3
                return selection.isCollapsed || !selection.toString(); // Boolean
            }
        }
    },

    getBookmark: function(){
        // summary: Retrieves a bookmark that can be used with moveToBookmark to return to the same range
        var bookmark, selection = dojo.doc.selection;
        if(selection){ // IE
            var range = selection.createRange();
            if(selection.type.toUpperCase()=='CONTROL'){
                if(range.length){
                    bookmark=[];
                    var i=0,len=range.length;
                    while(i<len){
                        bookmark.push(range.item(i++));
                    }
                }else{
                    bookmark=null;
                }
            }else{
                bookmark = range.getBookmark();
            }
        }else{
            if(window.getSelection){
                selection = dojo.global.getSelection();
                if(selection){
                    range = selection.getRangeAt(0);
                    bookmark = range.cloneRange();
                }
            }else{
                console.warn("No idea how to store the current selection for this browser!");
            }
        }
        return bookmark; // Array
    },

    moveToBookmark: function(/*Object*/bookmark){
        // summary: Moves current selection to a bookmark
        // bookmark: This should be a returned object from dojo.html.selection.getBookmark()
        var _document = dojo.doc;
        if(_document.selection){ // IE
            var range;
            if(dojo.isArray(bookmark)){
                range = _document.body.createControlRange();
                //range.addElement does not have call/apply method, so can not call it directly
                //range is not available in "range.addElement(item)", so can't use that either
                dojo.forEach(bookmark, function(n){
                    range.addElement(n);
                });
            }else{
                range = _document.selection.createRange();
                range.moveToBookmark(bookmark);
            }
            range.select();
        }else{ //Moz/W3C
            var selection = dojo.global.getSelection && dojo.global.getSelection();
            if(selection && selection.removeAllRanges){
                selection.removeAllRanges();
                selection.addRange(bookmark);
            }else{
                console.warn("No idea how to restore selection for this browser!");
            }
        }
    },

    getFocus: function(/*Widget?*/menu, /*Window?*/openedForWindow){
        // summary:
        //  Returns the current focus and selection.
        //  Called when a popup appears (either a top level menu or a dialog),
        //  or when a toolbar/menubar receives focus
        //
        // menu:
        //  The menu that's being opened
        //
        // openedForWindow:
        //  iframe in which menu was opened
        //
        // returns:
        //  A handle to restore focus/selection

        return {
            // Node to return focus to
            node: menu && dojo.isDescendant(dijit._curFocus, menu.domNode) ? dijit._prevFocus : dijit._curFocus,

            // Previously selected text
            bookmark:
                !dojo.withGlobal(openedForWindow||dojo.global, dijit.isCollapsed) ?
                dojo.withGlobal(openedForWindow||dojo.global, dijit.getBookmark) :
                null,

            openedForWindow: openedForWindow
        }; // Object
    },

    focus: function(/*Object || DomNode */ handle){
        // summary:
        //      Sets the focused node and the selection according to argument.
        //      To set focus to an iframe's content, pass in the iframe itself.
        // handle:
        //      object returned by get(), or a DomNode

        if(!handle){ return; }

        var node = "node" in handle ? handle.node : handle,     // because handle is either DomNode or a composite object
            bookmark = handle.bookmark,
            openedForWindow = handle.openedForWindow;

        // Set the focus
        // Note that for iframe's we need to use the <iframe> to follow the parentNode chain,
        // but we need to set focus to iframe.contentWindow
        if(node){
            var focusNode = (node.tagName.toLowerCase()=="iframe") ? node.contentWindow : node;
            if(focusNode && focusNode.focus){
                try{
                    // Gecko throws sometimes if setting focus is impossible,
                    // node not displayed or something like that
                    focusNode.focus();
                }catch(e){/*quiet*/}
            }
            dijit._onFocusNode(node);
        }

        // set the selection
        // do not need to restore if current selection is not empty
        // (use keyboard to select a menu item)
        if(bookmark && dojo.withGlobal(openedForWindow||dojo.global, dijit.isCollapsed)){
            if(openedForWindow){
                openedForWindow.focus();
            }
            try{
                dojo.withGlobal(openedForWindow||dojo.global, dijit.moveToBookmark, null, [bookmark]);
            }catch(e){
                /*squelch IE internal error, see http://trac.dojotoolkit.org/ticket/1984 */
            }
        }
    },

    // _activeStack: Array
    //      List of currently active widgets (focused widget and it's ancestors)
    _activeStack: [],

    registerWin: function(/*Window?*/targetWindow){
        // summary:
        //      Registers listeners on the specified window (either the main
        //      window or an iframe) to detect when the user has clicked somewhere.
        //      Anyone that creates an iframe should call this function.

        if(!targetWindow){
            targetWindow = window;
        }

        dojo.connect(targetWindow.document, "onmousedown", function(evt){
            dijit._justMouseDowned = true;
            setTimeout(function(){ dijit._justMouseDowned = false; }, 0);
            dijit._onTouchNode(evt.target||evt.srcElement);
        });
        //dojo.connect(targetWindow, "onscroll", ???);

        // Listen for blur and focus events on targetWindow's body
        var doc = targetWindow.document;
        if(doc){
            if(dojo.isIE){
                doc.attachEvent('onactivate', function(evt){
                    if(evt.srcElement.tagName.toLowerCase() != "#document"){
                        dijit._onFocusNode(evt.srcElement);
                    }
                });
                doc.attachEvent('ondeactivate', function(evt){
                    dijit._onBlurNode(evt.srcElement);
                });
            }else{
                doc.addEventListener('focus', function(evt){
                    dijit._onFocusNode(evt.target);
                }, true);
                doc.addEventListener('blur', function(evt){
                    dijit._onBlurNode(evt.target);
                }, true);
            }
        }
        doc = null; // prevent memory leak (apparent circular reference via closure)
    },

    _onBlurNode: function(/*DomNode*/ node){
        // summary:
        //      Called when focus leaves a node.
        //      Usually ignored, _unless_ it *isn't* follwed by touching another node,
        //      which indicates that we tabbed off the last field on the page,
        //      in which case every widget is marked inactive
        dijit._prevFocus = dijit._curFocus;
        dijit._curFocus = null;

        if(dijit._justMouseDowned){
            // the mouse down caused a new widget to be marked as active; this blur event
            // is coming late, so ignore it.
            return;
        }

        // if the blur event isn't followed by a focus event then mark all widgets as inactive.
        if(dijit._clearActiveWidgetsTimer){
            clearTimeout(dijit._clearActiveWidgetsTimer);
        }
        dijit._clearActiveWidgetsTimer = setTimeout(function(){
            delete dijit._clearActiveWidgetsTimer;
            dijit._setStack([]);
            dijit._prevFocus = null;
        }, 100);
    },

    _onTouchNode: function(/*DomNode*/ node){
        // summary:
        //      Callback when node is focused or mouse-downed

        // ignore the recent blurNode event
        if(dijit._clearActiveWidgetsTimer){
            clearTimeout(dijit._clearActiveWidgetsTimer);
            delete dijit._clearActiveWidgetsTimer;
        }

        // compute stack of active widgets (ex: ComboButton --> Menu --> MenuItem)
        var newStack=[];
        try{
            while(node){
                if(node.dijitPopupParent){
                    node=dijit.byId(node.dijitPopupParent).domNode;
                }else if(node.tagName && node.tagName.toLowerCase()=="body"){
                    // is this the root of the document or just the root of an iframe?
                    if(node===dojo.body()){
                        // node is the root of the main document
                        break;
                    }
                    // otherwise, find the iframe this node refers to (can't access it via parentNode,
                    // need to do this trick instead). window.frameElement is supported in IE/FF/Webkit
                    node=dijit.getDocumentWindow(node.ownerDocument).frameElement;
                }else{
                    var id = node.getAttribute && node.getAttribute("widgetId");
                    if(id){
                        newStack.unshift(id);
                    }
                    node=node.parentNode;
                }
            }
        }catch(e){ /* squelch */ }

        dijit._setStack(newStack);
    },

    _onFocusNode: function(/*DomNode*/ node){
        // summary
        //      Callback when node is focused

        if(!node){
            return;
        }

        if(node.nodeType == 9){
            // Ignore focus events on the document itself.  This is here so that
            // (for example) clicking the up/down arrows of a spinner
            //  (which don't get focus) won't cause that widget to blur. (FF issue)
            return;
        }

        if(node.nodeType == 9){
            // We focused on (the body of) the document itself, either the main document
            // or an iframe
            var iframe = dijit.getDocumentWindow(node).frameElement;
            if(!iframe){
                // Ignore focus events on main document.  This is specifically here
                // so that clicking the up/down arrows of a spinner (which don't get focus)
                // won't cause that widget to blur.
                return;
            }

            node = iframe;
        }

        dijit._onTouchNode(node);

        if(node==dijit._curFocus){ return; }
        if(dijit._curFocus){
            dijit._prevFocus = dijit._curFocus;
        }
        dijit._curFocus = node;
        dojo.publish("focusNode", [node]);
    },

    _setStack: function(newStack){
        // summary
        //  The stack of active widgets has changed.  Send out appropriate events and record new stack

        var oldStack = dijit._activeStack;
        dijit._activeStack = newStack;

        // compare old stack to new stack to see how many elements they have in common
        for(var nCommon=0; nCommon<Math.min(oldStack.length, newStack.length); nCommon++){
            if(oldStack[nCommon] != newStack[nCommon]){
                break;
            }
        }

        // for all elements that have gone out of focus, send blur event
        for(var i=oldStack.length-1; i>=nCommon; i--){
            var widget = dijit.byId(oldStack[i]);
            if(widget){
                widget._focused = false;
                widget._hasBeenBlurred = true;
                if(widget._onBlur){
                    widget._onBlur();
                }
                if (widget._setStateClass){
                    widget._setStateClass();
                }
                dojo.publish("widgetBlur", [widget]);
            }
        }

        // for all element that have come into focus, send focus event
        for(i=nCommon; i<newStack.length; i++){
            widget = dijit.byId(newStack[i]);
            if(widget){
                widget._focused = true;
                if(widget._onFocus){
                    widget._onFocus();
                }
                if (widget._setStateClass){
                    widget._setStateClass();
                }
                dojo.publish("widgetFocus", [widget]);
            }
        }
    }
});

// register top window and all the iframes it contains
dojo.addOnLoad(dijit.registerWin);

}

if(!dojo._hasResource["dijit._base.manager"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.manager"] = true;
dojo.provide("dijit._base.manager");

dojo.declare("dijit.WidgetSet", null, {
    // summary:
    //  A set of widgets indexed by id. A default instance of this Class is
    //  available as `dijit.registry`
    //
    // example:
    //  Create a small list of widgets:
    //  |   var ws = new dijit.WidgetSet();
    //  |   ws.add(dijit.byId("one"));
    //  |   ws.add(dijit.byId("two"));
    //  |   // destroy both:
    //  |   ws.forEach(function(w){ w.destroy(); });
    //
    // example:
    //  Using dijit.registry:
    //  |   dijit.registry.forEach(function(w){ /* do something */ });

    constructor: function(){
        this._hash = {};
    },

    add: function(/*Widget*/ widget){
        // summary: Add a widget to this list. If a duplicate ID is detected, a warning is issued.
        //
        // Widget: dijit._Widget
        //      Any dijit._Widget derrivitave.
        if(this._hash[widget.id]){
            throw new Error("Tried to register widget with id==" + widget.id + " but that id is already registered");
        }
        this._hash[widget.id]=widget;
    },

    remove: function(/*String*/ id){
        // summary: Remove a widget byID from this WidgetSet. Does not destry widget, simply
        //  removes the rerence in this list.
        delete this._hash[id];
    },

    forEach: function(/*Function*/ func){
        // summary: Iterate over this widgetSet, calling a function for each of the
        //  items.
        //
        // func:
        //      A callback function to run forEach item. Is passed a unique widget.
        //
        // exmample:
        // Using the default `dijit.registry` instance:
        // |    dijit.registry.forEach(function(widget){
        // |        console.log(widget.declaredClass);
        // |    });
        for(var id in this._hash){
            func(this._hash[id]);
        }
    },

    filter: function(/*Function*/ filter){
        // summary: Filter down this WidgetSet to a smaller new WidgetSet
        //      Works the same as `dojo.filter` and `dojo.NodeList.filter`
        //
        // filter:
        //      Callback function to test truthiness.
        //
        // example:
        //  Arbitrary: select the odd widgets in this list
        // |    var i = 0;
        // |    dijit.registry.filter(function(w){
        // |        return ++i % 2 == 0;
        // |    }).forEach(function(w){ /* odd ones */ });

        var res = new dijit.WidgetSet();
        this.forEach(function(widget){
            if(filter(widget)){ res.add(widget); }
        });
        return res; // dijit.WidgetSet
    },

    byId: function(/*String*/ id){
        // summary: Find a widget in this list byId.
        //
        // example:
        //  As a synonym for `dijit.byId`:
        // | dijit.registry.byId("foo");
        //
        // example:
        //  Test if an id is in a particular WidgetSet
        //  | var ws = new dijit.WidgetSet();
        //  | ws.add(dijit.byId("bar"));
        //  | var t = ws.byId("bar") // returns a widget
        //  | var x = ws.byId("foo"); // returns undefined

        return this._hash[id];
    },

    byClass: function(/*String*/ cls){
        // summary: Reduce this widgetset to a new WidgetSet of a particular declaredClass
        //
        // example:
        // Find all titlePane's in a page:
        // |    dijit.registry.byClass("dijit.TitlePane").forEach(function(tp){ tp.close(); });

        return this.filter(function(widget){ return widget.declaredClass==cls; });  // dijit.WidgetSet
    }

});

/*=====
dijit.registry = {
    // summary: A list of widgets on a page.
    // description: Is an instance of `dijit.WidgetSet`
};
=====*/
dijit.registry = new dijit.WidgetSet();

dijit._widgetTypeCtr = {};

dijit.getUniqueId = function(/*String*/widgetType){
    // summary: Generates a unique id for a given widgetType

    var id;
    do{
        id = widgetType + "_" +
            (widgetType in dijit._widgetTypeCtr ?
                ++dijit._widgetTypeCtr[widgetType] : dijit._widgetTypeCtr[widgetType] = 0);
    }while(dijit.byId(id));
    return id; // String
};


if(dojo.isIE){
    // Only run this for IE because we think it's only necessary in that case,
    // and because it causes problems on FF.  See bug #3531 for details.
    dojo.addOnWindowUnload(function(){
        dijit.registry.forEach(function(widget){ widget.destroy(); });
    });
}

dijit.byId = function(/*String|Widget*/id){
    // summary:
    //      Returns a widget by its id, or if passed a widget, no-op (like dojo.byId())
    return (dojo.isString(id)) ? dijit.registry.byId(id) : id; // Widget
};

dijit.byNode = function(/* DOMNode */ node){
    // summary:
    //      Returns the widget as referenced by node
    return dijit.registry.byId(node.getAttribute("widgetId")); // Widget
};

dijit.getEnclosingWidget = function(/* DOMNode */ node){
    // summary:
    //      Returns the widget whose dom tree contains node or null if
    //      the node is not contained within the dom tree of any widget
    while(node){
        if(node.getAttribute && node.getAttribute("widgetId")){
            return dijit.registry.byId(node.getAttribute("widgetId"));
        }
        node = node.parentNode;
    }
    return null;
};

// elements that are tab-navigable if they have no tabindex value set
// (except for "a", which must have an href attribute)
dijit._tabElements = {
    area: true,
    button: true,
    input: true,
    object: true,
    select: true,
    textarea: true
};

dijit._isElementShown = function(/*Element*/elem){
    var style = dojo.style(elem);
    return (style.visibility != "hidden")
        && (style.visibility != "collapsed")
        && (style.display != "none")
        && (dojo.attr(elem, "type") != "hidden");
}

dijit.isTabNavigable = function(/*Element*/elem){
    // summary:
    //      Tests if an element is tab-navigable
    if(dojo.hasAttr(elem, "disabled")){ return false; }
    var hasTabindex = dojo.hasAttr(elem, "tabindex");
    var tabindex = dojo.attr(elem, "tabindex");
    if(hasTabindex && tabindex >= 0) {
        return true; // boolean
    }
    var name = elem.nodeName.toLowerCase();
    if(((name == "a" && dojo.hasAttr(elem, "href"))
            || dijit._tabElements[name])
        && (!hasTabindex || tabindex >= 0)){
        return true; // boolean
    }
    return false; // boolean
};

dijit._getTabNavigable = function(/*DOMNode*/root){
    // summary:
    //      Finds descendants of the specified root node.
    //
    // description:
    //      Finds the following descendants of the specified root node:
    //      * the first tab-navigable element in document order
    //        without a tabindex or with tabindex="0"
    //      * the last tab-navigable element in document order
    //        without a tabindex or with tabindex="0"
    //      * the first element in document order with the lowest
    //        positive tabindex value
    //      * the last element in document order with the highest
    //        positive tabindex value
    var first, last, lowest, lowestTabindex, highest, highestTabindex;
    var walkTree = function(/*DOMNode*/parent){
        dojo.query("> *", parent).forEach(function(child){
            var isShown = dijit._isElementShown(child);
            if(isShown && dijit.isTabNavigable(child)){
                var tabindex = dojo.attr(child, "tabindex");
                if(!dojo.hasAttr(child, "tabindex") || tabindex == 0){
                    if(!first){ first = child; }
                    last = child;
                }else if(tabindex > 0){
                    if(!lowest || tabindex < lowestTabindex){
                        lowestTabindex = tabindex;
                        lowest = child;
                    }
                    if(!highest || tabindex >= highestTabindex){
                        highestTabindex = tabindex;
                        highest = child;
                    }
                }
            }
            if(isShown && child.nodeName.toUpperCase() != 'SELECT'){ walkTree(child) }
        });
    };
    if(dijit._isElementShown(root)){ walkTree(root) }
    return { first: first, last: last, lowest: lowest, highest: highest };
}
dijit.getFirstInTabbingOrder = function(/*String|DOMNode*/root){
    // summary:
    //      Finds the descendant of the specified root node
    //      that is first in the tabbing order
    var elems = dijit._getTabNavigable(dojo.byId(root));
    return elems.lowest ? elems.lowest : elems.first; // DomNode
};

dijit.getLastInTabbingOrder = function(/*String|DOMNode*/root){
    // summary:
    //      Finds the descendant of the specified root node
    //      that is last in the tabbing order
    var elems = dijit._getTabNavigable(dojo.byId(root));
    return elems.last ? elems.last : elems.highest; // DomNode
};

/*=====
dojo.mixin(dijit, {
    // defaultDuration: Integer
    //      The default animation speed (in ms) to use for all Dijit
    //      transitional animations, unless otherwise specified
    //      on a per-instance basis. Defaults to 200, overrided by
    //      `djConfig.defaultDuration`
    defaultDuration: 300
});
=====*/

dijit.defaultDuration = dojo.config["defaultDuration"] || 200;

}

if(!dojo._hasResource["dojo.AdapterRegistry"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.AdapterRegistry"] = true;
dojo.provide("dojo.AdapterRegistry");

dojo.AdapterRegistry = function(/*Boolean?*/ returnWrappers){
    //  summary:
    //      A registry to make contextual calling/searching easier.
    //  description:
    //      Objects of this class keep list of arrays in the form [name, check,
    //      wrap, directReturn] that are used to determine what the contextual
    //      result of a set of checked arguments is. All check/wrap functions
    //      in this registry should be of the same arity.
    //  example:
    //  |   // create a new registry
    //  |   var reg = new dojo.AdapterRegistry();
    //  |   reg.register("handleString",
    //  |       dojo.isString,
    //  |       function(str){
    //  |           // do something with the string here
    //  |       }
    //  |   );
    //  |   reg.register("handleArr",
    //  |       dojo.isArray,
    //  |       function(arr){
    //  |           // do something with the array here
    //  |       }
    //  |   );
    //  |
    //  |   // now we can pass reg.match() *either* an array or a string and
    //  |   // the value we pass will get handled by the right function
    //  |   reg.match("someValue"); // will call the first function
    //  |   reg.match(["someValue"]); // will call the second

    this.pairs = [];
    this.returnWrappers = returnWrappers || false; // Boolean
}

dojo.extend(dojo.AdapterRegistry, {
    register: function(/*String*/ name, /*Function*/ check, /*Function*/ wrap, /*Boolean?*/ directReturn, /*Boolean?*/ override){
        //  summary:
        //      register a check function to determine if the wrap function or
        //      object gets selected
        //  name:
        //      a way to identify this matcher.
        //  check:
        //      a function that arguments are passed to from the adapter's
        //      match() function.  The check function should return true if the
        //      given arguments are appropriate for the wrap function.
        //  directReturn:
        //      If directReturn is true, the value passed in for wrap will be
        //      returned instead of being called. Alternately, the
        //      AdapterRegistry can be set globally to "return not call" using
        //      the returnWrappers property. Either way, this behavior allows
        //      the registry to act as a "search" function instead of a
        //      function interception library.
        //  override:
        //      If override is given and true, the check function will be given
        //      highest priority. Otherwise, it will be the lowest priority
        //      adapter.
        this.pairs[((override) ? "unshift" : "push")]([name, check, wrap, directReturn]);
    },

    match: function(/* ... */){
        // summary:
        //      Find an adapter for the given arguments. If no suitable adapter
        //      is found, throws an exception. match() accepts any number of
        //      arguments, all of which are passed to all matching functions
        //      from the registered pairs.
        for(var i = 0; i < this.pairs.length; i++){
            var pair = this.pairs[i];
            if(pair[1].apply(this, arguments)){
                if((pair[3])||(this.returnWrappers)){
                    return pair[2];
                }else{
                    return pair[2].apply(this, arguments);
                }
            }
        }
        throw new Error("No match found");
    },

    unregister: function(name){
        // summary: Remove a named adapter from the registry

        // FIXME: this is kind of a dumb way to handle this. On a large
        // registry this will be slow-ish and we can use the name as a lookup
        // should we choose to trade memory for speed.
        for(var i = 0; i < this.pairs.length; i++){
            var pair = this.pairs[i];
            if(pair[0] == name){
                this.pairs.splice(i, 1);
                return true;
            }
        }
        return false;
    }
});

}

if(!dojo._hasResource["dijit._base.place"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.place"] = true;
dojo.provide("dijit._base.place");



// ported from dojo.html.util

dijit.getViewport = function(){
    //  summary
    //  Returns the dimensions and scroll position of the viewable area of a browser window

    var _window = dojo.global;
    var _document = dojo.doc;

    // get viewport size
    var w = 0, h = 0;
    var de = _document.documentElement;
    var dew = de.clientWidth, deh = de.clientHeight;
    if(dojo.isMozilla || dojo.isSafari){
        // mozilla
        // _window.innerHeight includes the height taken by the scroll bar
        // clientHeight is ideal but has DTD issues:
        // #4539: FF reverses the roles of body.clientHeight/Width and documentElement.clientHeight/Width based on the DTD!
        // check DTD to see whether body or documentElement returns the viewport dimensions using this algorithm:
        var minw, minh, maxw, maxh;
        var dbw = _document.body.clientWidth;
        if(dbw > dew){
            minw = dew;
            maxw = dbw;
        }else{
            maxw = dew;
            minw = dbw;
        }
        var dbh = _document.body.clientHeight;
        if(dbh > deh){
            minh = deh;
            maxh = dbh;
        }else{
            maxh = deh;
            minh = dbh;
        }
        w = (maxw > _window.innerWidth) ? minw : maxw;
        h = (maxh > _window.innerHeight) ? minh : maxh;
    }else if(_window.innerWidth){
        w = _window.innerWidth;
        h = _window.innerHeight;
    }else if(dojo.isIE && de && deh){
        w = dew;
        h = deh;
    }else if(dojo.body().clientWidth){
        // IE6?  If this isn't here then viewport.html fails
        w = dojo.body().clientWidth;
        h = dojo.body().clientHeight;
    }

    // get scroll position
    var scroll = dojo._docScroll();
    return { w: w, h: h, l: scroll.x, t: scroll.y };    //  object
};

/*=====
dijit.__Position = function(){
    //  x: Integer
    //      horizontal coordinate in pixels, relative to document body
    //  y: Integer
    //      vertical coordinate in pixels, relative to document body

    thix.x = x;
    this.y = y;
}
=====*/


dijit.placeOnScreen = function(
    /* DomNode */           node,
    /* dijit.__Position */  pos,
    /* String[] */          corners,
    /* dijit.__Position? */ padding){
    //  summary:
    //      Positions one of the node's corners at specified position
    //      such that node is fully visible in viewport.
    //  description:
    //      NOTE: node is assumed to be absolutely or relatively positioned.
    //  pos:
    //      Object like {x: 10, y: 20}
    //  corners:
    //      Array of Strings representing order to try corners in, like ["TR", "BL"].
    //      Possible values are:
    //          * "BL" - bottom left
    //          * "BR" - bottom right
    //          * "TL" - top left
    //          * "TR" - top right
    //  padding:
    //      set padding to put some buffer around the element you want to position.
    //  example:
    //      Try to place node's top right corner at (10,20).
    //      If that makes node go (partially) off screen, then try placing
    //      bottom left corner at (10,20).
    //  |   placeOnScreen(node, {x: 10, y: 20}, ["TR", "BL"])

    var choices = dojo.map(corners, function(corner){
        var c = { corner: corner, pos: {x:pos.x,y:pos.y} };
        if(padding){
            c.pos.x += corner.charAt(1) == 'L' ? padding.x : -padding.x;
            c.pos.y += corner.charAt(0) == 'T' ? padding.y : -padding.y;
        }
        return c;
    });

    return dijit._place(node, choices);
}

dijit._place = function(/*DomNode*/ node, /* Array */ choices, /* Function */ layoutNode){
    // summary:
    //      Given a list of spots to put node, put it at the first spot where it fits,
    //      of if it doesn't fit anywhere then the place with the least overflow
    // choices: Array
    //      Array of elements like: {corner: 'TL', pos: {x: 10, y: 20} }
    //      Above example says to put the top-left corner of the node at (10,20)
    //  layoutNode: Function(node, aroundNodeCorner, nodeCorner)
    //      for things like tooltip, they are displayed differently (and have different dimensions)
    //      based on their orientation relative to the parent.   This adjusts the popup based on orientation.

    // get {x: 10, y: 10, w: 100, h:100} type obj representing position of
    // viewport over document
    var view = dijit.getViewport();

    // This won't work if the node is inside a <div style="position: relative">,
    // so reattach it to dojo.doc.body.   (Otherwise, the positioning will be wrong
    // and also it might get cutoff)
    if(!node.parentNode || String(node.parentNode.tagName).toLowerCase() != "body"){
        dojo.body().appendChild(node);
    }

    var best = null;
    dojo.some(choices, function(choice){
        var corner = choice.corner;
        var pos = choice.pos;

        // configure node to be displayed in given position relative to button
        // (need to do this in order to get an accurate size for the node, because
        // a tooltips size changes based on position, due to triangle)
        if(layoutNode){
            layoutNode(node, choice.aroundCorner, corner);
        }

        // get node's size
        var style = node.style;
        var oldDisplay = style.display;
        var oldVis = style.visibility;
        style.visibility = "hidden";
        style.display = "";
        var mb = dojo.marginBox(node);
        style.display = oldDisplay;
        style.visibility = oldVis;

        // coordinates and size of node with specified corner placed at pos,
        // and clipped by viewport
        var startX = (corner.charAt(1) == 'L' ? pos.x : Math.max(view.l, pos.x - mb.w)),
            startY = (corner.charAt(0) == 'T' ? pos.y : Math.max(view.t, pos.y -  mb.h)),
            endX = (corner.charAt(1) == 'L' ? Math.min(view.l + view.w, startX + mb.w) : pos.x),
            endY = (corner.charAt(0) == 'T' ? Math.min(view.t + view.h, startY + mb.h) : pos.y),
            width = endX - startX,
            height = endY - startY,
            overflow = (mb.w - width) + (mb.h - height);

        if(best == null || overflow < best.overflow){
            best = {
                corner: corner,
                aroundCorner: choice.aroundCorner,
                x: startX,
                y: startY,
                w: width,
                h: height,
                overflow: overflow
            };
        }
        return !overflow;
    });

    node.style.left = best.x + "px";
    node.style.top = best.y + "px";
    if(best.overflow && layoutNode){
        layoutNode(node, best.aroundCorner, best.corner);
    }
    return best;
}

dijit.placeOnScreenAroundNode = function(
    /* DomNode */       node,
    /* DomNode */       aroundNode,
    /* Object */        aroundCorners,
    /* Function? */     layoutNode){

    //  summary:
    //      Position node adjacent or kitty-corner to aroundNode
    //      such that it's fully visible in viewport.
    //
    //  description:
    //      Place node such that corner of node touches a corner of
    //      aroundNode, and that node is fully visible.
    //
    //  aroundCorners:
    //      Ordered list of pairs of corners to try matching up.
    //      Each pair of corners is represented as a key/value in the hash,
    //      where the key corresponds to the aroundNode's corner, and
    //      the value corresponds to the node's corner:
    //
    //  |   { aroundNodeCorner1: nodeCorner1, aroundNodeCorner2: nodeCorner2,  ...}
    //
    //      The following strings are used to represent the four corners:
    //          * "BL" - bottom left
    //          * "BR" - bottom right
    //          * "TL" - top left
    //          * "TR" - top right
    //
    //  layoutNode: Function(node, aroundNodeCorner, nodeCorner)
    //      For things like tooltip, they are displayed differently (and have different dimensions)
    //      based on their orientation relative to the parent.   This adjusts the popup based on orientation.
    //
    //  example:
    //  |   dijit.placeOnScreenAroundNode(node, aroundNode, {'BL':'TL', 'TR':'BR'});
    //      This will try to position node such that node's top-left corner is at the same position
    //      as the bottom left corner of the aroundNode (ie, put node below
    //      aroundNode, with left edges aligned).  If that fails it will try to put
    //      the bottom-right corner of node where the top right corner of aroundNode is
    //      (ie, put node above aroundNode, with right edges aligned)
    //

    // get coordinates of aroundNode
    aroundNode = dojo.byId(aroundNode);
    var oldDisplay = aroundNode.style.display;
    aroundNode.style.display="";
    // #3172: use the slightly tighter border box instead of marginBox
    var aroundNodeW = aroundNode.offsetWidth; //mb.w;
    var aroundNodeH = aroundNode.offsetHeight; //mb.h;
    var aroundNodePos = dojo.coords(aroundNode, true);
    aroundNode.style.display=oldDisplay;

    // place the node around the calculated rectangle
    return dijit._placeOnScreenAroundRect(node,
        aroundNodePos.x, aroundNodePos.y, aroundNodeW, aroundNodeH, // rectangle
        aroundCorners, layoutNode);
};

/*=====
dijit.__Rectangle = function(){
    //  x: Integer
    //      horizontal offset in pixels, relative to document body
    //  y: Integer
    //      vertical offset in pixels, relative to document body
    //  width: Integer
    //      width in pixels
    //  height: Integer
    //      height in pixels

    thix.x = x;
    this.y = y;
    thix.width = width;
    this.height = height;
}
=====*/


dijit.placeOnScreenAroundRectangle = function(
    /* DomNode */           node,
    /* dijit.__Rectangle */ aroundRect,
    /* Object */            aroundCorners,
    /* Function */          layoutNode){

    //  summary:
    //      Like dijit.placeOnScreenAroundNode(), except that the "around"
    //      parameter is an arbitrary rectangle on the screen (x, y, width, height)
    //      instead of a dom node.

    return dijit._placeOnScreenAroundRect(node,
        aroundRect.x, aroundRect.y, aroundRect.width, aroundRect.height,    // rectangle
        aroundCorners, layoutNode);
};

dijit._placeOnScreenAroundRect = function(
    /* DomNode */       node,
    /* Number */        x,
    /* Number */        y,
    /* Number */        width,
    /* Number */        height,
    /* Object */        aroundCorners,
    /* Function */      layoutNode){

    //  summary:
    //      Like dijit.placeOnScreenAroundNode(), except it accepts coordinates
    //      of a rectangle to place node adjacent to.

    // TODO: combine with placeOnScreenAroundRectangle()

    // Generate list of possible positions for node
    var choices = [];
    for(var nodeCorner in aroundCorners){
        choices.push( {
            aroundCorner: nodeCorner,
            corner: aroundCorners[nodeCorner],
            pos: {
                x: x + (nodeCorner.charAt(1) == 'L' ? 0 : width),
                y: y + (nodeCorner.charAt(0) == 'T' ? 0 : height)
            }
        });
    }

    return dijit._place(node, choices, layoutNode);
};

dijit.placementRegistry = new dojo.AdapterRegistry();
dijit.placementRegistry.register("node",
    function(n, x){
        return typeof x == "object" &&
            typeof x.offsetWidth != "undefined" && typeof x.offsetHeight != "undefined";
    },
    dijit.placeOnScreenAroundNode);
dijit.placementRegistry.register("rect",
    function(n, x){
        return typeof x == "object" &&
            "x" in x && "y" in x && "width" in x && "height" in x;
    },
    dijit.placeOnScreenAroundRectangle);

dijit.placeOnScreenAroundElement = function(
    /* DomNode */       node,
    /* Object */        aroundElement,
    /* Object */        aroundCorners,
    /* Function */      layoutNode){

    //  summary:
    //      Like dijit.placeOnScreenAroundNode(), except it accepts an arbitrary object
    //      for the "around" argument and finds a proper processor to place a node.

    return dijit.placementRegistry.match.apply(dijit.placementRegistry, arguments);
};

}

if(!dojo._hasResource["dijit._base.window"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.window"] = true;
dojo.provide("dijit._base.window");

dijit.getDocumentWindow = function(doc){
    // summary:
    //      Get window object associated with document doc

    // In some IE versions (at least 6.0), document.parentWindow does not return a
    // reference to the real window object (maybe a copy), so we must fix it as well
    // We use IE specific execScript to attach the real window reference to
    // document._parentWindow for later use
    if(dojo.isIE && window !== document.parentWindow && !doc._parentWindow){
        /*
        In IE 6, only the variable "window" can be used to connect events (others
        may be only copies).
        */
        doc.parentWindow.execScript("document._parentWindow = window;", "Javascript");
        //to prevent memory leak, unset it after use
        //another possibility is to add an onUnload handler which seems overkill to me (liucougar)
        var win = doc._parentWindow;
        doc._parentWindow = null;
        return win; //  Window
    }

    return doc._parentWindow || doc.parentWindow || doc.defaultView;    //  Window
}

}

if(!dojo._hasResource["dijit._base.popup"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.popup"] = true;
dojo.provide("dijit._base.popup");





dijit.popup = new function(){
    // summary:
    //      This class is used to show/hide widgets as popups.

    var stack = [],
        beginZIndex=1000,
        idGen = 1;

    this.prepare = function(/*DomNode*/ node){
        // summary:
        //      Prepares a node to be used as a popup
        //
        // description:
        //      Attaches node to dojo.doc.body, and
        //      positions it off screen, but not display:none, so that
        //      the widget doesn't appear in the page flow and/or cause a blank
        //      area at the bottom of the viewport (making scrollbar longer), but
        //      initialization of contained widgets works correctly

        dojo.body().appendChild(node);
        var s = node.style;
        if(s.display == "none"){
            s.display="";
        }
        s.visibility = "hidden";    // so TAB key doesn't navigate to hidden popup
        s.position = "absolute";
        s.top = "-9999px";
    };

/*=====
dijit.popup.__OpenArgs = function(){
    //      popup: Widget
    //          widget to display
    //      parent: Widget
    //          the button etc. that is displaying this popup
    //      around: DomNode
    //          DOM node (typically a button); place popup relative to this node.  (Specify this *or* "x" and "y" parameters.)
    //      x: Integer
    //          Absolute horizontal position (in pixels) to place node at.  (Specify this *or* "around" parameter.)
    //      y: Integer
    //          Absolute vertical position (in pixels) to place node at.  (Specity this *or* "around" parameter.)
    //      orient: Object || String
    //          When the around parameter is specified, orient should be an
    //          ordered list of tuples of the form (around-node-corner, popup-node-corner).
    //          dijit.popup.open() tries to position the popup according to each tuple in the list, in order,
    //          until the popup appears fully within the viewport.
    //
    //          The default value is {BL:'TL', TL:'BL'}, which represents a list of two tuples:
    //              1. (BL, TL)
    //              2. (TL, BL)
    //          where BL means "bottom left" and "TL" means "top left".
    //          So by default, it first tries putting the popup below the around node, left-aligning them,
    //          and then tries to put it above the around node, still left-aligning them.   Note that the
    //          default is horizontally reversed when in RTL mode.
    //
    //          When an (x,y) position is specified rather than an around node, orient is either
    //          "R" or "L".  R (for right) means that it tries to put the popup to the right of the mouse,
    //          specifically positioning the popup's top-right corner at the mouse position, and if that doesn't
    //          fit in the viewport, then it tries, in order, the bottom-right corner, the top left corner,
    //          and the top-right corner.
    //      onCancel: Function
    //          callback when user has canceled the popup by
    //              1. hitting ESC or
    //              2. by using the popup widget's proprietary cancel mechanism (like a cancel button in a dialog);
    //                 i.e. whenever popupWidget.onCancel() is called, args.onCancel is called
    //      onClose: Function
    //          callback whenever this popup is closed
    //      onExecute: Function
    //          callback when user "executed" on the popup/sub-popup by selecting a menu choice, etc. (top menu only)
    //      padding: dijit.__Position
    //          adding a buffer around the opening position. This is only useful when around is not set.
    this.popup = popup;
    this.parent = parent;
    this.around = around;
    this.x = x;
    this.y = y;
    this.orient = orient;
    this.onCancel = onCancel;
    this.onClose = onClose;
    this.onExecute = onExecute;
    this.padding = padding;
}
=====*/
    this.open = function(/*dijit.popup.__OpenArgs*/ args){
        // summary:
        //      Popup the widget at the specified position
        //
        // example:
        //  opening at the mouse position
        //  |       dijit.popup.open({popup: menuWidget, x: evt.pageX, y: evt.pageY});
        //
        // example:
        //  opening the widget as a dropdown
        //  |       dijit.popup.open({parent: this, popup: menuWidget, around: this.domNode, onClose: function(){...}  });
        //
        //  Note that whatever widget called dijit.popup.open() should also listen to its own _onBlur callback
        //  (fired from _base/focus.js) to know that focus has moved somewhere else and thus the popup should be closed.

        var widget = args.popup,
            orient = args.orient || {'BL':'TL', 'TL':'BL'},
            around = args.around,
            id = (args.around && args.around.id) ? (args.around.id+"_dropdown") : ("popup_"+idGen++);

        // make wrapper div to hold widget and possibly hold iframe behind it.
        // we can't attach the iframe as a child of the widget.domNode because
        // widget.domNode might be a <table>, <ul>, etc.
        var wrapper = dojo.create("div",{
            id: id,
            "class":"dijitPopup",
            style:{
                zIndex: beginZIndex + stack.length,
                visibility:"hidden"
            }
        }, dojo.body());
        dijit.setWaiRole(wrapper, "presentation");

        // prevent transient scrollbar causing misalign (#5776)
        wrapper.style.left = wrapper.style.top = "0px";

        if(args.parent){
            wrapper.dijitPopupParent=args.parent.id;
        }

        var s = widget.domNode.style;
        s.display = "";
        s.visibility = "";
        s.position = "";
        s.top = "0px";
        wrapper.appendChild(widget.domNode);

        var iframe = new dijit.BackgroundIframe(wrapper);

        // position the wrapper node
        var best = around ?
            dijit.placeOnScreenAroundElement(wrapper, around, orient, widget.orient ? dojo.hitch(widget, "orient") : null) :
            dijit.placeOnScreen(wrapper, args, orient == 'R' ? ['TR','BR','TL','BL'] : ['TL','BL','TR','BR'], args.padding);

        wrapper.style.visibility = "visible";
        // TODO: use effects to fade in wrapper

        var handlers = [];

        // Compute the closest ancestor popup that's *not* a child of another popup.
        // Ex: For a TooltipDialog with a button that spawns a tree of menus, find the popup of the button.
        var getTopPopup = function(){
            for(var pi=stack.length-1; pi > 0 && stack[pi].parent === stack[pi-1].widget; pi--){
                /* do nothing, just trying to get right value for pi */
            }
            return stack[pi];
        }

        // provide default escape and tab key handling
        // (this will work for any widget, not just menu)
        handlers.push(dojo.connect(wrapper, "onkeypress", this, function(evt){
            if(evt.charOrCode == dojo.keys.ESCAPE && args.onCancel){
                dojo.stopEvent(evt);
                args.onCancel();
            }else if(evt.charOrCode === dojo.keys.TAB){
                dojo.stopEvent(evt);
                var topPopup = getTopPopup();
                if(topPopup && topPopup.onCancel){
                    topPopup.onCancel();
                }
            }
        }));

        // watch for cancel/execute events on the popup and notify the caller
        // (for a menu, "execute" means clicking an item)
        if(widget.onCancel){
            handlers.push(dojo.connect(widget, "onCancel", null, args.onCancel));
        }

        handlers.push(dojo.connect(widget, widget.onExecute ? "onExecute" : "onChange", null, function(){
            var topPopup = getTopPopup();
            if(topPopup && topPopup.onExecute){
                topPopup.onExecute();
            }
        }));

        stack.push({
            wrapper: wrapper,
            iframe: iframe,
            widget: widget,
            parent: args.parent,
            onExecute: args.onExecute,
            onCancel: args.onCancel,
            onClose: args.onClose,
            handlers: handlers
        });

        if(widget.onOpen){
            widget.onOpen(best);
        }

        return best;
    };

    this.close = function(/*Widget*/ popup){
        // summary:
        //      Close specified popup and any popups that it parented
        while(dojo.some(stack, function(elem){return elem.widget == popup;})){
            var top = stack.pop(),
                wrapper = top.wrapper,
                iframe = top.iframe,
                widget = top.widget,
                onClose = top.onClose;

            if(widget.onClose){
                widget.onClose();
            }
            dojo.forEach(top.handlers, dojo.disconnect);

            // #2685: check if the widget still has a domNode so ContentPane can change its URL without getting an error
            if(!widget||!widget.domNode){ return; }

            this.prepare(widget.domNode);

            iframe.destroy();
            dojo.destroy(wrapper);

            if(onClose){
                onClose();
            }
        }
    };
}();

dijit._frames = new function(){
    // summary: cache of iframes
    var queue = [];

    this.pop = function(){
        var iframe;
        if(queue.length){
            iframe = queue.pop();
            iframe.style.display="";
        }else{
            if(dojo.isIE){
                var burl = dojo.config["dojoBlankHtmlUrl"] || (dojo.moduleUrl("dojo", "resources/blank.html")+"") || "javascript:\"\"";
                var html="<iframe src='" + burl + "'"
                    + " style='position: absolute; left: 0px; top: 0px;"
                    + "z-index: -1; filter:Alpha(Opacity=\"0\");'>";
                iframe = dojo.doc.createElement(html);
            }else{
                iframe = dojo.create("iframe");
                iframe.src = 'javascript:""';
                iframe.className = "dijitBackgroundIframe";
            }
            iframe.tabIndex = -1; // Magic to prevent iframe from getting focus on tab keypress - as style didnt work.
            dojo.body().appendChild(iframe);
        }
        return iframe;
    };

    this.push = function(iframe){
        iframe.style.display="";
        if(dojo.isIE){
            iframe.style.removeExpression("width");
            iframe.style.removeExpression("height");
        }
        queue.push(iframe);
    }
}();

// fill the queue
if(dojo.isIE < 7){
    dojo.addOnLoad(function(){
        var f = dijit._frames;
        dojo.forEach([f.pop()], f.push); //TODO can eliminate forEach
    });
}


dijit.BackgroundIframe = function(/* DomNode */node){
    //  summary:
    //      For IE z-index schenanigans. id attribute is required.
    //
    //  description:
    //      new dijit.BackgroundIframe(node)
    //          Makes a background iframe as a child of node, that fills
    //          area (and position) of node

    if(!node.id){ throw new Error("no id"); }
    if(dojo.isIE < 7 || (dojo.isFF < 3 && dojo.hasClass(dojo.body(), "dijit_a11y"))){
        var iframe = dijit._frames.pop();
        node.appendChild(iframe);
        if(dojo.isIE){
            iframe.style.setExpression("width", dojo._scopeName + ".doc.getElementById('" + node.id + "').offsetWidth");
            iframe.style.setExpression("height", dojo._scopeName + ".doc.getElementById('" + node.id + "').offsetHeight");
        }
        this.iframe = iframe;
    }
};

dojo.extend(dijit.BackgroundIframe, {
    destroy: function(){
        //  summary: destroy the iframe
        if(this.iframe){
            dijit._frames.push(this.iframe);
            delete this.iframe;
        }
    }
});

}

if(!dojo._hasResource["dijit._base.scroll"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.scroll"] = true;
dojo.provide("dijit._base.scroll");

dijit.scrollIntoView = function(/* DomNode */node){
    //  summary
    //  Scroll the passed node into view, if it is not.

    // don't rely on that node.scrollIntoView works just because the function is there
    // it doesnt work in Konqueror or Opera even though the function is there and probably
    //  not safari either
    // native scrollIntoView() causes FF3's whole window to scroll if there is no scroll bar
    //  on the immediate parent
    // dont like browser sniffs implementations but sometimes you have to use it
    // It's not enough just to scroll the menu node into view if
    // node.scrollIntoView hides part of the parent's scrollbar,
    // so just manage the parent scrollbar ourselves

    //var testdir="H"; //debug
    try{ // catch unexpected/unrecreatable errors (#7808) since we can recover using a semi-acceptable native method
    node = dojo.byId(node);
    var doc = dojo.doc;
    var body = dojo.body();
    var html = body.parentNode;
    // if FF2 (which is perfect) or an untested browser, then use the native method

    if(dojo.isFF < 3 || node == body || node == html || ((typeof node.scrollIntoView == "function") && !(dojo.isMoz || dojo.isIE || dojo.isWebKit))){ // FF2 is perfect, too bad FF3 is not
        node.scrollIntoView(false); // short-circuit to native if possible
        return;
    }
    var ltr = dojo._isBodyLtr();
    var rtl = !(ltr || (dojo.isIE >= 8 && !compatMode)); // IE8 mostly flips everything transparently (except border)
    // body and html elements are all messed up due to browser bugs and inconsistencies related to doctype
    // normalize the values before proceeding (FF2 is not listed since its native behavior is perfect)
    // for computation simplification, client and offset width and height are the same for body and html
    // strict:       html:       |      body:       | compatMode:
    //           width   height  |  width   height  |------------
    //    ie*:  clientW  clientH | scrollW  clientH | CSS1Compat
    //    ff3:  clientW  clientH |HscrollW  clientH | CSS1Compat
    //    sf3:  clientW  clientH | clientW HclientH | CSS1Compat
    //    op9:  clientW  clientH |HscrollW  clientH | CSS1Compat
    // ---------------------------------------------|-----------
    //   none:        html:      |      body:       |
    //           width    height |  width   height  |
    //    ie*: BclientW BclientH | clientW  clientH | BackCompat
    //    ff3: BclientW BclientH | clientW  clientH | BackCompat
    //    sf3:  clientW  clientH | clientW HclientH | CSS1Compat
    //    op9: BclientW BclientH | clientW  clientH | BackCompat
    // ---------------------------------------------|-----------
    //  loose:        html:      |      body:       |
    //           width    height |  width   height  |
    //    ie*:  clientW  clientH | scrollW  clientH | CSS1Compat
    //    ff3: BclientW BclientH | clientW  clientH | BackCompat
    //    sf3:  clientW  clientH | clientW HclientH | CSS1Compat
    //    op9:  clientW  clientH |HscrollW  clientH | CSS1Compat
    var scrollRoot = body;
    var compatMode = doc.compatMode == 'BackCompat';
    if(compatMode){ // BODY is scrollable, HTML has same client size
        // body client values already OK
        html._offsetWidth = html._clientWidth = body._offsetWidth = body.clientWidth;
        html._offsetHeight = html._clientHeight = body._offsetHeight = body.clientHeight;
    }else{
        if(dojo.isWebKit){
            body._offsetWidth = body._clientWidth  = html.clientWidth;
            body._offsetHeight = body._clientHeight = html.clientHeight;
        }else{
            scrollRoot = html;
        }
        html._offsetHeight = html.clientHeight;
        html._offsetWidth  = html.clientWidth;
    }

    var scrollBarSize = 17;
    if(dojo.isIE == 6){ scrollBarSize = 18; }
    else if(dojo.isWebKit){ scrollBarSize = 15; }

    function isFixedPosition(element){
        var ie = dojo.isIE;
        return ((ie <= 6 || (ie >= 7 && compatMode))? false : (dojo.style(element, 'position').toLowerCase() == "fixed"));
    }

    function addPseudoAttrs(element){
        var parent = element.parentNode;
        var offsetParent = element.offsetParent;
        if(offsetParent == null || isFixedPosition(element)){ // position:fixed has no real offsetParent
            offsetParent = html; // prevents exeptions
            parent = (element == body)? html : null;
        }
        // all the V/H object members below are to reuse code for both directions
        element._offsetParent = offsetParent; //(offsetParent == body)? scrollRoot : offsetParent;
        element._parent = parent; //(parent == body)? parent/*scrollRoot*/ : parent;
        //element._parentTag = element._parent?element._parent.tagName:'NULL'; //debug
        //element._offsetParentTag = element._offsetParent.tagName; //debug
        var bp = dojo._getBorderExtents(element);
        element._borderStart = { H:(dojo.isIE >= 8 && !ltr && !compatMode)?(bp.w-bp.l):bp.l, V:bp.t };
        element._borderSize = { H:bp.w, V:bp.h };
        element._offsetStart = { H:element.offsetLeft, V:element.offsetTop };
        //console.debug('element = ' + element.tagName + ', initial _relativeOffset = ' + element._offsetStart[testdir]);
        element._scrolledAmount = { H:element.scrollLeft, V:element.scrollTop };
        element._offsetSize = { H: element._offsetWidth||element.offsetWidth, V: element._offsetHeight||element.offsetHeight };
        //console.debug('element = ' + element.tagName + ', H size = ' + element.offsetWidth + ', parent = ' + element._parentTag);
        element._clientSize = { H:element._clientWidth||element.clientWidth, V:element._clientHeight||element.clientHeight };
        if(element != body && element != html && element != node){
            for(var dir in element._offsetSize){ // for both x and y directions
                var delta = element._offsetSize[dir] - element._clientSize[dir] - element._borderSize[dir];
                var hasScrollBar = element._clientSize[dir] > 0 && delta == scrollBarSize;
                if(hasScrollBar){
                    element._offsetSize[dir] -= scrollBarSize;
                    if(dojo.isIE && rtl && dir=="H"){ element._offsetStart[dir] += scrollBarSize; }
                }
            }
        }
    }

    var element = node;
    while(element != null){
        if(isFixedPosition(element)){ node.scrollIntoView(false); return; } //TODO: handle without native call
        addPseudoAttrs(element);
        element = element._parent;
    }
    if(dojo.isIE && node._parent){ // if no parent, then offsetParent._borderStart may not tbe set
        var offsetParent = node._offsetParent;
        //console.debug('adding offsetParent borderStart = ' + offsetParent._borderStart.H + ' to node offsetStart');
        node._offsetStart.H += offsetParent._borderStart.H;
        node._offsetStart.V += offsetParent._borderStart.V;
    }
    if(dojo.isIE >= 7 && scrollRoot == html && rtl && body._offsetStart && body._offsetStart.H == 0){ // IE7 bug
        var scroll = html.scrollWidth - html._offsetSize.H;
        if(scroll > 0){
            //console.debug('adjusting html scroll by ' + -scroll + ', scrollWidth = ' + html.scrollWidth + ', offsetSize = ' + html._offsetSize.H);
            body._offsetStart.H = -scroll;
        }
    }
    // eliminate offsetLeft/Top oddities by tweaking scroll for ease of computation
    if(rtl && body._offsetStart && scrollRoot == html && html._scrolledAmount){
        var ofs = body._offsetStart.H;
        if(ofs < 0){
            html._scrolledAmount.H += ofs;
            body._offsetStart.H = 0;
        }
    }
    element = node;
    while(element){
        var parent = element._parent;
        if(!parent){ break; }
            //console.debug('element = ' + element.tagName + ', parent = ' + parent.tagName + ', parent ' + testdir + ' offsetSize = ' + parent._offsetSize[testdir]);
            /*if(parent.tagName == "TD"){ //I could not recreate a scenario that exercised this IF statement
                var table = parent._parent._parent._parent; // point to TABLE
                if(table._offsetParent == element._offsetParent && parent._offsetParent != element._offsetParent){
                    alert('in TD');
                    parent = table; // child of TD has the same offsetParent as TABLE, so skip TD, TR, and TBODY (ie. verticalslider)
                }
            }*/
            // check if this node and its parent share the same offsetParent
            var relative = element._offsetParent == parent;
            //console.debug('element = ' + element.tagName + ', offsetParent = ' + element._offsetParent.tagName + ', parent = ' + parent.tagName + ', relative = ' + relative);
            for(var dir in element._offsetStart){ // for both x and y directions
                if(dojo.isIE >= 8 && !relative && !compatMode){ // offsetLeft/Top change in realtime to reflect the scroll amount
                    //console.debug('zeroing out scroll amount = ' + parent._scrolledAmount[dir]);
                    parent._scrolledAmount[dir] = 0;
                }
                var scrollFlipped = false;
                var otherDir = dir=="H"? "V" : "H";
                if(rtl && dir=="H" && (parent != html) && (parent != body) && (dojo.isIE || dojo.isWebKit) && parent._clientSize.H > 0 && parent.scrollWidth > parent._clientSize.H){ // scroll starts on the right
                    var delta = parent.scrollWidth - parent._clientSize.H;
                    //console.debug('rtl scroll delta = ' + delta + ', changing ' + parent.tagName + ' scroll from ' + parent._scrolledAmount.H + ' to ' + (parent._scrolledAmount.H - delta)  + ', parent.scrollWidth = ' + parent.scrollWidth + ', parent._clientSize.H = ' + parent._clientSize.H);
                    if(delta > 0){
                        parent._scrolledAmount.H -= delta;
                        scrollFlipped = true;
                    } // match FF3 which has cool negative scrollLeft values
                }
                if(parent._offsetParent.tagName == "TABLE"){ // make it consistent
                    if(dojo.isIE){ // make it consistent with Safari and FF3 and exclude the starting TABLE border of TABLE children
                        parent._offsetStart[dir] -= parent._offsetParent._borderStart[dir];
                        parent._borderStart[dir] = parent._borderSize[dir] = 0;
                    }
                    else{
                        parent._offsetStart[dir] += parent._offsetParent._borderStart[dir];
                    }
                }
                //if(dir==testdir)console.debug('border start = ' + parent._borderStart[dir] + ',  border size = ' + parent._borderSize[dir]);
                if(dojo.isIE){
                //if(dir==testdir)console.debug('changing parent offsetStart from ' + parent._offsetStart[dir] + ' by adding offsetParent ' + parent._offsetParent.tagName + ' border start = ' + parent._offsetParent._borderStart[dir]);
                    parent._offsetStart[dir] += parent._offsetParent._borderStart[dir];
                }
                //if(dir==testdir)console.debug('subtracting border start = ' + parent._borderStart[dir]);
                // underflow = visible gap between parent and this node taking scrolling into account
                // if negative, part of the node is obscured by the parent's beginning and should be scrolled to become visible
                var underflow = element._offsetStart[dir] - parent._scrolledAmount[dir] - (relative? 0 : parent._offsetStart[dir]) - parent._borderStart[dir];
                // if overflow is positive, number of pixels obscured by the parent's end
                var overflow = underflow + element._offsetSize[dir] - parent._offsetSize[dir] + parent._borderSize[dir];
                //if(dir==testdir)console.debug('element = ' + element.tagName + ', offsetStart = ' + element._offsetStart[dir] + ', relative = ' + relative + ', parent offsetStart = ' + parent._offsetStart[dir] + ', scroll = ' + parent._scrolledAmount[dir] + ', parent border start = ' + parent._borderStart[dir] + ', parent border size = ' + parent._borderSize[dir] + ', underflow = ' + underflow + ', overflow = ' + overflow + ', element offsetSize = ' + element._offsetSize[dir] + ', parent offsetSize = ' + parent._offsetSize[dir]);
                var scrollAmount, scrollAttr = (dir=="H")? "scrollLeft" : "scrollTop";
                // see if we should scroll forward or backward
                var reverse = dir=="H" && rtl; // flip everything
                var underflowScroll = reverse? -overflow : underflow;
                var overflowScroll = reverse? -underflow : overflow;
                if(underflowScroll <= 0){
                    scrollAmount = underflowScroll;
                }else if(overflowScroll <= 0){
                    scrollAmount = 0;
                }else if(underflowScroll < overflowScroll){
                    scrollAmount = underflowScroll;
                }else{
                    scrollAmount = overflowScroll;
                }
                //if(dir==testdir)console.debug('element = ' + element.tagName + ' dir = ' + dir + ', scrollAmount = ' + scrollAmount);
                var scrolledAmount = 0;
                if(scrollAmount != 0){
                    var oldScroll = parent[scrollAttr];
                    parent[scrollAttr] += (reverse)? -scrollAmount : scrollAmount; // actually perform the scroll
                    scrolledAmount = parent[scrollAttr] - oldScroll; // in case the scroll failed
                    //if(dir==testdir)console.debug('scrolledAmount = ' + scrolledAmount);
                    element._offsetStart[dir] -= scrolledAmount;
                }
            }
            element._parent = parent._parent;
            element._offsetParent = parent._offsetParent;
            if(relative){
                element._offsetStart.H += parent._offsetStart.H; // RTL can cause negative offsetLeft values
                element._offsetStart.V += parent._offsetStart.V;
            }
    }
    parent = node;
    while(parent && parent.removeAttribute){
        next = parent.parentNode;
        parent.removeAttribute('_offsetParent');
        parent.removeAttribute('_parent');
        parent = next;
    }
    }catch(error){
        console.debug('scrollIntoView: ' + error);
        node.scrollIntoView(false);
    }
};

}

if(!dojo._hasResource["dijit._base.sniff"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.sniff"] = true;
//  summary:
//      Applies pre-set CSS classes to the top-level HTML node, based on:
//          - browser (ex: dj_ie)
//          - browser version (ex: dj_ie6)
//          - box model (ex: dj_contentBox)
//          - text direction (ex: dijitRtl)
//
//      In addition, browser, browser version, and box model are
//      combined with an RTL flag when browser text is RTL.  ex: dj_ie-rtl.
//
//      Simply doing a require on this module will
//      establish this CSS.  Modified version of Morris' CSS hack.

dojo.provide("dijit._base.sniff");

(function(){

    var d = dojo,
        html = d.doc.documentElement,
        ie = d.isIE,
        opera = d.isOpera,
        maj = Math.floor,
        ff = d.isFF,
        boxModel = d.boxModel.replace(/-/,''),
        classes = {
            dj_ie: ie,
//          dj_ie55: ie == 5.5,
            dj_ie6: maj(ie) == 6,
            dj_ie7: maj(ie) == 7,
            dj_iequirks: ie && d.isQuirks,
            // NOTE: Opera not supported by dijit
            dj_opera: opera,
            dj_opera8: maj(opera) == 8,
            dj_opera9: maj(opera) == 9,
            dj_khtml: d.isKhtml,
            dj_webkit: d.isWebKit,
            dj_safari: d.isSafari,
            dj_gecko: d.isMozilla,
            dj_ff2: maj(ff) == 2,
            dj_ff3: maj(ff) == 3
        }; // no dojo unsupported browsers

    classes["dj_" + boxModel] = true;

    // apply browser, browser version, and box model class names
    for(var p in classes){
        if(classes[p]){
            if(html.className){
                html.className += " " + p;
            }else{
                html.className = p;
            }
        }
    }

    // If RTL mode then add dijitRtl flag plus repeat existing classes
    // with -rtl extension
    // (unshift is to make this code run after <body> node is loaded but before parser runs)
    dojo._loaders.unshift(function(){
        if(!dojo._isBodyLtr()){
            html.className += " dijitRtl";
            for(var p in classes){
                if(classes[p]){
                    html.className += " " + p + "-rtl";
                }
            }
        }
    });

})();

}

if(!dojo._hasResource["dijit._base.typematic"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.typematic"] = true;
dojo.provide("dijit._base.typematic");

dijit.typematic = {
    // summary:
    //  These functions are used to repetitively call a user specified callback
    //  method when a specific key or mouse click over a specific DOM node is
    //  held down for a specific amount of time.
    //  Only 1 such event is allowed to occur on the browser page at 1 time.

    _fireEventAndReload: function(){
        this._timer = null;
        this._callback(++this._count, this._node, this._evt);
        this._currentTimeout = (this._currentTimeout < 0) ? this._initialDelay : ((this._subsequentDelay > 1) ? this._subsequentDelay : Math.round(this._currentTimeout * this._subsequentDelay));
        this._timer = setTimeout(dojo.hitch(this, "_fireEventAndReload"), this._currentTimeout);
    },

    trigger: function(/*Event*/ evt, /* Object */ _this, /*DOMNode*/ node, /* Function */ callback, /* Object */ obj, /* Number */ subsequentDelay, /* Number */ initialDelay){
        // summary:
        //      Start a timed, repeating callback sequence.
        //      If already started, the function call is ignored.
        //      This method is not normally called by the user but can be
        //      when the normal listener code is insufficient.
        //  Parameters:
        //  evt: key or mouse event object to pass to the user callback
        //  _this: pointer to the user's widget space.
        //  node: the DOM node object to pass the the callback function
        //  callback: function to call until the sequence is stopped called with 3 parameters:
        //      count: integer representing number of repeated calls (0..n) with -1 indicating the iteration has stopped
        //      node: the DOM node object passed in
        //      evt: key or mouse event object
        //  obj: user space object used to uniquely identify each typematic sequence
        //  subsequentDelay: if > 1, the number of milliseconds until the 3->n events occur
        //      or else the fractional time multiplier for the next event's delay, default=0.9
        //  initialDelay: the number of milliseconds until the 2nd event occurs, default=500ms
        if(obj != this._obj){
            this.stop();
            this._initialDelay = initialDelay || 500;
            this._subsequentDelay = subsequentDelay || 0.90;
            this._obj = obj;
            this._evt = evt;
            this._node = node;
            this._currentTimeout = -1;
            this._count = -1;
            this._callback = dojo.hitch(_this, callback);
            this._fireEventAndReload();
        }
    },

    stop: function(){
        // summary:
        //    Stop an ongoing timed, repeating callback sequence.
        if(this._timer){
            clearTimeout(this._timer);
            this._timer = null;
        }
        if(this._obj){
            this._callback(-1, this._node, this._evt);
            this._obj = null;
        }
    },

    addKeyListener: function(/*DOMNode*/ node, /*Object*/ keyObject, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
        // summary: Start listening for a specific typematic key.
        //  keyObject: an object defining the key to listen for.
        //      charOrCode: the printable character (string) or keyCode (number) to listen for.
        //          keyCode: (deprecated - use charOrCode) the keyCode (number) to listen for (implies charCode = 0).
        //          charCode: (deprecated - use charOrCode) the charCode (number) to listen for.
        //      ctrlKey: desired ctrl key state to initiate the calback sequence:
        //          pressed (true)
        //          released (false)
        //          either (unspecified)
        //      altKey: same as ctrlKey but for the alt key
        //      shiftKey: same as ctrlKey but for the shift key
        //  See the trigger method for other parameters.
        //  Returns an array of dojo.connect handles
        if(keyObject.keyCode){
            keyObject.charOrCode = keyObject.keyCode;
            dojo.deprecated("keyCode attribute parameter for dijit.typematic.addKeyListener is deprecated. Use charOrCode instead.", "", "2.0");
        }else if(keyObject.charCode){
            keyObject.charOrCode = String.fromCharCode(keyObject.charCode);
            dojo.deprecated("charCode attribute parameter for dijit.typematic.addKeyListener is deprecated. Use charOrCode instead.", "", "2.0");
        }
        return [
            dojo.connect(node, "onkeypress", this, function(evt){
                if(evt.charOrCode == keyObject.charOrCode &&
                (keyObject.ctrlKey === undefined || keyObject.ctrlKey == evt.ctrlKey) &&
                (keyObject.altKey === undefined || keyObject.altKey == evt.ctrlKey) &&
                (keyObject.shiftKey === undefined || keyObject.shiftKey == evt.ctrlKey)){
                    dojo.stopEvent(evt);
                    dijit.typematic.trigger(keyObject, _this, node, callback, keyObject, subsequentDelay, initialDelay);
                }else if(dijit.typematic._obj == keyObject){
                    dijit.typematic.stop();
                }
            }),
            dojo.connect(node, "onkeyup", this, function(evt){
                if(dijit.typematic._obj == keyObject){
                    dijit.typematic.stop();
                }
            })
        ];
    },

    addMouseListener: function(/*DOMNode*/ node, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
        // summary: Start listening for a typematic mouse click.
        //  See the trigger method for other parameters.
        //  Returns an array of dojo.connect handles
        var dc = dojo.connect;
        return [
            dc(node, "mousedown", this, function(evt){
                dojo.stopEvent(evt);
                dijit.typematic.trigger(evt, _this, node, callback, node, subsequentDelay, initialDelay);
            }),
            dc(node, "mouseup", this, function(evt){
                dojo.stopEvent(evt);
                dijit.typematic.stop();
            }),
            dc(node, "mouseout", this, function(evt){
                dojo.stopEvent(evt);
                dijit.typematic.stop();
            }),
            dc(node, "mousemove", this, function(evt){
                dojo.stopEvent(evt);
            }),
            dc(node, "dblclick", this, function(evt){
                dojo.stopEvent(evt);
                if(dojo.isIE){
                    dijit.typematic.trigger(evt, _this, node, callback, node, subsequentDelay, initialDelay);
                    setTimeout(dojo.hitch(this, dijit.typematic.stop), 50);
                }
            })
        ];
    },

    addListener: function(/*Node*/ mouseNode, /*Node*/ keyNode, /*Object*/ keyObject, /*Object*/ _this, /*Function*/ callback, /*Number*/ subsequentDelay, /*Number*/ initialDelay){
        // summary: Start listening for a specific typematic key and mouseclick.
        //  This is a thin wrapper to addKeyListener and addMouseListener.
        //  mouseNode: the DOM node object to listen on for mouse events.
        //  keyNode: the DOM node object to listen on for key events.
        //  See the addMouseListener and addKeyListener methods for other parameters.
        //  Returns an array of dojo.connect handles
        return this.addKeyListener(keyNode, keyObject, _this, callback, subsequentDelay, initialDelay).concat(
            this.addMouseListener(mouseNode, _this, callback, subsequentDelay, initialDelay));
    }
};

}

if(!dojo._hasResource["dijit._base.wai"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base.wai"] = true;
dojo.provide("dijit._base.wai");

dijit.wai = {
    onload: function(){
        // summary:
        //      Detects if we are in high-contrast mode or not

        // This must be a named function and not an anonymous
        // function, so that the widget parsing code can make sure it
        // registers its onload function after this function.
        // DO NOT USE "this" within this function.

        // create div for testing if high contrast mode is on or images are turned off
        var div = dojo.create("div",{
            id: "a11yTestNode",
            style:{
                cssText:'border: 1px solid;'
                    + 'border-color:red green;'
                    + 'position: absolute;'
                    + 'height: 5px;'
                    + 'top: -999px;'
                    + 'background-image: url("' + (dojo.config.blankGif || dojo.moduleUrl("dojo", "resources/blank.gif")) + '");'
            }
        }, dojo.body());

        // test it
        var cs = dojo.getComputedStyle(div);
        if(cs){
            var bkImg = cs.backgroundImage;
            var needsA11y = (cs.borderTopColor==cs.borderRightColor) || (bkImg != null && (bkImg == "none" || bkImg == "url(invalid-url:)" ));
            dojo[needsA11y ? "addClass" : "removeClass"](dojo.body(), "dijit_a11y");
            if(dojo.isIE){
                div.outerHTML = "";     // prevent mixed-content warning, see http://support.microsoft.com/kb/925014
            }else{
                dojo.body().removeChild(div);
            }
        }
    }
};

// Test if computer is in high contrast mode.
// Make sure the a11y test runs first, before widgets are instantiated.
if(dojo.isIE || dojo.isMoz){    // NOTE: checking in Safari messes things up
    dojo._loaders.unshift(dijit.wai.onload);
}

dojo.mixin(dijit,
{
    _XhtmlRoles: /banner|contentinfo|definition|main|navigation|search|note|secondary|seealso/,

    hasWaiRole: function(/*Element*/ elem, /*String*/ role){
        // summary: Determines if an element has a particular non-XHTML role.
        // returns: true if elem has the specific non-XHTML role attribute and false if not.
        //      for backwards compatibility if role parameter not provided,
        //      returns true if has non XHTML role
        var waiRole = this.getWaiRole(elem);
        return role ? (waiRole.indexOf(role) > -1) : (waiRole.length > 0);
    },

    getWaiRole: function(/*Element*/ elem){
        // summary: Gets the non-XHTML role for an element (which should be a wai role).
        // returns:
        //      The non-XHTML role of elem or an empty string if elem
        //      does not have a role.
         return dojo.trim((dojo.attr(elem, "role") || "").replace(this._XhtmlRoles,"").replace("wairole:",""));
    },

    setWaiRole: function(/*Element*/ elem, /*String*/ role){
        // summary: Sets the role on an element.
        // description:
        //      in other than FF2 replace existing role attribute with new role
        //      FF3 supports XHTML and ARIA roles so
        //      If elem already has an XHTML role, append this role to XHTML role
        //      and remove other ARIA roles
        //      On Firefox 2 and below, "wairole:" is
        //      prepended to the provided role value.

        var curRole = dojo.attr(elem, "role") || "";
        if(dojo.isFF < 3 || !this._XhtmlRoles.test(curRole)){
            dojo.attr(elem, "role", dojo.isFF < 3 ? "wairole:" + role : role);
        }else{
            if((" "+ curRole +" ").indexOf(" " + role + " ") < 0){
                var clearXhtml = dojo.trim(curRole.replace(this._XhtmlRoles, ""));
                var cleanRole = dojo.trim(curRole.replace(clearXhtml, ""));
                dojo.attr(elem, "role", cleanRole + (cleanRole ? ' ' : '') + role);
            }
        }
    },

    removeWaiRole: function(/*Element*/ elem, /*String*/ role){
        // summary: Removes the specified non-XHTML role from an element.
        //      removes role attribute if no specific role provided (for backwards compat.)

        var roleValue = dojo.attr(elem, "role");
        if(!roleValue){ return; }
        if(role){
            var searchRole = dojo.isFF < 3 ? "wairole:" + role : role;
            var t = dojo.trim((" " + roleValue + " ").replace(" " + searchRole + " ", " "));
            dojo.attr(elem, "role", t);
        }else{
            elem.removeAttribute("role");
        }
    },

    hasWaiState: function(/*Element*/ elem, /*String*/ state){
        // summary: Determines if an element has a given state.
        // description:
        //      On Firefox 2 and below, we check for an attribute in namespace
        //      "http://www.w3.org/2005/07/aaa" with a name of the given state.
        //      On all other browsers, we check for an attribute
        //      called "aria-"+state.
        // returns:
        //      true if elem has a value for the given state and
        //      false if it does not.
        if(dojo.isFF < 3){
            return elem.hasAttributeNS("http://www.w3.org/2005/07/aaa", state);
        }
        return elem.hasAttribute ? elem.hasAttribute("aria-"+state) : !!elem.getAttribute("aria-"+state);
    },

    getWaiState: function(/*Element*/ elem, /*String*/ state){
        // summary: Gets the value of a state on an element.
        // description:
        //      On Firefox 2 and below, we check for an attribute in namespace
        //      "http://www.w3.org/2005/07/aaa" with a name of the given state.
        //      On all other browsers, we check for an attribute called
        //      "aria-"+state.
        // returns:
        //      The value of the requested state on elem
        //      or an empty string if elem has no value for state.
        if(dojo.isFF < 3){
            return elem.getAttributeNS("http://www.w3.org/2005/07/aaa", state);
        }
        return elem.getAttribute("aria-"+state) || "";
    },

    setWaiState: function(/*Element*/ elem, /*String*/ state, /*String*/ value){
        // summary: Sets a state on an element.
        // description:
        //      On Firefox 2 and below, we set an attribute in namespace
        //      "http://www.w3.org/2005/07/aaa" with a name of the given state.
        //      On all other browsers, we set an attribute called
        //      "aria-"+state.
        if(dojo.isFF < 3){
            elem.setAttributeNS("http://www.w3.org/2005/07/aaa",
                "aaa:"+state, value);
        }else{
            elem.setAttribute("aria-"+state, value);
        }
    },

    removeWaiState: function(/*Element*/ elem, /*String*/ state){
        // summary: Removes a state from an element.
        // description:
        //      On Firefox 2 and below, we remove the attribute in namespace
        //      "http://www.w3.org/2005/07/aaa" with a name of the given state.
        //      On all other browsers, we remove the attribute called
        //      "aria-"+state.
        if(dojo.isFF < 3){
            elem.removeAttributeNS("http://www.w3.org/2005/07/aaa", state);
        }else{
            elem.removeAttribute("aria-"+state);
        }
    }
});

}

if(!dojo._hasResource["dijit._base"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._base"] = true;
dojo.provide("dijit._base");











}

if(!dojo._hasResource["dijit._Widget"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._Widget"] = true;
dojo.provide("dijit._Widget");

dojo.require( "dijit._base" );

dojo.connect(dojo, "connect",
    function(/*Widget*/ widget, /*String*/ event){
        if(widget && dojo.isFunction(widget._onConnect)){
            widget._onConnect(event);
        }
    });

dijit._connectOnUseEventHandler = function(/*Event*/ event){};

(function(){

var _attrReg = {};
var getAttrReg = function(dc){
    if(!_attrReg[dc]){
        var r = [];
        var attrs;
        var proto = dojo.getObject(dc).prototype;
        for(var fxName in proto){
            if(dojo.isFunction(proto[fxName]) && (attrs = fxName.match(/^_set([a-zA-Z]*)Attr$/)) && attrs[1]){
                r.push(attrs[1].charAt(0).toLowerCase() + attrs[1].substr(1));
            }
        }
        _attrReg[dc] = r;
    }
    return _attrReg[dc]||[];
}

dojo.declare("dijit._Widget", null, {
    //  summary:
    //      The foundation of dijit widgets.
    //
    //  id: String
    //      a unique, opaque ID string that can be assigned by users or by the
    //      system. If the developer passes an ID which is known not to be
    //      unique, the specified ID is ignored and the system-generated ID is
    //      used instead.
    id: "",

    //  lang: String
    //      Rarely used.  Overrides the default Dojo locale used to render this widget,
    //      as defined by the [HTML LANG](http://www.w3.org/TR/html401/struct/dirlang.html#adef-lang) attribute.
    //      Value must be among the list of locales specified during by the Dojo bootstrap,
    //      formatted according to [RFC 3066](http://www.ietf.org/rfc/rfc3066.txt) (like en-us).
    lang: "",

    //  dir: String
    //      Unsupported by Dijit, but here for completeness.  Dijit only supports setting text direction on the
    //      entire document.
    //      Bi-directional support, as defined by the [HTML DIR](http://www.w3.org/TR/html401/struct/dirlang.html#adef-dir)
    //      attribute. Either left-to-right "ltr" or right-to-left "rtl".
    dir: "",

    // class: String
    //      HTML class attribute
    "class": "",

    // style: String
    //      HTML style attribute
    style: "",

    // title: String
    //      HTML title attribute
    title: "",

    // srcNodeRef: DomNode
    //      pointer to original dom node
    srcNodeRef: null,

    // domNode: DomNode
    //      This is our visible representation of the widget! Other DOM
    //      Nodes may by assigned to other properties, usually through the
    //      template system's dojoAttachPoint syntax, but the domNode
    //      property is the canonical "top level" node in widget UI.
    domNode: null,

    // containerNode: DomNode
    //      Designates where children of the source dom node will be placed.
    //      "Children" in this case refers to both dom nodes and widgets.
    //      For example, for myWidget:
    //
    //      |   <div dojoType=myWidget>
    //      |       <b> here's a plain dom node
    //      |       <span dojoType=subWidget>and a widget</span>
    //      |       <i> and another plain dom node </i>
    //      |   </div>
    //
    //      containerNode would point to:
    //
    //      |       <b> here's a plain dom node
    //      |       <span dojoType=subWidget>and a widget</span>
    //      |       <i> and another plain dom node </i>
    //
    //      In templated widgets, "containerNode" is set via a
    //      dojoAttachPoint assignment.
    //
    //      containerNode must be defined for any widget that accepts innerHTML
    //      (like ContentPane or BorderContainer or even Button), and conversely
    //      is null for widgets that don't, like TextBox.
    containerNode: null,

    // attributeMap: Object
    //      attributeMap sets up a "binding" between attributes (aka properties)
    //      of the widget and the widget's DOM.
    //      Changes to widget attributes listed in attributeMap will be
    //      reflected into the DOM.
    //
    //      For example, calling attr('title', 'hello')
    //      on a TitlePane will automatically cause the TitlePane's DOM to update
    //      with the new title.
    //
    //      attributeMap is a hash where the key is an attribute of the widget,
    //      and the value reflects a binding to a:
    //
    //      - DOM node attribute
    // |        focus: {node: "focusNode", type: "attribute"}
    //      Maps this.focus to this.focusNode.focus
    //
    //      - DOM node innerHTML
    //  |       title: { node: "titleNode", type: "innerHTML" }
    //      Maps this.title to this.titleNode.innerHTML
    //
    //      - DOM node CSS class
    // |        myClass: { node: "domNode", type: "class" }
    //      Maps this.myClass to this.domNode.className
    //
    //      If the value is an array, then each element in the array matches one of the
    //      formats of the above list.
    //
    //      There are also some shorthands for backwards compatibility:
    //      - string --> { node: string, type: "attribute" }, for example:
    //  |   "focusNode" ---> { node: "focusNode", type: "attribute" }
    //      - "" --> { node: "domNode", type: "attribute" }
    attributeMap: {id:"", dir:"", lang:"", "class":"", style:"", title:""},

    // _deferredConnects: Object
    //      attributeMap addendum for event handlers that should be connected only on first use
    _deferredConnects: {
        onClick: "",
        onDblClick: "",
        onKeyDown: "",
        onKeyPress: "",
        onKeyUp: "",
        onMouseMove: "",
        onMouseDown: "",
        onMouseOut: "",
        onMouseOver: "",
        onMouseLeave: "",
        onMouseEnter: "",
        onMouseUp: ""},

    onClick: dijit._connectOnUseEventHandler,
    /*=====
    onClick: function(event){
        // summary:
        //  Connect to this function to receive notifications of mouse click events.
        //  event: mouse Event
    },
    =====*/
    onDblClick: dijit._connectOnUseEventHandler,
    /*=====
    onDblClick: function(event){
        // summary:
        //  Connect to this function to receive notifications of mouse double click events.
        //  event: mouse Event
    },
    =====*/
    onKeyDown: dijit._connectOnUseEventHandler,
    /*=====
    onKeyDown: function(event){
        // summary:
        //  Connect to this function to receive notifications of keys being pressed down.
        //  event: key Event
    },
    =====*/
    onKeyPress: dijit._connectOnUseEventHandler,
    /*=====
    onKeyPress: function(event){
        // summary:
        //  Connect to this function to receive notifications of printable keys being typed.
        //  event: key Event
    },
    =====*/
    onKeyUp: dijit._connectOnUseEventHandler,
    /*=====
    onKeyUp: function(event){
        // summary:
        //  Connect to this function to receive notifications of keys being released.
        //  event: key Event
    },
    =====*/
    onMouseDown: dijit._connectOnUseEventHandler,
    /*=====
    onMouseDown: function(event){
        // summary:
        //  Connect to this function to receive notifications of when the mouse button is pressed down.
        //  event: mouse Event
    },
    =====*/
    onMouseMove: dijit._connectOnUseEventHandler,
    /*=====
    onMouseMove: function(event){
        // summary:
        //  Connect to this function to receive notifications of when the mouse moves over nodes contained within this widget.
        //  event: mouse Event
    },
    =====*/
    onMouseOut: dijit._connectOnUseEventHandler,
    /*=====
    onMouseOut: function(event){
        // summary:
        //  Connect to this function to receive notifications of when the mouse moves off of nodes contained within this widget.
        //  event: mouse Event
    },
    =====*/
    onMouseOver: dijit._connectOnUseEventHandler,
    /*=====
    onMouseOver: function(event){
        // summary:
        //  Connect to this function to receive notifications of when the mouse moves onto nodes contained within this widget.
        //  event: mouse Event
    },
    =====*/
    onMouseLeave: dijit._connectOnUseEventHandler,
    /*=====
    onMouseLeave: function(event){
        // summary:
        //  Connect to this function to receive notifications of when the mouse moves off of this widget.
        //  event: mouse Event
    },
    =====*/
    onMouseEnter: dijit._connectOnUseEventHandler,
    /*=====
    onMouseEnter: function(event){
        // summary:
        //  Connect to this function to receive notifications of when the mouse moves onto this widget.
        //  event: mouse Event
    },
    =====*/
    onMouseUp: dijit._connectOnUseEventHandler,
    /*=====
    onMouseUp: function(event){
        // summary:
        //  Connect to this function to receive notifications of when the mouse button is released.
        //  event: mouse Event
    },
    =====*/

    // Constants used in templates
    _blankGif: (dojo.config.blankGif || dojo.moduleUrl("dojo", "resources/blank.gif")),

    //////////// INITIALIZATION METHODS ///////////////////////////////////////

    postscript: function(/*Object?*/params, /*DomNode|String*/srcNodeRef){
        // summary: kicks off widget instantiation, see create() for details.
        this.create(params, srcNodeRef);
    },

    create: function(/*Object?*/params, /*DomNode|String?*/srcNodeRef){
        //  summary:
        //      Kick off the life-cycle of a widget
        //  params:
        //      Hash of initialization parameters for widget, including
        //      scalar values (like title, duration etc.) and functions,
        //      typically callbacks like onClick.
        //  srcNodeRef:
        //      If a srcNodeRef (dom node) is specified:
        //          - use srcNodeRef.innerHTML as my contents
        //          - if this is a behavioral widget then apply behavior
        //            to that srcNodeRef
        //          - otherwise, replace srcNodeRef with my generated DOM
        //            tree
        //  description:
        //      To understand the process by which widgets are instantiated, it
        //      is critical to understand what other methods create calls and
        //      which of them you'll want to override. Of course, adventurous
        //      developers could override create entirely, but this should
        //      only be done as a last resort.
        //
        //      Below is a list of the methods that are called, in the order
        //      they are fired, along with notes about what they do and if/when
        //      you should over-ride them in your widget:
        //
        // * postMixInProperties:
        //  |   * a stub function that you can over-ride to modify
        //      variables that may have been naively assigned by
        //      mixInProperties
        // * widget is added to manager object here
        // * buildRendering:
        //  |   * Subclasses use this method to handle all UI initialization
        //      Sets this.domNode.  Templated widgets do this automatically
        //      and otherwise it just uses the source dom node.
        // * postCreate:
        //  |   * a stub function that you can over-ride to modify take
        //      actions once the widget has been placed in the UI

        // store pointer to original dom tree
        this.srcNodeRef = dojo.byId(srcNodeRef);

        // For garbage collection.  An array of handles returned by Widget.connect()
        // Each handle returned from Widget.connect() is an array of handles from dojo.connect()
        this._connects = [];

        // To avoid double-connects, remove entries from _deferredConnects
        // that have been setup manually by a subclass (ex, by dojoAttachEvent).
        // If a subclass has redefined a callback (ex: onClick) then assume it's being
        // connected to manually.
        this._deferredConnects = dojo.clone(this._deferredConnects);
        for(var attr in this.attributeMap){
            delete this._deferredConnects[attr]; // can't be in both attributeMap and _deferredConnects
        }
        for(attr in this._deferredConnects){
            if(this[attr] !== dijit._connectOnUseEventHandler){
                delete this._deferredConnects[attr];    // redefined, probably dojoAttachEvent exists
            }
        }

        //mixin our passed parameters
        if(this.srcNodeRef && (typeof this.srcNodeRef.id == "string")){ this.id = this.srcNodeRef.id; }
        if(params){
            this.params = params;
            dojo.mixin(this,params);
        }
        this.postMixInProperties();

        // generate an id for the widget if one wasn't specified
        // (be sure to do this before buildRendering() because that function might
        // expect the id to be there.)
        if(!this.id){
            this.id = dijit.getUniqueId(this.declaredClass.replace(/\./g,"_"));
        }
        dijit.registry.add(this);

        this.buildRendering();

        if(this.domNode){
            // Copy attributes listed in attributeMap into the [newly created] DOM for the widget.
            this._applyAttributes();

            var source = this.srcNodeRef;
            if(source && source.parentNode){
                source.parentNode.replaceChild(this.domNode, source);
            }

            // If the developer has specified a handler as a widget parameter
            // (ex: new Button({onClick: ...})
            // then naturally need to connect from dom node to that handler immediately,
            for(attr in this.params){
                this._onConnect(attr);
            }
        }

        if(this.domNode){
            this.domNode.setAttribute("widgetId", this.id);
        }
        this.postCreate();

        // If srcNodeRef has been processed and removed from the DOM (e.g. TemplatedWidget) then delete it to allow GC.
        if(this.srcNodeRef && !this.srcNodeRef.parentNode){
            delete this.srcNodeRef;
        }

        this._created = true;
    },

    _applyAttributes: function(){
        // summary:
        //      Step during widget creation to copy all widget attributes to the
        //      DOM as per attributeMap and _setXXXAttr functions.
        // description:
        //      Skips over blank/false attribute values, unless they were explicitly specified
        //      as parameters to the widget, since those are the default anyway,
        //      and setting tabIndex="" is different than not setting tabIndex at all.
        //
        //      It processes the attributes in the attribute map first, ant then
        //      it goes through and processes the attributes for the _setXXXAttr
        //      functions that have been specified
        var condAttrApply = function(attr, scope){
            if( (scope.params && attr in scope.params) || scope[attr]){
                scope.attr(attr, scope[attr]);
            }
        };
        for(var attr in this.attributeMap){
            condAttrApply(attr, this);
        }
        dojo.forEach(getAttrReg(this.declaredClass), function(a){
            if(!(a in this.attributeMap)){
                condAttrApply(a, this);
            }
        }, this);
    },

    postMixInProperties: function(){
        // summary:
        //      Called after the parameters to the widget have been read-in,
        //      but before the widget template is instantiated. Especially
        //      useful to set properties that are referenced in the widget
        //      template.
    },

    buildRendering: function(){
        // summary:
        //      Construct the UI for this widget, setting this.domNode.  Most
        //      widgets will mixin dijit._Templated, which implements this
        //      method.
        this.domNode = this.srcNodeRef || dojo.create('div');
    },

    postCreate: function(){
        // summary:
        //      Called after a widget's dom has been setup
    },

    startup: function(){
        // summary:
        //      Called after a widget's children, and other widgets on the page, have been created.
        //      Provides an opportunity to manipulate any children before they are displayed.
        //      This is useful for composite widgets that need to control or layout sub-widgets.
        //      Many layout widgets can use this as a wiring phase.
        this._started = true;
    },

    //////////// DESTROY FUNCTIONS ////////////////////////////////

    destroyRecursive: function(/*Boolean?*/ preserveDom){
        // summary:
        //      Destroy this widget and it's descendants. This is the generic
        //      "destructor" function that all widget users should call to
        //      cleanly discard with a widget. Once a widget is destroyed, it's
        //      removed from the manager object.
        // preserveDom:
        //      If true, this method will leave the original Dom structure
        //      alone of descendant Widgets. Note: This will NOT work with
        //      dijit._Templated widgets.
        //
        this.destroyDescendants(preserveDom);
        this.destroy(preserveDom);
    },

    destroy: function(/*Boolean*/ preserveDom){
        // summary:
        //      Destroy this widget, but not its descendants.
        //      Will, however, destroy internal widgets such as those used within a template.
        // preserveDom: Boolean
        //      If true, this method will leave the original Dom structure alone.
        //      Note: This will not yet work with _Templated widgets

        this.uninitialize();
        dojo.forEach(this._connects, function(array){
            dojo.forEach(array, dojo.disconnect);
        });

        // destroy widgets created as part of template, etc.
        dojo.forEach(this._supportingWidgets||[], function(w){
            if(w.destroy){
                w.destroy();
            }
        });

        this.destroyRendering(preserveDom);
        dijit.registry.remove(this.id);
    },

    destroyRendering: function(/*Boolean?*/ preserveDom){
        // summary:
        //      Destroys the DOM nodes associated with this widget
        // preserveDom:
        //      If true, this method will leave the original Dom structure alone
        //      during tear-down. Note: this will not work with _Templated
        //      widgets yet.

        if(this.bgIframe){
            this.bgIframe.destroy(preserveDom);
            delete this.bgIframe;
        }

        if(this.domNode){
            if(!preserveDom){
                dojo.destroy(this.domNode);
            }
            delete this.domNode;
        }

        if(this.srcNodeRef){
            if(!preserveDom){
                dojo.destroy(this.srcNodeRef);
            }
            delete this.srcNodeRef;
        }
    },

    destroyDescendants: function(/*Boolean?*/ preserveDom){
        // summary:
        //      Recursively destroy the children of this widget and their
        //      descendants.
        // preserveDom:
        //      If true, the preserveDom attribute is passed to all descendant
        //      widget's .destroy() method. Not for use with _Templated
        //      widgets.

        // get all direct descendants and destroy them recursively
        dojo.forEach(this.getDescendants(true), function(widget){
            if(widget.destroyRecursive){
                widget.destroyRecursive(preserveDom);
            }
        });
    },


    uninitialize: function(){
        // summary:
        //      stub function. Override to implement custom widget tear-down
        //      behavior.
        return false;
    },

    ////////////////// MISCELLANEOUS METHODS ///////////////////

    onFocus: function(){
        // summary:
        //      stub function. Override or connect to this method to receive
        //      notifications for when the widget moves into focus.
    },

    onBlur: function(){
        // summary:
        //      stub function. Override or connect to this method to receive
        //      notifications for when the widget moves out of focus.
    },

    _onFocus: function(e){
        this.onFocus();
    },

    _onBlur: function(){
        this.onBlur();
    },

    _onConnect: function(/*String*/ event){
        // summary:
        //      Called when someone connects to one of my handlers.
        //      "Turn on" that handler if it isn't active yet.
        if(event in this._deferredConnects){
            var mapNode = this[this._deferredConnects[event]||'domNode'];
            this.connect(mapNode, event.toLowerCase(), this[event]);
            delete this._deferredConnects[event];
        }
    },

    _setClassAttr: function(/*String*/ value){
        var mapNode = this[this.attributeMap["class"]||'domNode'];
        dojo.removeClass(mapNode, this["class"])
        this["class"] = value;
        dojo.addClass(mapNode, value);
    },

    _setStyleAttr: function(/*String*/ value){
        var mapNode = this[this.attributeMap["style"]||'domNode'];
        if(mapNode.style.cssText){
            // TODO: remove old value
            mapNode.style.cssText += "; " + value; // FIXME: Opera
        }else{
            mapNode.style.cssText = value;
        }
        this["style"] = value;
    },

    setAttribute: function(/*String*/ attr, /*anything*/ value){
        dojo.deprecated(this.declaredClass+"::setAttribute() is deprecated. Use attr() instead.", "", "2.0");
        this.attr(attr, value);
    },

    _attrToDom: function(/*String*/ attr, /*String*/ value){
        //  summary:
        //      Reflect a widget attribute (title, tabIndex, duration etc.) to
        //      the widget DOM, as specified in attributeMap.
        //
        //  description:
        //      Also sets this["attr"] to the new value.
        //      Note some attributes like "type"
        //      cannot be processed this way as they are not mutable.

        var commands = this.attributeMap[attr];
        dojo.forEach( dojo.isArray(commands) ? commands : [commands], function(command){

            // Get target node and what we are doing to that node
            var mapNode = this[command.node || command || "domNode"];   // DOM node
            var type = command.type || "attribute"; // class, innerHTML, or attribute

            switch(type){
                case "attribute":
                    if(dojo.isFunction(value)){ // functions execute in the context of the widget
                        value = dojo.hitch(this, value);
                    }
                    if(/^on[A-Z][a-zA-Z]*$/.test(attr)){ // eg. onSubmit needs to be onsubmit
                        attr = attr.toLowerCase();
                    }
                    dojo.attr(mapNode, attr, value);
                    break;
                case "innerHTML":
                    mapNode.innerHTML = value;
                    break;
                case "class":
                    dojo.removeClass(mapNode, this[attr]);
                    dojo.addClass(mapNode, value);
                    break;
            }
        }, this);
        this[attr] = value;
    },

    attr: function(/*String|Object*/name, /*Object?*/value){
        //  summary:
        //      Set or get properties on a widget instance.
        //  name:
        //      The property to get or set. If an object is passed here and not
        //      a string, its keys are used as names of attributes to be set
        //      and the value of the object as values to set in the widget.
        //  value:
        //      Optional. If provided, attr() operates as a setter. If omitted,
        //      the current value of the named property is returned.
        //  description:
        //      Get or set named properties on a widget. If no value is
        //      provided, the current value of the attribute is returned,
        //      potentially via a getter method. If a value is provided, then
        //      the method acts as a setter, assigning the value to the name,
        //      potentially calling any explicitly provided setters to handle
        //      the operation. For instance, if the widget has properties "foo"
        //      and "bar" and a method named "_setFooAttr", calling:
        //  |   myWidget.attr("foo", "Howdy!");
        //      would be equivalent to calling:
        //  |   widget._setFooAttr("Howdy!");
        //      while calling:
        //  |   myWidget.attr("bar", "Howdy!");
        //      would be the same as writing:
        //  |   widget.bar = "Howdy!";
        //      It also tries to copy the changes to the widget's DOM according
        //      to settings in attributeMap (see description of attributeMap
        //      for details)
        //      For example, calling:
        //  |   myTitlePane.attr("title", "Howdy!");
        //      will do
        //  |   myTitlePane.title = "Howdy!";
        //  |   myTitlePane.title.innerHTML = "Howdy!";
        //      It works for dom node attributes too.  Calling
        //  |   widget.attr("disabled", true)
        //      will set the disabled attribute on the widget's focusNode,
        //      among other housekeeping for a change in disabled state.

        //  open questions:
        //      - how to handle build shortcut for attributes which want to map
        //      into DOM attributes?
        //      - what relationship should setAttribute()/attr() have to
        //      layout() calls?
        var args = arguments.length;
        if(args == 1 && !dojo.isString(name)){
            for(var x in name){ this.attr(x, name[x]); }
            return this;
        }
        var names = this._getAttrNames(name);
        if(args == 2){ // setter
            if(this[names.s]){
                // use the explicit setter
                return this[names.s](value) || this;
            }else{
                // if param is specified as DOM node attribute, copy it
                if(name in this.attributeMap){
                    this._attrToDom(name, value);
                }

                // FIXME: what about function assignments? Any way to connect() here?
                this[name] = value;
            }
            return this;
        }else{ // getter
            if(this[names.g]){
                return this[names.g]();
            }else{
                return this[name];
            }
        }
    },

    _attrPairNames: {},     // shared between all widgets
    _getAttrNames: function(name){
        // summary: helper function for Widget.attr()
        // cache attribute name values so we don't do the string ops every time
        var apn = this._attrPairNames;
        if(apn[name]){ return apn[name]; }
        var uc = name.charAt(0).toUpperCase() + name.substr(1);
        return apn[name] = {
            n: name+"Node",
            s: "_set"+uc+"Attr",
            g: "_get"+uc+"Attr"
        };
    },

    toString: function(){
        // summary:
        //      Returns a string that represents the widget. When a widget is
        //      cast to a string, this method will be used to generate the
        //      output. Currently, it does not implement any sort of reversable
        //      serialization.
        return '[Widget ' + this.declaredClass + ', ' + (this.id || 'NO ID') + ']'; // String
    },

    getDescendants: function(/*Boolean*/ directOnly, /*DomNode[]?*/ outAry){
        // summary:
        //      Returns all the widgets contained by this, i.e., all widgets underneath this.containerNode.
        // description:
        //      For example w/this markup:
        //
        //      |   <div dojoType=myWidget>
        //      |       <b> hello world </b>
        //      |       <div>
        //      |           <span dojoType=subwidget>
        //      |               <span dojoType=subwidget2>how's it going?</span>
        //      |           </span>
        //      |       </div>
        //      |   </div>
        //
        //      myWidget.getDescendants() will return subwidget and subwidget2.
        //
        //      This method is designed to *not* return widgets that are
        //      part of a widget's template, but rather to just return widgets that are defined in the
        //      original markup as descendants of this widget.
        // directOnly:
        //      If directOnly is true then won't find nested widgets (subwidget2 in above example)
        // outAry:
        //      If specified, put results in here
        outAry = outAry || [];
        if(this.containerNode){
            this._getDescendantsHelper(directOnly, outAry, this.containerNode);
        }
        return outAry;
    },
    _getDescendantsHelper: function(/*Boolean*/ directOnly, /* DomNode[] */ outAry, /*DomNode*/ root){
        // summary:
        //      Search subtree under root, putting found widgets in outAry
        // directOnly:
        //      If false, return widgets nested inside other widgets
        var list = dojo.isIE ? root.children : root.childNodes, i = 0, node;
        while(node = list[i++]){
            if(node.nodeType != 1){ continue; }
            var widgetId = node.getAttribute("widgetId");
            if(widgetId){
                var widget = dijit.byId(widgetId);
                outAry.push(widget);
                if(!directOnly){
                    widget.getDescendants(directOnly, outAry);
                }
            }else{
                this._getDescendantsHelper(directOnly, outAry, node);
            }
        }
    },

    // TODOC
    nodesWithKeyClick: ["input", "button"],

    connect: function(
            /*Object|null*/ obj,
            /*String|Function*/ event,
            /*String|Function*/ method){
        //  summary:
        //      Connects specified obj/event to specified method of this object
        //      and registers for disconnect() on widget destroy.
        //  description:
        //      Provide widget-specific analog to dojo.connect, except with the
        //      implicit use of this widget as the target object.
        //      This version of connect also provides a special "ondijitclick"
        //      event which triggers on a click or space-up, enter-down in IE
        //      or enter press in FF (since often can't cancel enter onkeydown
        //      in FF)
        //  example:
        //  |   var btn = new dijit.form.Button();
        //  |   // when foo.bar() is called, call the listener we're going to
        //  |   // provide in the scope of btn
        //  |   btn.connect(foo, "bar", function(){
        //  |       console.debug(this.toString());
        //  |   });

        var d = dojo;
        var dco = d.hitch(d, "connect", obj);
        var handles =[];
        if(event == "ondijitclick"){
            // add key based click activation for unsupported nodes.
            if(!this.nodesWithKeyClick[obj.nodeName]){
                var m = d.hitch(this, method);
                handles.push(
                    dco("onkeydown", this, function(e){
                        if(!d.isFF && e.keyCode == d.keys.ENTER &&
                            !e.ctrlKey && !e.shiftKey && !e.altKey && !e.metaKey){
                            return m(e);
                        }else if(e.keyCode == d.keys.SPACE){
                            // stop space down as it causes IE to scroll
                            // the browser window
                            d.stopEvent(e);
                        }
                    }),
                    dco("onkeyup", this, function(e){
                        if(e.keyCode == d.keys.SPACE &&
                            !e.ctrlKey && !e.shiftKey && !e.altKey && !e.metaKey){ return m(e); }
                    })
                );
                if(d.isFF){
                    handles.push(
                        dco("onkeypress", this, function(e){
                            if(e.keyCode == d.keys.ENTER &&
                                !e.ctrlKey && !e.shiftKey && !e.altKey && !e.metaKey){ return m(e); }
                        })
                    );
                }
            }
            event = "onclick";
        }
        handles.push(dco(event, this, method));

        // return handles for FormElement and ComboBox
        this._connects.push(handles);
        return handles;
    },

    disconnect: function(/*Object*/ handles){
        // summary:
        //      Disconnects handle created by this.connect.
        //      Also removes handle from this widget's list of connects
        for(var i=0; i<this._connects.length; i++){
            if(this._connects[i]==handles){
                dojo.forEach(handles, dojo.disconnect);
                this._connects.splice(i, 1);
                return;
            }
        }
    },

    isLeftToRight: function(){
        // summary:
        //      Checks the page for text direction
        return dojo._isBodyLtr(); //Boolean
    },

    isFocusable: function(){
        // summary:
        //      Return true if this widget can currently be focused
        //      and false if not
        return this.focus && (dojo.style(this.domNode, "display") != "none");
    },

    placeAt: function(/* String|DomNode|_Widget */reference, /* String?|Int? */position){
        // summary: Place this widget's domNode reference somewhere in the DOM based
        //      on standard dojo.place conventions, or passing a Widget reference that
        //      contains and addChild member.
        //
        // description:
        //      A convenience function provided in all _Widgets, providing a simple
        //      shorthand mechanism to put an existing (or newly created) Widget
        //      somewhere in the dom, and allow chaining.
        //
        //  reference:
        //      The String id of a domNode, a domNode reference, or a reference to a Widget posessing
        //      an addChild method.
        //
        //  position:
        //      If passed a string or domNode reference, the position argument
        //      accepts a string just as dojo.place does, one of: "first", "last",
        //      "before", or "after".
        //
        //      If passed a _Widget reference, and that widget reference has an ".addChild" method,
        //      it will be called passing this widget instance into that method, supplying the optional
        //      position index passed.
        //
        // example:
        // |    // create a Button with no srcNodeRef, and place it in the body:
        // |    var button = new dijit.form.Button({ label:"click" }).placeAt(dojo.body());
        // |    // now, 'button' is still the widget reference to the newly created button
        // |    dojo.connect(button, "onClick", function(e){ console.log('click'); });
        //
        // example:
        // |    // create a button out of a node with id="src" and append it to id="wrapper":
        // |    var button = new dijit.form.Button({},"src").placeAt("wrapper");
        //
        // example:
        // |    // place a new button as the first element of some div
        // |    var button = new dijit.form.Button({ label:"click" }).placeAt("wrapper","first");
        //
        // example:
        // |    // create a contentpane and add it to a TabContainer
        // |    var tc = dijit.byId("myTabs");
        // |    new dijit.layout.ContentPane({ href:"foo.html", title:"Wow!" }).placeAt(tc)

        if(reference["declaredClass"] && reference["addChild"]){
            reference.addChild(this, position);
        }else{
            dojo.place(this.domNode, reference, position);
        }
        return this;
    }

});

})();

}

if(!dojo._hasResource["dojo.string"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.string"] = true;
dojo.provide("dojo.string");

/*=====
dojo.string = {
    // summary: String utilities for Dojo
};
=====*/

dojo.string.rep = function(/*String*/str, /*Integer*/num){
    //  summary:
    //      Efficiently replicate a string `n` times.
    //  str:
    //      the string to replicate
    //  num:
    //      number of times to replicate the string

    if(num <= 0 || !str){ return ""; }

    var buf = [];
    for(;;){
        if(num & 1){
            buf.push(str);
        }
        if(!(num >>= 1)){ break; }
        str += str;
    }
    return buf.join("");    // String
};

dojo.string.pad = function(/*String*/text, /*Integer*/size, /*String?*/ch, /*Boolean?*/end){
    //  summary:
    //      Pad a string to guarantee that it is at least `size` length by
    //      filling with the character `ch` at either the start or end of the
    //      string. Pads at the start, by default.
    //  text:
    //      the string to pad
    //  size:
    //      length to provide padding
    //  ch:
    //      character to pad, defaults to '0'
    //  end:
    //      adds padding at the end if true, otherwise pads at start
    //  example:
    //  |   // Fill the string to length 10 with "+" characters on the right.  Yields "Dojo++++++".
    //  |   dojo.string.pad("Dojo", 10, "+", true);

    if(!ch){
        ch = '0';
    }
    var out = String(text),
        pad = dojo.string.rep(ch, Math.ceil((size - out.length) / ch.length));
    return end ? out + pad : pad + out; // String
};

dojo.string.substitute = function(  /*String*/      template,
                                    /*Object|Array*/map,
                                    /*Function?*/   transform,
                                    /*Object?*/     thisObject){
    //  summary:
    //      Performs parameterized substitutions on a string. Throws an
    //      exception if any parameter is unmatched.
    //  template:
    //      a string with expressions in the form `${key}` to be replaced or
    //      `${key:format}` which specifies a format function. keys are case-sensitive.
    //  map:
    //      hash to search for substitutions
    //  transform:
    //      a function to process all parameters before substitution takes
    //      place, e.g. dojo.string.encodeXML
    //  thisObject:
    //      where to look for optional format function; default to the global
    //      namespace
    //  example:
    //  |   // returns "File 'foo.html' is not found in directory '/temp'."
    //  |   dojo.string.substitute(
    //  |       "File '${0}' is not found in directory '${1}'.",
    //  |       ["foo.html","/temp"]
    //  |   );
    //  |
    //  |   // also returns "File 'foo.html' is not found in directory '/temp'."
    //  |   dojo.string.substitute(
    //  |       "File '${name}' is not found in directory '${info.dir}'.",
    //  |       { name: "foo.html", info: { dir: "/temp" } }
    //  |   );
    //  example:
    //      use a transform function to modify the values:
    //  |   // returns "file 'foo.html' is not found in directory '/temp'."
    //  |   dojo.string.substitute(
    //  |       "${0} is not found in ${1}.",
    //  |       ["foo.html","/temp"],
    //  |       function(str){
    //  |           // try to figure out the type
    //  |           var prefix = (str.charAt(0) == "/") ? "directory": "file";
    //  |           return prefix + " '" + str + "'";
    //  |       }
    //  |   );
    //  example:
    //      use a formatter
    //  |   // returns "thinger -- howdy"
    //  |   dojo.string.substitute(
    //  |       "${0:postfix}", ["thinger"], null, {
    //  |           postfix: function(value, key){
    //  |               return value + " -- howdy";
    //  |           }
    //  |       }
    //  |   );

    thisObject = thisObject||dojo.global;
    transform = (!transform) ?
                    function(v){ return v; } :
                    dojo.hitch(thisObject, transform);

    return template.replace(/\$\{([^\s\:\}]+)(?:\:([^\s\:\}]+))?\}/g, function(match, key, format){
        var value = dojo.getObject(key, false, map);
        if(format){
            value = dojo.getObject(format, false, thisObject).call(thisObject, value, key);
        }
        return transform(value, key).toString();
    }); // string
};

/*=====
dojo.string.trim = function(str){
    //  summary:
    //      Trims whitespace from both sides of the string
    //  str: String
    //      String to be trimmed
    //  returns: String
    //      Returns the trimmed string
    //  description:
    //      This version of trim() was taken from [Steven Levithan's blog](http://blog.stevenlevithan.com/archives/faster-trim-javascript).
    //      The short yet performant version of this function is dojo.trim(),
    //      which is part of Dojo base.  Uses String.prototype.trim instead, if available.
    return "";  // String
}
=====*/

dojo.string.trim = String.prototype.trim ?
    dojo.trim : // aliasing to the native function
    function(str){
        str = str.replace(/^\s+/, '');
        for(var i = str.length - 1; i >= 0; i--){
            if(/\S/.test(str.charAt(i))){
                str = str.substring(0, i + 1);
                break;
            }
        }
        return str;
    };

}

if(!dojo._hasResource["dojo.date.stamp"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.date.stamp"] = true;
dojo.provide("dojo.date.stamp");

// Methods to convert dates to or from a wire (string) format using well-known conventions

dojo.date.stamp.fromISOString = function(/*String*/formattedString, /*Number?*/defaultTime){
    //  summary:
    //      Returns a Date object given a string formatted according to a subset of the ISO-8601 standard.
    //
    //  description:
    //      Accepts a string formatted according to a profile of ISO8601 as defined by
    //      [RFC3339](http://www.ietf.org/rfc/rfc3339.txt), except that partial input is allowed.
    //      Can also process dates as specified [by the W3C](http://www.w3.org/TR/NOTE-datetime)
    //      The following combinations are valid:
    //
    //          * dates only
    //          |   * yyyy
    //          |   * yyyy-MM
    //          |   * yyyy-MM-dd
    //          * times only, with an optional time zone appended
    //          |   * THH:mm
    //          |   * THH:mm:ss
    //          |   * THH:mm:ss.SSS
    //          * and "datetimes" which could be any combination of the above
    //
    //      timezones may be specified as Z (for UTC) or +/- followed by a time expression HH:mm
    //      Assumes the local time zone if not specified.  Does not validate.  Improperly formatted
    //      input may return null.  Arguments which are out of bounds will be handled
    //      by the Date constructor (e.g. January 32nd typically gets resolved to February 1st)
    //      Only years between 100 and 9999 are supported.
    //
    //  formattedString:
    //      A string such as 2005-06-30T08:05:00-07:00 or 2005-06-30 or T08:05:00
    //
    //  defaultTime:
    //      Used for defaults for fields omitted in the formattedString.
    //      Uses 1970-01-01T00:00:00.0Z by default.

    if(!dojo.date.stamp._isoRegExp){
        dojo.date.stamp._isoRegExp =
//TODO: could be more restrictive and check for 00-59, etc.
            /^(?:(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(.\d+)?)?((?:[+-](\d{2}):(\d{2}))|Z)?)?$/;
    }

    var match = dojo.date.stamp._isoRegExp.exec(formattedString);
    var result = null;

    if(match){
        match.shift();
        if(match[1]){match[1]--;} // Javascript Date months are 0-based
        if(match[6]){match[6] *= 1000;} // Javascript Date expects fractional seconds as milliseconds

        if(defaultTime){
            // mix in defaultTime.  Relatively expensive, so use || operators for the fast path of defaultTime === 0
            defaultTime = new Date(defaultTime);
            dojo.map(["FullYear", "Month", "Date", "Hours", "Minutes", "Seconds", "Milliseconds"], function(prop){
                return defaultTime["get" + prop]();
            }).forEach(function(value, index){
                if(match[index] === undefined){
                    match[index] = value;
                }
            });
        }
        result = new Date(match[0]||1970, match[1]||0, match[2]||1, match[3]||0, match[4]||0, match[5]||0, match[6]||0);
//      result.setFullYear(match[0]||1970); // for year < 100

        var offset = 0;
        var zoneSign = match[7] && match[7].charAt(0);
        if(zoneSign != 'Z'){
            offset = ((match[8] || 0) * 60) + (Number(match[9]) || 0);
            if(zoneSign != '-'){ offset *= -1; }
        }
        if(zoneSign){
            offset -= result.getTimezoneOffset();
        }
        if(offset){
            result.setTime(result.getTime() + offset * 60000);
        }
    }

    return result; // Date or null
}

/*=====
    dojo.date.stamp.__Options = function(){
        //  selector: String
        //      "date" or "time" for partial formatting of the Date object.
        //      Both date and time will be formatted by default.
        //  zulu: Boolean
        //      if true, UTC/GMT is used for a timezone
        //  milliseconds: Boolean
        //      if true, output milliseconds
        this.selector = selector;
        this.zulu = zulu;
        this.milliseconds = milliseconds;
    }
=====*/

dojo.date.stamp.toISOString = function(/*Date*/dateObject, /*dojo.date.stamp.__Options?*/options){
    //  summary:
    //      Format a Date object as a string according a subset of the ISO-8601 standard
    //
    //  description:
    //      When options.selector is omitted, output follows [RFC3339](http://www.ietf.org/rfc/rfc3339.txt)
    //      The local time zone is included as an offset from GMT, except when selector=='time' (time without a date)
    //      Does not check bounds.  Only years between 100 and 9999 are supported.
    //
    //  dateObject:
    //      A Date object

    var _ = function(n){ return (n < 10) ? "0" + n : n; };
    options = options || {};
    var formattedDate = [];
    var getter = options.zulu ? "getUTC" : "get";
    var date = "";
    if(options.selector != "time"){
        var year = dateObject[getter+"FullYear"]();
        date = ["0000".substr((year+"").length)+year, _(dateObject[getter+"Month"]()+1), _(dateObject[getter+"Date"]())].join('-');
    }
    formattedDate.push(date);
    if(options.selector != "date"){
        var time = [_(dateObject[getter+"Hours"]()), _(dateObject[getter+"Minutes"]()), _(dateObject[getter+"Seconds"]())].join(':');
        var millis = dateObject[getter+"Milliseconds"]();
        if(options.milliseconds){
            time += "."+ (millis < 100 ? "0" : "") + _(millis);
        }
        if(options.zulu){
            time += "Z";
        }else if(options.selector != "time"){
            var timezoneOffset = dateObject.getTimezoneOffset();
            var absOffset = Math.abs(timezoneOffset);
            time += (timezoneOffset > 0 ? "-" : "+") +
                _(Math.floor(absOffset/60)) + ":" + _(absOffset%60);
        }
        formattedDate.push(time);
    }
    return formattedDate.join('T'); // String
}

}

if(!dojo._hasResource["dojo.parser"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.parser"] = true;
dojo.provide("dojo.parser");


dojo.parser = new function(){
    // summary: The Dom/Widget parsing package

    var d = dojo;
    var dtName = d._scopeName + "Type";
    var qry = "[" + dtName + "]";

    var _anonCtr = 0, _anon = {};
    var nameAnonFunc = function(/*Function*/anonFuncPtr, /*Object*/thisObj){
        // summary:
        //      Creates a reference to anonFuncPtr in thisObj with a completely
        //      unique name. The new name is returned as a String.
        var nso = thisObj || _anon;
        if(dojo.isIE){
            var cn = anonFuncPtr["__dojoNameCache"];
            if(cn && nso[cn] === anonFuncPtr){
                return cn;
            }
        }
        var name;
        do{
            name = "__" + _anonCtr++;
        }while(name in nso)
        nso[name] = anonFuncPtr;
        return name; // String
    }

    function val2type(/*Object*/ value){
        // summary:
        //      Returns name of type of given value.

        if(d.isString(value)){ return "string"; }
        if(typeof value == "number"){ return "number"; }
        if(typeof value == "boolean"){ return "boolean"; }
        if(d.isFunction(value)){ return "function"; }
        if(d.isArray(value)){ return "array"; } // typeof [] == "object"
        if(value instanceof Date) { return "date"; } // assume timestamp
        if(value instanceof d._Url){ return "url"; }
        return "object";
    }

    function str2obj(/*String*/ value, /*String*/ type){
        // summary:
        //      Convert given string value to given type
        switch(type){
            case "string":
                return value;
            case "number":
                return value.length ? Number(value) : NaN;
            case "boolean":
                // for checked/disabled value might be "" or "checked".  interpret as true.
                return typeof value == "boolean" ? value : !(value.toLowerCase()=="false");
            case "function":
                if(d.isFunction(value)){
                    // IE gives us a function, even when we say something like onClick="foo"
                    // (in which case it gives us an invalid function "function(){ foo }").
                    //  Therefore, convert to string
                    value=value.toString();
                    value=d.trim(value.substring(value.indexOf('{')+1, value.length-1));
                }
                try{
                    if(value.search(/[^\w\.]+/i) != -1){
                        // TODO: "this" here won't work
                        value = nameAnonFunc(new Function(value), this);
                    }
                    return d.getObject(value, false);
                }catch(e){ return new Function(); }
            case "array":
                return value ? value.split(/\s*,\s*/) : [];
            case "date":
                switch(value){
                    case "": return new Date("");   // the NaN of dates
                    case "now": return new Date();  // current date
                    default: return d.date.stamp.fromISOString(value);
                }
            case "url":
                return d.baseUrl + value;
            default:
                return d.fromJson(value);
        }
    }

    var instanceClasses = {
        // map from fully qualified name (like "dijit.Button") to structure like
        // { cls: dijit.Button, params: {label: "string", disabled: "boolean"} }
    };

    function getClassInfo(/*String*/ className){
        // className:
        //      fully qualified name (like "dijit.form.Button")
        // returns:
        //      structure like
        //          {
        //              cls: dijit.Button,
        //              params: { label: "string", disabled: "boolean"}
        //          }

        if(!instanceClasses[className]){
            // get pointer to widget class
            var cls = d.getObject(className);
            if(!d.isFunction(cls)){
                throw new Error("Could not load class '" + className +
                    "'. Did you spell the name correctly and use a full path, like 'dijit.form.Button'?");
            }
            var proto = cls.prototype;

            // get table of parameter names & types
            var params={};
            for(var name in proto){
                if(name.charAt(0)=="_"){ continue; }    // skip internal properties
                var defVal = proto[name];
                params[name]=val2type(defVal);
            }

            instanceClasses[className] = { cls: cls, params: params };
        }
        return instanceClasses[className];
    }

    this._functionFromScript = function(script){
        var preamble = "";
        var suffix = "";
        var argsStr = script.getAttribute("args");
        if(argsStr){
            d.forEach(argsStr.split(/\s*,\s*/), function(part, idx){
                preamble += "var "+part+" = arguments["+idx+"]; ";
            });
        }
        var withStr = script.getAttribute("with");
        if(withStr && withStr.length){
            d.forEach(withStr.split(/\s*,\s*/), function(part){
                preamble += "with("+part+"){";
                suffix += "}";
            });
        }
        return new Function(preamble+script.innerHTML+suffix);
    }

    this.instantiate = function(/* Array */nodes, /* Object? */mixin){
        // summary:
        //      Takes array of nodes, and turns them into class instances and
        //      potentially calls a layout method to allow them to connect with
        //      any children
        // mixin: Object
        //      An object that will be mixed in with each node in the array.
        //      Values in the mixin will override values in the node, if they
        //      exist.
        var thelist = [];
        mixin = mixin||{};
        d.forEach(nodes, function(node){
            if(!node){ return; }
            var type = dtName in mixin?mixin[dtName]:node.getAttribute(dtName);
            if(!type || !type.length){ return; }
            var clsInfo = getClassInfo(type),
                clazz = clsInfo.cls,
                ps = clazz._noScript || clazz.prototype._noScript;

            // read parameters (ie, attributes).
            // clsInfo.params lists expected params like {"checked": "boolean", "n": "number"}
            var params = {},
                attributes = node.attributes;
            for(var name in clsInfo.params){
                var item = name in mixin?{value:mixin[name],specified:true}:attributes.getNamedItem(name);
                if(!item || (!item.specified && (!dojo.isIE || name.toLowerCase()!="value"))){ continue; }
                var value = item.value;
                // Deal with IE quirks for 'class' and 'style'
                switch(name){
                case "class":
                    value = "className" in mixin?mixin.className:node.className;
                    break;
                case "style":
                    value = "style" in mixin?mixin.style:(node.style && node.style.cssText); // FIXME: Opera?
                }
                var _type = clsInfo.params[name];
                if(typeof value == "string"){
                    params[name] = str2obj(value, _type);
                }else{
                    params[name] = value;
                }
            }

            // Process <script type="dojo/*"> script tags
            // <script type="dojo/method" event="foo"> tags are added to params, and passed to
            // the widget on instantiation.
            // <script type="dojo/method"> tags (with no event) are executed after instantiation
            // <script type="dojo/connect" event="foo"> tags are dojo.connected after instantiation
            // note: dojo/* script tags cannot exist in self closing widgets, like <input />
            if(!ps){
                var connects = [],  // functions to connect after instantiation
                    calls = [];     // functions to call after instantiation

                d.query("> script[type^='dojo/']", node).orphan().forEach(function(script){
                    var event = script.getAttribute("event"),
                        type = script.getAttribute("type"),
                        nf = d.parser._functionFromScript(script);
                    if(event){
                        if(type == "dojo/connect"){
                            connects.push({event: event, func: nf});
                        }else{
                            params[event] = nf;
                        }
                    }else{
                        calls.push(nf);
                    }
                });
            }

            var markupFactory = clazz["markupFactory"];
            if(!markupFactory && clazz["prototype"]){
                markupFactory = clazz.prototype["markupFactory"];
            }
            // create the instance
            var instance = markupFactory ? markupFactory(params, node, clazz) : new clazz(params, node);
            thelist.push(instance);

            // map it to the JS namespace if that makes sense
            var jsname = node.getAttribute("jsId");
            if(jsname){
                d.setObject(jsname, instance);
            }

            // process connections and startup functions
            if(!ps){
                d.forEach(connects, function(connect){
                    d.connect(instance, connect.event, null, connect.func);
                });
                d.forEach(calls, function(func){
                    func.call(instance);
                });
            }
        });

        // Call startup on each top level instance if it makes sense (as for
        // widgets).  Parent widgets will recursively call startup on their
        // (non-top level) children
        d.forEach(thelist, function(instance){
            if( instance  &&
                instance.startup &&
                !instance._started &&
                (!instance.getParent || !instance.getParent())
            ){
                instance.startup();
            }
        });
        return thelist;
    };

    this.parse = function(/*DomNode?*/ rootNode){
        // summary:
        //      Search specified node (or root node) recursively for class instances,
        //      and instantiate them Searches for
        //      dojoType="qualified.class.name"
        var list = d.query(qry, rootNode);
        // go build the object instances
        var instances = this.instantiate(list);
        return instances;
    };
}();

//Register the parser callback. It should be the first callback
//after the a11y test.

(function(){
    var parseRunner = function(){
        if(dojo.config["parseOnLoad"] == true){
            dojo.parser.parse();
        }
    };

    // FIXME: need to clobber cross-dependency!!
    if(dojo.exists("dijit.wai.onload") && (dijit.wai.onload === dojo._loaders[0])){
        dojo._loaders.splice(1, 0, parseRunner);
    }else{
        dojo._loaders.unshift(parseRunner);
    }
})();

}

if(!dojo._hasResource["dijit._Templated"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._Templated"] = true;
dojo.provide("dijit._Templated");





dojo.declare("dijit._Templated",
    null,
    {
        //  summary:
        //      Mixin for widgets that are instantiated from a template
        //

        // templateString: String
        //      a string that represents the widget template. Pre-empts the
        //      templatePath. In builds that have their strings "interned", the
        //      templatePath is converted to an inline templateString, thereby
        //      preventing a synchronous network call.
        templateString: null,

        // templatePath: String
        //  Path to template (HTML file) for this widget relative to dojo.baseUrl
        templatePath: null,

        // widgetsInTemplate: Boolean
        //      should we parse the template to find widgets that might be
        //      declared in markup inside it? false by default.
        widgetsInTemplate: false,

        // skipNodeCache: Boolean
        //      if using a cached widget template node poses issues for a
        //      particular widget class, it can set this property to ensure
        //      that its template is always re-built from a string
        _skipNodeCache: false,

        _stringRepl: function(tmpl){
            var className = this.declaredClass, _this = this;
            // Cache contains a string because we need to do property replacement
            // do the property replacement
            return dojo.string.substitute(tmpl, this, function(value, key){
                if(key.charAt(0) == '!'){ value = dojo.getObject(key.substr(1), _this); }
                if(typeof value == "undefined"){ throw new Error(className+" template:"+key); } // a debugging aide
                if(value == null){ return ""; }

                // Substitution keys beginning with ! will skip the transform step,
                // in case a user wishes to insert unescaped markup, e.g. ${!foo}
                return key.charAt(0) == "!" ? value :
                    // Safer substitution, see heading "Attribute values" in
                    // http://www.w3.org/TR/REC-html40/appendix/notes.html#h-B.3.2
                    value.toString().replace(/"/g,"&quot;"); //TODO: add &amp? use encodeXML method?
            }, this);
        },

        // method over-ride
        buildRendering: function(){
            // summary:
            //      Construct the UI for this widget from a template, setting this.domNode.

            // Lookup cached version of template, and download to cache if it
            // isn't there already.  Returns either a DomNode or a string, depending on
            // whether or not the template contains ${foo} replacement parameters.
            var cached = dijit._Templated.getCachedTemplate(this.templatePath, this.templateString, this._skipNodeCache);

            var node;
            if(dojo.isString(cached)){
                node = dojo._toDom(this._stringRepl(cached));
            }else{
                // if it's a node, all we have to do is clone it
                node = cached.cloneNode(true);
            }

            this.domNode = node;

            // recurse through the node, looking for, and attaching to, our
            // attachment points and events, which should be defined on the template node.
            this._attachTemplateNodes(node);

            if(this.widgetsInTemplate){
                var cw = (this._supportingWidgets = dojo.parser.parse(node));
                this._attachTemplateNodes(cw, function(n,p){
                    return n[p];
                });
            }

            this._fillContent(this.srcNodeRef);
        },

        _fillContent: function(/*DomNode*/ source){
            // summary:
            //      relocate source contents to templated container node
            //      this.containerNode must be able to receive children, or exceptions will be thrown
            var dest = this.containerNode;
            if(source && dest){
                while(source.hasChildNodes()){
                    dest.appendChild(source.firstChild);
                }
            }
        },

        _attachTemplateNodes: function(rootNode, getAttrFunc){
            // summary: Iterate through the template and attach functions and nodes accordingly.
            // description:
            //      Map widget properties and functions to the handlers specified in
            //      the dom node and it's descendants. This function iterates over all
            //      nodes and looks for these properties:
            //          * dojoAttachPoint
            //          * dojoAttachEvent
            //          * waiRole
            //          * waiState
            // rootNode: DomNode|Array[Widgets]
            //      the node to search for properties. All children will be searched.
            // getAttrFunc: function?
            //      a function which will be used to obtain property for a given
            //      DomNode/Widget

            getAttrFunc = getAttrFunc || function(n,p){ return n.getAttribute(p); };

            var nodes = dojo.isArray(rootNode) ? rootNode : (rootNode.all || rootNode.getElementsByTagName("*"));
            var x = dojo.isArray(rootNode) ? 0 : -1;
            var attrs = {};
            for(; x<nodes.length; x++){
                var baseNode = (x == -1) ? rootNode : nodes[x];
                if(this.widgetsInTemplate && getAttrFunc(baseNode, "dojoType")){
                    continue;
                }
                // Process dojoAttachPoint
                var attachPoint = getAttrFunc(baseNode, "dojoAttachPoint");
                if(attachPoint){
                    var point, points = attachPoint.split(/\s*,\s*/);
                    while((point = points.shift())){
                        if(dojo.isArray(this[point])){
                            this[point].push(baseNode);
                        }else{
                            this[point]=baseNode;
                        }
                    }
                }

                // Process dojoAttachEvent
                var attachEvent = getAttrFunc(baseNode, "dojoAttachEvent");
                if(attachEvent){
                    // NOTE: we want to support attributes that have the form
                    // "domEvent: nativeEvent; ..."
                    var event, events = attachEvent.split(/\s*,\s*/);
                    var trim = dojo.trim;
                    while((event = events.shift())){
                        if(event){
                            var thisFunc = null;
                            if(event.indexOf(":") != -1){
                                // oh, if only JS had tuple assignment
                                var funcNameArr = event.split(":");
                                event = trim(funcNameArr[0]);
                                thisFunc = trim(funcNameArr[1]);
                            }else{
                                event = trim(event);
                            }
                            if(!thisFunc){
                                thisFunc = event;
                            }
                            this.connect(baseNode, event, thisFunc);
                        }
                    }
                }

                // waiRole, waiState
                var role = getAttrFunc(baseNode, "waiRole");
                if(role){
                    dijit.setWaiRole(baseNode, role);
                }
                var values = getAttrFunc(baseNode, "waiState");
                if(values){
                    dojo.forEach(values.split(/\s*,\s*/), function(stateValue){
                        if(stateValue.indexOf('-') != -1){
                            var pair = stateValue.split('-');
                            dijit.setWaiState(baseNode, pair[0], pair[1]);
                        }
                    });
                }
            }
        }
    }
);

// key is either templatePath or templateString; object is either string or DOM tree
dijit._Templated._templateCache = {};

dijit._Templated.getCachedTemplate = function(templatePath, templateString, alwaysUseString){
    // summary:
    //      Static method to get a template based on the templatePath or
    //      templateString key
    // templatePath: String
    //      The URL to get the template from. dojo.uri.Uri is often passed as well.
    // templateString: String?
    //      a string to use in lieu of fetching the template from a URL. Takes precedence
    //      over templatePath
    // Returns: Mixed
    //  Either string (if there are ${} variables that need to be replaced) or just
    //  a DOM tree (if the node can be cloned directly)

    // is it already cached?
    var tmplts = dijit._Templated._templateCache;
    var key = templateString || templatePath;
    var cached = tmplts[key];
    if(cached){
        if(!cached.ownerDocument || cached.ownerDocument == dojo.doc){
            // string or node of the same document
            return cached;
        }
        // destroy the old cached node of a different document
        dojo.destroy(cached);
    }

    // If necessary, load template string from template path
    if(!templateString){
        templateString = dijit._Templated._sanitizeTemplateString(dojo.trim(dojo._getText(templatePath)));
    }

    templateString = dojo.string.trim(templateString);

    if(alwaysUseString || templateString.match(/\$\{([^\}]+)\}/g)){
        // there are variables in the template so all we can do is cache the string
        return (tmplts[key] = templateString); //String
    }else{
        // there are no variables in the template so we can cache the DOM tree
        return (tmplts[key] = dojo._toDom(templateString)); //Node
    }
};

dijit._Templated._sanitizeTemplateString = function(/*String*/tString){
    // summary:
    //      Strips <?xml ...?> declarations so that external SVG and XML
    //      documents can be added to a document without worry. Also, if the string
    //      is an HTML document, only the part inside the body tag is returned.
    if(tString){
        tString = tString.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im, "");
        var matches = tString.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
        if(matches){
            tString = matches[1];
        }
    }else{
        tString = "";
    }
    return tString; //String
};


if(dojo.isIE){
    dojo.addOnWindowUnload(function(){
        var cache = dijit._Templated._templateCache;
        for(var key in cache){
            var value = cache[key];
            if(!isNaN(value.nodeType)){ // isNode equivalent
                dojo.destroy(value);
            }
            delete cache[key];
        }
    });
}

// These arguments can be specified for widgets which are used in templates.
// Since any widget can be specified as sub widgets in template, mix it
// into the base widget class.  (This is a hack, but it's effective.)
dojo.extend(dijit._Widget,{
    dojoAttachEvent: "",
    dojoAttachPoint: "",
    waiRole: "",
    waiState:""
})

}

if(!dojo._hasResource["dijit.form._FormWidget"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form._FormWidget"] = true;
dojo.provide("dijit.form._FormWidget");




dojo.declare("dijit.form._FormWidget", [dijit._Widget, dijit._Templated],
    {
    //
    // summary:
    //  _FormWidget's correspond to native HTML elements such as <checkbox> or <button>.
    //
    // description:
    //      Each _FormWidget represents a single HTML element.
    //      All these widgets should have these attributes just like native HTML input elements.
    //      You can set them during widget construction.
    //
    //  They also share some common methods.
    //
    // baseClass: String
    //      Root CSS class of the widget (ex: dijitTextBox), used to add CSS classes of widget
    //      (ex: "dijitTextBox dijitTextBoxInvalid dijitTextBoxFocused dijitTextBoxInvalidFocused")
    //      See _setStateClass().
    baseClass: "",

    // name: String
    //      Name used when submitting form; same as "name" attribute or plain HTML elements
    name: "",

    // alt: String
    //      Corresponds to the native HTML <input> element's attribute.
    alt: "",

    // value: String
    //      Corresponds to the native HTML <input> element's attribute.
    value: "",

    // type: String
    //      Corresponds to the native HTML <input> element's attribute.
    type: "text",

    // tabIndex: Integer
    //      Order fields are traversed when user hits the tab key
    tabIndex: "0",

    // disabled: Boolean
    //      Should this widget respond to user input?
    //      In markup, this is specified as "disabled='disabled'", or just "disabled".
    disabled: false,

    // readOnly: Boolean
    //      Should this widget respond to user input?
    //      In markup, this is specified as "readOnly".
    //      Similar to disabled except readOnly form values are submitted
    readOnly: false,

    // intermediateChanges: Boolean
    //      Fires onChange for each value change or only on demand
    intermediateChanges: false,

    // scrollOnFocus: Boolean
    //              On focus, should this widget scroll into view?
    scrollOnFocus: true,

    // These mixins assume that the focus node is an INPUT, as many but not all _FormWidgets are.
    attributeMap: dojo.delegate(dijit._Widget.prototype.attributeMap, {
        value: "focusNode",
        disabled: "focusNode",
        readOnly: "focusNode",
        id: "focusNode",
        tabIndex: "focusNode",
        alt: "focusNode"
    }),

    _setNameAttr: function(/*String*/ value){
        this.name = value;
        var node = this.valueNode || this.focusNode || this.domNode;
        dojo.attr(node, 'name', value);
    },

    _setDisabledAttr: function(/*Boolean*/ value){
        this.disabled = value;
        dojo.attr(this.focusNode, 'disabled', value);
        dijit.setWaiState(this.focusNode, "disabled", value);

                if(value){
                    //reset those, because after the domNode is disabled, we can no longer receive
                    //mouse related events, see #4200
                    this._hovering = false;
                    this._active = false;
                    // remove the tabIndex, especially for FF
                    this.focusNode.removeAttribute('tabIndex');
                }else{
                    this.focusNode.setAttribute('tabIndex', this.tabIndex);
                }
                this._setStateClass();
    },

    setDisabled: function(/*Boolean*/ disabled){
        // summary:
        //      Set disabled state of widget (Deprecated).
        dojo.deprecated("setDisabled("+disabled+") is deprecated. Use attr('disabled',"+disabled+") instead.", "", "2.0");
        this.attr('disabled', disabled);
    },

    _onFocus: function(e){
        if(this.scrollOnFocus){
            dijit.scrollIntoView(this.domNode);
        }
        this.inherited(arguments);
    },

    _onMouse : function(/*Event*/ event){
        // summary:
        //  Sets _hovering, _active, and stateModifier properties depending on mouse state,
        //  then calls setStateClass() to set appropriate CSS classes for this.domNode.
        //
        //  To get a different CSS class for hover, send onmouseover and onmouseout events to this method.
        //  To get a different CSS class while mouse button is depressed, send onmousedown to this method.

        var mouseNode = event.currentTarget;
        if(mouseNode && mouseNode.getAttribute){
            this.stateModifier = mouseNode.getAttribute("stateModifier") || "";
        }

        if(!this.disabled){
            switch(event.type){
                case "mouseenter":
                case "mouseover":
                    this._hovering = true;
                    this._active = this._mouseDown;
                    break;

                case "mouseout":
                case "mouseleave":
                    this._hovering = false;
                    this._active = false;
                    break;

                case "mousedown" :
                    this._active = true;
                    this._mouseDown = true;
                    // set a global event to handle mouseup, so it fires properly
                    //  even if the cursor leaves the button
                    var mouseUpConnector = this.connect(dojo.body(), "onmouseup", function(){
                        //if user clicks on the button, even if the mouse is released outside of it,
                        //this button should get focus (which mimics native browser buttons)
                        if(this._mouseDown && this.isFocusable()){
                            this.focus();
                        }
                        this._active = false;
                        this._mouseDown = false;
                        this._setStateClass();
                        this.disconnect(mouseUpConnector);
                    });
                    break;
            }
            this._setStateClass();
        }
    },

    isFocusable: function(){
        return !this.disabled && !this.readOnly && this.focusNode && (dojo.style(this.domNode, "display") != "none");
    },

    focus: function(){
        dijit.focus(this.focusNode);
    },

    _setStateClass: function(){
        // summary
        //  Update the visual state of the widget by setting the css classes on this.domNode
        //  (or this.stateNode if defined) by combining this.baseClass with
        //  various suffixes that represent the current widget state(s).
        //
        //  In the case where a widget has multiple
        //  states, it sets the class based on all possible
        //  combinations.  For example, an invalid form widget that is being hovered
        //  will be "dijitInput dijitInputInvalid dijitInputHover dijitInputInvalidHover".
        //
        //  For complex widgets with multiple regions, there can be various hover/active states,
        //  such as "Hover" or "CloseButtonHover" (for tab buttons).
        //  This is controlled by a stateModifier="CloseButton" attribute on the close button node.
        //
        //  The widget may have one or more of the following states, determined
        //  by this.state, this.checked, this.valid, and this.selected:
        //      Error - ValidationTextBox sets this.state to "Error" if the current input value is invalid
        //      Checked - ex: a checkmark or a ToggleButton in a checked state, will have this.checked==true
        //      Selected - ex: currently selected tab will have this.selected==true
        //
        //  In addition, it may have one or more of the following states,
        //  based on this.disabled and flags set in _onMouse (this._active, this._hovering, this._focused):
        //      Disabled    - if the widget is disabled
        //      Active      - if the mouse (or space/enter key?) is being pressed down
        //      Focused     - if the widget has focus
        //      Hover       - if the mouse is over the widget

        // Compute new set of classes
        var newStateClasses = this.baseClass.split(" ");

        function multiply(modifier){
            newStateClasses = newStateClasses.concat(dojo.map(newStateClasses, function(c){ return c+modifier; }), "dijit"+modifier);
        }

        if(this.checked){
            multiply("Checked");
        }
        if(this.state){
            multiply(this.state);
        }
        if(this.selected){
            multiply("Selected");
        }

        if(this.disabled){
            multiply("Disabled");
        }else if(this.readOnly){
            multiply("ReadOnly");
        }else if(this._active){
            multiply(this.stateModifier+"Active");
        }else{
            if(this._focused){
                multiply("Focused");
            }
            if(this._hovering){
                multiply(this.stateModifier+"Hover");
            }
        }

        // Remove old state classes and add new ones.
        // For performance concerns we only write into domNode.className once.
        var tn = this.stateNode || this.domNode,
            classHash = {}; // set of all classes (state and otherwise) for node

        dojo.forEach(tn.className.split(" "), function(c){ classHash[c] = true; });

        if("_stateClasses" in this){
            dojo.forEach(this._stateClasses, function(c){ delete classHash[c]; });
        }

        dojo.forEach(newStateClasses, function(c){ classHash[c] = true; });

        var newClasses = [];
        for(var c in classHash){
            newClasses.push(c);
        }
        tn.className = newClasses.join(" ");

        this._stateClasses = newStateClasses;
    },

    compare: function(/*anything*/val1, /*anything*/val2){
        // summary: compare 2 values
        if((typeof val1 == "number") && (typeof val2 == "number")){
            return (isNaN(val1) && isNaN(val2))? 0 : (val1-val2);
        }else if(val1 > val2){ return 1; }
        else if(val1 < val2){ return -1; }
        else { return 0; }
    },

    onChange: function(newValue){
        // summary: callback when value is changed
    },

    _onChangeActive: false,

    _handleOnChange: function(/*anything*/ newValue, /*Boolean, optional*/ priorityChange){
        // summary: set the value of the widget.
        this._lastValue = newValue;
        if(this._lastValueReported == undefined && (priorityChange === null || !this._onChangeActive)){
            // this block executes not for a change, but during initialization,
            // and is used to store away the original value (or for ToggleButton, the original checked state)
            this._resetValue = this._lastValueReported = newValue;
        }
        if((this.intermediateChanges || priorityChange || priorityChange === undefined) &&
            ((typeof newValue != typeof this._lastValueReported) ||
                this.compare(newValue, this._lastValueReported) != 0)){
            this._lastValueReported = newValue;
            if(this._onChangeActive){ this.onChange(newValue); }
        }
    },

    create: function(){
        this.inherited(arguments);
        this._onChangeActive = true;
        this._setStateClass();
    },

    destroy: function(){
        if(this._layoutHackHandle){
            clearTimeout(this._layoutHackHandle);
        }
        this.inherited(arguments);
    },

    setValue: function(/*String*/ value){
        dojo.deprecated("dijit.form._FormWidget:setValue("+value+") is deprecated.  Use attr('value',"+value+") instead.", "", "2.0");
        this.attr('value', value);
    },

    getValue: function(){
        dojo.deprecated(this.declaredClass+"::getValue() is deprecated. Use attr('value') instead.", "", "2.0");
        return this.attr('value');
    },

    _layoutHack: function(){
        // summary: work around table sizing bugs on FF2 by forcing redraw
        if(dojo.isFF == 2 && !this._layoutHackHandle){
            var node=this.domNode;
            var old = node.style.opacity;
            node.style.opacity = "0.999";
            this._layoutHackHandle = setTimeout(dojo.hitch(this, function(){
                this._layoutHackHandle = null;
                node.style.opacity = old;
            }), 0);
        }
    }
});

dojo.declare("dijit.form._FormValueWidget", dijit.form._FormWidget,
{
    /*
    Summary:
        _FormValueWidget's correspond to native HTML elements such as <input> or <select> that have user changeable values.
        Each _ValueWidget represents a single input value, and has a (possibly hidden) <input> element,
        to which it serializes its input value, so that form submission (either normal submission or via FormBind?)
        works as expected.
    */

    // TODO: unclear what that {value: ""} is for; FormWidget.attributeMap copies value to focusNode,
    // so maybe {value: ""} is so the value *doesn't* get copied to focusNode?
    // Seems like we really want value removed from attributeMap altogether
    // (although there's no easy way to do that now)
    attributeMap: dojo.delegate(dijit.form._FormWidget.prototype.attributeMap, { value: "" }),

    postCreate: function(){
        if(dojo.isIE || dojo.isWebKit){ // IE won't stop the event with keypress and Safari won't send an ESCAPE to keypress at all
            this.connect(this.focusNode || this.domNode, "onkeydown", this._onKeyDown);
        }
        // Update our reset value if it hasn't yet been set (because this.attr
        // is only called when there *is* a value
        if(this._resetValue === undefined){
            this._resetValue = this.value;
        }
    },

    _setValueAttr: function(/*anything*/ newValue, /*Boolean, optional*/ priorityChange){
        // summary:
        //      Hook so attr('value', value) works.
        // description:
        //      Sets the value of the widget.
        //      If the value has changed, then fire onChange event, unless priorityChange
        //      is specified as null (or false?)
        this.value = newValue;
        this._handleOnChange(newValue, priorityChange);
    },

    _getValueAttr: function(/*String*/ value){
        // summary:
        //      Hook so attr('value') works.
        return this._lastValue;
    },

    undo: function(){
        // summary: restore the value to the last value passed to onChange
        this._setValueAttr(this._lastValueReported, false);
    },

    reset: function(){
        this._hasBeenBlurred = false;
        this._setValueAttr(this._resetValue, true);
    },

    _valueChanged: function(){
        var v = this.attr('value');
        var lv = this._lastValueReported;
        // Equality comparison of objects such as dates are done by reference so
        // two distinct objects are != even if they have the same data. So use
        // toStrings in case the values are objects.
        return ((v !== null && (v !== undefined) && v.toString)?v.toString():'') !== ((lv !== null && (lv !== undefined) && lv.toString)?lv.toString():'');
    },

    _onKeyDown: function(e){
        if(e.keyCode == dojo.keys.ESCAPE && !e.ctrlKey && !e.altKey){
            var te;
            if(dojo.isIE){
                e.preventDefault(); // default behavior needs to be stopped here since keypress is too late
                te = document.createEventObject();
                te.keyCode = dojo.keys.ESCAPE;
                te.shiftKey = e.shiftKey;
                e.srcElement.fireEvent('onkeypress', te);
            }else if(dojo.isWebKit){ // ESCAPE needs help making it into keypress
                te = document.createEvent('Events');
                te.initEvent('keypress', true, true);
                te.keyCode = dojo.keys.ESCAPE;
                te.shiftKey = e.shiftKey;
                e.target.dispatchEvent(te);
            }
        }
    },

    _onKeyPress: function(e){
        if(e.charOrCode == dojo.keys.ESCAPE && !e.ctrlKey && !e.altKey && this._valueChanged()){
            this.undo();
            dojo.stopEvent(e);
            return false;
        }
        return true;
    }
});

}

if(!dojo._hasResource["dijit.form.TextBox"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.TextBox"] = true;
dojo.provide("dijit.form.TextBox");



dojo.declare(
    "dijit.form.TextBox",
    dijit.form._FormValueWidget,
    {
        //  summary:
        //      A base class for textbox form inputs

        //  trim: Boolean
        //      Removes leading and trailing whitespace if true.  Default is false.
        trim: false,

        //  uppercase: Boolean
        //      Converts all characters to uppercase if true.  Default is false.
        uppercase: false,

        //  lowercase: Boolean
        //      Converts all characters to lowercase if true.  Default is false.
        lowercase: false,

        //  propercase: Boolean
        //      Converts the first character of each word to uppercase if true.
        propercase: false,

        //  maxLength: String
        //      HTML INPUT tag maxLength declaration.
        maxLength: "",

        templateString:"<input class=\"dijit dijitReset dijitLeft\" dojoAttachPoint='textbox,focusNode'\n\tdojoAttachEvent='onmouseenter:_onMouse,onmouseleave:_onMouse,onkeypress:_onKeyPress'\n\tautocomplete=\"off\" type=\"${type}\"\n\t/>\n",
        baseClass: "dijitTextBox",

        attributeMap: dojo.delegate(dijit.form._FormValueWidget.prototype.attributeMap, {
            maxLength: "focusNode"
        }),

        _getValueAttr: function(){
            // summary:
            //      Hook so attr('value') works as we like.
            // description:
            //      For TextBox this simply returns the value of the <input>,
            //      but the parse() call is so subclasses can change this
            //      behavior w/out overriding this method.
            return this.parse(this.attr('displayedValue'), this.constraints);
        },

        _setValueAttr: function(value, /*Boolean?*/ priorityChange, /*String?*/ formattedValue){
            //  summary:
            //      Hook so attr('value', ...) works.
            //
            //  description:
            //      Sets the value of the widget to "value" which can be of
            //      any type as determined by the widget.
            //
            //  value:
            //      The visual element value is also set to a corresponding,
            //      but not necessarily the same, value.
            //
            //  formattedValue:
            //      If specified, used to set the visual element value,
            //      otherwise a computed visual value is used.
            //
            //  priorityChange:
            //      If true, an onChange event is fired immediately instead of
            //      waiting for the next blur event.

            var filteredValue;
            if(value !== undefined){
                filteredValue = this.filter(value);
                if(filteredValue !== null && ((typeof filteredValue != "number") || !isNaN(filteredValue))){
                    if(typeof formattedValue != "string"){
                        formattedValue = this.filter(this.format(filteredValue, this.constraints));
                    }
                }else{ formattedValue = ''; }
            }
            if(formattedValue != null && formattedValue != undefined && this.textbox.value != formattedValue){
                this.textbox.value = formattedValue;
            }
            dijit.form.TextBox.superclass._setValueAttr.call(this, filteredValue, priorityChange);
        },

        // displayedValue: String
        //      For subclasses like ComboBox where the displayed value
        //      (ex: Kentucky) and the serialized value (ex: KY) are different,
        //      this represents the displayed value.
        //
        //      Setting 'displayedValue' through attr('displayedValue', ...)
        //      updates 'value', and vice-versa.  Othewise 'value' is updated
        //      from 'displayedValue' periodically, like onBlur etc.
        //
        //      TODO: move declaration to MappedTextBox?
        //      Problem is that ComboBox references displayedValue,
        //      for benefit of FilteringSelect.
        displayedValue: "",

        getDisplayedValue: function(){
            dojo.deprecated(this.declaredClass+"::getDisplayedValue() is deprecated. Use attr('displayedValue') instead.", "", "2.0");
            return this.attr('displayedValue');
        },

        _getDisplayedValueAttr: function(){
            //  summary:
            //      Hook so attr('displayedValue') works.
            //  description:
            //      Returns the displayed value (what the user sees on the screen),
            //      after filtering (ie, trimming spaces etc.).
            //
            //      For some subclasses of TextBox (like ComboBox), the displayed value
            //      is different from the serialized value that's actually
            //      sent to the server (see dijit.form.ValidationTextBox.serialize)

            return this.filter(this.textbox.value);
        },

        setDisplayedValue: function(/*String*/value){
            dojo.deprecated(this.declaredClass+"::setDisplayedValue() is deprecated. Use attr('displayedValue', ...) instead.", "", "2.0");
            this.attr('displayedValue', value);
        },

        _setDisplayedValueAttr: function(/*String*/value){
            // summary:
            //      Hook so attr('displayedValue', ...) works.
            //  description:
            //      Sets the value of the visual element to the string "value".
            //      The widget value is also set to a corresponding,
            //      but not necessarily the same, value.

            this.textbox.value = value;
            this._setValueAttr(this.attr('value'), undefined, value);
        },

        format: function(/* String */ value, /* Object */ constraints){
            //  summary:
            //      Replacable function to convert a value to a properly formatted string
            return ((value == null || value == undefined) ? "" : (value.toString ? value.toString() : value));
        },

        parse: function(/* String */ value, /* Object */ constraints){
            //  summary:
            //      Replacable function to convert a formatted string to a value
            return value;
        },

        _refreshState: function(){
        },

        _onInput: function(e){
            if(e && e.type && /key/i.test(e.type) && e.keyCode){
                switch(e.keyCode){
                    case dojo.keys.SHIFT:
                    case dojo.keys.ALT:
                    case dojo.keys.CTRL:
                    case dojo.keys.TAB:
                        return;
                }
            }
            if(this.intermediateChanges){
                var _this = this;
                // the setTimeout allows the key to post to the widget input box
                setTimeout(function(){ _this._handleOnChange(_this.attr('value'), false); }, 0);
            }
            this._refreshState();
        },

        postCreate: function(){
            // setting the value here is needed since value="" in the template causes "undefined"
            // and setting in the DOM (instead of the JS object) helps with form reset actions
            this.textbox.setAttribute("value", this.textbox.value); // DOM and JS values shuld be the same
            this.inherited(arguments);
            if(dojo.isMoz || dojo.isOpera){
                this.connect(this.textbox, "oninput", this._onInput);
            }else{
                this.connect(this.textbox, "onkeydown", this._onInput);
                this.connect(this.textbox, "onkeyup", this._onInput);
                this.connect(this.textbox, "onpaste", this._onInput);
                this.connect(this.textbox, "oncut", this._onInput);
            }

            /*#5297:if(this.srcNodeRef){
                dojo.style(this.textbox, "cssText", this.style);
                this.textbox.className += " " + this["class"];
            }*/
            this._layoutHack();
        },

        filter: function(val){
            //  summary:
            //      Auto-corrections (such as trimming) that are applied to textbox
            //      value on blur or form submit
            if(typeof val != "string"){ return val; }
            if(this.trim){
                val = dojo.trim(val);
            }
            if(this.uppercase){
                val = val.toUpperCase();
            }
            if(this.lowercase){
                val = val.toLowerCase();
            }
            if(this.propercase){
                val = val.replace(/[^\s]+/g, function(word){
                    return word.substring(0,1).toUpperCase() + word.substring(1);
                });
            }
            return val;
        },

        _setBlurValue: function(){
            this._setValueAttr(this.attr('value'), true);
        },

        _onBlur: function(e){
            if(this.disabled){ return; }
            this._setBlurValue();
            this.inherited(arguments);
        },

        _onFocus: function(e){
            if(this.disabled){ return; }
            this._refreshState();
            this.inherited(arguments);
        },

        reset: function(){
            //      summary:
            //              Additionally reset the displayed textbox value to ''
            this.textbox.value = '';
            this.inherited(arguments);
        }
    }
);

dijit.selectInputText = function(/*DomNode*/element, /*Number?*/ start, /*Number?*/ stop){
    //  summary:
    //      Select text in the input element argument, from start (default 0), to stop (default end).

    // TODO: use functions in _editor/selection.js?
    var _window = dojo.global;
    var _document = dojo.doc;
    element = dojo.byId(element);
    if(isNaN(start)){ start = 0; }
    if(isNaN(stop)){ stop = element.value ? element.value.length : 0; }
    element.focus();
    if(_document["selection"] && dojo.body()["createTextRange"]){ // IE
        if(element.createTextRange){
            var range = element.createTextRange();
            with(range){
                collapse(true);
                moveStart("character", start);
                moveEnd("character", stop);
                select();
            }
        }
    }else if(_window["getSelection"]){
        var selection = _window.getSelection();
        // FIXME: does this work on Safari?
        if(element.setSelectionRange){
            element.setSelectionRange(start, stop);
        }
    }
}

}

if(!dojo._hasResource["dijit.Tooltip"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.Tooltip"] = true;
dojo.provide("dijit.Tooltip");




dojo.declare(
    "dijit._MasterTooltip",
    [dijit._Widget, dijit._Templated],
    {
        // summary
        //      Internal widget that holds the actual tooltip markup,
        //      which occurs once per page.
        //      Called by Tooltip widgets which are just containers to hold
        //      the markup

        // duration: Integer
        //      Milliseconds to fade in/fade out
        duration: dijit.defaultDuration,

        templateString:"<div class=\"dijitTooltip dijitTooltipLeft\" id=\"dojoTooltip\">\n\t<div class=\"dijitTooltipContainer dijitTooltipContents\" dojoAttachPoint=\"containerNode\" waiRole='alert'></div>\n\t<div class=\"dijitTooltipConnector\"></div>\n</div>\n",

        postCreate: function(){
            dojo.body().appendChild(this.domNode);

            this.bgIframe = new dijit.BackgroundIframe(this.domNode);

            // Setup fade-in and fade-out functions.
            this.fadeIn = dojo.fadeIn({ node: this.domNode, duration: this.duration, onEnd: dojo.hitch(this, "_onShow") });
            this.fadeOut = dojo.fadeOut({ node: this.domNode, duration: this.duration, onEnd: dojo.hitch(this, "_onHide") });

        },

        show: function(/*String*/ innerHTML, /*DomNode*/ aroundNode, /*String[]?*/ position){
            // summary:
            //  Display tooltip w/specified contents to right specified node
            //  (To left if there's no space on the right, or if LTR==right)

            if(this.aroundNode && this.aroundNode === aroundNode){
                return;
            }

            if(this.fadeOut.status() == "playing"){
                // previous tooltip is being hidden; wait until the hide completes then show new one
                this._onDeck=arguments;
                return;
            }
            this.containerNode.innerHTML=innerHTML;

            // Firefox bug. when innerHTML changes to be shorter than previous
            // one, the node size will not be updated until it moves.
            this.domNode.style.top = (this.domNode.offsetTop + 1) + "px";

            // position the element and change CSS according to position[] (a list of positions to try)
            var align = {};
            var ltr = this.isLeftToRight();
            dojo.forEach( (position && position.length) ? position : dijit.Tooltip.defaultPosition, function(pos){
                switch(pos){
                    case "after":
                        align[ltr ? "BR" : "BL"] = ltr ? "BL" : "BR";
                        break;
                    case "before":
                        align[ltr ? "BL" : "BR"] = ltr ? "BR" : "BL";
                        break;
                    case "below":
                        // first try to align left borders, next try to align right borders (or reverse for RTL mode)
                        align[ltr ? "BL" : "BR"] = ltr ? "TL" : "TR";
                        align[ltr ? "BR" : "BL"] = ltr ? "TR" : "TL";
                        break;
                    case "above":
                    default:
                        // first try to align left borders, next try to align right borders (or reverse for RTL mode)
                        align[ltr ? "TL" : "TR"] = ltr ? "BL" : "BR";
                        align[ltr ? "TR" : "TL"] = ltr ? "BR" : "BL";
                        break;
                }
            });
            var pos = dijit.placeOnScreenAroundElement(this.domNode, aroundNode, align, dojo.hitch(this, "orient"));

            // show it
            dojo.style(this.domNode, "opacity", 0);
            this.fadeIn.play();
            this.isShowingNow = true;
            this.aroundNode = aroundNode;
        },

        orient: function(/* DomNode */ node, /* String */ aroundCorner, /* String */ tooltipCorner){
            // summary: private function to set CSS for tooltip node based on which position it's in
            node.className = "dijitTooltip " +
                {
                    "BL-TL": "dijitTooltipBelow dijitTooltipABLeft",
                    "TL-BL": "dijitTooltipAbove dijitTooltipABLeft",
                    "BR-TR": "dijitTooltipBelow dijitTooltipABRight",
                    "TR-BR": "dijitTooltipAbove dijitTooltipABRight",
                    "BR-BL": "dijitTooltipRight",
                    "BL-BR": "dijitTooltipLeft"
                }[aroundCorner + "-" + tooltipCorner];
        },

        _onShow: function(){
            if(dojo.isIE){
                // the arrow won't show up on a node w/an opacity filter
                this.domNode.style.filter="";
            }
        },

        hide: function(aroundNode){
            // summary: hide the tooltip
            if(this._onDeck && this._onDeck[1] == aroundNode){
                // this hide request is for a show() that hasn't even started yet;
                // just cancel the pending show()
                this._onDeck=null;
            }else if(this.aroundNode === aroundNode){
                // this hide request is for the currently displayed tooltip
                this.fadeIn.stop();
                this.isShowingNow = false;
                this.aroundNode = null;
                this.fadeOut.play();
            }else{
                // just ignore the call, it's for a tooltip that has already been erased
            }
        },

        _onHide: function(){
            this.domNode.style.cssText="";  // to position offscreen again
            if(this._onDeck){
                // a show request has been queued up; do it now
                this.show.apply(this, this._onDeck);
                this._onDeck=null;
            }
        }

    }
);

dijit.showTooltip = function(/*String*/ innerHTML, /*DomNode*/ aroundNode, /*String[]?*/ position){
    // summary:
    //  Display tooltip w/specified contents in specified position.
    //  See description of dijit.Tooltip.defaultPosition for details on position parameter.
    //  If position is not specified then dijit.Tooltip.defaultPosition is used.
    if(!dijit._masterTT){ dijit._masterTT = new dijit._MasterTooltip(); }
    return dijit._masterTT.show(innerHTML, aroundNode, position);
};

dijit.hideTooltip = function(aroundNode){
    // summary: hide the tooltip
    if(!dijit._masterTT){ dijit._masterTT = new dijit._MasterTooltip(); }
    return dijit._masterTT.hide(aroundNode);
};

dojo.declare(
    "dijit.Tooltip",
    dijit._Widget,
    {
        // summary
        //      Pops up a tooltip (a help message) when you hover over a node.

        // label: String
        //      Text to display in the tooltip.
        //      Specified as innerHTML when creating the widget from markup.
        label: "",

        // showDelay: Integer
        //      Number of milliseconds to wait after hovering over/focusing on the object, before
        //      the tooltip is displayed.
        showDelay: 400,

        // connectId: String[]
        //      Id(s) of domNodes to attach the tooltip to.
        //      When user hovers over any of the specified dom nodes, the tooltip will appear.
        connectId: [],

        //  position: String[]
        //      See description of dijit.Tooltip.defaultPosition for details on position parameter.
        position: [],

        postCreate: function(){

            dojo.addClass(this.domNode,"dijitTooltipData");

            this._connectNodes = [];

            dojo.forEach(this.connectId, function(id) {
                var node = dojo.byId(id);
                if (node) {
                    this._connectNodes.push(node);
                    dojo.forEach(["onMouseEnter", "onMouseLeave", "onFocus", "onBlur"], function(event){
                        this.connect(node, event.toLowerCase(), "_"+event);
                    }, this);
                    if(dojo.isIE){
                        // BiDi workaround
                        node.style.zoom = 1;
                    }
                }
            }, this);
        },

        _onMouseEnter: function(/*Event*/ e){
            this._onHover(e);
        },

        _onMouseLeave: function(/*Event*/ e){
            this._onUnHover(e);
        },

        _onFocus: function(/*Event*/ e){
            this._focus = true;
            this._onHover(e);
            this.inherited(arguments);
        },

        _onBlur: function(/*Event*/ e){
            this._focus = false;
            this._onUnHover(e);
            this.inherited(arguments);
        },

        _onHover: function(/*Event*/ e){
            if(!this._showTimer){
                var target = e.target;
                this._showTimer = setTimeout(dojo.hitch(this, function(){this.open(target)}), this.showDelay);
            }
        },

        _onUnHover: function(/*Event*/ e){
            // keep a tooltip open if the associated element has focus
            if(this._focus){ return; }
            if(this._showTimer){
                clearTimeout(this._showTimer);
                delete this._showTimer;
            }
            this.close();
        },

        open: function(/*DomNode*/ target){
            // summary: display the tooltip; usually not called directly.
            target = target || this._connectNodes[0];
            if(!target){ return; }

            if(this._showTimer){
                clearTimeout(this._showTimer);
                delete this._showTimer;
            }
            dijit.showTooltip(this.label || this.domNode.innerHTML, target, this.position);

            this._connectNode = target;
        },

        close: function(){
            // summary: hide the tooltip or cancel timer for show of tooltip
            if(this._connectNode){
                // if tooltip is currently shown
                dijit.hideTooltip(this._connectNode);
                delete this._connectNode;
            }
            if(this._showTimer){
                // if tooltip is scheduled to be shown (after a brief delay)
                clearTimeout(this._showTimer);
                delete this._showTimer;
            }
        },

        uninitialize: function(){
            this.close();
        }
    }
);

// dijit.Tooltip.defaultPosition: String[]
//      This variable controls the position of tooltips, if the position is not specified to
//      the Tooltip widget or *TextBox widget itself.  It's an array of strings with the following values:
//
//          * before: places tooltip to the left of the target node/widget, or to the right in
//            the case of RTL scripts like Hebrew and Arabic
//          * after: places tooltip to the right of the target node/widget, or to the left in
//            the case of RTL scripts like Hebrew and Arabic
//          * above: tooltip goes above target node
//          * below: tooltip goes below target node
//
//      The list is positions is tried, in order, until a position is found where the tooltip fits
//      within the viewport.
//
//      Be careful setting this parameter.  A value of "above" may work fine until the user scrolls
//      the screen so that there's no room above the target node.   Nodes with drop downs, like
//      DropDownButton or FilteringSelect, are especially problematic, in that you need to be sure
//      that the drop down and tooltip don't overlap, even when the viewport is scrolled so that there
//      is only room below (or above) the target node, but not both.
dijit.Tooltip.defaultPosition = ["after", "before"];

}

if(!dojo._hasResource["dijit.form.ValidationTextBox"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.ValidationTextBox"] = true;
dojo.provide("dijit.form.ValidationTextBox");








/*=====
    dijit.form.ValidationTextBox.__Constraints = function(){
        // locale: String
        //      locale used for validation, picks up value from this widget's lang attribute
        // _flags_: anything
        //      various flags passed to regExpGen function
        this.locale = "";
        this._flags_ = "";
    }
=====*/

dojo.declare(
    "dijit.form.ValidationTextBox",
    dijit.form.TextBox,
    {
        // summary:
        //      A TextBox subclass with the ability to validate content of various types and provide user feedback.

        templateString:"<div class=\"dijit dijitReset dijitInlineTable dijitLeft\"\n\tid=\"widget_${id}\"\n\tdojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\" waiRole=\"presentation\"\n\t><div style=\"overflow:hidden;\"\n\t\t><div class=\"dijitReset dijitValidationIcon\"><br></div\n\t\t><div class=\"dijitReset dijitValidationIconText\">&Chi;</div\n\t\t><div class=\"dijitReset dijitInputField\"\n\t\t\t><input class=\"dijitReset\" dojoAttachPoint='textbox,focusNode' dojoAttachEvent='onkeypress:_onKeyPress' autocomplete=\"off\"\n\t\t\ttype='${type}'\n\t\t/></div\n\t></div\n></div>\n",
        baseClass: "dijitTextBox",

        // default values for new subclass properties
        // required: Boolean
        //      Can be true or false, default is false.
        required: false,

        // promptMessage: String
        //      If defined, display this hint string immediately on focus to the an empty textbox.
        //      Think of this like a tooltip that tells the user what to do, not an error message
        //      that tells the user what they've done wrong.
        //
        //      Message disappears when user starts typing.
        promptMessage: "",

        // invalidMessage: String
        //      The message to display if value is invalid.
        invalidMessage: "$_unset_$", // read from the message file if not overridden

        // constraints: dijit.form.ValidationTextBox.__Constraints
        //      user-defined object needed to pass parameters to the validator functions
        constraints: {},

        // regExp: String
        //      regular expression string used to validate the input
        //      Do not specify both regExp and regExpGen
        regExp: ".*",

        // regExpGen: Function
        //      user replaceable function used to generate regExp when dependent on constraints
        //      Do not specify both regExp and regExpGen
        regExpGen: function(/*dijit.form.ValidationTextBox.__Constraints*/constraints){ return this.regExp; },

        // state: String
        //      Shows current state (ie, validation result) of input (Normal, Warning, or Error)
        state: "",

        //  tooltipPosition: String[]
        //      See description of dijit.Tooltip.defaultPosition for details on this parameter.
        tooltipPosition: [],

        _setValueAttr: function(){
            // summary:
            //      Hook so attr('value', ...) works.
            this.inherited(arguments);
            this.validate(this._focused);
        },

        validator: function(/*anything*/value, /*dijit.form.ValidationTextBox.__Constraints*/constraints){
            // summary: user replaceable function used to validate the text input against the regular expression.
            return (new RegExp("^(?:" + this.regExpGen(constraints) + ")"+(this.required?"":"?")+"$")).test(value) &&
                (!this.required || !this._isEmpty(value)) &&
                (this._isEmpty(value) || this.parse(value, constraints) !== undefined); // Boolean
        },

        _isValidSubset: function(){
            // summary:
            //  Returns true if the value is either already valid or could be made valid by appending characters.
            return this.textbox.value.search(this._partialre) == 0;
        },

        isValid: function(/*Boolean*/ isFocused){
            // summary: Need to over-ride with your own validation code in subclasses
            return this.validator(this.textbox.value, this.constraints);
        },

        _isEmpty: function(value){
            // summary: Checks for whitespace
            return /^\s*$/.test(value); // Boolean
        },

        getErrorMessage: function(/*Boolean*/ isFocused){
            // summary: return an error message to show if appropriate
            return this.invalidMessage; // String
        },

        getPromptMessage: function(/*Boolean*/ isFocused){
            // summary: return a hint to show if appropriate
            return this.promptMessage; // String
        },

        _maskValidSubsetError: true,
        validate: function(/*Boolean*/ isFocused){
            // summary:
            //      Called by oninit, onblur, and onkeypress.
            // description:
            //      Show missing or invalid messages if appropriate, and highlight textbox field.
            var message = "";
            var isValid = this.disabled || this.isValid(isFocused);
            if(isValid){ this._maskValidSubsetError = true; }
            var isValidSubset = !isValid && isFocused && this._isValidSubset();
            var isEmpty = this._isEmpty(this.textbox.value);
            this.state = (isValid || (!this._hasBeenBlurred && isEmpty) || isValidSubset) ? "" : "Error";
            if(this.state == "Error"){ this._maskValidSubsetError = false; }
            this._setStateClass();
            dijit.setWaiState(this.focusNode, "invalid", isValid ? "false" : "true");
            if(isFocused){
                if(isEmpty){
                    message = this.getPromptMessage(true);
                }
                if(!message && (this.state == "Error" || (isValidSubset && !this._maskValidSubsetError))){
                    message = this.getErrorMessage(true);
                }
            }
            this.displayMessage(message);
            return isValid;
        },

        // currently displayed message
        _message: "",

        displayMessage: function(/*String*/ message){
            // summary:
            //      User overridable method to display validation errors/hints.
            //      By default uses a tooltip.
            if(this._message == message){ return; }
            this._message = message;
            dijit.hideTooltip(this.domNode);
            if(message){
                dijit.showTooltip(message, this.domNode, this.tooltipPosition);
            }
        },

        _refreshState: function(){
            this.validate(this._focused);
            this.inherited(arguments);
        },

        //////////// INITIALIZATION METHODS ///////////////////////////////////////

        constructor: function(){
            this.constraints = {};
        },

        postMixInProperties: function(){
            this.inherited(arguments);
            this.constraints.locale = this.lang;
            this.messages = dojo.i18n.getLocalization("dijit.form", "validate", this.lang);
            if(this.invalidMessage == "$_unset_$"){ this.invalidMessage = this.messages.invalidMessage; }
            var p = this.regExpGen(this.constraints);
            this.regExp = p;
            var partialre = "";
            // parse the regexp and produce a new regexp that matches valid subsets
            // if the regexp is .* then there's no use in matching subsets since everything is valid
            if(p != ".*"){ this.regExp.replace(/\\.|\[\]|\[.*?[^\\]{1}\]|\{.*?\}|\(\?[=:!]|./g,
                function (re){
                    switch(re.charAt(0)){
                        case '{':
                        case '+':
                        case '?':
                        case '*':
                        case '^':
                        case '$':
                        case '|':
                        case '(': partialre += re; break;
                        case ")": partialre += "|$)"; break;
                         default: partialre += "(?:"+re+"|$)"; break;
                    }
                }
            );}
            try{ // this is needed for now since the above regexp parsing needs more test verification
                "".search(partialre);
            }catch(e){ // should never be here unless the original RE is bad or the parsing is bad
                partialre = this.regExp;
                console.warn('RegExp error in ' + this.declaredClass + ': ' + this.regExp);
            } // should never be here unless the original RE is bad or the parsing is bad
            this._partialre = "^(?:" + partialre + ")$";
        },

        _setDisabledAttr: function(/*Boolean*/ value){
            this.inherited(arguments);  // call FormValueWidget._setDisabledAttr()
            if(this.valueNode){
                this.valueNode.disabled = value;
            }
            this._refreshState();
        },

        _setRequiredAttr: function(/*Boolean*/ value){
            this.required = value;
            dijit.setWaiState(this.focusNode,"required", value);
            this._refreshState();
        },

        postCreate: function(){
            if(dojo.isIE){ // IE INPUT tag fontFamily has to be set directly using STYLE
                var s = dojo.getComputedStyle(this.focusNode);
                if(s){
                    var ff = s.fontFamily;
                    if(ff){
                        this.focusNode.style.fontFamily = ff;
                    }
                }
            }
            this.inherited(arguments);
        }
    }
);

dojo.declare(
    "dijit.form.MappedTextBox",
    dijit.form.ValidationTextBox,
    {
        // summary:
        //      A dijit.form.ValidationTextBox subclass which provides a visible formatted display and a serializable
        //      value in a hidden input field which is actually sent to the server.  The visible display may
        //      be locale-dependent and interactive.  The value sent to the server is stored in a hidden
        //      input field which uses the `name` attribute declared by the original widget.  That value sent
        //      to the serveris defined by the dijit.form.MappedTextBox.serialize method and is typically
        //      locale-neutral.

        serialize: function(/*anything*/val, /*Object?*/options){
            // summary: user replaceable function used to convert the attr('value') result to a String
            return val.toString ? val.toString() : ""; // String
        },

        toString: function(){
            // summary: display the widget as a printable string using the widget's value
            var val = this.filter(this.attr('value')); // call filter in case value is nonstring and filter has been customized
            return val != null ? (typeof val == "string" ? val : this.serialize(val, this.constraints)) : ""; // String
        },

        validate: function(){
            this.valueNode.value = this.toString();
            return this.inherited(arguments);
        },

        buildRendering: function(){
            this.inherited(arguments);

            // Create a hidden <input> node with the serialized value used for submit
            // (as opposed to the displayed value)
            this.valueNode = dojo.create("input", {
                style:{ display:"none" },
                type: this.type
            }, this.textbox, "after");
        },

        _setDisabledAttr: function(/*Boolean*/ value){
            this.inherited(arguments);
            dojo.attr(this.valueNode, 'disabled', value);
        },

        reset:function(){
            //      summary:
            //              Additionally reset the hidden textbox value to ''
            this.valueNode.value = '';
            this.inherited(arguments);
        }
    }
);

/*=====
    dijit.form.RangeBoundTextBox.__Constraints = function(){
        // min: Number
        //      Minimum signed value.  Default is -Infinity
        // max: Number
        //      Maximum signed value.  Default is +Infinity
        this.min = min;
        this.max = max;
    }
=====*/

dojo.declare(
    "dijit.form.RangeBoundTextBox",
    dijit.form.MappedTextBox,
    {
        // summary:
        //      A dijit.form.MappedTextBox subclass which defines a range of valid values
        //
        // constraints: dijit.form.RangeBoundTextBox.__Constraints
        //
        // rangeMessage: String
        //      The message to display if value is out-of-range

        /*=====
        constraints: {},
        ======*/
        rangeMessage: "",

        rangeCheck: function(/*Number*/ primitive, /*dijit.form.RangeBoundTextBox.__Constraints*/ constraints){
            // summary: user replaceable function used to validate the range of the numeric input value
            var isMin = "min" in constraints;
            var isMax = "max" in constraints;
            if(isMin || isMax){
                return (!isMin || this.compare(primitive,constraints.min) >= 0) &&
                    (!isMax || this.compare(primitive,constraints.max) <= 0);
            }
            return true; // Boolean
        },

        isInRange: function(/*Boolean*/ isFocused){
            // summary: Need to over-ride with your own validation code in subclasses
            return this.rangeCheck(this.attr('value'), this.constraints);
        },

        _isDefinitelyOutOfRange: function(){
            // summary:
            //  Returns true if the value is out of range and will remain
            //  out of range even if the user types more characters
            var val = this.attr('value');
            var isTooLittle = false;
            var isTooMuch = false;
            if("min" in this.constraints){
                var min = this.constraints.min;
                val = this.compare(val, ((typeof min == "number") && min >= 0 && val !=0)? 0 : min);
                isTooLittle = (typeof val == "number") && val < 0;
            }
            if("max" in this.constraints){
                var max = this.constraints.max;
                val = this.compare(val, ((typeof max != "number") || max > 0)? max : 0);
                isTooMuch = (typeof val == "number") && val > 0;
            }
            return isTooLittle || isTooMuch;
        },

        _isValidSubset: function(){
            return this.inherited(arguments) && !this._isDefinitelyOutOfRange();
        },

        isValid: function(/*Boolean*/ isFocused){
            return this.inherited(arguments) &&
                ((this._isEmpty(this.textbox.value) && !this.required) || this.isInRange(isFocused)); // Boolean
        },

        getErrorMessage: function(/*Boolean*/ isFocused){
            if(dijit.form.RangeBoundTextBox.superclass.isValid.call(this, false) && !this.isInRange(isFocused)){ return this.rangeMessage; } // String
            return this.inherited(arguments);
        },

        postMixInProperties: function(){
            this.inherited(arguments);
            if(!this.rangeMessage){
                this.messages = dojo.i18n.getLocalization("dijit.form", "validate", this.lang);
                this.rangeMessage = this.messages.rangeMessage;
            }
        },

        postCreate: function(){
            this.inherited(arguments);
            if(this.constraints.min !== undefined){
                dijit.setWaiState(this.focusNode, "valuemin", this.constraints.min);
            }
            if(this.constraints.max !== undefined){
                dijit.setWaiState(this.focusNode, "valuemax", this.constraints.max);
            }
        },

        _setValueAttr: function(/*Number*/ value, /*Boolean?*/ priorityChange){
            // summary:
            //      Hook so attr('value', ...) works.
            dijit.setWaiState(this.focusNode, "valuenow", value);
            this.inherited(arguments);
        }
    }
);

}

if(!dojo._hasResource["dijit.form._Spinner"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form._Spinner"] = true;
dojo.provide("dijit.form._Spinner");



dojo.declare(
    "dijit.form._Spinner",
    dijit.form.RangeBoundTextBox,
    {

        // summary: Mixin for validation widgets with a spinner
        // description: This class basically (conceptually) extends dijit.form.ValidationTextBox.
        //  It modifies the template to have up/down arrows, and provides related handling code.

        // defaultTimeout: Number
        //    number of milliseconds before a held key or button becomes typematic
        defaultTimeout: 500,

        // timeoutChangeRate: Number
        //    fraction of time used to change the typematic timer between events
        //    1.0 means that each typematic event fires at defaultTimeout intervals
        //    < 1.0 means that each typematic event fires at an increasing faster rate
        timeoutChangeRate: 0.90,

        // smallDelta: Number
        //    adjust the value by this much when spinning using the arrow keys/buttons
        smallDelta: 1,
        // largeDelta: Number
        //    adjust the value by this much when spinning using the PgUp/Dn keys
        largeDelta: 10,

        templateString:"<div class=\"dijit dijitReset dijitInlineTable dijitLeft\"\n\tid=\"widget_${id}\"\n\tdojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\" waiRole=\"presentation\"\n\t><div class=\"dijitInputLayoutContainer\"\n\t\t><div class=\"dijitReset dijitSpinnerButtonContainer\"\n\t\t\t>&nbsp;<div class=\"dijitReset dijitLeft dijitButtonNode dijitArrowButton dijitUpArrowButton\"\n\t\t\t\tdojoAttachPoint=\"upArrowNode\"\n\t\t\t\tdojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse\"\n\t\t\t\tstateModifier=\"UpArrow\"\n\t\t\t\t><div class=\"dijitArrowButtonInner\">&thinsp;</div\n\t\t\t\t><div class=\"dijitArrowButtonChar\">&#9650;</div\n\t\t\t></div\n\t\t\t><div class=\"dijitReset dijitLeft dijitButtonNode dijitArrowButton dijitDownArrowButton\"\n\t\t\t\tdojoAttachPoint=\"downArrowNode\"\n\t\t\t\tdojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse\"\n\t\t\t\tstateModifier=\"DownArrow\"\n\t\t\t\t><div class=\"dijitArrowButtonInner\">&thinsp;</div\n\t\t\t\t><div class=\"dijitArrowButtonChar\">&#9660;</div\n\t\t\t></div\n\t\t></div\n\t\t><div class=\"dijitReset dijitValidationIcon\"><br></div\n\t\t><div class=\"dijitReset dijitValidationIconText\">&Chi;</div\n\t\t><div class=\"dijitReset dijitInputField\"\n\t\t\t><input class='dijitReset' dojoAttachPoint=\"textbox,focusNode\" type=\"${type}\" dojoAttachEvent=\"onkeypress:_onKeyPress\"\n\t\t\t\twaiRole=\"spinbutton\" autocomplete=\"off\"\n\t\t/></div\n\t></div\n></div>\n",
        baseClass: "dijitSpinner",

        adjust: function(/* Object */ val, /*Number*/ delta){
            // summary: user replaceable function used to adjust a primitive value(Number/Date/...) by the delta amount specified
            // the val is adjusted in a way that makes sense to the object type
            return val;
        },

        _arrowState: function(/*Node*/ node, /*Boolean*/ pressed){
            this._active = pressed;
            this.stateModifier = node.getAttribute("stateModifier") || "";
            this._setStateClass();
        },

        _arrowPressed: function(/*Node*/ nodePressed, /*Number*/ direction, /*Number*/ increment){
            if(this.disabled || this.readOnly){ return; }
            this._arrowState(nodePressed, true);
            this._setValueAttr(this.adjust(this.attr('value'), direction*increment), false);
            dijit.selectInputText(this.textbox, this.textbox.value.length);
        },

        _arrowReleased: function(/*Node*/ node){
            this._wheelTimer = null;
            if(this.disabled || this.readOnly){ return; }
            this._arrowState(node, false);
        },

        _typematicCallback: function(/*Number*/ count, /*DOMNode*/ node, /*Event*/ evt){
            var inc=this.smallDelta;
            if(node == this.textbox){
                var k=dojo.keys;
                var key = evt.charOrCode;
                inc = (key == k.PAGE_UP || key == k.PAGE_DOWN) ? this.largeDelta : this.smallDelta;
                node = (key == k.UP_ARROW ||key == k.PAGE_UP) ? this.upArrowNode : this.downArrowNode;
            }
            if(count == -1){ this._arrowReleased(node); }
            else{ this._arrowPressed(node, (node == this.upArrowNode) ? 1 : -1, inc); }
        },

        _wheelTimer: null,
        _mouseWheeled: function(/*Event*/ evt){
            // summary: Mouse wheel listener where supported
            dojo.stopEvent(evt);
            // FIXME: Safari bubbles

            // be nice to DOH and scroll as much as the event says to
            var scrollAmount = evt.detail ? (evt.detail * -1) : (evt.wheelDelta / 120);
            if(scrollAmount !== 0){
                var node = this[(scrollAmount > 0 ? "upArrowNode" : "downArrowNode" )];

                this._arrowPressed(node, scrollAmount, this.smallDelta);

                if(!this._wheelTimer){
                    clearTimeout(this._wheelTimer);
                }
                this._wheelTimer = setTimeout(dojo.hitch(this,"_arrowReleased",node), 50);
            }

        },

        postCreate: function(){
            this.inherited('postCreate', arguments);

            // extra listeners
            this.connect(this.domNode, !dojo.isMozilla ? "onmousewheel" : 'DOMMouseScroll', "_mouseWheeled");
            this._connects.push(dijit.typematic.addListener(this.upArrowNode, this.textbox, {charOrCode:dojo.keys.UP_ARROW,ctrlKey:false,altKey:false,shiftKey:false}, this, "_typematicCallback", this.timeoutChangeRate, this.defaultTimeout));
            this._connects.push(dijit.typematic.addListener(this.downArrowNode, this.textbox, {charOrCode:dojo.keys.DOWN_ARROW,ctrlKey:false,altKey:false,shiftKey:false}, this, "_typematicCallback", this.timeoutChangeRate, this.defaultTimeout));
            this._connects.push(dijit.typematic.addListener(this.upArrowNode, this.textbox, {charOrCode:dojo.keys.PAGE_UP,ctrlKey:false,altKey:false,shiftKey:false}, this, "_typematicCallback", this.timeoutChangeRate, this.defaultTimeout));
            this._connects.push(dijit.typematic.addListener(this.downArrowNode, this.textbox, {charOrCode:dojo.keys.PAGE_DOWN,ctrlKey:false,altKey:false,shiftKey:false}, this, "_typematicCallback", this.timeoutChangeRate, this.defaultTimeout));
            if(dojo.isIE){
                var _this = this;
                this.connect(this.domNode, "onresize",
                    function(){ setTimeout(dojo.hitch(_this,
                        function(){
                                var sz = this.upArrowNode.parentNode.offsetHeight;
                            if(sz){
                                this.upArrowNode.style.height = sz >> 1;
                                this.downArrowNode.style.height = sz - (sz >> 1);
                                this.focusNode.parentNode.style.height = sz;
                            }
                            // cause IE to rerender when spinner is moved from hidden to visible
                            this._setStateClass();
                        }), 0);
                    }
                );
            }
        }
});

}

if(!dojo._hasResource["dojo.regexp"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.regexp"] = true;
dojo.provide("dojo.regexp");

/*=====
dojo.regexp = {
    // summary: Regular expressions and Builder resources
};
=====*/

dojo.regexp.escapeString = function(/*String*/str, /*String?*/except){
    //  summary:
    //      Adds escape sequences for special characters in regular expressions
    // except:
    //      a String with special characters to be left unescaped

    return str.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, function(ch){
        if(except && except.indexOf(ch) != -1){
            return ch;
        }
        return "\\" + ch;
    }); // String
}

dojo.regexp.buildGroupRE = function(/*Object|Array*/arr, /*Function*/re, /*Boolean?*/nonCapture){
    //  summary:
    //      Builds a regular expression that groups subexpressions
    //  description:
    //      A utility function used by some of the RE generators. The
    //      subexpressions are constructed by the function, re, in the second
    //      parameter.  re builds one subexpression for each elem in the array
    //      a, in the first parameter. Returns a string for a regular
    //      expression that groups all the subexpressions.
    // arr:
    //      A single value or an array of values.
    // re:
    //      A function. Takes one parameter and converts it to a regular
    //      expression.
    // nonCapture:
    //      If true, uses non-capturing match, otherwise matches are retained
    //      by regular expression. Defaults to false

    // case 1: a is a single value.
    if(!(arr instanceof Array)){
        return re(arr); // String
    }

    // case 2: a is an array
    var b = [];
    for(var i = 0; i < arr.length; i++){
        // convert each elem to a RE
        b.push(re(arr[i]));
    }

     // join the REs as alternatives in a RE group.
    return dojo.regexp.group(b.join("|"), nonCapture); // String
}

dojo.regexp.group = function(/*String*/expression, /*Boolean?*/nonCapture){
    // summary:
    //      adds group match to expression
    // nonCapture:
    //      If true, uses non-capturing match, otherwise matches are retained
    //      by regular expression.
    return "(" + (nonCapture ? "?:":"") + expression + ")"; // String
}

}

if(!dojo._hasResource["dojo.number"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.number"] = true;
dojo.provide("dojo.number");







/*=====
dojo.number = {
    // summary: localized formatting and parsing routines for Number
}

dojo.number.__FormatOptions = function(){
    //  pattern: String?
    //      override [formatting pattern](http://www.unicode.org/reports/tr35/#Number_Format_Patterns)
    //      with this string
    //  type: String?
    //      choose a format type based on the locale from the following:
    //      decimal, scientific, percent, currency. decimal by default.
    //  places: Number?
    //      fixed number of decimal places to show.  This overrides any
    //      information in the provided pattern.
    //  round: Number?
    //      5 rounds to nearest .5; 0 rounds to nearest whole (default). -1
    //      means do not round.
    //  currency: String?
    //      an [ISO4217](http://en.wikipedia.org/wiki/ISO_4217) currency code, a three letter sequence like "USD"
    //  symbol: String?
    //      localized currency symbol
    //  locale: String?
    //      override the locale used to determine formatting rules
    this.pattern = pattern;
    this.type = type;
    this.places = places;
    this.round = round;
    this.currency = currency;
    this.symbol = symbol;
    this.locale = locale;
}
=====*/

dojo.number.format = function(/*Number*/value, /*dojo.number.__FormatOptions?*/options){
    // summary:
    //      Format a Number as a String, using locale-specific settings
    // description:
    //      Create a string from a Number using a known localized pattern.
    //      Formatting patterns appropriate to the locale are chosen from the
    //      [CLDR](http://unicode.org/cldr) as well as the appropriate symbols and
    //      delimiters.  See <http://www.unicode.org/reports/tr35/#Number_Elements>
    // value:
    //      the number to be formatted.  If not a valid JavaScript number,
    //      return null.

    options = dojo.mixin({}, options || {});
    var locale = dojo.i18n.normalizeLocale(options.locale);
    var bundle = dojo.i18n.getLocalization("dojo.cldr", "number", locale);
    options.customs = bundle;
    var pattern = options.pattern || bundle[(options.type || "decimal") + "Format"];
    if(isNaN(value)){ return null; } // null
    return dojo.number._applyPattern(value, pattern, options); // String
};

//dojo.number._numberPatternRE = /(?:[#0]*,?)*[#0](?:\.0*#*)?/; // not precise, but good enough
dojo.number._numberPatternRE = /[#0,]*[#0](?:\.0*#*)?/; // not precise, but good enough

dojo.number._applyPattern = function(/*Number*/value, /*String*/pattern, /*dojo.number.__FormatOptions?*/options){
    // summary:
    //      Apply pattern to format value as a string using options. Gives no
    //      consideration to local customs.
    // value:
    //      the number to be formatted.
    // pattern:
    //      a pattern string as described by
    //      [unicode.org TR35](http://www.unicode.org/reports/tr35/#Number_Format_Patterns)
    // options: dojo.number.__FormatOptions?
    //      _applyPattern is usually called via `dojo.number.format()` which
    //      populates an extra property in the options parameter, "customs".
    //      The customs object specifies group and decimal parameters if set.

    //TODO: support escapes
    options = options || {};
    var group = options.customs.group;
    var decimal = options.customs.decimal;

    var patternList = pattern.split(';');
    var positivePattern = patternList[0];
    pattern = patternList[(value < 0) ? 1 : 0] || ("-" + positivePattern);

    //TODO: only test against unescaped
    if(pattern.indexOf('%') != -1){
        value *= 100;
    }else if(pattern.indexOf('\u2030') != -1){
        value *= 1000; // per mille
    }else if(pattern.indexOf('\u00a4') != -1){
        group = options.customs.currencyGroup || group;//mixins instead?
        decimal = options.customs.currencyDecimal || decimal;// Should these be mixins instead?
        pattern = pattern.replace(/\u00a4{1,3}/, function(match){
            var prop = ["symbol", "currency", "displayName"][match.length-1];
            return options[prop] || options.currency || "";
        });
    }else if(pattern.indexOf('E') != -1){
        throw new Error("exponential notation not supported");
    }

    //TODO: support @ sig figs?
    var numberPatternRE = dojo.number._numberPatternRE;
    var numberPattern = positivePattern.match(numberPatternRE);
    if(!numberPattern){
        throw new Error("unable to find a number expression in pattern: "+pattern);
    }
    if(options.fractional === false){ options.places = 0; }
    return pattern.replace(numberPatternRE,
        dojo.number._formatAbsolute(value, numberPattern[0], {decimal: decimal, group: group, places: options.places, round: options.round}));
}

dojo.number.round = function(/*Number*/value, /*Number?*/places, /*Number?*/increment){
    //  summary:
    //      An inexact rounding method to compensate for binary floating point artifacts and browser quirks.
    //  description:
    //      Rounds to the nearest value with the given number of decimal places, away from zero if equal,
    //      similar to Number.toFixed().  Rounding can be done by fractional increments also.
    //      Makes minor adjustments to accommodate for precision errors due to binary floating point representation
    //      of Javascript Numbers.  See http://speleotrove.com/decimal/decifaq.html for more information.
    //      Because of this adjustment, the rounding may not be mathematically correct for full precision
    //      floating point values.  The calculations assume 14 significant figures, so the number of decimal
    //      places preserved will vary with the magnitude of the input.  This is not a substitute for
    //      decimal arithmetic.
    //  value:
    //      The number to round
    //  places:
    //      The number of decimal places where rounding takes place.  Defaults to 0 for whole rounding.
    //      Must be non-negative.
    //  increment:
    //      Rounds next place to nearest value of increment/10.  10 by default.
    //  example:
    //      >>> 4.8-(1.1+2.2)
    //      1.4999999999999996
    //      >>> Math.round(4.8-(1.1+2.2))
    //      1
    //      >>> dojo.number.round(4.8-(1.1+2.2))
    //      2
    //      >>> ((4.8-(1.1+2.2))/100)
    //      0.014999999999999996
    //      >>> ((4.8-(1.1+2.2))/100).toFixed(2)
    //      "0.01"
    //      >>> dojo.number.round((4.8-(1.1+2.2))/100,2)
    //      0.02
    //      >>> dojo.number.round(10.71, 0, 2.5)
    //      10.75
    var wholeFigs = Math.log(Math.abs(value))/Math.log(10);
    var factor = 10 / (increment || 10);
    var delta = Math.pow(10, -14 + wholeFigs);
    return (factor * (+value + (value > 0 ? delta : -delta))).toFixed(places) / factor; // Number
}

if((0.9).toFixed() == 0){
    // (isIE) toFixed() bug workaround: Rounding fails on IE when most significant digit
    // is just after the rounding place and is >=5
    (function(){
        var round = dojo.number.round;
        dojo.number.round = function(v, p, m){
            var d = Math.pow(10, -p || 0), a = Math.abs(v);
            if(!v || a >= d || a * Math.pow(10, p + 1) < 5){
                d = 0;
            }
            return round(v, p, m) + (v > 0 ? d : -d);
        }
    })();
}

/*=====
dojo.number.__FormatAbsoluteOptions = function(){
    //  decimal: String?
    //      the decimal separator
    //  group: String?
    //      the group separator
    //  places: Integer?|String?
    //      number of decimal places.  the range "n,m" will format to m places.
    //  round: Number?
    //      5 rounds to nearest .5; 0 rounds to nearest whole (default). -1
    //      means don't round.
    this.decimal = decimal;
    this.group = group;
    this.places = places;
    this.round = round;
}
=====*/

dojo.number._formatAbsolute = function(/*Number*/value, /*String*/pattern, /*dojo.number.__FormatAbsoluteOptions?*/options){
    // summary:
    //      Apply numeric pattern to absolute value using options. Gives no
    //      consideration to local customs.
    // value:
    //      the number to be formatted, ignores sign
    // pattern:
    //      the number portion of a pattern (e.g. `#,##0.00`)
    options = options || {};
    if(options.places === true){options.places=0;}
    if(options.places === Infinity){options.places=6;} // avoid a loop; pick a limit

    var patternParts = pattern.split(".");
    var maxPlaces = (options.places >= 0) ? options.places : (patternParts[1] && patternParts[1].length) || 0;
    if(!(options.round < 0)){
        value = dojo.number.round(value, maxPlaces, options.round);
    }

    var valueParts = String(Math.abs(value)).split(".");
    var fractional = valueParts[1] || "";
    if(options.places){
        var comma = dojo.isString(options.places) && options.places.indexOf(",");
        if(comma){
            options.places = options.places.substring(comma+1);
        }
        valueParts[1] = dojo.string.pad(fractional.substr(0, options.places), options.places, '0', true);
    }else if(patternParts[1] && options.places !== 0){
        // Pad fractional with trailing zeros
        var pad = patternParts[1].lastIndexOf("0") + 1;
        if(pad > fractional.length){
            valueParts[1] = dojo.string.pad(fractional, pad, '0', true);
        }

        // Truncate fractional
        var places = patternParts[1].length;
        if(places < fractional.length){
            valueParts[1] = fractional.substr(0, places);
        }
    }else{
        if(valueParts[1]){ valueParts.pop(); }
    }

    // Pad whole with leading zeros
    var patternDigits = patternParts[0].replace(',', '');
    pad = patternDigits.indexOf("0");
    if(pad != -1){
        pad = patternDigits.length - pad;
        if(pad > valueParts[0].length){
            valueParts[0] = dojo.string.pad(valueParts[0], pad);
        }

        // Truncate whole
        if(patternDigits.indexOf("#") == -1){
            valueParts[0] = valueParts[0].substr(valueParts[0].length - pad);
        }
    }

    // Add group separators
    var index = patternParts[0].lastIndexOf(',');
    var groupSize, groupSize2;
    if(index != -1){
        groupSize = patternParts[0].length - index - 1;
        var remainder = patternParts[0].substr(0, index);
        index = remainder.lastIndexOf(',');
        if(index != -1){
            groupSize2 = remainder.length - index - 1;
        }
    }
    var pieces = [];
    for(var whole = valueParts[0]; whole;){
        var off = whole.length - groupSize;
        pieces.push((off > 0) ? whole.substr(off) : whole);
        whole = (off > 0) ? whole.slice(0, off) : "";
        if(groupSize2){
            groupSize = groupSize2;
            delete groupSize2;
        }
    }
    valueParts[0] = pieces.reverse().join(options.group || ",");

    return valueParts.join(options.decimal || ".");
};

/*=====
dojo.number.__RegexpOptions = function(){
    //  pattern: String?
    //      override pattern with this string.  Default is provided based on
    //      locale.
    //  type: String?
    //      choose a format type based on the locale from the following:
    //      decimal, scientific, percent, currency. decimal by default.
    //  locale: String?
    //      override the locale used to determine formatting rules
    //  strict: Boolean?
    //      strict parsing, false by default
    //  places: Number|String?
    //      number of decimal places to accept: Infinity, a positive number, or
    //      a range "n,m".  Defined by pattern or Infinity if pattern not provided.
    this.pattern = pattern;
    this.type = type;
    this.locale = locale;
    this.strict = strict;
    this.places = places;
}
=====*/
dojo.number.regexp = function(/*dojo.number.__RegexpOptions?*/options){
    //  summary:
    //      Builds the regular needed to parse a number
    //  description:
    //      Returns regular expression with positive and negative match, group
    //      and decimal separators
    return dojo.number._parseInfo(options).regexp; // String
}

dojo.number._parseInfo = function(/*Object?*/options){
    options = options || {};
    var locale = dojo.i18n.normalizeLocale(options.locale);
    var bundle = dojo.i18n.getLocalization("dojo.cldr", "number", locale);
    var pattern = options.pattern || bundle[(options.type || "decimal") + "Format"];
//TODO: memoize?
    var group = bundle.group;
    var decimal = bundle.decimal;
    var factor = 1;

    if(pattern.indexOf('%') != -1){
        factor /= 100;
    }else if(pattern.indexOf('\u2030') != -1){
        factor /= 1000; // per mille
    }else{
        var isCurrency = pattern.indexOf('\u00a4') != -1;
        if(isCurrency){
            group = bundle.currencyGroup || group;
            decimal = bundle.currencyDecimal || decimal;
        }
    }

    //TODO: handle quoted escapes
    var patternList = pattern.split(';');
    if(patternList.length == 1){
        patternList.push("-" + patternList[0]);
    }

    var re = dojo.regexp.buildGroupRE(patternList, function(pattern){
        pattern = "(?:"+dojo.regexp.escapeString(pattern, '.')+")";
        return pattern.replace(dojo.number._numberPatternRE, function(format){
            var flags = {
                signed: false,
                separator: options.strict ? group : [group,""],
                fractional: options.fractional,
                decimal: decimal,
                exponent: false};
            var parts = format.split('.');
            var places = options.places;
            if(parts.length == 1 || places === 0){flags.fractional = false;}
            else{
                if(places === undefined){ places = options.pattern ? parts[1].lastIndexOf('0')+1 : Infinity; }
                if(places && options.fractional == undefined){flags.fractional = true;} // required fractional, unless otherwise specified
                if(!options.places && (places < parts[1].length)){ places += "," + parts[1].length; }
                flags.places = places;
            }
            var groups = parts[0].split(',');
            if(groups.length>1){
                flags.groupSize = groups.pop().length;
                if(groups.length>1){
                    flags.groupSize2 = groups.pop().length;
                }
            }
            return "("+dojo.number._realNumberRegexp(flags)+")";
        });
    }, true);

    if(isCurrency){
        // substitute the currency symbol for the placeholder in the pattern
        re = re.replace(/([\s\xa0]*)(\u00a4{1,3})([\s\xa0]*)/g, function(match, before, target, after){
            var prop = ["symbol", "currency", "displayName"][target.length-1];
            var symbol = dojo.regexp.escapeString(options[prop] || options.currency || "");
            before = before ? "[\\s\\xa0]" : "";
            after = after ? "[\\s\\xa0]" : "";
            if(!options.strict){
                if(before){before += "*";}
                if(after){after += "*";}
                return "(?:"+before+symbol+after+")?";
            }
            return before+symbol+after;
        });
    }

//TODO: substitute localized sign/percent/permille/etc.?

    // normalize whitespace and return
    return {regexp: re.replace(/[\xa0 ]/g, "[\\s\\xa0]"), group: group, decimal: decimal, factor: factor}; // Object
}

/*=====
dojo.number.__ParseOptions = function(){
    //  pattern: String
    //      override pattern with this string.  Default is provided based on
    //      locale.
    //  type: String?
    //      choose a format type based on the locale from the following:
    //      decimal, scientific, percent, currency. decimal by default.
    //  locale: String
    //      override the locale used to determine formatting rules
    //  strict: Boolean?
    //      strict parsing, false by default
    //  currency: Object
    //      object with currency information
    this.pattern = pattern;
    this.type = type;
    this.locale = locale;
    this.strict = strict;
    this.currency = currency;
}
=====*/
dojo.number.parse = function(/*String*/expression, /*dojo.number.__ParseOptions?*/options){
    // summary:
    //      Convert a properly formatted string to a primitive Number, using
    //      locale-specific settings.
    // description:
    //      Create a Number from a string using a known localized pattern.
    //      Formatting patterns are chosen appropriate to the locale
    //      and follow the syntax described by
    //      [unicode.org TR35](http://www.unicode.org/reports/tr35/#Number_Format_Patterns)
    // expression:
    //      A string representation of a Number
    var info = dojo.number._parseInfo(options);
    var results = (new RegExp("^"+info.regexp+"$")).exec(expression);
    if(!results){
        return NaN; //NaN
    }
    var absoluteMatch = results[1]; // match for the positive expression
    if(!results[1]){
        if(!results[2]){
            return NaN; //NaN
        }
        // matched the negative pattern
        absoluteMatch =results[2];
        info.factor *= -1;
    }

    // Transform it to something Javascript can parse as a number.  Normalize
    // decimal point and strip out group separators or alternate forms of whitespace
    absoluteMatch = absoluteMatch.
        replace(new RegExp("["+info.group + "\\s\\xa0"+"]", "g"), "").
        replace(info.decimal, ".");
    // Adjust for negative sign, percent, etc. as necessary
    return absoluteMatch * info.factor; //Number
};

/*=====
dojo.number.__RealNumberRegexpFlags = function(){
    //  places: Number?
    //      The integer number of decimal places or a range given as "n,m".  If
    //      not given, the decimal part is optional and the number of places is
    //      unlimited.
    //  decimal: String?
    //      A string for the character used as the decimal point.  Default
    //      is ".".
    //  fractional: Boolean|Array?
    //      Whether decimal places are allowed.  Can be true, false, or [true,
    //      false].  Default is [true, false]
    //  exponent: Boolean|Array?
    //      Express in exponential notation.  Can be true, false, or [true,
    //      false]. Default is [true, false], (i.e. will match if the
    //      exponential part is present are not).
    //  eSigned: Boolean|Array?
    //      The leading plus-or-minus sign on the exponent.  Can be true,
    //      false, or [true, false].  Default is [true, false], (i.e. will
    //      match if it is signed or unsigned).  flags in regexp.integer can be
    //      applied.
    this.places = places;
    this.decimal = decimal;
    this.fractional = fractional;
    this.exponent = exponent;
    this.eSigned = eSigned;
}
=====*/

dojo.number._realNumberRegexp = function(/*dojo.number.__RealNumberRegexpFlags?*/flags){
    // summary:
    //      Builds a regular expression to match a real number in exponential
    //      notation

    // assign default values to missing paramters
    flags = flags || {};
    //TODO: use mixin instead?
    if(!("places" in flags)){ flags.places = Infinity; }
    if(typeof flags.decimal != "string"){ flags.decimal = "."; }
    if(!("fractional" in flags) || /^0/.test(flags.places)){ flags.fractional = [true, false]; }
    if(!("exponent" in flags)){ flags.exponent = [true, false]; }
    if(!("eSigned" in flags)){ flags.eSigned = [true, false]; }

    // integer RE
    var integerRE = dojo.number._integerRegexp(flags);

    // decimal RE
    var decimalRE = dojo.regexp.buildGroupRE(flags.fractional,
        function(q){
            var re = "";
            if(q && (flags.places!==0)){
                re = "\\" + flags.decimal;
                if(flags.places == Infinity){
                    re = "(?:" + re + "\\d+)?";
                }else{
                    re += "\\d{" + flags.places + "}";
                }
            }
            return re;
        },
        true
    );

    // exponent RE
    var exponentRE = dojo.regexp.buildGroupRE(flags.exponent,
        function(q){
            if(q){ return "([eE]" + dojo.number._integerRegexp({ signed: flags.eSigned}) + ")"; }
            return "";
        }
    );

    // real number RE
    var realRE = integerRE + decimalRE;
    // allow for decimals without integers, e.g. .25
    if(decimalRE){realRE = "(?:(?:"+ realRE + ")|(?:" + decimalRE + "))";}
    return realRE + exponentRE; // String
};

/*=====
dojo.number.__IntegerRegexpFlags = function(){
    //  signed: Boolean?
    //      The leading plus-or-minus sign. Can be true, false, or `[true,false]`.
    //      Default is `[true, false]`, (i.e. will match if it is signed
    //      or unsigned).
    //  separator: String?
    //      The character used as the thousands separator. Default is no
    //      separator. For more than one symbol use an array, e.g. `[",", ""]`,
    //      makes ',' optional.
    //  groupSize: Number?
    //      group size between separators
    //  groupSize2: Number?
    //      second grouping, where separators 2..n have a different interval than the first separator (for India)
    this.signed = signed;
    this.separator = separator;
    this.groupSize = groupSize;
    this.groupSize2 = groupSize2;
}
=====*/

dojo.number._integerRegexp = function(/*dojo.number.__IntegerRegexpFlags?*/flags){
    // summary:
    //      Builds a regular expression that matches an integer

    // assign default values to missing paramters
    flags = flags || {};
    if(!("signed" in flags)){ flags.signed = [true, false]; }
    if(!("separator" in flags)){
        flags.separator = "";
    }else if(!("groupSize" in flags)){
        flags.groupSize = 3;
    }
    // build sign RE
    var signRE = dojo.regexp.buildGroupRE(flags.signed,
        function(q){ return q ? "[-+]" : ""; },
        true
    );

    // number RE
    var numberRE = dojo.regexp.buildGroupRE(flags.separator,
        function(sep){
            if(!sep){
                return "(?:\\d+)";
            }

            sep = dojo.regexp.escapeString(sep);
            if(sep == " "){ sep = "\\s"; }
            else if(sep == "\xa0"){ sep = "\\s\\xa0"; }

            var grp = flags.groupSize, grp2 = flags.groupSize2;
            //TODO: should we continue to enforce that numbers with separators begin with 1-9?  See #6933
            if(grp2){
                var grp2RE = "(?:0|[1-9]\\d{0," + (grp2-1) + "}(?:[" + sep + "]\\d{" + grp2 + "})*[" + sep + "]\\d{" + grp + "})";
                return ((grp-grp2) > 0) ? "(?:" + grp2RE + "|(?:0|[1-9]\\d{0," + (grp-1) + "}))" : grp2RE;
            }
            return "(?:0|[1-9]\\d{0," + (grp-1) + "}(?:[" + sep + "]\\d{" + grp + "})*)";
        },
        true
    );

    // integer RE
    return signRE + numberRE; // String
}

}

if(!dojo._hasResource["dijit.form.NumberTextBox"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.NumberTextBox"] = true;
dojo.provide("dijit.form.NumberTextBox");




/*=====
dojo.declare(
    "dijit.form.NumberTextBox.__Constraints",
    [dijit.form.RangeBoundTextBox.__Constraints, dojo.number.__FormatOptions, dojo.number.__ParseOptions]
);
=====*/

dojo.declare("dijit.form.NumberTextBoxMixin",
    null,
    {
        // summary:
        //      A mixin for all number textboxes

        // TODOC: no inherited. ValidationTextBox describes this, but why is this here:
        regExpGen: dojo.number.regexp,

        /*=====
        // constraints: dijit.form.NumberTextBox.__Constraints
        constraints: {},
        ======*/

        // editOptions: Object
        //      properties to mix into constraints when the value is being edited
        editOptions: { pattern: '#.######' },

        _formatter: dojo.number.format,

        _onFocus: function(){
            if(this.disabled){ return; }
            var val = this.attr('value');
            if(typeof val == "number" && !isNaN(val)){
                this.textbox.value = this.format(val, this.constraints);
            }
            this.inherited(arguments);
        },

        format: function(/*Number*/ value, /*dojo.number.__FormatOptions*/ constraints){
            //  summary: formats the value as a Number, according to constraints

            if(typeof value == "string") { return value; }
            if(isNaN(value)){ return ""; }
            if(this.editOptions && this._focused){
                constraints = dojo.mixin(dojo.mixin({}, this.editOptions), this.constraints);
            }
            return this._formatter(value, constraints);
        },

        parse: dojo.number.parse,
        /*=====
        parse: function(value, constraints){
            //  summary: parses the value as a Number, according to constraints
            //  value: String
            //
            //  constraints: dojo.number.__ParseOptions
        },
        =====*/

        _getDisplayedValueAttr: function(){
            var v = this.inherited(arguments);
            return isNaN(v) ? this.textbox.value : v;
        },

        filter: function(/*Number*/ value){
            return (value === null || value === '' || value === undefined) ? NaN : this.inherited(arguments); // attr('value', null||''||undefined) should fire onChange(NaN)
        },

        serialize: function(/*Number*/ value, /*Object?*/options){
            return (typeof value != "number" || isNaN(value))? '' : this.inherited(arguments);
        },

        _getValueAttr: function(){
            // summary:
            //      Hook so attr('value') works.
            var v = this.inherited(arguments);
            if(isNaN(v) && this.textbox.value !== ''){ return undefined; }
            return v;
        },

        value: NaN
    }
);

dojo.declare("dijit.form.NumberTextBox",
    [dijit.form.RangeBoundTextBox,dijit.form.NumberTextBoxMixin],
    {
        // summary:
        //      A validating, serializable, range-bound text box.
    }
);

}

if(!dojo._hasResource["dijit.form.NumberSpinner"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.NumberSpinner"] = true;
dojo.provide("dijit.form.NumberSpinner");




dojo.declare("dijit.form.NumberSpinner",
    [dijit.form._Spinner, dijit.form.NumberTextBoxMixin],
    {
    // summary:
    //  Extends NumberTextBox to add up/down arrows and pageup/pagedown for incremental change to the value
    //
    // description:
    //      A `dijit.form.NumberTextBox` extension to provide keyboard accessible value selection
    //      as well as icons for spinning direction. When using the keyboard, the typematic rules
    //      apply, meaning holding the key will gradually increarease or decrease the value and
    //      accelerate.
    //
    // example:
    //  | new dijit.form.NumberSpinner({ constraints:{ max:300, min:100 }}, "someInput");

    required: true,

    adjust: function(/* Object */val, /* Number*/delta){
        // summary: change Number val by the given amount
        var tc = this.constraints,
            v = isNaN(val),
            gotMax = !isNaN(tc.max),
            gotMin = !isNaN(tc.min)
        ;
        if(v && delta != 0){ // blank or invalid value and they want to spin, so create defaults
            val = (delta > 0) ?
                gotMin ? tc.min : gotMax ? tc.max : 0 :
                gotMax ? this.constraints.max : gotMin ? tc.min : 0
            ;
        }
        var newval = val + delta;
        if(v || isNaN(newval)){ return val; }
        if(gotMax && (newval > tc.max)){
            newval = tc.max;
        }
        if(gotMin && (newval < tc.min)){
            newval = tc.min;
        }
        return newval;
    },

    _onKeyPress: function(e){
        if((e.charOrCode == dojo.keys.HOME || e.charOrCode == dojo.keys.END) && !e.ctrlKey && !e.altKey){
            var value = this.constraints[(e.charOrCode == dojo.keys.HOME ? "min" : "max")];
            if(value){
                this._setValueAttr(value,true);
            }
            // eat home or end key whether we change the value or not
            dojo.stopEvent(e);
            return false;
        }else{
            return this.inherited(arguments);
        }
    }

});

}

if(!dojo._hasResource["dijit._Container"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._Container"] = true;
dojo.provide("dijit._Container");

dojo.declare("dijit._Container",
    null,
    {
        // summary:
        //      Mixin for widgets that contain a set of widget children.
        // description:
        //      Use this mixin for widgets that needs to know about and
        //      keep track of their widget children. Suitable for widgets like BorderContainer
        //      and TabContainer which contain (only) a set of child widgets.
        //
        //      It's not suitable for widgets like ContentPane
        //      which contains mixed HTML (plain DOM nodes in addition to widgets),
        //      and where contained widgets are not necessarily directly below
        //      this.containerNode.   In that case calls like addChild(node, position)
        //      wouldn't make sense.

        // isContainer: Boolean
        //      Just a flag indicating that this widget descends from dijit._Container
        isContainer: true,

        buildRendering: function(){
            this.inherited(arguments);
            if(!this.containerNode){
                // all widgets with descendants must set containerNode
                this.containerNode = this.domNode;
            }
        },

        addChild: function(/*Widget*/ widget, /*int?*/ insertIndex){
            // summary:
            //      Makes the given widget a child of this widget.
            // description:
            //      Inserts specified child widget's dom node as a child of this widget's
            //      container node, and possibly does other processing (such as layout).

            var refNode = this.containerNode;
            if(insertIndex && typeof insertIndex == "number"){
                var children = this.getChildren();
                if(children && children.length >= insertIndex){
                    refNode = children[insertIndex-1].domNode;
                    insertIndex = "after";
                }
            }
            dojo.place(widget.domNode, refNode, insertIndex);

            // If I've been started but the child widget hasn't been started,
            // start it now.  Make sure to do this after widget has been
            // inserted into the DOM tree, so it can see that it's being controlled by me,
            // so it doesn't try to size itself.
            if(this._started && !widget._started){
                widget.startup();
            }
        },

        removeChild: function(/*Widget or int*/ widget){
            // summary:
            //      Removes the passed widget instance from this widget but does
            //      not destroy it.  You can also pass in an integer indicating
            //      the index within the container to remove
            if(typeof widget == "number" && widget > 0){
                widget = this.getChildren()[widget];
            }
            // If we cannot find the widget, just return
            if(!widget || !widget.domNode){ return; }

            var node = widget.domNode;
            node.parentNode.removeChild(node);  // detach but don't destroy
        },

        _nextElement: function(node){
            do{
                node = node.nextSibling;
            }while(node && node.nodeType != 1);
            return node;
        },

        _firstElement: function(node){
            node = node.firstChild;
            if(node && node.nodeType != 1){
                node = this._nextElement(node);
            }
            return node;
        },

        getChildren: function(){
            // summary:
            //      Returns array of children widgets.
            // description:
            //      Returns the widgets that are directly under this.containerNode.
            return dojo.query("> [widgetId]", this.containerNode).map(dijit.byNode); // Widget[]
        },

        hasChildren: function(){
            // summary:
            //      Returns true if widget has children, i.e. if this.containerNode contains something.
            return !!this._firstElement(this.containerNode); // Boolean
        },

        destroyDescendants: function(/*Boolean*/ preserveDom){
            dojo.forEach(this.getChildren(), function(child){ child.destroyRecursive(preserveDom); });
        },

        _getSiblingOfChild: function(/*Widget*/ child, /*int*/ dir){
            // summary:
            //      Get the next or previous widget sibling of child
            // dir:
            //      if 1, get the next sibling
            //      if -1, get the previous sibling
            var node = child.domNode;
            var which = (dir>0 ? "nextSibling" : "previousSibling");
            do{
                node = node[which];
            }while(node && (node.nodeType != 1 || !dijit.byNode(node)));
            return node ? dijit.byNode(node) : null;
        },

        getIndexOfChild: function(/*Widget*/ child){
            // summary:
            //      Gets the index of the child in this container or -1 if not found
            var children = this.getChildren();
            for(var i=0, c; c=children[i]; i++){
                if(c == child){
                    return i; // int
                }
            }
            return -1; // int
        }
    }
);

}

if(!dojo._hasResource["dijit._Contained"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._Contained"] = true;
dojo.provide("dijit._Contained");

dojo.declare("dijit._Contained",
        null,
        {
            // summary
            //      Mixin for widgets that are children of a container widget
            //
            // example:
            // |    // make a basic custom widget that knows about it's parents
            // |    dojo.declare("my.customClass",[dijit._Widget,dijit._Contained],{});
            //
            getParent: function(){
                // summary:
                //      Returns the parent widget of this widget, assuming the parent
                //      implements dijit._Container
                for(var p=this.domNode.parentNode; p; p=p.parentNode){
                    var id = p.getAttribute && p.getAttribute("widgetId");
                    if(id){
                        var parent = dijit.byId(id);
                        return parent.isContainer ? parent : null;
                    }
                }
                return null;
            },

            _getSibling: function(which){
                var node = this.domNode;
                do{
                    node = node[which+"Sibling"];
                }while(node && node.nodeType != 1);
                if(!node){ return null; } // null
                var id = node.getAttribute("widgetId");
                return dijit.byId(id);
            },

            getPreviousSibling: function(){
                // summary:
                //      Returns null if this is the first child of the parent,
                //      otherwise returns the next element sibling to the "left".

                return this._getSibling("previous"); // Mixed
            },

            getNextSibling: function(){
                // summary:
                //      Returns null if this is the last child of the parent,
                //      otherwise returns the next element sibling to the "right".

                return this._getSibling("next"); // Mixed
            },

            getIndexInParent: function(){
                // summary:
                //      Returns the index of this widget within its container parent.
                //      It returns -1 if the parent does not exist, or if the parent
                //      is not a dijit._Container

                var p = this.getParent();
                if(!p || !p.getIndexOfChild){
                    return -1; // int
                }
                return p.getIndexOfChild(this); // int
            }
        }
    );


}

if(!dojo._hasResource["dijit.layout._LayoutWidget"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.layout._LayoutWidget"] = true;
dojo.provide("dijit.layout._LayoutWidget");





dojo.declare("dijit.layout._LayoutWidget",
    [dijit._Widget, dijit._Container, dijit._Contained],
    {
        // summary
        //      Mixin for widgets that contain a list of children like SplitContainer.
        //      Widgets which mixin this code must define layout() to lay out the children

        // baseClass: String
        //      This class name is applied to the widget's domNode
        //      and also may be used to generate names for sub nodes,
        //      like for example dijitTabContainer-content.
        baseClass: "dijitLayoutContainer",

        isLayoutContainer: true,

        postCreate: function(){
            dojo.addClass(this.domNode, "dijitContainer");
            dojo.addClass(this.domNode, this.baseClass);
        },

        startup: function(){
            // summary:
            //      Called after all the widgets have been instantiated and their
            //      dom nodes have been inserted somewhere under dojo.doc.body.
            //
            //      Widgets should override this method to do any initialization
            //      dependent on other widgets existing, and then call
            //      this superclass method to finish things off.
            //
            //      startup() in subclasses shouldn't do anything
            //      size related because the size of the widget hasn't been set yet.

            if(this._started){ return; }

            dojo.forEach(this.getChildren(), function(child){ child.startup(); });

            // If I am a top level widget
            if(!this.getParent || !this.getParent()){
                // Do recursive sizing and layout of all my descendants
                // (passing in no argument to resize means that it has to glean the size itself)
                this.resize();

                // Since my parent isn't a layout container, and my style is width=height=100% (or something similar),
                // then I need to watch when the window resizes, and size myself accordingly.
                // (Passing in no arguments to resize means that it has to glean the size itself.)
                this.connect(dojo.global, 'onresize', dojo.hitch(this, 'resize'));
            }

            this.inherited(arguments);
        },

        resize: function(changeSize, resultSize){
            // summary:
            //      Call this to resize a widget, or after it's size has changed.
            // description:
            //      Change size mode:
            //          When changeSize is specified, changes the marginBox of this widget
            //           and forces it to relayout it's contents accordingly.
            //          changeSize may specify height, width, or both.
            //
            //          If resultSize is specified it indicates the size the widget will
            //          become after changeSize has been applied.
            //
            //      Notification mode:
            //          When changeSize is null, indicates that the caller has already changed
            //          the size of the widget, or perhaps it changed because the browser
            //          window was resized.  Tells widget to relayout it's contents accordingly.
            //
            //          If resultSize is also specified it indicates the size the widget has
            //          become.
            //
            //      In either mode, this method also:
            //          1. Sets this._borderBox and this._contentBox to the new size of
            //              the widget.  Queries the current domNode size if necessary.
            //          2. Calls layout() to resize contents (and maybe adjust child widgets).
            //
            // changeSize: Object?
            //      Sets the widget to this margin-box size and position.
            //      May include any/all of the following properties:
            //  |   {w: int, h: int, l: int, t: int}
            //
            // resultSize: Object?
            //      The margin-box size of this widget after applying changeSize (if
            //      changeSize is specified).  If caller knows this size and
            //      passes it in, we don't need to query the browser to get the size.
            //  |   {w: int, h: int}

            var node = this.domNode;

            // set margin box size, unless it wasn't specified, in which case use current size
            if(changeSize){
                dojo.marginBox(node, changeSize);

                // set offset of the node
                if(changeSize.t){ node.style.top = changeSize.t + "px"; }
                if(changeSize.l){ node.style.left = changeSize.l + "px"; }
            }

            // If either height or width wasn't specified by the user, then query node for it.
            // But note that setting the margin box and then immediately querying dimensions may return
            // inaccurate results, so try not to depend on it.
            var mb = resultSize || {};
            dojo.mixin(mb, changeSize || {});   // changeSize overrides resultSize
            if ( !("h" in mb) || !("w" in mb) ){
                mb = dojo.mixin(dojo.marginBox(node), mb);  // just use dojo.marginBox() to fill in missing values
            }

            // Compute and save the size of my border box and content box
            // (w/out calling dojo.contentBox() since that may fail if size was recently set)
            var cs = dojo.getComputedStyle(node);
            var me = dojo._getMarginExtents(node, cs);
            var be = dojo._getBorderExtents(node, cs);
            var bb = this._borderBox = {
                w: mb.w - (me.w + be.w),
                h: mb.h - (me.h + be.h)
            };
            var pe = dojo._getPadExtents(node, cs);
            this._contentBox = {
                l: dojo._toPixelValue(node, cs.paddingLeft),
                t: dojo._toPixelValue(node, cs.paddingTop),
                w: bb.w - pe.w,
                h: bb.h - pe.h
            };

            // Callback for widget to adjust size of it's children
            this.layout();
        },

        layout: function(){
            //  summary
            //      Widgets override this method to size & position their contents/children.
            //      When this is called this._contentBox is guaranteed to be set (see resize()).
            //
            //      This is called after startup(), and also when the widget's size has been
            //      changed.
        },

        _setupChild: function(/*Widget*/child){
            // summary: common setup for initial children or children which are added after startup
            dojo.addClass(child.domNode, this.baseClass+"-child");
            if(child.baseClass){
                dojo.addClass(child.domNode, this.baseClass+"-"+child.baseClass);
            }
        },

        addChild: function(/*Widget*/ child, /*Integer?*/ insertIndex){
            this.inherited(arguments);
            if(this._started){
                this._setupChild(child);
            }
        },

        removeChild: function(/*Widget*/ child){
            dojo.removeClass(child.domNode, this.baseClass+"-child");
            if(child.baseClass){
                dojo.removeClass(child.domNode, this.baseClass+"-"+child.baseClass);
            }
            this.inherited(arguments);
        }
    }
);

dijit.layout.marginBox2contentBox = function(/*DomNode*/ node, /*Object*/ mb){
    // summary:
    //      Given the margin-box size of a node, return it's content box size.
    //      Functions like dojo.contentBox() but is more reliable since it doesn't have
    //      to wait for the browser to compute sizes.
    var cs = dojo.getComputedStyle(node);
    var me = dojo._getMarginExtents(node, cs);
    var pb = dojo._getPadBorderExtents(node, cs);
    return {
        l: dojo._toPixelValue(node, cs.paddingLeft),
        t: dojo._toPixelValue(node, cs.paddingTop),
        w: mb.w - (me.w + pb.w),
        h: mb.h - (me.h + pb.h)
    };
};

(function(){
    var capitalize = function(word){
        return word.substring(0,1).toUpperCase() + word.substring(1);
    };

    var size = function(widget, dim){
        // size the child
        widget.resize ? widget.resize(dim) : dojo.marginBox(widget.domNode, dim);

        // record child's size, but favor our own numbers when we have them.
        // the browser lies sometimes
        dojo.mixin(widget, dojo.marginBox(widget.domNode));
        dojo.mixin(widget, dim);
    };

    dijit.layout.layoutChildren = function(/*DomNode*/ container, /*Object*/ dim, /*Object[]*/ children){
        /**
         * summary
         *      Layout a bunch of child dom nodes within a parent dom node
         * container:
         *      parent node
         * dim:
         *      {l, t, w, h} object specifying dimensions of container into which to place children
         * children:
         *      an array like [ {domNode: foo, layoutAlign: "bottom" }, {domNode: bar, layoutAlign: "client"} ]
         */

        // copy dim because we are going to modify it
        dim = dojo.mixin({}, dim);

        dojo.addClass(container, "dijitLayoutContainer");

        // Move "client" elements to the end of the array for layout.  a11y dictates that the author
        // needs to be able to put them in the document in tab-order, but this algorithm requires that
        // client be last.
        children = dojo.filter(children, function(item){ return item.layoutAlign != "client"; })
            .concat(dojo.filter(children, function(item){ return item.layoutAlign == "client"; }));

        // set positions/sizes
        dojo.forEach(children, function(child){
            var elm = child.domNode,
                pos = child.layoutAlign;

            // set elem to upper left corner of unused space; may move it later
            var elmStyle = elm.style;
            elmStyle.left = dim.l+"px";
            elmStyle.top = dim.t+"px";
            elmStyle.bottom = elmStyle.right = "auto";

            dojo.addClass(elm, "dijitAlign" + capitalize(pos));

            // set size && adjust record of remaining space.
            // note that setting the width of a <div> may affect it's height.
            if(pos == "top" || pos == "bottom"){
                size(child, { w: dim.w });
                dim.h -= child.h;
                if(pos=="top"){
                    dim.t += child.h;
                }else{
                    elmStyle.top = dim.t + dim.h + "px";
                }
            }else if(pos == "left" || pos == "right"){
                size(child, { h: dim.h });
                dim.w -= child.w;
                if(pos == "left"){
                    dim.l += child.w;
                }else{
                    elmStyle.left = dim.l + dim.w + "px";
                }
            }else if(pos == "client"){
                size(child, dim);
            }
        });
    };

})();

}

if(!dojo._hasResource["dojo.cookie"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.cookie"] = true;
dojo.provide("dojo.cookie");



/*=====
dojo.__cookieProps = function(){
    //  expires: Date|String|Number?
    //      If a number, the number of days from today at which the cookie
    //      will expire. If a date, the date past which the cookie will expire.
    //      If expires is in the past, the cookie will be deleted.
    //      If expires is omitted or is 0, the cookie will expire when the browser closes. << FIXME: 0 seems to disappear right away? FF3.
    //  path: String?
    //      The path to use for the cookie.
    //  domain: String?
    //      The domain to use for the cookie.
    //  secure: Boolean?
    //      Whether to only send the cookie on secure connections
    this.expires = expires;
    this.path = path;
    this.domain = domain;
    this.secure = secure;
}
=====*/


dojo.cookie = function(/*String*/name, /*String?*/value, /*dojo.__cookieProps?*/props){
    //  summary:
    //      Get or set a cookie.
    //  description:
    //      If one argument is passed, returns the value of the cookie
    //      For two or more arguments, acts as a setter.
    //  name:
    //      Name of the cookie
    //  value:
    //      Value for the cookie
    //  props:
    //      Properties for the cookie
    //  example:
    //      set a cookie with the JSON-serialized contents of an object which
    //      will expire 5 days from now:
    //  |   dojo.cookie("configObj", dojo.toJson(config), { expires: 5 });
    //
    //  example:
    //      de-serialize a cookie back into a JavaScript object:
    //  |   var config = dojo.fromJson(dojo.cookie("configObj"));
    //
    //  example:
    //      delete a cookie:
    //  |   dojo.cookie("configObj", null, {expires: -1});
    var c = document.cookie;
    if(arguments.length == 1){
        var matches = c.match(new RegExp("(?:^|; )" + dojo.regexp.escapeString(name) + "=([^;]*)"));
        return matches ? decodeURIComponent(matches[1]) : undefined; // String or undefined
    }else{
        props = props || {};
// FIXME: expires=0 seems to disappear right away, not on close? (FF3)  Change docs?
        var exp = props.expires;
        if(typeof exp == "number"){
            var d = new Date();
            d.setTime(d.getTime() + exp*24*60*60*1000);
            exp = props.expires = d;
        }
        if(exp && exp.toUTCString){ props.expires = exp.toUTCString(); }

        value = encodeURIComponent(value);
        var updatedCookie = name + "=" + value, propName;
        for(propName in props){
            updatedCookie += "; " + propName;
            var propValue = props[propName];
            if(propValue !== true){ updatedCookie += "=" + propValue; }
        }
        document.cookie = updatedCookie;
    }
};

dojo.cookie.isSupported = function(){
    //  summary:
    //      Use to determine if the current browser supports cookies or not.
    //
    //      Returns true if user allows cookies.
    //      Returns false if user doesn't allow cookies.

    if(!("cookieEnabled" in navigator)){
        this("__djCookieTest__", "CookiesAllowed");
        navigator.cookieEnabled = this("__djCookieTest__") == "CookiesAllowed";
        if(navigator.cookieEnabled){
            this("__djCookieTest__", "", {expires: -1});
        }
    }
    return navigator.cookieEnabled;
};

}

if(!dojo._hasResource["dijit.layout.BorderContainer"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.layout.BorderContainer"] = true;
dojo.provide("dijit.layout.BorderContainer");




dojo.declare(
    "dijit.layout.BorderContainer",
    dijit.layout._LayoutWidget,
{
    // summary:
    //  Provides layout in 5 regions, a center and borders along its 4 sides.
    //
    // description:
    //  A BorderContainer is a box with a specified size (like style="width: 500px; height: 500px;"),
    //  that contains a child widget marked region="center" and optionally children widgets marked
    //  region equal to "top", "bottom", "leading", "trailing", "left" or "right".
    //  Children along the edges will be laid out according to width or height dimensions.  The remaining
    //  space is designated for the center region.
    //  The outer size must be specified on the BorderContainer node.  Width must be specified for the sides
    //  and height for the top and bottom, respectively.  No dimensions should be specified on the center;
    //  it will fill the remaining space.  Regions named "leading" and "trailing" may be used just like
    //  "left" and "right" except that they will be reversed in right-to-left environments.
    //  Optional splitters may be specified on the edge widgets only to make them resizable by the user.
    //  NOTE: Splitters must not be more than 50 pixels in width.
    //
    // example:
    // |    <div dojoType="dijit.layout.BorderContainer" design="sidebar" style="width: 400px; height: 300px;">
    // |        <div dojoType="ContentPane" region="top">header text</div>
    // |        <div dojoType="ContentPane" region="right" style="width: 200px;">table of contents</div>
    // |        <div dojoType="ContentPane" region="center">client area</div>
    // |    </div>

    // design: String
    //  choose which design is used for the layout: "headline" (default) where the top and bottom extend
    //  the full width of the container, or "sidebar" where the left and right sides extend from top to bottom.
    design: "headline",

    // gutters: Boolean
    //  Give each pane a border and margin.
    //  Margin determined by domNode.paddingLeft.
    //  When false, only resizable panes have a gutter (i.e. draggable splitter) for resizing.
    gutters: true,

    // liveSplitters: Boolean
    //  specifies whether splitters resize as you drag (true) or only upon mouseup (false)
    liveSplitters: true,

    // persist: Boolean
    //      Save splitter positions in a cookie.
    persist: false, // Boolean

    baseClass: "dijitBorderContainer",

    // _splitterClass: String
    //      Optional hook to override the default Splitter widget used by BorderContainer
    _splitterClass: "dijit.layout._Splitter",

    postMixInProperties: function(){
        // change class name to indicate that BorderContainer is being used purely for
        // layout (like LayoutContainer) rather than for pretty formatting.
        if(!this.gutters){
            this.baseClass += "NoGutter";
        }
        this.inherited(arguments);
    },

    postCreate: function(){
        this.inherited(arguments);

        this._splitters = {};
        this._splitterThickness = {};
    },

    startup: function(){
        if(this._started){ return; }
        dojo.forEach(this.getChildren(), this._setupChild, this);
        this.inherited(arguments);
    },

    _setupChild: function(/*Widget*/child){
        var region = child.region;
        if(region){
            this.inherited(arguments);

            dojo.addClass(child.domNode, this.baseClass+"Pane");

            var ltr = this.isLeftToRight();
            if(region == "leading"){ region = ltr ? "left" : "right"; }
            if(region == "trailing"){ region = ltr ? "right" : "left"; }

            //FIXME: redundant?
            this["_"+region] = child.domNode;
            this["_"+region+"Widget"] = child;

            // Create draggable splitter for resizing pane,
            // or alternately if splitter=false but BorderContainer.gutters=true then
            // insert dummy div just for spacing
            if((child.splitter || this.gutters) && !this._splitters[region]){
                var _Splitter = dojo.getObject(child.splitter ? this._splitterClass : "dijit.layout._Gutter");
                var flip = {left:'right', right:'left', top:'bottom', bottom:'top', leading:'trailing', trailing:'leading'};
                var splitter = new _Splitter({
                    container: this,
                    child: child,
                    region: region,
//                  oppNode: dojo.query('[region=' + flip[child.region] + ']', this.domNode)[0],
                    oppNode: this["_" + flip[child.region]],
                    live: this.liveSplitters
                });
                splitter.isSplitter = true;
                this._splitters[region] = splitter.domNode;
                dojo.place(this._splitters[region], child.domNode, "after");

                // Splitters arent added as Contained children, so we need to call startup explicitly
                splitter.startup();
            }
            child.region = region;
        }
    },

    _computeSplitterThickness: function(region){
        this._splitterThickness[region] = this._splitterThickness[region] ||
            dojo.marginBox(this._splitters[region])[(/top|bottom/.test(region) ? 'h' : 'w')];
    },

    layout: function(){
        for(var region in this._splitters){ this._computeSplitterThickness(region); }
        this._layoutChildren();
    },

    addChild: function(/*Widget*/ child, /*Integer?*/ insertIndex){
        this.inherited(arguments);
        if(this._started){
            this._layoutChildren(); //OPT
        }
    },

    removeChild: function(/*Widget*/ child){
        var region = child.region;
        var splitter = this._splitters[region];
        if(splitter){
            dijit.byNode(splitter).destroy();
            delete this._splitters[region];
            delete this._splitterThickness[region];
        }
        this.inherited(arguments);
        delete this["_"+region];
        delete this["_" +region+"Widget"];
        if(this._started){
            this._layoutChildren(child.region);
        }
        dojo.removeClass(child.domNode, this.baseClass+"Pane");
    },

    getChildren: function(){
        return dojo.filter(this.inherited(arguments), function(widget){
            return !widget.isSplitter;
        });
    },

    getSplitter: function(/*String*/region){
        // summary: returns the widget responsible for rendering the splitter associated with region
        var splitter = this._splitters[region];
        return splitter ? dijit.byNode(splitter) : null;
    },

    resize: function(newSize, currentSize){
        // resetting potential padding to 0px to provide support for 100% width/height + padding
        // TODO: this hack doesn't respect the box model and is a temporary fix
        if (!this.cs || !this.pe){
            var node = this.domNode;
            this.cs = dojo.getComputedStyle(node);
            this.pe = dojo._getPadExtents(node, this.cs);
            this.pe.r = dojo._toPixelValue(node, this.cs.paddingRight);
            this.pe.b = dojo._toPixelValue(node, this.cs.paddingBottom);

            dojo.style(node, "padding", "0px");
        }

        this.inherited(arguments);
    },

    _layoutChildren: function(/*String?*/changedRegion){

        if(!this._borderBox.h){
            // We are currently hidden.  Abort.
            // Someone will resize us later.
            return;
        }

        var sidebarLayout = (this.design == "sidebar");
        var topHeight = 0, bottomHeight = 0, leftWidth = 0, rightWidth = 0;
        var topStyle = {}, leftStyle = {}, rightStyle = {}, bottomStyle = {},
            centerStyle = (this._center && this._center.style) || {};

        var changedSide = /left|right/.test(changedRegion);

        var layoutSides = !changedRegion || (!changedSide && !sidebarLayout);
        var layoutTopBottom = !changedRegion || (changedSide && sidebarLayout);

        // Ask browser for width/height of side panes.
        // Would be nice to cache this but height can change according to width
        // (because words wrap around).  I don't think width will ever change though
        // (except when the user drags a splitter).
        if(this._top){
            topStyle = layoutTopBottom && this._top.style;
            topHeight = dojo.marginBox(this._top).h;
        }
        if(this._left){
            leftStyle = layoutSides && this._left.style;
            leftWidth = dojo.marginBox(this._left).w;
        }
        if(this._right){
            rightStyle = layoutSides && this._right.style;
            rightWidth = dojo.marginBox(this._right).w;
        }
        if(this._bottom){
            bottomStyle = layoutTopBottom && this._bottom.style;
            bottomHeight = dojo.marginBox(this._bottom).h;
        }

        var splitters = this._splitters;
        var topSplitter = splitters.top, bottomSplitter = splitters.bottom,
            leftSplitter = splitters.left, rightSplitter = splitters.right;
        var splitterThickness = this._splitterThickness;
        var topSplitterThickness = splitterThickness.top || 0,
            leftSplitterThickness = splitterThickness.left || 0,
            rightSplitterThickness = splitterThickness.right || 0,
            bottomSplitterThickness = splitterThickness.bottom || 0;

        // Check for race condition where CSS hasn't finished loading, so
        // the splitter width == the viewport width (#5824)
        if(leftSplitterThickness > 50 || rightSplitterThickness > 50){
            setTimeout(dojo.hitch(this, function(){
                // Results are invalid.  Clear them out.
                this._splitterThickness = {};

                for(var region in this._splitters){
                    this._computeSplitterThickness(region);
                }
                this._layoutChildren();
            }), 50);
            return false;
        }

        var pe = this.pe;

        var splitterBounds = {
            left: (sidebarLayout ? leftWidth + leftSplitterThickness: 0) + pe.l + "px",
            right: (sidebarLayout ? rightWidth + rightSplitterThickness: 0) + pe.r + "px"
        };

        if(topSplitter){
            dojo.mixin(topSplitter.style, splitterBounds);
            topSplitter.style.top = topHeight + pe.t + "px";
        }

        if(bottomSplitter){
            dojo.mixin(bottomSplitter.style, splitterBounds);
            bottomSplitter.style.bottom = bottomHeight + pe.b + "px";
        }

        splitterBounds = {
            top: (sidebarLayout ? 0 : topHeight + topSplitterThickness) + pe.t + "px",
            bottom: (sidebarLayout ? 0 : bottomHeight + bottomSplitterThickness) + pe.b + "px"
        };

        if(leftSplitter){
            dojo.mixin(leftSplitter.style, splitterBounds);
            leftSplitter.style.left = leftWidth + pe.l + "px";
        }

        if(rightSplitter){
            dojo.mixin(rightSplitter.style, splitterBounds);
            rightSplitter.style.right = rightWidth + pe.r +  "px";
        }

        dojo.mixin(centerStyle, {
            top: pe.t + topHeight + topSplitterThickness + "px",
            left: pe.l + leftWidth + leftSplitterThickness + "px",
            right: pe.r + rightWidth + rightSplitterThickness + "px",
            bottom: pe.b + bottomHeight + bottomSplitterThickness + "px"
        });

        var bounds = {
            top: sidebarLayout ? pe.t + "px" : centerStyle.top,
            bottom: sidebarLayout ? pe.b + "px" : centerStyle.bottom
        };
        dojo.mixin(leftStyle, bounds);
        dojo.mixin(rightStyle, bounds);
        leftStyle.left = pe.l + "px"; rightStyle.right = pe.r + "px"; topStyle.top = pe.t + "px"; bottomStyle.bottom = pe.b + "px";
        if(sidebarLayout){
            topStyle.left = bottomStyle.left = leftWidth + leftSplitterThickness + pe.l + "px";
            topStyle.right = bottomStyle.right = rightWidth + rightSplitterThickness + pe.r + "px";
        }else{
            topStyle.left = bottomStyle.left = pe.l + "px";
            topStyle.right = bottomStyle.right = pe.r + "px";
        }

        // More calculations about sizes of panes
        var containerHeight = this._borderBox.h - pe.t - pe.b,
            middleHeight = containerHeight - ( topHeight + topSplitterThickness + bottomHeight + bottomSplitterThickness),
            sidebarHeight = sidebarLayout ? containerHeight : middleHeight;

        var containerWidth = this._borderBox.w - pe.l - pe.r,
            middleWidth = containerWidth - (leftWidth  + leftSplitterThickness + rightWidth + rightSplitterThickness),
            sidebarWidth = sidebarLayout ? middleWidth : containerWidth;

        // New margin-box size of each pane
        var dim = {
            top:    { w: sidebarWidth, h: topHeight },
            bottom: { w: sidebarWidth, h: bottomHeight },
            left:   { w: leftWidth, h: sidebarHeight },
            right:  { w: rightWidth, h: sidebarHeight },
            center: { h: middleHeight, w: middleWidth }
        };

        // Nodes in IE<8 don't respond to t/l/b/r, and TEXTAREA doesn't respond in any browser
        var janky = dojo.isIE < 8 || (dojo.isIE && dojo.isQuirks) || dojo.some(this.getChildren(), function(child){
            return child.domNode.tagName == "TEXTAREA" || child.domNode.tagName == "INPUT";
        });
        if(janky){
            // Set the size of the children the old fashioned way, by setting
            // CSS width and height

            var resizeWidget = function(widget, changes, result){
                if(widget){
                    (widget.resize ? widget.resize(changes, result) : dojo.marginBox(widget.domNode, changes));
                }
            };

            if(leftSplitter){ leftSplitter.style.height = sidebarHeight; }
            if(rightSplitter){ rightSplitter.style.height = sidebarHeight; }
            resizeWidget(this._leftWidget, {h: sidebarHeight}, dim.left);
            resizeWidget(this._rightWidget, {h: sidebarHeight}, dim.right);

            if(topSplitter){ topSplitter.style.width = sidebarWidth; }
            if(bottomSplitter){ bottomSplitter.style.width = sidebarWidth; }
            resizeWidget(this._topWidget, {w: sidebarWidth}, dim.top);
            resizeWidget(this._bottomWidget, {w: sidebarWidth}, dim.bottom);

            resizeWidget(this._centerWidget, dim.center);
        }else{
            // We've already sized the children by setting style.top/bottom/left/right...
            // Now just need to call resize() on those children telling them their new size,
            // so they can re-layout themselves

            // Calculate which panes need a notification
            var resizeList = {};
            if(changedRegion){
                resizeList[changedRegion] = resizeList.center = true;
                if(/top|bottom/.test(changedRegion) && this.design != "sidebar"){
                    resizeList.left = resizeList.right = true;
                }else if(/left|right/.test(changedRegion) && this.design == "sidebar"){
                    resizeList.top = resizeList.bottom = true;
                }
            }

            dojo.forEach(this.getChildren(), function(child){
                if(child.resize && (!changedRegion || child.region in resizeList)){
                    child.resize(null, dim[child.region]);
                }
            }, this);
        }
    },

    destroy: function(){
        for(var region in this._splitters){
            var splitter = this._splitters[region];
            dijit.byNode(splitter).destroy();
            dojo.destroy(splitter);
        }
        delete this._splitters;
        delete this._splitterThickness;
        this.inherited(arguments);
    }
});

// This argument can be specified for the children of a BorderContainer.
// Since any widget can be specified as a LayoutContainer child, mix it
// into the base widget class.  (This is a hack, but it's effective.)
dojo.extend(dijit._Widget, {
    // region: String
    //      "top", "bottom", "leading", "trailing", "left", "right", "center".
    //      See the BorderContainer description for details on this parameter.
    region: '',

    // splitter: Boolean
    splitter: false,

    // minSize: Number
    minSize: 0,

    // maxSize: Number
    maxSize: Infinity
});



dojo.declare("dijit.layout._Splitter", [ dijit._Widget, dijit._Templated ],
{
/*=====
    container: null,
    child: null,
    region: null,
=====*/

    // live: Boolean
    //      If true, the child's size changes and the child widget is redrawn as you drag the splitter;
    //      otherwise, the size doesn't change until you drop the splitter (by mouse-up)
    live: true,

    // summary: A draggable spacer between two items in a BorderContainer
    templateString: '<div class="dijitSplitter" dojoAttachEvent="onkeypress:_onKeyPress,onmousedown:_startDrag" tabIndex="0" waiRole="separator"><div class="dijitSplitterThumb"></div></div>',

    postCreate: function(){
        this.inherited(arguments);
        this.horizontal = /top|bottom/.test(this.region);
        dojo.addClass(this.domNode, "dijitSplitter" + (this.horizontal ? "H" : "V"));
//      dojo.addClass(this.child.domNode, "dijitSplitterPane");
//      dojo.setSelectable(this.domNode, false); //TODO is this necessary?

        this._factor = /top|left/.test(this.region) ? 1 : -1;
        this._minSize = this.child.minSize;

        // trigger constraints calculations
        this.child.domNode._recalc = true;
        this.connect(this.container, "resize", function(){ this.child.domNode._recalc = true; });

        this._cookieName = this.container.id + "_" + this.region;
        if(this.container.persist){
            // restore old size
            var persistSize = dojo.cookie(this._cookieName);
            if(persistSize){
                this.child.domNode.style[this.horizontal ? "height" : "width"] = persistSize;
            }
        }
    },

    _computeMaxSize: function(){
        var dim = this.horizontal ? 'h' : 'w',
            thickness = this.container._splitterThickness[this.region];
        var available = dojo.contentBox(this.container.domNode)[dim] -
            (this.oppNode ? dojo.marginBox(this.oppNode)[dim] : 0) -
            20 - thickness * 2;
        this._maxSize = Math.min(this.child.maxSize, available);
    },

    _startDrag: function(e){
        if(this.child.domNode._recalc){
            this._computeMaxSize();
            this.child.domNode._recalc = false;
        }

        if(!this.cover){
            this.cover = dojo.doc.createElement('div');
            dojo.addClass(this.cover, "dijitSplitterCover");
            dojo.place(this.cover, this.child.domNode, "after");
        }
        dojo.addClass(this.cover, "dijitSplitterCoverActive");

        // Safeguard in case the stop event was missed.  Shouldn't be necessary if we always get the mouse up.
        if(this.fake){ dojo.destroy(this.fake); }
        if(!(this._resize = this.live)){ //TODO: disable live for IE6?
            // create fake splitter to display at old position while we drag
            (this.fake = this.domNode.cloneNode(true)).removeAttribute("id");
            dojo.addClass(this.domNode, "dijitSplitterShadow");
            dojo.place(this.fake, this.domNode, "after");
        }
        dojo.addClass(this.domNode, "dijitSplitterActive");

        //Performance: load data info local vars for onmousevent function closure
        var factor = this._factor,
            max = this._maxSize,
            min = this._minSize || 20,
            isHorizontal = this.horizontal,
            axis = isHorizontal ? "pageY" : "pageX",
            pageStart = e[axis],
            splitterStyle = this.domNode.style,
            dim = isHorizontal ? 'h' : 'w',
            childStart = dojo.marginBox(this.child.domNode)[dim],
            region = this.region,
            splitterStart = parseInt(this.domNode.style[region], 10),
            resize = this._resize,
            mb = {},
            childNode = this.child.domNode,
            layoutFunc = dojo.hitch(this.container, this.container._layoutChildren),
            de = dojo.doc.body;

        this._handlers = (this._handlers || []).concat([
            dojo.connect(de, "onmousemove", this._drag = function(e, forceResize){
                var delta = e[axis] - pageStart,
                    childSize = factor * delta + childStart,
                    boundChildSize = Math.max(Math.min(childSize, max), min);

                if(resize || forceResize){
                    mb[dim] = boundChildSize;
                    // TODO: inefficient; we set the marginBox here and then immediately layoutFunc() needs to query it
                    dojo.marginBox(childNode, mb);
                    layoutFunc(region);
                }
                splitterStyle[region] = factor * delta + splitterStart + (boundChildSize - childSize) + "px";
            }),
            dojo.connect(dojo.doc, "ondragstart",   dojo.stopEvent),
            dojo.connect(dojo.body(), "onselectstart", dojo.stopEvent),
            dojo.connect(de, "onmouseup", this, "_stopDrag")
        ]);
        dojo.stopEvent(e);
    },

    _stopDrag: function(e){
        try{
            if(this.cover){
                dojo.removeClass(this.cover, "dijitSplitterCoverActive");
            }
            if(this.fake){ dojo.destroy(this.fake); }
            dojo.removeClass(this.domNode, "dijitSplitterActive");
            dojo.removeClass(this.domNode, "dijitSplitterShadow");
            this._drag(e); //TODO: redundant with onmousemove?
            this._drag(e, true);
        }finally{
            this._cleanupHandlers();
            if(this.oppNode){ this.oppNode._recalc = true; }
            delete this._drag;
        }

        if(this.container.persist){
            dojo.cookie(this._cookieName, this.child.domNode.style[this.horizontal ? "height" : "width"], {expires:365});
        }
    },

    _cleanupHandlers: function(){
        dojo.forEach(this._handlers, dojo.disconnect);
        delete this._handlers;
    },

    _onKeyPress: function(/*Event*/ e){
        if(this.child.domNode._recalc){
            this._computeMaxSize();
            this.child.domNode._recalc = false;
        }

        // should we apply typematic to this?
        this._resize = true;
        var horizontal = this.horizontal;
        var tick = 1;
        var dk = dojo.keys;
        switch(e.charOrCode){
            case horizontal ? dk.UP_ARROW : dk.LEFT_ARROW:
                tick *= -1;
//              break;
            case horizontal ? dk.DOWN_ARROW : dk.RIGHT_ARROW:
                break;
            default:
//              this.inherited(arguments);
                return;
        }
        var childSize = dojo.marginBox(this.child.domNode)[ horizontal ? 'h' : 'w' ] + this._factor * tick;
        var mb = {};
        mb[ this.horizontal ? "h" : "w"] = Math.max(Math.min(childSize, this._maxSize), this._minSize);
        dojo.marginBox(this.child.domNode, mb);
        if(this.oppNode){ this.oppNode._recalc = true; }
        this.container._layoutChildren(this.region);
        dojo.stopEvent(e);
    },

    destroy: function(){
        this._cleanupHandlers();
        delete this.child;
        delete this.container;
        delete this.cover;
        delete this.fake;
        this.inherited(arguments);
    }
});

dojo.declare("dijit.layout._Gutter", [dijit._Widget, dijit._Templated ],
{
    // summary:
    //      Just a spacer div to separate side pane from center pane.
    //      Basically a trick to lookup the gutter/splitter width from the theme.

    templateString: '<div class="dijitGutter" waiRole="presentation"></div>',

    postCreate: function(){
        this.horizontal = /top|bottom/.test(this.region);
        dojo.addClass(this.domNode, "dijitGutter" + (this.horizontal ? "H" : "V"));
    }
});

}

if(!dojo._hasResource["dojo.html"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.html"] = true;
dojo.provide("dojo.html");

// the parser might be needed..


(function(){ // private scope, sort of a namespace

    // idCounter is incremented with each instantiation to allow asignment of a unique id for tracking, logging purposes
    var idCounter = 0;

    dojo.html._secureForInnerHtml = function(/*String*/ cont){
        // summary:
        //      removes !DOCTYPE and title elements from the html string.
        //
        //      khtml is picky about dom faults, you can't attach a style or <title> node as child of body
        //      must go into head, so we need to cut out those tags
        //  cont:
        //      An html string for insertion into the dom
        //
        return cont.replace(/(?:\s*<!DOCTYPE\s[^>]+>|<title[^>]*>[\s\S]*?<\/title>)/ig, ""); // String
    };

/*====
    dojo.html._emptyNode = function(node){
        // summary:
        //      removes all child nodes from the given node
        //  node: DOMNode
        //      the parent element
    };
=====*/
    dojo.html._emptyNode = dojo.empty;

    dojo.html._setNodeContent = function(/* DomNode */ node, /* String|DomNode|NodeList */ cont, /* Boolean? */ shouldEmptyFirst){
        // summary:
        //      inserts the given content into the given node
        //      overlaps similiar functionality in dijit.layout.ContentPane._setContent
        //  node:
        //      the parent element
        //  content:
        //      the content to be set on the parent element.
        //      This can be an html string, a node reference or a NodeList, dojo.NodeList, Array or other enumerable list of nodes
        // shouldEmptyFirst
        //      if shouldEmptyFirst is true, the node will first be emptied of all content before the new content is inserted
        //      defaults to false
        if(shouldEmptyFirst){
            dojo.html._emptyNode(node);
        }

        if(typeof cont == "string"){
            // there's some hoops to jump through before we can set innerHTML on the would-be parent element.

            // rationale for this block:
            // if node is a table derivate tag, some browsers dont allow innerHTML on those
            // TODO: <select>, <dl>? what other elements will give surprises if you naively set innerHTML?

            var pre = '', post = '', walk = 0, name = node.nodeName.toLowerCase();
            switch(name){
                case 'tr':
                    pre = '<tr>'; post = '</tr>';
                    walk += 1;//fallthrough
                case 'tbody': case 'thead':// children of THEAD is of same type as TBODY
                    pre = '<tbody>' + pre; post += '</tbody>';
                    walk += 1;// falltrough
                case 'table':
                    pre = '<table>' + pre; post += '</table>';
                    walk += 1;
                    break;
            }
            if(walk){
                var n = node.ownerDocument.createElement('div');
                n.innerHTML = pre + cont + post;
                do{
                    n = n.firstChild;
                }while(--walk);
                // now we can safely add the child nodes...
                dojo.forEach(n.childNodes, function(n){
                    node.appendChild(n.cloneNode(true));
                });
            }else{
                // innerHTML the content as-is into the node (element)
                // should we ever support setting content on non-element node types?
                // e.g. text nodes, comments, etc.?
                node.innerHTML = cont;
            }

        }else{
            // DomNode or NodeList
            if(cont.nodeType){ // domNode (htmlNode 1 or textNode 3)
                node.appendChild(cont);
            }else{// nodelist or array such as dojo.Nodelist
                dojo.forEach(cont, function(n){
                    node.appendChild(n.cloneNode(true));
                });
            }
        }
        // return DomNode
        return node;
    };

    // we wrap up the content-setting operation in a object
    dojo.declare("dojo.html._ContentSetter", null,
        {
            // node: DomNode|String
            //      An node which will be the parent element that we set content into
            node: "",

            // content: String|DomNode|DomNode[]
            //      The content to be placed in the node. Can be an HTML string, a node reference, or a enumerable list of nodes
            content: "",

            // id: String?
            //      Usually only used internally, and auto-generated with each instance
            id: "",

            // cleanContent: Boolean
            //      Should the content be treated as a full html document,
            //      and the real content stripped of <html>, <body> wrapper before injection
            cleanContent: false,

            // extractContent: Boolean
            //      Should the content be treated as a full html document, and the real content stripped of <html>, <body> wrapper before injection
            extractContent: false,

            // parseContent: Boolean
            //      Should the node by passed to the parser after the new content is set
            parseContent: false,

            // lifecyle methods
            constructor: function(/* Object */params, /* String|DomNode */node){
                //  summary:
                //      Provides a configurable, extensible object to wrap the setting on content on a node
                //      call the set() method to actually set the content..

                // the original params are mixed directly into the instance "this"
                dojo.mixin(this, params || {});

                // give precedence to params.node vs. the node argument
                // and ensure its a node, not an id string
                node = this.node = dojo.byId( this.node || node );

                if(!this.id){
                    this.id = [
                        "Setter",
                        (node) ? node.id || node.tagName : "",
                        idCounter++
                    ].join("_");
                }

                if(! (this.node || node)){
                    new Error(this.declaredClass + ": no node provided to " + this.id);
                }
            },
            set: function(/* String|DomNode|NodeList? */ cont, /* Object? */ params){
                // summary:
                //      front-end to the set-content sequence
                //  cont:
                //      An html string, node or enumerable list of nodes for insertion into the dom
                //      If not provided, the object's content property will be used
                if(undefined !== cont){
                    this.content = cont;
                }
                // in the re-use scenario, set needs to be able to mixin new configuration
                if(params){
                    this._mixin(params);
                }

                this.onBegin();
                this.setContent();
                this.onEnd();

                return this.node;
            },
            setContent: function(){
                // summary:
                //      sets the content on the node

                var node = this.node;
                if(!node) {
                    console.error("setContent given no node");
                }
                try{
                    node = dojo.html._setNodeContent(node, this.content);
                }catch(e){
                    // check if a domfault occurs when we are appending this.errorMessage
                    // like for instance if domNode is a UL and we try append a DIV

                    // FIXME: need to allow the user to provide a content error message string
                    var errMess = this.onContentError(e);
                    try{
                        node.innerHTML = errMess;
                    }catch(e){
                        console.error('Fatal ' + this.declaredClass + '.setContent could not change content due to '+e.message, e);
                    }
                }
                // always put back the node for the next method
                this.node = node; // DomNode
            },

            empty: function() {
                // summary
                //  cleanly empty out existing content

                // destroy any widgets from a previous run
                // NOTE: if you dont want this you'll need to empty
                // the parseResults array property yourself to avoid bad things happenning
                if(this.parseResults && this.parseResults.length) {
                    dojo.forEach(this.parseResults, function(w) {
                        if(w.destroy){
                            w.destroy();
                        }
                    });
                    delete this.parseResults;
                }
                // this is fast, but if you know its already empty or safe, you could
                // override empty to skip this step
                dojo.html._emptyNode(this.node);
            },

            onBegin: function(){
                // summary
                //      Called after instantiation, but before set();
                //      It allows modification of any of the object properties
                //      - including the node and content provided - before the set operation actually takes place
                //      This default implementation checks for cleanContent and extractContent flags to
                //      optionally pre-process html string content
                var cont = this.content;

                if(dojo.isString(cont)){
                    if(this.cleanContent){
                        cont = dojo.html._secureForInnerHtml(cont);
                    }

                    if(this.extractContent){
                        var match = cont.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
                        if(match){ cont = match[1]; }
                    }
                }

                // clean out the node and any cruft associated with it - like widgets
                this.empty();

                this.content = cont;
                return this.node; /* DomNode */
            },

            onEnd: function(){
                // summary
                //      Called after set(), when the new content has been pushed into the node
                //      It provides an opportunity for post-processing before handing back the node to the caller
                //      This default implementation checks a parseContent flag to optionally run the dojo parser over the new content
                if(this.parseContent){
                    // populates this.parseResults if you need those..
                    this._parse();
                }
                return this.node; /* DomNode */
            },

            tearDown: function(){
                // summary
                //      manually reset the Setter instance if its being re-used for example for another set()
                // description
                //      tearDown() is not called automatically.
                //      In normal use, the Setter instance properties are simply allowed to fall out of scope
                //      but the tearDown method can be called to explicitly reset this instance.
                delete this.parseResults;
                delete this.node;
                delete this.content;
            },

            onContentError: function(err){
                return "Error occured setting content: " + err;
            },

            _mixin: function(params){
                // mix properties/methods into the instance
                // TODO: the intention with tearDown is to put the Setter's state
                // back to that of the original constructor (vs. deleting/resetting everything regardless of ctor params)
                // so we could do something here to move the original properties aside for later restoration
                var empty = {}, key;
                for(key in params){
                    if(key in empty){ continue; }
                    // TODO: here's our opportunity to mask the properties we dont consider configurable/overridable
                    // .. but history shows we'll almost always guess wrong
                    this[key] = params[key];
                }
            },
            _parse: function(){
                // summary:
                //      runs the dojo parser over the node contents, storing any results in this.parseResults
                //      Any errors resulting from parsing are passed to _onError for handling

                var rootNode = this.node;
                try{
                    // store the results (widgets, whatever) for potential retrieval
                    this.parseResults = dojo.parser.parse(rootNode, true);
                }catch(e){
                    this._onError('Content', e, "Error parsing in _ContentSetter#"+this.id);
                }
            },

            _onError: function(type, err, consoleText){
                // summary:
                //      shows user the string that is returned by on[type]Error
                //      overide/implement on[type]Error and return your own string to customize
                var errText = this['on' + type + 'Error'].call(this, err);
                if(consoleText){
                    console.error(consoleText, err);
                }else if(errText){ // a empty string won't change current content
                    dojo.html._setNodeContent(this.node, errText, true);
                }
            }
    }); // end dojo.declare()

    dojo.html.set = function(/* DomNode */ node, /* String|DomNode|NodeList */ cont, /* Object? */ params){
            // summary:
            //      inserts (replaces) the given content into the given node
            //  node:
            //      the parent element that will receive the content
            //  cont:
            //      the content to be set on the parent element.
            //      This can be an html string, a node reference or a NodeList, dojo.NodeList, Array or other enumerable list of nodes
            //  params:
            //      Optional flags/properties to configure the content-setting. See dojo.html._ContentSetter
            //  example:
            //      A safe string/node/nodelist content replacement/injection with hooks for extension
            //      Example Usage:
            //      dojo.html.set(node, "some string");
            //      dojo.html.set(node, contentNode, {options});
            //      dojo.html.set(node, myNode.childNodes, {options});
        if(undefined == cont){
            console.warn("dojo.html.set: no cont argument provided, using empty string");
            cont = "";
        }
        if(!params){
            // simple and fast
            return dojo.html._setNodeContent(node, cont, true);
        }else{
            // more options but slower
            // note the arguments are reversed in order, to match the convention for instantiation via the parser
            var op = new dojo.html._ContentSetter(dojo.mixin(
                    params,
                    { content: cont, node: node }
            ));
            return op.set();
        }
    };
})();

}

if(!dojo._hasResource["dijit.layout.ContentPane"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.layout.ContentPane"] = true;
dojo.provide("dijit.layout.ContentPane");










dojo.declare(
    "dijit.layout.ContentPane",
    [dijit._Widget, dijit._Container, dijit._Contained],
{
    // summary:
    //      A widget that acts as a container for mixed HTML and widgets, and includes a ajax interface
    // description:
    //      A widget that can be used as a standalone widget
    //      or as a baseclass for other widgets
    //      Handles replacement of document fragment using either external uri or javascript
    //      generated markup or DOM content, instantiating widgets within that content.
    //      Don't confuse it with an iframe, it only needs/wants document fragments.
    //      It's useful as a child of LayoutContainer, SplitContainer, or TabContainer.
    //      But note that those classes can contain any widget as a child.
    // example:
    //      Some quick samples:
    //      To change the innerHTML use .attr('content', '<b>new content</b>')
    //
    //      Or you can send it a NodeList, .attr('content', dojo.query('div [class=selected]', userSelection))
    //      please note that the nodes in NodeList will copied, not moved
    //
    //      To do a ajax update use .attr('href', url)

    // href: String
    //      The href of the content that displays now.
    //      Set this at construction if you want to load data externally when the
    //      pane is shown.  (Set preload=true to load it immediately.)
    //      Changing href after creation doesn't have any effect; use attr('href', ...);
    href: "",

/*=====
    // content: String
    //      The innerHTML of the ContentPane.
    //      Note that the initialization parameter / argument to attr("content", ...)
    //      can be a String, DomNode, Nodelist, or widget.
    content: "",
=====*/

    // extractContent: Boolean
    //  Extract visible content from inside of <body> .... </body>
    extractContent: false,

    // parseOnLoad: Boolean
    //  parse content and create the widgets, if any
    parseOnLoad:    true,

    // preventCache: Boolean
    //      Cache content retreived externally
    preventCache:   false,

    // preload: Boolean
    //  Force load of data even if pane is hidden.
    preload: false,

    // refreshOnShow: Boolean
    //      Refresh (re-download) content when pane goes from hidden to shown
    refreshOnShow: false,

    // loadingMessage: String
    //  Message that shows while downloading
    loadingMessage: "<span class='dijitContentPaneLoading'>${loadingState}</span>",

    // errorMessage: String
    //  Message that shows if an error occurs
    errorMessage: "<span class='dijitContentPaneError'>${errorState}</span>",

    // isLoaded: Boolean
    //      True if the ContentPane has data in it, either specified
    //      during initialization (via href or inline content), or set
    //      via attr('content', ...) / attr('href', ...)
    //
    //      False if it doesn't have any content, or if ContentPane is
    //      still in the process of downloading href.
    isLoaded: false,

    baseClass: "dijitContentPane",

    // doLayout: Boolean
    //      - false - don't adjust size of children
    //      - true - if there is a single visible child widget, set it's size to
    //              however big the ContentPane is
    doLayout: true,

    // ioArgs: Object
    //      Parameters to pass to xhrGet() request, for example:
    // |    <div dojoType="dijit.layout.ContentPane" href="./bar" ioArgs="{timeout: 500}">
    ioArgs: {},

    postMixInProperties: function(){
        this.inherited(arguments);
        var messages = dojo.i18n.getLocalization("dijit", "loading", this.lang);
        this.loadingMessage = dojo.string.substitute(this.loadingMessage, messages);
        this.errorMessage = dojo.string.substitute(this.errorMessage, messages);

        // Detect if we were initialized with data
        if(!this.href && this.srcNodeRef && this.srcNodeRef.innerHTML){
            this.isLoaded = true;
        }
    },

    buildRendering: function(){
        this.inherited(arguments);
        if(!this.containerNode){
            // make getDescendants() work
            this.containerNode = this.domNode;
        }
    },

    postCreate: function(){
        // remove the title attribute so it doesn't show up when i hover
        // over a node
        this.domNode.title = "";

        if (!dijit.hasWaiRole(this.domNode)){
            dijit.setWaiRole(this.domNode, "group");
        }

        dojo.addClass(this.domNode, this.baseClass);
    },

    startup: function(){
        if(this._started){ return; }

        if(this.isLoaded){
            dojo.forEach(this.getChildren(), function(child){
                child.startup();
            });

            // If we have static content in the content pane (specified during
            // initialization) then we need to do layout now... unless we are
            // a child of a TabContainer etc. in which case wait until the TabContainer
            // calls resize() on us.
            if(this.doLayout){
                this._checkIfSingleChild();
            }
            if(!this._singleChild || !this.getParent()){
                this._scheduleLayout();
            }
        }

        // If we have an href then check if we should load it now
        this._loadCheck();

        this.inherited(arguments);
    },

    _checkIfSingleChild: function(){
        // summary:
        //      Test if we have exactly one visible widget as a child,
        //      and if so assume that we are a container for that widget,
        //      and should propogate startup() and resize() calls to it.
        //      Skips over things like data stores since they aren't visible.

        var childNodes = dojo.query(">", this.containerNode),
            childWidgetNodes = childNodes.filter(function(node){
                return dojo.hasAttr(node, "dojoType") || dojo.hasAttr(node, "widgetId");
            }),
            candidateWidgets = dojo.filter(childWidgetNodes.map(dijit.byNode), function(widget){
                return widget && widget.domNode && widget.resize;
            });

        if(
            // all child nodes are widgets
            childNodes.length == childWidgetNodes.length &&

            // all but one are invisible (like dojo.data)
            candidateWidgets.length == 1
        ){
            this._singleChild = candidateWidgets[0];
        }else{
            delete this._singleChild;
        }
    },

    setHref: function(/*String|Uri*/ href){
        dojo.deprecated("dijit.layout.ContentPane.setHref() is deprecated.  Use attr('href', ...) instead.", "", "2.0");
        return this.attr("href", href);
    },
    _setHrefAttr: function(/*String|Uri*/ href){
        // summary:
        //      Hook so attr("href", ...) works.
        // description:
        //      Reset the (external defined) content of this pane and replace with new url
        //      Note: It delays the download until widget is shown if preload is false.
        //  href:
        //      url to the page you want to get, must be within the same domain as your mainpage

        // Cancel any in-flight requests (an attr('href') will cancel any in-flight attr('href', ...))
        this.cancel();

        this.href = href;

        // _setHrefAttr() is called during creation and by the user, after creation.
        // only in the second case do we actually load the URL; otherwise it's done in startup()
        if(this._created && (this.preload || this._isShown())){
            // we return result of refresh() here to avoid code dup. in dojox.layout.ContentPane
            return this.refresh();
        }else{
            // Set flag to indicate that href needs to be loaded the next time the
            // ContentPane is made visible
            this._hrefChanged = true;
        }
    },

    setContent: function(/*String|DomNode|Nodelist*/data){
        dojo.deprecated("dijit.layout.ContentPane.setContent() is deprecated.  Use attr('content', ...) instead.", "", "2.0");
        this.attr("content", data);
    },
    _setContentAttr: function(/*String|DomNode|Nodelist*/data){
        // summary:
        //      Hook to make attr("content", ...) work.
        //      Replaces old content with data content, include style classes from old content
        //  data:
        //      the new Content may be String, DomNode or NodeList
        //
        //      if data is a NodeList (or an array of nodes) nodes are copied
        //      so you can import nodes from another document implicitly

        // clear href so we can't run refresh and clear content
        // refresh should only work if we downloaded the content
        this.href = "";

        // Cancel any in-flight requests (an attr('content') will cancel any in-flight attr('href', ...))
        this.cancel();

        this._setContent(data || "");

        this._isDownloaded = false; // mark that content is from a attr('content') not an attr('href')
    },
    _getContentAttr: function(){
        // summary: hook to make attr("content") work
        return this.containerNode.innerHTML;
    },

    cancel: function(){
        // summary:
        //      Cancels a inflight download of content
        if(this._xhrDfd && (this._xhrDfd.fired == -1)){
            this._xhrDfd.cancel();
        }
        delete this._xhrDfd; // garbage collect
    },

    uninitialize: function(){
        if(this._beingDestroyed){
            this.cancel();
        }
    },

    destroyRecursive: function(/*Boolean*/ preserveDom){
        // summary:
        //      Destroy the ContentPane and it's contents

        // if we have multiple controllers destroying us, bail after the first
        if(this._beingDestroyed){
            return;
        }
        this._beingDestroyed = true;
        this.inherited(arguments);
    },

    resize: function(size){
        dojo.marginBox(this.domNode, size);

        // Compute content box size in case we [later] need to size child
        // If either height or width wasn't specified by the user, then query node for it.
        // But note that setting the margin box and then immediately querying dimensions may return
        // inaccurate results, so try not to depend on it.
        var node = this.containerNode,
            mb = dojo.mixin(dojo.marginBox(node), size||{});

        var cb = this._contentBox = dijit.layout.marginBox2contentBox(node, mb);

        // If we have a single widget child then size it to fit snugly within my borders
        if(this._singleChild && this._singleChild.resize){
            // note: if widget has padding this._contentBox will have l and t set,
            // but don't pass them to resize() or it will doubly-offset the child
            this._singleChild.resize({w: cb.w, h: cb.h});
        }
    },

    _isShown: function(){
        // summary: returns true if the content is currently shown
        if("open" in this){
            return this.open;       // for TitlePane, etc.
        }else{
            var node = this.domNode;
            return (node.style.display != 'none')  && (node.style.visibility != 'hidden') && !dojo.hasClass(node, "dijitHidden");
        }
    },

    _onShow: function(){
        // summary:
        //      Called when the ContentPane is made visible
        // description:
        //      For a plain ContentPane, this is called on initialization, from startup().
        //      If the ContentPane is a hidden pane of a TabContainer etc., then it's
        //      called whever the pane is made visible.
        //
        //      Does processing necessary, including href download and layout/resize of
        //      child widget(s)

        if(this._needLayout){
            // If a layout has been scheduled for when we become visible, do it now
            this._layoutChildren();
        }

        // Do lazy-load of URL
        this._loadCheck();
    },

    _loadCheck: function(){
        // summary:
        //      Call this to load href contents if necessary.
        // description:
        //      Call when !ContentPane has been made visible [from prior hidden state],
        //      or href has been changed, or on startup, etc.

        if(
            (this.href && !this._xhrDfd) &&     // if there's an href that isn't already being loaded
            (!this.isLoaded || this._hrefChanged || this.refreshOnShow) &&  // and we need a [re]load
            (this.preload || this._isShown())   // and now is the time to [re]load
        ){
            delete this._hrefChanged;
            this.refresh();
        }
    },

    refresh: function(){
        // summary:
        //      [Re]download contents of href and display
        // description:
        //      1. cancels any currently in-flight requests
        //      2. posts "loading..." message
        //      3. sends XHR to download new data

        // cancel possible prior inflight request
        this.cancel();

        // display loading message
        this._setContent(this.onDownloadStart(), true);

        var self = this;
        var getArgs = {
            preventCache: (this.preventCache || this.refreshOnShow),
            url: this.href,
            handleAs: "text"
        };
        if(dojo.isObject(this.ioArgs)){
            dojo.mixin(getArgs, this.ioArgs);
        }

        var hand = this._xhrDfd = (this.ioMethod || dojo.xhrGet)(getArgs);

        hand.addCallback(function(html){
            try{
                self._isDownloaded = true;
                self._setContent(html, false);
                self.onDownloadEnd();
            }catch(err){
                self._onError('Content', err); // onContentError
            }
            delete self._xhrDfd;
            return html;
        });

        hand.addErrback(function(err){
            if(!hand.canceled){
                // show error message in the pane
                self._onError('Download', err); // onDownloadError
            }
            delete self._xhrDfd;
            return err;
        });
    },

    _onLoadHandler: function(data){
        // summary:
        //      This is called whenever new content is being loaded
        this.isLoaded = true;
        try{
            this.onLoad(data);
        }catch(e){
            console.error('Error '+this.widgetId+' running custom onLoad code: ' + e.message);
        }
    },

    _onUnloadHandler: function(){
        // summary:
        //      This is called whenever the content is being unloaded
        this.isLoaded = false;
        try{
            this.onUnload();
        }catch(e){
            console.error('Error '+this.widgetId+' running custom onUnload code: ' + e.message);
        }
    },

    destroyDescendants: function(){
        // summary:
        //      Destroy all the widgets inside the ContentPane and empty containerNode

        // Make sure we call onUnload (but only when the ContentPane has real content)
        if(this.isLoaded){
            this._onUnloadHandler();
        }

        // Even if this.isLoaded == false there might still be a "Loading..." message
        // to erase, so continue...

        // For historical reasons we need to delete all widgets under this.containerNode,
        // even ones that the user has created manually.
        var setter = this._contentSetter;
        dojo.forEach(this.getDescendants(true), function(widget){
            if(widget.destroyRecursive){
                widget.destroyRecursive();
            }
        });
        if(setter){
            // Most of the widgets in setter.parseResults have already been destroyed, but
            // things like Menu that have been moved to <body> haven't yet
            dojo.forEach(setter.parseResults, function(widget){
                if(widget.destroyRecursive && widget.domNode && widget.domNode.parentNode == dojo.body()){
                    widget.destroyRecursive();
                }
            });
            delete setter.parseResults;
        }

        // And then clear away all the DOM nodes
        dojo.html._emptyNode(this.containerNode);
    },

    _setContent: function(cont, isFakeContent){
        // summary:
        //      Insert the content into the container node

        // first get rid of child widgets
        this.destroyDescendants();

        // dojo.html.set will take care of the rest of the details
        // we provide an overide for the error handling to ensure the widget gets the errors
        // configure the setter instance with only the relevant widget instance properties
        // NOTE: unless we hook into attr, or provide property setters for each property,
        // we need to re-configure the ContentSetter with each use
        var setter = this._contentSetter;
        if(! (setter && setter instanceof dojo.html._ContentSetter)) {
            setter = this._contentSetter = new dojo.html._ContentSetter({
                node: this.containerNode,
                _onError: dojo.hitch(this, this._onError),
                onContentError: dojo.hitch(this, function(e){
                    // fires if a domfault occurs when we are appending this.errorMessage
                    // like for instance if domNode is a UL and we try append a DIV
                    var errMess = this.onContentError(e);
                    try{
                        this.containerNode.innerHTML = errMess;
                    }catch(e){
                        console.error('Fatal '+this.id+' could not change content due to '+e.message, e);
                    }
                })/*,
                _onError */
            });
        };

        var setterParams = dojo.mixin({
            cleanContent: this.cleanContent,
            extractContent: this.extractContent,
            parseContent: this.parseOnLoad
        }, this._contentSetterParams || {});

        dojo.mixin(setter, setterParams);

        setter.set( (dojo.isObject(cont) && cont.domNode) ? cont.domNode : cont );

        // setter params must be pulled afresh from the ContentPane each time
        delete this._contentSetterParams;

        if(!isFakeContent){
            dojo.forEach(this.getChildren(), function(child){
                child.startup();
            });

            if(this.doLayout){
                this._checkIfSingleChild();
            }

            // Call resize() on each of my child layout widgets,
            // or resize() on my single child layout widget...
            // either now (if I'm currently visible)
            // or when I become visible
            this._scheduleLayout();

            this._onLoadHandler(cont);
        }
    },

    _onError: function(type, err, consoleText){
        // shows user the string that is returned by on[type]Error
        // overide on[type]Error and return your own string to customize
        var errText = this['on' + type + 'Error'].call(this, err);
        if(consoleText){
            console.error(consoleText, err);
        }else if(errText){// a empty string won't change current content
            this._setContent(errText, true);
        }
    },

    // Implement _Container API as well as we can
    // Note that methods like addChild() don't mean much if our contents are free form HTML

    getChildren: function(){
        // Normally the children's dom nodes are direct children of this.containerNode,
        // but not so with ContentPane... they could be many levels deep.   So we can't
        // use the getChildren() in _Container.
        return this.getDescendants(true);
    },

    addChild: function(/*Widget*/ child, /*Integer?*/ insertIndex){
        this.inherited(arguments);
        if(this._started && child.resize){
            // Layout widgets expect their parent to call resize() on them
            child.resize();
        }
    },

    _scheduleLayout: function(){
        // summary:
        //      Call resize() on each of my child layout widgets, either now
        //      (if I'm currently visible) or when I become visible
        if(this._isShown()){
            this._layoutChildren();
        }else{
            this._needLayout = true;
        }
    },

    _layoutChildren: function(){
        // summary:
        //      Since I am a Container widget, each of my children expects me to
        //      call resize() or layout() on them.
        // description:
        //      Should be called on initialization and also whenever we get new content
        //      (from an href, or from attr('content', ...))... but deferred until
        //      the ContentPane is visible

        if(this._singleChild && this._singleChild.resize){
            var cb = this._contentBox || dojo.contentBox(this.containerNode);
            this._singleChild.resize({w: cb.w, h: cb.h});
        }else{
            // All my child widgets are independently sized (rather than matching my size),
            // but I still need to call resize() on each child to make it layout.
            dojo.forEach(this.getChildren(), function(widget){
                if(widget.resize){
                    widget.resize();
                }
            });
        }
        delete this._needLayout;
    },

    // EVENT's, should be overide-able
    onLoad: function(data){
        // summary:
        //      Event hook, is called after everything is loaded and widgetified
    },

    onUnload: function(){
        // summary:
        //      Event hook, is called before old content is cleared
    },

    onDownloadStart: function(){
        // summary:
        //      called before download starts
        //      the string returned by this function will be the html
        //      that tells the user we are loading something
        //      override with your own function if you want to change text
        return this.loadingMessage;
    },

    onContentError: function(/*Error*/ error){
        // summary:
        //      called on DOM faults, require fault etc in content
        //      default is to display errormessage inside pane
    },

    onDownloadError: function(/*Error*/ error){
        // summary:
        //      Called when download error occurs, default is to display
        //      errormessage inside pane. Overide function to change that.
        //      The string returned by this function will be the html
        //      that tells the user a error happend
        return this.errorMessage;
    },

    onDownloadEnd: function(){
        // summary:
        //      called when download is finished
    }
});

}

if(!dojo._hasResource["dijit._editor.selection"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._editor.selection"] = true;
dojo.provide("dijit._editor.selection");

// FIXME:
//      all of these methods branch internally for IE. This is probably
//      sub-optimal in terms of runtime performance. We should investigate the
//      size difference for differentiating at definition time.

dojo.mixin(dijit._editor.selection, {
    getType: function(){
        // summary: Get the selection type (like dojo.doc.select.type in IE).
        if(dojo.doc.selection){ //IE
            return dojo.doc.selection.type.toLowerCase();
        }else{
            var stype = "text";

            // Check if the actual selection is a CONTROL (IMG, TABLE, HR, etc...).
            var oSel;
            try{
                oSel = dojo.global.getSelection();
            }catch(e){ /*squelch*/ }

            if(oSel && oSel.rangeCount==1){
                var oRange = oSel.getRangeAt(0);
                if( (oRange.startContainer == oRange.endContainer) &&
                    ((oRange.endOffset - oRange.startOffset) == 1) &&
                    (oRange.startContainer.nodeType != 3 /* text node*/)
                ){
                    stype = "control";
                }
            }
            return stype;
        }
    },

    getSelectedText: function(){
        // summary:
        //      Return the text (no html tags) included in the current selection or null if no text is selected
        if(dojo.doc.selection){ //IE
            if(dijit._editor.selection.getType() == 'control'){
                return null;
            }
            return dojo.doc.selection.createRange().text;
        }else{
            var selection = dojo.global.getSelection();
            if(selection){
                return selection.toString();
            }
        }
        return ''
    },

    getSelectedHtml: function(){
        // summary:
        //      Return the html of the current selection or null if unavailable
        if(dojo.doc.selection){ //IE
            if(dijit._editor.selection.getType() == 'control'){
                return null;
            }
            return dojo.doc.selection.createRange().htmlText;
        }else{
            var selection = dojo.global.getSelection();
            if(selection && selection.rangeCount){
                var frag = selection.getRangeAt(0).cloneContents();
                var div = dojo.doc.createElement("div");
                div.appendChild(frag);
                return div.innerHTML;
            }
            return null;
        }
    },

    getSelectedElement: function(){
        // summary:
        //      Retrieves the selected element (if any), just in the case that
        //      a single element (object like and image or a table) is
        //      selected.
        if(dijit._editor.selection.getType() == "control"){
            if(dojo.doc.selection){ //IE
                var range = dojo.doc.selection.createRange();
                if(range && range.item){
                    return dojo.doc.selection.createRange().item(0);
                }
            }else{
                var selection = dojo.global.getSelection();
                return selection.anchorNode.childNodes[ selection.anchorOffset ];
            }
        }
        return null;
    },

    getParentElement: function(){
        // summary:
        //      Get the parent element of the current selection
        if(dijit._editor.selection.getType() == "control"){
            var p = this.getSelectedElement();
            if(p){ return p.parentNode; }
        }else{
            if(dojo.doc.selection){ //IE
                var r=dojo.doc.selection.createRange();
                r.collapse(true);
                return r.parentElement();
            }else{
                var selection = dojo.global.getSelection();
                if(selection){
                    var node = selection.anchorNode;

                    while(node && (node.nodeType != 1)){ // not an element
                        node = node.parentNode;
                    }

                    return node;
                }
            }
        }
        return null;
    },

    hasAncestorElement: function(/*String*/tagName /* ... */){
        // summary:
        //      Check whether current selection has a  parent element which is
        //      of type tagName (or one of the other specified tagName)
        return this.getAncestorElement.apply(this, arguments) != null;
    },

    getAncestorElement: function(/*String*/tagName /* ... */){
        // summary:
        //      Return the parent element of the current selection which is of
        //      type tagName (or one of the other specified tagName)

        var node = this.getSelectedElement() || this.getParentElement();
        return this.getParentOfType(node, arguments);
    },

    isTag: function(/*DomNode*/node, /*Array*/tags){
        if(node && node.tagName){
            var _nlc = node.tagName.toLowerCase();
            for(var i=0; i<tags.length; i++){
                var _tlc = String(tags[i]).toLowerCase();
                if(_nlc == _tlc){
                    return _tlc;
                }
            }
        }
        return "";
    },

    getParentOfType: function(/*DomNode*/node, /*Array*/tags){
        while(node){
            if(this.isTag(node, tags).length){
                return node;
            }
            node = node.parentNode;
        }
        return null;
    },

    collapse: function(/*Boolean*/beginning) {
        // summary: clear current selection
      if(window['getSelection']){
              var selection = dojo.global.getSelection();
              if(selection.removeAllRanges){ // Mozilla
                      if(beginning){
                              selection.collapseToStart();
                      }else{
                              selection.collapseToEnd();
                      }
              }else{ // Safari
                      // pulled from WebCore/ecma/kjs_window.cpp, line 2536
                       selection.collapse(beginning);
              }
      }else if(dojo.doc.selection){ // IE
              var range = dojo.doc.selection.createRange();
              range.collapse(beginning);
              range.select();
      }
    },

    remove: function(){
        // summary: delete current selection
        var _s = dojo.doc.selection;
        if(_s){ //IE
            if(_s.type.toLowerCase() != "none"){
                _s.clear();
            }
            return _s;
        }else{
            _s = dojo.global.getSelection();
            _s.deleteFromDocument();
            return _s;
        }
    },

    selectElementChildren: function(/*DomNode*/element,/*Boolean?*/nochangefocus){
        // summary:
        //      clear previous selection and select the content of the node
        //      (excluding the node itself)
        var _window = dojo.global;
        var _document = dojo.doc;
        element = dojo.byId(element);
        if(_document.selection && dojo.body().createTextRange){ // IE
            var range = element.ownerDocument.body.createTextRange();
            range.moveToElementText(element);
            if(!nochangefocus){
                try{
                    range.select(); // IE throws an exception here if the widget is hidden.  See #5439
                }catch(e){ /* squelch */}
            }
        }else if(_window.getSelection){
            var selection = _window.getSelection();
            if(selection.setBaseAndExtent){ // Safari
                selection.setBaseAndExtent(element, 0, element, element.innerText.length - 1);
            }else if(selection.selectAllChildren){ // Mozilla
                selection.selectAllChildren(element);
            }
        }
    },

    selectElement: function(/*DomNode*/element,/*Boolean?*/nochangefocus){
        // summary:
        //      clear previous selection and select element (including all its children)
        var range, _document = dojo.doc;
        element = dojo.byId(element);
        if(_document.selection && dojo.body().createTextRange){ // IE
            try{
                range = dojo.body().createControlRange();
                range.addElement(element);
                if(!nochangefocus){
                    range.select();
                }
            }catch(e){
                this.selectElementChildren(element,nochangefocus);
            }
        }else if(dojo.global.getSelection){
            var selection = dojo.global.getSelection();
            // FIXME: does this work on Safari?
            if(selection.removeAllRanges){ // Mozilla
                range = _document.createRange();
                range.selectNode(element);
                selection.removeAllRanges();
                selection.addRange(range);
            }
        }
    }
});

}

if(!dojo._hasResource["dijit._editor.range"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._editor.range"] = true;
dojo.provide("dijit._editor.range");

dijit.range={};

dijit.range.getIndex=function(/*DomNode*/node, /*DomNode*/parent){
//  dojo.profile.start("dijit.range.getIndex");
    var ret=[], retR=[];
    var stop = parent;
    var onode = node;

    var pnode, n;
    while(node != stop){
        var i = 0;
        pnode = node.parentNode;
        while((n=pnode.childNodes[i++])){
            if(n===node){
                --i;
                break;
            }
        }
        if(i>=pnode.childNodes.length){
            dojo.debug("Error finding index of a node in dijit.range.getIndex");
        }
        ret.unshift(i);
        retR.unshift(i-pnode.childNodes.length);
        node = pnode;
    }

    //normalized() can not be called so often to prevent
    //invalidating selection/range, so we have to detect
    //here that any text nodes in a row
    if(ret.length>0 && onode.nodeType==3){
        n = onode.previousSibling;
        while(n && n.nodeType==3){
            ret[ret.length-1]--;
            n = n.previousSibling;
        }
        n = onode.nextSibling;
        while(n && n.nodeType==3){
            retR[retR.length-1]++;
            n = n.nextSibling;
        }
    }
//  dojo.profile.end("dijit.range.getIndex");
    return {o: ret, r:retR};
}

dijit.range.getNode = function(/*Array*/index, /*DomNode*/parent){
    if(!dojo.isArray(index) || index.length==0){
        return parent;
    }
    var node = parent;
//  if(!node)debugger
    dojo.every(index, function(i){
        if(i>=0&&i< node.childNodes.length){
            node = node.childNodes[i];
        }else{
            node = null;
            console.debug('Error: can not find node with index',index,'under parent node',parent );
            return false; //terminate dojo.every
        }
        return true; //carry on the every loop
    });

    return node;
}

dijit.range.getCommonAncestor = function(n1,n2){
    var getAncestors = function(n){
        var as=[];
        while(n){
            as.unshift(n);
            if(n.nodeName!='BODY'){
                n = n.parentNode;
            }else{
                break;
            }
        }
        return as;
    };
    var n1as = getAncestors(n1);
    var n2as = getAncestors(n2);

    var m = Math.min(n1as.length,n2as.length);
    var com = n1as[0]; //at least, one element should be in the array: the root (BODY by default)
    for(var i=1;i<m;i++){
        if(n1as[i]===n2as[i]){
            com = n1as[i]
        }else{
            break;
        }
    }
    return com;
}

dijit.range.getAncestor = function(/*DomNode*/node, /*RegEx?*/regex, /*DomNode?*/root){
    root = root || node.ownerDocument.body;
    while(node && node !== root){
        var name = node.nodeName.toUpperCase() ;
        if(regex.test(name)){
            return node;
        }

        node = node.parentNode;
    }
    return null;
}

dijit.range.BlockTagNames = /^(?:P|DIV|H1|H2|H3|H4|H5|H6|ADDRESS|PRE|OL|UL|LI|DT|DE)$/;
dijit.range.getBlockAncestor = function(/*DomNode*/node, /*RegEx?*/regex, /*DomNode?*/root){
    root = root || node.ownerDocument.body;
    regex = regex || dijit.range.BlockTagNames;
    var block=null, blockContainer;
    while(node && node !== root){
        var name = node.nodeName.toUpperCase() ;
        if(!block && regex.test(name)){
            block = node;
        }
        if(!blockContainer && (/^(?:BODY|TD|TH|CAPTION)$/).test(name)){
            blockContainer = node;
        }

        node = node.parentNode;
    }
    return {blockNode:block, blockContainer:blockContainer || node.ownerDocument.body};
}

dijit.range.atBeginningOfContainer = function(/*DomNode*/container, /*DomNode*/node, /*Int*/offset){
    var atBeginning = false;
    var offsetAtBeginning = (offset == 0);
    if(!offsetAtBeginning && node.nodeType==3){ //if this is a text node, check whether the left part is all space
        if(dojo.trim(node.nodeValue.substr(0,offset))==0){
            offsetAtBeginning = true;
        }
    }
    if(offsetAtBeginning){
        var cnode = node;
        atBeginning = true;
        while(cnode && cnode !== container){
            if(cnode.previousSibling){
                atBeginning = false;
                break;
            }
            cnode = cnode.parentNode;
        }
    }
    return atBeginning;
}

dijit.range.atEndOfContainer = function(/*DomNode*/container, /*DomNode*/node, /*Int*/offset){
    var atEnd = false;
    var offsetAtEnd = (offset == (node.length || node.childNodes.length));
    if(!offsetAtEnd && node.nodeType==3){ //if this is a text node, check whether the right part is all space
        if(dojo.trim(node.nodeValue.substr(offset))==0){
            offsetAtEnd = true;
        }
    }
    if(offsetAtEnd){
        var cnode = node;
        atEnd = true;
        while(cnode && cnode !== container){
            if(cnode.nextSibling){
                atEnd = false;
                break;
            }
            cnode = cnode.parentNode;
        }
    }
    return atEnd;
}

dijit.range.adjacentNoneTextNode=function(startnode, next){
    var node = startnode;
    var len = (0-startnode.length) || 0;
    var prop = next?'nextSibling':'previousSibling';
    while(node){
        if(node.nodeType!=3){
            break;
        }
        len += node.length
        node = node[prop];
    }
    return [node,len];
}

dijit.range._w3c = Boolean(window['getSelection']);
dijit.range.create = function(){
    if(dijit.range._w3c){
        return dojo.doc.createRange();
    }else{//IE
        return new dijit.range.W3CRange;
    }
}

dijit.range.getSelection = function(win, /*Boolean?*/ignoreUpdate){
    if(dijit.range._w3c){
        return win.getSelection();
    }else{//IE
        var s = new dijit.range.ie.selection(win);
        if(!ignoreUpdate){
            s._getCurrentSelection();
        }
        return s;
    }
}

if(!dijit.range._w3c){
    dijit.range.ie={
        cachedSelection: {},
        selection: function(win){
            this._ranges = [];
            this.addRange = function(r, /*boolean*/internal){
                this._ranges.push(r);
                if(!internal){
                    r._select();
                }
                this.rangeCount = this._ranges.length;
            };
            this.removeAllRanges = function(){
                //don't detach, the range may be used later
//              for(var i=0;i<this._ranges.length;i++){
//                  this._ranges[i].detach();
//              }
                this._ranges = [];
                this.rangeCount = 0;
            };
            var _initCurrentRange = function(){
                var r = win.document.selection.createRange();
                var type=win.document.selection.type.toUpperCase();
                if(type == "CONTROL"){
                    //TODO: multiple range selection(?)
                    return new dijit.range.W3CRange(dijit.range.ie.decomposeControlRange(r));
                }else{
                    return new dijit.range.W3CRange(dijit.range.ie.decomposeTextRange(r));
                }
            };
            this.getRangeAt = function(i){
                return this._ranges[i];
            };
            this._getCurrentSelection = function(){
                this.removeAllRanges();
                var r=_initCurrentRange();
                if(r){
                    this.addRange(r, true);
                }
            };
        },
        decomposeControlRange: function(range){
            var firstnode = range.item(0), lastnode = range.item(range.length-1)
            var startContainer = firstnode.parentNode, endContainer = lastnode.parentNode;
            var startOffset = dijit.range.getIndex(firstnode, startContainer).o;
            var endOffset = dijit.range.getIndex(lastnode, endContainer).o+1;
            return [startContainer, startOffset,endContainer, endOffset];
        },
        getEndPoint: function(range, end){
            var atmrange = range.duplicate();
            atmrange.collapse(!end);
            var cmpstr = 'EndTo' + (end?'End':'Start');
            var parentNode = atmrange.parentElement();

            var startnode, startOffset, lastNode;
            if(parentNode.childNodes.length>0){
                dojo.every(parentNode.childNodes, function(node,i){
                    var calOffset;
                    if(node.nodeType != 3){
                        atmrange.moveToElementText(node);

                        if(atmrange.compareEndPoints(cmpstr,range) > 0){
                            startnode = node.previousSibling;
                            if(lastNode && lastNode.nodeType == 3){
                                //where share we put the start? in the text node or after?
                                startnode = lastNode;
                                calOffset = true;
                            }else{
                                startnode = parentNode;
                                startOffset = i;
                                return false;
                            }
                        }else{
                            if(i==parentNode.childNodes.length-1){
                                startnode = parentNode;
                                startOffset = parentNode.childNodes.length;
                                return false;
                            }
                        }
                    }else{
                        if(i==parentNode.childNodes.length-1){//at the end of this node
                            startnode = node;
                            calOffset = true;
                        }
                    }
        //          try{
                        if(calOffset && startnode){
                            var prevnode = dijit.range.adjacentNoneTextNode(startnode)[0];
                            if(prevnode){
                                startnode = prevnode.nextSibling;
                            }else{
                                startnode = parentNode.firstChild; //firstChild must be a text node
                            }
                            var prevnodeobj = dijit.range.adjacentNoneTextNode(startnode);
                            prevnode = prevnodeobj[0];
                            var lenoffset = prevnodeobj[1];
                            if(prevnode){
                                atmrange.moveToElementText(prevnode);
                                atmrange.collapse(false);
                            }else{
                                atmrange.moveToElementText(parentNode);
                            }
                            atmrange.setEndPoint(cmpstr, range);
                            startOffset = atmrange.text.length-lenoffset;

                            return false;
                        }
        //          }catch(e){ debugger }
                    lastNode = node;
                    return true;
                });
            }else{
                startnode = parentNode;
                startOffset = 0;
            }

            //if at the end of startnode and we are dealing with start container, then
            //move the startnode to nextSibling if it is a text node
            //TODO: do this for end container?
            if(!end && startnode.nodeType!=3 && startOffset == startnode.childNodes.length){
                if(startnode.nextSibling && startnode.nextSibling.nodeType==3){
                    startnode = startnode.nextSibling;
                    startOffset = 0;
                }
            }
            return [startnode, startOffset];
        },
        setEndPoint: function(range, container, offset){
            //text node
            var atmrange = range.duplicate(), node, len;
            if(container.nodeType!=3){ //normal node
                if(offset > 0){
                    node = container.childNodes[offset-1];
                    if(node.nodeType==3){
                        container = node;
                        offset = node.length;
                        //pass through
                    }else{
                        if(node.nextSibling && node.nextSibling.nodeType==3){
                            container=node.nextSibling;
                            offset=0;
                            //pass through
                        }else{
                            atmrange.moveToElementText(node.nextSibling?node:container);
                            var tempnode=node.parentNode.insertBefore(document.createTextNode(' '),node.nextSibling);
                            atmrange.collapse(false);
                            tempnode.parentNode.removeChild(tempnode);
                        }
                    }
                }else{
                    atmrange.moveToElementText(container);
                    atmrange.collapse(true);
                }
            }
            if(container.nodeType==3){
                var prevnodeobj = dijit.range.adjacentNoneTextNode(container);
                var prevnode = prevnodeobj[0];
                len = prevnodeobj[1];
                if(prevnode){
                    atmrange.moveToElementText(prevnode);
                    atmrange.collapse(false);
                    //if contentEditable is not inherit, the above collapse won't make the end point
                    //in the correctly position: it always has a -1 offset, so compensate it
                    if(prevnode.contentEditable!='inherit'){
                        len++;
                    }
                }else{
                    atmrange.moveToElementText(container.parentNode);
                    atmrange.collapse(true);
                }

                offset += len;
                if(offset>0){
                    if(atmrange.move('character',offset) != offset){
                        console.error('Error when moving!');
                    }
                }
            }

            return atmrange;
        },
        decomposeTextRange: function(range){
            var tmpary = dijit.range.ie.getEndPoint(range);
            var startContainter = tmpary[0], startOffset = tmpary[1];
            var endContainter = tmpary[0], endOffset = tmpary[1];

            if(range.htmlText.length){
                if(range.htmlText == range.text){ //in the same text node
                    endOffset = startOffset+range.text.length;
                }else{
                    tmpary = dijit.range.ie.getEndPoint(range,true);
                    endContainter = tmpary[0], endOffset = tmpary[1];
                }
            }
            return [startContainter, startOffset,endContainter, endOffset];
        },
        setRange: function(range, startContainter,
            startOffset, endContainter, endOffset, collapsed){
            var start=dijit.range.ie.setEndPoint(range, startContainter, startOffset);

            range.setEndPoint('StartToStart',start);
            if(!collapsed){
                var end=dijit.range.ie.setEndPoint(range, endContainter, endOffset);
            }
            range.setEndPoint('EndToEnd',end||start);

            return range;
        }
    }

dojo.declare("dijit.range.W3CRange",null, {
    constructor: function(){
        if(arguments.length>0){
            this.setStart(arguments[0][0],arguments[0][1]);
            this.setEnd(arguments[0][2],arguments[0][3]);
        }else{
            this.commonAncestorContainer = null;
            this.startContainer = null;
            this.startOffset = 0;
            this.endContainer = null;
            this.endOffset = 0;
            this.collapsed = true;
        }
    },
    _updateInternal: function(){
        if(this.startContainer !== this.endContainer){
            this.commonAncestorContainer = dijit.range.getCommonAncestor(this.startContainer, this.endContainer);
        }else{
            this.commonAncestorContainer = this.startContainer;
        }
        this.collapsed = (this.startContainer === this.endContainer) && (this.startOffset == this.endOffset);
    },
    setStart: function(node, offset){
        offset=parseInt(offset);
        if(this.startContainer === node && this.startOffset == offset){
            return;
        }
        delete this._cachedBookmark;

        this.startContainer = node;
        this.startOffset = offset;
        if(!this.endContainer){
            this.setEnd(node, offset);
        }else{
            this._updateInternal();
        }
    },
    setEnd: function(node, offset){
        offset=parseInt(offset);
        if(this.endContainer === node && this.endOffset == offset){
            return;
        }
        delete this._cachedBookmark;

        this.endContainer = node;
        this.endOffset = offset;
        if(!this.startContainer){
            this.setStart(node, offset);
        }else{
            this._updateInternal();
        }
    },
    setStartAfter: function(node, offset){
        this._setPoint('setStart', node, offset, 1);
    },
    setStartBefore: function(node, offset){
        this._setPoint('setStart', node, offset, 0);
    },
    setEndAfter: function(node, offset){
        this._setPoint('setEnd', node, offset, 1);
    },
    setEndBefore: function(node, offset){
        this._setPoint('setEnd', node, offset, 0);
    },
    _setPoint: function(what, node, offset, ext){
        var index = dijit.range.getIndex(node, node.parentNode).o;
        this[what](node.parentNode, index.pop()+ext);
    },
    _getIERange: function(){
        var r=(this._body||this.endContainer.ownerDocument.body).createTextRange();
        dijit.range.ie.setRange(r, this.startContainer, this.startOffset, this.endContainer, this.endOffset, this.collapsed);
        return r;
    },
    getBookmark: function(body){
        this._getIERange();
        return this._cachedBookmark;
    },
    _select: function(){
        var r = this._getIERange();
        r.select();
    },
    deleteContents: function(){
        var r = this._getIERange();
        r.pasteHTML('');
        this.endContainer = this.startContainer;
        this.endOffset = this.startOffset;
        this.collapsed = true;
    },
    cloneRange: function(){
        var r = new dijit.range.W3CRange([this.startContainer,this.startOffset,
            this.endContainer,this.endOffset]);
        r._body = this._body;
        return r;
    },
    detach: function(){
        this._body = null;
        this.commonAncestorContainer = null;
        this.startContainer = null;
        this.startOffset = 0;
        this.endContainer = null;
        this.endOffset = 0;
        this.collapsed = true;
}
});
} //if(!dijit.range._w3c)

}

if(!dojo._hasResource["dijit._editor.html"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._editor.html"] = true;
dojo.provide("dijit._editor.html");

dijit._editor.escapeXml=function(/*String*/str, /*Boolean?*/noSingleQuotes){
    //summary:
    //      Adds escape sequences for special characters in XML: &<>"'
    //      Optionally skips escapes for single quotes
    str = str.replace(/&/gm, "&amp;").replace(/</gm, "&lt;").replace(/>/gm, "&gt;").replace(/"/gm, "&quot;");
    if(!noSingleQuotes){
        str = str.replace(/'/gm, "&#39;");
    }
    return str; // string
};

dijit._editor.getNodeHtml=function(/* DomNode */node){
    var output;
    switch(node.nodeType){
        case 1: //element node
            output = '<' + node.nodeName.toLowerCase();

            //store the list of attributes and sort it to have the
            //attributes appear in the dictionary order
            var attrarray = [];
            if(dojo.isIE && node.outerHTML){
                var s = node.outerHTML;
                s = s.substr(0, s.indexOf('>'))
                    .replace(/(['"])[^"']*\1/g, ''); //to make the following regexp safe
                var reg = /([^\s=]+)=/g;
                var m, key;
                while((m = reg.exec(s))){
                    key = m[1];
                    if(key.substr(0,3) != '_dj'){
                        if(key == 'src' || key == 'href'){
                            if(node.getAttribute('_djrealurl')){
                                attrarray.push([key,node.getAttribute('_djrealurl')]);
                                continue;
                            }
                        }
                        var val;
                        switch(key){
                            case 'style':
                                val = node.style.cssText.toLowerCase();
                                break;
                            case 'class':
                                val = node.className;
                                break;
                            default:
                                val = node.getAttribute(key);
                        }
                        attrarray.push([key, val.toString()]);
                    }
                }
            }else{
                var attr, i = 0;
                while((attr = node.attributes[i++])){
                    //ignore all attributes starting with _dj which are
                    //internal temporary attributes used by the editor
                    var n = attr.name;
                    if(n.substr(0,3) != '_dj' /*&&
                        (attr.specified == undefined || attr.specified)*/){
                        var v = attr.value;
                        if(n == 'src' || n == 'href'){
                            if(node.getAttribute('_djrealurl')){
                                v = node.getAttribute('_djrealurl');
                            }
                        }
                        attrarray.push([n,v]);
                    }
                }
            }
            attrarray.sort(function(a,b){
                return a[0]<b[0]?-1:(a[0]==b[0]?0:1);
            });
            var j = 0;
            while((attr = attrarray[j++])){
                output += ' ' + attr[0] + '="' +
                    (dojo.isString(attr[1]) ? dijit._editor.escapeXml(attr[1], true) : attr[1]) + '"';
            }
            if(node.childNodes.length){
                output += '>' + dijit._editor.getChildrenHtml(node)+'</'+node.nodeName.toLowerCase()+'>';
            }else{
                output += ' />';
            }
            break;
        case 3: //text
            // FIXME:
            output = dijit._editor.escapeXml(node.nodeValue, true);
            break;
        case 8: //comment
            // FIXME:
            output = '<!--' + dijit._editor.escapeXml(node.nodeValue, true) + '-->';
            break;
        default:
            output = "<!-- Element not recognized - Type: " + node.nodeType + " Name: " + node.nodeName + "-->";
    }
    return output;
};

dijit._editor.getChildrenHtml = function(/* DomNode */dom){
    // summary: Returns the html content of a DomNode and children
    var out = "";
    if(!dom){ return out; }
    var nodes = dom["childNodes"] || dom;

    //IE issue.
    //If we have an actual node we can check parent relationships on for IE,
    //We should check, as IE sometimes builds invalid DOMS.  If no parent, we can't check
    //And should just process it and hope for the best.
    var checkParent = !dojo.isIE || nodes !== dom;

    var node, i = 0;
    while((node = nodes[i++])){
        //IE is broken.  DOMs are supposed to be a tree.  But in the case of malformed HTML, IE generates a graph
        //meaning one node ends up with multiple references (multiple parents).  This is totally wrong and invalid, but
        //such is what it is.  We have to keep track and check for this because otherise the source output HTML will have dups.
        //No other browser generates a graph.  Leave it to IE to break a fundamental DOM rule.  So, we check the parent if we can
        //If we can't, nothing more we can do other than walk it.
        if(!checkParent || node.parentNode == dom){
            out += dijit._editor.getNodeHtml(node);
        }
    }
    return out; // String
};

}

if(!dojo._hasResource["dijit._editor.RichText"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._editor.RichText"] = true;
dojo.provide("dijit._editor.RichText");








// used to restore content when user leaves this page then comes back
// but do not try doing dojo.doc.write if we are using xd loading.
// dojo.doc.write will only work if RichText.js is included in the dojo.js
// file. If it is included in dojo.js and you want to allow rich text saving
// for back/forward actions, then set dojo.config.allowXdRichTextSave = true.
if(!dojo.config["useXDomain"] || dojo.config["allowXdRichTextSave"]){
    if(dojo._postLoad){
        (function(){
            var savetextarea = dojo.doc.createElement('textarea');
            savetextarea.id = dijit._scopeName + "._editor.RichText.savedContent";
            dojo.style(savetextarea, {
                display:'none',
                position:'absolute',
                top:"-100px",
                height:"3px",
                width:"3px"
            });
            dojo.body().appendChild(savetextarea);
        })();
    }else{
        //dojo.body() is not available before onLoad is fired
        try {
            dojo.doc.write('<textarea id="' + dijit._scopeName + '._editor.RichText.savedContent" ' +
                'style="display:none;position:absolute;top:-100px;left:-100px;height:3px;width:3px;overflow:hidden;"></textarea>');
        }catch(e){ }
    }
}

dojo.declare("dijit._editor.RichText", dijit._Widget, {
    constructor: function(params){
        // summary:
        //      dijit._editor.RichText is the core of dijit.Editor, which provides basic
        //      WYSIWYG editing features.
        //
        // description:
        //      dijit._editor.RichText is the core of dijit.Editor, which provides basic
        //      WYSIWYG editing features. It also encapsulates the differences
        //      of different js engines for various browsers.  Do not use this widget
        //      with an HTML &lt;TEXTAREA&gt; tag, since the browser unescapes XML escape characters,
        //      like &lt;.  This can have unexpected behavior and lead to security issues
        //      such as scripting attacks.
        //
        // contentPreFilters: Array
        //      pre content filter function register array.
        //      these filters will be executed before the actual
        //      editing area get the html content
        this.contentPreFilters = [];

        // contentPostFilters: Array
        //      post content filter function register array.
        //      these will be used on the resulting html
        //      from contentDomPostFilters. The resuling
        //      content is the final html (returned by getValue())
        this.contentPostFilters = [];

        // contentDomPreFilters: Array
        //      pre content dom filter function register array.
        //      these filters are applied after the result from
        //      contentPreFilters are set to the editing area
        this.contentDomPreFilters = [];

        // contentDomPostFilters: Array
        //      post content dom filter function register array.
        //      these filters are executed on the editing area dom
        //      the result from these will be passed to contentPostFilters
        this.contentDomPostFilters = [];

        // editingAreaStyleSheets: Array
        //      array to store all the stylesheets applied to the editing area
        this.editingAreaStyleSheets=[];

        this._keyHandlers = {};
        this.contentPreFilters.push(dojo.hitch(this, "_preFixUrlAttributes"));
        if(dojo.isMoz){
            this.contentPreFilters.push(this._fixContentForMoz);
            this.contentPostFilters.push(this._removeMozBogus);
        }
        if(dojo.isSafari){
            this.contentPostFilters.push(this._removeSafariBogus);
        }
        //this.contentDomPostFilters.push(this._postDomFixUrlAttributes);

        this.onLoadDeferred = new dojo.Deferred();
    },

    // inheritWidth: Boolean
    //      whether to inherit the parent's width or simply use 100%
    inheritWidth: false,

    // focusOnLoad: Boolean
    //      whether focusing into this instance of richtext when page onload
    focusOnLoad: false,

    // name: String
    //      If a save name is specified the content is saved and restored when the user
    //      leave this page can come back, or if the editor is not properly closed after
    //      editing has started.
    name: "",

    // styleSheets: String
    //      semicolon (";") separated list of css files for the editing area
    styleSheets: "",

    // _content: String
    //      temporary content storage
    _content: "",

    // height: String
    //      set height to fix the editor at a specific height, with scrolling.
    //      By default, this is 300px. If you want to have the editor always
    //      resizes to accommodate the content, use AlwaysShowToolbar plugin
    //      and set height="". If this editor is used within a layout widget,
    //      set height="100%".
    height: "300px",

    // minHeight: String
    //      The minimum height that the editor should have
    minHeight: "1em",

    // isClosed: Boolean
    isClosed: true,

    // isLoaded: Boolean
    isLoaded: false,

    // _SEPARATOR: String
    //      used to concat contents from multiple textareas into a single string
    _SEPARATOR: "@@**%%__RICHTEXTBOUNDRY__%%**@@",

    // onLoadDeferred: dojo.Deferred
    //      deferred which is fired when the editor finishes loading
    onLoadDeferred: null,

    // isTabIndent: Boolean
    //      used to allow tab key and shift-tab to indent and outdent rather than navigate
    isTabIndent: false,

    // disableSpellCheck: Boolean
    //      when true, disables the browser's native spell checking, if supported.
    //      Works only in Firefox.
    disableSpellCheck: false,

    postCreate: function(){
        if("textarea" == this.domNode.tagName.toLowerCase()){
            console.warn("RichText should not be used with the TEXTAREA tag.  See dijit._editor.RichText docs.");
        }
        dojo.publish(dijit._scopeName + "._editor.RichText::init", [this]);
        this.open();
        this.setupDefaultShortcuts();
    },

    setupDefaultShortcuts: function(){
        // summary: add some default key handlers
        // description:
        //      Overwrite this to setup your own handlers. The default
        //      implementation does not use Editor commands, but directly
        //      executes the builtin commands within the underlying browser
        //      support.
        var exec = dojo.hitch(this, function(cmd, arg){
            return function(){
                return !this.execCommand(cmd,arg);
            };
        });

        var ctrlKeyHandlers = {
            b: exec("bold"),
            i: exec("italic"),
            u: exec("underline"),
            a: exec("selectall"),
            s: function(){ this.save(true); },
            m: function(){ this.isTabIndent = !this.isTabIndent; },

            "1": exec("formatblock", "h1"),
            "2": exec("formatblock", "h2"),
            "3": exec("formatblock", "h3"),
            "4": exec("formatblock", "h4"),

            "\\": exec("insertunorderedlist")
        };

        if(!dojo.isIE){
            ctrlKeyHandlers.Z = exec("redo"); //FIXME: undo?
        }

        for(var key in ctrlKeyHandlers){
            this.addKeyHandler(key, true, false, ctrlKeyHandlers[key]);
        }
    },

    // events: Array
    //       events which should be connected to the underlying editing area
    events: ["onKeyPress", "onKeyDown", "onKeyUp", "onClick"],

    // events: Array
    //       events which should be connected to the underlying editing
    //       area, events in this array will be addListener with
    //       capture=true
    captureEvents: [],

    _editorCommandsLocalized: false,
    _localizeEditorCommands: function(){
        if(this._editorCommandsLocalized){
            return;
        }
        this._editorCommandsLocalized = true;

        //in IE, names for blockformat is locale dependent, so we cache the values here

        //if the normal way fails, we try the hard way to get the list

        //do not use _cacheLocalBlockFormatNames here, as it will
        //trigger security warning in IE7

        //put p after div, so if IE returns Normal, we show it as paragraph
        //We can distinguish p and div if IE returns Normal, however, in order to detect that,
        //we have to call this.document.selection.createRange().parentElement() or such, which
        //could slow things down. Leave it as it is for now
        var formats = ['div', 'p', 'pre', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ol', 'ul', 'address'];
        var localhtml = "", format, i=0;
        while((format=formats[i++])){
            //append a <br> after each element to separate the elements more reliably
            if(format.charAt(1) != 'l'){
                localhtml += "<"+format+"><span>content</span></"+format+"><br/>";
            }else{
                localhtml += "<"+format+"><li>content</li></"+format+"><br/>";
            }
        }
        //queryCommandValue returns empty if we hide editNode, so move it out of screen temporary
        var div = dojo.doc.createElement('div');
        dojo.style(div, {
            position: "absolute",
            top: "-2000px"
        });
        dojo.doc.body.appendChild(div);
        div.innerHTML = localhtml;
        var node = div.firstChild;
        while(node){
            dijit._editor.selection.selectElement(node.firstChild);
            dojo.withGlobal(this.window, "selectElement", dijit._editor.selection, [node.firstChild]);
            var nativename = node.tagName.toLowerCase();
            this._local2NativeFormatNames[nativename] = document.queryCommandValue("formatblock");
            //this.queryCommandValue("formatblock");
            this._native2LocalFormatNames[this._local2NativeFormatNames[nativename]] = nativename;
            node = node.nextSibling.nextSibling;
        }
        dojo.body().removeChild(div);
    },

    open: function(/*DomNode?*/element){
        //  summary:
        //      Transforms the node referenced in this.domNode into a rich text editing
        //      node.
        //  description:
        //      Sets up the editing area asynchronously. This will result in
        //      the creation and replacement with an <iframe> if
        //      designMode(FF)/contentEditable(IE) is used and stylesheets are
        //      specified, if we're in a browser that doesn't support
        //      contentEditable.
        //
        //      A dojo.Deferred object is created at this.onLoadDeferred, and
        //      users may attach to it to be informed when the rich-text area
        //      initialization is finalized.

        if(!this.onLoadDeferred || this.onLoadDeferred.fired >= 0){
            this.onLoadDeferred = new dojo.Deferred();
        }

        if(!this.isClosed){ this.close(); }
        dojo.publish(dijit._scopeName + "._editor.RichText::open", [ this ]);

        this._content = "";
        if(arguments.length == 1 && element.nodeName){ // else unchanged
            this.domNode = element;
        }

        var dn = this.domNode;

        var html;
        if(dn.nodeName && dn.nodeName.toLowerCase() == "textarea"){
            // if we were created from a textarea, then we need to create a
            // new editing harness node.
            var ta = (this.textarea = dn);
            this.name = ta.name;
            html = this._preFilterContent(ta.value);
            dn = this.domNode = dojo.doc.createElement("div");
            dn.setAttribute('widgetId', this.id);
            ta.removeAttribute('widgetId');
            dn.cssText = ta.cssText;
            dn.className += " " + ta.className;
            dojo.place(dn, ta, "before");
            var tmpFunc = dojo.hitch(this, function(){
                //some browsers refuse to submit display=none textarea, so
                //move the textarea out of screen instead
                dojo.style(ta, {
                    display: "block",
                    position: "absolute",
                    top: "-1000px"
                });

                if(dojo.isIE){ //nasty IE bug: abnormal formatting if overflow is not hidden
                    var s = ta.style;
                    this.__overflow = s.overflow;
                    s.overflow = "hidden";
                }
            });
            if(dojo.isIE){
                setTimeout(tmpFunc, 10);
            }else{
                tmpFunc();
            }

            // this.domNode.innerHTML = html;

            if(ta.form){
                dojo.connect(ta.form, "onsubmit", this, function(){
                    // FIXME: should we be calling close() here instead?
                    ta.value = this.getValue();
                });
            }
        }else{
            html = this._preFilterContent(dijit._editor.getChildrenHtml(dn));
            dn.innerHTML = "";
        }
        if(html == ""){ html = "&nbsp;"; }

        var content = dojo.contentBox(dn);
        // var content = dojo.contentBox(this.srcNodeRef);
        this._oldHeight = content.h;
        this._oldWidth = content.w;

        this.savedContent = html;

        // If we're a list item we have to put in a blank line to force the
        // bullet to nicely align at the top of text
        if(dn.nodeName && dn.nodeName == "LI"){
            dn.innerHTML = " <br>";
        }

        this.editingArea = dn.ownerDocument.createElement("div");
        dn.appendChild(this.editingArea);

        if(this.name != "" && (!dojo.config["useXDomain"] || dojo.config["allowXdRichTextSave"])){
            var saveTextarea = dojo.byId(dijit._scopeName + "._editor.RichText.savedContent");
            if(saveTextarea.value != ""){
                var datas = saveTextarea.value.split(this._SEPARATOR), i=0, dat;
                while((dat=datas[i++])){
                    var data = dat.split(":");
                    if(data[0] == this.name){
                        html = data[1];
                        datas.splice(i, 1);
                        break;
                    }
                }
            }

            // FIXME: need to do something different for Opera/Safari
            this.connect(window, "onbeforeunload", "_saveContent");
            // dojo.connect(window, "onunload", this, "_saveContent");
        }

        this.isClosed = false;

        // Safari's selections go all out of whack if we do it inline,
        // so for now IE is our only hero
        //if(typeof dojo.doc.body.contentEditable != "undefined")
        if(dojo.isIE || dojo.isWebKit || dojo.isOpera){ // contentEditable, easy
            var burl = dojo.config["dojoBlankHtmlUrl"] || (dojo.moduleUrl("dojo", "resources/blank.html")+"");
            var ifr = (this.editorObject = this.iframe = dojo.doc.createElement('iframe'));
            ifr.id = this.id+"_iframe";
            ifr.src = burl;
            ifr.style.border = "none";
            ifr.style.width = "100%";
            ifr.frameBorder = 0;
            // ifr.style.scrolling = this.height ? "auto" : "vertical";
            this.editingArea.appendChild(ifr);
            var h = null; // set later in non-ie6 branch
            var loadFunc = dojo.hitch( this, function(){
                if(h){ dojo.disconnect(h); h = null; }
                this.window = ifr.contentWindow;
                var d = (this.document = this.window.document);
                d.open();
                d.write(this._getIframeDocTxt(html));
                d.close();

                if(this._layoutMode){
                    // iframe should be 100% height, thus getting it's height from surrounding
                    // <div> (which has the correct height set by Editor
                    ifr.style.height = "100%";
                }else{
                    if(dojo.isIE >= 7){
                        if(this.height){
                            ifr.style.height = this.height;
                        }
                        if(this.minHeight){
                            ifr.style.minHeight = this.minHeight;
                        }
                    }else{
                        ifr.style.height = this.height ? this.height : this.minHeight;
                    }
                }

                if(dojo.isIE){
                    this._localizeEditorCommands();
                }

                this.onLoad();
                this.savedContent = this.getValue(true);
            });
            if(dojo.isIE <= 7){
                var t = setInterval(function(){
                    if(ifr.contentWindow.isLoaded){
                        clearInterval(t);
                        loadFunc();
                    }
                }, 100);
            }else{ // blissful sanity!
                h = dojo.connect(dojo.isIE ? ifr.contentWindow : ifr, "onload", loadFunc);
            }
        }else{ // designMode in iframe
            this._drawIframe(html);
            this.savedContent = this.getValue(true);
        }

        // TODO: this is a guess at the default line-height, kinda works
        if(dn.nodeName == "LI"){
            dn.lastChild.style.marginTop = "-1.2em";
        }

        if(this.domNode.nodeName == "LI"){ this.domNode.lastChild.style.marginTop = "-1.2em"; }
        dojo.addClass(this.domNode, "RichTextEditable");
    },

    //static cache variables shared among all instance of this class
    _local2NativeFormatNames: {},
    _native2LocalFormatNames: {},
    _localizedIframeTitles: null,

    _getIframeDocTxt: function(/* String */ html){
        var _cs = dojo.getComputedStyle(this.domNode);
        if(dojo.isIE || (!this.height && !dojo.isMoz)){
            html="<div>"+html+"</div>";
        }
        var font = [ _cs.fontWeight, _cs.fontSize, _cs.fontFamily ].join(" ");

        // line height is tricky - applying a units value will mess things up.
        // if we can't get a non-units value, bail out.
        var lineHeight = _cs.lineHeight;
        if(lineHeight.indexOf("px") >= 0){
            lineHeight = parseFloat(lineHeight)/parseFloat(_cs.fontSize);
            // console.debug(lineHeight);
        }else if(lineHeight.indexOf("em")>=0){
            lineHeight = parseFloat(lineHeight);
        }else{
            lineHeight = "1.0";
        }
        var userStyle = "";
        this.style.replace(/(^|;)(line-|font-?)[^;]+/g, function(match){ userStyle += match.replace(/^;/g,"") + ';' });
        return [
            this.isLeftToRight() ? "<html><head>" : "<html dir='rtl'><head>",
            (dojo.isMoz ? "<title>" + this._localizedIframeTitles.iframeEditTitle + "</title>" : ""),
            "<style>",
            "body,html {",
            "\tbackground:transparent;",
            "\tpadding: 1em 0 0 0;",
            "\tmargin: -1em 0 0 0;", // remove extraneous vertical scrollbar on safari and firefox
            "\theight: 100%;",
            "}",
            // TODO: left positioning will cause contents to disappear out of view
            //     if it gets too wide for the visible area
            "body{",
            "\ttop:0px; left:0px; right:0px;",
            "\tfont:", font, ";",
                ((this.height||dojo.isOpera) ? "" : "position: fixed;"),
            // FIXME: IE 6 won't understand min-height?
            "\tmin-height:", this.minHeight, ";",
            "\tline-height:", lineHeight,
            "}",
            "p{ margin: 1em 0 !important; }",
            (this.height ? // height:auto undoes the height:100%
                "" : "body,html{height:auto;overflow-y:hidden;/*for IE*/} body > div {overflow-x:auto;/*for FF to show vertical scrollbar*/}"
            ),
            "li > ul:-moz-first-node, li > ol:-moz-first-node{ padding-top: 1.2em; } ",
            "li{ min-height:1.2em; }",
            "</style>",
            this._applyEditingAreaStyleSheets(),
            "</head><body style='"+userStyle+"'>"+html+"</body></html>"
        ].join(""); // String
    },

    _drawIframe: function(/*String*/html){
        // summary:
        //      Draws an iFrame using the existing one if one exists.
        //      Used by Mozilla, Safari, and Opera

        if(!this.iframe){
            var ifr = (this.iframe = dojo.doc.createElement("iframe"));
            ifr.id=this.id+"_iframe";
            // this.iframe.src = "about:blank";
            // dojo.doc.body.appendChild(this.iframe);
            // console.debug(this.iframe.contentDocument.open());
            // dojo.body().appendChild(this.iframe);
            var ifrs = ifr.style;
            // ifrs.border = "1px solid black";
            ifrs.border = "none";
            ifrs.lineHeight = "0"; // squash line height
            ifrs.verticalAlign = "bottom";
            // ifrs.scrolling = this.height ? "auto" : "vertical";
            this.editorObject = this.iframe;
            // get screen reader text for mozilla here, too
            this._localizedIframeTitles = dojo.i18n.getLocalization("dijit.form", "Textarea");
            // need to find any associated label element and update iframe document title
            var label=dojo.query('label[for="'+this.id+'"]');
            if(label.length){
                this._localizedIframeTitles.iframeEditTitle = label[0].innerHTML + " " + this._localizedIframeTitles.iframeEditTitle;
            }
        }
        // opera likes this to be outside the with block
        //  this.iframe.src = "javascript:void(0)";//dojo.uri.dojoUri("src/widget/templates/richtextframe.html") + ((dojo.doc.domain != currentDomain) ? ("#"+dojo.doc.domain) : "");
        this.iframe.style.width = this.inheritWidth ? this._oldWidth : "100%";

        if(this._layoutMode){
            // iframe should be 100% height, thus getting it's height from surrounding
            // <div> (which has the correct height set by Editor
            this.iframe.style.height = "100%";
        }else{
            if(this.height){
                this.iframe.style.height = this.height;
            }else{
                this.iframe.height = this._oldHeight;
            }
        }

        var tmpContent;
        if(this.textarea){
            tmpContent = this.srcNodeRef;
        }else{
            tmpContent = dojo.doc.createElement('div');
            tmpContent.style.display="none";
            tmpContent.innerHTML = html;
            //append tmpContent to under the current domNode so that the margin
            //calculation below is correct
            this.editingArea.appendChild(tmpContent);
        }

        this.editingArea.appendChild(this.iframe);

        //do we want to show the content before the editing area finish loading here?
        //if external style sheets are used for the editing area, the appearance now
        //and after loading of the editing area won't be the same (and padding/margin
        //calculation above may not be accurate)
        //  tmpContent.style.display = "none";
        //  this.editingArea.appendChild(this.iframe);


        // now we wait for the iframe to load. Janky hack!
        var ifrFunc = dojo.hitch(this, function(){
            if(!this.editNode){
                // Iframe hasn't been loaded yet.
                // First deal w/the document to be available (may have to wait for it)
                if(!this.document){
                    try{
                        if(this.iframe.contentWindow){
                            this.window = this.iframe.contentWindow;
                            this.document = this.iframe.contentWindow.document
                        }else if(this.iframe.contentDocument){
                            // for opera
                            // TODO: this method is only being called for FF2; can we remove this?
                            this.window = this.iframe.contentDocument.window;
                            this.document = this.iframe.contentDocument;
                        }
                    }catch(e){}
                    if(!this.document){
                        setTimeout(ifrFunc,50);
                        return;
                    }

                    // note that on Safari lower than 420+, we have to get the iframe
                    // by ID in order to get something w/ a contentDocument property
                    var contentDoc = this.document;
                    contentDoc.open();
                    if(dojo.isAIR){
                        contentDoc.body.innerHTML = html;
                    }else{
                        contentDoc.write(this._getIframeDocTxt(html));
                    }
                    contentDoc.close();

                    dojo.destroy(tmpContent);
                }

                // Wait for body to be available
                // Writing into contentDoc (above) can make <body> temporarily unavailable, may have to delay again
                if(!this.document.body){
                    //console.debug("waiting for iframe body...");
                    setTimeout(ifrFunc,50);
                    return;
                }

                this.onLoad();
            }else{
                // Iframe is already loaded, we are just switching the content
                dojo.destroy(tmpContent);
                this.editNode.innerHTML = html;
                this.onDisplayChanged();
            }
            this._preDomFilterContent(this.editNode);
        });

        ifrFunc();
    },

    _applyEditingAreaStyleSheets: function(){
        // summary:
        //      apply the specified css files in styleSheets
        var files = [];
        if(this.styleSheets){
            files = this.styleSheets.split(';');
            this.styleSheets = '';
        }

        //empty this.editingAreaStyleSheets here, as it will be filled in addStyleSheet
        files = files.concat(this.editingAreaStyleSheets);
        this.editingAreaStyleSheets = [];

        var text='', i=0, url;
        while((url=files[i++])){
            var abstring = (new dojo._Url(dojo.global.location, url)).toString();
            this.editingAreaStyleSheets.push(abstring);
            text += '<link rel="stylesheet" type="text/css" href="'+abstring+'"/>'
        }
        return text;
    },

    addStyleSheet: function(/*dojo._Url*/uri){
        // summary:
        //      add an external stylesheet for the editing area
        // uri: a dojo.uri.Uri pointing to the url of the external css file
        var url=uri.toString();

        //if uri is relative, then convert it to absolute so that it can be resolved correctly in iframe
        if(url.charAt(0) == '.' || (url.charAt(0) != '/' && !uri.host)){
            url = (new dojo._Url(dojo.global.location, url)).toString();
        }

        if(dojo.indexOf(this.editingAreaStyleSheets, url) > -1){
//          console.debug("dijit._editor.RichText.addStyleSheet: Style sheet "+url+" is already applied");
            return;
        }

        this.editingAreaStyleSheets.push(url);
        if(this.document.createStyleSheet){ //IE
            this.document.createStyleSheet(url);
        }else{ //other browser
            var head = this.document.getElementsByTagName("head")[0];
            var stylesheet = this.document.createElement("link");
            stylesheet.rel="stylesheet";
            stylesheet.type="text/css";
            stylesheet.href=url;
            head.appendChild(stylesheet);
        }
    },

    removeStyleSheet: function(/*dojo._Url*/uri){
        // summary:
        //      remove an external stylesheet for the editing area
        var url=uri.toString();
        //if uri is relative, then convert it to absolute so that it can be resolved correctly in iframe
        if(url.charAt(0) == '.' || (url.charAt(0) != '/' && !uri.host)){
            url = (new dojo._Url(dojo.global.location, url)).toString();
        }
        var index = dojo.indexOf(this.editingAreaStyleSheets, url);
        if(index == -1){
//          console.debug("dijit._editor.RichText.removeStyleSheet: Style sheet "+url+" has not been applied");
            return;
        }
        delete this.editingAreaStyleSheets[index];
        dojo.withGlobal(this.window,'query', dojo, ['link:[href="'+url+'"]']).orphan()
    },

    disabled: true,
    _mozSettingProps: ['styleWithCSS','insertBrOnReturn'],
    _setDisabledAttr: function(/*Boolean*/ value){
        if(!this.editNode || "_delayedDisabled" in this){
            this._delayedDisabled = value;
            return;
        }
        value = !!value;
        if(dojo.isIE || dojo.isWebKit || dojo.isOpera){
            var preventIEfocus = dojo.isIE && (this.isLoaded || !this.focusOnLoad);
            if(preventIEfocus){ this.editNode.unselectable = "on"; }
            this.editNode.contentEditable = !value;
            if(preventIEfocus){
                var _this = this;
                setTimeout(function(){ _this.editNode.unselectable = "off"; }, 0);
            }
        }else{ //moz
            if(value){
                //AP: why isn't this set in the constructor, or put in mozSettingProps as a hash?
                this._mozSettings=[false,this.blockNodeForEnter==='BR'];
            }
            this.document.designMode=(value?'off':'on');
            if(!value && this._mozSettingProps){
                var ps = this._mozSettingProps;
                for(var n in ps){
                    if(ps.hasOwnProperty(n)){
                        try{
                            this.document.execCommand(n,false,ps[n]);
                        }catch(e){}
                    }
                }
            }
//          this.document.execCommand('contentReadOnly', false, value);
//              if(value){
//                  this.blur(); //to remove the blinking caret
//              }
        }
        this.disabled = value;
    },

/* Event handlers
 *****************/

    _isResized: function(){ return false; },

    onLoad: function(/* Event */ e){
        // summary:
        //      handler after the content of the document finishes loading
        if(!this.window.__registeredWindow){
            this.window.__registeredWindow = true;
            dijit.registerWin(this.window);
        }
        if(!dojo.isIE && (this.height || dojo.isMoz)){
            this.editNode=this.document.body;
        }else{
            this.editNode=this.document.body.firstChild;
            var _this = this;
            if(dojo.isIE){ // #4996 IE wants to focus the BODY tag
                var tabStop = (this.tabStop = dojo.doc.createElement('<div tabIndex=-1>'));
                this.editingArea.appendChild(tabStop);
                this.iframe.onfocus = function(){ _this.editNode.setActive(); }
            }
        }
        this.focusNode = this.editNode; // for InlineEditBox

        try{
            this.attr('disabled',false);
        }catch(e){
            // Firefox throws an exception if the editor is initially hidden
            // so, if this fails, try again onClick by adding "once" advice
            var handle = dojo.connect(this, "onClick", this, function(){
                this.attr('disabled',false);
                dojo.disconnect(handle);
            });
        }

        this._preDomFilterContent(this.editNode);

        var events = this.events.concat(this.captureEvents);
        var ap = this.iframe ? this.document : this.editNode;
        dojo.forEach(events, function(item){
            // dojo.connect(ap, item.toLowerCase(), console, "debug");
            this.connect(ap, item.toLowerCase(), item);
        }, this);

        if(dojo.isIE){ // IE contentEditable
            // give the node Layout on IE
            this.connect(this.document, "onmousedown", "_onIEMouseDown"); // #4996 fix focus
            this.editNode.style.zoom = 1.0;
        }
        if(this.focusOnLoad){
            dojo.addOnLoad(dojo.hitch(this, "focus"));
        }

        this.onDisplayChanged(e);

        if("_delayedDisabled" in this){
            // We tried to set the disabled attribute previously - but we didn't
            // have everything set up.  Set it up now that we have our nodes
            // created
            var d = this._delayedDisabled;
            delete this._delayedDisabled;
            this.attr("disabled", d);
        }
        this.isLoaded = true;

        if(this.onLoadDeferred){
            this.onLoadDeferred.callback(true);
        }
    },

    onKeyDown: function(/* Event */ e){
        // summary: Fired on keydown

        // we need this event at the moment to get the events from control keys
        // such as the backspace. It might be possible to add this to Dojo, so that
        // keyPress events can be emulated by the keyDown and keyUp detection.

        if(e.keyCode === dojo.keys.TAB && this.isTabIndent ){
            dojo.stopEvent(e); //prevent tab from moving focus out of editor

            // FIXME: this is a poor-man's indent/outdent. It would be
            // better if it added 4 "&nbsp;" chars in an undoable way.
            // Unfortunately pasteHTML does not prove to be undoable
            if(this.queryCommandEnabled((e.shiftKey ? "outdent" : "indent"))){
                this.execCommand((e.shiftKey ? "outdent" : "indent"));
            }
        }
        if(dojo.isIE){
            if(e.keyCode == dojo.keys.TAB && !this.isTabIndent){
                if(e.shiftKey && !e.ctrlKey && !e.altKey){
                    // focus the BODY so the browser will tab away from it instead
                    this.iframe.focus();
                }else if(!e.shiftKey && !e.ctrlKey && !e.altKey){
                    // focus the BODY so the browser will tab away from it instead
                    this.tabStop.focus();
                }
            }else if(e.keyCode === dojo.keys.BACKSPACE && this.document.selection.type === "Control"){
                // IE has a bug where if a non-text object is selected in the editor,
                // hitting backspace would act as if the browser's back button was
                // clicked instead of deleting the object. see #1069
                dojo.stopEvent(e);
                this.execCommand("delete");
            }else if((65 <= e.keyCode&&e.keyCode <= 90) ||
                (e.keyCode>=37&&e.keyCode<=40) // FIXME: get this from connect() instead!
            ){ //arrow keys
                e.charCode = e.keyCode;
                this.onKeyPress(e);
            }
        }else if(dojo.isMoz  && !this.isTabIndent){
            if(e.keyCode == dojo.keys.TAB && !e.shiftKey && !e.ctrlKey && !e.altKey && this.iframe){
                // update iframe document title for screen reader
                var titleObj = dojo.isFF<3 ? this.iframe.contentDocument : this.iframe;
                titleObj.title = this._localizedIframeTitles.iframeFocusTitle;
                // Place focus on the iframe. A subsequent tab or shift tab will put focus
                // on the correct control.
                this.iframe.focus();  // this.focus(); won't work
                dojo.stopEvent(e);
            }else if(e.keyCode == dojo.keys.TAB && e.shiftKey){
                // if there is a toolbar, set focus to it, otherwise ignore
                if(this.toolbar){
                    this.toolbar.focus();
                }
                dojo.stopEvent(e);
            }
        }
        return true;
    },

    onKeyUp: function(e){
        // summary: Fired on keyup
        return;
    },

    setDisabled: function(/*Boolean*/ disabled){
        dojo.deprecated('dijit.Editor::setDisabled is deprecated','use dijit.Editor::attr("disabled",boolean) instead', 2);
        this.attr('disabled',disabled);
    },
    _setValueAttr: function(/*String*/ value){
        // summary: registers that attr("value", foo) should call setValue(foo)
        this.setValue(value);
    },
    _getDisableSpellCheckAttr: function(){
        return !dojo.attr(this.document.body, "spellcheck");
    },
    _setDisableSpellCheckAttr: function(/*Boolean*/ disabled){
        if(this.document){
            dojo.attr(this.document.body, "spellcheck", !disabled);
        }else{
            // try again after the editor is finished loading
            this.onLoadDeferred.addCallback(dojo.hitch(this, function(){
                dojo.attr(this.document.body, "spellcheck", !disabled);
            }));
        }
    },

    onKeyPress: function(e){
        // handle the various key events
        //console.debug("keyup char:", e.keyChar, e.ctrlKey);
        var c = (e.keyChar && e.keyChar.toLowerCase()) || e.keyCode
        var handlers = this._keyHandlers[c];
        //console.debug("handler:", handlers);
        var args = arguments;
        if(handlers){
            dojo.forEach(handlers, function(h){
                if((!!h.shift == !!e.shiftKey)&&(!!h.ctrl == !!e.ctrlKey)){
                    if(!h.handler.apply(this, args)){
                        e.preventDefault();
                    }
                    // break;
                }
            }, this);
        }

        // function call after the character has been inserted
        if(!this._onKeyHitch){
            this._onKeyHitch=dojo.hitch(this, "onKeyPressed");
        }
        setTimeout(this._onKeyHitch, 1);
        return true;
    },

    addKeyHandler: function(/*String*/key, /*Boolean*/ctrl, /*Boolean*/shift, /*Function*/handler){
        // summary: add a handler for a keyboard shortcut
        // description:
        //  The key argument should be in lowercase if it is a letter charater
        if(!dojo.isArray(this._keyHandlers[key])){
            this._keyHandlers[key] = [];
        }
        this._keyHandlers[key].push({
            shift: shift || false,
            ctrl: ctrl || false,
            handler: handler
        });
    },

    onKeyPressed: function(){
        this.onDisplayChanged(/*e*/); // can't pass in e
    },

    onClick: function(/*Event*/e){
        // console.info('onClick',this._tryDesignModeOn);
        this.onDisplayChanged(e);
    },

    _onIEMouseDown: function(/*Event*/e){ // IE only to prevent 2 clicks to focus
        if(!this._focused && !this.disabled){
            this.focus();
        }
    },


    _onBlur: function(e){
        // console.info('_onBlur')

        this.inherited(arguments);
        var _c=this.getValue(true);

        if(_c!=this.savedContent){
            this.onChange(_c);
            this.savedContent=_c;
        }
        if(dojo.isMoz && this.iframe){
            var titleObj = dojo.isFF<3 ? this.iframe.contentDocument : this.iframe;
             titleObj.title = this._localizedIframeTitles.iframeEditTitle;
        }

    },
    _initialFocus: true,
    _onFocus: function(/*Event*/e){
        // summary: Fired on focus

        // console.info('_onFocus')
        if(dojo.isMoz && this._initialFocus){
            this._initialFocus = false;
            if(this.editNode.innerHTML.replace(/^\s+|\s+$/g, "") == "&nbsp;"){
                this.placeCursorAtStart();
                // this.execCommand("selectall");
                // this.window.getSelection().collapseToStart();
            }
        }
        this.inherited(arguments);
    },

    // TODO: why is this needed - should we deprecate this ?
    blur: function(){
        // summary: remove focus from this instance
        if(!dojo.isIE && this.window.document.documentElement && this.window.document.documentElement.focus){
            this.window.document.documentElement.focus();
        }else if(dojo.doc.body.focus){
            dojo.doc.body.focus();
        }
    },

    focus: function(){
        // summary: move focus to this instance
        if(!dojo.isIE){
            dijit.focus(this.iframe);
        }else if(this.editNode && this.editNode.focus){
            // editNode may be hidden in display:none div, lets just punt in this case
            //this.editNode.focus(); -> causes IE to scroll always (strict and quirks mode) to the top the Iframe
            // if we fire the event manually and let the browser handle the focusing, the latest
            // cursor position is focused like in FF
            this.iframe.fireEvent('onfocus', document.createEventObject()); // createEventObject only in IE
        //  }else{
        //  // TODO: should we throw here?
        //  console.debug("Have no idea how to focus into the editor!");
        }
    },

    // _lastUpdate: 0,
    updateInterval: 200,
    _updateTimer: null,
    onDisplayChanged: function(/*Event*/e){
        // summary:
        //      This event will be fired everytime the display context
        //      changes and the result needs to be reflected in the UI.
        // description:
        //      If you don't want to have update too often,
        //      onNormalizedDisplayChanged should be used instead

        // var _t=new Date();
        if(this._updateTimer){
            clearTimeout(this._updateTimer);
        }
        if(!this._updateHandler){
            this._updateHandler = dojo.hitch(this,"onNormalizedDisplayChanged");
        }
        this._updateTimer = setTimeout(this._updateHandler, this.updateInterval);
    },
    onNormalizedDisplayChanged: function(){
        // summary:
        //      This event is fired every updateInterval ms or more
        // description:
        //      If something needs to happen immidiately after a
        //      user change, please use onDisplayChanged instead
        delete this._updateTimer;
    },
    onChange: function(newContent){
        // summary:
        //      this is fired if and only if the editor loses focus and
        //      the content is changed
    },
    _normalizeCommand: function(/*String*/cmd){
        // summary:
        //      Used as the advice function by dojo.connect to map our
        //      normalized set of commands to those supported by the target
        //      browser

        var command = cmd.toLowerCase();
        if(command == "formatblock"){
            if(dojo.isSafari){ command = "heading"; }
        }else if(command == "hilitecolor" && !dojo.isMoz){
            command = "backcolor";
        }

        return command;
    },

    _qcaCache: {},
    queryCommandAvailable: function(/*String*/command){
        // summary:
        //      Tests whether a command is supported by the host. Clients
        //      SHOULD check whether a command is supported before attempting
        //      to use it, behaviour for unsupported commands is undefined.
        // command: The command to test for

        // memoizing version. See _queryCommandAvailable for computing version
        var ca = this._qcaCache[command];
        if(ca != undefined){ return ca; }
        return (this._qcaCache[command] = this._queryCommandAvailable(command));
    },

    _queryCommandAvailable: function(/*String*/command){

        var ie = 1;
        var mozilla = 1 << 1;
        var webkit = 1 << 2;
        var opera = 1 << 3;
        var webkit420 = 1 << 4;

        var gt420 = dojo.isWebKit;

        function isSupportedBy(browsers){
            return {
                ie: Boolean(browsers & ie),
                mozilla: Boolean(browsers & mozilla),
                webkit: Boolean(browsers & webkit),
                webkit420: Boolean(browsers & webkit420),
                opera: Boolean(browsers & opera)
            }
        }

        var supportedBy = null;

        switch(command.toLowerCase()){
            case "bold": case "italic": case "underline":
            case "subscript": case "superscript":
            case "fontname": case "fontsize":
            case "forecolor": case "hilitecolor":
            case "justifycenter": case "justifyfull": case "justifyleft":
            case "justifyright": case "delete": case "selectall": case "toggledir":
                supportedBy = isSupportedBy(mozilla | ie | webkit | opera);
                break;

            case "createlink": case "unlink": case "removeformat":
            case "inserthorizontalrule": case "insertimage":
            case "insertorderedlist": case "insertunorderedlist":
            case "indent": case "outdent": case "formatblock":
            case "inserthtml": case "undo": case "redo": case "strikethrough": case "tabindent":
                supportedBy = isSupportedBy(mozilla | ie | opera | webkit420);
                break;

            case "blockdirltr": case "blockdirrtl":
            case "dirltr": case "dirrtl":
            case "inlinedirltr": case "inlinedirrtl":
                supportedBy = isSupportedBy(ie);
                break;
            case "cut": case "copy": case "paste":
                supportedBy = isSupportedBy( ie | mozilla | webkit420);
                break;

            case "inserttable":
                supportedBy = isSupportedBy(mozilla | ie);
                break;

            case "insertcell": case "insertcol": case "insertrow":
            case "deletecells": case "deletecols": case "deleterows":
            case "mergecells": case "splitcell":
                supportedBy = isSupportedBy(ie | mozilla);
                break;

            default: return false;
        }

        return (dojo.isIE && supportedBy.ie) ||
            (dojo.isMoz && supportedBy.mozilla) ||
            (dojo.isWebKit && supportedBy.webkit) ||
            (dojo.isWebKit > 420 && supportedBy.webkit420) ||
            (dojo.isOpera && supportedBy.opera);  // Boolean return true if the command is supported, false otherwise
    },

    execCommand: function(/*String*/command, argument){
        // summary: Executes a command in the Rich Text area
        // command: The command to execute
        // argument: An optional argument to the command
        var returnValue;

        //focus() is required for IE to work
        //In addition, focus() makes sure after the execution of
        //the command, the editor receives the focus as expected
        this.focus();

        command = this._normalizeCommand(command);

        if(argument != undefined){
            if(command == "heading"){
                throw new Error("unimplemented");
            }else if((command == "formatblock") && dojo.isIE){
                argument = '<'+argument+'>';
            }
        }
        if(command == "inserthtml"){
            //TODO: we shall probably call _preDomFilterContent here as well
            argument = this._preFilterContent(argument);
            returnValue = true;
            if(dojo.isIE){
                var insertRange = this.document.selection.createRange();
                if(this.document.selection.type.toUpperCase()=='CONTROL'){
                    var n=insertRange.item(0);
                    while(insertRange.length){
                        insertRange.remove(insertRange.item(0));
                    }
                    n.outerHTML=argument;
                }else{
                    insertRange.pasteHTML(argument);
                }
                insertRange.select();
                //insertRange.collapse(true);
            }else if(dojo.isMoz && !argument.length){
                //mozilla can not inserthtml an empty html to delete current selection
                //so we delete the selection instead in this case
                this._sCall("remove"); // FIXME
            }else{
                returnValue = this.document.execCommand(command, false, argument);
            }
        }else if(
            (command == "unlink")&&
            (this.queryCommandEnabled("unlink"))&&
            (dojo.isMoz || dojo.isWebKit)
        ){
            // fix up unlink in Mozilla to unlink the link and not just the selection

            // grab selection
            // Mozilla gets upset if we just store the range so we have to
            // get the basic properties and recreate to save the selection
            //  var selection = this.window.getSelection();

            //  var selectionRange = selection.getRangeAt(0);
            //  var selectionStartContainer = selectionRange.startContainer;
            //  var selectionStartOffset = selectionRange.startOffset;
            //  var selectionEndContainer = selectionRange.endContainer;
            //  var selectionEndOffset = selectionRange.endOffset;

            // select our link and unlink
            var a = this._sCall("getAncestorElement", [ "a" ]);
            this._sCall("selectElement", [ a ]);

            returnValue = this.document.execCommand("unlink", false, null);
        }else if((command == "hilitecolor")&&(dojo.isMoz)){
            // mozilla doesn't support hilitecolor properly when useCSS is
            // set to false (bugzilla #279330)

            this.document.execCommand("styleWithCSS", false, true);
            returnValue = this.document.execCommand(command, false, argument);
            this.document.execCommand("styleWithCSS", false, false);

        }else if((dojo.isIE)&&( (command == "backcolor")||(command == "forecolor") )){
            // Tested under IE 6 XP2, no problem here, comment out
            // IE weirdly collapses ranges when we exec these commands, so prevent it
            //  var tr = this.document.selection.createRange();
            argument = arguments.length > 1 ? argument : null;
            returnValue = this.document.execCommand(command, false, argument);

            // timeout is workaround for weird IE behavior were the text
            // selection gets correctly re-created, but subsequent input
            // apparently isn't bound to it
            //  setTimeout(function(){tr.select();}, 1);
        }else{
            argument = arguments.length > 1 ? argument : null;
            //  if(dojo.isMoz){
            //      this.document = this.iframe.contentWindow.document
            //  }

//          console.debug("execCommand:", command, argument);
            if(argument || command!="createlink"){
                returnValue = this.document.execCommand(command, false, argument);
            }
        }

        this.onDisplayChanged();
        return returnValue;
    },

    queryCommandEnabled: function(/*String*/command){
        // summary: check whether a command is enabled or not

        if(this.disabled){ return false; }
        command = this._normalizeCommand(command);
        if(dojo.isMoz || dojo.isWebKit){
            if(command == "unlink"){ // mozilla returns true always
                // console.debug(this._sCall("hasAncestorElement", ['a']));
                this._sCall("hasAncestorElement", ["a"]);
            }else if(command == "inserttable"){
                return true;
            }
        }
        //see #4109
        if(dojo.isWebKit){
            if(command == "copy"){
                command = "cut";
            }else if(command == "paste"){
                return true;
            }
        }
        //should not allow user to indent neither a non-list node nor list item which is the first item in its parent
        if(command == 'indent'){
            var li = this._sCall("getAncestorElement", ["li"]);
            var n = li && li.previousSibling;
            while(n){
                if(n.nodeType == 1){
                  return true;
                }
                n = n.previousSibling;
            }
            return false;
        }else if(command == 'outdent'){
            //should not allow user to outdent a non-list node
            return this._sCall("hasAncestorElement", ["li"]);
        }

        // return this.document.queryCommandEnabled(command);
        var elem = dojo.isIE ? this.document.selection.createRange() : this.document;
        return elem.queryCommandEnabled(command);
    },

    queryCommandState: function(command){
        // summary:
        //      check the state of a given command and returns true or false

        if(this.disabled){ return false; }
        command = this._normalizeCommand(command);
        // try{
            //this.editNode.contentEditable = true;
            return this.document.queryCommandState(command);
        // }catch(e){
        //  console.debug(e);
        //  return false;
        // }
    },

    queryCommandValue: function(command){
        // summary:
        //      check the value of a given command. This matters most for
        //      custom selections and complex values like font value setting

        if(this.disabled){ return false; }
        var r;
        command = this._normalizeCommand(command);
        if(dojo.isIE && command == "formatblock"){
            r = this._native2LocalFormatNames[this.document.queryCommandValue(command)];
        }else{
            r = this.document.queryCommandValue(command);
        }
        return r;
    },

    // Misc.

    _sCall: function(name, args){
        // summary:
        //      run the named method of dijit._editor.selection over the
        //      current editor instance's window, with the passed args
        return dojo.withGlobal(this.window, name, dijit._editor.selection, args);
    },

    // FIXME: this is a TON of code duplication. Why?

    placeCursorAtStart: function(){
        // summary:
        //      place the cursor at the start of the editing area
        this.focus();

        //see comments in placeCursorAtEnd
        var isvalid=false;
        if(dojo.isMoz){
            var first=this.editNode.firstChild;
            while(first){
                if(first.nodeType == 3){
                    if(first.nodeValue.replace(/^\s+|\s+$/g, "").length>0){
                        isvalid=true;
                        this._sCall("selectElement", [ first ]);
                        break;
                    }
                }else if(first.nodeType == 1){
                    isvalid=true;
                    this._sCall("selectElementChildren", [ first ]);
                    break;
                }
                first = first.nextSibling;
            }
        }else{
            isvalid=true;
            this._sCall("selectElementChildren", [ this.editNode ]);
        }
        if(isvalid){
            this._sCall("collapse", [ true ]);
        }
    },

    placeCursorAtEnd: function(){
        // summary:
        //      place the cursor at the end of the editing area
        this.focus();

        //In mozilla, if last child is not a text node, we have to use
        // selectElementChildren on this.editNode.lastChild otherwise the
        // cursor would be placed at the end of the closing tag of
        //this.editNode.lastChild
        var isvalid=false;
        if(dojo.isMoz){
            var last=this.editNode.lastChild;
            while(last){
                if(last.nodeType == 3){
                    if(last.nodeValue.replace(/^\s+|\s+$/g, "").length>0){
                        isvalid=true;
                        this._sCall("selectElement", [ last ]);
                        break;
                    }
                }else if(last.nodeType == 1){
                    isvalid=true;
                    if(last.lastChild){
                        this._sCall("selectElement", [ last.lastChild ]);
                    }else{
                        this._sCall("selectElement", [ last ]);
                    }
                    break;
                }
                last = last.previousSibling;
            }
        }else{
            isvalid=true;
            this._sCall("selectElementChildren", [ this.editNode ]);
        }
        if(isvalid){
            this._sCall("collapse", [ false ]);
        }
    },

    getValue: function(/*Boolean?*/nonDestructive){
        // summary:
        //      return the current content of the editing area (post filters
        //      are applied)
        //  nonDestructive:
        //      defaults to false. Should the post-filtering be run over a copy
        //      of the live DOM? Most users should pass "true" here unless they
        //      *really* know that none of the installed filters are going to
        //      mess up the editing session.
        if(this.textarea){
            if(this.isClosed || !this.isLoaded){
                return this.textarea.value;
            }
        }

        return this._postFilterContent(null, nonDestructive);
    },
    _getValueAttr: function(){
        // summary: hook to make attr("value") work
        return this.getValue();
    },

    setValue: function(/*String*/html){
        // summary:
        //      This function sets the content. No undo history is preserved.
        if(!this.isLoaded){
            // try again after the editor is finished loading
            this.onLoadDeferred.addCallback(dojo.hitch(this, function(){
                this.setValue(html);
            }));
            return;
        }
        if(this.textarea && (this.isClosed || !this.isLoaded)){
            this.textarea.value=html;
        }else{
            html = this._preFilterContent(html);
            var node = this.isClosed ? this.domNode : this.editNode;
            node.innerHTML = html;
            this._preDomFilterContent(node);
        }
        this.onDisplayChanged();
    },

    replaceValue: function(/*String*/html){
        // summary:
        //      this function set the content while trying to maintain the undo stack
        //      (now only works fine with Moz, this is identical to setValue in all
        //      other browsers)
        if(this.isClosed){
            this.setValue(html);
        }else if(this.window && this.window.getSelection && !dojo.isMoz){ // Safari
            // look ma! it's a totally f'd browser!
            this.setValue(html);
        }else if(this.window && this.window.getSelection){ // Moz
            html = this._preFilterContent(html);
            this.execCommand("selectall");
            if(dojo.isMoz && !html){ html = "&nbsp;" }
            this.execCommand("inserthtml", html);
            this._preDomFilterContent(this.editNode);
        }else if(this.document && this.document.selection){//IE
            //In IE, when the first element is not a text node, say
            //an <a> tag, when replacing the content of the editing
            //area, the <a> tag will be around all the content
            //so for now, use setValue for IE too
            this.setValue(html);
        }
    },

    _preFilterContent: function(/*String*/html){
        // summary:
        //      filter the input before setting the content of the editing
        //      area. DOM pre-filtering may happen after this
        //      string-based filtering takes place but as of 1.2, this is not
        //      gauranteed for operations such as the inserthtml command.
        var ec = html;
        dojo.forEach(this.contentPreFilters, function(ef){ if(ef){ ec = ef(ec); } });
        return ec;
    },
    _preDomFilterContent: function(/*DomNode*/dom){
        // summary:
        //      filter the input's live DOM. All filter operations should be
        //      considered to be "live" and operating on the DOM that the user
        //      will be interacting with in their editing session.
        dom = dom || this.editNode;
        dojo.forEach(this.contentDomPreFilters, function(ef){
            if(ef && dojo.isFunction(ef)){
                ef(dom);
            }
        }, this);
    },

    _postFilterContent: function(
        /*DomNode|DomNode[]|String?*/ dom,
        /*Boolean?*/ nonDestructive){
        // summary:
        //      filter the output after getting the content of the editing area
        //
        //  description:
        //      post-filtering allows plug-ins and users to specify any number
        //      of transforms over the editor's content, enabling many common
        //      use-cases such as transforming absolute to relative URLs (and
        //      vice-versa), ensuring conformance with a particular DTD, etc.
        //      The filters are registered in the contentDomPostFilters and
        //      contentPostFilters arrays. Each item in the
        //      contentDomPostFilters array is a function which takes a DOM
        //      Node or array of nodes as its only argument and returns the
        //      same. It is then passed down the chain for further filtering.
        //      The contentPostFilters array behaves the same way, except each
        //      member operates on strings. Together, the DOM and string-based
        //      filtering allow the full range of post-processing that should
        //      be necessaray to enable even the most agressive of post-editing
        //      conversions to take place.
        //
        //      If nonDestructive is set to "true", the nodes are cloned before
        //      filtering proceeds to avoid potentially destructive transforms
        //      to the content which may still needed to be edited further.
        //      Once DOM filtering has taken place, the serialized version of
        //      the DOM which is passed is run through each of the
        //      contentPostFilters functions.
        //
        //  dom:
        //      a node, set of nodes, which to filter using each of the current
        //      members of the contentDomPostFilters and contentPostFilters arrays.
        //
        //  nonDestructive:
        //      defaults to "false". If true, ensures that filtering happens on
        //      a clone of the passed-in content and not the actual node
        //      itself.
        var ec;
        if(!dojo.isString(dom)){
            dom = dom || this.editNode;
            if(this.contentDomPostFilters.length){
                if(nonDestructive){
                    dom = dojo.clone(dom);
                }
                dojo.forEach(this.contentDomPostFilters, function(ef){
                    dom = ef(dom);
                });
            }
            ec = dijit._editor.getChildrenHtml(dom);
        }else{
            ec = dom;
        }

        if(!dojo.trim(ec.replace(/^\xA0\xA0*/, '').replace(/\xA0\xA0*$/, '')).length){
            ec = "";
        }

        //  if(dojo.isIE){
        //      //removing appended <P>&nbsp;</P> for IE
        //      ec = ec.replace(/(?:<p>&nbsp;</p>[\n\r]*)+$/i,"");
        //  }
        dojo.forEach(this.contentPostFilters, function(ef){
            ec = ef(ec);
        });

        return ec;
    },

    _saveContent: function(/*Event*/e){
        // summary:
        //      Saves the content in an onunload event if the editor has not been closed
        var saveTextarea = dojo.byId(dijit._scopeName + "._editor.RichText.savedContent");
        saveTextarea.value += this._SEPARATOR + this.name + ":" + this.getValue();
    },


    escapeXml: function(/*String*/str, /*Boolean*/noSingleQuotes){
        //summary:
        //      Adds escape sequences for special characters in XML: &<>"'
        //      Optionally skips escapes for single quotes
        str = str.replace(/&/gm, "&amp;").replace(/</gm, "&lt;").replace(/>/gm, "&gt;").replace(/"/gm, "&quot;");
        if(!noSingleQuotes){
            str = str.replace(/'/gm, "&#39;");
        }
        return str; // string
    },

    getNodeHtml: function(/* DomNode */node){
        dojo.deprecated('dijit.Editor::getNodeHtml is deprecated','use dijit._editor.getNodeHtml instead', 2);
        return dijit._editor.getNodeHtml(node); // String
    },

    getNodeChildrenHtml: function(/* DomNode */dom){
        dojo.deprecated('dijit.Editor::getNodeChildrenHtml is deprecated','use dijit._editor.getChildrenHtml instead', 2);
        return dijit._editor.getChildrenHtml(dom);
    },

    close: function(/*Boolean*/save, /*Boolean*/force){
        // summary:
        //      Kills the editor and optionally writes back the modified contents to the
        //      element from which it originated.
        // save:
        //      Whether or not to save the changes. If false, the changes are discarded.
        // force:
        if(this.isClosed){return false; }

        if(!arguments.length){ save = true; }
        this._content = this.getValue();
        var changed = (this.savedContent != this._content);

        // line height is squashed for iframes
        // FIXME: why was this here? if (this.iframe){ this.domNode.style.lineHeight = null; }

        if(this.interval){ clearInterval(this.interval); }

        if(this.textarea){
            var s = this.textarea.style;
            s.position = "";
            s.left = s.top = "";
            if(dojo.isIE){
                s.overflow = this.__overflow;
                this.__overflow = null;
            }
            this.textarea.value = save ? this._content : this.savedContent;
            dojo.destroy(this.domNode);
            this.domNode = this.textarea;
        }else{
            // if(save){
            // why we treat moz differently? comment out to fix #1061
            //      if(dojo.isMoz){
            //          var nc = dojo.doc.createElement("span");
            //          this.domNode.appendChild(nc);
            //          nc.innerHTML = this.editNode.innerHTML;
            //      }else{
            //          this.domNode.innerHTML = this._content;
            //      }
            // }
            this.domNode.innerHTML = save ? this._content : this.savedContent;
        }

        dojo.removeClass(this.domNode, "RichTextEditable");
        this.isClosed = true;
        this.isLoaded = false;
        // FIXME: is this always the right thing to do?
        delete this.editNode;

        if(this.window && this.window._frameElement){
            this.window._frameElement = null;
        }

        this.window = null;
        this.document = null;
        this.editingArea = null;
        this.editorObject = null;

        return changed; // Boolean: whether the content has been modified
    },

    destroyRendering: function(){
        // summary: stub
    },

    destroy: function(){
        this.destroyRendering();
        if(!this.isClosed){ this.close(false); }
        this.inherited(arguments);
    },

    _removeMozBogus: function(/* String */ html){
        return html.replace(/\stype="_moz"/gi, '').replace(/\s_moz_dirty=""/gi, ''); // String
    },
    _removeSafariBogus: function(/* String */ html){
        return html.replace(/\sclass="webkit-block-placeholder"/gi, ''); // String
    },
    _fixContentForMoz: function(/* String */ html){
        // summary:
        //      Moz can not handle strong/em tags correctly, convert them to b/i
        return html.replace(/<(\/)?strong([ \>])/gi, '<$1b$2')
            .replace(/<(\/)?em([ \>])/gi, '<$1i$2' ); // String
    },

    _preFixUrlAttributes: function(/* String */ html){
        return html.replace(/(?:(<a(?=\s).*?\shref=)("|')(.*?)\2)|(?:(<a\s.*?href=)([^"'][^ >]+))/gi,
                '$1$4$2$3$5$2 _djrealurl=$2$3$5$2')
            .replace(/(?:(<img(?=\s).*?\ssrc=)("|')(.*?)\2)|(?:(<img\s.*?src=)([^"'][^ >]+))/gi,
                '$1$4$2$3$5$2 _djrealurl=$2$3$5$2'); // String
    }
});

}

if(!dojo._hasResource["dijit._KeyNavContainer"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._KeyNavContainer"] = true;
dojo.provide("dijit._KeyNavContainer");


dojo.declare("dijit._KeyNavContainer",
    [dijit._Container],
    {

        // summary: A _Container with keyboard navigation of its children.
        // decscription:
        //      To use this mixin, call connectKeyNavHandlers() in
        //      postCreate() and call startupKeyNavChildren() in startup().
        //      It provides normalized keyboard and focusing code for Container
        //      widgets.
/*=====
        // focusedChild: Widget
        //      The currently focused child widget, or null if there isn't one
        focusedChild: null,
=====*/

        // tabIndex: Integer
        //      Tab index of the container; same as HTML tabindex attribute.
        //      Note then when user tabs into the container, focus is immediately
        //      moved to the first item in the container.
        tabIndex: "0",


        _keyNavCodes: {},

        connectKeyNavHandlers: function(/*Array*/ prevKeyCodes, /*Array*/ nextKeyCodes){
            // summary:
            //      Call in postCreate() to attach the keyboard handlers
            //      to the container.
            // preKeyCodes: Array
            //      Key codes for navigating to the previous child.
            // nextKeyCodes: Array
            //      Key codes for navigating to the next child.

            var keyCodes = this._keyNavCodes = {};
            var prev = dojo.hitch(this, this.focusPrev);
            var next = dojo.hitch(this, this.focusNext);
            dojo.forEach(prevKeyCodes, function(code){ keyCodes[code] = prev });
            dojo.forEach(nextKeyCodes, function(code){ keyCodes[code] = next });
            this.connect(this.domNode, "onkeypress", "_onContainerKeypress");
            this.connect(this.domNode, "onfocus", "_onContainerFocus");
        },

        startupKeyNavChildren: function(){
            // summary:
            //      Call in startup() to set child tabindexes to -1
            dojo.forEach(this.getChildren(), dojo.hitch(this, "_startupChild"));
        },

        addChild: function(/*Widget*/ widget, /*int?*/ insertIndex){
            // summary: Add a child to our _Container
            dijit._KeyNavContainer.superclass.addChild.apply(this, arguments);
            this._startupChild(widget);
        },

        focus: function(){
            // summary: Default focus() implementation: focus the first child.
            this.focusFirstChild();
        },

        focusFirstChild: function(){
            // summary: Focus the first focusable child in the container.
            this.focusChild(this._getFirstFocusableChild());
        },

        focusNext: function(){
            // summary: Focus the next widget or focal node (for widgets
            //      with multiple focal nodes) within this container.
            if(this.focusedChild && this.focusedChild.hasNextFocalNode
                    && this.focusedChild.hasNextFocalNode()){
                this.focusedChild.focusNext();
                return;
            }
            var child = this._getNextFocusableChild(this.focusedChild, 1);
            if(child.getFocalNodes){
                this.focusChild(child, child.getFocalNodes()[0]);
            }else{
                this.focusChild(child);
            }
        },

        focusPrev: function(){
            // summary: Focus the previous widget or focal node (for widgets
            //      with multiple focal nodes) within this container.
            if(this.focusedChild && this.focusedChild.hasPrevFocalNode
                    && this.focusedChild.hasPrevFocalNode()){
                this.focusedChild.focusPrev();
                return;
            }
            var child = this._getNextFocusableChild(this.focusedChild, -1);
            if(child.getFocalNodes){
                var nodes = child.getFocalNodes();
                this.focusChild(child, nodes[nodes.length-1]);
            }else{
                this.focusChild(child);
            }
        },

        focusChild: function(/*Widget*/ widget, /*Node?*/ node){
            // summary: Focus widget. Optionally focus 'node' within widget.
            if(widget){
                if(this.focusedChild && widget !== this.focusedChild){
                    this._onChildBlur(this.focusedChild);
                }
                this.focusedChild = widget;
                if(node && widget.focusFocalNode){
                    widget.focusFocalNode(node);
                }else{
                    widget.focus();
                }
            }
        },

        _startupChild: function(/*Widget*/ widget){
            // summary:
            //      Set tabindex="-1" on focusable widgets so that we
            //      can focus them programmatically and by clicking.
            //      Connect focus and blur handlers.
            if(widget.getFocalNodes){
                dojo.forEach(widget.getFocalNodes(), function(node){
                    dojo.attr(node, "tabindex", -1);
                    this._connectNode(node);
                }, this);
            }else{
                var node = widget.focusNode || widget.domNode;
                if(widget.isFocusable()){
                    dojo.attr(node, "tabindex", -1);
                }
                this._connectNode(node);
            }
        },

        _connectNode: function(/*Element*/ node){
            this.connect(node, "onfocus", "_onNodeFocus");
            this.connect(node, "onblur", "_onNodeBlur");
        },

        _onContainerFocus: function(evt){
            // Initially the container itself has a tabIndex, but when it gets
            // focus, switch focus to first child...
            // Note that we can't use _onFocus() because switching focus from the
            // _onFocus() handler confuses the focus.js code
            // (because it causes _onFocusNode() to be called recursively)

            // focus bubbles on Firefox,
            // so just make sure that focus has really gone to the container
            if(evt.target !== this.domNode){ return; }

            this.focusFirstChild();

            // and then remove the container's tabIndex,
            // so that tab or shift-tab will go to the fields after/before
            // the container, rather than the container itself
            dojo.removeAttr(this.domNode, "tabIndex");
        },

        _onBlur: function(evt){
            // When focus is moved away the container, and it's descendant (popup) widgets,
            // then restore the container's tabIndex so that user can tab to it again.
            // Note that using _onBlur() so that this doesn't happen when focus is shifted
            // to one of my child widgets (typically a popup)
            if(this.tabIndex){
                dojo.attr(this.domNode, "tabindex", this.tabIndex);
            }
        },

        _onContainerKeypress: function(evt){
            if(evt.ctrlKey || evt.altKey){ return; }
            var func = this._keyNavCodes[evt.charOrCode];
            if(func){
                func();
                dojo.stopEvent(evt);
            }
        },

        _onNodeFocus: function(evt){
            // record the child that has been focused
            var widget = dijit.getEnclosingWidget(evt.target);
            if(widget && widget.isFocusable()){
                this.focusedChild = widget;
            }
            dojo.stopEvent(evt);
        },

        _onNodeBlur: function(evt){
            dojo.stopEvent(evt);
        },

        _onChildBlur: function(/*Widget*/ widget){
            // summary:
            //      Called when focus leaves a child widget to go
            //      to a sibling widget.
        },

        _getFirstFocusableChild: function(){
            return this._getNextFocusableChild(null, 1);
        },

        _getNextFocusableChild: function(child, dir){
            if(child){
                child = this._getSiblingOfChild(child, dir);
            }
            var children = this.getChildren();
            for(var i=0; i < children.length; i++){
                if(!child){
                    child = children[(dir>0) ? 0 : (children.length-1)];
                }
                if(child.isFocusable()){
                    return child;
                }
                child = this._getSiblingOfChild(child, dir);
            }
            // no focusable child found
            return null;
        }
    }
);

}

if(!dojo._hasResource["dijit.ToolbarSeparator"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.ToolbarSeparator"] = true;
dojo.provide("dijit.ToolbarSeparator");




dojo.declare("dijit.ToolbarSeparator",
        [ dijit._Widget, dijit._Templated ],
        {
        // summary: A spacer between two Toolbar items
        templateString: '<div class="dijitToolbarSeparator dijitInline"></div>',
        postCreate: function(){ dojo.setSelectable(this.domNode, false); },
        isFocusable: function(){
            // summary: This widget isn't focusable, so pass along that fact.
            return false;
        }

    });



}

if(!dojo._hasResource["dijit.Toolbar"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.Toolbar"] = true;
dojo.provide("dijit.Toolbar");





dojo.declare("dijit.Toolbar",
    [dijit._Widget, dijit._Templated, dijit._KeyNavContainer],
    {
    // summary: A Toolbar widget, used to hold things like dijit.Editor buttons

    templateString:
        '<div class="dijit dijitToolbar" waiRole="toolbar" tabIndex="${tabIndex}" dojoAttachPoint="containerNode">' +
        //  '<table style="table-layout: fixed" class="dijitReset dijitToolbarTable">' + // factor out style
        //      '<tr class="dijitReset" dojoAttachPoint="containerNode"></tr>'+
        //  '</table>' +
        '</div>',

    postCreate: function(){
        this.connectKeyNavHandlers(
            this.isLeftToRight() ? [dojo.keys.LEFT_ARROW] : [dojo.keys.RIGHT_ARROW],
            this.isLeftToRight() ? [dojo.keys.RIGHT_ARROW] : [dojo.keys.LEFT_ARROW]
        );
    },

    startup: function(){
        if(this._started){ return; }

        this.startupKeyNavChildren();

        this.inherited(arguments);
    }
}
);

// For back-compat, remove for 2.0


}

if(!dojo._hasResource["dijit.form.Button"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.form.Button"] = true;
dojo.provide("dijit.form.Button");




dojo.declare("dijit.form.Button",
    dijit.form._FormWidget,
    {
    // summary:
    //      Basically the same thing as a normal HTML button, but with special styling.
    // description:
    //      Buttons can display a label, an icon, or both.
    //      A label should always be specified (through innerHTML) or the label
    //      attribute.  It can be hidden via showLabel=false.
    // example:
    // |    <button dojoType="dijit.form.Button" onClick="...">Hello world</button>
    //
    // example:
    // |    var button1 = new dijit.form.Button({label: "hello world", onClick: foo});
    // |    dojo.body().appendChild(button1.domNode);

    // label: HTML String
    //      Text to display in button.
    //      If the label is hidden (showLabel=false) then and no title has
    //      been specified, then label is also set as title attribute of icon.
    label: "",

    // showLabel: Boolean
    //      Set this to true to hide the label text and display only the icon.
    //      (If showLabel=false then iconClass must be specified.)
    //      Especially useful for toolbars.
    //      If showLabel=true, the label will become the title (a.k.a. tooltip/hint) of the icon.
    //
    //      The exception case is for computers in high-contrast mode, where the label
    //      will still be displayed, since the icon doesn't appear.
    showLabel: true,

    // iconClass: String
    //      Class to apply to div in button to make it display an icon
    iconClass: "",

    type: "button",
    baseClass: "dijitButton",
    templateString:"<span class=\"dijit dijitReset dijitLeft dijitInline\"\n\tdojoAttachEvent=\"ondijitclick:_onButtonClick,onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\"\n\t><span class=\"dijitReset dijitRight dijitInline\"\n\t\t><span class=\"dijitReset dijitInline dijitButtonNode\"\n\t\t\t><button class=\"dijitReset dijitStretch dijitButtonContents\"\n\t\t\t\tdojoAttachPoint=\"titleNode,focusNode\" \n\t\t\t\ttype=\"${type}\" value=\"${value}\" waiRole=\"button\" waiState=\"labelledby-${id}_label\"\n\t\t\t\t><span class=\"dijitReset dijitInline\" dojoAttachPoint=\"iconNode\" \n\t\t\t\t\t><span class=\"dijitReset dijitToggleButtonIconChar\">&#10003;</span \n\t\t\t\t></span \n\t\t\t\t><span class=\"dijitReset dijitInline dijitButtonText\" \n\t\t\t\t\tid=\"${id}_label\"  \n\t\t\t\t\tdojoAttachPoint=\"containerNode\"\n\t\t\t\t></span\n\t\t\t></button\n\t\t></span\n\t></span\n></span>\n",

    attributeMap: dojo.delegate(dijit.form._FormWidget.prototype.attributeMap, {
        label: { node: "containerNode", type: "innerHTML" },
        iconClass: { node: "iconNode", type: "class" }
    }),


    _onClick: function(/*Event*/ e){
        // summary: internal function to handle click actions
        if(this.disabled || this.readOnly){
            return false;
        }
        this._clicked(); // widget click actions
        return this.onClick(e); // user click actions
    },

    _onButtonClick: function(/*Event*/ e){
        // summary: callback when the user activates the button portion
        // if is activated via a keystroke, stop the event unless is submit or reset
        if(e.type!='click' && !(this.type=="submit" || this.type=="reset")){
            dojo.stopEvent(e);
        }
        if(this._onClick(e) === false){ // returning nothing is same as true
            e.preventDefault(); // needed for checkbox
        }else if(this.type=="submit" && !this.focusNode.form){ // see if a nonform widget needs to be signalled
            for(var node=this.domNode; node.parentNode/*#5935*/; node=node.parentNode){
                var widget=dijit.byNode(node);
                if(widget && typeof widget._onSubmit == "function"){
                    widget._onSubmit(e);
                    break;
                }
            }
        }
    },

    _setValueAttr: function(/*String*/ value){
        // Verify that value cannot be set for BUTTON elements.
        var attr = this.attributeMap.value || '';
        if(this[attr.node||attr||'domNode'].tagName == 'BUTTON'){
            // On IE, setting value actually overrides innerHTML, so disallow for everyone for consistency
            if(value != this.value){
                console.debug('Cannot change the value attribute on a Button widget.');
            }
        }
    },

    _fillContent: function(/*DomNode*/ source){
        // summary:
        //      If button label is specified as srcNodeRef.innerHTML rather than
        //      this.params.label, handle it here.
        if(source && !("label" in this.params)){
            this.attr('label', source.innerHTML);
        }
    },

    postCreate: function(){
        if (this.showLabel == false){
            dojo.addClass(this.containerNode,"dijitDisplayNone");
        }
        dojo.setSelectable(this.focusNode, false);
        this.inherited(arguments);
    },

    onClick: function(/*Event*/ e){
        // summary: user callback for when button is clicked
        //      if type="submit", return true to perform submit
        return true;
    },

    _clicked: function(/*Event*/ e){
        // summary: internal replaceable function for when the button is clicked
    },

    setLabel: function(/*String*/ content){
        dojo.deprecated("dijit.form.Button.setLabel() is deprecated.  Use attr('label', ...) instead.", "", "2.0");
        this.attr("label", content);
    },
    _setLabelAttr: function(/*String*/ content){
        // summary:
        //      Hook for attr('label', ...) to work.
        // description:
        //      Set the label (text) of the button; takes an HTML string.
        this.containerNode.innerHTML = this.label = content;
        this._layoutHack();
        if (this.showLabel == false && !this.params.title){
            this.titleNode.title = dojo.trim(this.containerNode.innerText || this.containerNode.textContent || '');
        }
    }
});


dojo.declare("dijit.form.DropDownButton", [dijit.form.Button, dijit._Container], {
    // summary: A button with a popup
    //
    // example:
    // |    <button dojoType="dijit.form.DropDownButton" label="Hello world">
    // |        <div dojotype="dijit.Menu">...</div>
    // |    </button>
    //
    // example:
    // |    var button1 = new dijit.form.DropDownButton({ label: "hi", dropDown: new dijit.Menu(...) });
    // |    dojo.body().appendChild(button1);
    //

    baseClass : "dijitDropDownButton",

    templateString:"<span class=\"dijit dijitReset dijitLeft dijitInline\"\n\tdojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse,onclick:_onDropDownClick,onkeydown:_onDropDownKeydown,onblur:_onDropDownBlur,onkeypress:_onKey\"\n\t><span class='dijitReset dijitRight dijitInline'\n\t\t><span class='dijitReset dijitInline dijitButtonNode'\n\t\t\t><button class=\"dijitReset dijitStretch dijitButtonContents\" \n\t\t\t\ttype=\"${type}\" value=\"${value}\"\n\t\t\t\tdojoAttachPoint=\"focusNode,titleNode\" \n\t\t\t\twaiRole=\"button\" waiState=\"haspopup-true,labelledby-${id}_label\"\n\t\t\t\t><span class=\"dijitReset dijitInline\" \n\t\t\t\t\tdojoAttachPoint=\"iconNode\"\n\t\t\t\t></span\n\t\t\t\t><span class=\"dijitReset dijitInline dijitButtonText\"  \n\t\t\t\t\tdojoAttachPoint=\"containerNode,popupStateNode\" \n\t\t\t\t\tid=\"${id}_label\"\n\t\t\t\t></span\n\t\t\t\t><span class=\"dijitReset dijitInline dijitArrowButtonInner\">&thinsp;</span\n\t\t\t\t><span class=\"dijitReset dijitInline dijitArrowButtonChar\">&#9660;</span\n\t\t\t></button\n\t\t></span\n\t></span\n></span>\n",

    _fillContent: function(){
        // my inner HTML contains both the button contents and a drop down widget, like
        // <DropDownButton>  <span>push me</span>  <Menu> ... </Menu> </DropDownButton>
        // The first node is assumed to be the button content. The widget is the popup.
        if(this.srcNodeRef){ // programatically created buttons might not define srcNodeRef
            //FIXME: figure out how to filter out the widget and use all remaining nodes as button
            //  content, not just nodes[0]
            var nodes = dojo.query("*", this.srcNodeRef);
            dijit.form.DropDownButton.superclass._fillContent.call(this, nodes[0]);

            // save pointer to srcNode so we can grab the drop down widget after it's instantiated
            this.dropDownContainer = this.srcNodeRef;
        }
    },

    startup: function(){
        if(this._started){ return; }

        // the child widget from srcNodeRef is the dropdown widget.  Insert it in the page DOM,
        // make it invisible, and store a reference to pass to the popup code.
        if(!this.dropDown){
            var dropDownNode = dojo.query("[widgetId]", this.dropDownContainer)[0];
            this.dropDown = dijit.byNode(dropDownNode);
            delete this.dropDownContainer;
        }
        dijit.popup.prepare(this.dropDown.domNode);

        this.inherited(arguments);
    },

    destroyDescendants: function(){
        if(this.dropDown){
            this.dropDown.destroyRecursive();
            delete this.dropDown;
        }
        this.inherited(arguments);
    },

    _onArrowClick: function(/*Event*/ e){
        // summary: callback when the user mouse clicks on menu popup node
        if(this.disabled || this.readOnly){ return; }
        this._toggleDropDown();
    },

    _onDropDownClick: function(/*Event*/ e){
        // on Firefox 2 on the Mac it is possible to fire onclick
        // by pressing enter down on a second element and transferring
        // focus to the DropDownButton;
        // we want to prevent opening our menu in this situation
        // and only do so if we have seen a keydown on this button;
        // e.detail != 0 means that we were fired by mouse
        var isMacFFlessThan3 = dojo.isFF && dojo.isFF < 3
            && navigator.appVersion.indexOf("Macintosh") != -1;
        if(!isMacFFlessThan3 || e.detail != 0 || this._seenKeydown){
            this._onArrowClick(e);
        }
        this._seenKeydown = false;
    },

    _onDropDownKeydown: function(/*Event*/ e){
        this._seenKeydown = true;
    },

    _onDropDownBlur: function(/*Event*/ e){
        this._seenKeydown = false;
    },

    _onKey: function(/*Event*/ e){
        // summary: callback when the user presses a key on menu popup node
        if(this.disabled || this.readOnly){ return; }
        if(e.charOrCode == dojo.keys.DOWN_ARROW){
            if(!this.dropDown || this.dropDown.domNode.style.visibility=="hidden"){
                dojo.stopEvent(e);
                this._toggleDropDown();
            }
        }
    },

    _onBlur: function(){
        // summary: called magically when focus has shifted away from this widget and it's dropdown
        this._closeDropDown();
        // don't focus on button.  the user has explicitly focused on something else.
        this.inherited(arguments);
    },

    _toggleDropDown: function(){
        // summary: toggle the drop-down widget; if it is up, close it, if not, open it
        if(this.disabled || this.readOnly){ return; }
        dijit.focus(this.popupStateNode);
        var dropDown = this.dropDown;
        if(!dropDown){ return; }
        if(!this._opened){
            // If there's an href, then load that first, so we don't get a flicker
            if(dropDown.href && !dropDown.isLoaded){
                var self = this;
                var handler = dojo.connect(dropDown, "onLoad", function(){
                    dojo.disconnect(handler);
                    self._openDropDown();
                });
                dropDown.refresh();
                return;
            }else{
                this._openDropDown();
            }
        }else{
            this._closeDropDown();
        }
    },

    _openDropDown: function(){
        var dropDown = this.dropDown;
        var oldWidth=dropDown.domNode.style.width;
        var self = this;

        dijit.popup.open({
            parent: this,
            popup: dropDown,
            around: this.domNode,
            orient:
                // TODO: add user-defined positioning option, like in Tooltip.js
                this.isLeftToRight() ? {'BL':'TL', 'BR':'TR', 'TL':'BL', 'TR':'BR'}
                : {'BR':'TR', 'BL':'TL', 'TR':'BR', 'TL':'BL'},
            onExecute: function(){
                self._closeDropDown(true);
            },
            onCancel: function(){
                self._closeDropDown(true);
            },
            onClose: function(){
                dropDown.domNode.style.width = oldWidth;
                self.popupStateNode.removeAttribute("popupActive");
                self._opened = false;
            }
        });
        if(this.domNode.offsetWidth > dropDown.domNode.offsetWidth){
            var adjustNode = null;
            if(!this.isLeftToRight()){
                adjustNode = dropDown.domNode.parentNode;
                var oldRight = adjustNode.offsetLeft + adjustNode.offsetWidth;
            }
            // make menu at least as wide as the button
            dojo.marginBox(dropDown.domNode, {w: this.domNode.offsetWidth});
            if(adjustNode){
                adjustNode.style.left = oldRight - this.domNode.offsetWidth + "px";
            }
        }
        this.popupStateNode.setAttribute("popupActive", "true");
        this._opened=true;
        if(dropDown.focus){
            dropDown.focus();
        }
        // TODO: set this.checked and call setStateClass(), to affect button look while drop down is shown
    },

    _closeDropDown: function(/*Boolean*/ focus){
        if(this._opened){
            dijit.popup.close(this.dropDown);
            if(focus){ this.focus(); }
            this._opened = false;
        }
    }
});

dojo.declare("dijit.form.ComboButton", dijit.form.DropDownButton, {
    // summary: A Normal Button with a DropDown
    //
    // example:
    // |    <button dojoType="dijit.form.ComboButton" onClick="...">
    // |        <span>Hello world</span>
    // |        <div dojoType="dijit.Menu">...</div>
    // |    </button>
    //
    // example:
    // |    var button1 = new dijit.form.ComboButton({label: "hello world", onClick: foo, dropDown: "myMenu"});
    // |    dojo.body().appendChild(button1.domNode);
    //

    templateString:"<table class='dijit dijitReset dijitInline dijitLeft'\n\tcellspacing='0' cellpadding='0' waiRole=\"presentation\"\n\t><tbody waiRole=\"presentation\"><tr waiRole=\"presentation\"\n\t\t><td class=\"dijitReset dijitStretch dijitButtonContents dijitButtonNode\"\n\t\t\tdojoAttachEvent=\"ondijitclick:_onButtonClick,onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\"  dojoAttachPoint=\"titleNode\"\n\t\t\twaiRole=\"button\" waiState=\"labelledby-${id}_label\"\n\t\t\t><div class=\"dijitReset dijitInline\" dojoAttachPoint=\"iconNode\" waiRole=\"presentation\"></div\n\t\t\t><div class=\"dijitReset dijitInline dijitButtonText\" id=\"${id}_label\" dojoAttachPoint=\"containerNode\" waiRole=\"presentation\"></div\n\t\t></td\n\t\t><td class='dijitReset dijitRight dijitButtonNode dijitArrowButton dijitDownArrowButton'\n\t\t\tdojoAttachPoint=\"popupStateNode,focusNode\"\n\t\t\tdojoAttachEvent=\"ondijitclick:_onArrowClick, onkeypress:_onKey,onmouseenter:_onMouse,onmouseleave:_onMouse\"\n\t\t\tstateModifier=\"DownArrow\"\n\t\t\ttitle=\"${optionsTitle}\"\n\t\t\twaiRole=\"button\" waiState=\"haspopup-true\"\n\t\t\t><div class=\"dijitReset dijitArrowButtonInner\" waiRole=\"presentation\">&thinsp;</div\n\t\t\t><div class=\"dijitReset dijitArrowButtonChar\" waiRole=\"presentation\">&#9660;</div\n\t\t></td\n\t></tr></tbody\n></table>\n",

    attributeMap: dojo.mixin(dojo.clone(dijit.form.Button.prototype.attributeMap), {
        id:"",
        tabIndex: ["focusNode", "titleNode"]
    }),

    // optionsTitle: String
    //  text that describes the options menu (accessibility)
    optionsTitle: "",

    baseClass: "dijitComboButton",

    _focusedNode: null,

    postCreate: function(){
        this.inherited(arguments);
        this._focalNodes = [this.titleNode, this.popupStateNode];
        dojo.forEach(this._focalNodes, dojo.hitch(this, function(node){
            if(dojo.isIE){
                this.connect(node, "onactivate", this._onNodeFocus);
                this.connect(node, "ondeactivate", this._onNodeBlur);
            }else{
                this.connect(node, "onfocus", this._onNodeFocus);
                this.connect(node, "onblur", this._onNodeBlur);
            }
        }));
    },

    focusFocalNode: function(node){
        // summary: Focus the focal node node.
        this._focusedNode = node;
        dijit.focus(node);
    },

    hasNextFocalNode: function(){
        // summary: Returns true if this widget has no node currently
        //      focused or if there is a node following the focused one.
        //      False is returned if the last node has focus.
        return this._focusedNode !== this.getFocalNodes()[1];
    },

    focusNext: function(){
        // summary: Focus the focal node following the current node with focus
        //      or the first one if no node currently has focus.
        this._focusedNode = this.getFocalNodes()[this._focusedNode ? 1 : 0];
        dijit.focus(this._focusedNode);
    },

    hasPrevFocalNode: function(){
        // summary: Returns true if this widget has no node currently
        //      focused or if there is a node before the focused one.
        //      False is returned if the first node has focus.
        return this._focusedNode !== this.getFocalNodes()[0];
    },

    focusPrev: function(){
        // summary: Focus the focal node before the current node with focus
        //      or the last one if no node currently has focus.
        this._focusedNode = this.getFocalNodes()[this._focusedNode ? 0 : 1];
        dijit.focus(this._focusedNode);
    },

    getFocalNodes: function(){
        // summary: Returns an array of focal nodes for this widget.
        return this._focalNodes;
    },

    _onNodeFocus: function(evt){
        this._focusedNode = evt.currentTarget;
        var fnc = this._focusedNode == this.focusNode ? "dijitDownArrowButtonFocused" : "dijitButtonContentsFocused";
        dojo.addClass(this._focusedNode, fnc);
    },

    _onNodeBlur: function(evt){
        var fnc = evt.currentTarget == this.focusNode ? "dijitDownArrowButtonFocused" : "dijitButtonContentsFocused";
        dojo.removeClass(evt.currentTarget, fnc);
    },

    _onBlur: function(){
        this.inherited(arguments);
        this._focusedNode = null;
    }
});

dojo.declare("dijit.form.ToggleButton", dijit.form.Button, {
    // summary:
    //  A button that can be in two states (checked or not).
    //  Can be base class for things like tabs or checkbox or radio buttons

    baseClass: "dijitToggleButton",

    // checked: Boolean
    //      Corresponds to the native HTML <input> element's attribute.
    //      In markup, specified as "checked='checked'" or just "checked".
    //      True if the button is depressed, or the checkbox is checked,
    //      or the radio button is selected, etc.
    checked: false,

    attributeMap: dojo.mixin(dojo.clone(dijit.form.Button.prototype.attributeMap),
        {checked:"focusNode"}),

    _clicked: function(/*Event*/ evt){
        this.attr('checked', !this.checked);
    },

    _setCheckedAttr: function(/*Boolean*/ value){
        this.checked = value;
        dojo.attr(this.focusNode || this.domNode, "checked", value);
        dijit.setWaiState(this.focusNode || this.domNode, "pressed", value);
        this._setStateClass();
        this._handleOnChange(value, true);
    },

    setChecked: function(/*Boolean*/ checked){
        // summary:
        //  Programatically deselect the button
        dojo.deprecated("setChecked("+checked+") is deprecated. Use attr('checked',"+checked+") instead.", "", "2.0");
        this.attr('checked', checked);
    },

    reset: function(){
        this._hasBeenBlurred = false;

        // set checked state to original setting
        this.attr('checked', this.params.checked || false);
    }
});

}

if(!dojo._hasResource["dijit._editor._Plugin"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._editor._Plugin"] = true;
dojo.provide("dijit._editor._Plugin");




dojo.declare("dijit._editor._Plugin", null, {
    // summary
    //      This represents a "plugin" to the editor, which is basically
    //      a single button on the Toolbar and some associated code
    constructor: function(/*Object?*/args, /*DomNode?*/node){
        if(args){
            dojo.mixin(this, args);
        }
        this._connects=[];
    },

    editor: null,
    iconClassPrefix: "dijitEditorIcon",
    button: null,
    queryCommand: null,
    command: "",
    commandArg: null,
    useDefaultCommand: true,
    buttonClass: dijit.form.Button,
    getLabel: function(key){
        return this.editor.commands[key];
    },
    _initButton: function(props){
        if(this.command.length){
            var label = this.getLabel(this.command);
            var className = this.iconClassPrefix+" "+this.iconClassPrefix + this.command.charAt(0).toUpperCase() + this.command.substr(1);
            if(!this.button){
                props = dojo.mixin({
                    label: label,
                    showLabel: false,
                    iconClass: className,
                    dropDown: this.dropDown,
                    tabIndex: "-1"
                }, props || {});
                this.button = new this.buttonClass(props);
            }
        }
    },
    destroy: function(f){
        dojo.forEach(this._connects, dojo.disconnect);
        if(this.dropDown){
            this.dropDown.destroyRecursive();
        }
    },
    connect: function(o, f, tf){
        this._connects.push(dojo.connect(o, f, this, tf));
    },
    updateState: function(){
        var _e = this.editor;
        var _c = this.command;
        if(!_e){ return; }
        if(!_e.isLoaded){ return; }
        if(!_c.length){ return; }
        if(this.button){
            try{
                var enabled = _e.queryCommandEnabled(_c);
                if(this.enabled!==enabled){
                    this.enabled=enabled;
                    this.button.attr('disabled', !enabled);
                }
                if(typeof this.button.checked == 'boolean'){
                    var checked=_e.queryCommandState(_c);
                    if(this.checked!==checked){
                        this.checked=checked;
                        this.button.attr('checked', _e.queryCommandState(_c));
                    }
                }
            }catch(e){
                console.debug(e);
            }
        }
    },
    setEditor: function(/*Widget*/editor){
        // FIXME: detatch from previous editor!!
        this.editor = editor;

        // FIXME: prevent creating this if we don't need to (i.e., editor can't handle our command)
        this._initButton();

        // FIXME: wire up editor to button here!
        if(this.command.length &&
            !this.editor.queryCommandAvailable(this.command)
        ){
            // console.debug("hiding:", this.command);
            if(this.button){
                this.button.domNode.style.display = "none";
            }
        }
        if(this.button && this.useDefaultCommand){
            this.connect(this.button, "onClick",
                dojo.hitch(this.editor, "execCommand", this.command, this.commandArg)
            );
        }
        this.connect(this.editor, "onNormalizedDisplayChanged", "updateState");
    },
    setToolbar: function(/*Widget*/toolbar){
        if(this.button){
            toolbar.addChild(this.button);
        }
        // console.debug("adding", this.button, "to:", toolbar);
    }
});

}

if(!dojo._hasResource["dijit._editor.plugins.EnterKeyHandling"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit._editor.plugins.EnterKeyHandling"] = true;
dojo.provide("dijit._editor.plugins.EnterKeyHandling");

dojo.declare("dijit._editor.plugins.EnterKeyHandling", dijit._editor._Plugin, {
    // summary:
    //      This plugin tries to make all browsers have identical behavior
    //      when the user presses the ENTER key.
    //      Specifically, it fixes the double-spaced line problem on IE.
    // description:
    //      On IE the ENTER key creates a new paragraph, which visually looks
    //      bad (ie, "double-spaced") and is also different than FF, which
    //      makes a <br> in that.
    //
    //      In this plugin's default operation, where blockNodeForEnter==BR, it
    //      makes the Editor on IE appear to work like other browsers, by:
    //          1. changing the CSS for the <p> node to not have top/bottom margins,
    //              thus eliminating the double-spaced appearance.
    //          2. adds the singleLinePsToRegularPs callback when the
    //              editor writes out it's data, in order to convert adjacent <p>
    //              nodes into a single node
    //      There's also a pre-filter to convert a single <p> with <br> line breaks
    //       into separate <p> nodes, to mirror the post-filter.
    //
    //      (Note: originally based on http://bugs.dojotoolkit.org/ticket/2859)
    //
    //      If you set the blockNodeForEnter option to another value, then this
    //      plugin will monitor keystrokes (as they are typed) and apparently
    //      update the editor's content on the fly so that the ENTER key will
    //      either create a new <div>, or a new <p>.
    //
    //      This is useful because in some cases, you need the editor content to be
    //      consistent with the serialized html even while the user is editing
    //      (such as in a collaboration mode extension to the editor).
    //
    //      The handleEnterKey() code was mainly written for the IE double-spacing
    //      issue that is now handled in the pre/post filters.  And it has some
    //      issues... on IE setting blockNodeForEnter to P or BR
    //      causes screen jumps as you type (making it unusable), and on safari
    //      it just has no effect (safari creates a <div> every time the user
    //      hits the enter key).  But apparently useful for case mentioned above.
    //
    //      (Note: originally based on http://bugs.dojotoolkit.org/ticket/1331)

    // blockNodeForEnter: String
    //      this property decides the behavior of Enter key. It can be either P,
    //      DIV, BR, or empty (which means disable this feature). Anything else
    //      will trigger errors.
    blockNodeForEnter: 'BR',

    constructor: function(args){
        if(args){
            dojo.mixin(this,args);
        }
    },
    setEditor: function(editor){
        this.editor = editor;
        if(this.blockNodeForEnter == 'BR'){
            if(dojo.isIE){
                editor.contentDomPreFilters.push(dojo.hitch(this, "regularPsToSingleLinePs"));
                editor.contentDomPostFilters.push(dojo.hitch(this, "singleLinePsToRegularPs"));
                editor.onLoadDeferred.addCallback(dojo.hitch(this, "_fixNewLineBehaviorForIE"));
            }else{
                editor.onLoadDeferred.addCallback(dojo.hitch(this,function(d){
                    try{
                        this.editor.document.execCommand("insertBrOnReturn", false, true);
                    }catch(e){}
                    return d;
                }));
            }
        }else if(this.blockNodeForEnter){
            //add enter key handler
            // FIXME: need to port to the new event code!!
            dojo['require']('dijit._editor.range');
            var h = dojo.hitch(this,this.handleEnterKey);
            editor.addKeyHandler(13, 0, 0, h); //enter
            editor.addKeyHandler(13, 0, 1, h); //shift+enter
            this.connect(this.editor,'onKeyPressed','onKeyPressed');
        }
    },
    connect: function(o,f,tf){
        if(!this._connects){
            this._connects=[];
        }
        this._connects.push(dojo.connect(o,f,this,tf));
    },
    destroy: function(){
        dojo.forEach(this._connects,dojo.disconnect);
        this._connects=[];
    },
    onKeyPressed: function(e){
        if(this._checkListLater){
            if(dojo.withGlobal(this.editor.window, 'isCollapsed', dijit)){
                var liparent=dojo.withGlobal(this.editor.window, 'getAncestorElement', dijit._editor.selection, ['LI']);
                if(!liparent){
                    //circulate the undo detection code by calling RichText::execCommand directly
                    dijit._editor.RichText.prototype.execCommand.call(this.editor, 'formatblock',this.blockNodeForEnter);
                    //set the innerHTML of the new block node
                    var block = dojo.withGlobal(this.editor.window, 'getAncestorElement', dijit._editor.selection, [this.blockNodeForEnter]);
                    if(block){
                        block.innerHTML=this.bogusHtmlContent;
                        if(dojo.isIE){
                            //the following won't work, it will move the caret to the last list item in the previous list
                            /*var newrange = dijit.range.create();
                            newrange.setStart(block.firstChild,0);
                            var selection = dijit.range.getSelection(this.editor.window)
                            selection.removeAllRanges();
                            selection.addRange(newrange);*/
                            //move to the start by move backward one char
                            var r = this.editor.document.selection.createRange();
                            r.move('character',-1);
                            r.select();
                        }
                    }else{
                        alert('onKeyPressed: Can not find the new block node'); //FIXME
                    }
                }else{

                    if(dojo.isMoz){
                        if(liparent.parentNode.parentNode.nodeName=='LI'){
                            liparent=liparent.parentNode.parentNode;
                        }
                    }
                    var fc=liparent.firstChild;
                    if(fc && fc.nodeType==1 && (fc.nodeName=='UL' || fc.nodeName=='OL')){
                        liparent.insertBefore(fc.ownerDocument.createTextNode('\xA0'),fc);
                        var newrange = dijit.range.create();
                        newrange.setStart(liparent.firstChild,0);
                        var selection = dijit.range.getSelection(this.editor.window,true)
                        selection.removeAllRanges();
                        selection.addRange(newrange);
                    }
                }
            }
            this._checkListLater = false;
        }
        if(this._pressedEnterInBlock){
            //the new created is the original current P, so we have previousSibling below
            if(this._pressedEnterInBlock.previousSibling){
                this.removeTrailingBr(this._pressedEnterInBlock.previousSibling);
            }
            delete this._pressedEnterInBlock;
        }
    },
    bogusHtmlContent: '&nbsp;',
    blockNodes: /^(?:P|H1|H2|H3|H4|H5|H6|LI)$/,
    handleEnterKey: function(e){
        // summary:
        //      Manually handle enter key event to make the behavior consistant across
        //      all supported browsers. See property blockNodeForEnter for available options

         // let browser handle this
        // TODO: delete.  this code will never fire because
        // onKeyPress --> handleEnterKey is only called when blockNodeForEnter != null
        if(!this.blockNodeForEnter){ return true; }

        var selection, range, newrange, doc=this.editor.document,br;
        if(e.shiftKey  //shift+enter always generates <br>
            || this.blockNodeForEnter=='BR'){
            // TODO: above condition 'this.blockNodeForEnter=='BR'' is meaningless,
            // onKeyPress --> handleEnterKey is only called when blockNodeForEnter != BR
            var parent = dojo.withGlobal(this.editor.window, "getParentElement", dijit._editor.selection);
            var header = dijit.range.getAncestor(parent,this.blockNodes);
            if(header){
                if(!e.shiftKey && header.tagName=='LI'){
                    return true; //let brower handle
                }
                selection = dijit.range.getSelection(this.editor.window);
                range = selection.getRangeAt(0);
                if(!range.collapsed){
                    range.deleteContents();
                }
                if(dijit.range.atBeginningOfContainer(header, range.startContainer, range.startOffset)){
                    if(e.shiftKey){
                        br=doc.createElement('br');
                        newrange = dijit.range.create();
                        header.insertBefore(br,header.firstChild);
                        newrange.setStartBefore(br.nextSibling);
                        selection.removeAllRanges();
                        selection.addRange(newrange);
                    }else{
                        dojo.place(br, header, "before");
                    }
                }else if(dijit.range.atEndOfContainer(header, range.startContainer, range.startOffset)){
                    newrange = dijit.range.create();
                    br=doc.createElement('br');
                    if(e.shiftKey){
                        header.appendChild(br);
                        header.appendChild(doc.createTextNode('\xA0'));
                        newrange.setStart(header.lastChild,0);
                    }else{
                        dojo.place(br, header, "after");
                        newrange.setStartAfter(header);
                    }

                    selection.removeAllRanges();
                    selection.addRange(newrange);
                }else{
                    return true; //let brower handle
                }
            }else{
                //don't change this: do not call this.execCommand, as that may have other logic in subclass
                // FIXME
                dijit._editor.RichText.prototype.execCommand.call(this.editor, 'inserthtml', '<br>');
            }
            return false;
        }
        var _letBrowserHandle = true;
        //blockNodeForEnter is either P or DIV
        //first remove selection
        selection = dijit.range.getSelection(this.editor.window);
        range = selection.getRangeAt(0);
        if(!range.collapsed){
            range.deleteContents();
        }

        var block = dijit.range.getBlockAncestor(range.endContainer, null, this.editor.editNode);
        var blockNode = block.blockNode;

        //if this is under a LI or the parent of the blockNode is LI, just let browser to handle it
        if((this._checkListLater = (blockNode && (blockNode.nodeName == 'LI' || blockNode.parentNode.nodeName == 'LI')))){

            if(dojo.isMoz){
                //press enter in middle of P may leave a trailing <br/>, let's remove it later
                this._pressedEnterInBlock = blockNode;
            }
            //if this li only contains spaces, set the content to empty so the browser will outdent this item
            if(/^(?:\s|&nbsp;)$/.test(blockNode.innerHTML)){
                blockNode.innerHTML='';
            }

            return true;
        }

        //text node directly under body, let's wrap them in a node
        if(!block.blockNode || block.blockNode===this.editor.editNode){
            dijit._editor.RichText.prototype.execCommand.call(this.editor, 'formatblock',this.blockNodeForEnter);
            //get the newly created block node
            // FIXME
            block = {blockNode:dojo.withGlobal(this.editor.window, "getAncestorElement", dijit._editor.selection, [this.blockNodeForEnter]),
                    blockContainer: this.editor.editNode};
            if(block.blockNode){
                if(!(block.blockNode.textContent || block.blockNode.innerHTML).replace(/^\s+|\s+$/g, "").length){
                    this.removeTrailingBr(block.blockNode);
                    return false;
                }
            }else{
                block.blockNode = this.editor.editNode;
            }
            selection = dijit.range.getSelection(this.editor.window);
            range = selection.getRangeAt(0);
        }

        var newblock = doc.createElement(this.blockNodeForEnter);
        newblock.innerHTML=this.bogusHtmlContent;
        this.removeTrailingBr(block.blockNode);
        if(dijit.range.atEndOfContainer(block.blockNode, range.endContainer, range.endOffset)){
            if(block.blockNode === block.blockContainer){
                block.blockNode.appendChild(newblock);
            }else{
                dojo.place(newblock, block.blockNode, "after");
            }
            _letBrowserHandle = false;
            //lets move caret to the newly created block
            newrange = dijit.range.create();
            newrange.setStart(newblock,0);
            selection.removeAllRanges();
            selection.addRange(newrange);
            if(this.editor.height){
                newblock.scrollIntoView(false);
            }
        }else if(dijit.range.atBeginningOfContainer(block.blockNode,
                range.startContainer, range.startOffset)){
            dojo.place(newblock, block.blockNode, block.blockNode === block.blockContainer ? "first" : "before");
            if(newblock.nextSibling && this.editor.height){
                //browser does not scroll the caret position into view, do it manually
                newblock.nextSibling.scrollIntoView(false);
            }
            _letBrowserHandle = false;
        }else{ //press enter in the middle of P
            if(dojo.isMoz){
                //press enter in middle of P may leave a trailing <br/>, let's remove it later
                this._pressedEnterInBlock = block.blockNode;
            }
        }
        return _letBrowserHandle;
    },
    removeTrailingBr: function(container){
        var para = /P|DIV|LI/i.test(container.tagName) ?
            container : dijit._editor.selection.getParentOfType(container,['P','DIV','LI']);

        if(!para){ return; }
        if(para.lastChild){
            if((para.childNodes.length > 1 && para.lastChild.nodeType == 3 && /^[\s\xAD]*$/.test(para.lastChild.nodeValue)) ||
                (para.lastChild && para.lastChild.tagName=='BR')){

                dojo.destroy(para.lastChild);
            }
        }
        if(!para.childNodes.length){
            para.innerHTML=this.bogusHtmlContent;
        }
    },
    _fixNewLineBehaviorForIE: function(d){
        // summary:
        //      Insert CSS so <p> nodes don't have spacing around them,
        //      thus hiding the fact that ENTER key on IE is creating new
        //      paragraphs
        if(this.editor.document.__INSERTED_EDITIOR_NEWLINE_CSS === undefined){
            var lineFixingStyles = "p{margin:0 !important;}";
            var insertCssText = function(
                /*String*/ cssStr,
                /*Document*/ doc,
                /*String*/ URI)
            {
                //  summary:
                //      Attempt to insert CSS rules into the document through inserting a
                //      style element

                // DomNode Style  = insertCssText(String ".dojoMenu {color: green;}"[, DomDoc document, dojo.uri.Uri Url ])
                if(!cssStr){
                    return null; // HTMLStyleElement
                }
                if(!doc){ doc = document; }
//                  if(URI){// fix paths in cssStr
//                      cssStr = dojo.html.fixPathsInCssText(cssStr, URI);
//                  }
                var style = doc.createElement("style");
                style.setAttribute("type", "text/css");
                // IE is b0rken enough to require that we add the element to the doc
                // before changing it's properties
                var head = doc.getElementsByTagName("head")[0];
                if(!head){ // must have a head tag
                    console.debug("No head tag in document, aborting styles");
                    return null;    //  HTMLStyleElement
                }else{
                    head.appendChild(style);
                }
                if(style.styleSheet){// IE
                    var setFunc = function(){
                        try{
                            style.styleSheet.cssText = cssStr;
                        }catch(e){ console.debug(e); }
                    };
                    if(style.styleSheet.disabled){
                        setTimeout(setFunc, 10);
                    }else{
                        setFunc();
                    }
                }else{ // w3c
                    var cssText = doc.createTextNode(cssStr);
                    style.appendChild(cssText);
                }
                return style;   //  HTMLStyleElement
            }
            insertCssText(lineFixingStyles, this.editor.document);
            this.editor.document.__INSERTED_EDITIOR_NEWLINE_CSS = true;
            // this.regularPsToSingleLinePs(this.editNode);
            return d;
        }
        return null;
    },
    regularPsToSingleLinePs: function(element, noWhiteSpaceInEmptyP){
        // summary:
        //      Converts a <p> node containing <br>'s into multiple <p> nodes.
        // description:
        //      See singleLinePsToRegularPs().   This method does the
        //      opposite thing, and is used as a pre-filter when loading the
        //      editor, to mirror the effects of the post-filter at end of edit.
        function wrapLinesInPs(el){
          // move "lines" of top-level text nodes into ps
            function wrapNodes(nodes){
                // nodes are assumed to all be siblings
                var newP = nodes[0].ownerDocument.createElement('p'); // FIXME: not very idiomatic
                nodes[0].parentNode.insertBefore(newP, nodes[0]);
                dojo.forEach(nodes, function(node){
                    newP.appendChild(node);
                });
            }

            var currentNodeIndex = 0;
            var nodesInLine = [];
            var currentNode;
            while(currentNodeIndex < el.childNodes.length){
                currentNode = el.childNodes[currentNodeIndex];
                if( currentNode.nodeType==3 ||  // text node
                    (currentNode.nodeType==1 && currentNode.nodeName!='BR' && dojo.style(currentNode, "display")!="block")
                ){
                    nodesInLine.push(currentNode);
                }else{
                    // hit line delimiter; process nodesInLine if there are any
                    var nextCurrentNode = currentNode.nextSibling;
                    if(nodesInLine.length){
                        wrapNodes(nodesInLine);
                        currentNodeIndex = (currentNodeIndex+1)-nodesInLine.length;
                        if(currentNode.nodeName=="BR"){
                            dojo.destroy(currentNode);
                        }
                    }
                    nodesInLine = [];
                }
                currentNodeIndex++;
            }
            if(nodesInLine.length){ wrapNodes(nodesInLine); }
        }

        function splitP(el){
            // split a paragraph into seperate paragraphs at BRs
            var currentNode = null;
            var trailingNodes = [];
            var lastNodeIndex = el.childNodes.length-1;
            for(var i=lastNodeIndex; i>=0; i--){
                currentNode = el.childNodes[i];
                if(currentNode.nodeName=="BR"){
                    var newP = currentNode.ownerDocument.createElement('p');
                    dojo.place(newP, el, "after");
                    if (trailingNodes.length==0 && i != lastNodeIndex) {
                        newP.innerHTML = "&nbsp;"
                    }
                    dojo.forEach(trailingNodes, function(node){
                        newP.appendChild(node);
                    });
                    dojo.destroy(currentNode);
                    trailingNodes = [];
                }else{
                    trailingNodes.unshift(currentNode);
                }
            }
        }

        var pList = [];
        var ps = element.getElementsByTagName('p');
        dojo.forEach(ps, function(p){ pList.push(p); });
        dojo.forEach(pList, function(p){
            if( (p.previousSibling) &&
                (p.previousSibling.nodeName == 'P' || dojo.style(p.previousSibling, 'display') != 'block')
            ){
                var newP = p.parentNode.insertBefore(this.document.createElement('p'), p);
                // this is essential to prevent IE from losing the P.
                // if it's going to be innerHTML'd later we need
                // to add the &nbsp; to _really_ force the issue
                newP.innerHTML = noWhiteSpaceInEmptyP ? "" : "&nbsp;";
            }
            splitP(p);
      },this.editor);
        wrapLinesInPs(element);
        return element;
    },

    singleLinePsToRegularPs: function(element){
        // summary:
        //      Called as post-filter.
        //      Apparently collapses adjacent <p> nodes into a single <p>
        //      nodes with <br> separating each line.
        //
        //  example:
        //      Given this input:
        //  |   <p>line 1</p>
        //  |   <p>line 2</p>
        //  |   <ol>
        //  |       <li>item 1
        //  |       <li>item 2
        //  |   </ol>
        //  |   <p>line 3</p>
        //  |   <p>line 4</p>
        //
        //      Will convert to:
        //  |   <p>line 1<br>line 2</p>
        //  |   <ol>
        //  |       <li>item 1
        //  |       <li>item 2
        //  |   </ol>
        //  |   <p>line 3<br>line 4</p>
        //
        // Not sure why this situation would even come up after the pre-filter and
        // the enter-key-handling code.

        function getParagraphParents(node){
            // summary:
            //      Used to get list of all nodes that contain paragraphs.
            //      Seems like that would just be the very top node itself, but apparently not.
            var ps = node.getElementsByTagName('p');
            var parents = [];
            for(var i=0; i<ps.length; i++){
                var p = ps[i];
                var knownParent = false;
                for(var k=0; k < parents.length; k++){
                    if(parents[k] === p.parentNode){
                        knownParent = true;
                        break;
                    }
                }
                if(!knownParent){
                    parents.push(p.parentNode);
                }
            }
            return parents;
        }

        function isParagraphDelimiter(node){
            if(node.nodeType != 1 || node.tagName != 'P'){
                return dojo.style(node, 'display') == 'block';
            }else{
                if(!node.childNodes.length || node.innerHTML=="&nbsp;"){ return true; }
                //return node.innerHTML.match(/^(<br\ ?\/?>| |\&nbsp\;)$/i);
            }
            return false;
        }

        var paragraphContainers = getParagraphParents(element);
        for(var i=0; i<paragraphContainers.length; i++){
            var container = paragraphContainers[i];
            var firstPInBlock = null;
            var node = container.firstChild;
            var deleteNode = null;
            while(node){
                if(node.nodeType != "1" || node.tagName != 'P'){
                    firstPInBlock = null;
                }else if (isParagraphDelimiter(node)){
                    deleteNode = node;
                    firstPInBlock = null;
                }else{
                    if(firstPInBlock == null){
                        firstPInBlock = node;
                    }else{
                        if( (!firstPInBlock.lastChild || firstPInBlock.lastChild.nodeName != 'BR') &&
                            (node.firstChild) &&
                            (node.firstChild.nodeName != 'BR')
                        ){
                            firstPInBlock.appendChild(this.editor.document.createElement('br'));
                        }
                        while(node.firstChild){
                            firstPInBlock.appendChild(node.firstChild);
                        }
                        deleteNode = node;
                    }
                }
                node = node.nextSibling;
                if(deleteNode){
                    dojo.destroy(deleteNode);
                    deleteNode = null;
                }
            }
        }
        return element;
    }
});

}

if(!dojo._hasResource["dijit.Editor"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dijit.Editor"] = true;
dojo.provide("dijit.Editor");










dojo.declare(
    "dijit.Editor",
    dijit._editor.RichText,
    {
        // summary:
        //  A rich text Editing widget
        //
        // description:
        //  This widget provides basic WYSIWYG editing features, based on the browser's
        //  underlying rich text editing capability, accompanied by a toolbar (dijit.Toolbar).
        //  A plugin model is available to extend the editor's capabilities as well as the
        //  the options available in the toolbar.  Content generation may vary across
        //  browsers, and clipboard operations may have different results, to name
        //  a few limitations.  Note: this widget should not be used with the HTML
        //  &lt;TEXTAREA&gt; tag -- see dijit._editor.RichText for details.

        // plugins: Array
        //      a list of plugin names (as strings) or instances (as objects)
        //      for this widget.
        plugins: null,

        // extraPlugins: Array
        //      a list of extra plugin names which will be appended to plugins array
        extraPlugins: null,

        constructor: function(){
            if(!dojo.isArray(this.plugins)){
                this.plugins=["undo","redo","|","cut","copy","paste","|","bold","italic","underline","strikethrough","|",
                "insertOrderedList","insertUnorderedList","indent","outdent","|","justifyLeft","justifyRight","justifyCenter","justifyFull",
                "dijit._editor.plugins.EnterKeyHandling" /*, "createLink"*/];
            }

            this._plugins=[];
            this._editInterval = this.editActionInterval * 1000;

            //IE will always lose focus when other element gets focus, while for FF and safari,
            //when no iframe is used, focus will be lost whenever another element gets focus.
            //For IE, we can connect to onBeforeDeactivate, which will be called right before
            //the focus is lost, so we can obtain the selected range. For other browsers,
            //no equivelent of onBeforeDeactivate, so we need to do two things to make sure
            //selection is properly saved before focus is lost: 1) when user clicks another
            //element in the page, in which case we listen to mousedown on the entire page and
            //see whether user clicks out of a focus editor, if so, save selection (focus will
            //only lost after onmousedown event is fired, so we can obtain correct caret pos.)
            //2) when user tabs away from the editor, which is handled in onKeyDown below.
            if(dojo.isIE){
                this.events.push("onBeforeDeactivate");
            }
        },

        postCreate: function(){
            //for custom undo/redo
            if(this.customUndo){
                dojo['require']("dijit._editor.range");
                this._steps=this._steps.slice(0);
                this._undoedSteps=this._undoedSteps.slice(0);
//              this.addKeyHandler('z',this.KEY_CTRL,this.undo);
//              this.addKeyHandler('y',this.KEY_CTRL,this.redo);
            }
            if(dojo.isArray(this.extraPlugins)){
                this.plugins=this.plugins.concat(this.extraPlugins);
            }

//          try{
            this.inherited(arguments);
//          dijit.Editor.superclass.postCreate.apply(this, arguments);

            this.commands = dojo.i18n.getLocalization("dijit._editor", "commands", this.lang);

            if(!this.toolbar){
                // if we haven't been assigned a toolbar, create one
                this.toolbar = new dijit.Toolbar({});
                dojo.place(this.toolbar.domNode, this.editingArea, "before");
            }

            dojo.forEach(this.plugins, this.addPlugin, this);
            this.onNormalizedDisplayChanged(); //update toolbar button status
//          }catch(e){ console.debug(e); }

            this.toolbar.startup();
        },
        destroy: function(){
            dojo.forEach(this._plugins, function(p){
                if(p && p.destroy){
                    p.destroy();
                }
            });
            this._plugins=[];
            this.toolbar.destroyRecursive();
            delete this.toolbar;
            this.inherited(arguments);
        },
        addPlugin: function(/*String||Object*/plugin, /*Integer?*/index){
            //  summary:
            //      takes a plugin name as a string or a plugin instance and
            //      adds it to the toolbar and associates it with this editor
            //      instance. The resulting plugin is added to the Editor's
            //      plugins array. If index is passed, it's placed in the plugins
            //      array at that index. No big magic, but a nice helper for
            //      passing in plugin names via markup.
            //
            //  plugin: String, args object or plugin instance
            //
            //  args: This object will be passed to the plugin constructor
            //
            //  index: Integer
            //      Used when creating an instance from
            //      something already in this.plugins. Ensures that the new
            //      instance is assigned to this.plugins at that index.
            var args=dojo.isString(plugin)?{name:plugin}:plugin;
            if(!args.setEditor){
                var o={"args":args,"plugin":null,"editor":this};
                dojo.publish(dijit._scopeName + ".Editor.getPlugin",[o]);
                if(!o.plugin){
                    var pc = dojo.getObject(args.name);
                    if(pc){
                        o.plugin=new pc(args);
                    }
                }
                if(!o.plugin){
                    console.warn('Cannot find plugin',plugin);
                    return;
                }
                plugin=o.plugin;
            }
            if(arguments.length > 1){
                this._plugins[index] = plugin;
            }else{
                this._plugins.push(plugin);
            }
            plugin.setEditor(this);
            if(dojo.isFunction(plugin.setToolbar)){
                plugin.setToolbar(this.toolbar);
            }
        },
        //the following 3 functions are required to make the editor play nice under a layout widget, see #4070
        startup: function(){
            //console.log('startup',arguments);
        },
        resize: function(){
            dijit.layout._LayoutWidget.prototype.resize.apply(this,arguments);
        },
        layout: function(){
            this.editingArea.style.height=(this._contentBox.h - dojo.marginBox(this.toolbar.domNode).h)+"px";
            if(this.iframe){
                this.iframe.style.height="100%";
            }
            this._layoutMode = true;
        },
        onBeforeDeactivate: function(e){
            if(this.customUndo){
                this.endEditing(true);
            }
            //in IE, the selection will be lost when other elements get focus,
            //let's save focus before the editor is deactivated
            this._saveSelection();
            //console.log('onBeforeDeactivate',this);
        },
        /* beginning of custom undo/redo support */

        // customUndo: Boolean
        //      Whether we shall use custom undo/redo support instead of the native
        //      browser support. By default, we only enable customUndo for IE, as it
        //      has broken native undo/redo support. Note: the implementation does
        //      support other browsers which have W3C DOM2 Range API implemented.
        customUndo: dojo.isIE,

        //  editActionInterval: Integer
        //      When using customUndo, not every keystroke will be saved as a step.
        //      Instead typing (including delete) will be grouped together: after
        //      a user stop typing for editActionInterval seconds, a step will be
        //      saved; if a user resume typing within editActionInterval seconds,
        //      the timeout will be restarted. By default, editActionInterval is 3
        //      seconds.
        editActionInterval: 3,
        beginEditing: function(cmd){
            if(!this._inEditing){
                this._inEditing=true;
                this._beginEditing(cmd);
            }
            if(this.editActionInterval>0){
                if(this._editTimer){
                    clearTimeout(this._editTimer);
                }
                this._editTimer = setTimeout(dojo.hitch(this, this.endEditing), this._editInterval);
            }
        },
        _steps:[],
        _undoedSteps:[],
        execCommand: function(cmd){
            if(this.customUndo && (cmd=='undo' || cmd=='redo')){
                return this[cmd]();
            }else{
                if(this.customUndo){
                    this.endEditing();
                    this._beginEditing();
                }
                try{
                    var r = this.inherited('execCommand', arguments);
                    if(dojo.isWebKit && cmd=='paste' && !r){ //see #4598: safari does not support invoking paste from js
                        throw { code: 1011 }; // throw an object like Mozilla's error
                    }
                }catch(e){
                    //TODO: when else might we get an exception?  Do we need the Mozilla test below?
                    if(e.code == 1011 /* Mozilla: service denied */ && /copy|cut|paste/.test(cmd)){
                        // Warn user of platform limitation.  Cannot programmatically access clipboard. See ticket #4136
                        var sub = dojo.string.substitute,
                            accel = {cut:'X', copy:'C', paste:'V'},
                            isMac = navigator.userAgent.indexOf("Macintosh") != -1;
                        alert(sub(this.commands.systemShortcut,
                            [this.commands[cmd], sub(this.commands[isMac ? 'appleKey' : 'ctrlKey'], [accel[cmd]])]));
                    }
                    r = false;
                }
                if(this.customUndo){
                    this._endEditing();
                }
                return r;
            }
        },
        queryCommandEnabled: function(cmd){
            if(this.customUndo && (cmd=='undo' || cmd=='redo')){
                return cmd=='undo'?(this._steps.length>1):(this._undoedSteps.length>0);
            }else{
                return this.inherited('queryCommandEnabled',arguments);
            }
        },

        focus: function(){
            var restore=0;
            //console.log('focus',dijit._curFocus==this.editNode)
            if(this._savedSelection && dojo.isIE){
                restore = dijit._curFocus!=this.editNode;
            }
            this.inherited(arguments);
            if(restore){
                this._restoreSelection();
            }
        },
        _moveToBookmark: function(b){
            var bookmark=b;
            if(dojo.isIE){
                if(dojo.isArray(b)){//IE CONTROL
                    bookmark=[];
                    dojo.forEach(b,function(n){
                        bookmark.push(dijit.range.getNode(n,this.editNode));
                    },this);
                }
            }else{//w3c range
                var r=dijit.range.create();
                r.setStart(dijit.range.getNode(b.startContainer,this.editNode),b.startOffset);
                r.setEnd(dijit.range.getNode(b.endContainer,this.editNode),b.endOffset);
                bookmark=r;
            }
            dojo.withGlobal(this.window,'moveToBookmark',dijit,[bookmark]);
        },
        _changeToStep: function(from,to){
            this.setValue(to.text);
            var b=to.bookmark;
            if(!b){ return; }
            this._moveToBookmark(b);
        },
        undo: function(){
//          console.log('undo');
            this.endEditing(true);
            var s=this._steps.pop();
            if(this._steps.length>0){
                this.focus();
                this._changeToStep(s,this._steps[this._steps.length-1]);
                this._undoedSteps.push(s);
                this.onDisplayChanged();
                return true;
            }
            return false;
        },
        redo: function(){
//          console.log('redo');
            this.endEditing(true);
            var s=this._undoedSteps.pop();
            if(s && this._steps.length>0){
                this.focus();
                this._changeToStep(this._steps[this._steps.length-1],s);
                this._steps.push(s);
                this.onDisplayChanged();
                return true;
            }
            return false;
        },
        endEditing: function(ignore_caret){
            if(this._editTimer){
                clearTimeout(this._editTimer);
            }
            if(this._inEditing){
                this._endEditing(ignore_caret);
                this._inEditing=false;
            }
        },
        _getBookmark: function(){
            var b=dojo.withGlobal(this.window,dijit.getBookmark);
            var tmp=[];
            if(dojo.isIE){
                if(dojo.isArray(b)){//CONTROL
                    dojo.forEach(b,function(n){
                        tmp.push(dijit.range.getIndex(n,this.editNode).o);
                    },this);
                    b=tmp;
                }
            }else{//w3c range
                tmp=dijit.range.getIndex(b.startContainer,this.editNode).o;
                b={startContainer:tmp,
                    startOffset:b.startOffset,
                    endContainer:b.endContainer===b.startContainer?tmp:dijit.range.getIndex(b.endContainer,this.editNode).o,
                    endOffset:b.endOffset};
            }
            return b;
        },
        _beginEditing: function(cmd){
            if(this._steps.length===0){
                this._steps.push({'text':this.savedContent,'bookmark':this._getBookmark()});
            }
        },
        _endEditing: function(ignore_caret){
            var v=this.getValue(true);

            this._undoedSteps=[];//clear undoed steps
            this._steps.push({text: v, bookmark: this._getBookmark()});
        },
        onKeyDown: function(e){
            //We need to save selection if the user TAB away from this editor
            //no need to call _saveSelection for IE, as that will be taken care of in onBeforeDeactivate
            if(!dojo.isIE && !this.iframe && e.keyCode==dojo.keys.TAB && !this.tabIndent){
                this._saveSelection();
            }
            if(!this.customUndo){
                this.inherited('onKeyDown',arguments);
                return;
            }
            var k = e.keyCode, ks = dojo.keys;
            if(e.ctrlKey && !e.altKey){//undo and redo only if the special right Alt + z/y are not pressed #5892
                if(k == 90 || k == 122){ //z
                    dojo.stopEvent(e);
                    this.undo();
                    return;
                }else if(k == 89 || k == 121){ //y
                    dojo.stopEvent(e);
                    this.redo();
                    return;
                }
            }
            this.inherited('onKeyDown',arguments);

            switch(k){
                    case ks.ENTER:
                    case ks.BACKSPACE:
                    case ks.DELETE:
                        this.beginEditing();
                        break;
                    case 88: //x
                    case 86: //v
                        if(e.ctrlKey && !e.altKey && !e.metaKey){
                            this.endEditing();//end current typing step if any
                            if(e.keyCode == 88){
                                this.beginEditing('cut');
                                //use timeout to trigger after the cut is complete
                                setTimeout(dojo.hitch(this, this.endEditing), 1);
                            }else{
                                this.beginEditing('paste');
                                //use timeout to trigger after the paste is complete
                                setTimeout(dojo.hitch(this, this.endEditing), 1);
                            }
                            break;
                        }
                        //pass through
                    default:
                        if(!e.ctrlKey && !e.altKey && !e.metaKey && (e.keyCode<dojo.keys.F1 || e.keyCode>dojo.keys.F15)){
                            this.beginEditing();
                            break;
                        }
                        //pass through
                    case ks.ALT:
                        this.endEditing();
                        break;
                    case ks.UP_ARROW:
                    case ks.DOWN_ARROW:
                    case ks.LEFT_ARROW:
                    case ks.RIGHT_ARROW:
                    case ks.HOME:
                    case ks.END:
                    case ks.PAGE_UP:
                    case ks.PAGE_DOWN:
                        this.endEditing(true);
                        break;
                    //maybe ctrl+backspace/delete, so don't endEditing when ctrl is pressed
                    case ks.CTRL:
                    case ks.SHIFT:
                    case ks.TAB:
                        break;
                }
        },
        _onBlur: function(){
            //this._saveSelection();
            this.inherited('_onBlur',arguments);
            this.endEditing(true);
        },
        _saveSelection: function(){
            this._savedSelection=this._getBookmark();
            //console.log('save selection',this._savedSelection,this);
        },
        _restoreSelection: function(){
            if(this._savedSelection){
                //only restore the selection if the current range is collapsed
                //if not collapsed, then it means the editor does not lose
                //selection and there is no need to restore it
                //if(dojo.withGlobal(this.window,'isCollapsed',dijit)){
                    //console.log('_restoreSelection true')
                    this._moveToBookmark(this._savedSelection);
                //}
                delete this._savedSelection;
            }
        },
        _onFocus: function(){
            //console.log('_onFocus');
            this._restoreSelection();
            this.inherited(arguments);
        },
        onClick: function(){
            this.endEditing(true);
            this.inherited('onClick',arguments);
        }
        /* end of custom undo/redo support */
    }
);

/* the following code is to registered a handler to get default plugins */
dojo.subscribe(dijit._scopeName + ".Editor.getPlugin",null,function(o){
    if(o.plugin){ return; }
    var args = o.args, p;
    var _p = dijit._editor._Plugin;
    var name = args.name;
    switch(name){
        case "undo": case "redo": case "cut": case "copy": case "paste": case "insertOrderedList":
        case "insertUnorderedList": case "indent": case "outdent": case "justifyCenter":
        case "justifyFull": case "justifyLeft": case "justifyRight": case "delete":
        case "selectAll": case "removeFormat": case "unlink":
        case "insertHorizontalRule":
            p = new _p({ command: name });
            break;

        case "bold": case "italic": case "underline": case "strikethrough":
        case "subscript": case "superscript":
            p = new _p({ buttonClass: dijit.form.ToggleButton, command: name });
            break;
        case "|":
            p = new _p({ button: new dijit.ToolbarSeparator() });
    }
//  console.log('name',name,p);
    o.plugin=p;
});

}

if(!dojo._hasResource["dojo.dnd.common"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.common"] = true;
dojo.provide("dojo.dnd.common");

dojo.dnd._isMac = navigator.appVersion.indexOf("Macintosh") >= 0;
dojo.dnd._copyKey = dojo.dnd._isMac ? "metaKey" : "ctrlKey";

dojo.dnd.getCopyKeyState = function(e) {
    // summary: abstracts away the difference between selection on Mac and PC,
    //  and returns the state of the "copy" key to be pressed.
    // e: Event: mouse event
    return e[dojo.dnd._copyKey];    // Boolean
};

dojo.dnd._uniqueId = 0;
dojo.dnd.getUniqueId = function(){
    // summary: returns a unique string for use with any DOM element
    var id;
    do{
        id = dojo._scopeName + "Unique" + (++dojo.dnd._uniqueId);
    }while(dojo.byId(id));
    return id;
};

dojo.dnd._empty = {};

dojo.dnd.isFormElement = function(/*Event*/ e){
    // summary: returns true, if user clicked on a form element
    var t = e.target;
    if(t.nodeType == 3 /*TEXT_NODE*/){
        t = t.parentNode;
    }
    return " button textarea input select option ".indexOf(" " + t.tagName.toLowerCase() + " ") >= 0;   // Boolean
};

}

if(!dojo._hasResource["dojo.dnd.Container"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Container"] = true;
dojo.provide("dojo.dnd.Container");




/*
    Container states:
        ""      - normal state
        "Over"  - mouse over a container
    Container item states:
        ""      - normal state
        "Over"  - mouse over a container item
*/

dojo.declare("dojo.dnd.Container", null, {
    // summary: a Container object, which knows when mouse hovers over it,
    //  and over which element it hovers

    // object attributes (for markup)
    skipForm: false,

    constructor: function(node, params){
        // summary: a constructor of the Container
        // node: Node: node or node's id to build the container on
        // params: Object: a dict of parameters, recognized parameters are:
        //  creator: Function: a creator function, which takes a data item, and returns an object like that:
        //      {node: newNode, data: usedData, type: arrayOfStrings}
        //  skipForm: Boolean: don't start the drag operation, if clicked on form elements
        //  dropParent: Node: node or node's id to use as the parent node for dropped items
        //      (must be underneath the 'node' parameter in the DOM)
        //  _skipStartup: Boolean: skip startup(), which collects children, for deferred initialization
        //      (this is used in the markup mode)
        this.node = dojo.byId(node);
        if(!params){ params = {}; }
        this.creator = params.creator || null;
        this.skipForm = params.skipForm;
        this.parent = params.dropParent && dojo.byId(params.dropParent);

        // class-specific variables
        this.map = {};
        this.current = null;

        // states
        this.containerState = "";
        dojo.addClass(this.node, "dojoDndContainer");

        // mark up children
        if(!(params && params._skipStartup)){
            this.startup();
        }

        // set up events
        this.events = [
            dojo.connect(this.node, "onmouseover", this, "onMouseOver"),
            dojo.connect(this.node, "onmouseout",  this, "onMouseOut"),
            // cancel text selection and text dragging
            dojo.connect(this.node, "ondragstart",   this, "onSelectStart"),
            dojo.connect(this.node, "onselectstart", this, "onSelectStart")
        ];
    },

    // object attributes (for markup)
    creator: function(){},  // creator function, dummy at the moment

    // abstract access to the map
    getItem: function(/*String*/ key){
        // summary: returns a data item by its key (id)
        return this.map[key];   // Object
    },
    setItem: function(/*String*/ key, /*Object*/ data){
        // summary: associates a data item with its key (id)
        this.map[key] = data;
    },
    delItem: function(/*String*/ key){
        // summary: removes a data item from the map by its key (id)
        delete this.map[key];
    },
    forInItems: function(/*Function*/ f, /*Object?*/ o){
        // summary: iterates over a data map skipping members, which
        //  are present in the empty object (IE and/or 3rd-party libraries).
        o = o || dojo.global;
        var m = this.map, e = dojo.dnd._empty;
        for(var i in m){
            if(i in e){ continue; }
            f.call(o, m[i], i, this);
        }
        return o;   // Object
    },
    clearItems: function(){
        // summary: removes all data items from the map
        this.map = {};
    },

    // methods
    getAllNodes: function(){
        // summary: returns a list (an array) of all valid child nodes
        return dojo.query("> .dojoDndItem", this.parent);   // NodeList
    },
    sync: function(){
        // summary: synch up the node list with the data map
        var map = {};
        this.getAllNodes().forEach(function(node){
            if(node.id){
                var item = this.getItem(node.id);
                if(item){
                    map[node.id] = item;
                    return;
                }
            }else{
                node.id = dojo.dnd.getUniqueId();
            }
            var type = node.getAttribute("dndType"),
                data = node.getAttribute("dndData");
            map[node.id] = {
                data: data || node.innerHTML,
                type: type ? type.split(/\s*,\s*/) : ["text"]
            };
        }, this);
        this.map = map;
        return this;    // self
    },
    insertNodes: function(data, before, anchor){
        // summary: inserts an array of new nodes before/after an anchor node
        // data: Array: a list of data items, which should be processed by the creator function
        // before: Boolean: insert before the anchor, if true, and after the anchor otherwise
        // anchor: Node: the anchor node to be used as a point of insertion
        if(!this.parent.firstChild){
            anchor = null;
        }else if(before){
            if(!anchor){
                anchor = this.parent.firstChild;
            }
        }else{
            if(anchor){
                anchor = anchor.nextSibling;
            }
        }
        if(anchor){
            for(var i = 0; i < data.length; ++i){
                var t = this._normalizedCreator(data[i]);
                this.setItem(t.node.id, {data: t.data, type: t.type});
                this.parent.insertBefore(t.node, anchor);
            }
        }else{
            for(var i = 0; i < data.length; ++i){
                var t = this._normalizedCreator(data[i]);
                this.setItem(t.node.id, {data: t.data, type: t.type});
                this.parent.appendChild(t.node);
            }
        }
        return this;    // self
    },
    destroy: function(){
        // summary: prepares the object to be garbage-collected
        dojo.forEach(this.events, dojo.disconnect);
        this.clearItems();
        this.node = this.parent = this.current = null;
    },

    // markup methods
    markupFactory: function(params, node){
        params._skipStartup = true;
        return new dojo.dnd.Container(node, params);
    },
    startup: function(){
        // summary: collects valid child items and populate the map

        // set up the real parent node
        if(!this.parent){
            // use the standard algorithm, if not assigned
            this.parent = this.node;
            if(this.parent.tagName.toLowerCase() == "table"){
                var c = this.parent.getElementsByTagName("tbody");
                if(c && c.length){ this.parent = c[0]; }
            }
        }
        this.defaultCreator = dojo.dnd._defaultCreator(this.parent);

        // process specially marked children
        this.sync();
    },

    // mouse events
    onMouseOver: function(e){
        // summary: event processor for onmouseover
        // e: Event: mouse event
        var n = e.relatedTarget;
        while(n){
            if(n == this.node){ break; }
            try{
                n = n.parentNode;
            }catch(x){
                n = null;
            }
        }
        if(!n){
            this._changeState("Container", "Over");
            this.onOverEvent();
        }
        n = this._getChildByEvent(e);
        if(this.current == n){ return; }
        if(this.current){ this._removeItemClass(this.current, "Over"); }
        if(n){ this._addItemClass(n, "Over"); }
        this.current = n;
    },
    onMouseOut: function(e){
        // summary: event processor for onmouseout
        // e: Event: mouse event
        for(var n = e.relatedTarget; n;){
            if(n == this.node){ return; }
            try{
                n = n.parentNode;
            }catch(x){
                n = null;
            }
        }
        if(this.current){
            this._removeItemClass(this.current, "Over");
            this.current = null;
        }
        this._changeState("Container", "");
        this.onOutEvent();
    },
    onSelectStart: function(e){
        // summary: event processor for onselectevent and ondragevent
        // e: Event: mouse event
        if(!this.skipForm || !dojo.dnd.isFormElement(e)){
            dojo.stopEvent(e);
        }
    },

    // utilities
    onOverEvent: function(){
        // summary: this function is called once, when mouse is over our container
    },
    onOutEvent: function(){
        // summary: this function is called once, when mouse is out of our container
    },
    _changeState: function(type, newState){
        // summary: changes a named state to new state value
        // type: String: a name of the state to change
        // newState: String: new state
        var prefix = "dojoDnd" + type;
        var state  = type.toLowerCase() + "State";
        //dojo.replaceClass(this.node, prefix + newState, prefix + this[state]);
        dojo.removeClass(this.node, prefix + this[state]);
        dojo.addClass(this.node, prefix + newState);
        this[state] = newState;
    },
    _addItemClass: function(node, type){
        // summary: adds a class with prefix "dojoDndItem"
        // node: Node: a node
        // type: String: a variable suffix for a class name
        dojo.addClass(node, "dojoDndItem" + type);
    },
    _removeItemClass: function(node, type){
        // summary: removes a class with prefix "dojoDndItem"
        // node: Node: a node
        // type: String: a variable suffix for a class name
        dojo.removeClass(node, "dojoDndItem" + type);
    },
    _getChildByEvent: function(e){
        // summary: gets a child, which is under the mouse at the moment, or null
        // e: Event: a mouse event
        var node = e.target;
        if(node){
            for(var parent = node.parentNode; parent; node = parent, parent = node.parentNode){
                if(parent == this.parent && dojo.hasClass(node, "dojoDndItem")){ return node; }
            }
        }
        return null;
    },
    _normalizedCreator: function(item, hint){
        // summary: adds all necessary data to the output of the user-supplied creator function
        var t = (this.creator || this.defaultCreator).call(this, item, hint);
        if(!dojo.isArray(t.type)){ t.type = ["text"]; }
        if(!t.node.id){ t.node.id = dojo.dnd.getUniqueId(); }
        dojo.addClass(t.node, "dojoDndItem");
        return t;
    }
});

dojo.dnd._createNode = function(tag){
    // summary: returns a function, which creates an element of given tag
    //  (SPAN by default) and sets its innerHTML to given text
    // tag: String: a tag name or empty for SPAN
    if(!tag){ return dojo.dnd._createSpan; }
    return function(text){  // Function
        return dojo.create(tag, {innerHTML: text}); // Node
    };
};

dojo.dnd._createTrTd = function(text){
    // summary: creates a TR/TD structure with given text as an innerHTML of TD
    // text: String: a text for TD
    var tr = dojo.create("tr");
    dojo.create("td", {innerHTML: text}, tr);
    return tr;  // Node
};

dojo.dnd._createSpan = function(text){
    // summary: creates a SPAN element with given text as its innerHTML
    // text: String: a text for SPAN
    return dojo.create("span", {innerHTML: text});  // Node
};

// dojo.dnd._defaultCreatorNodes: Object: a dicitionary, which maps container tag names to child tag names
dojo.dnd._defaultCreatorNodes = {ul: "li", ol: "li", div: "div", p: "div"};

dojo.dnd._defaultCreator = function(node){
    // summary: takes a parent node, and returns an appropriate creator function
    // node: Node: a container node
    var tag = node.tagName.toLowerCase();
    var c = tag == "tbody" || tag == "thead" ? dojo.dnd._createTrTd :
            dojo.dnd._createNode(dojo.dnd._defaultCreatorNodes[tag]);
    return function(item, hint){    // Function
        var isObj = item && dojo.isObject(item), data, type, n;
        if(isObj && item.tagName && item.nodeType && item.getAttribute){
            // process a DOM node
            data = item.getAttribute("dndData") || item.innerHTML;
            type = item.getAttribute("dndType");
            type = type ? type.split(/\s*,\s*/) : ["text"];
            n = item;   // this node is going to be moved rather than copied
        }else{
            // process a DnD item object or a string
            data = (isObj && item.data) ? item.data : item;
            type = (isObj && item.type) ? item.type : ["text"];
            n = (hint == "avatar" ? dojo.dnd._createSpan : c)(String(data));
        }
        n.id = dojo.dnd.getUniqueId();
        return {node: n, data: data, type: type};
    };
};

}

if(!dojo._hasResource["dojo.dnd.Selector"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Selector"] = true;
dojo.provide("dojo.dnd.Selector");




/*
    Container item states:
        ""          - an item is not selected
        "Selected"  - an item is selected
        "Anchor"    - an item is selected, and is an anchor for a "shift" selection
*/

dojo.declare("dojo.dnd.Selector", dojo.dnd.Container, {
    // summary: a Selector object, which knows how to select its children

    constructor: function(node, params){
        // summary: a constructor of the Selector
        // node: Node: node or node's id to build the selector on
        // params: Object: a dict of parameters, recognized parameters are:
        //  singular: Boolean
        //      allows selection of only one element, if true
        //      the rest of parameters are passed to the container
        //  autoSync: Boolean
        //      autosynchronizes the source with its list of DnD nodes,
        //      false by default
        if(!params){ params = {}; }
        this.singular = params.singular;
        this.autoSync = params.autoSync;
        // class-specific variables
        this.selection = {};
        this.anchor = null;
        this.simpleSelection = false;
        // set up events
        this.events.push(
            dojo.connect(this.node, "onmousedown", this, "onMouseDown"),
            dojo.connect(this.node, "onmouseup",   this, "onMouseUp"));
    },

    // object attributes (for markup)
    singular: false,    // is singular property

    // methods
    getSelectedNodes: function(){
        // summary: returns a list (an array) of selected nodes
        var t = new dojo.NodeList();
        var e = dojo.dnd._empty;
        for(var i in this.selection){
            if(i in e){ continue; }
            t.push(dojo.byId(i));
        }
        return t;   // Array
    },
    selectNone: function(){
        // summary: unselects all items
        return this._removeSelection()._removeAnchor(); // self
    },
    selectAll: function(){
        // summary: selects all items
        this.forInItems(function(data, id){
            this._addItemClass(dojo.byId(id), "Selected");
            this.selection[id] = 1;
        }, this);
        return this._removeAnchor();    // self
    },
    deleteSelectedNodes: function(){
        // summary: deletes all selected items
        var e = dojo.dnd._empty;
        for(var i in this.selection){
            if(i in e){ continue; }
            var n = dojo.byId(i);
            this.delItem(i);
            dojo.destroy(n);
        }
        this.anchor = null;
        this.selection = {};
        return this;    // self
    },
    forInSelectedItems: function(/*Function*/ f, /*Object?*/ o){
        // summary: iterates over selected items,
        // see dojo.dnd.Container.forInItems() for details
        o = o || dojo.global;
        var s = this.selection, e = dojo.dnd._empty;
        for(var i in s){
            if(i in e){ continue; }
            f.call(o, this.getItem(i), i, this);
        }
    },
    sync: function(){
        // summary: synch up the node list with the data map

        dojo.dnd.Selector.superclass.sync.call(this);

        // fix the anchor
        if(this.anchor){
            if(!this.getItem(this.anchor.id)){
                this.anchor = null;
            }
        }

        // fix the selection
        var t = [], e = dojo.dnd._empty;
        for(var i in this.selection){
            if(i in e){ continue; }
            if(!this.getItem(i)){
                t.push(i);
            }
        }
        dojo.forEach(t, function(i){
            delete this.selection[i];
        }, this);

        return this;    // self
    },
    insertNodes: function(addSelected, data, before, anchor){
        // summary: inserts new data items (see Container's insertNodes method for details)
        // addSelected: Boolean: all new nodes will be added to selected items, if true, no selection change otherwise
        // data: Array: a list of data items, which should be processed by the creator function
        // before: Boolean: insert before the anchor, if true, and after the anchor otherwise
        // anchor: Node: the anchor node to be used as a point of insertion
        var oldCreator = this._normalizedCreator;
        this._normalizedCreator = function(item, hint){
            var t = oldCreator.call(this, item, hint);
            if(addSelected){
                if(!this.anchor){
                    this.anchor = t.node;
                    this._removeItemClass(t.node, "Selected");
                    this._addItemClass(this.anchor, "Anchor");
                }else if(this.anchor != t.node){
                    this._removeItemClass(t.node, "Anchor");
                    this._addItemClass(t.node, "Selected");
                }
                this.selection[t.node.id] = 1;
            }else{
                this._removeItemClass(t.node, "Selected");
                this._removeItemClass(t.node, "Anchor");
            }
            return t;
        };
        dojo.dnd.Selector.superclass.insertNodes.call(this, data, before, anchor);
        this._normalizedCreator = oldCreator;
        return this;    // self
    },
    destroy: function(){
        // summary: prepares the object to be garbage-collected
        dojo.dnd.Selector.superclass.destroy.call(this);
        this.selection = this.anchor = null;
    },

    // markup methods
    markupFactory: function(params, node){
        params._skipStartup = true;
        return new dojo.dnd.Selector(node, params);
    },

    // mouse events
    onMouseDown: function(e){
        // summary: event processor for onmousedown
        // e: Event: mouse event
        if(this.autoSync){ this.sync(); }
        if(!this.current){ return; }
        if(!this.singular && !dojo.dnd.getCopyKeyState(e) && !e.shiftKey && (this.current.id in this.selection)){
            this.simpleSelection = true;
            dojo.stopEvent(e);
            return;
        }
        if(!this.singular && e.shiftKey){
            if(!dojo.dnd.getCopyKeyState(e)){
                this._removeSelection();
            }
            var c = this.getAllNodes();
            if(c.length){
                if(!this.anchor){
                    this.anchor = c[0];
                    this._addItemClass(this.anchor, "Anchor");
                }
                this.selection[this.anchor.id] = 1;
                if(this.anchor != this.current){
                    var i = 0;
                    for(; i < c.length; ++i){
                        var node = c[i];
                        if(node == this.anchor || node == this.current){ break; }
                    }
                    for(++i; i < c.length; ++i){
                        var node = c[i];
                        if(node == this.anchor || node == this.current){ break; }
                        this._addItemClass(node, "Selected");
                        this.selection[node.id] = 1;
                    }
                    this._addItemClass(this.current, "Selected");
                    this.selection[this.current.id] = 1;
                }
            }
        }else{
            if(this.singular){
                if(this.anchor == this.current){
                    if(dojo.dnd.getCopyKeyState(e)){
                        this.selectNone();
                    }
                }else{
                    this.selectNone();
                    this.anchor = this.current;
                    this._addItemClass(this.anchor, "Anchor");
                    this.selection[this.current.id] = 1;
                }
            }else{
                if(dojo.dnd.getCopyKeyState(e)){
                    if(this.anchor == this.current){
                        delete this.selection[this.anchor.id];
                        this._removeAnchor();
                    }else{
                        if(this.current.id in this.selection){
                            this._removeItemClass(this.current, "Selected");
                            delete this.selection[this.current.id];
                        }else{
                            if(this.anchor){
                                this._removeItemClass(this.anchor, "Anchor");
                                this._addItemClass(this.anchor, "Selected");
                            }
                            this.anchor = this.current;
                            this._addItemClass(this.current, "Anchor");
                            this.selection[this.current.id] = 1;
                        }
                    }
                }else{
                    if(!(this.current.id in this.selection)){
                        this.selectNone();
                        this.anchor = this.current;
                        this._addItemClass(this.current, "Anchor");
                        this.selection[this.current.id] = 1;
                    }
                }
            }
        }
        dojo.stopEvent(e);
    },
    onMouseUp: function(e){
        // summary: event processor for onmouseup
        // e: Event: mouse event
        if(!this.simpleSelection){ return; }
        this.simpleSelection = false;
        this.selectNone();
        if(this.current){
            this.anchor = this.current;
            this._addItemClass(this.anchor, "Anchor");
            this.selection[this.current.id] = 1;
        }
    },
    onMouseMove: function(e){
        // summary: event processor for onmousemove
        // e: Event: mouse event
        this.simpleSelection = false;
    },

    // utilities
    onOverEvent: function(){
        // summary: this function is called once, when mouse is over our container
        this.onmousemoveEvent = dojo.connect(this.node, "onmousemove", this, "onMouseMove");
    },
    onOutEvent: function(){
        // summary: this function is called once, when mouse is out of our container
        dojo.disconnect(this.onmousemoveEvent);
        delete this.onmousemoveEvent;
    },
    _removeSelection: function(){
        // summary: unselects all items
        var e = dojo.dnd._empty;
        for(var i in this.selection){
            if(i in e){ continue; }
            var node = dojo.byId(i);
            if(node){ this._removeItemClass(node, "Selected"); }
        }
        this.selection = {};
        return this;    // self
    },
    _removeAnchor: function(){
        if(this.anchor){
            this._removeItemClass(this.anchor, "Anchor");
            this.anchor = null;
        }
        return this;    // self
    }
});

}

if(!dojo._hasResource["dojo.dnd.autoscroll"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.autoscroll"] = true;
dojo.provide("dojo.dnd.autoscroll");

dojo.dnd.getViewport = function(){
    // summary: returns a viewport size (visible part of the window)

    // FIXME: need more docs!!
    var d = dojo.doc, dd = d.documentElement, w = window, b = dojo.body();
    if(dojo.isMozilla){
        return {w: dd.clientWidth, h: w.innerHeight};   // Object
    }else if(!dojo.isOpera && w.innerWidth){
        return {w: w.innerWidth, h: w.innerHeight};     // Object
    }else if (!dojo.isOpera && dd && dd.clientWidth){
        return {w: dd.clientWidth, h: dd.clientHeight}; // Object
    }else if (b.clientWidth){
        return {w: b.clientWidth, h: b.clientHeight};   // Object
    }
    return null;    // Object
};

dojo.dnd.V_TRIGGER_AUTOSCROLL = 32;
dojo.dnd.H_TRIGGER_AUTOSCROLL = 32;

dojo.dnd.V_AUTOSCROLL_VALUE = 16;
dojo.dnd.H_AUTOSCROLL_VALUE = 16;

dojo.dnd.autoScroll = function(e){
    // summary:
    //      a handler for onmousemove event, which scrolls the window, if
    //      necesary
    // e: Event:
    //      onmousemove event

    // FIXME: needs more docs!
    var v = dojo.dnd.getViewport(), dx = 0, dy = 0;
    if(e.clientX < dojo.dnd.H_TRIGGER_AUTOSCROLL){
        dx = -dojo.dnd.H_AUTOSCROLL_VALUE;
    }else if(e.clientX > v.w - dojo.dnd.H_TRIGGER_AUTOSCROLL){
        dx = dojo.dnd.H_AUTOSCROLL_VALUE;
    }
    if(e.clientY < dojo.dnd.V_TRIGGER_AUTOSCROLL){
        dy = -dojo.dnd.V_AUTOSCROLL_VALUE;
    }else if(e.clientY > v.h - dojo.dnd.V_TRIGGER_AUTOSCROLL){
        dy = dojo.dnd.V_AUTOSCROLL_VALUE;
    }
    window.scrollBy(dx, dy);
};

dojo.dnd._validNodes = {"div": 1, "p": 1, "td": 1};
dojo.dnd._validOverflow = {"auto": 1, "scroll": 1};

dojo.dnd.autoScrollNodes = function(e){
    // summary:
    //      a handler for onmousemove event, which scrolls the first avaialble
    //      Dom element, it falls back to dojo.dnd.autoScroll()
    // e: Event:
    //      onmousemove event

    // FIXME: needs more docs!
    for(var n = e.target; n;){
        if(n.nodeType == 1 && (n.tagName.toLowerCase() in dojo.dnd._validNodes)){
            var s = dojo.getComputedStyle(n);
            if(s.overflow.toLowerCase() in dojo.dnd._validOverflow){
                var b = dojo._getContentBox(n, s), t = dojo._abs(n, true);
                //console.debug(b.l, b.t, t.x, t.y, n.scrollLeft, n.scrollTop);
                var w = Math.min(dojo.dnd.H_TRIGGER_AUTOSCROLL, b.w / 2),
                    h = Math.min(dojo.dnd.V_TRIGGER_AUTOSCROLL, b.h / 2),
                    rx = e.pageX - t.x, ry = e.pageY - t.y, dx = 0, dy = 0;
                if(dojo.isWebKit || dojo.isOpera){
                    // FIXME: this code should not be here, it should be taken into account
                    // either by the event fixing code, or the dojo._abs()
                    // FIXME: this code doesn't work on Opera 9.5 Beta
                    rx += dojo.body().scrollLeft, ry += dojo.body().scrollTop;
                }
                if(rx > 0 && rx < b.w){
                    if(rx < w){
                        dx = -w;
                    }else if(rx > b.w - w){
                        dx = w;
                    }
                }
                //console.debug("ry =", ry, "b.h =", b.h, "h =", h);
                if(ry > 0 && ry < b.h){
                    if(ry < h){
                        dy = -h;
                    }else if(ry > b.h - h){
                        dy = h;
                    }
                }
                var oldLeft = n.scrollLeft, oldTop = n.scrollTop;
                n.scrollLeft = n.scrollLeft + dx;
                n.scrollTop  = n.scrollTop  + dy;
                if(oldLeft != n.scrollLeft || oldTop != n.scrollTop){ return; }
            }
        }
        try{
            n = n.parentNode;
        }catch(x){
            n = null;
        }
    }
    dojo.dnd.autoScroll(e);
};

}

if(!dojo._hasResource["dojo.dnd.Avatar"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Avatar"] = true;
dojo.provide("dojo.dnd.Avatar");



dojo.declare("dojo.dnd.Avatar", null, {
    // summary: an object, which represents transferred DnD items visually
    // manager: Object: a DnD manager object

    constructor: function(manager){
        this.manager = manager;
        this.construct();
    },

    // methods
    construct: function(){
        // summary: a constructor function;
        //  it is separate so it can be (dynamically) overwritten in case of need
        var a = dojo.create("table", {
                "class": "dojoDndAvatar",
                style: {
                    position: "absolute",
                    zIndex:   "1999",
                    margin:   "0px"
                }
            }),
            b = dojo.create("tbody", null, a),
            tr = dojo.create("tr", null, b),
            td = dojo.create("td", {
                innerHTML: this._generateText()
            }, tr),
            k = Math.min(5, this.manager.nodes.length), i = 0,
            source = this.manager.source, node;
        // we have to set the opacity on IE only after the node is live
        dojo.attr(tr, {
            "class": "dojoDndAvatarHeader",
            style: {opacity: 0.9}
        });
        for(; i < k; ++i){
            if(source.creator){
                // create an avatar representation of the node
                node = source._normalizedCreator(source.getItem(this.manager.nodes[i].id).data, "avatar").node;
            }else{
                // or just clone the node and hope it works
                node = this.manager.nodes[i].cloneNode(true);
                if(node.tagName.toLowerCase() == "tr"){
                    // insert extra table nodes
                    var table = dojo.create("table"),
                        tbody = dojo.create("tbody", null, table);
                    tbody.appendChild(node);
                    node = table;
                }
            }
            node.id = "";
            tr = dojo.create("tr", null, b);
            td = dojo.create("td", null, tr);
            td.appendChild(node);
            dojo.attr(tr, {
                "class": "dojoDndAvatarItem",
                style: {opacity: (9 - i) / 10}
            });
        }
        this.node = a;
    },
    destroy: function(){
        // summary: a desctructor for the avatar, called to remove all references so it can be garbage-collected
        dojo.destroy(this.node);
        this.node = false;
    },
    update: function(){
        // summary: updates the avatar to reflect the current DnD state
        dojo[(this.manager.canDropFlag ? "add" : "remove") + "Class"](this.node, "dojoDndAvatarCanDrop");
        // replace text
        dojo.query("tr.dojoDndAvatarHeader td", this.node).forEach(function(node){
            node.innerHTML = this._generateText();
        }, this);
    },
    _generateText: function(){
        // summary: generates a proper text to reflect copying or moving of items
        return this.manager.nodes.length.toString();
    }
});

}

if(!dojo._hasResource["dojo.dnd.Manager"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Manager"] = true;
dojo.provide("dojo.dnd.Manager");





dojo.declare("dojo.dnd.Manager", null, {
    // summary: the manager of DnD operations (usually a singleton)
    constructor: function(){
        this.avatar  = null;
        this.source = null;
        this.nodes = [];
        this.copy  = true;
        this.target = null;
        this.canDropFlag = false;
        this.events = [];
    },

    // avatar's offset from the mouse
    OFFSET_X: 16,
    OFFSET_Y: 16,

    // methods
    overSource: function(source){
        // summary: called when a source detected a mouse-over conditiion
        // source: Object: the reporter
        if(this.avatar){
            this.target = (source && source.targetState != "Disabled") ? source : null;
            this.canDropFlag = Boolean(this.target);
            this.avatar.update();
        }
        dojo.publish("/dnd/source/over", [source]);
    },
    outSource: function(source){
        // summary: called when a source detected a mouse-out conditiion
        // source: Object: the reporter
        if(this.avatar){
            if(this.target == source){
                this.target = null;
                this.canDropFlag = false;
                this.avatar.update();
                dojo.publish("/dnd/source/over", [null]);
            }
        }else{
            dojo.publish("/dnd/source/over", [null]);
        }
    },
    startDrag: function(source, nodes, copy){
        // summary: called to initiate the DnD operation
        // source: Object: the source which provides items
        // nodes: Array: the list of transferred items
        // copy: Boolean: copy items, if true, move items otherwise
        this.source = source;
        this.nodes  = nodes;
        this.copy   = Boolean(copy); // normalizing to true boolean
        this.avatar = this.makeAvatar();
        dojo.body().appendChild(this.avatar.node);
        dojo.publish("/dnd/start", [source, nodes, this.copy]);
        this.events = [
            dojo.connect(dojo.doc, "onmousemove", this, "onMouseMove"),
            dojo.connect(dojo.doc, "onmouseup",   this, "onMouseUp"),
            dojo.connect(dojo.doc, "onkeydown",   this, "onKeyDown"),
            dojo.connect(dojo.doc, "onkeyup",     this, "onKeyUp"),
            // cancel text selection and text dragging
            dojo.connect(dojo.doc, "ondragstart",   dojo.stopEvent),
            dojo.connect(dojo.body(), "onselectstart", dojo.stopEvent)
        ];
        var c = "dojoDnd" + (copy ? "Copy" : "Move");
        dojo.addClass(dojo.body(), c);
    },
    canDrop: function(flag){
        // summary: called to notify if the current target can accept items
        var canDropFlag = Boolean(this.target && flag);
        if(this.canDropFlag != canDropFlag){
            this.canDropFlag = canDropFlag;
            this.avatar.update();
        }
    },
    stopDrag: function(){
        // summary: stop the DnD in progress
        dojo.removeClass(dojo.body(), "dojoDndCopy");
        dojo.removeClass(dojo.body(), "dojoDndMove");
        dojo.forEach(this.events, dojo.disconnect);
        this.events = [];
        this.avatar.destroy();
        this.avatar = null;
        this.source = this.target = null;
        this.nodes = [];
    },
    makeAvatar: function(){
        // summary: makes the avatar, it is separate to be overwritten dynamically, if needed
        return new dojo.dnd.Avatar(this);
    },
    updateAvatar: function(){
        // summary: updates the avatar, it is separate to be overwritten dynamically, if needed
        this.avatar.update();
    },

    // mouse event processors
    onMouseMove: function(e){
        // summary: event processor for onmousemove
        // e: Event: mouse event
        var a = this.avatar;
        if(a){
            dojo.dnd.autoScrollNodes(e);
            //dojo.dnd.autoScroll(e);
            var s = a.node.style;
            s.left = (e.pageX + this.OFFSET_X) + "px";
            s.top  = (e.pageY + this.OFFSET_Y) + "px";
            var copy = Boolean(this.source.copyState(dojo.dnd.getCopyKeyState(e)));
            if(this.copy != copy){
                this._setCopyStatus(copy);
            }
        }
    },
    onMouseUp: function(e){
        // summary: event processor for onmouseup
        // e: Event: mouse event
        if(this.avatar && (!("mouseButton" in this.source) ||
                (dojo.isWebKit && dojo.dnd._isMac && this.source.mouseButton == 2 ?
                    e.button == 0 : this.source.mouseButton == e.button))){
            if(this.target && this.canDropFlag){
                var copy = Boolean(this.source.copyState(dojo.dnd.getCopyKeyState(e))),
                params = [this.source, this.nodes, copy, this.target];
                dojo.publish("/dnd/drop/before", params);
                dojo.publish("/dnd/drop", params);
            }else{
                dojo.publish("/dnd/cancel");
            }
            this.stopDrag();
        }
    },

    // keyboard event processors
    onKeyDown: function(e){
        // summary: event processor for onkeydown:
        //  watching for CTRL for copy/move status, watching for ESCAPE to cancel the drag
        // e: Event: keyboard event
        if(this.avatar){
            switch(e.keyCode){
                case dojo.keys.CTRL:
                    var copy = Boolean(this.source.copyState(true));
                    if(this.copy != copy){
                        this._setCopyStatus(copy);
                    }
                    break;
                case dojo.keys.ESCAPE:
                    dojo.publish("/dnd/cancel");
                    this.stopDrag();
                    break;
            }
        }
    },
    onKeyUp: function(e){
        // summary: event processor for onkeyup, watching for CTRL for copy/move status
        // e: Event: keyboard event
        if(this.avatar && e.keyCode == dojo.keys.CTRL){
            var copy = Boolean(this.source.copyState(false));
            if(this.copy != copy){
                this._setCopyStatus(copy);
            }
        }
    },

    // utilities
    _setCopyStatus: function(copy){
        // summary: changes the copy status
        // copy: Boolean: the copy status
        this.copy = copy;
        this.source._markDndStatus(this.copy);
        this.updateAvatar();
        dojo.removeClass(dojo.body(), "dojoDnd" + (this.copy ? "Move" : "Copy"));
        dojo.addClass(dojo.body(), "dojoDnd" + (this.copy ? "Copy" : "Move"));
    }
});

// summary: the manager singleton variable, can be overwritten, if needed
dojo.dnd._manager = null;

dojo.dnd.manager = function(){
    // summary: returns the current DnD manager, creates one if it is not created yet
    if(!dojo.dnd._manager){
        dojo.dnd._manager = new dojo.dnd.Manager();
    }
    return dojo.dnd._manager;   // Object
};

}

if(!dojo._hasResource["dojo.dnd.Source"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojo.dnd.Source"] = true;
dojo.provide("dojo.dnd.Source");




/*
    Container property:
        "Horizontal"- if this is the horizontal container
    Source states:
        ""          - normal state
        "Moved"     - this source is being moved
        "Copied"    - this source is being copied
    Target states:
        ""          - normal state
        "Disabled"  - the target cannot accept an avatar
    Target anchor state:
        ""          - item is not selected
        "Before"    - insert point is before the anchor
        "After"     - insert point is after the anchor
*/

/*=====
dojo.dnd.__SourceArgs = function(){
    //  summary:
    //      a dict of parameters for DnD Source configuration. Note that any
    //      property on Source elements may be configured, but this is the
    //      short-list
    //  isSource: Boolean?
    //      can be used as a DnD source. Defaults to true.
    //  accept: Array?
    //      list of accepted types (text strings) for a target; defaults to
    //      ["text"]
    //  autoSync: Boolean
    //      if true refreshes the node list on every operation; false by default
    //  copyOnly: Boolean?
    //      copy items, if true, use a state of Ctrl key otherwise,
    //      see selfCopy and selfAccept for more details
    //  delay: Number
    //      the move delay in pixels before detecting a drag; 0 by default
    //  horizontal: Boolean?
    //      a horizontal container, if true, vertical otherwise or when omitted
    //  selfCopy: Boolean?
    //      copy items by default when dropping on itself,
    //      false by default, works only if copyOnly is true
    //  selfAccept: Boolean?
    //      accept its own items when copyOnly is true,
    //      true by default, works only if copyOnly is true
    //  withHandles: Boolean?
    //      allows dragging only by handles, false by default
    this.isSource = isSource;
    this.accept = accept;
    this.autoSync = autoSync;
    this.copyOnly = copyOnly;
    this.delay = delay;
    this.horizontal = horizontal;
    this.selfCopy = selfCopy;
    this.selfAccept = selfAccept;
    this.withHandles = withHandles;
}
=====*/

dojo.declare("dojo.dnd.Source", dojo.dnd.Selector, {
    // summary: a Source object, which can be used as a DnD source, or a DnD target

    // object attributes (for markup)
    isSource: true,
    horizontal: false,
    copyOnly: false,
    selfCopy: false,
    selfAccept: true,
    skipForm: false,
    withHandles: false,
    autoSync: false,
    delay: 0, // pixels
    accept: ["text"],

    constructor: function(/*DOMNode|String*/node, /*dojo.dnd.__SourceArgs?*/params){
        // summary:
        //      a constructor of the Source
        // node:
        //      node or node's id to build the source on
        // params:
        //      any property of this class may be configured via the params
        //      object which is mixed-in to the `dojo.dnd.Source` instance
        dojo.mixin(this, dojo.mixin({}, params));
        var type = this.accept;
        if(type.length){
            this.accept = {};
            for(var i = 0; i < type.length; ++i){
                this.accept[type[i]] = 1;
            }
        }
        // class-specific variables
        this.isDragging = false;
        this.mouseDown = false;
        this.targetAnchor = null;
        this.targetBox = null;
        this.before = true;
        this._lastX = 0;
        this._lastY = 0;
        // states
        this.sourceState  = "";
        if(this.isSource){
            dojo.addClass(this.node, "dojoDndSource");
        }
        this.targetState  = "";
        if(this.accept){
            dojo.addClass(this.node, "dojoDndTarget");
        }
        if(this.horizontal){
            dojo.addClass(this.node, "dojoDndHorizontal");
        }
        // set up events
        this.topics = [
            dojo.subscribe("/dnd/source/over", this, "onDndSourceOver"),
            dojo.subscribe("/dnd/start",  this, "onDndStart"),
            dojo.subscribe("/dnd/drop",   this, "onDndDrop"),
            dojo.subscribe("/dnd/cancel", this, "onDndCancel")
        ];
    },

    // methods
    checkAcceptance: function(source, nodes){
        // summary: checks, if the target can accept nodes from this source
        // source: Object: the source which provides items
        // nodes: Array: the list of transferred items
        if(this == source){
            return !this.copyOnly || this.selfAccept;
        }
        for(var i = 0; i < nodes.length; ++i){
            var type = source.getItem(nodes[i].id).type;
            // type instanceof Array
            var flag = false;
            for(var j = 0; j < type.length; ++j){
                if(type[j] in this.accept){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                return false;   // Boolean
            }
        }
        return true;    // Boolean
    },
    copyState: function(keyPressed, self){
        // summary: Returns true, if we need to copy items, false to move.
        //      It is separated to be overwritten dynamically, if needed.
        // keyPressed: Boolean: the "copy" was pressed
        // self: Boolean?: optional flag, which means that we are about to drop on itself

        if(keyPressed){ return true; }
        if(arguments.length < 2){
            self = this == dojo.dnd.manager().target;
        }
        if(self){
            if(this.copyOnly){
                return this.selfCopy;
            }
        }else{
            return this.copyOnly;
        }
        return false;   // Boolean
    },
    destroy: function(){
        // summary: prepares the object to be garbage-collected
        dojo.dnd.Source.superclass.destroy.call(this);
        dojo.forEach(this.topics, dojo.unsubscribe);
        this.targetAnchor = null;
    },

    // markup methods
    markupFactory: function(params, node){
        params._skipStartup = true;
        return new dojo.dnd.Source(node, params);
    },

    // mouse event processors
    onMouseMove: function(e){
        // summary: event processor for onmousemove
        // e: Event: mouse event
        if(this.isDragging && this.targetState == "Disabled"){ return; }
        dojo.dnd.Source.superclass.onMouseMove.call(this, e);
        var m = dojo.dnd.manager();
        if(this.isDragging){
            // calculate before/after
            var before = false;
            if(this.current){
                if(!this.targetBox || this.targetAnchor != this.current){
                    this.targetBox = {
                        xy: dojo.coords(this.current, true),
                        w: this.current.offsetWidth,
                        h: this.current.offsetHeight
                    };
                }
                if(this.horizontal){
                    before = (e.pageX - this.targetBox.xy.x) < (this.targetBox.w / 2);
                }else{
                    before = (e.pageY - this.targetBox.xy.y) < (this.targetBox.h / 2);
                }
            }
            if(this.current != this.targetAnchor || before != this.before){
                this._markTargetAnchor(before);
                m.canDrop(!this.current || m.source != this || !(this.current.id in this.selection));
            }
        }else{
            if(this.mouseDown && this.isSource &&
                    (Math.abs(e.pageX - this._lastX) > this.delay || Math.abs(e.pageY - this._lastY) > this.delay)){
                var nodes = this.getSelectedNodes();
                if(nodes.length){
                    m.startDrag(this, nodes, this.copyState(dojo.dnd.getCopyKeyState(e), true));
                }
            }
        }
    },
    onMouseDown: function(e){
        // summary: event processor for onmousedown
        // e: Event: mouse event
        if(this._legalMouseDown(e) && (!this.skipForm || !dojo.dnd.isFormElement(e))){
            this.mouseDown = true;
            this.mouseButton = e.button;
            this._lastX = e.pageX;
            this._lastY = e.pageY;
            dojo.dnd.Source.superclass.onMouseDown.call(this, e);
        }
    },
    onMouseUp: function(e){
        // summary: event processor for onmouseup
        // e: Event: mouse event
        if(this.mouseDown){
            this.mouseDown = false;
            dojo.dnd.Source.superclass.onMouseUp.call(this, e);
        }
    },

    // topic event processors
    onDndSourceOver: function(source){
        // summary: topic event processor for /dnd/source/over, called when detected a current source
        // source: Object: the source which has the mouse over it
        if(this != source){
            this.mouseDown = false;
            if(this.targetAnchor){
                this._unmarkTargetAnchor();
            }
        }else if(this.isDragging){
            var m = dojo.dnd.manager();
            m.canDrop(this.targetState != "Disabled" && (!this.current || m.source != this || !(this.current.id in this.selection)));
        }
    },
    onDndStart: function(source, nodes, copy){
        // summary: topic event processor for /dnd/start, called to initiate the DnD operation
        // source: Object: the source which provides items
        // nodes: Array: the list of transferred items
        // copy: Boolean: copy items, if true, move items otherwise
        if(this.autoSync){ this.sync(); }
        if(this.isSource){
            this._changeState("Source", this == source ? (copy ? "Copied" : "Moved") : "");
        }
        var accepted = this.accept && this.checkAcceptance(source, nodes);
        this._changeState("Target", accepted ? "" : "Disabled");
        if(this == source){
            dojo.dnd.manager().overSource(this);
        }
        this.isDragging = true;
    },
    onDndDrop: function(source, nodes, copy, target){
        // summary: topic event processor for /dnd/drop, called to finish the DnD operation
        // source: Object: the source which provides items
        // nodes: Array: the list of transferred items
        // copy: Boolean: copy items, if true, move items otherwise
        // target: Object: the target which accepts items
        if(this == target){
            // this one is for us => move nodes!
            this.onDrop(source, nodes, copy);
        }
        this.onDndCancel();
    },
    onDndCancel: function(){
        // summary: topic event processor for /dnd/cancel, called to cancel the DnD operation
        if(this.targetAnchor){
            this._unmarkTargetAnchor();
            this.targetAnchor = null;
        }
        this.before = true;
        this.isDragging = false;
        this.mouseDown = false;
        delete this.mouseButton;
        this._changeState("Source", "");
        this._changeState("Target", "");
    },

    // local events
    onDrop: function(source, nodes, copy){
        // summary: called only on the current target, when drop is performed
        // source: Object: the source which provides items
        // nodes: Array: the list of transferred items
        // copy: Boolean: copy items, if true, move items otherwise

        if(this != source){
            this.onDropExternal(source, nodes, copy);
        }else{
            this.onDropInternal(nodes, copy);
        }
    },
    onDropExternal: function(source, nodes, copy){
        // summary: called only on the current target, when drop is performed
        //  from an external source
        // source: Object: the source which provides items
        // nodes: Array: the list of transferred items
        // copy: Boolean: copy items, if true, move items otherwise

        var oldCreator = this._normalizedCreator;
        // transferring nodes from the source to the target
        if(this.creator){
            // use defined creator
            this._normalizedCreator = function(node, hint){
                return oldCreator.call(this, source.getItem(node.id).data, hint);
            };
        }else{
            // we have no creator defined => move/clone nodes
            if(copy){
                // clone nodes
                this._normalizedCreator = function(node, hint){
                    var t = source.getItem(node.id);
                    var n = node.cloneNode(true);
                    n.id = dojo.dnd.getUniqueId();
                    return {node: n, data: t.data, type: t.type};
                };
            }else{
                // move nodes
                this._normalizedCreator = function(node, hint){
                    var t = source.getItem(node.id);
                    source.delItem(node.id);
                    return {node: node, data: t.data, type: t.type};
                };
            }
        }
        this.selectNone();
        if(!copy && !this.creator){
            source.selectNone();
        }
        this.insertNodes(true, nodes, this.before, this.current);
        if(!copy && this.creator){
            source.deleteSelectedNodes();
        }
        this._normalizedCreator = oldCreator;
    },
    onDropInternal: function(nodes, copy){
        // summary: called only on the current target, when drop is performed
        //  from the same target/source
        // nodes: Array: the list of transferred items
        // copy: Boolean: copy items, if true, move items otherwise

        var oldCreator = this._normalizedCreator;
        // transferring nodes within the single source
        if(this.current && this.current.id in this.selection){
            // do nothing
            return;
        }
        if(copy){
            if(this.creator){
                // create new copies of data items
                this._normalizedCreator = function(node, hint){
                    return oldCreator.call(this, this.getItem(node.id).data, hint);
                };
            }else{
                // clone nodes
                this._normalizedCreator = function(node, hint){
                    var t = this.getItem(node.id);
                    var n = node.cloneNode(true);
                    n.id = dojo.dnd.getUniqueId();
                    return {node: n, data: t.data, type: t.type};
                };
            }
        }else{
            // move nodes
            if(!this.current){
                // do nothing
                return;
            }
            this._normalizedCreator = function(node, hint){
                var t = this.getItem(node.id);
                return {node: node, data: t.data, type: t.type};
            };
        }
        this._removeSelection();
        this.insertNodes(true, nodes, this.before, this.current);
        this._normalizedCreator = oldCreator;
    },
    onDraggingOver: function(){
        // summary: called during the active DnD operation, when items
        // are dragged over this target, and it is not disabled
    },
    onDraggingOut: function(){
        // summary: called during the active DnD operation, when items
        // are dragged away from this target, and it is not disabled
    },

    // utilities
    onOverEvent: function(){
        // summary: this function is called once, when mouse is over our container
        dojo.dnd.Source.superclass.onOverEvent.call(this);
        dojo.dnd.manager().overSource(this);
        if(this.isDragging && this.targetState != "Disabled"){
            this.onDraggingOver();
        }
    },
    onOutEvent: function(){
        // summary: this function is called once, when mouse is out of our container
        dojo.dnd.Source.superclass.onOutEvent.call(this);
        dojo.dnd.manager().outSource(this);
        if(this.isDragging && this.targetState != "Disabled"){
            this.onDraggingOut();
        }
    },
    _markTargetAnchor: function(before){
        // summary: assigns a class to the current target anchor based on "before" status
        // before: Boolean: insert before, if true, after otherwise
        if(this.current == this.targetAnchor && this.before == before){ return; }
        if(this.targetAnchor){
            this._removeItemClass(this.targetAnchor, this.before ? "Before" : "After");
        }
        this.targetAnchor = this.current;
        this.targetBox = null;
        this.before = before;
        if(this.targetAnchor){
            this._addItemClass(this.targetAnchor, this.before ? "Before" : "After");
        }
    },
    _unmarkTargetAnchor: function(){
        // summary: removes a class of the current target anchor based on "before" status
        if(!this.targetAnchor){ return; }
        this._removeItemClass(this.targetAnchor, this.before ? "Before" : "After");
        this.targetAnchor = null;
        this.targetBox = null;
        this.before = true;
    },
    _markDndStatus: function(copy){
        // summary: changes source's state based on "copy" status
        this._changeState("Source", copy ? "Copied" : "Moved");
    },
    _legalMouseDown: function(e){
        // summary: checks if user clicked on "approved" items
        // e: Event: mouse event
        if(!this.withHandles){ return true; }
        for(var node = e.target; node; node = node.parentNode){
            if(dojo.hasClass(node, "dojoDndHandle")){ return true; }
            if(dojo.hasClass(node, "dojoDndItem")){ break; }
        }
        return false;   // Boolean
    }
});

dojo.declare("dojo.dnd.Target", dojo.dnd.Source, {
    // summary: a Target object, which can be used as a DnD target

    constructor: function(node, params){
        // summary: a constructor of the Target --- see the Source constructor for details
        this.isSource = false;
        dojo.removeClass(this.node, "dojoDndSource");
    },

    // markup methods
    markupFactory: function(params, node){
        params._skipStartup = true;
        return new dojo.dnd.Target(node, params);
    }
});

dojo.declare("dojo.dnd.AutoSource", dojo.dnd.Source, {
    // summary: a source, which syncs its DnD nodes by default

    constructor: function(node, params){
        // summary: a constructor of the AutoSource --- see the Source constructor for details
        this.autoSync = true;
    },

    // markup methods
    markupFactory: function(params, node){
        params._skipStartup = true;
        return new dojo.dnd.AutoSource(node, params);
    }
});

}


dojo.i18n._preloadLocalizations("dojo.nls.eFapsDojo", ["pt","es","de","es-es","it-it","ROOT","fr-fr","pt-br","fr","en-us","de-de","it","en","en-gb","xx"]);
