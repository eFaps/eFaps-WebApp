/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.ui.wicket.pages.pivot;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.drools.core.util.StringUtils;

public class JsonResponsePage
    extends WebPage
{
    private static final long serialVersionUID = 1L;

    public JsonResponsePage(final PageParameters _pageParameters)
    {
        final StringValue parameter = _pageParameters.get("datasource");
        final String val = parameter.toOptionalString();
        getRequestCycle().scheduleRequestHandlerAfterCurrent(_requestCycle -> {
            final WebResponse response = (WebResponse) _requestCycle.getResponse();
            response.setContentType("application/json");
            if (StringUtils.isEmpty(val)) {
                response.write("[]");
            } else {
                response.write("[\n" +
                                "    {\n" +
                                "        \"Product\": \"Apple\",\n" +
                                "        \"Price\": 2.50\n" +
                                "    },\n" +
                                "    {\n" +
                                "        \"Product\": \"Cherry\",\n" +
                                "        \"Price\": 5.25\n" +
                                "    }\n" +
                                "]");
            }
        });
        setStatelessHint(true);
    }
}
