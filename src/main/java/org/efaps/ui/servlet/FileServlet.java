/*
 * Copyright 2003 - 2014 The eFaps Team
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.NumberFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.efaps.admin.AppConfigHandler;
import org.efaps.admin.user.Person;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet used to give access to files for a user.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FileServlet
    extends HttpServlet
{
    /**
     * Name of the folder inside the "official" temporary folder.
     */
    public static final String TMPFOLDERNAME = "eFapsUserDepTemp";

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ImageServlet.class);

    /**
     * Search for the requested file in the folder corresponding to the user of the context.
     *
     * @param _req request variable
     * @param _resp response variable
     * @throws ServletException on error
     */
    @Override
    protected void doGet(final HttpServletRequest _req,
                         final HttpServletResponse _resp)
        throws ServletException
    {
        String fileName = _req.getRequestURI();
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        try {
            final Person pers = Context.getThreadContext().getPerson();
            if (pers != null) {
                final File file = getFile(pers.getId(), fileName);
                if (file != null && file.exists()) {
                    _resp.setContentType(getServletContext().getMimeType(file.getName()));
                    _resp.setContentLength((int) file.length());
                    _resp.setDateHeader("Last-Modified", System.currentTimeMillis());
                    _resp.setDateHeader("Expires", System.currentTimeMillis());
                    _resp.addHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
                    _resp.setHeader("Cache-Control", "max-age=10");
                    final FileInputStream input = new FileInputStream(file);
                    IOUtils.copy(input, _resp.getOutputStream());
                }
            }
        } catch (final EFapsException e) {
            FileServlet.LOG.error("EFapsException", e);
            throw new ServletException(e);
        } catch (final IOException e) {
            FileServlet.LOG.error("IOException", e);
            throw new ServletException(e);
        }
    }

    /**
     * @param _personId personid
     * @param _fileName name of the file
     * @return the file if found
     * @throws IOException on error
     */
    private File getFile(final Long _personId,
                         final String _fileName)
        throws IOException
    {
        File tmpfld = AppConfigHandler.get().getTempFolder();
        if (tmpfld == null) {
            final File temp = File.createTempFile("eFaps", ".tmp");
            tmpfld = temp.getParentFile();
            temp.delete();
        }
        final File storeFolder = new File(tmpfld, FileServlet.TMPFOLDERNAME);
        final NumberFormat formater = NumberFormat.getInstance();
        formater.setMinimumIntegerDigits(8);
        formater.setGroupingUsed(false);
        final File userFolder = new File(storeFolder, formater.format(_personId));
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }
        return new File(userFolder, _fileName);
    }
}
