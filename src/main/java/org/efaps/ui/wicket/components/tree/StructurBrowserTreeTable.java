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
import org.apache.wicket.PageReference;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.SetModel;
import org.efaps.ui.wicket.components.date.UnnestedDatePickers;
import org.efaps.ui.wicket.components.table.cell.CellPanel;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.cell.UIStructurBrowserTableCell;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;

/**
 * This class renders a TreeTable, which loads the children asynchron.<br>
 * The items of the tree consists of junction link, icon and label.
 * The table shows the columns as defined in the model.
 *
 * @author The eFaps Team
 * @version $Id$
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
     * @param _parentLink must the link be done over the parent
     * @param _datePickers DatePicker
     */
    public StructurBrowserTreeTable(final String _wicketId,
                                    final IModel<UIStructurBrowser> _model,
                                    final boolean _parentLink,
                                    final UnnestedDatePickers _datePickers)
    {
        super(_wicketId, new StructurBrowserProvider(_model),
                        new SetModel<UIStructurBrowser>(_model.getObject().getExpandedBrowsers()));
        if ("human".equals(Configuration.getAttribute(ConfigAttribute.STRUCBRWSRTREE_CLASS))) {
            add(new HumanTheme());
        } else if ("windows".equals(Configuration.getAttribute(ConfigAttribute.STRUCBRWSRTREE_CLASS))) {
            add(new WindowsTheme());
        }
        this.parentLink = _parentLink;
        this.datePickers = _datePickers;
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(StructurBrowserTreeTable.CSS));
    }

    @Override
    protected Component newContentComponent(final String _wicketId,
                                            final IModel<UIStructurBrowser> _model)
    {
        final UIStructurBrowser strucBrws = _model.getObject();
        final UIStructurBrowserTableCell uicell = strucBrws.getColumns().get(strucBrws.getBrowserFieldIndex());
        final PageReference pageRef = ((AbstractContentPage) getPage()).getCalledByPageReference();
        boolean updateMenu = false;
        if (pageRef != null && pageRef.getPage() instanceof ContentContainerPage) {
            updateMenu = true;
        }
        return new CellPanel(_wicketId, new UIModel<UITableCell>(uicell), updateMenu, strucBrws, 0);
    }

    @Override
    public void expand(final UIStructurBrowser _uiStrBrws)
    {
        super.expand(_uiStrBrws);
        _uiStrBrws.setExpanded(true);
    }

    /**
     * Collapse the given node, tries to update the affected branch if the
     * change happens on an {@link AjaxRequestTarget}.
     *
     * @param _uiStrBrws    the object to collapse
     */
    @Override
    public void collapse(final UIStructurBrowser _uiStrBrws)
    {
        super.collapse(_uiStrBrws);
        _uiStrBrws.setExpanded(false);
    }

    /**
     * Create a new component for a node.
     *
     * @param _wicketId  the component id
     * @param _model     the model containing the node
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

            @Override
            protected MarkupContainer createJunctionComponent(final String _id)
            {
                final UIStructurBrowser strucBrws = (UIStructurBrowser) getDefaultModelObject();
                final MarkupContainer ret;
                if (strucBrws.hasChildren() && strucBrws.isForceExpanded()) {
                    ret = new WebMarkupContainer(_id);

                } else {
                    ret = super.createJunctionComponent(_id);
                }

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
     * @param _wicketId     wicket id for this component
     * @param _model        the model of the new subtree
     * @return the created component
     */
    @Override
    public Component newSubtree(final String _wicketId,
                                final IModel<UIStructurBrowser> _model)
    {
        return new SubElement(_wicketId, this, _model);
    }
}
