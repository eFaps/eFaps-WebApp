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
package org.efaps.ui.wicket.behaviors.update;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public interface IRemoteUpdateListener
{
    /**
     * Key for the parameter passed by the UserInterface.
     */
    String PARAMETERKEY = "eFapsULKey";

    /**
     * @return key that is a unique identifier for this IMenuUpdateListener.
     */
    String getKey();

    /**
     * @param _component component the vent belongs to
     * @param _target the AjaxTarget
     */
    void onEvent(Component _component,
                 AjaxRequestTarget _target);
}
