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
package org.efaps.ui.wicket.components.modalwindow;

import org.apache.wicket.Page;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * Thic Class is used to create a page inside a modal window lazily.
 *
 * @author The eFaps Team
 */
public class ModalWindowAjaxPageCreator
    implements LegacyModalWindow.PageCreator
{

    /**
     * Needed foer serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Model for the page to be created.
     */
    private final ICmdUIObject uiObject;

    /**
     * The modal window the page will be created in.
     */
    private final ModalWindowContainer modalWindow;

    /** The page position. */
    private final PagePosition pagePosition;

    /**
     * Constructor.
     *
     * @param _uiObject object for the page to create
     * @param _modalWindow modal window the page will be created in
     * @param _pagePosition the page position
     */
    public ModalWindowAjaxPageCreator(final ICmdUIObject _uiObject,
                                      final ModalWindowContainer _modalWindow,
                                      final PagePosition _pagePosition)
    {
        this.uiObject = _uiObject;
        this.modalWindow = _modalWindow;
        this.pagePosition = _pagePosition;
    }

    /**
     * Method that creates the page.
     *
     * @return new Page
     */
    @Override
    public Page createPage()
    {
        Page ret = null;
        try {
            final String instKey = this.uiObject.getInstance() == null ? null : this.uiObject.getInstance().getKey();

            if (this.uiObject.getCommand().getTargetTable() == null) {
                final UIForm uiform = new UIForm(this.uiObject.getCommand().getUUID(), instKey)
                                .setPagePosition(this.pagePosition);
                uiform.setPicker(this.uiObject);
                if (!uiform.isInitialized()) {
                    uiform.execute();
                }
                if (uiform.getElements().isEmpty()) {
                    ret = new DialogPage(this.modalWindow.getPage().getPageReference(),
                                    uiform.getCommand().getName() + ".InvalidInstance", false, false);
                } else {
                    if (this.uiObject.getCommand().isSubmit()) {
                        final IRequestParameters parameters = RequestCycle.get().getRequest().getRequestParameters();
                        uiform.setSelected(parameters.getParameterValues("selectedRow"));
                    }
                    ret = new FormPage(Model.of(uiform), this.modalWindow, this.modalWindow.getPage()
                                    .getPageReference());
                }
            } else if (this.uiObject.getCommand().getTargetStructurBrowserField() == null) {
                final UITable uitable = new UITable(this.uiObject.getCommand().getUUID(), instKey)
                                .setPagePosition(this.pagePosition);
                uitable.setPicker(this.uiObject);
                ret = new TablePage(Model.of(uitable), this.modalWindow, this.modalWindow.getPage()
                                .getPageReference());
            } else {
                final UIStructurBrowser uiPageObject = new UIStructurBrowser(this.uiObject.getCommand().getUUID(),
                                instKey);
                uiPageObject.setPicker(this.uiObject);
                ret = new StructurBrowserPage(Model.of(uiPageObject), this.modalWindow,
                                this.modalWindow.getPage().getPageReference());
            }
        } catch (final EFapsException e) {
            ret = new ErrorPage(e);
        }
        return ret;
    }
}
