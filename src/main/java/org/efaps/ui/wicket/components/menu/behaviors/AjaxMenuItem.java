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
package org.efaps.ui.wicket.components.menu.behaviors;

import org.apache.wicket.markup.html.WebComponent;

/**
 * The Class AjaxMenuItem.
 *
 * @author The eFaps Team
 */
public class AjaxMenuItem
    extends WebComponent
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new ajax menu item.
     *
     * @param _wicketId the wicket id
     */
    public AjaxMenuItem(final String _wicketId)
    {
        super(_wicketId);
        add(new ExecBehavior());
        add(new OpenModalBehavior());
    }
}
