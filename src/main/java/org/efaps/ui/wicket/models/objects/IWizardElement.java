/*
 * Copyright 2003 - 2017 The eFaps Team
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
 */


package org.efaps.ui.wicket.models.objects;


/**
 * The Interface IWizardElement.
 *
 * @author The eFaps Team
 */
public interface IWizardElement
{

    /**
     * Checks if is wizard call.
     *
     * @return true, if is wizard call
     */
    boolean isWizardCall();

    /**
     * Gets the UI wizard object.
     *
     * @return the UI wizard object
     */
    UIWizardObject getUIWizardObject();

    /**
     * Sets the UI wizard object.
     *
     * @param _uiWizardObject the ui wizard object
     * @return the i wizard element
     */
    IWizardElement setUIWizardObject(UIWizardObject _uiWizardObject);
}
