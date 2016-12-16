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
package org.efaps.ui.wicket.components.gridx;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.api.ui.FilterBase;
import org.efaps.api.ui.FilterType;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UIGrid.Column;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GridXPanel.
 *
 * @author The eFaps Team
 */
public class GridXPanel
    extends GenericPanel<UIGrid>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GridXPanel.class);

    /**
     * Instantiates a new grid X panel.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @throws EFapsException on error
     */
    public GridXPanel(final String _wicketId,
                      final IModel<UIGrid> _model)
        throws EFapsException
    {
        super(_wicketId, _model);
        add(new AbstractDojoBehavior()
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;
        });
        // add a hidden element that has all the events used by the menu
        add(new MenuItem("menuItem"));

        add(new GridXComponent("grid", new LoadableDetachableModel<UIGrid>()
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            protected UIGrid load()
            {
                return _model.getObject();
            }
        }));
        final RepeatingView filterRepeater = new RepeatingView("filterRepeater");
        add(filterRepeater);
        for (final Column column : _model.getObject().getColumns()) {
            if (FilterBase.DATABASE.equals(column.getField().getFilter().getBase())) {
                final WebMarkupContainer container = new WebMarkupContainer(filterRepeater.newChildId());
                container.add(AttributeModifier.replace("title",
                                Model.of(DBProperties.getFormatedDBProperty(GridXPanel.class.getName() + ".FilterTitel",
                                                (Object) column.getField().getLabel()))));
                container.setOutputMarkupId(true);
                container.setMarkupId("fttd_" + column.getField().getId());
                filterRepeater.add(container);
                final Form<Void> form = new Form<>("filterForm");
                container.add(form);
                if (FilterType.STATUS.equals(column.getField().getFilter().getType())) {

                }
            }
        }
    }
}
