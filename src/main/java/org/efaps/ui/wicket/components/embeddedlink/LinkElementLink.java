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

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.Model;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.EmbeddedLink;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LinkElementLink
    extends AjaxLink<EmbeddedLink>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _id
     * @param _model
     */
    public LinkElementLink(final String _wicketId,
                           final EmbeddedLink _embededLink)
    {
        super(_wicketId, Model.of(_embededLink));
        setMarkupId(_embededLink.getId());
    }

    @Override
    public void onClick(final AjaxRequestTarget _target)
    {
        final EmbeddedLink link = (EmbeddedLink) getDefaultModelObject();
        Instance instance = null;
        if (link.getInstanceKey() != null) {
            Menu menu = null;
            try {
                instance = Instance.get(link.getInstanceKey());
                menu = Menu.getTypeTreeMenu(instance.getType());
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
            if (menu == null) {
                final Exception ex = new Exception("no tree menu defined for type " + instance.getType().getName());
                throw new RestartResponseException(new ErrorPage(ex));
            }

            Page page;
            try {
                page = new ContentContainerPage(menu.getUUID(), link.getInstanceKey(),
                                getPage() instanceof StructurBrowserPage);

            } catch (final EFapsException e) {
                page = new ErrorPage(e);
            }
            this.setResponsePage(page);
        }
    }
}
