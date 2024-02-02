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
package org.efaps.ui.wicket.components.form.command;

import java.util.List;
import java.util.Map;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.models.field.UICmdField;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class AjaxExecuteLink
    extends AjaxButton<UICmdField>
{
    /**
    * Needed for serialization.
    */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new ajax execute link.
     *
     * @param _wicketId wicket id for this component
     * @param _model the model
     * @param _reference the reference
     * @param _label the label
     */
    public AjaxExecuteLink(final String _wicketId,
                           final IModel<UICmdField> _model,
                           final EFapsContentReference _reference,
                           final String _label)
    {
        super(_wicketId, _model, _reference, _label);
    }

    @Override
    protected boolean getDefaultProcessing()
    {
        return false;
    }

    /**
     * On submit.
     *
     * @param _target   AjaxRequestTarget
     */
    @Override
    public void onRequest(final AjaxRequestTarget _target)
    {
        final UICmdField uiObject = (UICmdField) getDefaultModelObject();
        final StringBuilder snip = new StringBuilder();
        try {
            final AbstractUIPageObject pageObject = (AbstractUIPageObject) getPage().getDefaultModelObject();
            final Map<String, String> uiID2Oid = pageObject == null ? null : pageObject.getUiID2Oid();
            final List<Return> returns = uiObject.executeEvents(null, uiID2Oid);
            for (final Return oneReturn : returns) {
                if (oneReturn.contains(ReturnValues.SNIPLETT)) {
                    snip.append(oneReturn.get(ReturnValues.SNIPLETT));
                }
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        _target.appendJavaScript(snip.toString());
    }
}
