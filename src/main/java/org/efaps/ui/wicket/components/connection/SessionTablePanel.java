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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.EFapsApplication;
import org.efaps.ui.wicket.components.bpm.AbstractSortableProvider;
import org.efaps.ui.wicket.models.objects.UIUserSession;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SessionTablePanel
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
     * DataProvier for this TaskTable.
     */
    private final SessionProvider dataProvider;

    /**
     * @param _wicketId wicket for this component
     * @param _pageReference Reference to the calling page
     * @param _dataProvider provider for the task table
     * @throws EFapsException on error
     */
    public SessionTablePanel(final String _wicketId,
                             final PageReference _pageReference,
                             final SessionProvider _dataProvider)
        throws EFapsException
    {
        super(_wicketId);
        this.dataProvider = _dataProvider;

        final List<IColumn<UIUserSession, String>> columns = new ArrayList<IColumn<UIUserSession, String>>();

        columns.add(new AbstractColumn<UIUserSession, String>(new Model<String>(""))
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<UIUserSession>> _cellItem,
                                     final String _componentId,
                                     final IModel<UIUserSession> _rowModel)
            {
                _cellItem.add(new AjaxLink<UIUserSession>(_componentId, _rowModel)
                {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(final AjaxRequestTarget _target)
                    {
                        final UIUserSession uiSession = (UIUserSession) getDefaultModelObject();
                        final IWebSocketConnection conn = EFapsApplication.get().getConnectionRegistry()
                                        .getConnection4UserSession(uiSession.getUserName(), uiSession.getSessionId());
                        conn.sendMessage(uiSession);
                    }
                });
            }
        });

        final String userName = DBProperties.getProperty(SessionTablePanel.class.getName() + ".UserName");
        final String sessionId = DBProperties.getProperty(SessionTablePanel.class.getName() + ".SessionId");

        columns.add(new PropertyColumn<UIUserSession, String>(new Model<String>(userName), "userName",
                        "userName"));
        columns.add(new PropertyColumn<UIUserSession, String>(new Model<String>(sessionId), "sessionId", "sessionId"));

        add(new AjaxFallbackDefaultDataTable<UIUserSession, String>("table", columns, this.dataProvider,
                        this.dataProvider.getRowsPerPage()));
    }


    /**
     * @return update the underlying data
     */
    public boolean updateData()
    {
        this.dataProvider.requery();
        return true;
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(SessionTablePanel.CSS));
    }
}
