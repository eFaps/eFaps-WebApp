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
package org.efaps.ui.wicket.components.table;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.api.ui.FilterBase;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.table.filter.StatusPanel;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.util.EFapsException;

/**
 * The Class GridXPanel.
 *
 * @author The eFaps Team
 */
public class GridXPanel
    extends GenericPanel<UITable>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new grid X panel.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @throws EFapsException on error
     */
    public GridXPanel(final String _wicketId,
                      final IModel<UITable> _model)
                    throws EFapsException
    {
        super(_wicketId, _model);
        add(new GridXComponent("grid", _model));
        for (final UITableHeader header : _model.getObject().getHeaders()) {
            if (header.getFilter() != null && FilterBase.DATABASE.equals(header.getFilter().getBase())) {
                add(new StatusPanel("filter", Model.of(header)));
                add(new AjaxButton<UITableHeader>("btn", Model.of(header))
                {

                    /** The Constant serialVersionUID. */
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onSubmit(final AjaxRequestTarget _target)
                    {
                        System.out.println(_target);
                    }
                });
            }
        }
    }
}
