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

package org.efaps.ui.wicket.components.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.json.index.SearchResult;
import org.efaps.json.index.SearchResult.Element;

/**
 * The Class ResultPanel.
 *
 * @author The eFaps Team
 */
public class ResultPanel
    extends Panel
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The provider. */
    private final ElementDataProvider provider;

    /**
     * Instantiates a new result panel.
     *
     * @param _wicketId the wicket id
     */
    public ResultPanel(final String _wicketId)
    {
        super(_wicketId);
        add(new Label("label", Model.of("")));

        this.provider = new ElementDataProvider();

        final List<IColumn<SearchResult.Element, Void>> columns = new ArrayList<>();

        columns.add(new AbstractColumn<SearchResult.Element, Void>(new Model<String>(""))
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(final Item<ICellPopulator<SearchResult.Element>> _cellItem,
                                     final String _componentId,
                                     final IModel<SearchResult.Element> _rowModel)
            {
                _cellItem.add(new Link(_componentId, _rowModel));
            }
        });

        columns.add(new PropertyColumn<SearchResult.Element, Void>(new Model<String>("Last Name"), "text"));
        add(new DataTable<SearchResult.Element, Void>("table", columns, this.provider, 25));
    }

    /**
     * Update.
     *
     * @param _result the result
     */
    public void update(final SearchResult _result)
    {

        ResultPanel.this.visitChildren(new IVisitor<Component, Void>()
        {

            @Override
            public void component(final Component _component,
                                  final IVisit<Void> _visit)
            {
                final String compid = _component.getId();
                switch (compid) {
                    case "label":
                        if (_result.getElements().isEmpty()) {
                            _component.setVisible(true);
                            ((Label) _component).setDefaultModelObject("no hay resultados");
                        } else {
                            _component.setVisible(false);
                        }
                        break;
                    case "table":
                        if (_result.getElements().isEmpty()) {
                            _component.setVisible(false);
                        } else {
                            _component.setVisible(true);
                            ResultPanel.this.provider.setElements(_result.getElements());
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * The Class ElementDataProvider.
     *
     * @author The eFaps Team
     */
    public static class ElementDataProvider
        extends ListDataProvider<Element>
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The elements. */
        private List<SearchResult.Element> elements = new ArrayList<>();

        @Override
        protected List<Element> getData()
        {
            return this.elements;
        }

        /**
         * Gets the elements.
         *
         * @return the elements
         */
        public List<SearchResult.Element> getElements()
        {
            return this.elements;
        }

        /**
         * Sets the elements.
         *
         * @param _elements the new elements
         */
        public void setElements(final List<SearchResult.Element> _elements)
        {
            this.elements = _elements;
        }
    }

    /**
     * The Class Link.
     *
     * @author The eFaps Team
     */
    public static class Link
        extends AjaxLink<SearchResult.Element>
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
                    final IModel<SearchResult.Element> _model)
        {
            super(_wicketId, _model);
            setBody(Model.of(""));
            add(new AttributeAppender("class", "eFapsLink", " "));
        }

        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final ResultPanel resultPanel = findParent(ResultPanel.class);
            final StringBuilder js = new StringBuilder();
            js.append("require(['dijit/TooltipDialog','dijit/popup','dojo/dom',")
                .append("'dijit/registry'], function (TooltipDialog, popup, dom, registry) {\n")
                .append("var rN = dom.byId('").append(resultPanel.getMarkupId()).append("');\n")
                .append("var dialog = registry.byId(rN.id);\n")
                .append("popup.close(dialog);\n")
                .append("});\n");
            _target.appendJavaScript(js);
        }
    }
}
