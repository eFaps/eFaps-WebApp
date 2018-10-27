/*
 * Copyright 2003 - 2018 The eFaps Team
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
package org.efaps.ui.wicket.pages.pivot;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.ui.IOption;
import org.efaps.api.ui.IPivotProvider;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.modalwindow.AbstractModalWindow;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PivotPage
    extends WebPage
{

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(PivotPage.class);

    private final static JavaScriptResourceReference WDR_JS = new JavaScriptResourceReference(PivotPage.class,
                    "webdatarocks.js");
    private final static JavaScriptResourceReference WDRTB_JS = new JavaScriptResourceReference(PivotPage.class,
                    "webdatarocks.toolbar.js");
    private final static CssResourceReference WDR_CSS = new CssResourceReference(PivotPage.class, "webdatarocks.css");

    private final static PackageResourceReference LOG_ES = new PackageResourceReference(PivotPage.class, "loc/es.json");

    public PivotPage()
    {
        final String providerClass = Configuration.getAttribute(ConfigAttribute.PIVOT_PROVIDER);
        if (providerClass != null) {
            try {
                final Class<?> clazz = Class.forName(providerClass, false, EFapsClassLoader.getInstance());
                final IPivotProvider provider = (IPivotProvider) clazz.newInstance();
                final List<IOption> datasources = provider.getDataSources();
                final List<IOption> reports = provider.getReports();

                final DropDownChoice<IOption> dsDropDown = new DropDownChoice<>("dataSources");
                dsDropDown.setChoices(datasources);
                dsDropDown.setChoiceRenderer(new ChoiceRenderer<IOption>()
                {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Object getDisplayValue(final IOption _object)
                    {
                        return _object.getLabel();
                    }

                    @Override
                    public String getIdValue(final IOption _object, final int _index)
                    {
                        return String.valueOf(_object.getValue());
                    }
                });
                add(dsDropDown);

                dsDropDown.add(new AttributeAppender("onchange", new Model<>("loadDataSource(this.value)"), ";"));

                final DropDownChoice<IOption> repDropDown = new DropDownChoice<>("reports");
                repDropDown.setChoices(reports);
                repDropDown.setChoiceRenderer(new ChoiceRenderer<IOption>()
                {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Object getDisplayValue(final IOption _object)
                    {
                        return _object.getLabel();
                    }

                    @Override
                    public String getIdValue(final IOption _object, final int _index)
                    {
                        return String.valueOf(_object.getValue());
                    }
                });
                add(repDropDown);
                repDropDown.add(new AttributeAppender("onchange", new Model<>("loadReport(this.value)"), ";"));

                final ModalWindow modal = new AbstractModalWindow("modal")
                {
                    private static final long serialVersionUID = 1L;
                };
                add(modal);
                modal.setInitialWidth(500);
                modal.setInitialHeight(500);
                modal.setContent(new SaveReportPanel(modal.getContentId(), provider, reports));

                add(new AjaxButton<Void>("showModal")
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onRequest(final AjaxRequestTarget target)
                    {
                        modal.show(target);
                    }

                    @Override
                    protected boolean isSubmit()
                    {
                        return false;
                    }
                });
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                LOG.error("Could not find/instantiate Provider Class", e);
            }
        }

        add(new WebMarkupContainer("wdr") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTagBody(final MarkupStream _markupStream, final ComponentTag _openTag)
            {
                final IRequestHandler handler = new ResourceReferenceRequestHandler(LOG_ES, getPageParameters());
                final CharSequence locUrl = RequestCycle.get().urlFor(handler);
                final CharSequence url = RequestCycle.get().urlFor(JsonResponsePage.class, new PageParameters());
                final StringBuilder js = new StringBuilder()
                        .append("function loadDataSource(_datasource) {\n")
                        .append("var url = new URL('").append(url)
                            .append("?datasource=' + _datasource, window.location);\n")
                        .append("webdatarocks.updateData({\n")
                        .append("dataSourceType: 'json',\n")
                        .append("filename: url.href\n")
                        .append("});\n")
                    .append("document.getElementById('wdr-component').style.height=window.innerHeight - 50 + 'px';\n")
                        .append("}\n")
                        .append("var pivot = new WebDataRocks({\n")
                        .append("container : '#wdr-component',\n")
                        .append("beforetoolbarcreated: customizeToolbar,\n")
                        .append("toolbar : true,\n")
                        .append("report : {\n")
                        .append("dataSource : {\n")
                        .append("dataSourceType: 'json',\n")
                        .append("data : []\n")
                        .append("}\n")
                        .append("},\n")
                        .append("global: {\n")
                        .append("localization: '").append(locUrl).append("'\n")
                        .append("}\n")
                        .append("});\n")
                        .append("function customizeToolbar(_toolbar) {\n")
                        .append("var tabs = _toolbar.getTabs();\n")
                        .append("_toolbar.getTabs = function() {\n")
                        .append("return [tabs[3], tabs[4], tabs[5], tabs[6]];\n")
                        .append("}\n")
                        .append("}\n")
                        .append("function loadReport(_report) {\n")
                        .append("var url = new URL('").append(url)
                            .append("?report=' + _report, window.location);\n")
                        .append("webdatarocks.load(url.href);\n")
                        .append("}\n")
                        ;
//var pivotDataSource= new URL(report.dataSource.filename).searchParams.get('datasource');
                replaceComponentTagBody(_markupStream, _openTag, js);
            }
        });
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(JavaScriptHeaderItem.forReference(WDR_JS));
        _response.render(JavaScriptHeaderItem.forReference(WDRTB_JS));
        _response.render(CssHeaderItem.forReference(WDR_CSS));
    }
}
