/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.component.IRequestablePage;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.ui.wicket.models.objects.AbstractUIHeaderObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.grid.filter.FormFilterPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.content.table.filter.FilterPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class renders the SortLink for the Header.
 *
 * @author The eFaps Team
 */
public class SortLink
    extends Link<UITableHeader>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SortLink.class);

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
    @SuppressWarnings("unchecked")
    @Override
    public void onClick()
    {
        final AbstractUIHeaderObject uiHeaderObject = (AbstractUIHeaderObject) this.findParent(HeaderPanel.class)
                        .getDefaultModelObject();
        final UITableHeader uiTableHeader = super.getModelObject();
        uiHeaderObject.setSortKey(uiTableHeader.getFieldName());

        for (final UITableHeader headermodel : uiHeaderObject.getHeaders()) {
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

        try {
            uiHeaderObject.setSortDirection(uiTableHeader.getSortDirection());
            uiHeaderObject.sort();

            IRequestablePage page;
            if (getPage() instanceof TablePage) {
                page = new TablePage(Model.of(uiHeaderObject),
                                ((AbstractContentPage) getPage()).getModalWindow(),
                                ((AbstractContentPage) getPage()).getCalledByPageReference());
            } else if (getPage() instanceof StructurBrowserPage) {
                page = new StructurBrowserPage(Model.of(uiHeaderObject),
                                ((AbstractContentPage) getPage()).getModalWindow(),
                                ((AbstractContentPage) getPage()).getCalledByPageReference());
            } else if (getPage() instanceof FilterPage) {
                page = getPage();
                getPage().visitChildren(HeaderPanel.class, (_child, _visit) -> {
                    try {
                        final HeaderPanel replacement = new HeaderPanel(_child.getId(),
                                        ((HeaderPanel) _child).getTablePanel(), _child.getDefaultModel());
                        _child.replaceWith(replacement);
                    } catch (final CacheReloadException e) {
                        LOG.error("Catched error: ", e);
                    }
                });
            } else if (getPage() instanceof FormFilterPage) {
                page = new GridPage((IModel<UIGrid>) ((FormFilterPage) getPage()).getDefaultModel());
            } else {
                page = new FormPage(Model.of((UIForm) getPage().getDefaultModelObject()),
                                ((AbstractContentPage) getPage()).getModalWindow(),
                                ((AbstractContentPage) getPage()).getCalledByPageReference());
            }

            getRequestCycle().setResponsePage(page);
        } catch (final EFapsException e) {
            LOG.error("Catched error: ", e);
            getRequestCycle().setResponsePage(new ErrorPage(e));
        }
    }
}
