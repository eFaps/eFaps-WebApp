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

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.index.ISearch;
import org.efaps.admin.index.Index;
import org.efaps.admin.index.Searcher;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Command;
import org.efaps.json.index.SearchResult;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
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
    implements IAjaxIndicatorAware
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
        boolean access = false;
        try {
            final Command cmd = Command.get(UUID.fromString(
                            Configuration.getAttribute(ConfigAttribute.INDEXACCESSCMD)));
            access = cmd.hasAccess(TargetMode.VIEW, null);
        } catch (final EFapsException e) {
            LOG.error("Catched error during access control to index.", e);
        }
        this.setVisible(access);

        final ResultPanel resultPanel = new ResultPanel("result");
        resultPanel.setOutputMarkupPlaceholderTag(true).setVisible(false);
        add(resultPanel);
        final Form<Void> form = new Form<>("form");
        add(form);
        final TextField<String> input = new TextField<>("input", Model.of(""));
        input.setOutputMarkupId(true);
        input.add(new AttributeModifier("placeholder",
                        DBProperties.getProperty(SearchPanel.class.getName() + ".Placeholder")));
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
                        final ISearch search = Index.getSearch();
                        search.setQuery(queryStr);
                        final SearchResult result = Searcher.search(search);
                        SearchPanel.this.visitChildren(ResultPanel.class, new IVisitor<Component, Void>()
                        {

                            @Override
                            public void component(final Component _component,
                                                  final IVisit<Void> _visit)
                            {
                                ((ResultPanel) _component).update(search, result);
                            }
                        });
                        resultPanel.setVisible(true);
                        _target.add(resultPanel);
                        final StringBuilder js = new StringBuilder();
                        js.append("require(['dijit/TooltipDialog', 'dijit/popup', 'dojo/dom', 'dijit/registry',")
                                .append("'dojo/_base/window', 'dojo/window', 'dojo/query',")
                                .append("'dojo/dom-construct', 'dojo/NodeList-dom'],")
                                .append("function(TooltipDialog, popup, dom, registry, baseWindow, win, ")
                                .append(" query, domConstruct){\n")
                            .append("var rN = dom.byId('").append(resultPanel.getMarkupId()).append("');\n")
                            .append("var dialog = registry.byId(rN.id);")
                            .append("if (typeof(dialog) !== \"undefined\") {\n")
                            .append("popup.close(dialog);")
                            .append("registry.remove(dialog.id);")
                            .append("}\n")
                            .append("var vs = win.getBox();\n")
                            .append("var wi = (vs.w - 100) + 'px';\n")
                            .append("var wh = (vs.h - 150) + 'px';\n")
                            .append("query(\".searchOverlay\").forEach(domConstruct.destroy);\n")
                            .append("var ov = domConstruct.create(\"div\", {'class' : 'searchOverlay'}, ")
                                .append("baseWindow.body());\n")
                            .append("query('.searchOverlay').on('click', function (e) {\n")
                            .append("popup.close(registry.byId(rN.id));\n")
                            .append("});\n")
                            .append("query(\".resultPlaceholder\", rN).style(\"width\", wi);\n")
                            .append("query('.resultOverflow', rN).style('height', wh);")
                            .append("query('.resultClose', rN).on(\"click\", function(e){\n")
                            .append("popup.close(registry.byId(rN.id));\n")
                            .append("});\n")
                            .append("dialog = new TooltipDialog({}, rN);\n")
                            .append("popup.open({\n")
                            .append("popup: dialog,")
                            .append("orient: [ \"below-centered\", \"below-alt\", \"below\"],")
                            .append("onClose: function(){\n")
                            .append("query(\".searchOverlay\").forEach(domConstruct.destroy);\n")
                            .append("")
                            .append("},\n")
                            .append("around: dom.byId('").append(form.getMarkupId()).append("')")
                            .append("});")
                            .append("});");

                        _target.appendJavaScript(js);
                    } catch (final EFapsException e) {
                        LOG.error("Catched EFapsException", e);
                    }
                }
            }

            @Override
            public void onComponentTagBody(final MarkupStream _markupStream,
                                           final ComponentTag _openTag)
            {
                replaceComponentTagBody(_markupStream, _openTag,
                                DBProperties.getProperty(SearchPanel.class.getName() + ".Button"));
            }
        };
        form.add(button);
        form.setDefaultButton(button);
    }

    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return "searchIndicator";
    }
}
