/*
 * Copyright 2003 - 2013 The eFaps Team
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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.model.IModel;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SnippletField
    extends Label
    implements ILabelProvider<String>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final IModel<String> label;

    /**
     * @param _id
     * @param _model
     */
    public SnippletField(final String _id,
                         final IModel<String> _model,
                         final IModel<String> _label)
    {
        super(_id, _model);
        setEscapeModelStrings(false);
        this.label = _label;
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.markup.html.form.ILabelProvider#getLabel()
     */
    @Override
    public IModel<String> getLabel()
    {
        return this.label;
    }
}
