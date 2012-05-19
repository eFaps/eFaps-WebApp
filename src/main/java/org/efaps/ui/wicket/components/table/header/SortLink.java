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

package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * This class renders the SortLink for the Header.
 *
 * @author The eFaps Team
 * @version $Id:SortLinkContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class SortLink
    extends Link<UITableHeader>
{

    /**
     * Needed foer serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId       wicket id
     * @param _model    model
     */
    public SortLink(final String _wicketId,
                    final IModel<UITableHeader> _model)
    {
        super(_wicketId, _model);
    }

    /**
     * @see org.apache.wicket.markup.html.link.Link#onClick()
     */
    @Override
    public void onClick()
    {
        final UITable uiTable = (UITable) (this.findParent(HeaderPanel.class)).getDefaultModelObject();
        final UITableHeader uiTableHeader = super.getModelObject();
        uiTable.setSortKey(uiTableHeader.getFieldName());

        for (final UITableHeader headermodel : uiTable.getHeaders()) {
            if (!headermodel.equals(uiTableHeader)) {
                headermodel.setSortDirection(SortDirection.NONE);
            }
        }

        if (uiTableHeader.getSortDirection() == SortDirection.NONE
                        || uiTableHeader.getSortDirection() == SortDirection.DESCENDING) {
            uiTableHeader.setSortDirection(SortDirection.ASCENDING);
        } else {
            uiTableHeader.setSortDirection(SortDirection.DESCENDING);
        }

        final String menuTreeKey = ((AbstractContentPage) getPage()).getMenuTreeKey();
        try {
            uiTable.setSortDirection(uiTableHeader.getSortDirection());
            uiTable.sort();

            AbstractContentPage page;
            if (getPage() instanceof TablePage) {
                page = new TablePage(new TableModel(uiTable), true);
            } else {
                page = new FormPage(new FormModel((UIForm) getPage().getDefaultModelObject()), true);
            }
            page.setMenuTreeKey(menuTreeKey);
            getRequestCycle().setResponsePage(page);
        } catch (final EFapsException e) {
            getRequestCycle().setResponsePage(new ErrorPage(e));
        }
    }
}
