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

package org.efaps.ui.wicket.components.form.command;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.models.objects.UIForm;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CommandCellPanel
    extends Panel
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId     wicket id for this component
     * @param _model        model for this component
     * @param _formmodel    model of the form containing this component
     * @param _form         form containing this component
     */
    public CommandCellPanel(final String _wicketId,
                            final IModel<?> _model,
                            final UIForm _formmodel,
                            final FormContainer _form)
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        getDefaultModelObject();
    }
}
