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

package org.efaps.ui.wicket.components.modalwindow;

import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.mock.MockHomePage;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.IRequestHandler;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;

/**
 * This is a wrapper class for a modal window.
 *
 * @author The eFaps Team
 * @version $Id:ModalWindowContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ModalWindowContainer
    extends ModalWindow
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores if the child mut be reloaded.
     */
    private boolean reloadChild = false;

    /**
     * Stores if the parent must be updated on close.
     */
    private boolean updateParent = false;

    /**
     * Show a file on close.
     */
    private boolean targetShowFile = false;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     */
    public ModalWindowContainer(final String _wicketId)
    {
        super(_wicketId);
        if ("w_silver".equals(Configuration.getAttribute(ConfigAttribute.DOJO_MODALCLASS))) {
            setCssClassName(ModalWindow.CSS_CLASS_GRAY);
        } else if ("w_blue".equals(Configuration.getAttribute(ConfigAttribute.DOJO_MODALCLASS))) {
            setCssClassName(ModalWindow.CSS_CLASS_BLUE);
        }
        showUnloadConfirmation(false);
        setTitle(DBProperties.getProperty("Logo.Version.Label"));
    }

    /**
     * This is the getter method for the instance variable {@link #reloadChild}.
     *
     * @return value of instance variable {@link #reloadChild}
     */

    public boolean isReloadChild()
    {
        return this.reloadChild;
    }

    /**
     * This is the setter method for the instance variable {@link #reloadChild}.
     *
     * @param _reloadchild the reloadParent to set
     */
    public void setReloadChild(final boolean _reloadchild)
    {
        this.reloadChild = _reloadchild;
    }

    /**
     * Method is called when the modal window is closed.
     *
     * @param _target AjaxRequestTarget
     */
    @Override
    public void close(final AjaxRequestTarget _target)
    {
        super.close(_target);
        if (this.reloadChild) {
            _target.prependJavaScript(getReloadJavaScript());
        }
        if (this.targetShowFile) {
            ((AbstractMergePage) getPage()).getDownloadBehavior().initiate(_target);
        }
    }

    /**
     * Method creates a JavaScript to reload the parent page.
     *
     * @return JavaScript
     * @throws EFapsException
     */
    public String getReloadJavaScript()
    {
        final AbstractUIPageObject uiObject = (AbstractUIPageObject) getPage().getDefaultModelObject();
        final StringBuilder javascript = new StringBuilder();
        if (uiObject != null) {
            try {
                PageReference calledByPageRef = ((AbstractContentPage) getPage()).getCalledByPageReference();
                if (calledByPageRef != null && calledByPageRef.getPage() instanceof AbstractContentPage) {
                    calledByPageRef = ((AbstractContentPage) calledByPageRef.getPage()).getCalledByPageReference();
                }

                uiObject.resetModel();
                final Page page;
                if (uiObject instanceof UITable) {
                    page = new TablePage(Model.of((UITable) uiObject), calledByPageRef);
                } else if (uiObject instanceof UIForm) {
                    page = new FormPage(Model.of((UIForm) uiObject), calledByPageRef);
                } else if (uiObject instanceof UIStructurBrowser) {
                    page = new StructurBrowserPage(Model.of((UIStructurBrowser) uiObject), calledByPageRef);
                } else {
                    page = new MockHomePage();
                }

                final IRequestHandler handler = new RenderPageRequestHandler(new PageProvider(page));
                final String url = getRequestCycle().urlFor(handler).toString();
                if (calledByPageRef != null && calledByPageRef.getPage() instanceof ContentContainerPage) {
                    final String panelId = ((ContentContainerPage) calledByPageRef.getPage()).getCenterPanelId();
                    javascript.append("var mainFrame = top.dojo.byId(\"").append(MainPage.IFRAME_ID)
                        .append("\").contentWindow;").append("mainFrame.dijit.byId(\"").append(panelId)
                        .append("\").set(\"content\", dojo.create(\"iframe\", {")
                        .append("\"src\": \"").append(url)
                        .append("\",\"style\": \"border: 0; width: 100%; height: 100%\"")
                        .append("}));");
                } else {
                    javascript.append("top.dijit.byId(\"").append("mainPanel")
                        .append("\").set(\"content\", dojo.create(\"iframe\", {")
                        .append("\"src\": \"./wicket/").append(url)
                        .append("\",\"style\": \"border: 0; width: 100%; height: 100%\"")
                        .append("}));");
                }
            } catch (final EFapsException e) {

            }
        }
        return javascript.toString();
    }

    /**
     * This is the getter method for the instance variable {@link #updateParent}
     * .
     *
     * @return value of instance variable {@link #updateParent}
     */

    public boolean isUpdateParent()
    {
        return this.updateParent;
    }

    /**
     * This is the setter method for the instance variable {@link #updateParent}
     * .
     *
     * @param _updateParent the updateParent to set
     */
    public void setUpdateParent(final boolean _updateParent)
    {
        this.updateParent = _updateParent;
    }

    /**
     * This method sets this ModalWindowContainer into the state like it was
     * just created. It uses the default values as they are defined in
     * <code>org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow
     * </code>
     */
    public void reset()
    {
        super.setMinimalWidth(200);
        super.setMinimalHeight(200);
        super.setInitialHeight(400);
        super.setInitialWidth(600);
        super.setUseInitialHeight(true);
        super.setResizable(true);
        super.setHeightUnit("px");
        super.setWidthUnit("px");
        super.setPageCreator(null);
        super.setCloseButtonCallback(null);
        super.setWindowClosedCallback(null);
    }

    /**
     * This method is a exact copy of the private method getCloseJavacript() in
     * {@link #org.efaps.ui.wicket.components.modalwindow.ModalWindow}, but it has to be public.
     *
     * @return JavaScript
     */
    @Override
    public String getCloseJavacript()
    {
        return ModalWindowContainer.getCloseJavacriptInternal();
    }


    private static String getCloseJavacriptInternal()
    {
        return "var win;\n" //
            + "try {\n"
            + " win = top.window.parent.Wicket.Window;\n"
            + "} catch (ignore) {\n"
            + "}\n"
            + "if (typeof(win) == \"undefined\" || typeof(win.current) == \"undefined\") {\n"
            + "  try {\n"
            + "     win = window.Wicket.Window;\n"
            + "  } catch (ignore) {\n"
            + "  }\n"
            + "}\n"
            + "if (typeof(win) != \"undefined\" && typeof(win.current) != \"undefined\") {\n"
            + " var close = function(w) { w.setTimeout(function() {\n"
            + "     win.current.close();\n"
            + " }, 0);  } \n"
            + " try { close(window.parent); } catch (ignore) { close(window); };\n" + "}";
    }



    @Override
    protected CharSequence getShowJavaScript()
    {
        return "top.Wicket.Window.create(settings).show();\n";
    }


    /**
     * Check it the size of the modal window is not to big and reduces it if
     * necessary.
     *
     * @see org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow#setInitialHeight(int)
     * @param _initialHeight height
     * @return this Modalwindow
     */
    @Override
    public ModalWindow setInitialHeight(final int _initialHeight)
    {
        int height = _initialHeight;
        final WebClientInfo wcInfo = (WebClientInfo) getSession().getClientInfo();
        if (wcInfo.getProperties().getBrowserHeight() > 0 && wcInfo.getProperties().getBrowserHeight() < height) {
            height = wcInfo.getProperties().getBrowserHeight() - 33;
        }
        return super.setInitialHeight(height);
    }

    /**
     * Check it the size of the modal window is not to big and reduces it if
     * necessary.
     *
     * @see org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow#setInitialWidth(int)
     * @param _initialWidth width
     * @return this Modalwindow
     */
    @Override
    public ModalWindow setInitialWidth(final int _initialWidth)
    {
        int width = _initialWidth;
        final WebClientInfo wcInfo = (WebClientInfo) getSession().getClientInfo();
        if (wcInfo.getProperties().getBrowserWidth() > 0 && wcInfo.getProperties().getBrowserWidth() < _initialWidth) {
            width = wcInfo.getProperties().getBrowserWidth();
        }
        return super.setInitialWidth(width);
    }

    /**
     * @param _targetShowFile show a file
     */
    public void setTargetShowFile(final boolean _targetShowFile)
    {
        this.targetShowFile = _targetShowFile;
    }
}
