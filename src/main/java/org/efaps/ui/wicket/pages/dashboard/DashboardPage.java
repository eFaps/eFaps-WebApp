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

package org.efaps.ui.wicket.pages.dashboard;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.time.Duration;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.ui.wicket.components.task.TaskTablePanel;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DashboardPage
    extends AbstractMergePage
{

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
        final SystemConfiguration config = EFapsSystemConfiguration.KERNEL.get();
        final boolean active = config != null
                        ? config.getAttributeValueAsBoolean(KernelSettings.ACTIVATE_BPM) : false;
        if (active) {
            final TaskTablePanel table = new TaskTablePanel("table", _pageReference);
            add(table);
            table.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(15)) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onPostProcessTarget(final AjaxRequestTarget _target)
                {
                    table.updateData();
                }
            });
        } else {
            add(new WebMarkupContainer("table").setVisible(false));
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(DashboardPage.CSS));
    }
}
