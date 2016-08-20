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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;
import org.efaps.api.ui.FilterBase;
import org.efaps.api.ui.FilterType;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.table.filter.FreeTextPanel;
import org.efaps.ui.wicket.components.table.filter.StatusPanel;
import org.efaps.ui.wicket.models.objects.UIStatusSet;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.models.objects.UITableHeader.FilterValueType;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
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

        final RepeatingView filterRepeater = new RepeatingView("filterRepeater");
        add(filterRepeater);

        for (final UITableHeader header : _model.getObject().getHeaders()) {
            if (header.getFilter() != null && FilterBase.DATABASE.equals(header.getFilter().getBase())) {
                final WebMarkupContainer container = new WebMarkupContainer(filterRepeater.newChildId());
                container.setOutputMarkupId(true);
                container.setMarkupId("fttd_" + header.getFieldId());
                filterRepeater.add(container);
                final Form<Void> form = new Form<>("filterForm");
                container.add(form);
                if (FilterValueType.DATE.equals(header.getFilterType())) {
                    final FreeTextPanel freeTextPanel = new FreeTextPanel("filter", Model.of(header));
                    form.add(freeTextPanel);
                    form.add(new AjaxButton<UITableHeader>("btn", Model.of(header))
                    {

                        /** The Constant serialVersionUID. */
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onSubmit(final AjaxRequestTarget _target)
                        {
                            final UITable uiTable = _model.getObject();
                            try {
                                final Iterator<? extends Component> iter = freeTextPanel.iterator();
                                String from = null;
                                String to = null;
                                while (iter.hasNext()) {
                                    final Component comp = iter.next();
                                    if (comp instanceof DateTimePanel) {
                                        final DateTimePanel datePanel = (DateTimePanel) comp;
                                        if (datePanel.getId().equals(freeTextPanel.getFromFieldName())) {
                                            final List<StringValue> tmp = getRequest().getRequestParameters()
                                                            .getParameterValues(
                                                                            datePanel.getDateFieldName());
                                            if (!tmp.isEmpty()) {
                                                final List<StringValue> fromTmp = datePanel.getDateAsString(tmp,
                                                                null, null, null);
                                                if (!fromTmp.isEmpty()) {
                                                    from = fromTmp.get(0).toString();
                                                }
                                            }
                                        } else {
                                            final List<StringValue> tmp = getRequest().getRequestParameters()
                                                            .getParameterValues(datePanel.getDateFieldName());
                                            if (!tmp.isEmpty()) {
                                                final List<StringValue> toTmp = datePanel
                                                                .getDateAsString(tmp, null, null, null);
                                                if (toTmp != null) {
                                                    to = toTmp.get(0).toString();
                                                }
                                            }
                                        }
                                    }
                                }
                                uiTable.addFilterRange(header, from, to);
                                uiTable.resetModel();
                                uiTable.execute();
                                _target.appendJavaScript(getJavascript(uiTable));
                            } catch (final EFapsException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });
                } else if (FilterType.STATUS.equals(header.getFilter().getType())) {
                    final StatusPanel statusPanel = new StatusPanel("filter", Model.of(header));
                    form.add(statusPanel);
                    form.add(new AjaxButton<UITableHeader>("btn", Model.of(header))
                    {

                        /** The Constant serialVersionUID. */
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onSubmit(final AjaxRequestTarget _target)
                        {
                            final UITable uiTable = _model.getObject();
                            try {
                                final List<StringValue> selection = getRequest().getRequestParameters()
                                                .getParameterValues(StatusPanel.CHECKBOXNAME);
                                if (selection != null) {
                                    final List<UIStatusSet> sets = statusPanel.getStatusSets();
                                    final UITableHeader uitableHeader = (UITableHeader) getDefaultModelObject();

                                    // all value are selected and the not required,
                                    // meaning that nothing must be filtered
                                    if (selection.size() == sets.size() && !uitableHeader.getFilter().isRequired()) {
                                        _model.getObject().removeFilter(uitableHeader);
                                    } else {
                                        final Set<Object> filterList = new HashSet<>();
                                        for (final StringValue value : selection) {
                                            final Integer intpos = Integer.valueOf(value.toString());
                                            filterList.addAll(sets.get(intpos).getIds());
                                        }
                                        uiTable.addFilterList(uitableHeader, filterList);
                                        uiTable.resetModel();
                                        uiTable.execute();
                                    }
                                }
                                _target.appendJavaScript(getJavascript(uiTable));
                            } catch (final EFapsException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    });
                }
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
    private CharSequence getJavascript(final UITable _uiTable)
        throws EFapsException
    {
        final StringBuilder js = new StringBuilder()
                    .append("require([")
                    .append("'dijit/registry']")
                    .append(", function (registry) {\n")
                    .append("var grid = registry.byId('grid');\n")
                    .append("var items= ")
                    .append(GridXComponent.getDataJS(_uiTable))
                    .append("grid.model.clearCache();\n")
                    .append("grid.model.store.setData(items);\n")
                    .append("grid.body.refresh();\n")
                    .append("});");
        return js;
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(TablePanel.CSS));
    }
}
