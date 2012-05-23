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

package org.efaps.ui.wicket.components.menu;

import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.login.LoginPage;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class LogOutItem
    extends LinkItem
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId Wicket ID
     * @param _model    model
     */
    public LogOutItem(final String _wicketId,
                      final IModel<UIMenuItem> _model)
    {
        super(_wicketId, _model);
    }

    @Override
    public void onClick()
    {
        this.setResponsePage(new LoginPage());
        ((EFapsSession) getSession()).logout();
    }


    @Override
    protected CharSequence getOnClickScript(final CharSequence _url)
    {
        final StringBuilder js = new StringBuilder()
            .append("window.location.href=\"").append(_url)
            .append("\";");
        return js;
    }
}