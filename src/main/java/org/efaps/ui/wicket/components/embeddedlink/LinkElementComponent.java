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

package org.efaps.ui.wicket.components.embeddedlink;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.efaps.ui.wicket.models.EmbeddedLink;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LinkElementComponent
    extends AjaxLink<Void>
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId     wicket id of the component
     * @param _embededLink  link the component is representing
     */
    public LinkElementComponent(final String _wicketId,
                                final EmbeddedLink _embededLink)
    {
        super(_wicketId);
        setMarkupId(_elementId);
    }

    @Override
    public void onClick(final AjaxRequestTarget _target)
    {
        System.out.println("click");
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        //nothing to add, because only the javascript added is wanted
    }

}
