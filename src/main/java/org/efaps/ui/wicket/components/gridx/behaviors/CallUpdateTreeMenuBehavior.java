/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.ui.wicket.components.gridx.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.components.menutree.MenuUpdateBehavior;
import org.efaps.ui.wicket.pages.main.MainPage;

/**
 * The Class UpdateTreeMenuBehavior.
 *
 * @author The eFaps Team
 */
public class CallUpdateTreeMenuBehavior
    extends Behavior
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The instance. */
    private final Instance instance;

    /**
     * Instantiates a new call update tree menu behavior.
     *
     * @param _instance the instance
     */
    public CallUpdateTreeMenuBehavior(final Instance _instance)
    {
        this.instance = _instance;
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        final StringBuilder js = new StringBuilder()
                    .append("var frameWin = top.dojo.doc.getElementById(\"")
                        .append(MainPage.IFRAME_ID).append("\").contentWindow;")
                    .append(" frameWin.").append(MenuUpdateBehavior.FUNCTION_NAME).append("(\"")
                    .append(MenuUpdateBehavior.PARAMETERKEY4INSTANCE).append("\");");
        _response.render(JavaScriptHeaderItem.forScript(js, CallUpdateTreeMenuBehavior.class.getName()));
        Session.get().setMetaData(MenuUpdateBehavior.METAKEY, this.instance);
    }
}
