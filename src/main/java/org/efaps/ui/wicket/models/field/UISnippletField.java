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

package org.efaps.ui.wicket.models.field;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.values.SnippletField;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.objects.AbstractUIModeObject;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UISnippletField
    extends AbstractUIField
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The snipplet of this field.
     */
    private String html;

    /**
     * @param _instanceKey  key to the instance
     * @param _parent       parent object
     * @param _config the FieldConfiguration for this Field
     * @throws EFapsException on error
     */
    public UISnippletField(final String _instanceKey,
                           final AbstractUIModeObject _parent,
                           final FieldConfiguration _config)
        throws EFapsException
    {
        super(_instanceKey, _parent, null);
        setFieldConfiguration(_config);
    }

    /**
     * @param _html the html that will be presented
     */
    public void setHtml(final String _html)
    {
        this.html = _html;
    }

    /**
     * Getter method for the instance variable {@link #html}.
     *
     * @return value of instance variable {@link #html}
     */
    public String getHtml()
    {
        return this.html;
    }

    @Override
    protected FieldConfiguration getNewFieldConfiguration()
        throws EFapsException
    {
        return null;
    }


    @Override
    public Component getComponent(final String _wicketId)
    {
        Model<String> label = null;
        if (!getFieldConfiguration().isHideLabel()) {
            label = Model.of(getFieldConfiguration().getLabel());
        }
        return new SnippletField(_wicketId, Model.of(this.html), label);
    }
}
