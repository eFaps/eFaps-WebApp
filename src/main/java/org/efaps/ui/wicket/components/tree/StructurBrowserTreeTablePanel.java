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

package org.efaps.ui.wicket.components.tree;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.behaviors.RowSelectedInput;
import org.efaps.ui.wicket.components.date.UnnestedDatePickers;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.header.HeaderPanel;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class StructurBrowserTreeTablePanel
    extends Panel
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * must the link be done using the parent or the listmenu updated.
     */
    private final boolean parentLink;

    /**
     * DatePickers.
     */
    private final UnnestedDatePickers datePickers;

    /**
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     * @param _parentLink must the link be done using the parent
     */
    public StructurBrowserTreeTablePanel(final String _wicketId,
                                         final IModel<UIStructurBrowser> _model,
                                         final boolean _parentLink)
    {
        super(_wicketId, _model);
        add(StaticHeaderContrBehavior.forCss(TablePanel.CSS));
        this.parentLink = _parentLink;
        final UIStructurBrowser uiObject = (UIStructurBrowser) super.getDefaultModelObject();

        if (!uiObject.isInitialized()) {
            uiObject.execute();
        }
        this.datePickers = new UnnestedDatePickers("datePickers");
        add(this.datePickers);
        add(new RowSelectedInput("selected"));
        final StructurBrowserTreeTable tree = new StructurBrowserTreeTable("treeTable", _model, _parentLink,
                        this.datePickers);

        final HeaderPanel header = new HeaderPanel("header", tree, _model);
        add(tree);
        add(header);
    }
}
