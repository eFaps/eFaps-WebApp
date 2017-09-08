/*
 * Copyright 2003 - 2017 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.efaps.ui.wicket.pages.preferences;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;

/**
 * The Class PreferencesPage.
 */
public class PreferencesPage
    extends AbstractMergePage
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new preferences page.
     */
    public PreferencesPage()
    {
       // add(new AjaxSaveBtn("saveBtn"));
    }

    /**
     * AjaxLink that closes the ModalWindow this Page was opened in.
     */
    public class AjaxSaveBtn
        extends AjaxButton<Void>
    {

        /** Needed for serialization. */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id of this component
         */
        public AjaxSaveBtn(final String _wicketId)
        {
            super(_wicketId, AjaxButton.ICON.ACCEPT.getReference(), DBProperties.getProperty(
                            "org.efaps.ui.wicket.pages.preferences.saveBtn.Label"));
        }

        @Override
        public void onRequest(final AjaxRequestTarget _target)
        {
            _target.appendJavaScript(DojoWrapper.require("popup.close(registry.byId(\"eFapsUserName\"))",
                            DojoClasses.popup, DojoClasses.registry));
        }

        @Override
        protected boolean isSubmit()
        {
            return false;
        }
    }
}
