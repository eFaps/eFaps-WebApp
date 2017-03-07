/*
 * Copyright 2003 - 2017 The eFaps Team
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


package org.efaps.ui.wicket.components.help;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.ui.wicket.pages.help.HelpPage;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;

/**
 * The Class ShowHelpBehavior.
 *
 * @author The eFaps Team
 */
public class ShowHelpBehavior
    extends Behavior
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        final StringBuilder fnct = new StringBuilder()
                .append("var eHDialog = new Dialog({")
                .append(" id: 'eFapsHelpDialog'")
                .append("});")
                .append("eFaps.help = function(_key){")
                .append("eHDialog.href=\"").append(_component.urlFor(HelpPage.class, new PageParameters()))
                .append("?p=\" + _key").append(";")
                .append("eHDialog.show();")
                .append("};");

        final StringBuilder js = new StringBuilder().append("var eFaps = eFaps || {};")
                        .append(DojoWrapper.require(fnct, DojoClasses.popup, DojoClasses.DialogX));

        _response.render(JavaScriptHeaderItem.forScript(js, ShowHelpBehavior.class.getSimpleName()));
    }
}
