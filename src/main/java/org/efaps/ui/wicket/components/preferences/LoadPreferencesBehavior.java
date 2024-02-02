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
package org.efaps.ui.wicket.components.preferences;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;

/**
 * The Class LoadPreferencesBehavior.
 */
public class LoadPreferencesBehavior
    extends AjaxEventBehavior
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new load preferences behavior.
     */
    public LoadPreferencesBehavior()
    {
        super("click");
    }

    @Override
    protected void onEvent(final AjaxRequestTarget _target)
    {
        final String componentId = getComponent().getPage().visitChildren(PreferencesPanel.class, (_panel,
                                                                        _visit) -> {
            _panel.setVisible(true);
            _visit.stop(_panel.getMarkupId(true));
            _target.add(_panel);
        });

        final StringBuilder js = new StringBuilder()
                        .append("var pttd = new TooltipDialog({\n")
                        .append("  style: 'width: 300px;',\n")
                        .append("  content: dom.byId('").append(componentId).append("'),\n")
                        .append("});\n")
                        .append("popup.open({\n")
                        .append("  popup: pttd,\n")
                        .append("  orient: [\"below-centered\", \"above-centered\"],\n")
                        .append("  around: dom.byId('eFapsUserName'),\n")
                        .append("});\n")
                        .append("var ov = domConstruct.create('div', {\n")
                        .append("'class': 'preferencesOverlay'\n")
                        .append("}, baseWindow.body());\n")
                        .append("query('.preferencesOverlay').on('click', function (e) {\n")
                        .append("popup.close(pttd);\n")
                        .append("var dp = dom.byId('").append(componentId).append("');\n")
                        .append("if (dp) {\n")
                        .append("var nw = registry.findWidgets(dp);\n")
                        .append("array.forEach(nw, function (w) {\n")
                        .append("w.destroyRecursive();\n")
                        .append("});\n")
                        .append("}\n")
                        .append("domConstruct.destroy(e.target);\n")
                        .append("});\n")
                        .append("\n");

        _target.appendJavaScript(DojoWrapper.require(js, DojoClasses.registry, DojoClasses.dom, DojoClasses.query,
                        DojoClasses.domConstruct, DojoClasses.on, DojoClasses.TooltipDialog, DojoClasses.popup,
                        DojoClasses.baseWindow, DojoClasses.array));
    }
}
