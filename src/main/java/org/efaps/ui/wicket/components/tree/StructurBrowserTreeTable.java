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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.date.UnnestedDatePickers;
import org.efaps.ui.wicket.components.table.cell.CellPanel;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.cell.UIStructurBrowserTableCell;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;

/**
 * This class renders a TreeTable, which loads the children asynchron.<br>
 * The items of the tree consists of junction link, icon and label. An
 * additional arrow showing the direction of the child can be rendered depending
 * on a Tristate. The table shows the columns as defined in the model.
 *
 * @author The eFaps Team
 * @version $Id: StructurBrowserTreeTable.java 7534 2012-05-19 09:32:04Z
 *          jan@moxter.net $
 */
public class StructurBrowserTreeTable
    extends NestedTree<UIStructurBrowser>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * ResourceReference to the StyleSheet used for this TreeTable.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(StructurBrowserTreeTable.class,
                    "StructurTreeTable.css");

    /**
     * Must the link update the parent in the link.
     */
    private final boolean parentLink;

    /**
     * DatePicker.
     */
    private final UnnestedDatePickers datePickers;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id for this component
     * @param _model model
     * @param _columns columns
     * @param _parentLink must the link be done over the parent
     * @param _datePickers DatePicker
     */
    public StructurBrowserTreeTable(final String _wicketId,
                                    final IModel<UIStructurBrowser> _model,
                                    final boolean _parentLink,
                                    final UnnestedDatePickers _datePickers)
    {
        super(_wicketId, new StructurBrowserProvider(_model));
        add(new HumanTheme());
        this.add(StaticHeaderContrBehavior.forCss(StructurBrowserTreeTable.CSS));
        this.parentLink = _parentLink;
        this.datePickers = _datePickers;
    }

    @Override
    protected Component newContentComponent(final String _wicketId,
                                            final IModel<UIStructurBrowser> _model)
    {
        final UIStructurBrowser strucBrws = _model.getObject();
        final UIStructurBrowserTableCell uicell = strucBrws.getColumns().get(strucBrws.getBrowserFieldIndex());
        return  new CellPanel(_wicketId, new UIModel<UITableCell>(uicell),
                        false, strucBrws, 0);
    }



    /**
    * Create a new component for a node.
    *
    * @param _wicketId
    *            the component id
    * @param _model
    *            the model containing the node
    * @return created component
    */
    @Override
    public Component newNodeComponent(final String _wicketId,
                                      final IModel<UIStructurBrowser> _model)
    {
        return new Node<UIStructurBrowser>(_wicketId, this, _model)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component createContent(final String _wicketId,
                                              final IModel<UIStructurBrowser> _model)
            {
                return newContentComponent(_wicketId, _model);
            }

            /*
             * (non-Javadoc)
             * @see org.apache.wicket.extensions.markup.html.repeater.tree.Node#
             * createJunctionComponent(java.lang.String)
             */
            @Override
            protected MarkupContainer createJunctionComponent(final String _id)
            {
                final MarkupContainer ret = super.createJunctionComponent(_id);
                final UIStructurBrowser strucBrws = (UIStructurBrowser) getDefaultModelObject();
                if (strucBrws.getLevel() > 0) {
                    ret.add(AttributeModifier.append("style", "margin-left:" + 15 * (strucBrws.getLevel() - 1) + "px"));
                }
                return ret;
            }
        };
    }

    /**
     * Create a new subtree.
     *
     * @param id component id
     * @param model the model of the new subtree
     * @return the created component
     */
    @Override
    public Component newSubtree(final String _wicketId,
                                final IModel<UIStructurBrowser> _model)
    {
        return new SubElement(_wicketId, this, _model);
    }
}
