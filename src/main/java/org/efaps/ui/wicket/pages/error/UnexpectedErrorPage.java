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
package org.efaps.ui.wicket.pages.error;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.html.pages.AbstractErrorPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.WebResponse;
import org.wicketstuff.datetime.markup.html.basic.DateLabel;


/**
 * TODO comment!.
 *
 * @author The eFaps Team
 */
public class UnexpectedErrorPage
    extends AbstractErrorPage
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public UnexpectedErrorPage()
    {
        add(homePageLink("homePageLink"));
        add(DateLabel.forDateStyle("date", Model.of(new Date()), "FF"));
    }

    @Override
    protected void setHeaders(final WebResponse _response)
    {
        _response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
