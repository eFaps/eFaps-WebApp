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


package org.efaps.ui.wicket.behaviors;

import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.util.string.JavascriptUtils;
import org.efaps.admin.common.SystemMessage;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SetMessageStatusContributor
    extends StringHeaderContributor
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;


    /**
     * @param contribution
     * @throws EFapsException
     */
    public SetMessageStatusContributor()
        throws EFapsException
    {
        super(SetMessageStatusContributor.getScript());
    }

    private static String getScript()
        throws EFapsException
    {
        final StringBuilder js = new StringBuilder();
        js.append(JavascriptUtils.SCRIPT_OPEN_TAG);
        if (SystemMessage.hasUnreadMsg(Context.getThreadContext().getPersonId())) {
            js.append("top.document.getElementById('eFapsUserMsg').className = 'unread';")
                .append("top.document.getElementById('eFapsUserMsg').style.display = 'table-cell';");
        } else if (SystemMessage.hasReadMsg(Context.getThreadContext().getPersonId())) {
            js.append("top.document.getElementById('eFapsUserMsg').className = '';")
                .append("top.document.getElementById('eFapsUserMsg').style.display = 'table-cell';");
        } else {
            js.append("top.document.getElementById('eFapsUserMsg').style.display = 'none';");
        }
        js.append(JavascriptUtils.SCRIPT_CLOSE_TAG);
        return js.toString();
    }

}
