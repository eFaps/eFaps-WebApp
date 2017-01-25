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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.menutree;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;

/**
 * This Class renders a Link which removes a Child from a MenuTree.
 *
 * @author The eFaps Team
 * @version $Id:AjaxRemoveLink.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxRemoveLink
    extends AjaxLink<UIMenuItem>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor setting the ID and the Node of this Component.
     *
     * @param _wicketId wicketid for this component
     * @param _model    model for this component
     */
    public AjaxRemoveLink(final String _wicketId,
                          final IModel<UIMenuItem> _model)
    {
        super(_wicketId, _model);
    }

    @Override
    public void onClick(final AjaxRequestTarget _target)
    {
        final MenuTree menutree = findParent(MenuTree.class);
        menutree.removeChild(getModelObject(), _target);
    }

    @Override
    protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
    {
        super.updateAjaxAttributes(_attributes);
        final AjaxCallListener listener = new AjaxCallListener();
        final StringBuilder js = new StringBuilder();
        final MenuTree menuTree = findParent(MenuTree.class);
        final String key = RandomStringUtils.randomAlphabetic(6);
        menuTree.add(key, (UIMenuItem) getDefaultModelObject());
        js.append("registry.byId(\"").append(((ContentContainerPage) getPage()).getCenterPanelId())
            .append("\").set(\"content\", domConstruct.create(\"iframe\", {")
            .append("\"src\": \"")
            .append(menuTree.urlFor(ILinkListener.INTERFACE, new PageParameters()))
            .append("&D=").append(key)
            .append("\",\"style\": \"border: 0; width: 100%; height: 99%\"")
            .append("})); ");
        listener.onBefore(DojoWrapper.require(js, DojoClasses.registry, DojoClasses.domConstruct));
        _attributes.getAjaxCallListeners().add(listener);
    }
}
