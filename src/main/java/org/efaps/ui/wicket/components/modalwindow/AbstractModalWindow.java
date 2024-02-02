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
package org.efaps.ui.wicket.components.modalwindow;

import org.apache.wicket.model.IModel;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;


/**
 * Abstract Modal window to set the stylesheets equaly for all modal in eFaps.
 *
 * @author The eFaps Team
 */
public abstract class AbstractModalWindow
    extends LegacyModalWindow
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id for this component
     */
    public AbstractModalWindow(final String _wicketId)
    {
        super(_wicketId);
        initialize();
    }

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     */
    public AbstractModalWindow(final String _wicketId,
                               final IModel<?> _model)
    {
        super(_wicketId, _model);
        initialize();
    }

    /**
     * Initialize.
     */
    protected void initialize()
    {
        if ("w_silver".equals(Configuration.getAttribute(ConfigAttribute.DOJO_MODALCLASS))) {
            setCssClassName(LegacyModalWindow.CSS_CLASS_GRAY);
        } else if ("w_blue".equals(Configuration.getAttribute(ConfigAttribute.DOJO_MODALCLASS))) {
            setCssClassName(LegacyModalWindow.CSS_CLASS_BLUE);
        }
        showUnloadConfirmation(false);
        setTitle(DBProperties.getProperty("Logo.Version.Label"));
    }
}
