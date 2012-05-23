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
package org.efaps.ui.wicket.pages.contentcontainer;

import java.util.UUID;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Search;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.Opener;
import org.efaps.ui.wicket.behaviors.dojo.BorderContainerBehavior;
import org.efaps.ui.wicket.behaviors.dojo.BorderContainerBehavior.Design;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior.Region;
import org.efaps.ui.wicket.components.LazyIframe;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.split.ListOnlyPanel;
import org.efaps.ui.wicket.components.split.StructBrowsSplitPanel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.util.EFapsException;

/**
 * This class renders a Page with is used as a Container for the Content. <br/>
 * This is necessary to be able to have a spilt in the page, abd be able to
 * reuse the same classes for the ContentPages. The Split contains on the left a
 * menu or tree and on the right an iframe for the content.
 *
 * @author The eFaps Team
 * @version $Id:ContentContainerPage.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ContentContainerPage
    extends AbstractMergePage
{

    /**
     * this static variable is used as the wicketid for the IFrame.
     */
    public static final String IFRAME_WICKETID = "splitrightactiframe";

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
     * Variable contains the key to the MenuTree.
     */
    private String menuTreeKey;

    /**
     * Variable contains the Path to the IFrame-Component so that ist can be
     * accessed by other classes.
     *
     * @see #getInlinePath()
     */
    private String inlinePath;

    /**
     * Variable contains the Path to the Split-Component so that ist can be
     * accessed by other classes.
     *
     * @see #getSplitPath()
     */
    private String splitPath;

    /**
     * Does this Page contain a StucturBrowser.
     */
    private boolean structurbrowser;

    /**
     * Is the content a WebForm or a Table?
     */
    private boolean webForm;

    private String centerPanelId;

    private MenuTree menuTree;


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
     * Constructor called from the client directly by using parameters. Normally
     * it should only contain one parameter Opener.OPENER_PARAKEY to access the
     * opener.
     *
     * @param _parameters PageParameters
     * @throws EFapsException on error
     */
    public ContentContainerPage(final PageParameters _parameters)
        throws EFapsException
    {
        super();
        final Opener opener = ((EFapsSession) getSession())
                        .getOpener(_parameters.get(Opener.OPENER_PARAKEY).toString());
        final UUID commandUUID;
        final String instanceKey;
        if (opener.getModel() != null) {
            final AbstractUIObject uiObject = (AbstractUIObject) opener.getModel().getObject();
            commandUUID = uiObject.getCommandUUID();
            instanceKey = uiObject.getInstanceKey();
        } else {
            commandUUID = opener.getCommandUUID();
            instanceKey = opener.getInstanceKey();
        }
        initialise(commandUUID, instanceKey, null);
    }

    /**
     * @param _uuid UUID of the command
     * @param _instanceKey oid
     * @throws EFapsException on error on error
     */
    public ContentContainerPage(final UUID _uuid,
                                final String _instanceKey)
        throws EFapsException
    {
        initialise(_uuid, _instanceKey, null);
    }

    /**
     * @param _pageMap page map
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
     * @param _pageMap page map
     * @param _uuid UUID of the command
     * @param _instanceKey oid
     * @param _addStructurBrowser add a structor browser
     * @throws EFapsException on error
     */
    public ContentContainerPage(final UUID _uuid,
                                final String _instanceKey,
                                final UUID _selectedCmdUUID,
                                final boolean _addStructurBrowser)
        throws EFapsException
    {
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
        borderPanel.add(new BorderContainerBehavior(Design.SIDEBAR));

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

        final LazyIframe centerPanel = new LazyIframe("centerPanel", new IPageLink()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public Page getPage()
            {
                Page error = null;
                AbstractContentPage page = null;
                try {
                    if (ContentContainerPage.this.webForm) {
                        page = new FormPage(uuid4NewPage, _instanceKey, true);
                    } else {
                        if (getCommand(uuid4NewPage).getTargetStructurBrowserField() == null) {
                            page = new TablePage(uuid4NewPage, _instanceKey, true);
                        } else {
                            page = new StructurBrowserPage(uuid4NewPage, _instanceKey, true);
                        }
                    }
                } catch (final EFapsException e) {
                    error = new ErrorPage(e);
                }
                page.setMenuTreeKey(ContentContainerPage.this.menuTreeKey);
                return error == null ? page : error;
            }

            @Override
            public Class<? extends Page> getPageIdentity()
            {
                return AbstractContentPage.class;
            }
        });
        borderPanel.add(centerPanel);
        centerPanel.add(new ContentPaneBehavior(Region.CENTER, false));
        this.centerPanelId = centerPanel.getMarkupId(true);

        // ((EFapsSession) getSession()).getUpdateBehaviors().clear();
        //
        final ClientProperties properties = ((WebClientInfo) getSession().getClientInfo()).getProperties();
        // we use different StyleSheets for different Browsers
        if (properties.isBrowserInternetExplorer()) {
            add(StaticHeaderContrBehavior.forCss(ContentContainerPage.CSS_IE));
        } else {
            add(StaticHeaderContrBehavior.forCss(ContentContainerPage.CSS));
        }
        if (this.structurbrowser) {
            borderPanel.add(new StructBrowsSplitPanel("leftPanel", _uuid, _instanceKey, this.menuTreeKey,
                            _selectCmdUUID));
        } else {
            borderPanel.add(new ListOnlyPanel("leftPanel", _uuid, _instanceKey, this.menuTreeKey, _selectCmdUUID));
        }

        //
        // final WebMarkupContainer parent = new
        // WebMarkupContainer("splitrightact");
        // right.add(parent);
        // parent.setOutputMarkupId(true);
        //
        // // select the defaultCommand
        //
        // this.webForm = cmd.getTargetForm() != null;
        // // set the Path to the IFrame
        // this.inlinePath =
        // inline.getPath().substring(inline.getPath().indexOf(":") + 1);
        // // set the Path to the Split
        // this.splitPath =
        // split.getPath().substring(inline.getPath().indexOf(":") + 1);
        //
        // this.add(new ChildCallBackHeaderContributer());
    }

    /**
     * This is the getter method for the instance variable {@link #inlinePath}.
     *
     * @return value of instance variable {@link #inlinePath}
     */

    public String getInlinePath()
    {
        return this.inlinePath;
    }

    /**
     * This is the getter method for the instance variable {@link #menuTreeKey}.
     *
     * @return value of instance variable {@link #menuTreeKey}
     */

    public String getMenuTreeKey()
    {
        return this.menuTreeKey;
    }

    /**
     * This is the getter method for the instance variable {@link #splitPath}.
     *
     * @return value of instance variable {@link #splitPath}
     */

    public String getSplitPath()
    {
        return this.splitPath;
    }

    /**
     * Method to get a Command.
     *
     * @param _uuid Uuid of the Command
     * @return a AbstractCommand
     */
    private AbstractCommand getCommand(final UUID _uuid)
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
     * @param _menuTree
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
}
