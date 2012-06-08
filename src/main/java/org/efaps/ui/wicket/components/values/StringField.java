/*
 * Copyright 2003 - 2011 The eFaps Team
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


package org.efaps.ui.wicket.components.values;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StringField
    extends TextField<String>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final FieldConfiguration config;

    /**
     * @param _id
     * @param _model
     */
    public StringField(final String _wicketId,
                       final IModel<String> _model,
                       final FieldConfiguration _config)
    {
        super(_wicketId, _model);
        this.config = _config;
    }


    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("input");
        _tag.append("style", "text-align:" + this.config.getAlign(), ";");
        _tag.put("size", this.config.getSize());
        super.onComponentTag(_tag);
    }

    @Override
    public String getInputName()
    {
        return  this.config.getName();
    }
}

