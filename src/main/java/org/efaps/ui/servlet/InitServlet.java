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

package org.efaps.ui.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.util.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Init the webapp.
 * @author The eFaps Team
 * @version $Id$
 */
public class InitServlet
    extends HttpServlet
{
    /**
     * Needed foer serialization.
     */
    private static final long serialVersionUID = 7212518317632161066L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InitServlet.class);


    /**
     * @param _config ServletConfig
     * @throws ServletException on error
     */
    @Override
    public void init(final ServletConfig _config)
        throws ServletException
    {
        super.init(_config);

        RequestHandler.initReplacableMacros("/"
                        + _config.getServletContext().getServletContextName()
                        + "/");

     // the runlevel should only be loaded once from a filter during the init phase, as long as is is the same one
        if (!"webapp".equals(RunLevel.getName4Current())) {
            try {
                Context.begin();
                RunLevel.init("webapp");
                RunLevel.execute();
            } catch (final EFapsException e) {
                InitServlet.LOG.error("Error during execution of runlevel", e);
                throw new ServletException(e);
            } finally {
                try {
                    Context.rollback();
                } catch (final EFapsException e) {
                    InitServlet.LOG.error("Error during rollback of context after load of runlevel", e);
                    throw new ServletException(e);
                }
            }
        }
    }
}
