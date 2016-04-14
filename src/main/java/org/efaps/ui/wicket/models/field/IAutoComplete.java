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

package org.efaps.ui.wicket.models.field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.efaps.admin.event.Return;
import org.efaps.api.ui.IOption;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.cell.AutoCompleteSettings;
import org.efaps.ui.wicket.models.objects.AbstractUIModeObject;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public interface IAutoComplete
    extends Serializable
{

    /**
     * Gets the auto complete setting.
     *
     * @return the auto complete setting
     */
    AutoCompleteSettings getAutoCompleteSetting();

    /**
     * Gets the auto completion.
     *
     * @param _input the input
     * @param _uiID2Oid the ui i d2 oid
     * @return the auto completion
     * @throws EFapsException on error
     */
    List<Return> getAutoCompletion(final String _input,
                                   final Map<String, String> _uiID2Oid)
        throws EFapsException;

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    AbstractUIModeObject getParent();

    /**
     * Gets the single instance of IAutoComplete.
     *
     * @return single instance of IAutoComplete
     * @throws EFapsException on error
     */
    Instance getInstance()
        throws EFapsException;

    /**
     * Gets the auto complete value.
     *
     * @return the auto complete value
     * @throws EFapsException on error
     */
    String getAutoCompleteValue()
        throws EFapsException;

    /**
     * Checks if is field update.
     *
     * @return true, if is field update
     */
    boolean isFieldUpdate();

    /**
     * Gets the label.
     *
     * @return the label
     * @throws EFapsException on error
     */
    String getLabel()
        throws EFapsException;

    /**
     * Gets the tokens.
     *
     * @return the tokens
     * @throws EFapsException on error
     */
    List<IOption> getTokens()
        throws EFapsException;

    /**
     * Checks if is auto complete.
     *
     * @return true, if is auto complete
     */
    boolean isAutoComplete();
}
