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

package org.efaps.ui.wicket.components.values;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.efaps.admin.datamodel.ui.RateUI;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class HiddenRateField
    extends RateField
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId
     * @param _model
     * @param _config
     * @throws EFapsException
     */
    public HiddenRateField(final String _wicketId,
                           final IModel<RateUI.Value> _model,
                           final FieldConfiguration _config)
        throws EFapsException
    {
        super(_wicketId, _model, _config);
    }

    @Override
    protected String[] getInputTypes4Rate()
    {
        return new String[] { "hidden" };
    }

    @Override
    protected void onComponentTag4Rate(final ComponentTag _tag)
    {
        super.onComponentTag4Rate(_tag);
        _tag.put("type", "hidden");
    }
}
