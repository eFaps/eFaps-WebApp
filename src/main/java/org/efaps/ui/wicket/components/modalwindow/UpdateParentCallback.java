/*
 * Copyright 2003 - 2020 The eFaps Team
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

package org.efaps.ui.wicket.components.modalwindow;

import java.io.File;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * Class implements the WindowClosedCallback to be able to update (refresch)
 * the parent page on closing a modal window.
 *
 * @author The eFaps Team
 */
public class UpdateParentCallback
    implements ModalWindow.WindowClosedCallback
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the Page this call back belongs to.
     */
    private final PageReference pageReference;

    /**
     * Modal window this call back belongs to.
     */
    private final ModalWindowContainer modalwindow;

    /**
     * Must the model of the page be updated on update of the parent page.
     */
    private final boolean clearmodel;

    private final boolean showFile;

    /**
     * Constructor setting the panel and the modal window.
     *
     * @see #UpdateParentCallback(Component, ModalWindowContainer, boolean)
     *
     * @param _pageReference Reference to the Page this call back belongs to
     * @param _modalwindow modal window belonging to this call back
     *
     */
    public UpdateParentCallback(final PageReference _pageReference,
                                final ModalWindowContainer _modalwindow)
    {
        this(_pageReference, _modalwindow, true, false);
    }

    /**
     * Constructor setting the panel and the modal window.
     *
     *
     *
     * @param _pageReference Reference to the Page this call back belongs to
     * @param _modalwindow modal window belonging to this call back
     * @param _clearmodel must the model of the page be updated
     */
    public UpdateParentCallback(final PageReference _pageReference,
                                final ModalWindowContainer _modalwindow,
                                final boolean _clearmodel,
                                final boolean _showFile)
    {
        pageReference = _pageReference;
        modalwindow = _modalwindow;
        clearmodel = _clearmodel;
        showFile = _showFile;
    }

    /**
     * Method is executed on close of the modal window.
     *
     * @param _target Target
     */
    @Override
    public void onClose(final AjaxRequestTarget _target)
    {
        if (modalwindow.isUpdateParent()) {
            final Object object = pageReference.getPage().getDefaultModelObject();
            try {
                if (object instanceof AbstractUIObject) {

                    final AbstractUIObject uiObject = (AbstractUIObject) object;
                    if (clearmodel) {
                        uiObject.resetModel();
                    }
                    AbstractContentPage page = null;

                    if (uiObject instanceof UITable) {
                        page = new TablePage(Model.of((UITable) uiObject), ((AbstractContentPage) pageReference
                                        .getPage()).getModalWindow(), ((AbstractContentPage) pageReference
                                                        .getPage()).getCalledByPageReference());
                    } else if (uiObject instanceof UIForm) {
                        page = new FormPage(Model.of((UIForm) uiObject), ((AbstractContentPage) pageReference
                                        .getPage()).getModalWindow(), ((AbstractContentPage) pageReference
                                                        .getPage()).getCalledByPageReference());
                    } else if (uiObject instanceof UIStructurBrowser) {
                        page = new StructurBrowserPage(Model.of((UIStructurBrowser) uiObject),
                                        ((AbstractContentPage) pageReference.getPage()).getModalWindow(),
                                        ((AbstractContentPage) pageReference.getPage())
                                                        .getCalledByPageReference());
                    }
                    RequestCycle.get().setResponsePage(page);
                    if (showFile && uiObject.getInstance() != null && uiObject.getInstance().isValid()) {
                        final File file = UIGrid.checkout(uiObject.getInstance());
                        if (file != null) {
                            ((EFapsSession) _target.getPage().getSession()).setFile(file);
                            page.getDownloadBehavior().initiate();
                        }
                    }
                } else if (object instanceof UIGrid) {
                    final UIGrid uiGrid = (UIGrid) object;
                    uiGrid.reload();
                    final var gridPage = new GridPage(Model.of(uiGrid));
                    RequestCycle.get().setResponsePage(gridPage);
                    if (showFile) {
                        final File file = UIGrid.checkout(uiGrid.getInstance());
                        if (file != null) {
                            ((EFapsSession) _target.getPage().getSession()).setFile(file);
                            gridPage.getDownloadBehavior().initiate();
                        }
                    }
                }
            } catch (final EFapsException e) {
                RequestCycle.get().setResponsePage(new ErrorPage(e));
            }
        }
    }
}
