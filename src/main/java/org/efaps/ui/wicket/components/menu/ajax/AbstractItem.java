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

package org.efaps.ui.wicket.components.menu.ajax;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractItem
    extends WebMarkupContainer
{

    /**
     * @param _id
     * @param _model
     */
    public AbstractItem(final String _id,
                        final IModel<UIMenuItem> _model)
    {
        super(_id, _model);
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Gets model object
     *
     * @return model object
     */
    public final UIMenuItem getModelObject()
    {
        return (UIMenuItem) getDefaultModelObject();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.wicket.markup.html.link.AbstractLink#onComponentTagBody(org
     * .apache.wicket.markup.MarkupStream,
     * org.apache.wicket.markup.ComponentTag)
     */
    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        super.onComponentTagBody(_markupStream, _openTag);
        final StringBuilder html = new StringBuilder();
        if (getModelObject().getImage() == null) {
            html.append("<div class=\"eFapsMenuImagePlaceHolder\">").append("&nbsp;</div>");
        } else {
            html.append("<img src=\"/..").append(getModelObject().getImage()).append("\" class=\"eFapsMenuImage\"/>");
        }
        html.append("<span class=\"eFapsMenuLabel\">").append(getModelObject().getLabel()).append("</span>");
        replaceComponentTagBody(_markupStream, _openTag, html);
    }
}
