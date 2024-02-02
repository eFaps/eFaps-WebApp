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
package org.efaps.ui.wicket.components.tree;

import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.SetModel;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.ui.wicket.components.table.field.FieldPanel;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.objects.UIFieldStructurBrowser;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.cache.CacheReloadException;

/**
 * This class renders a TreeTable, which loads the children asynchron.<br>
 * The items of the tree consists of junction link, icon and label.
 * The table shows the columns as defined in the model.
 *
 * @author The eFaps Team
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

    /** The topic name. */
    private final String topicName;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id for this component
     * @param _model model
     * @throws CacheReloadException on error
     */
    public StructurBrowserTreeTable(final String _wicketId,
                                    final IModel<UIStructurBrowser> _model)
        throws CacheReloadException
    {
        super(_wicketId, new StructurBrowserProvider(_model),
                        new SetModel<>(_model.getObject().getExpandedBrowsers()));
        if (_model.getObject() instanceof UIFieldStructurBrowser) {
            final FieldTable field = FieldTable.get(((UIFieldStructurBrowser) _model.getObject()).getFieldTabelId());
            this.topicName = field.getName();
        } else {
            this.topicName = _model.getObject().getTable().getName();
        }

        if ("human".equals(Configuration.getAttribute(ConfigAttribute.STRUCBRWSRTREE_CLASS))) {
            add(new HumanTheme());
        } else if ("windows".equals(Configuration.getAttribute(ConfigAttribute.STRUCBRWSRTREE_CLASS))) {
            add(new WindowsTheme());
        }
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
        final AbstractUIField uiField = strucBrws.getColumns().get(strucBrws.getBrowserFieldIndex());
        return new FieldPanel(_wicketId, Model.of(uiField));
    }

    @Override
    public void expand(final UIStructurBrowser _uiStrBrws)
    {
        super.expand(_uiStrBrws);
        _uiStrBrws.setExpanded(true);
        final Optional<AjaxRequestTarget> optionalTarget = getRequestCycle().find(AjaxRequestTarget.class);
        final StringBuilder js = new StringBuilder()
                    .append("highlight();positionTableColumns(eFapsTable").append(_uiStrBrws.getTableId()).append(");")
                    .append("require([\"dojo/topic\"], function(topic){\n")
                    .append("topic.publish(\"eFaps/expand/").append(this.topicName).append("\");\n")
                    .append("})\n");
        optionalTarget.ifPresent(target -> target.appendJavaScript(js));
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
        final Optional<AjaxRequestTarget> optionalTarget = getRequestCycle().find(AjaxRequestTarget.class);
        final StringBuilder js = new StringBuilder()
                    .append("positionTableColumns(eFapsTable").append(_uiStrBrws.getTableId()).append(");")
                    .append("require([\"dojo/topic\"], function(topic){\n")
                    .append("topic.publish(\"eFaps/collapse/").append(this.topicName).append("\");\n")
                    .append("})\n");
        optionalTarget.ifPresent(target -> target.appendJavaScript(js));
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
