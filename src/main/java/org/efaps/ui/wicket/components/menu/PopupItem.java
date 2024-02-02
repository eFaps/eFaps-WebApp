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

import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;


public class PopupItem
    extends LinkItem
{
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId Wicket id for this component
     * @param _model    model for this component
     */
    public PopupItem(final String _wicketId,
                     final IModel<UIMenuItem> _model)
    {
        super(_wicketId, _model);
        final PopupSettings popupsetting = new PopupSettings();
        popupsetting.setHeight(_model.getObject().getWindowHeight());
        popupsetting.setWidth(_model.getObject().getWindowWidth());
        popupsetting.setWindowName("eFapsPopup");
        setPopupSettings(popupsetting);
    }

    /**
     * Checks if is popup.
     *
     * @return true, if is popup
     */
    @Override
    protected boolean isPopup()
    {
        return true;
    }

    @Override
    protected boolean useJSEventBindingWhenNeeded()
    {
        return true;
    }
}
