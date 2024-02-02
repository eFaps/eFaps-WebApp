/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.components.search;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.json.index.result.Dimension;
import org.efaps.ui.wicket.components.search.IndexSearch.DimTreeNode;
import org.efaps.ui.wicket.components.search.IndexSearch.DimensionProvider;
import org.efaps.ui.wicket.components.tree.StructurBrowserTree;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class DimensionPanel
    extends Panel
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new taxonomy panel.
     *
     * @param _wicketId the _wicket id
     * @param _model the model
     */
    public DimensionPanel(final String _wicketId,
                          final IModel<IndexSearch> _model)
    {
        super(_wicketId, _model);
        setOutputMarkupId(true);
        final Form<Void> form = new Form<>("dimForm");
        add(form);
        final DimensionProvider provider = _model.getObject().getDimensionProvider();

        final NestedTree<DimTreeNode> dimTree = new NestedTree<DimTreeNode>("dimTree", provider)
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            protected Component newContentComponent(final String _wicketId,
                                                    final IModel<DimTreeNode> _model)
            {
                final Component ret;
                if (_model.getObject().getValue() instanceof Dimension) {
                    ret = new Label(_wicketId, _model.getObject().getLabel());
                } else {
                    ret = new DimValuePanel(_wicketId, _model);
                }
                return ret;
            }
        };

        form.add(dimTree);
        dimTree.getModelObject().addAll(provider.getRootList());
        dimTree.add(new HumanTheme());
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(StructurBrowserTree.CSS));
    }
}
