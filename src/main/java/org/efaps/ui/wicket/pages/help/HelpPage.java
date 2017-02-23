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

package org.efaps.ui.wicket.pages.help;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.ui.IHelpProvider;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.util.EFapsBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class HelpPage.
 *
 * @author The eFaps Team
 */
public class HelpPage
    extends AbstractMergePage
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HelpPage.class);

    /**
     * Instantiates a new help page.
     *
     * @param _parameters the parameters
     */
    public HelpPage(final PageParameters _parameters)
    {
        final StringBuilder html = new StringBuilder();
        try {
            final long cmdId = _parameters.get("p").toLong();
            final Class<?> clazz = Class.forName(Configuration.getAttribute(Configuration.ConfigAttribute.HELPSNIPPROV),
                            false, EFapsClassLoader.getInstance());
            final IHelpProvider provider = (IHelpProvider) clazz.newInstance();
            html.append(provider.getHelp(cmdId));
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                        | EFapsBaseException e) {
            HelpPage.LOG.error("ClassNotFoundException", e);
        }

        final WebComponent markup = new WebComponent("helpContent")
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTagBody(final MarkupStream _markupStream,
                                           final ComponentTag _openTag)
            {
                super.onComponentTagBody(_markupStream, _openTag);
                replaceComponentTagBody(_markupStream, _openTag, html);
            }
        };
        add(markup);
    }

    /**
     * Checks for help.
     *
     * @param _cmdId the cmd id
     * @return true, if successful
     */
    public static boolean hasHelp(final Long _cmdId)
    {
        boolean ret = false;
        try {
            final Class<?> clazz = Class.forName(Configuration.getAttribute(Configuration.ConfigAttribute.HELPSNIPPROV),
                            false, EFapsClassLoader.getInstance());
            final IHelpProvider provider = (IHelpProvider) clazz.newInstance();
            ret = provider.hasHelp(_cmdId);
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                        | EFapsBaseException e) {
            HelpPage.LOG.error("ClassNotFoundException", e);
        }
        return ret;
    }
}
