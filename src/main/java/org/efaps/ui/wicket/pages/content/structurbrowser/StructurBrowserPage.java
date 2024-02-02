/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.pages.content.structurbrowser;

import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageReference;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreeTablePanel;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * Class renders a page containing a structure browser.
 *
 * @author The eFaps Team
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
     * @param _commandUUID  UUID of the calling command
     * @param _instanceKey  key to the instance
     * @throws EFapsException on error
     */
    public StructurBrowserPage(final UUID _commandUUID,
                               final String _instanceKey)
        throws EFapsException
    {
        this(Model.of(new UIStructurBrowser(_commandUUID, _instanceKey)));
    }

    /**
     * @param _model        model for this pager
     * @throws EFapsException on error
     */
    public StructurBrowserPage(final IModel<?> _model)
        throws EFapsException
    {
        this(_model, (ModalWindowContainer) null);
    }

    /**
     * @param _model model  for this pager
     * @param _modalWindow  modal Window this page is opened in
     * @throws EFapsException on error
     */
    public StructurBrowserPage(final IModel<?> _model,
                               final ModalWindowContainer _modalWindow)
        throws EFapsException
    {
        this(_model, _modalWindow, null);
    }

    /**
     * @param _model            model for this pager
     * @param _calledByPageRef  Reference to the Page opening this StructurBrowserPage
     * @throws EFapsException on error
     */
    public StructurBrowserPage(final IModel<?> _model,
                               final PageReference _calledByPageRef)
        throws EFapsException
    {
        this(_model, null, _calledByPageRef);
    }


    /**
     * @param _commandUUID      UUID of the command opening the StructurBrowserPage
     * @param _instanceKey      key to the instance opening the StructurBrowserPage
     * @param _calledByPageRef  Reference to the Page opening this StructurBrowserPage
     * @throws EFapsException on error
     */
    public StructurBrowserPage(final UUID _commandUUID,
                               final String _instanceKey,
                               final PageReference _calledByPageRef)
        throws EFapsException
    {
        this(Model.of(new UIStructurBrowser(_commandUUID, _instanceKey)), null, _calledByPageRef);
    }

    /**
     * @param _model            model for this pager
     * @param _modalWindow      modal Window this page is opened in
     * @param _calledByPageRef  Refernce to the Page opening this StructurBrowserPage
     * @throws EFapsException on error
     */
    public StructurBrowserPage(final IModel<?> _model,
                               final ModalWindowContainer _modalWindow,
                               final PageReference _calledByPageRef)
        throws EFapsException
    {
        super(_model, _modalWindow, _calledByPageRef);
        this.addComponents();
    }

    /**
     * Method to add the components to this page.
     *
     * @throws EFapsException on error
     */
    protected void addComponents()
        throws EFapsException
    {
        final UIStructurBrowser uiObject = (UIStructurBrowser) super.getDefaultModelObject();
        if (!uiObject.isInitialized()) {
            uiObject.execute();
        }

        final FormContainer form = new FormContainer("form");
        this.add(form);
        super.addComponents(form);
        form.add(AttributeModifier.append("class", uiObject.getMode().toString()));
        form.add(new StructurBrowserTreeTablePanel("structurBrowserTable", Model.of(uiObject)));
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(StructurBrowserPage.CSS));
    }
}
