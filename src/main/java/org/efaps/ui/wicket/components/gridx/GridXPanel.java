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

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.field.Field;
import org.efaps.api.ui.IFilter;
import org.efaps.api.ui.IMapFilter;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.gridx.filter.DateFilterPanel;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.util.DateTimeUtil;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
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
        for (final IFilter filter : _model.getObject().getFilterList()) {
            final Field field = Field.get(filter.getFieldId());
            final WebMarkupContainer container = new WebMarkupContainer(filterRepeater.newChildId());
            container.add(AttributeModifier.replace("title",
                            Model.of(DBProperties.getFormatedDBProperty(GridXPanel.class.getName() + ".FilterTitel",
                                            (Object) field.getLabel()))));
            container.setOutputMarkupId(true);
            container.setMarkupId("fttd_" + filter.getFieldId());
            filterRepeater.add(container);
            final Form<Void> form = new Form<>("filterForm");
            container.add(form);
            switch (field.getFilter().getType()) {
                case FREETEXT:
                    if (_model.getObject().isDateFilter(filter)) {
                        form.add(new DateFilterPanel("filter", Model.of((IMapFilter) filter)));
                        form.add(new AjaxButton<IMapFilter>("btn", Model.of((IMapFilter) filter), Button.ICON.ACCEPT
                                        .getReference())
                        {

                            /** The Constant serialVersionUID. */
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onSubmit(final AjaxRequestTarget _target)
                            {
                                final GridXPanel gridpanel = findParent(GridXPanel.class);
                                final UIGrid uiGrid = gridpanel.getModelObject();

                                form.visitChildren(DateTimePanel.class, new IVisitor<DateTimePanel, Void>()
                                {

                                    @Override
                                    public void component(final DateTimePanel _datePanel,
                                                          final IVisit<Void> _visit)
                                    {
                                        try {
                                            if ("dateFrom".equals(_datePanel.getId())) {
                                                final List<StringValue> tmp = getRequest().getRequestParameters()
                                                                .getParameterValues(_datePanel.getDateFieldName());
                                                if (!tmp.isEmpty()) {
                                                    final List<StringValue> fromTmp = _datePanel.getDateAsString(tmp,
                                                                    null, null, null);
                                                    if (!fromTmp.isEmpty()) {
                                                        final String from = fromTmp.get(0).toString();
                                                        final DateTime date = DateTimeUtil.translateFromUI(from);
                                                        ((IMapFilter) filter).put("from", date.toString());
                                                    }
                                                }
                                            } else {
                                                final List<StringValue> tmp = getRequest().getRequestParameters()
                                                                .getParameterValues(_datePanel.getDateFieldName());
                                                if (!tmp.isEmpty()) {
                                                    final List<StringValue> toTmp = _datePanel.getDateAsString(tmp,
                                                                    null, null, null);
                                                    if (toTmp != null) {
                                                        final String to = toTmp.get(0).toString();
                                                        final DateTime date = DateTimeUtil.translateFromUI(to);
                                                        ((IMapFilter) filter).put("to", date.toString());
                                                    }
                                                }
                                            }
                                        } catch (final EFapsException e) {
                                            GridXPanel.LOG.error("Catched error", e);
                                        }
                                    }
                                });

                                try {
                                    uiGrid.reload();
                                    _target.appendJavaScript(getJavascript(uiGrid));
                                } catch (final EFapsException e) {
                                    GridXPanel.LOG.error("Catched error", e);
                                }
                            }
                        });
                    } else {

                    }
                        break;
                    case STATUS:
                    case CLASSIFICATION:
                    case FORM:
                    case PICKLIST:
                    case NONE:

                    default:
                        form.add(new WebMarkupContainer("filter"));
                        form.add(new WebMarkupContainer("btn"));
                        break;
                }


        }
    }


    /**
     * Gets the javascript.
     *
     * @param _uiTable the ui table
     * @return the javascript
     * @throws EFapsException on error
     */
    private CharSequence getJavascript(final UIGrid _uiGrid)
        throws EFapsException
    {
        final StringBuilder js = new StringBuilder()
                    .append("require([")
                    .append("'dijit/registry']")
                    .append(", function (registry) {\n")
                    .append("var grid = registry.byId('grid');\n")
                    .append("var items= ")
                    .append(GridXComponent.getDataJS(_uiGrid))
                    .append("grid.model.clearCache();\n")
                    .append("grid.model.store.setData(items);\n");

        js.append("grid.body.refresh();\n")
                    .append("});");
        return js;
    }
}
