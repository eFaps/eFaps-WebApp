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
package org.efaps.ui.wicket.components.menu;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.menu.behaviors.AjaxMenuItem;
import org.efaps.ui.wicket.models.objects.UIMenuItem;

/**
 * The Class SlideInPanel.
 *
 * @author The eFaps Team
 */
public class SlideInPanel
    extends GenericPanel<UIMenuItem>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new slide in panel.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     */
    public SlideInPanel(final String _wicketId,
                        final IModel<UIMenuItem> _model)
    {
        super(_wicketId, _model);
        add(new SlideIn("slidein", _model));
        add(new AjaxMenuItem("ajaxMenuItem"));
    }
}
