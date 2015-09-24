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
package org.efaps.ui.wicket.components.dashboard;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.EsjpInvoker;

/**
 * The Class DashboardPanel.
 *
 * @author The eFaps Team
 */
public class DashboardPanel
    extends AjaxLazyLoadPanel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new panel.
     *
     * @param _wicketId the _wicket id
     * @param _model the _model
     */
    public DashboardPanel(final String _wicketId,
                          final IModel<EsjpInvoker> _model)
    {
        super(_wicketId, _model);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getLazyLoadComponent(final String _markupId)
    {
        return new EsjpComponent(_markupId, (IModel<EsjpInvoker>) getDefaultModel());
    }

    @Override
    public boolean isVisible()
    {
        final EsjpInvoker invoker = (EsjpInvoker) getDefaultModelObject();
        return invoker.isVisible();
    }
}
