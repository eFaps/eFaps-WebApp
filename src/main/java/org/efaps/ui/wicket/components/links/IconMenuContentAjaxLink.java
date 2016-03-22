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

package org.efaps.ui.wicket.components.links;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.values.LabelField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class IconMenuContentAjaxLink
    extends MenuContentAjaxLink
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new icon menu content ajax link.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @param _content the content
     * @param _icon the icon
     * @throws EFapsException on error
     */
    public IconMenuContentAjaxLink(final String _wicketId,
                                   final IModel<AbstractUIField> _model,
                                   final String _content,
                                   final String _icon)
        throws EFapsException
    {
        super(_wicketId, _model, _content);
        add(new StaticImageComponent("icon", _icon));
        add(new LabelField("labelField", Model.of(getContent()), _model.getObject()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onComponentTagInternal(final ComponentTag _tag)
    {
       // no adding necessary
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy()
    {
        return new PanelMarkupSourcingStrategy(false);
    }
}
