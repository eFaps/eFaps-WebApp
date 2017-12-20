/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.components.gridx.behaviors;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.efaps.ui.wicket.components.gridx.GridXComponent;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 */
public class ReloadBehavior
    extends AjaxEventBehavior
    implements IAjaxIndicatorAware
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReloadBehavior.class);

    /**
     * @param _event
     */
    public ReloadBehavior()
    {
        super("click");
    }

    @Override
    protected void onEvent(final AjaxRequestTarget _target)
    {
        try {
            final UIGrid uiGrid = (UIGrid) getComponent().getPage().getDefaultModelObject();
            uiGrid.reload();
            _target.appendJavaScript(GridXComponent.getDataReloadJS(uiGrid));
        } catch (final EFapsException e) {
            LOG.error("Catched ", e);
        }
    }

    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return "eFapsVeil";
    }
}
