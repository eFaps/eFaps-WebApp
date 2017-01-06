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
package org.efaps.ui.wicket.pages.contentcontainer;

import java.util.UUID;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Search;
import org.efaps.ui.wicket.behaviors.dojo.BorderContainerBehavior;
import org.efaps.ui.wicket.behaviors.dojo.BorderContainerBehavior.Design;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior.Region;
import org.efaps.ui.wicket.components.LazyIframe;
import org.efaps.ui.wicket.components.LazyIframe.IFrameProvider;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.split.SidePanel;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * This class renders a Page with is used as a Container for the Content. <br/>
 * This is necessary to be able to have a split in the page and be able to reuse
 * the same classes for the ContentPages. The Split contains on the left a menu
 * or tree and on the right an iframe for the content.
 *
 * @author The eFaps Team
 * @version $Id:ContentContainerPage.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ContentContainerPage
    extends AbstractMergePage
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 3169723830151134904L;

    /**
     * Static variable as Reference to the Stylesheet for the Page (normal).
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(ContentContainerPage.class,
                    "ContentContainerPage.css");

    /**
     * static variable as Reference to the Stylesheet for the Page (Internet
     * Explorer).
     */
    private static final EFapsContentReference CSS_IE = new EFapsContentReference(ContentContainerPage.class,
                    "ContentContainerPage_IE.css");

    /**
     * Does this Page contain a StucturBrowser.
     */
    private final boolean structurbrowser;

    /**
     * Is the content a WebForm or a Table?
     */
    private boolean webForm;

    /**
     * Id of the center panel.
     */
    private String borderPanelId;

    /**
     * Id of the center panel.
     */
    private String centerPanelId;

    /**
     * The MenuTree for this page.
     */
    private MenuTree menuTree;

    /**
     * @param _uuid UUID of the command
     * @param _instanceKey oid
     * @throws EFapsException on error on error
     */
    public ContentContainerPage(final UUID _uuid,
                                final String _instanceKey)
        throws EFapsException
    {
        this(_uuid, _instanceKey, false);
    }

    /**
     * @param _uuid UUID of the command
     * @param _instanceKey oid
     * @param _addStructurBrowser add a StructurBrowser to this page
     * @throws EFapsException on error on error
     */
    public ContentContainerPage(final UUID _uuid,
                                final String _instanceKey,
                                final boolean _addStructurBrowser)
        throws EFapsException
    {
        this(_uuid, _instanceKey, null, _addStructurBrowser);
    }

    /**
     * @param _uuid UUID of the calling command
     * @param _instanceKey instance key
     * @param _selectedCmdUUID UUID of the selected command
     * @throws EFapsException on error
     */
    public ContentContainerPage(final UUID _uuid,
                                final String _instanceKey,
                                final UUID _selectedCmdUUID)
        throws EFapsException
    {

        this(_uuid, _instanceKey, _selectedCmdUUID, false);
    }

    /**
     * @param _uuid UUID of the command
     * @param _instanceKey oid
     * @param _selectedCmdUUID UUID of the selected command
     * @param _addStructurBrowser add a StructurBrowser to this page
     * @throws EFapsException on error
     */
    public ContentContainerPage(final UUID _uuid,
                                final String _instanceKey,
                                final UUID _selectedCmdUUID,
                                final boolean _addStructurBrowser)
        throws EFapsException
    {
        super();
        this.structurbrowser = _addStructurBrowser;
        initialise(_uuid, _instanceKey, _selectedCmdUUID);
    }

    /**
     * Method to initialize the Page.
     *
     * @param _uuid uuid of the command
     * @param _instanceKey key to the instance
     * @param _selectCmdUUID uuid of the selected Command
     * @throws EFapsException on error
     */
    private void initialise(final UUID _uuid,
                            final String _instanceKey,
                            final UUID _selectCmdUUID)
        throws EFapsException
    {
        final WebMarkupContainer borderPanel = new WebMarkupContainer("borderPanel");
        this.add(borderPanel);
        borderPanel.add(new BorderContainerBehavior(Design.SIDEBAR, true));
        this.borderPanelId = borderPanel.getMarkupId(true);

        final AbstractCommand cmd = getCommand(_uuid);
        UUID tmpUUID = _uuid;
        this.webForm = cmd.getTargetForm() != null;
        if (cmd instanceof Menu) {
            for (final AbstractCommand childcmd : ((Menu) cmd).getCommands()) {
                if (_selectCmdUUID == null && childcmd.isDefaultSelected()) {
                    tmpUUID = childcmd.getUUID();
                    this.webForm = childcmd.getTargetForm() != null;
                    break;
                } else if (childcmd.getUUID().equals(_selectCmdUUID)) {
                    tmpUUID = childcmd.getUUID();
                    this.webForm = childcmd.getTargetForm() != null;
                    break;
                }
            }
        }
        final UUID uuid4NewPage = tmpUUID;

        final LazyIframe centerPanel = new LazyIframe("centerPanel", new IFrameProvider()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public Page getPage(final Component _component)
            {
                Page error = null;
                AbstractContentPage page = null;
                try {
                    if (ContentContainerPage.this.webForm) {
                        final UIForm uiForm = new UIForm(uuid4NewPage, _instanceKey).setPagePosition(PagePosition.TREE);
                        page = new FormPage(Model.of(uiForm), getPageReference());
                    } else {
                        if (getCommand(uuid4NewPage).getTargetStructurBrowserField() == null) {
                            page = new TablePage(uuid4NewPage, _instanceKey, getPageReference());
                        } else {
                            page = new StructurBrowserPage(uuid4NewPage, _instanceKey, getPageReference());
                        }
                    }
                } catch (final EFapsException e) {
                    error = new ErrorPage(e);
                }
                return error == null ? page : error;
            }
        }, null);

        borderPanel.add(centerPanel);
        centerPanel.add(new ContentPaneBehavior(Region.CENTER, false));
        this.centerPanelId = centerPanel.getMarkupId(true);

        borderPanel.add(new SidePanel("leftPanel", _uuid, _instanceKey, _selectCmdUUID,
                        this.structurbrowser));
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        final ClientProperties properties = ((WebClientInfo) getSession().getClientInfo()).getProperties();
        // we use different StyleSheets for different Browsers
        if (properties.isBrowserInternetExplorer()) {
            _response.render(AbstractEFapsHeaderItem.forCss(ContentContainerPage.CSS_IE));
        } else {
            _response.render(AbstractEFapsHeaderItem.forCss(ContentContainerPage.CSS));
        }
    }

    /**
     * Method to get a Command.
     *
     * @param _uuid Uuid of the Command
     * @return a AbstractCommand
     * @throws CacheReloadException on error
     */
    private AbstractCommand getCommand(final UUID _uuid)
        throws CacheReloadException
    {
        AbstractCommand cmd = Command.get(_uuid);
        if (cmd == null) {
            cmd = Menu.get(_uuid);
            if (cmd == null) {
                cmd = Search.get(_uuid);
            }
        }
        return cmd;
    }

    /**
     * @param _menuTree the menuTree for this page
     */
    public void setMenuTree(final MenuTree _menuTree)
    {
        this.menuTree = _menuTree;
    }

    /**
     * Getter method for the instance variable {@link #menuTree}.
     *
     * @return value of instance variable {@link #menuTree}
     */
    public MenuTree getMenuTree()
    {
        return this.menuTree;
    }

    /**
     * Getter method for the instance variable {@link #centerPanelId}.
     *
     * @return value of instance variable {@link #centerPanelId}
     */
    public String getCenterPanelId()
    {
        return this.centerPanelId;
    }

    /**
     * Getter method for the instance variable {@link #borderPanelId}.
     *
     * @return value of instance variable {@link #borderPanelId}
     */
    public String getBorderPanelId()
    {
        return this.borderPanelId;
    }
}
