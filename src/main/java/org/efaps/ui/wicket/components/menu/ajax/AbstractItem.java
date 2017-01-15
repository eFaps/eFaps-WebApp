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

package org.efaps.ui.wicket.components.menu.ajax;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class AbstractItem
    extends WebMarkupContainer
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new abstract item.
     *
     * @param _id the id
     * @param _model the model
     */
    public AbstractItem(final String _id,
                        final IModel<UIMenuItem> _model)
    {
        super(_id, _model);
    }

    /**
     * Gets model object.
     *
     * @return model object
     */
    public final UIMenuItem getModelObject()
    {
        return (UIMenuItem) getDefaultModelObject();
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        super.onComponentTagBody(_markupStream, _openTag);
        final StringBuilder html = new StringBuilder();
        if (getModelObject().getImage() == null) {
            html.append("<div class=\"eFapsMenuImagePlaceHolder\">").append("&nbsp;</div>");
        } else {
            html.append("<img src=\"").append(EFapsContentReference.getImageURL(getModelObject().getImage()))
                .append("\" class=\"eFapsMenuImage\"/>");
        }
        html.append("<span class=\"eFapsMenuLabel\">").append(getModelObject().getLabel()).append("</span>");
        replaceComponentTagBody(_markupStream, _openTag, html);
    }
}
