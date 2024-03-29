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
package org.efaps.ui.wicket.pages.pivot;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.efaps.api.ui.IOption;
import org.efaps.api.ui.IPivotProvider;
import org.efaps.ui.wicket.components.modalwindow.LegacyModalWindow;

public class SaveReportPanel
    extends Panel
{

    private static final long serialVersionUID = 1L;

    public SaveReportPanel(final String _wicketId,  final IPivotProvider _provider, final List<IOption> _reports)
    {
        super(_wicketId);

        final Form<Void> form = new Form<>("saveform");
        add(form);

        form.add(new TextField<>("reportName", Model.of("")));
        final DropDownChoice<IOption> repDropDown = new DropDownChoice<>("reports4save");
        repDropDown.setDefaultModel(Model.of());
        repDropDown.setChoices(_reports);
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
        form.add(repDropDown);
        repDropDown.add(new AttributeAppender("onchange",
                        new Model<>("""
                            this.value == '' ? $('#reportName4save').val('').prop('disabled', false)\s\
                            : $('#reportName4save').val(this.options[this.selectedIndex].innerHTML)\
                            .prop('disabled', true)"""), ";"));

        form.add(new AjaxButton("save")
        {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            protected void onSubmit(final AjaxRequestTarget _target)
            {
                LegacyModalWindow.closeCurrent(_target);
                final IRequestParameters parameters = getRequest().getPostParameters();
                final String pivotReport = parameters.getParameterValue("pivotReport").toString();
                final String reportName = parameters.getParameterValue("reportName").toString();
                final String reports = parameters.getParameterValue("reports4save").toString();
                final String key = StringUtils.isNotEmpty(reportName) ? reportName : reports;
                if (StringUtils.isNotEmpty(key)) {
                    _provider.save(key, pivotReport);
                    findPage().visitChildren(DropDownChoice.class, (_dropDown, _visit) -> {
                        if ("reports".equals(_dropDown.getId())) {
                            ((DropDownChoice<IOption>) _dropDown).setChoices(_provider.getReports());
                            _target.add(_dropDown);
                        }
                    });
                }
            }

            @Override
            protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
            {
                super.updateAjaxAttributes(_attributes);
                final StringBuilder js = new StringBuilder()
                                .append("return {\n")
                                .append("'pivotReport': JSON.stringify(webdatarocks.getReport())\n")
                                .append("}\n");
                _attributes.getDynamicExtraParameters().add(js);
            }
        });

        final Form<Void> delForm = new Form<>("deleteform");
        add(delForm);

        final DropDownChoice<IOption> delRepDropDown = new DropDownChoice<>("reports4delete");
        delRepDropDown.setDefaultModel(Model.of());
        delRepDropDown.setChoices(_reports);
        delRepDropDown.setChoiceRenderer(new ChoiceRenderer<IOption>()
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
        delForm.add(delRepDropDown);

        delForm.add(new AjaxButton("delete")
        {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            protected void onSubmit(final AjaxRequestTarget _target)
            {
                LegacyModalWindow.closeCurrent(_target);
                final IRequestParameters parameters = getRequest().getPostParameters();
                final String key = parameters.getParameterValue("reports4delete").toString();
                if (StringUtils.isNotEmpty(key)) {
                    _provider.delete(key);
                    findPage().visitChildren(DropDownChoice.class, (_dropDown, _visit) -> {
                        if ("reports".equals(_dropDown.getId())) {
                            ((DropDownChoice<IOption>) _dropDown).setChoices(_provider.getReports());
                            _target.add(_dropDown);
                        }
                    });
                }
            }
        });
    }
}
