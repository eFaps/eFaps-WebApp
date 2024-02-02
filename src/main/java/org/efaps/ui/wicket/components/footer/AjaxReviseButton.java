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
package org.efaps.ui.wicket.components.footer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.models.objects.IWizardElement;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UIWizardObject;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * Class renders a Link used on a search result to return to the previous page
 * and revise the search.
 *
 * @author The eFaps Team
 */
public class AjaxReviseButton
    extends AbstractFooterButton<ICmdUIObject>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new ajax revise button.
     *
     * @param _wicketId wicket id of this component
     * @param _model the model
     * @param _eFapsContentReference the e faps content reference
     * @param _label the label
     */
    public AjaxReviseButton(final String _wicketId,
                            final IModel<ICmdUIObject> _model,
                            final EFapsContentReference _eFapsContentReference,
                            final String _label)
    {
        super(_wicketId, _model, _eFapsContentReference, _label);
    }

    @Override
    public void onRequest(final AjaxRequestTarget _target)
    {
        final IWizardElement element = (IWizardElement) getPage().getDefaultModelObject();
        final UIWizardObject wizard = element.getUIWizardObject();
        final IWizardElement prevObject = wizard.getPrevious();
        // prevObject.setPartOfWizardCall(true);
        final FooterPanel footer = findParent(FooterPanel.class);
        final ModalWindowContainer modal = footer.getModalWindow();
        final WebPage page;
        try {
            if (prevObject instanceof UITable) {
                ((UITable) prevObject).resetModel();
                page = new TablePage(Model.of((UITable) prevObject), modal);
            } else if (prevObject instanceof UIGrid) {
                page = new GridPage(Model.of((UIGrid) prevObject));
            } else {
                ((UIForm) prevObject).resetModel();
                page = new FormPage(Model.of((UIForm) prevObject), modal);
            }
            getRequestCycle().setResponsePage(page);
        } catch (final EFapsException e) {
            getRequestCycle().setResponsePage(new ErrorPage(e));
        }
    }
}
