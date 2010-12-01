/*
 * Copyright 2003 - 2010 The eFaps Team
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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.efaps.ui.wicket.models.cell.UIPicker;


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
     * Picker this CallBack belongs to.
     */
    private final UIPicker picker;

    /**
     * @param _picker picker this callback belongs to
     */
    public PickerCallBack(final UIPicker _picker)
    {
        this.picker = _picker;
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.extensions.ajax.markup.html.modal.
     * ModalWindow.WindowClosedCallback#onClose(org.apache.wicket.ajax.AjaxRequestTarget)
     */
    @Override
    public void onClose(final AjaxRequestTarget _target)
    {
        if (this.picker.isExecuted()) {
            this.picker.getReturnMap();
            _target.prependJavascript("alert(document.getElementsByName('description')[0].value);");
            this.picker.setExecuted(false);
        }
    }
}
