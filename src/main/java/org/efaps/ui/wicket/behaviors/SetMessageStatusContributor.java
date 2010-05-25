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

import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.util.string.JavascriptUtils;
import org.efaps.admin.ui.Command;
import org.efaps.db.Context;
import org.efaps.message.MessageStatusHolder;
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
     * @throws EFapsException on errro
     */
    public SetMessageStatusContributor()
        throws EFapsException
    {
        super(SetMessageStatusContributor.getScript());
    }

    /**
     *
     * @return script
     * @throws EFapsException on error
     */
    private static String getScript()
        throws EFapsException
    {
        final StringBuilder js = new StringBuilder();
        final long usrId = Context.getThreadContext().getPersonId();
        js.append(JavascriptUtils.SCRIPT_OPEN_TAG);
        if (MessageStatusHolder.hasUnreadMsg(usrId) || MessageStatusHolder.hasReadMsg(usrId)) {
            js.append("var ma = top.document.getElementById('eFapsUserMsg');")
                .append("ma.style.display = 'table-cell';")
                .append("ma.getElementsByTagName('A')[0].firstChild.nodeValue= '")
                .append(StringEscapeUtils.escapeJavaScript(
                                SetMessageStatusContributor.getLabel(MessageStatusHolder.getUnReadCount(usrId),
                                MessageStatusHolder.getReadCount(usrId))))
                .append("';");
            if (MessageStatusHolder.hasUnreadMsg(usrId)) {
                js.append("ma.className = 'unread';");
            } else {
                js.append("ma.className = '';");
            }
        } else {
            js.append("top.document.getElementById('eFapsUserMsg').style.display = 'none';");
        }
        js.append(JavascriptUtils.SCRIPT_CLOSE_TAG);
        return js.toString();
    }

    /**
     * @return UUID of the command used for the alert button
     */
    public static UUID getCmdUUD()
    {
      //Admin_Common_SystemMessageAlert
        return UUID.fromString("5a6f2d4a-df81-4211-b7ed-18ae83608c81");
    }

    /**
     * @param _unread   unread messages
     * @param _read     read messages
     * @return string
     */
    public static String getLabel(final int _unread,
                                  final int _read)
    {
        return String.format(Command.get(SetMessageStatusContributor.getCmdUUD()).getLabelProperty(), _unread, _read);
    }

}
