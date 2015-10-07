/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.ui.wicket.pages.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageReference;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.ui.IDashboard;
import org.efaps.api.ui.IDashboardProvider;
import org.efaps.ui.wicket.behaviors.KeepAliveBehavior;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.components.dashboard.DachboardContainerPanel;
import org.efaps.ui.wicket.components.tabs.AjaxIndicatingTabbedPanel;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsBaseException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class DashboardPage
    extends AbstractMergePage
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DashboardPage.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(DashboardPage.class,
                    "DashboardPage.css");

    /**
     * @param _pageReference Reference to the calling page
     * @throws EFapsException on error
     */
    public DashboardPage(final PageReference _pageReference)
        throws EFapsException
    {
        super();
        add(new AbstractDojoBehavior()
        {
            private static final long serialVersionUID = 1L;
        });
        add(new KeepAliveBehavior());

        final List<ITab> tabs = new ArrayList<ITab>();
        final String providerClass = Configuration.getAttribute(ConfigAttribute.BOARD_PROVIDER);
        if (providerClass != null) {
            Class<?> clazz;
            try {
                clazz = Class.forName(providerClass, false, EFapsClassLoader.getInstance());
                final IDashboardProvider provider = (IDashboardProvider) clazz.newInstance();
                final List<IDashboard> dashboards = provider.getDashboards();
                if (dashboards.isEmpty()) {
                    add(new WebMarkupContainer("tabs").setVisible(false));
                } else if (dashboards.size() == 1) {
                    add(new DachboardContainerPanel("tabs", _pageReference, dashboards.get(0), true));
                } else {
                    boolean first = true;
                    for (final IDashboard dashboard : dashboards) {
                        final boolean main = first;
                        if (first) {
                            first = false;
                        }
                        tabs.add(new AbstractTab(new Model<String>(dashboard.getTitle()))
                        {
                            private static final long serialVersionUID = 1L;
                            @Override
                            public Panel getPanel(final String _panelId)
                            {
                                Panel ret = null;
                                try {
                                    ret = new DachboardContainerPanel(_panelId, _pageReference, dashboard, main);
                                } catch (final EFapsException e) {
                                    LOG.error("Could not load DashboardContainerPanel", e);
                                }
                                return ret;
                            }
                        });
                    }
                    add(new AjaxIndicatingTabbedPanel("tabs", tabs));
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOG.error("Could not find/instantiate Provider Class", e);
            } catch (final EFapsBaseException e1) {
                LOG.error("Could not retrieve dashboard classes", e1);
            }
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(DashboardPage.CSS));
    }
}
