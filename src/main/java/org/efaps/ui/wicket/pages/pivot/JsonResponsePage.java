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
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.ui.IPivotProvider;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonResponsePage
    extends WebPage
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(JsonResponsePage.class);

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
                try {
                    final String providerClass = Configuration.getAttribute(ConfigAttribute.PIVOT_PROVIDER);
                    final Class<?> clazz = Class.forName(providerClass, false, EFapsClassLoader.getInstance());
                    final IPivotProvider provider = (IPivotProvider) clazz.newInstance();
                    response.write(provider.getJsonData(val));
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    LOG.error("Could not find/instantiate Provider Class", e);
                }
            }
        });
        setStatelessHint(true);
    }
}
