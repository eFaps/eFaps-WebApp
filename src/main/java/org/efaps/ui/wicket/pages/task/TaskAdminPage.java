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

package org.efaps.ui.wicket.pages.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageReference;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.bpm.TaskAdminPanel;
import org.efaps.ui.wicket.components.bpm.process.ProcessAdminPanel;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TaskAdminPage
    extends AbstractMergePage
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TaskAdminPage.class);

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(TaskPage.class, "TaskPage.css");

    /**
     * @param _pageReference reference to the open page
     * @throws EFapsException on error
     */
    public TaskAdminPage(final PageReference _pageReference)
        throws EFapsException
    {

        final List<ITab> tabs = new ArrayList<ITab>();
        tabs.add(new AbstractTab(new Model<String>("Task"))
        {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(final String panelId)
            {
                Panel ret = null;
                try {
                   ret =  new TaskAdminPanel(panelId, _pageReference);
                } catch (final EFapsException e) {
                    TaskAdminPage.LOG.error("Could not load TaskAdminPanel", e);
                }
                return ret;
            }
        });



        tabs.add(new AbstractTab(new Model<String>("Process"))
        {
            private static final long serialVersionUID = 1L;

            @Override
            public Panel getPanel(final String panelId)
            {
                Panel ret = null;
                try {
                   ret =  new ProcessAdminPanel(panelId, _pageReference);
                } catch (final EFapsException e) {
                    TaskAdminPage.LOG.error("Could not load TaskAdminPanel", e);
                }
                return ret;

            }
        });
        add(new AjaxTabbedPanel<ITab>("tabs", tabs));
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(TaskAdminPage.CSS));
    }


}
