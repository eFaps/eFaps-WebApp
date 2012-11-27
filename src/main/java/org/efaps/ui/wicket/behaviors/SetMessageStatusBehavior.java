/*
 * Copyright 2003 - 2012 The eFaps Team
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
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
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
public class SetMessageStatusBehavior
    extends Behavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Render to the web response whatever the component wants to contribute to
     * the head section.
     *
     * @param _component    Component this behavior belongs to
     * @param _response     Response object
     */
    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        try {
            _response.render(JavaScriptHeaderItem.forScript(SetMessageStatusBehavior.getScript(),
                            SetMessageStatusBehavior.class.getName()));
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        if (MessageStatusHolder.hasUnreadMsg(usrId) || MessageStatusHolder.hasReadMsg(usrId)) {
            js.append("top.postMessage('").append(StringEscapeUtils.escapeJavaScript(
                                SetMessageStatusBehavior.getLabel(
                                                MessageStatusHolder.getUnReadCount(usrId),
                                                MessageStatusHolder.getReadCount(usrId))))
                .append("', '*');");
        } else {
            js.append("top.postMessage('").append("', '*');");
        }
        return js.toString();
    }

    /**
     * @return UUID of the command used for the alert button
     */
    public static UUID getCmdUUD()
    {
        // Admin_Common_SystemMessageAlert
        return UUID.fromString("5a6f2d4a-df81-4211-b7ed-18ae83608c81");
    }

    /**
     * @param _unread unread messages
     * @param _read read messages
     * @return string
     */
    public static String getLabel(final int _unread,
                                  final int _read)
    {
        final StringBuilder ret = new StringBuilder()
            .append("<span").append(_unread > 0 ? "class=\"unread\"": "").append(">")
            .append(String.format(Command.get(SetMessageStatusBehavior.getCmdUUD()).getLabelProperty(), _unread, _read))
            .append("</span>");
        return ret.toString();
    }
}
