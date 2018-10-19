/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.ui.wicket.models.field.factories;

import org.apache.wicket.Component;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 *
 * @author The eFaps Team
 *
 */
public interface IComponentFactory
{

    /**
     * Get an editable component.
     *
     * @param _wicketId wicket if for the component
     * @param _uiField field use for the component
     * @return the component
     * @throws EFapsException on error
     */
    Component getEditable(final String _wicketId,
                          final AbstractUIField _uiField)
        throws EFapsException;

    /**
     * Get an read only component.
     *
     * @param _wicketId wicket if for the component
     * @param _uiField field use for the component
     * @return the component
     * @throws EFapsException on error
     */
    Component getReadOnly(final String _wicketId,
                          final AbstractUIField _uiField)
        throws EFapsException;

    /**
     * Get an hidden component.
     *
     * @param _wicketId wicket if for the component
     * @param _uiField field used for the component
     * @return the component
     * @throws EFapsException on error
     */
    Component getHidden(final String _wicketId,
                        final AbstractUIField _uiField)
        throws EFapsException;

    /**
     * Get key to this Factory.
     *
     * @return key to this Factory
     */
    String getKey();

    /**
     * @param _uiField field used
     * @return value for the picklist
     * @throws EFapsException on error
     */
    String getPickListValue(final AbstractUIField _uiField)
        throws EFapsException;

    /**
     * @param __uiField field used
     * @return comparable
     * @throws EFapsException on error
     */
    Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException;

    /**
     * @param __uiField field used
     * @return comparable
     * @throws EFapsException on error
     */
    boolean applies(final AbstractUIField _uiField)
                    throws EFapsException;

    /**
     * @param __uiField field used
     * @return comparable
     * @throws EFapsException on error
     */
    String getStringValue(final AbstractUIField _uiField)
        throws EFapsException;

}
