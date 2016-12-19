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

package org.efaps.ui.wicket.components.table.row;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class RowId
    extends WebComponent
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicketid for this component
     * @param _model model for this component
     */
    public RowId(final String _wicketId,
                 final IModel<AbstractInstanceObject> _model)
    {
        super(_wicketId, _model);
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        final Object object = getPage().getDefaultModelObject();
        if (object instanceof AbstractUIPageObject) {
            final AbstractUIPageObject uiObject = (AbstractUIPageObject) getPage().getDefaultModelObject();
            final AbstractInstanceObject instObj = (AbstractInstanceObject) getDefaultModelObject();
            instObj.setUserinterfaceId(uiObject.getNewRandom());
            try {
                uiObject.getUiID2Oid().put(instObj.getUserinterfaceId(), instObj.getInstance() == null ? null
                                : instObj.getInstance().getOid());
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
            _tag.put("name", EFapsKey.TABLEROW_NAME.getKey());
            _tag.put("value", instObj.getUserinterfaceId());
        }
        _tag.put("type", "hidden");

    }
}
