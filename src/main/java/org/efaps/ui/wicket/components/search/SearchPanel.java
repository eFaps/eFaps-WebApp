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

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.index.Search;
import org.efaps.json.index.SearchResult;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SearchPanel.
 *
 * @author The eFaps Team
 */
public class SearchPanel
    extends Panel
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SearchPanel.class);

    /**
     * Instantiates a new search panel.
     *
     * @param _wicketId the wicket id
     */
    public SearchPanel(final String _wicketId)
    {
        super(_wicketId);
        final ResultPanel resultPanel = new ResultPanel("result");
        resultPanel.setOutputMarkupPlaceholderTag(true).setVisible(false);
        add(resultPanel);
        final Form<Void> form = new Form<>("form");
        add(form);
        final TextField<String> input = new TextField<>("input", Model.of(""));
        input.setOutputMarkupId(true);
        form.add(input);
        final AjaxButton button = new AjaxButton("button")
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget _target,
                                    final Form<?> _form)
            {
                super.onSubmit(_target, _form);
                final String queryStr = (String) input.getDefaultModelObject();
                if (StringUtils.isNotEmpty(queryStr)) {
                    try {
                        final SearchResult result = Search.search(queryStr);
                        SearchPanel.this.visitChildren(ResultPanel.class, new IVisitor<Component, Void>()
                        {
                            @Override
                            public void component(final Component _component,
                                                  final IVisit<Void> _visit)
                            {
                               ((ResultPanel) _component).update(result);
                            }
                        });
                        resultPanel.setVisible(true);
                        _target.add(resultPanel);
                        final StringBuilder js = new StringBuilder();
                        js.append("require(['dijit/TooltipDialog', 'dijit/popup', 'dojo/dom', 'dijit/registry'], ")
                                .append("function(TooltipDialog, popup, dom, registry){\n")
                            .append("var rN = dom.byId('").append(resultPanel.getMarkupId()).append("');\n")
                            .append("var dialog = registry.byId(rN.id);")
                            .append("if (typeof(dialog) !== \"undefined\") {\n")
                            .append("registry.remove(dialog.id);")
                            .append("}\n")
                            .append("var params = {")
                            //.append("content:''")
                            .append("};\n")
                            .append("dialog = new TooltipDialog(params, rN);\n")
                                .append("")
                            .append("popup.open({\n")
                            .append("popup: dialog,")
                            .append("around: dom.byId('").append(input.getMarkupId()).append("')")
                            .append("});")
                            .append("});");

                        _target.appendJavaScript(js);
                    } catch (final EFapsException e) {
                        LOG.error("Catched EFapsException", e);
                    }
                }
            }
        };
        form.add(button);
        form.setDefaultButton(button);
    }
}
