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
package org.efaps.ui.wicket.components.gridx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.field.Field;
import org.efaps.api.ui.IClassificationFilter;
import org.efaps.api.ui.IFilter;
import org.efaps.api.ui.IListFilter;
import org.efaps.api.ui.IMapFilter;
import org.efaps.api.ui.IOption;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.gridx.filter.ClassificationFilter;
import org.efaps.ui.wicket.components.gridx.filter.DateFilterPanel;
import org.efaps.ui.wicket.components.gridx.filter.FormFilterPanel;
import org.efaps.ui.wicket.components.gridx.filter.ListFilterPanel;
import org.efaps.ui.wicket.components.gridx.filter.TextFilterPanel;
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
            container.add(AttributeModifier.replace("title", Model.of(DBProperties.getFormatedDBProperty(
                            GridXPanel.class.getName() + ".FilterTitel", (Object) field.getLabel()))));
            container.setOutputMarkupId(true);
            container.setMarkupId("fttd_" + filter.getFieldId());
            filterRepeater.add(container);
            final Form<Void> form = new Form<>("filterForm");
            container.add(form);
            switch (field.getFilter().getType()) {
                case FREETEXT:
                    container.add(new WebMarkupContainer("formFilter"));
                    if (_model.getObject().isDateFilter(filter)) {
                        form.add(new DateFilterPanel("filter", Model.of((IMapFilter) filter)));
                        form.add(new AjaxButton<IMapFilter>("btn", Model.of((IMapFilter) filter), AjaxButton.ICON.ACCEPT
                                        .getReference())
                        {

                            /** The Constant serialVersionUID. */
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onRequest(final AjaxRequestTarget _target)
                            {
                                final GridXPanel gridpanel = findParent(GridXPanel.class);
                                final UIGrid uiGrid = gridpanel.getModelObject();
                                form.visitChildren(DateTimePanel.class, (_datePanel,
                                                                         _visit) -> {
                                    try {
                                        if ("dateFrom".equals(_datePanel.getId())) {
                                            final List<StringValue> tmp1 = getRequest().getRequestParameters()
                                                            .getParameterValues(((DateTimePanel) _datePanel)
                                                                            .getDateFieldName());
                                            if (!tmp1.isEmpty()) {
                                                final List<StringValue> fromTmp = ((DateTimePanel) _datePanel)
                                                                .getDateAsString(tmp1, null, null, null);
                                                if (!fromTmp.isEmpty()) {
                                                    final String from = fromTmp.get(0).toString();
                                                    final DateTime date1 = DateTimeUtil.translateFromUI(from);
                                                    ((IMapFilter) filter).put("from", date1.toString());
                                                }
                                            }
                                        } else {
                                            final List<StringValue> tmp2 = getRequest().getRequestParameters()
                                                            .getParameterValues(((DateTimePanel) _datePanel)
                                                                            .getDateFieldName());
                                            if (!tmp2.isEmpty()) {
                                                final List<StringValue> toTmp = ((DateTimePanel) _datePanel)
                                                                .getDateAsString(tmp2, null, null, null);
                                                if (toTmp != null) {
                                                    final String to = toTmp.get(0).toString();
                                                    final DateTime date2 = DateTimeUtil.translateFromUI(to);
                                                    ((IMapFilter) filter).put("to", date2.toString());
                                                }
                                            }
                                        }
                                    } catch (final EFapsException e) {
                                        GridXPanel.LOG.error("Catched error", e);
                                    }
                                });

                                try {
                                    uiGrid.reload();
                                    _target.appendJavaScript(GridXComponent.getDataReloadJS(uiGrid));
                                } catch (final EFapsException e) {
                                    GridXPanel.LOG.error("Catched error", e);
                                }
                            }
                        });
                    } else {
                        form.add(new TextFilterPanel("filter", Model.of((IMapFilter) filter)));
                        form.add(new AjaxButton<IMapFilter>("btn", Model.of((IMapFilter) filter), AjaxButton.ICON.ACCEPT
                                        .getReference())
                        {

                            /** The Constant serialVersionUID. */
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onRequest(final AjaxRequestTarget _target)
                            {
                                final GridXPanel gridpanel = findParent(GridXPanel.class);
                                final UIGrid uiGrid = gridpanel.getModelObject();

                                form.visitChildren(TextField.class, (_text,
                                                                     _visit) -> {
                                    ((IMapFilter) filter).put("from", ((TextField<?>) _text).getModelObject());
                                    ((IMapFilter) filter).put("filter", ((TextField<?>) _text).getModelObject());
                                });

                                form.visitChildren(CheckBox.class, (_checkBox,
                                                                    _visit) -> ((IMapFilter) filter)
                                                .put(_checkBox.getId(), ((CheckBox) _checkBox).getModelObject()));

                                try {
                                    uiGrid.reload();
                                    _target.appendJavaScript(GridXComponent.getDataReloadJS(uiGrid));
                                } catch (final EFapsException e) {
                                    GridXPanel.LOG.error("Catched error", e);
                                }
                            }
                        });

                    }
                    break;
                case STATUS:
                    container.add(new WebMarkupContainer("formFilter"));
                    form.add(new ListFilterPanel("filter", new Model<>((IListFilter) filter)));
                    form.add(new AjaxButton<IListFilter>("btn", new Model<>((IListFilter) filter),
                                    AjaxButton.ICON.ACCEPT.getReference())
                    {

                        /** The Constant serialVersionUID. */
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onRequest(final AjaxRequestTarget _target)
                        {
                            final GridXPanel gridpanel = findParent(GridXPanel.class);
                            final UIGrid uiGrid = gridpanel.getModelObject();
                            form.visitChildren(CheckBoxMultipleChoice.class, (_checkBox,
                                                                              _visit) -> {
                                try {
                                    final ListFilterPanel filterPanel = _checkBox.findParent(ListFilterPanel.class);
                                    @SuppressWarnings("unchecked")
                                    final List<IOption> sel = (List<IOption>) _checkBox.getDefaultModelObject();
                                    for (final IOption option : filterPanel.getModelObject()) {
                                        final Method method = option.getClass().getMethod("setSelected", boolean.class);
                                        method.invoke(option, sel.contains(option));
                                    }
                                } catch (final IllegalAccessException | InvocationTargetException
                                                | NoSuchMethodException e) {
                                    GridXPanel.LOG.error("Catched error", e);
                                }
                            });

                            try {
                                uiGrid.reload();
                                _target.appendJavaScript(GridXComponent.getDataReloadJS(uiGrid));
                            } catch (final EFapsException e) {
                                GridXPanel.LOG.error("Catched error", e);
                            }
                        }
                    });
                    break;
                case FORM:
                    form.setVisible(false);
                    container.add(new FormFilterPanel("formFilter", new Model<>((IMapFilter) filter),
                                    getModelObject()));
                    break;
                case CLASSIFICATION:
                    container.add(new WebMarkupContainer("formFilter"));
                    form.add(new ClassificationFilter("filter", new Model<>((IClassificationFilter) filter)));
                    form.add(new AjaxButton<IClassificationFilter>("btn", new Model<>((IClassificationFilter) filter),
                                    AjaxButton.ICON.ACCEPT.getReference())
                    {
                        /** The Constant serialVersionUID. */
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onRequest(final AjaxRequestTarget _target)
                        {
                            final List<StringValue> values = getPage().getRequest().getRequestParameters()
                                            .getParameterValues(ClassificationFilter.INPUTNAME);
                            ((IClassificationFilter) filter).clear();
                            if (CollectionUtils.isNotEmpty(values)) {
                                for (final StringValue value : values) {
                                    ((IClassificationFilter) filter).add(UUID.fromString(value.toString()));
                                }
                            }
                            try {
                                final GridXPanel gridpanel = findParent(GridXPanel.class);
                                final UIGrid uiGrid = gridpanel.getModelObject();
                                uiGrid.reload();
                                _target.appendJavaScript(GridXComponent.getDataReloadJS(uiGrid));
                            } catch (final EFapsException e) {
                                GridXPanel.LOG.error("Catched error", e);
                            }
                        }
                    });
                    break;
                case PICKLIST:
                case NONE:
                default:
                    container.add(new WebMarkupContainer("formFilter"));
                    form.add(new WebMarkupContainer("filter"));
                    form.add(new WebMarkupContainer("btn"));
                    break;
            }
        }
    }
}
