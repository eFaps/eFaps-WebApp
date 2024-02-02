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
package org.efaps.ui.wicket.components.tabs;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.pages.dashboard.DashboardPage;


/**
 * Extending TabbedPanel to add full screen indicator.
 *
 * @author The eFaps Team
 */
public class AjaxIndicatingTabbedPanel
    extends AjaxTabbedPanel<ITab>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicketId of the component
     * @param _tabs tabs to be added
     */
    public AjaxIndicatingTabbedPanel(final String _wicketId,
                                     final List<ITab> _tabs)
    {
        super(_wicketId, _tabs);
    }

    @Override
    protected WebMarkupContainer newLink(final String _linkId,
                                         final int _index)
    {
        return new IndicatingAjaxLink(_linkId)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final Optional<AjaxRequestTarget> _targetOptional)
            {
                setSelectedTab(_index);
                EFapsSession.get().setAttribute(DashboardPage.class.getName() + ".SelectedTab", _index);
                _targetOptional.ifPresent(target -> target.add(AjaxIndicatingTabbedPanel.this));
                onAjaxUpdate(_targetOptional);
            }
        };
    }
}
