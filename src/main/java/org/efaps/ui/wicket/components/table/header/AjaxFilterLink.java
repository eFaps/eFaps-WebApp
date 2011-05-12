/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.pages.content.table.filter.FilterPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * Class renders a link that opens the form to filter the tables.
 *
 * @author The eFasp Team
 * @version $Id$
 */
public class AjaxFilterLink
    extends AjaxLink<UITableHeader>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     */
    public AjaxFilterLink(final String _wicketId,
                          final IModel<UITableHeader> _model)
    {
        super(_wicketId, _model);
    }

    /**
     * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
     * @param _target AjaxRequestTarget
     */
    @Override
    public void onClick(final AjaxRequestTarget _target)
    {
        final HeaderPanel tableheaderpanel = this.findParent(HeaderPanel.class);

        final UITable tablemodel = (UITable) tableheaderpanel.getDefaultModelObject();

        final FilterPageCreator pagecreator = new FilterPageCreator(tablemodel, tableheaderpanel.getModal(),
                                                                    (UITableHeader) getDefaultModelObject());
        tableheaderpanel.getModal().setPageCreator(pagecreator);
        tableheaderpanel.getModal().setInitialWidth(300);
        tableheaderpanel.getModal().show(_target);
    }

    /**
     * Class is used to create the filter page lazily.
     */
    private class FilterPageCreator
        implements ModalWindow.PageCreator
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * UITabel for the page.
         */
        private final UITable uitable;

        /**
         * Modal window the page will be opened in.
         */
        private final ModalWindowContainer modalwindow;

        /**
         * uiTableheader belonging to this pagecreator.
         */
        private final UITableHeader uitableHeader;

        /**
         * @param _model model for the page
         * @param _modalwindow modal window the page will be opened in
         * @param _uiTableHeader uiTableheader belonging to this pagecreator
         */
        public FilterPageCreator(final UITable _model,
                                 final ModalWindowContainer _modalwindow,
                                 final UITableHeader _uiTableHeader)
        {
            this.uitable = _model;
            this.modalwindow = _modalwindow;
            this.uitableHeader = _uiTableHeader;
        }

        /**
         * @see org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.PageCreator#createPage()
         * @return Page
         */
        public Page createPage()
        {
            Page ret;
            try {
                ret = new FilterPage(new TableModel(this.uitable), this.modalwindow, this.uitableHeader);
            } catch (final EFapsException e) {
                ret = new ErrorPage(e);
            }
            return ret;
        }
    }
}
