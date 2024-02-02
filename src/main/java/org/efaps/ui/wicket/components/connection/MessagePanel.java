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
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.efaps.ui.wicket.EFapsApplication;
import org.efaps.ui.wicket.components.AbstractSortableProvider;
import org.efaps.ui.wicket.components.connection.MessageTablePanel.CheckBoxPanel;
import org.efaps.ui.wicket.connectionregistry.RegistryManager;
import org.efaps.ui.wicket.models.PushMsg;
import org.efaps.ui.wicket.models.objects.UIUser;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
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
        final Form<Void> msgForm = new Form<>("msgForm");
        add(msgForm);

        final AjaxSubmitLink sendMsgBtn = new AjaxSubmitLink("sendMsgBtn", msgForm)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onAfterSubmit(final AjaxRequestTarget _target)
            {
                final StringBuilder msg = new StringBuilder();
                msgForm.visitChildren(TextArea.class, (_textArea, _visit) -> {
                    _textArea.setEscapeModelStrings(false);
                    msg.append(_textArea.getDefaultModelObjectAsString());
                    _visit.stop();
                });

                if (msg.length() > 0) {
                    msgForm.visitChildren(CheckBox.class, (_checkBox, _visit) -> {
                        final Boolean selected = (Boolean) _checkBox.getDefaultModelObject();
                        if (selected) {
                            final CheckBoxPanel panel = (CheckBoxPanel) _checkBox.getParent();
                            final UIUser user = (UIUser) panel.getDefaultModelObject();
                            final List<IWebSocketConnection> conns = RegistryManager
                                            .getConnections4User(user.getUserName());
                            for (final IWebSocketConnection conn : conns) {
                                conn.sendMessage(new PushMsg(msg.toString()));
                            }
                        }
                    });
                }
            }
        };
        msgForm.add(sendMsgBtn);

        final AjaxSubmitLink broadcastMsgBtn = new AjaxSubmitLink("broadcastMsgBtn", msgForm)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onAfterSubmit(final AjaxRequestTarget _target)
            {

                final StringBuilder msg = new StringBuilder();
                msgForm.visitChildren(TextArea.class, (_textArea, _visit) -> {
                    _textArea.setEscapeModelStrings(false);
                    msg.append(_textArea.getDefaultModelObjectAsString());
                    _visit.stop();
                });

                if (msg.length() > 0) {
                    final WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(getApplication());
                    final WebSocketPushBroadcaster broadcaster =
                                    new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
                    broadcaster.broadcastAll(EFapsApplication.get(), new PushMsg(msg.toString()));
                }
            }
        };
        msgForm.add(broadcastMsgBtn);

        final TextArea<String> msg = new TextArea<>("msg", Model.of(""));
        msgForm.add(msg);

        final MessageTablePanel messageTable = new MessageTablePanel("messageTable", _pageReference,
                        new UserProvider());
        msgForm.add(messageTable);
    }
}
