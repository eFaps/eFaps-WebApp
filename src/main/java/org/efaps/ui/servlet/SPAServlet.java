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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPAServlet
    extends HttpServlet
{

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(SPAServlet.class);

    private String resourceBase;
    private final Tika tika = new Tika();

    @Override
    public void init(final ServletConfig _config)
    {
        resourceBase = _config.getInitParameter("resourceBase");
    }

    @Override
    protected void doGet(final HttpServletRequest _req, final HttpServletResponse _resp)
        throws ServletException, IOException
    {
        var path = _req.getPathInfo();
        LOG.info("Serving SPA for: {}", path);
        File file = null;
        if (path == null ) {
            file = new File(resourceBase, "index.html");
        } else if (path != null && path.length() > 1 && path.contains("/")) {
            file = new File(resourceBase, path);
            if (!file.exists() || file.isDirectory()) {
                final var pathArray = path.split("\\/");
                path = pathArray[pathArray.length - 1];
                file = new File(resourceBase, path);
            }
        }
        if (file == null || !file.exists() || file.isDirectory()) {
            file = new File(resourceBase, "index.html");
        }
        final String mimeType = tika.detect(file);
        _resp.setContentType(mimeType);
        _resp.setHeader("Cache-Control", "private, max-age=2629746");
        FileUtils.copyFile(file, _resp.getOutputStream());
    }
}
