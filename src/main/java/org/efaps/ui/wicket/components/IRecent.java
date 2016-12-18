/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.ui.wicket.components;

import org.apache.wicket.Component;
import org.efaps.util.EFapsException;

/**
 * Interface that must be implemented from a component if is used to navigate
 * back to a recent page or object.
 *
 * @author The eFaps Team
 */
public interface IRecent
{

    /**
     * Open the Page the Link directs to.
     *
     * @param _openComponent Component that class the open command
     * @throws EFapsException on any error
     */
    void open(Component _openComponent)
        throws EFapsException;

    /**
     * The String presented to the User to open this Link.
     *
     * @param _maxLength maximum Length of the Label
     * @return the label of the link
     * @throws EFapsException on any error
     */
    String getLabel(int _maxLength)
        throws EFapsException;
}
