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

package org.efaps.ui.wicket.pages.content.structurbrowser;

import java.util.UUID;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreeTablePanel;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * Class renders a page containing a structure browser.
 *
 * @author The eFaps Team
 * @version $Id:StructurBrowserPage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class StructurBrowserPage
    extends AbstractContentPage
{
    /**
     * Reference to the style sheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(StructurBrowserPage.class,
                    "StructurBrowserPage.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 7564911406648729094L;

    /**
     * Constructor called from the client directly by using parameters. Normally
     * it should only contain one parameter Opener.OPENER_PARAKEY to access the
     * opener.
     *
     * @param _parameters PageParameters
     * @throws EFapsException on error
     */
    public StructurBrowserPage(final PageParameters _parameters)
        throws EFapsException
    {
        this(new UIModel<UIStructurBrowser>(new UIStructurBrowser(_parameters)));
    }

    /**
     * @param _model model for this pager
     * @throws EFapsException  on error
     */
    public StructurBrowserPage(final IModel<UIStructurBrowser> _model)
        throws EFapsException
    {
        this(_model, null);
    }

    /**
     * @param _model model for this pager
     * @param _modalWindow modal Winthis page is opened in
     * @throws EFapsException  on error
     */
    public StructurBrowserPage(final IModel<UIStructurBrowser> _model,
                               final ModalWindowContainer _modalWindow)
        throws EFapsException
    {
        super(_model, _modalWindow);
        this.addComponents();
    }


    /**
     * @param _pageMap pagemap
     * @param _commandUUID UUID of the calling command
     * @param _oid oid
     * @throws EFapsException on error
     */
    public StructurBrowserPage(final UUID _commandUUID,
                               final String _oid)
        throws EFapsException
    {
        super(new UIModel<UIStructurBrowser>(new UIStructurBrowser(_commandUUID, _oid)), null);
        this.addComponents();
    }

    /**
     * Method to add the components to this page.
     * @throws EFapsException  on error
     */
    protected void addComponents()
        throws EFapsException
    {
        add(StaticHeaderContributor.forCss(StructurBrowserPage.CSS));

        final UIStructurBrowser uiObject = (UIStructurBrowser) super.getDefaultModelObject();
        if (!uiObject.isInitialized()) {
            uiObject.execute();
        }

        final FormContainer form = new FormContainer("form");
        this.add(form);
        super.addComponents(form);
        form.add(new StructurBrowserTreeTablePanel("structurBrowserTable", new UIModel<UIStructurBrowser>(uiObject), true));
    }
}
