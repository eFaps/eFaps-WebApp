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

package org.efaps.ui.wicket.pages.content.grid.filter;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.field.Field;
import org.efaps.api.ci.UIFormFieldProperty;
import org.efaps.api.ui.IMapFilter;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.gridx.GridXComponent;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.util.ParameterUtil;
import org.efaps.util.EFapsException;

/**
 * The Class FormFilterPage.
 *
 * @author The eFaps Team
 */
public class FormFilterPage
    extends AbstractMergePage
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new form filter page.
     *
     * @param _model the model
     * @param _uiGrid the ui grid
     * @throws EFapsException on error
     */
    public FormFilterPage(final IModel<IMapFilter> _model,
                          final UIGrid _uiGrid)
        throws EFapsException
    {
        final String cmdName = Field.get(_model.getObject().getFieldId()).getProperty(UIFormFieldProperty.FILTER_CMD);
        final Command cmd = Command.get(cmdName);
        final UIForm uiform = new UIForm(cmd.getUUID(), null);
        uiform.setCallingCommandUUID(_uiGrid.getCmdUUID());
        final FormPage formPage = new FormPage(Model.of(uiform));
        final FormContainer formContainer = new FormContainer("form");
        add(formContainer);
        FormPage.updateFormContainer(formPage, formContainer, uiform);

        formContainer.add(new AjaxButton<IMapFilter>("btn", _model, Button.ICON.ACCEPT
                        .getReference())
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(final AjaxRequestTarget _target)
            {
                try {
                    _model.getObject().putAll(ParameterUtil.parameter2Map(getRequest().getRequestParameters()));
                    _uiGrid.reload();

                    final StringBuilder js = new StringBuilder()
                            .append("var grid =  window.parent.dijit.registry.byId('grid');\n")
                            .append("var items= ").append(GridXComponent.getDataJS(_uiGrid))
                            .append("grid.model.clearCache();\n")
                            .append("grid.model.store.setData(items);\n")
                            .append("grid.body.refresh();\n");

                    _target.appendJavaScript(js);
                } catch (final EFapsException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
}
