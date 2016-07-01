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

package org.efaps.ui.wicket.pages.content.table;

import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageReference;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.header.HeaderPanel;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * Class renders a page containing a table.
 *
 * @author The eFaps Team
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
    public TablePage(final IModel<?> _model)
        throws EFapsException
    {
        this(_model, (ModalWindowContainer) null);
    }

    /**
     * @param _model model for the page
     * @param _modalWindow modal window
     * @throws EFapsException on error
     */
    public TablePage(final IModel<?> _model,
                     final ModalWindowContainer _modalWindow)
        throws EFapsException
    {
        super(_model, _modalWindow);
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
        this(Model.of(new UITable(_uuid, _instanceKey, _openerId)));
    }

    /**
     * @param _uuid uuid of a commmand
     * @param _instanceKey key to an instance
     * @throws EFapsException on error
     */
    public TablePage(final UUID _uuid,
                     final String _instanceKey)
        throws EFapsException
    {
        this(Model.of(new UITable(_uuid, _instanceKey)));
    }

    /**
     * @param _model            modle for the table
     * @param _pageReference    reference top the page referenced
     * @throws EFapsException on error
     */
    public TablePage(final IModel<UITable> _model,
                     final PageReference _pageReference)
        throws EFapsException
    {
        this(_model, null, _pageReference);
    }

    /**
     * @param _commandUUID             UUID of the calling command
     * @param _instanceKey      key to the instance
     * @param _pageReference    reference top the page referenced
     * @throws EFapsException on error
     */
    public TablePage(final UUID _commandUUID,
                     final String _instanceKey,
                     final PageReference _pageReference)
        throws EFapsException
    {
        this(Model.of(new UITable(_commandUUID, _instanceKey)), null, _pageReference);
    }


    /**
     * @param _model            model for the page
     * @param _modalWindow      modal window
     * @param _pageReference    reference to the page
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
     */
    protected void addComponents()
        throws EFapsException
    {
        final UITable uiTable = (UITable) super.getDefaultModelObject();
        if (!uiTable.isInitialized()) {
            uiTable.execute();
        }

        final TablePanel tablebody = new TablePanel("tablebody", Model.of(uiTable), this);
        this.add(new HeaderPanel("header", tablebody));

        final FormContainer form = new FormContainer("form");
        this.add(form);
        super.addComponents(form);
        form.add(AttributeModifier.append("class", uiTable.getMode().toString()));
        if (uiTable.isOpenedByPicker()) {
            form.add(new AttributeAppender("class", "PICKER", " "));
        }
        form.add(tablebody);
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(TablePage.CSS));
    }
}
