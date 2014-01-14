/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.ui.wicket.models.field.factories;

import org.apache.wicket.Component;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface IComponentFactory
{

    /**
     * Get an editable component.
     * @param _wicketId wicket if for the component
     * @param _abstractUIField field use for the component
     * @return the component
     * @throws EFapsException on error
     */
    Component getEditable(final String _wicketId,
                          final AbstractUIField _abstractUIField)
        throws EFapsException;

    /**
     *  Get an read only component.
     * @param _wicketId wicket if for the component
     * @param _abstractUIField field use for the component
     * @return the component
     * @throws EFapsException on error
     */
    Component getReadOnly(final String _wicketId,
                          final AbstractUIField _abstractUIField)
        throws EFapsException;

}
