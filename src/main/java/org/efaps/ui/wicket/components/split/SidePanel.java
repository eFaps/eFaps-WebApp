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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.split;

import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.efaps.ui.wicket.behaviors.dojo.BorderContainerBehavior;
import org.efaps.ui.wicket.behaviors.dojo.BorderContainerBehavior.Design;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior.Region;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.split.header.SplitHeaderPanel;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreePanel;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.UserAttribute;
import org.efaps.util.EFapsException;

/**
 * Class is used to render a Panel which contains a ListMenu.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SidePanel
    extends Panel
{
    /**
     * Reference to the StyleSheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(SidePanel.class, "SidePanel.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The menutree that lies within this panel.
     */
    private final MenuTree menuTree;

    /**
     * Id of the top panel.
     */
    private final String topPanelId;

    /**
     * Getter method for the instance variable {@link #topPanelId}.
     *
     * @return value of instance variable {@link #topPanelId}
     */
    public String getTopPanelId()
    {
        return this.topPanelId;
    }

    /**
     * Constructor.
     *
     * @param _wicketId             wicket id of this component
     * @param _commandUUID          UUID of the related command
     * @param _oid                  oid
     * @param _selectCmdUUID        UUID of the selected Command
     * @param _showStructurBrowser  show the StructurBrowser
     * @throws EFapsException on error
     */
    public SidePanel(final String _wicketId,
                         final UUID _commandUUID,
                         final String _oid,
                         final UUID _selectCmdUUID,
                         final boolean _showStructurBrowser)
        throws EFapsException
    {
        super(_wicketId);
        this.add(new AjaxStorePositionBehavior(_showStructurBrowser));
        this.add(StaticHeaderContrBehavior.forCss(SidePanel.CSS));
        String positionH = Configuration.getUserAttribute(UserAttribute.SPLITTERPOSHORIZONTAL);
        String positionV = Configuration.getUserAttribute(UserAttribute.SPLITTERPOSVERTICAL);

        String splitterState = positionH.equals("0")  ? splitterState = "closed" : null;

        if (positionH == null || positionH.equals("0")) {
            positionH = "200px";
        } else {
            positionH += "px";
        }
        if (positionV == null) {
            positionV = "50%";
        } else {
            positionV += "px";
        }

        final SplitHeaderPanel header = new SplitHeaderPanel(_showStructurBrowser ? "headerTop" : "header");

        final WebMarkupContainer bottom = new WebMarkupContainer("bottom");
        this.add(bottom);

        final WebMarkupContainer overflow = new WebMarkupContainer("overflow");
        bottom.add(overflow);
        overflow.setOutputMarkupId(true);

        final WebMarkupContainer top = new WebMarkupContainer("top");
        this.add(top);
        top.setOutputMarkupPlaceholderTag(true).setOutputMarkupId(true);
        this.topPanelId = top.getMarkupId(true);
        this.menuTree = new MenuTree("menu", _commandUUID, _oid, _selectCmdUUID);

        overflow.add(this.menuTree.setOutputMarkupId(true));

        final StructurBrowserTreePanel stuctbrows = new StructurBrowserTreePanel("stuctbrows", _commandUUID, _oid);
        stuctbrows.setOutputMarkupId(true);
        top.add(stuctbrows);

        this.add(new ContentPaneBehavior(Region.LEADING, true, positionH , null, splitterState));

        if (_showStructurBrowser) {
            add(new WebMarkupContainer("header").setVisible(false));
            add(new BorderContainerBehavior(Design.HEADLINE, true));
            bottom.add(new ContentPaneBehavior(Region.CENTER, true));
            top.add(new ContentPaneBehavior(Region.TOP, true, null, positionV, null));
            top.add(header);
            overflow.add(AttributeModifier.replace("class", "eFapsSplit eFapsListMenuOverflow"));
        } else {
            add(header);
            top.setVisible(false);
            overflow.add(AttributeModifier.replace("class", "eFapsListMenuOverflow"));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.wicket.MarkupContainer#onAfterRenderChildren()
     */
    @Override
    protected void onAfterRenderChildren()
    {
        super.onAfterRenderChildren();
        ((ContentContainerPage) getWebPage()).setMenuTree(this.menuTree);
    }
}
