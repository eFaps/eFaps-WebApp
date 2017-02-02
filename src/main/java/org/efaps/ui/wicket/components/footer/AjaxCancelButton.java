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
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * Link using Ajax to close the ModalWindow the FooterPanel was opened in.
 *
 * @author The eFaps Team
 */
public class AjaxCancelButton
    extends AbstractFooterButton<ICmdUIObject>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new ajax cancel button.
     *
     * @param _wicketid ic of the component
     * @param _model the model
     * @param _eFapsContentReference the e faps content reference
     * @param _label the label
     */
    public AjaxCancelButton(final String _wicketid,
                            final IModel<ICmdUIObject> _model,
                            final EFapsContentReference _eFapsContentReference,
                            final String _label)
    {
        super(_wicketid, _model, _eFapsContentReference, _label);
        setSubmit(false);
    }

    @Override
    public void onRequest(final AjaxRequestTarget _target)
    {
        final FooterPanel footer = this.findParent(FooterPanel.class);
        footer.getModalWindow().setReloadChild(false);
        footer.getModalWindow().close(_target);
    }
}
