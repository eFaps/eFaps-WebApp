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

package org.efaps.ui.wicket.pages.content.grid.filter;



import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.field.Field;
import org.efaps.api.ci.UIFormFieldProperty;
import org.efaps.api.ui.IFilter;
import org.efaps.api.ui.IMapFilter;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.gridx.GridXComponent;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.ui.wicket.util.ParameterUtil;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FormFilterPage.class);

    /**
     * Instantiates a new form filter page.
     *
     * @param _model the model
     * @param _uiGrid the ui grid
     * @param _pageReference the page ref
     * @throws EFapsException on error
     */
    public FormFilterPage(final IModel<IMapFilter> _model,
                          final UIGrid _uiGrid,
                          final PageReference _pageReference)
        throws EFapsException
    {
        setDefaultModel(Model.of(_uiGrid));
        final String cmdName = Field.get(_model.getObject().getFieldId()).getProperty(UIFormFieldProperty.FILTER_CMD);
        final Command cmd = Command.get(cmdName);
        final UIForm uiform = new UIForm(cmd.getUUID(), null);
        uiform.setCallingCommandUUID(_uiGrid.getCmdUUID());
        final FormPage formPage = new FormPage(Model.of(uiform));
        final FormContainer formContainer = new FormContainer("form");
        add(formContainer);
        FormPage.updateFormContainer(formPage, formContainer, uiform);

        add(new AjaxButton<Long>("btn", Model.of(_model.getObject().getFieldId()),
                        AjaxButton.ICON.ACCEPT.getReference())
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void onRequest(final AjaxRequestTarget _target)
            {
                try {
                    final long fieldid = (long) getDefaultModelObject();
                    final UIGrid uiGrid = (UIGrid) _pageReference.getPage().getDefaultModelObject();
                    for (final IFilter filter : uiGrid.getFilterList()) {
                        if (filter.getFieldId() == fieldid) {
                            ((IMapFilter) filter).clear();
                            ((IMapFilter) filter).putAll(
                                            ParameterUtil.parameter2Map(getRequest().getRequestParameters()));
                        }
                    }
                    uiGrid.reload();

                    final StringBuilder js = new StringBuilder()
                            .append("var grid =  registry.byId('grid');\n")
                            .append("var items= ").append(GridXComponent.getDataJS(uiGrid))
                            .append("grid.model.clearCache();\n")
                            .append("grid.model.store.setData(items);\n")
                            .append("grid.body.refresh();\n");

                    _target.appendJavaScript(DojoWrapper.require(js, DojoClasses.registry));
                } catch (final EFapsException e) {
                    FormFilterPage.LOG.error("Catched error: ", e);
                }
            }

            @Override
            public Form<?> getForm()
            {
                return formContainer;
            }
        });
    }
}
