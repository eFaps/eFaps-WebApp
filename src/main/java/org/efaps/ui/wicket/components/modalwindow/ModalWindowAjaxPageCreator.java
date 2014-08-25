/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.ui.wicket.components.modalwindow;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.UIModel;
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
 * @version $Id:ModalWindowAjaxPageCreator.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ModalWindowAjaxPageCreator
    implements ModalWindow.PageCreator
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

    /**
     * Constructor.
     *
     * @param _uiObject object for the page to create
     * @param _modalWindow modal window the page will be created in
     */
    public ModalWindowAjaxPageCreator(final ICmdUIObject _uiObject,
                                      final ModalWindowContainer _modalWindow)
    {
        this.uiObject = _uiObject;
        this.modalWindow = _modalWindow;
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
            if (this.uiObject.getCommand().getTargetTable() == null) {
                final UIForm uiform = new UIForm(this.uiObject.getCommand().getUUID(), this.uiObject.getInstanceKey());
                uiform.setPicker(this.uiObject);
                if (!uiform.isInitialized()) {
                    uiform.execute();
                }
                if (uiform.getElements().isEmpty()) {
                    ret = new DialogPage(this.modalWindow.getPage().getPageReference(),
                                    uiform.getCommand().getName() + ".InvalidInstance", false, false);
                } else {
                    ret = new FormPage(new FormModel(uiform), this.modalWindow, this.modalWindow.getPage()
                                    .getPageReference());
                }
            } else {
                if (this.uiObject.getCommand().getTargetStructurBrowserField() == null) {
                    final UITable uitable = new UITable(this.uiObject.getCommand().getUUID(),
                                    this.uiObject.getInstanceKey());
                    uitable.setPicker(this.uiObject);
                    ret = new TablePage(new TableModel(uitable), this.modalWindow, this.modalWindow.getPage()
                                    .getPageReference());
                } else {
                    final UIStructurBrowser uiPageObject = new UIStructurBrowser(this.uiObject.getCommand().getUUID(),
                                    this.uiObject.getInstanceKey());
                    uiPageObject.setPicker(this.uiObject);
                    ret = new StructurBrowserPage(new UIModel<UIStructurBrowser>(uiPageObject), this.modalWindow,
                                    this.modalWindow.getPage().getPageReference());
                }
            }
        } catch (final EFapsException e) {
            ret = new ErrorPage(e);
        }
        return ret;
    }
}
