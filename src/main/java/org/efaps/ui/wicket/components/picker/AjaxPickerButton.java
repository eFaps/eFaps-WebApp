/*
 * Copyright 2003 - 2014 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.picker;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.PageCreator;
import org.apache.wicket.model.IModel;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowAjaxPageCreator;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.cell.UIPicker;
import org.efaps.ui.wicket.models.field.IPickable;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxPickerButton
    extends AjaxButton<IPickable>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AjaxPickerButton.class);

    /**
     * @param _wicketId
     * @param _model
     */
    public AjaxPickerButton(final String _wicketId,
                            final IModel<IPickable> _model)
    {
        super(_wicketId, _model, AjaxPickerLink.ICON, _model.getObject().getPicker().getLabel());
    }

    @Override
    public void onSubmit(final AjaxRequestTarget _target)
    {
        ModalWindowContainer modal;
        if (getPage() instanceof MainPage) {
            modal = ((MainPage) getPage()).getModal();
        } else {
            modal = ((AbstractContentPage) getPage()).getModal();
        }
        modal.reset();
        try {
            final UIPicker picker = ((IPickable) getDefaultModelObject()).getPicker();
            picker.setParentParameters(Context.getThreadContext().getParameters());
            final PageCreator pageCreator = new ModalWindowAjaxPageCreator(picker, modal);
            modal.setPageCreator(pageCreator);
            modal.setInitialHeight(picker.getWindowHeight());
            modal.setInitialWidth(picker.getWindowWidth());
            modal.setWindowClosedCallback(new PickerCallBack(null, getPage().getPageReference()));
            modal.show(_target);
        } catch (final EFapsException e) {
            LOG.error("Error on submit", e);
        }

    }
}
