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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.tree;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.behaviors.RowSelectedInput;
import org.efaps.ui.wicket.behaviors.dojo.OnDojoReadyHeaderItem;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.header.HeaderPanel;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.util.cache.CacheReloadException;

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
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     * @throws CacheReloadException on error
     */
    public StructurBrowserTreeTablePanel(final String _wicketId,
                                         final IModel<UIStructurBrowser> _model)
        throws CacheReloadException
    {
        super(_wicketId, _model);
        final UIStructurBrowser uiObject = (UIStructurBrowser) super.getDefaultModelObject();

        if (!uiObject.isInitialized()) {
            uiObject.execute();
        }

        add(new RowSelectedInput("selected"));
        final StructurBrowserTreeTable tree = new StructurBrowserTreeTable("treeTable", _model);

        final HeaderPanel header = new HeaderPanel("header", tree, _model);
        add(tree);
        add(header);
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(TablePanel.CSS));
        final StringBuilder js = new StringBuilder();
        js.append("function highlight() {")
            .append("require([\"dojo/on\", \"dojo/dom\", \"dojo/dom-class\", \"dojo/mouse\",")
            .append("\"dojo/query\",\"dojo/NodeList-traverse\", \"dojo/domReady!\"],")
            .append("function(on, dom, domClass, mouse, query) {\n")
            .append("var list =query(\".eFapsSTBRWtmp\");\n")
            .append("list.forEach(function(node){\n")
            .append("on(node, mouse.enter, function(){\n")
            .append("var sibl = query(node).siblings(\".eFapsTableCell,.eFapsTableCheckBoxCell\");\n")
            .append("sibl.forEach(function(sib){\n")
            .append("domClass.add(sib, \"highlight\");\n")
            .append("});\n")
            .append("domClass.add(node, \"highlight\");\n")
            .append(" });\n")
            .append("on(node, mouse.leave, function(){\n")
            .append("var sibl = query(node).siblings(\".eFapsTableCell,.eFapsTableCheckBoxCell\");\n")
            .append("sibl.forEach(function(sib){\n")
            .append("domClass.remove(sib, \"highlight\");\n")
            .append("});\n")
            .append("domClass.remove(node, \"highlight\");\n")
            .append("});\n")
            .append("domClass.remove(node, \"eFapsSTBRWtmp\");\n")
            .append("});\n")
            .append("});\n")
            .append("}\n");
        _response.render(JavaScriptHeaderItem.forScript(js, StructurBrowserTreeTablePanel.class.getName()));
        _response.render(OnDojoReadyHeaderItem.forScript("highlight();"));
    }
}
