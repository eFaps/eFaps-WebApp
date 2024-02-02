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
package org.efaps.ui.wicket.components.menu;

import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.dashboard.DashboardPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsException;

/**
 * Render an home link.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class HomeItem
    extends LinkItem
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId     wicketId for this item
     * @param _model        model for this item
     */
    public HomeItem(final String _wicketId,
                    final IModel<UIMenuItem> _model)
    {
        super(_wicketId, _model);
    }

    /**
     * On click it is evaluated what must be responded.
     */
    @Override
    public void onClick()
    {
        try {
            setResponsePage(new DashboardPage(getPage().getPageReference()));
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected CharSequence getOnClickScript(final CharSequence _url)
    {
        CharSequence script;
        if (Configuration.getAttributeAsBoolean(ConfigAttribute.BOARDV2_ACTIVE)) {
            final StringBuilder js = new StringBuilder()
                        .append("registry.byId(\"").append("mainPanel").append(
                                        "\").set(\"content\", domConstruct.create(\"iframe\", {")
                        .append("\"id\": \"")
                        .append(MainPage.IFRAME_ID).append("\",\"src\": \"").append("./servlet/apps/dashboard/")
                            .append("\",\"style\": \"border: 0; width: 100%; height: 99%\"")
                        .append(",\"id\": \"")
                        .append(MainPage.IFRAME_ID).append("\"").append("}));");
                script = DojoWrapper.require(js, DojoClasses.registry, DojoClasses.domConstruct);
        } else {
            script =  super.getOnClickScript(_url);
        }
        return script;
    }
}
