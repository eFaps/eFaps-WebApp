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

package org.efaps.ui.wicket.components.footer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.models.objects.IPageObject;
import org.efaps.ui.wicket.models.objects.IWizardElement;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UIWizardObject;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;

/**
 * Link used to submit a Search.
 *
 * @author The eFaps Team
 */
public class AjaxSearchSubmitButton
    extends AbstractFooterButton<ICmdUIObject>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     * @param _eFapsContentReference the e faps content reference
     * @param _label the label
     */
    public AjaxSearchSubmitButton(final String _wicketId,
                                  final IModel<ICmdUIObject> _model,
                                  final EFapsContentReference _eFapsContentReference,
                                  final String _label)
    {
        super(_wicketId, _model, _eFapsContentReference, _label);
    }

    @Override
    public void onRequest(final AjaxRequestTarget _target)
    {
        final AbstractUIPageObject uiObject = (AbstractUIPageObject) getDefaultModelObject();
        try {
            if ("GridX".equals(Configuration.getAttribute(ConfigAttribute.TABLEDEFAULTTYPESEARCH))) {
                final UIGrid uiGrid = UIGrid.get(uiObject.getCommandUUID(), ((IPageObject) uiObject).getPagePosition());
                uiGrid.setCallInstance(uiObject.getInstance());
                final UIWizardObject wizard = new UIWizardObject(uiGrid);
                wizard.addParameters((IWizardElement) uiObject, Context.getThreadContext().getParameters());
                wizard.insertBefore((IWizardElement) uiObject);
                final FooterPanel footer = findParent(FooterPanel.class);
                final ModalWindowContainer modal = footer.getModalWindow();
                final GridPage page = new GridPage(Model.of(uiGrid), modal);
                getRequestCycle().setResponsePage(page);
            } else {
                final UITable newTable = new UITable(uiObject.getCommandUUID(), uiObject.getInstanceKey(), uiObject
                                .getOpenerId());
                final UIWizardObject wizard = new UIWizardObject(newTable);
                uiObject.setWizard(wizard);
                wizard.addParameters((IWizardElement) uiObject, Context.getThreadContext().getParameters());
                wizard.insertBefore((IWizardElement) uiObject);
                newTable.setWizard(wizard);
                newTable.setPartOfWizardCall(true);
                newTable.setRenderRevise(uiObject.isTargetCmdRevise());
                if (uiObject.isSubmit()) {
                    newTable.setSubmit(true);
                    newTable.setCallingCommandUUID(uiObject.getCallingCommandUUID());
                }
                final FooterPanel footer = findParent(FooterPanel.class);
                final ModalWindowContainer modal = footer.getModalWindow();
                final TablePage page = new TablePage(Model.of(newTable), modal);
                getRequestCycle().setResponsePage(page);
            }
        } catch (final EFapsException e) {
            getRequestCycle().setResponsePage(new ErrorPage(e));
        }
    }
}
