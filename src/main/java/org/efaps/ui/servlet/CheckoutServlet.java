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
package org.efaps.ui.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * The servlet checks out files from objects.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CheckoutServlet
    extends HttpServlet
{

    /**
     * Needed foer serialization.
     */
    private static final long serialVersionUID = 7810426422513524710L;

    /**
     * name of the object id parameter.
     */
    private static final String PARAM_OID = "oid";

    /**
     * The method checks the file from the object out and returns them
     * in a output stream to the web client. The object
     * id must be given with paramter {@link #PARAM_OID}.<br/>
     *
     * @param _req request variable
     * @param _res response variable
     * @throws ServletException on error
     */
    @Override
    protected void doGet(final HttpServletRequest _req,
                         final HttpServletResponse _res)
        throws ServletException
    {
        final String oid = _req.getParameter(CheckoutServlet.PARAM_OID);

        try {
            final Instance instance = Instance.get(oid);
            if (instance.isValid()) {
                final Checkout checkout = new Checkout(instance);
                final InputStream input = checkout.execute();
                _res.setContentType(this.getServletContext().getMimeType(
                                checkout.getFileName()));
                _res.setContentLength((int) checkout.getFileLength());
                _res.addHeader("Content-Disposition", "inline; filename=\""
                                + checkout.getFileName() + "\"");
                IOUtils.copy(input, _res.getOutputStream());
            }
        } catch (final IOException e) {
            throw new ServletException(e);
        } catch (final EFapsException e) {
            throw new ServletException(e);
        }
    }
}
