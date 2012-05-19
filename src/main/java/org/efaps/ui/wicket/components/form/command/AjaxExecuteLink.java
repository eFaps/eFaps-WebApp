/*
 * Copyright 2003 - 2012 The eFaps Team
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

package org.efaps.ui.wicket.components.form.command;

import java.util.List;
import java.util.Map;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.models.cell.UIFormCellCmd;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxExecuteLink
    extends AjaxSubmitLink
{
    /**
    * Needed for serialization.
    */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id for this component
     * @param _form     form this button lies in
     * @param _uiObject uiobjetc for this component
     */
    public AjaxExecuteLink(final String _wicketId,
                           final FormContainer _form,
                           final UIFormCellCmd _uiObject)
    {
        super(_wicketId, _form);
        setDefaultModel(new Model<UIFormCellCmd>(_uiObject));
    }

    /**
     * @param _target   AjaxRequestTarget
     * @param _form     form
     */
    @Override
    protected void onSubmit(final AjaxRequestTarget _target,
                            final Form<?> _form)
    {
        final UIFormCellCmd uiObject = (UIFormCellCmd) getDefaultModelObject();
        final StringBuilder snip = new StringBuilder();
        try {
            final AbstractUIPageObject pageObject = (AbstractUIPageObject) (getPage().getDefaultModelObject());
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

    /* (non-Javadoc)
     * @see org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink#onError(org.apache.wicket.ajax.AjaxRequestTarget, org.apache.wicket.markup.html.form.Form)
     */
    @Override
    protected void onError(final AjaxRequestTarget _target,
                           final Form<?> _form)
    {
        // TODO Auto-generated method stub

    }
}
