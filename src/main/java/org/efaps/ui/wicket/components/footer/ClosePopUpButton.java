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
import org.apache.wicket.markup.html.link.PopupCloseLink.ClosePopupPage;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * Class extends the standard PopupCloseLink due to the reason that the opener
 * must be removed from the cache in the session.
 *
 * @author The eFaps Team
 */
public class ClosePopUpButton
    extends AbstractFooterButton<ICmdUIObject>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new close pop up button.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @param _eFapsContentReference the e faps content reference
     * @param _label the label
     */
    public ClosePopUpButton(final String _wicketId,
                            final IModel<ICmdUIObject> _model,
                            final EFapsContentReference _eFapsContentReference,
                            final String _label)
    {
        super(_wicketId, _model, _eFapsContentReference, _label);
    }

    @Override
    public void onRequest(final AjaxRequestTarget _target)
    {
        setResponsePage(ClosePopupPage.class);
    }
}
