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
package org.efaps.ui.wicket.models.field;

import org.apache.wicket.Component;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public interface IHidden
{

    /**
     * Sets the added.
     *
     * @param _true the true
     * @return the i hidden
     */
    IHidden setAdded(boolean _true);

    /**
     * Checks if is added.
     *
     * @return true, if is added
     */
    boolean isAdded();

    /**
     * Gets the component.
     *
     * @param _wicketId the wicket id
     * @return the component
     * @throws EFapsException on error
     */
    Component getComponent(final String _wicketId)
        throws EFapsException;

}
