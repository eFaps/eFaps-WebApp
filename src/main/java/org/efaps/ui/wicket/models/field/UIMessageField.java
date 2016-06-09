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


package org.efaps.ui.wicket.models.field;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.values.SnippletField;
import org.efaps.ui.wicket.models.objects.AbstractUIModeObject;
import org.efaps.util.EFapsException;


/**
 * Field used to send messages from the application.
 *
 * @author The eFaps Team
 */
public class UIMessageField
    extends UISnippletField
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _instanceKey  instanckey
     * @param _parent       parent object
     * @throws EFapsException on error
     */
    public UIMessageField(final AbstractUIModeObject _parent,
                          final String _instanceKey)
        throws EFapsException
    {
        super(_parent, _instanceKey, (FieldConfiguration) null);
    }

    @Override
    public Component getComponent(final String _wicketId)
    {
        return new SnippletField(_wicketId, Model.of(getHtml()), null, null);
    }

    @Override
    public String toString()
    {
        return getHtml();
    }
}
