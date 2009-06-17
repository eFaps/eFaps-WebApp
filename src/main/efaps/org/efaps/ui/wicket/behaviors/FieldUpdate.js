/*
 * Copyright 2003 - 2009 The eFaps Team
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
 *
 * Author:          The eFaps Team
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

/* 
 * @eFapsPackage  org.efaps.ui.wicket.behaviors
 * @eFapsUUID     9dae1678-6945-4141-bbe9-25d916250ae3
 * @eFapsRevision $Rev$
 */

function eFapsSetFieldValue(_referenceId, _fieldName, _fieldValue) {
    // get the position in the field collection of the given reference 
    var refField = document.getElementById(_referenceId);
    var name = refField.getAttribute('name');
    var eFapsFields = document.getElementsByName(name);
    var pos = 0;
    for ( var i = 0; i < eFapsFields.length; i++) {
        if (eFapsFields[i].id == _referenceId) {
            pos = i;
            break;
        }
    }
    // get the field collection 
    var fields = document.getElementsByName(_fieldName);
    var cp = 0;
    if (fields.length > 1) {
        var cp = pos;
    }
    if (_fieldValue instanceof Array) {
        var sel = fields[cp];
        if (sel.nodeName == 'SELECT') {
        } else {
            sel = document.createElement('SELECT');
            for (i = 0; i< fields[cp].attributes.length;i++) {
                if (sel.getAttributeNode(fields[cp].attributes[i].name)==null) {
                    sel.setAttribute(fields[cp].attributes[i].name, fields[cp].attributes[i].value);
                }
            }
            sel.size=1;
            fields[cp].parentNode.replaceChild(sel, fields[cp]);
        }
        while (sel.options.length) {
            sel.options[0] = null;
        }
        for (i = 1; i < _fieldValue.length; i = i + 2) {
            option = new Option(_fieldValue[i + 1], _fieldValue[i], false,
                    _fieldValue[i] == _fieldValue[0]);
            sel.options[sel.length] = option;
        }
    } else {
        // if it is an input, the value can be set directly, else the dom must be used 
        if (fields[cp].nodeName == 'INPUT') {
            fields[cp].value = _fieldValue;
        } else {
            if (fields[cp].hasChildNodes()) {
                fields[cp].firstChild.data = _fieldValue;
            } else {
                var n = document.createTextNode(_fieldValue);
                fields[cp].appendChild(n);
            }
        }
    }
}

