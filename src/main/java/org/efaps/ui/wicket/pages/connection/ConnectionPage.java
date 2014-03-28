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

package org.efaps.ui.wicket.pages.connection;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.connection.MessagePanel;
import org.efaps.ui.wicket.components.connection.SessionPanel;
import org.efaps.ui.wicket.components.tabs.AjaxIndicatingTabbedPanel;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ConnectionPage
    extends AbstractMergePage
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionPage.class);

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(ConnectionPage.class,
                    "ConnectionPage.css");

    /**
     * @param _pageReference reference to the open page
     * @throws EFapsException on error
     */
    public ConnectionPage(final PageReference _pageReference)
        throws EFapsException
    {

        final List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(new AbstractTab(new Model<String>("Sessions"))
        {

            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(final String _panelId)
            {
                Panel ret = null;
                try {
                    ret = new SessionPanel(_panelId, _pageReference);
                } catch (final EFapsException e) {
                    ConnectionPage.LOG.error("Could not load SessionPanel", e);
                }
                return ret;
            }
        });

        if (Configuration.getAttributeAsBoolean(Configuration.ConfigAttribute.WEBSOCKET_ACTVATE)) {
            tabs.add(new AbstractTab(new Model<String>("Message"))
            {

                private static final long serialVersionUID = 1L;

                @Override
                public Panel getPanel(final String _panelId)
                {
                    Panel ret = null;
                    try {
                        ret = new MessagePanel(_panelId, _pageReference);
                    } catch (final EFapsException e) {
                        ConnectionPage.LOG.error("Could not load SessionPanel", e);
                    }
                    return ret;
                }
            });
        }
        add(new AjaxIndicatingTabbedPanel("tabs", tabs) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onAjaxUpdate(final AjaxRequestTarget _target)
            {
                super.onAjaxUpdate(_target);
                _target.addChildren(getPage(), FeedbackPanel.class);
            }
        });
        add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(ConnectionPage.CSS));
    }
}
