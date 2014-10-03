/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.ui.wicket.models.field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.efaps.admin.event.Return;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.cell.AutoCompleteSettings;
import org.efaps.ui.wicket.models.objects.AbstractUIModeObject;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface IAutoComplete
    extends Serializable
{

    /**
     * @return
     */
    AutoCompleteSettings getAutoCompleteSetting();

    /**
     * @param _input
     * @param _uiID2Oid
     * @return
     */
    List<Return> getAutoCompletion(final String _input,
                                   final Map<String, String> _uiID2Oid)
        throws EFapsException;

    /**
     * @return
     */
    AbstractUIModeObject getParent();

    /**
     * @return
     */
    Instance getInstance()
        throws EFapsException;

    /**
     * @return
     */
    String getAutoCompleteValue()
        throws EFapsException;

    /**
     * @return
     */
    boolean isFieldUpdate();

    /**
     * @return
     */
    String getLabel()
        throws EFapsException;

}
