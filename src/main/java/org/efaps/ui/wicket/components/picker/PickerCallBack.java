/*
 * Copyright 2003 - 2012 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.ui.wicket.components.picker;

import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.efaps.ui.wicket.models.cell.UIPicker;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.util.EFapsKey;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class PickerCallBack
    implements ModalWindow.WindowClosedCallback
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * MarkupId of the Target Component.
     */
    private final String targetMarkupId;

    /**
     * Refernce to the page.
     */
    private final PageReference pageReference;

    /**
     * @param _targetMarkupId   MarkupId of the target
     * @param _pageReference    refernce to the page te call back must be executed in
     *
     */
    public PickerCallBack(final String _targetMarkupId,
                          final PageReference _pageReference)
    {
        this.targetMarkupId = _targetMarkupId;
        this.pageReference = _pageReference;
    }

    /**
     * The actual Javascript that will be executed on close of the modal window.
     * @param _target Target
     */
    @Override
    public void onClose(final AjaxRequestTarget _target)
    {
        final AbstractUIPageObject pageObject = (AbstractUIPageObject) this.pageReference.getPage()
                        .getDefaultModelObject();
        if (pageObject.isOpenedByPicker()) {
            final UIPicker picker = pageObject.getPicker();
            pageObject.setPicker(null);
            if (picker.isExecuted()) {
                final Map<String, String> map = picker.getReturnMap();
                final boolean escape = escape(map);
                final StringBuilder js = new StringBuilder();

                final String value = map.get(EFapsKey.PICKER_VALUE.getKey());
                if (value != null) {
                    js.append("require(['dojo/dom'], function(dom){\n")
                        .append("dom.byId('").append(this.targetMarkupId).append("').value ='")
                        .append(escape ? StringEscapeUtils.escapeEcmaScript(StringEscapeUtils.escapeHtml4(value))
                                                    : value).append("';").append("});");
                }
                for (final String keyString : map.keySet()) {
                    // if the map contains a key that is not defined in this
                    // class it is assumed to be the name of a field
                    if (!(EFapsKey.PICKER_JAVASCRIPT.getKey().equals(keyString)
                                    || EFapsKey.PICKER_DEACTIVATEESCAPE.getKey().equals(keyString)
                                    || EFapsKey.PICKER_VALUE.getKey().equals(keyString))) {
                        if (map.get(keyString).contains("Array(")) {
                            js.append("eFapsSetFieldValue('").append(this.targetMarkupId).append("','")
                                            .append(keyString).append("',").append(map.get(keyString)).append(");");
                        } else {
                            js.append("eFapsSetFieldValue('").append(this.targetMarkupId).append("','")
                                .append(keyString).append("','")
                                .append(escape  ? StringEscapeUtils.escapeEcmaScript(map.get(keyString))
                                                            : map.get(keyString)).append("');");
                        }
                    }
                }
                if (map.containsKey(EFapsKey.PICKER_JAVASCRIPT.getKey())) {
                    js.append(map.get(EFapsKey.PICKER_JAVASCRIPT.getKey()));
                }
                _target.prependJavaScript(js.toString());
                picker.setExecuted(false);
            }
        }
    }

    /**
     * Check if for the current values the escape is activated.<br>
     * Default: true,<br>
     * key exits: null = false else evaluation of given String
     * @param _map map to be checked
     * @return boolean
     */
    private boolean escape(final Map<String, String> _map)
    {
        boolean ret = true;
        if (_map.containsKey(EFapsKey.PICKER_DEACTIVATEESCAPE.getKey())) {
            final String value = _map.get(EFapsKey.PICKER_DEACTIVATEESCAPE.getKey());
            if (value == null) {
                ret = false;
            } else {
                ret = !"true".equalsIgnoreCase(EFapsKey.PICKER_DEACTIVATEESCAPE.getKey());
            }
        }
        return ret;
    }
}
