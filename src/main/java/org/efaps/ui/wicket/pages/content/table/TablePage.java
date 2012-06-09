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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content.table;

import java.util.UUID;

import org.apache.wicket.PageReference;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.header.HeaderPanel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.util.EFapsException;

/**
 * CLass renders a page containing a table.
 *
 * @author The eFaps Team
 * @version $Id:TablePage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class TablePage
    extends AbstractContentPage
{
    /**
     * Needed foer serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the style sheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(TablePage.class, "TablePage.css");

    /**
     * @param _model modle for the table
     * @throws EFapsException on error
     */
    public TablePage(final IModel<UITable> _model,
                     final boolean _updateMenu)
        throws EFapsException
    {
        this(_model, null, _updateMenu);
    }

    /**
     * @param _model model for the page
     * @param _modalWindow modal window
     * @throws EFapsException on error
     */
    public TablePage(final IModel<?> _model,
                     final ModalWindowContainer _modalWindow,
                     final boolean _updateMenu)
        throws EFapsException
    {
        super(_model, _modalWindow, _updateMenu);
        this.addComponents();
    }

    /**
     * @param _uuid uuid of a command
     * @param _instanceKey key to an instance
     * @param _openerId id of an opener
     * @throws EFapsException on error
     */
    public TablePage(final UUID _uuid,
                     final String _instanceKey,
                     final String _openerId)
        throws EFapsException
    {
        this(new TableModel(new UITable(_uuid, _instanceKey, _openerId)), false);
    }

    /**
     * @param _model modle for the table
     * @throws EFapsException on error
     */
    public TablePage(final TableModel _model,
                     final boolean _updateMenu)
        throws EFapsException
    {
        super(_model, null, _updateMenu);
        this.addComponents();
    }

    /**
     * @param _uuid uuid of a commmand
     * @param _instanceKey key to an instance
     * @throws EFapsException on error
     */
    public TablePage(final UUID _uuid,
                     final String _instanceKey,
                     final boolean _updateMenu)
        throws EFapsException
    {
        this(new TableModel(new UITable(_uuid, _instanceKey)), _updateMenu);
    }

    /**
     * @param _model modle for the table
     * @throws EFapsException on error
     */
    public TablePage(final IModel<UITable> _model,
                     final PageReference _pageReference)
        throws EFapsException
    {
        this(_model, null, _pageReference);
    }

    /**
     * @param _uuid
     * @param _instanceKey
     * @param _calledByPageRef
     * @throws EFapsException
     */
    public TablePage(final UUID _commandUUID,
                     final String _instanceKey,
                     final PageReference _pageReference)
        throws EFapsException
    {
        this(new TableModel(new UITable(_commandUUID, _instanceKey)), null, _pageReference);
    }


    /**
     * @param _model model for the page
     * @param _modalWindow modal window
     * @throws EFapsException on error
     */
    public TablePage(final IModel<?> _model,
                     final ModalWindowContainer _modalWindow,
                     final PageReference _pageReference)
        throws EFapsException
    {
        super(_model, _modalWindow, _pageReference);
        this.addComponents();
    }

    /**
     * @throws EFapsException on error
     *
     */
    protected void addComponents()
        throws EFapsException
    {
        final UITable table = (UITable) super.getDefaultModelObject();
        if (!table.isInitialized()) {
            table.execute();
        }
        final TablePanel tablebody = new TablePanel("tablebody", new TableModel(table), this);
        this.add(new HeaderPanel("header", tablebody));

        final FormContainer form = new FormContainer("form");
        this.add(form);
        super.addComponents(form);

        form.add(tablebody);
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(TablePage.CSS));
    }
}
