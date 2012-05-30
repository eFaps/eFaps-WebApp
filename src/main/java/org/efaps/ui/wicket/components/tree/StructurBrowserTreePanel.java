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

import java.util.UUID;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.util.EFapsException;

/**
 * This class renders a Panel containing a StructurBrowserTree.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StructurBrowserTreePanel
    extends Panel
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     * @param _commandUUID UUID of the related command
     * @param _oid oid
     * @param _listmenukey key to the list menu
     * @throws EFapsException on error
     */
    public StructurBrowserTreePanel(final String _wicketId,
                                    final UUID _commandUUID,
                                    final String _oid)
        throws EFapsException
    {
        this(_wicketId, new UIModel<UIStructurBrowser>(new UIStructurBrowser(_commandUUID, _oid)));
    }

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     * @param _listmenukey key to the list menu
     */
    public StructurBrowserTreePanel(final String _wicketId,
                                    final IModel<UIStructurBrowser> _model)
    {
        super(_wicketId, _model);
        final UIStructurBrowser uiStrBrws = _model.getObject();
        if (!uiStrBrws.isInitialized()) {
            uiStrBrws.execute();
        }
        final StructurBrowserTree tree = new StructurBrowserTree("tree", _model);
        this.add(tree);
    }
}
