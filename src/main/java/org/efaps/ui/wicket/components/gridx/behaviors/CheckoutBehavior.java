/*
 * Copyright 2003 - 2016 The eFaps Team
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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.CallbackParameter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UIGrid.Cell;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
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

    /** The render. */
    private boolean render;

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
            LOG.error("Catched error", e);
        }
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        if (isRender()) {
            super.renderHead(_component, _response);
            final StringBuilder js = new StringBuilder()
                            .append("function checkOut(rowId, colId) {\n")
                            .append(getCallbackFunctionBody(CallbackParameter.explicit("rowId"),
                            CallbackParameter.explicit("colId")))
                            .append(" return false; }\n");
            _response.render(JavaScriptHeaderItem.forScript(js, CheckoutBehavior.class.getName()));
        }
    }

    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return "eFapsVeil";
    }

    /**
     * Getter method for the instance variable {@link #render}.
     *
     * @return value of instance variable {@link #render}
     */
    public boolean isRender()
    {
        return this.render;
    }

    /**
     * Setter method for instance variable {@link #render}.
     *
     * @param _render value for instance variable {@link #render}
     */
    public void setRender(final boolean _render)
    {
        this.render = _render;
    }
}
