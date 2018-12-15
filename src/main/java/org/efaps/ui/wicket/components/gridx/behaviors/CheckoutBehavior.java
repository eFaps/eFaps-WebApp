/*
 * Copyright 2003 - 2018 The eFaps Team
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

import java.io.File;
import java.util.List;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.models.objects.grid.Cell;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author The eFaps Team
 */
public class CheckoutBehavior
    extends AjaxEventBehavior
    implements IAjaxIndicatorAware
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CheckoutBehavior.class);

    /**
     * @param _event
     */
    public CheckoutBehavior()
    {
        super("click");
    }

    @Override
    protected void onEvent(final AjaxRequestTarget _target)
    {
        final StringValue rowId = getComponent().getRequest().getRequestParameters().getParameterValue("rowId");
        final StringValue colId = getComponent().getRequest().getRequestParameters().getParameterValue("colId");
        try {

            final UIGrid uiGrid = (UIGrid) getComponent().getPage().getDefaultModelObject();
            final List<Cell> row = uiGrid.getValues().get(rowId.toInt());
            final Cell cell = row.get(colId.toInt());

            if (cell.getInstance() != null) {
                final File file = UIGrid.checkout(cell.getInstance());
                if (file != null) {
                    ((EFapsSession) getComponent().getSession()).setFile(file);
                    ((GridPage) getComponent().getPage()).getDownloadBehavior().initiate(_target);
                }
            }
        } catch (final StringValueConversionException | EFapsException e) {
            CheckoutBehavior.LOG.error("Catched error", e);
        }
    }

    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return "eFapsVeil";
    }
}
