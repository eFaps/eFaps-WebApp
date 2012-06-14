/*
 * Copyright 2003 - 2012 The eFaps Team
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

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * This Class renders a Link which removes a Child from a MenuTree.
 *
 * @author The eFaps Team
 * @version $Id:AjaxRemoveLink.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxRemoveLink
    extends AjaxLink<UIMenuItem>
    implements ILinkListener
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

    /*
     * (non-Javadoc)
     * @see
     * org.apache.wicket.ajax.AjaxEventBehavior#updateAjaxAttributes(org
     * .apache.wicket.ajax.attributes.AjaxRequestAttributes)
     */
    @Override
    protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
    {
        super.updateAjaxAttributes(_attributes);
        final AjaxCallListener listener = new AjaxCallListener();
        final StringBuilder js = new StringBuilder();
        js.append("dijit.byId(\"").append(((ContentContainerPage) getPage()).getCenterPanelId())
            .append("\").set(\"content\", dojo.create(\"iframe\", {")
            .append("\"src\": \"")
            .append(AjaxRemoveLink.this.urlFor(ILinkListener.INTERFACE, new PageParameters()))
            .append("\",\"style\": \"border: 0; width: 100%; height: 99%\"")
            .append("})); ");
        listener.onBefore(js);
        _attributes.getAjaxCallListeners().add(listener);
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.markup.html.link.ILinkListener#onLinkClicked()
     */
    @Override
    public void onLinkClicked()
    {
        final MenuTree menutree = findParent(MenuTree.class);
        final UIMenuItem currentItem = getModelObject();

        UIMenuItem menuItem = (UIMenuItem) menutree.getSelected().getDefaultModelObject();

        if (menuItem.isChild(currentItem) || menuItem.equals(currentItem)) {
            menuItem =  currentItem.getAncestor();
        }

        Page page;
        try {
            if (menuItem.getCommand().getTargetTable() != null) {
                if (menuItem.getCommand().getTargetStructurBrowserField() != null) {
                    page = new StructurBrowserPage(menuItem.getCommandUUID(),
                                    menuItem.getInstanceKey(), getPage().getPageReference());
                } else {
                    page = new TablePage(menuItem.getCommandUUID(), menuItem.getInstanceKey(), getPage()
                                    .getPageReference());
                }
            } else {
                page = new FormPage(menuItem.getCommandUUID(), menuItem.getInstanceKey(), getPage()
                                .getPageReference());
            }
        } catch (final EFapsException e) {
            page = new ErrorPage(e);
        }
        setResponsePage(page);
    }
}
