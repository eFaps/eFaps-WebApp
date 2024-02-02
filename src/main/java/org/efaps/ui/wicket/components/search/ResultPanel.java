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
package org.efaps.ui.wicket.components.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.index.SearchConfig;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.json.index.result.Element;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ResultPanel.
 *
 * @author The eFaps Team
 */
public class ResultPanel
    extends GenericPanel<IndexSearch>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ResultPanel.class);

    /**
     * Instantiates a new result panel.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     */
    public ResultPanel(final String _wicketId,
                       final Model<IndexSearch> _model)
    {
        super(_wicketId, _model);
        add(new Label("noResult", Model.of("")));
        add(new Label("hits", Model.of("")));
        add(new DimensionPanel("dimension", _model));
        add(getDataTable(_model.getObject()));
    }

    /**
     * Gets the data table.
     *
     * @param _indexSearch the index search
     * @return the data table
     */
    private DataTable<Element, Void> getDataTable(final IndexSearch _indexSearch)
    {
        final List<IColumn<Element, Void>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<Element, Void>(new Model<>(""))
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<Element>> _cellItem,
                                     final String _componentId,
                                     final IModel<Element> _rowModel)
            {
                _cellItem.add(new Link(_componentId, _rowModel));
            }
        });

        if (_indexSearch.getSearch() == null || _indexSearch.getSearch().getResultFields().isEmpty()) {
            columns.add(new PropertyColumn<Element, Void>(new Model<>(""), "text"));
        } else {
            for (final Entry<String, Collection<String>> entry : _indexSearch.getSearch().getResultFields()
                            .entrySet()) {
                columns.add(new ResultColumn(_indexSearch.getSearch().getResultLabel().get(entry.getKey()), entry
                                .getValue()));
            }
        }
        final DataTable<Element, Void> ret = new DataTable<>("table", columns, _indexSearch
                        .getDataProvider(), _indexSearch.getSearch() == null ? 100
                                        : _indexSearch.getSearch().getNumHits());

        ret.addTopToolbar(new HeadersToolbar<>(ret, null));
        return ret;
    }

    /**
     * Update.
     *
     * @param _target the target
     * @param _indexSearch the index search
     */
    public void update(final AjaxRequestTarget _target,
                       final IndexSearch _indexSearch)
    {

        ResultPanel.this.visitChildren(DimensionPanel.class, new IVisitor<Component, Void>()
        {
            @Override
            public void component(final Component _component,
                                  final IVisit<Void> _visit)
            {
                try {
                    if (_indexSearch.getSearch().getConfigs().contains(SearchConfig.ACTIVATE_DIMENSION)) {
                        _component.setVisible(true);
                    } else {
                        _component.setVisible(false);
                    }
                } catch (final EFapsException e) {
                    ResultPanel.LOG.error("Catched error", e);
                }
                _visit.dontGoDeeper();
            }
        });

        ResultPanel.this.visitChildren(DataTable.class, new IVisitor<Component, Void>()
        {
            @Override
            public void component(final Component _component,
                                  final IVisit<Void> _visit)
            {
                if (_indexSearch.getResult().getElements().isEmpty()) {
                    _component.setVisible(false);
                } else {
                    _component.setVisible(true);
                }
                if (_indexSearch.isUpdateTable()) {
                    _component.replaceWith(getDataTable(_indexSearch));
                }
                _visit.dontGoDeeper();
            }
        });

        ResultPanel.this.visitChildren(Label.class, new IVisitor<Component, Void>()
        {

            @Override
            public void component(final Component _component,
                                  final IVisit<Void> _visit)
            {
                final String compid = _component.getId();
                switch (compid) {
                    case "hits":
                        if (_indexSearch.getResult().getElements().isEmpty()) {
                            _component.setVisible(false);
                        } else {
                            _component.setVisible(true);
                            ((Label) _component).setDefaultModelObject(DBProperties.getFormatedDBProperty(
                                            ResultPanel.class.getName() + ".Hits", _indexSearch.getResult()
                                                            .getElements().size(), _indexSearch.getResult()
                                                                            .getHitCount()));
                        }
                        break;
                    case "noResult":
                        if (_indexSearch.getResult().getElements().isEmpty()) {
                            _component.setVisible(true);
                            ((Label) _component).setDefaultModelObject(DBProperties.getProperty(ResultPanel.class
                                            .getName() + ".NoResult"));
                        } else {
                            _component.setVisible(false);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * The Class Link.
     *
     * @author The eFaps Team
     */
    public static class Link
        extends AjaxLink<Element>
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new link.
         *
         * @param _wicketId the wicket id
         * @param _model the model
         */
        public Link(final String _wicketId,
                    final IModel<Element> _model)
        {
            super(_wicketId, _model);
            setBody(Model.of(""));
            add(new AttributeAppender("class", "eFapsLink", " "));
        }

        @Override
        @SuppressWarnings("checkstyle:illegalcatch")
        public void onClick(final AjaxRequestTarget _target)
        {
            final ResultPanel resultPanel = findParent(ResultPanel.class);
            final Element element = (Element) getDefaultModelObject();
            final Menu menu;
            final Instance instance;
            try {
                instance = Instance.get(element.getOid());
                menu = Menu.getTypeTreeMenu(instance.getType());
            } catch (final Exception e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
            if (menu == null) {
                final Exception ex = new Exception("no tree menu defined for type " + instance.getType().getName());
                throw new RestartResponseException(new ErrorPage(ex));
            }
            Page page;
            try {
                page = new ContentContainerPage(menu.getUUID(), instance.getOid());
            } catch (final EFapsException e) {
                page = new ErrorPage(e);
            }

            final CharSequence pageUrl;
            final RequestCycle requestCycle = RequestCycle.get();

            page.getSession().getPageManager().touchPage(page);
            if (page.isPageStateless()) {
                pageUrl = requestCycle.urlFor(page.getClass(), page.getPageParameters());
            } else {
                final IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(page));
                pageUrl = requestCycle.urlFor(handler);
            }

            final StringBuilder js = new StringBuilder();
            js.append("var rN = dom.byId('").append(resultPanel.getMarkupId()).append("');\n")
                .append("var dialog = registry.byId(rN.id);\n")
                .append("popup.close(dialog);\n")
                .append("registry.byId(\"").append("mainPanel")
                .append("\").set(\"content\", domConstruct.create(\"iframe\", {")
                .append("\"id\": \"").append(MainPage.IFRAME_ID)
                .append("\",\"src\": \"").append(pageUrl)
                .append("\",\"style\": \"border: 0; width: 100%; height: 99%\"")
                .append(",\"id\": \"").append(MainPage.IFRAME_ID).append("\"")
                .append("}));");
            _target.appendJavaScript(DojoWrapper.require(js, DojoClasses.TooltipDialog, DojoClasses.popup,
                            DojoClasses.dom, DojoClasses.domConstruct, DojoClasses.registry));
        }
    }

    /**
     * The Class ResultColumn.
     */
    public static class ResultColumn
        extends AbstractColumn<Element, Void>
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /** The keys. */
        private final Collection<String> keys;

        /**
         * Instantiates a new result column.
         *
         * @param _label the label
         * @param _keys the keys
         */
        public ResultColumn(final String _label,
                            final Collection<String> _keys)
        {
            super(Model.of(_label));
            this.keys = _keys;
        }

        @Override
        public void populateItem(final Item<ICellPopulator<Element>> _cellItem,
                                 final String _componentId,
                                 final IModel<Element> _rowModel)
        {
            String val = "";
            for (final String key : this.keys) {
                if (_rowModel.getObject().getFields().containsKey(key)) {
                    val = _rowModel.getObject().getFields().get(key);
                    break;
                }
            }
            _cellItem.add(new Label(_componentId, val));
        }
    }
}
