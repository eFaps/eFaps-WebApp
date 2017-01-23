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

package org.efaps.ui.wicket.components.footer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.models.objects.IWizardElement;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UIWizardObject;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * Class renders a Link used on a search result to return to the previous page
 * and revise the search.
 *
 * @author The eFaps Team
 */
public class AjaxReviseLink
    extends AjaxLink<AbstractUIObject>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id of this component
     * @param _uiObject uiobject for this component
     */
    public AjaxReviseLink(final String _wicketId,
                          final AbstractUIObject _uiObject)
    {
        super(_wicketId, new Model<>(_uiObject));
    }

    /**
     * On click the previous page will be restored using wizard from the
     * uiobject.
     *
     * @param _target target for this request
     */
    @Override
    public void onClick(final AjaxRequestTarget _target)
    {
        final AbstractUIPageObject uiobject = (AbstractUIPageObject) getDefaultModelObject();
        final UIWizardObject wizard = uiobject.getWizard();
        final IWizardElement prevObject = wizard.getPrevious();
        //prevObject.setPartOfWizardCall(true);
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
