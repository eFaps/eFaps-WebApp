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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.ws.api.HttpSessionCopy;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.efaps.ui.wicket.EFapsApplication;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.login.LoginPage;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;


/**
 * @author The eFaps Team
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
        final var logoutProviders = EFapsApplication.get().getLogoutProviders();
        final String url = Configuration.getAttribute(ConfigAttribute.LOGOUT_URL);
        if (logoutProviders.isEmpty() && StringUtils.isEmpty(url)) {
            this.setResponsePage(LoginPage.class);
        }
        ((EFapsSession) getSession()).logout();

        if (!logoutProviders.isEmpty()) {
            final HttpServletRequest httpRequest = ((ServletWebRequest) RequestCycle.get().getRequest())
                            .getContainerRequest();
            final HttpSession httpSession = httpRequest.getSession(false);
            if (httpSession != null && !(httpSession instanceof HttpSessionCopy)) {
                for (final var logoutProvider: logoutProviders) {
                    final var logoutUrl = logoutProvider.logoutUrl(httpSession);
                    if (logoutUrl != null) {
                        throw new RedirectToUrlException(logoutUrl);
                    }
                }
            }
        }

        if (StringUtils.isNotEmpty(url)) {
            throw new RedirectToUrlException(url);
        }
    }

    @Override
    protected void onComponentTag(final ComponentTag tag)
    {
        super.onComponentTag(tag);
        tag.put("onClick", getOnClickScript());
    }

    @Override
    protected CharSequence getOnClickScript()
    {
        final StringBuilder js = new StringBuilder()
            .append("window.location.href=\"").append(getURL())
            .append("\";");
        return js;
    }
}
