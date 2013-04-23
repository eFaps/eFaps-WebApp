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

package org.efaps.ui.wicket.components.split.header;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.IRecent;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.util.EFapsException;

/**
 * Class renders the header inside a ListOnlyPanel or a StructBrowsSplitPanel.
 * The header contains all functionalities
 * to expand and collapse the menus.
 *
 * @author The eFaps TEam
 * @version $Id:SplitHeaderPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class SplitHeaderPanel
    extends Panel
{

    /**
     * Reference to the style sheet.
     */
    public static final EFapsContentReference CSS =
                    new EFapsContentReference(SplitHeaderPanel.class, "SplitHeaderPanel.css");

    /**
     * Enum for the StyleSheets which are used in this Component.
     */
    public enum Css
    {
        /**style sheet for the recetn links. */
        RECENT("eFapsRecent"),
        /** style sheet for the open title. */
        TITEL("eFapsSplitTitel");

        /**
         * Stores the key of the Region.
         */
        private final String value;

        /**
         * Private Constructor.
         *
         * @param _value Key
         */
        private Css(final String _value)
        {
            this.value = _value;
        }

        /**
         * Getter method for instance variable {@link #value}.
         *
         * @return value of instance variable {@link #value}
         */
        public String getValue()
        {
            return this.value;
        }
    }

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id of this component
     * @throws EFapsException on error
     */
    public SplitHeaderPanel(final String _wicketId)
        throws EFapsException
    {
        super(_wicketId);
        setOutputMarkupId(true);
        add(AttributeModifier.append("class", "eFapsSplitHeader"));
        final Label titel = new Label("titel", DBProperties.getProperty("Split.Titel"));
        this.add(titel);
        titel.add(AttributeModifier.append("class", SplitHeaderPanel.Css.TITEL.value));

        // the recent link part
        final WebMarkupContainer recent = new WebMarkupContainer("recent");
        this.add(recent);
        recent.add(AttributeModifier.append("class", SplitHeaderPanel.Css.RECENT.value));
        final List<IRecent> allRecents = ((EFapsSession) getSession()).getAllRecents();
        if (allRecents.isEmpty()) {
            recent.setVisible(false);
        } else {

            final int maxLength = Configuration.getAttributeAsInteger(Configuration.ConfigAttribute.RECENT_LINKMAX);

            final RepeatingView repeater = new RepeatingView("repeater");
            recent.add(repeater);
            for (final IRecent rec : allRecents) {
                repeater.add(new RecentLink(repeater.newChildId(), rec, maxLength));
            }
        }
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(SplitHeaderPanel.CSS));
    }
}
