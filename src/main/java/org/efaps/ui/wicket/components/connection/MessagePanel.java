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

package org.efaps.ui.wicket.components.connection;

import java.util.List;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;
import org.efaps.ui.wicket.EFapsApplication;
import org.efaps.ui.wicket.components.bpm.AbstractSortableProvider;
import org.efaps.ui.wicket.components.connection.MessageTablePanel.CheckBoxPanel;
import org.efaps.ui.wicket.models.PushMsg;
import org.efaps.ui.wicket.models.objects.UIUser;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class MessagePanel
    extends Panel
{

    /**
     * Reference to the style sheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(AbstractSortableProvider.class,
                    "BPM.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicketId of this component
     * @param _pageReference reference to the page
     * @throws EFapsException on error
     */
    public MessagePanel(final String _wicketId,
                        final PageReference _pageReference)
        throws EFapsException
    {
        super(_wicketId);
        final Form<Void> msgForm = new Form<Void>("msgForm");
        add(msgForm);

        final AjaxSubmitLink sendMsgBtn = new AjaxSubmitLink("sendMsgBtn", msgForm)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onAfterSubmit(final AjaxRequestTarget _target,
                                         final Form<?> _form)
            {
                @SuppressWarnings("unchecked")
                final TextArea<String> txt = (TextArea<String>) _form.visitChildren(TextArea.class).next();
                final String msg = txt.getDefaultModelObjectAsString();
                if (!msg.isEmpty()) {
                    final ComponentHierarchyIterator iter = _form.visitChildren(CheckBox.class);
                    while (iter.hasNext()) {
                        final CheckBox checkBox = (CheckBox) iter.next();
                        final Boolean selected = (Boolean) checkBox.getDefaultModelObject();
                        if (selected) {
                            final CheckBoxPanel panel = (CheckBoxPanel) checkBox.getParent();
                            final UIUser user = (UIUser) panel.getDefaultModelObject();
                            final List<IWebSocketConnection> conns = EFapsApplication.get().getConnectionRegistry()
                                            .getConnections4User(user.getUserName());
                            for (final IWebSocketConnection conn : conns) {
                                conn.sendMessage(new PushMsg(msg));
                            }
                        }
                    }
                }
            }
        };
        msgForm.add(sendMsgBtn);

        final AjaxSubmitLink broadcastMsgBtn = new AjaxSubmitLink("broadcastMsgBtn", msgForm)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onAfterSubmit(final AjaxRequestTarget _target,
                                         final Form<?> _form)
            {
                @SuppressWarnings("unchecked")
                final TextArea<String> txt = (TextArea<String>) _form.visitChildren(TextArea.class).next();
                final String msg = txt.getDefaultModelObjectAsString();
                if (!msg.isEmpty()) {
                    final IWebSocketSettings webSocketSettings = IWebSocketSettings.Holder.get(getApplication());
                    final WebSocketPushBroadcaster broadcaster =
                                    new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
                    broadcaster.broadcastAll(EFapsApplication.get(), new PushMsg(msg));
                }
            }
        };
        msgForm.add(broadcastMsgBtn);

        final TextArea<String> msg = new TextArea<String>("msg", Model.of(""));
        msgForm.add(msg);

        final MessageTablePanel messageTable = new MessageTablePanel("messageTable", _pageReference,
                        new UserProvider());
        msgForm.add(messageTable);
    }
}
