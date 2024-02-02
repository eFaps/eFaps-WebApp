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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.efaps.admin.program.bundle.BundleInterface;
import org.efaps.admin.program.bundle.BundleMaker;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet that serves the static content for the WebApp.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StaticContentServlet
    extends HttpServlet
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StaticContentServlet.class);

    /**
     * Name of the Cache.
     */
    private static final String CACHENAME = StaticContentServlet.class.getName() + ".Cache";

    /**
     * Used as <code>null</code> for caching purpose.
     */
    private static ContentMapper NULL = new ContentMapper(null, null, null, Long.valueOf(0), Long.valueOf(0));

    /**
     * The method checks the image from the user interface image object out and
     * returns them in a output stream to the web client. The name of the user
     * interface image object must given as name at the end of the path.
     *
     * @param _req request variable
     * @param _res response variable
     * @throws ServletException on error
     *
     */
    @Override
    protected void doGet(final HttpServletRequest _req,
                         final HttpServletResponse _res)
        throws ServletException
    {
        String contentName = _req.getRequestURI();
        contentName = contentName.substring(contentName.lastIndexOf('/') + 1);
        try {
            final Cache<String, ContentMapper> cache = InfinispanCache.get().<String, ContentMapper>getCache(
                            StaticContentServlet.CACHENAME);
            final int cacheDuration = Configuration.getAttributeAsInteger(Configuration.ConfigAttribute.CACHE_DURATION);

            if (!cache.containsKey(contentName)) {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.StaticCompiled);
                queryBldr.addWhereAttrEqValue(CIAdminProgram.StaticCompiled.Name, contentName);
                final MultiPrintQuery multi = queryBldr.getPrint();
                final SelectBuilder selLabel = new SelectBuilder().file().label();
                final SelectBuilder selLength = new SelectBuilder().file().length();
                multi.addSelect(selLabel, selLength);
                multi.addAttribute(CIAdminProgram.StaticCompiled.Name,
                                CIAdminProgram.StaticCompiled.OID,
                                CIAdminProgram.StaticCompiled.Modified);
                multi.executeWithoutAccessCheck();
                if (multi.next()) {
                    final String name = multi.<String>getAttribute(CIAdminProgram.StaticCompiled.Name);
                    final String file = multi.<String>getSelect(selLabel);
                    final Long filelength = multi.<Long>getSelect(selLength);
                    final DateTime datetime = multi.<DateTime>getAttribute(CIAdminProgram.StaticCompiled.Modified);

                    final ContentMapper mapper = new ContentMapper(name, file, multi.getCurrentInstance().getOid(),
                                    filelength, datetime.getMillis());
                    cache.put(contentName, mapper);
                } else {
                    cache.put(contentName, StaticContentServlet.NULL);
                }
            }
            final ContentMapper contentMapper = cache.get(contentName);
            if (contentMapper != null && !contentMapper.equals(StaticContentServlet.NULL)) {
                final Checkout checkout = new Checkout(contentMapper.oid);

                _res.setContentType(getServletContext().getMimeType(contentMapper.file));
                _res.setDateHeader("Last-Modified", contentMapper.time);
                _res.setDateHeader("Expires", System.currentTimeMillis()
                                + (cacheDuration * 1000));
                _res.setHeader("Cache-Control", "max-age=" + cacheDuration);

                if (supportsCompression(_req)) {
                    _res.setHeader("Content-Encoding", "gzip");

                    final ByteArrayOutputStream bytearray = new ByteArrayOutputStream();
                    final GZIPOutputStream zout = new GZIPOutputStream(bytearray);
                    checkout.execute(zout);
                    zout.close();
                    final byte[] b = bytearray.toByteArray();
                    bytearray.close();
                    _res.getOutputStream().write(b);
                    checkout.close();
                } else {
                    _res.setContentLength((int) contentMapper.filelength);
                    checkout.execute(_res.getOutputStream());
                }
            } else if (BundleMaker.containsKey(contentName)) {
                final BundleInterface bundle = BundleMaker.getBundle(contentName);

                _res.setContentType(bundle.getContentType());
                _res.setDateHeader("Last-Modified", bundle.getCreationTime());
                _res.setDateHeader("Expires", System.currentTimeMillis()
                                + (cacheDuration * 1000));
                _res.setHeader("Cache-Control", "max-age=" + cacheDuration);
                _res.setHeader("Content-Encoding", "gzip");

                int bytesRead;
                final byte[] buffer = new byte[2048];

                final InputStream in = bundle.getInputStream(supportsCompression(_req));
                while ((bytesRead = in.read(buffer)) != -1) {
                    _res.getOutputStream().write(buffer, 0, bytesRead);
                }
            }
        } catch (final IOException e) {
            StaticContentServlet.LOG.error("while reading Static Content", e);
            throw new ServletException(e);
        } catch (final CacheReloadException e) {
            StaticContentServlet.LOG.error("while reading Static Content", e);
            throw new ServletException(e);
        } catch (final EFapsException e) {
            StaticContentServlet.LOG.error("while reading Static Content", e);
            throw new ServletException(e);
        }
    }

    /**
     * @param _req request to be analysed
     * @return true  if compression is supported else false
     */
    private boolean supportsCompression(final HttpServletRequest _req)
    {
        boolean ret = false;
        final String accencoding = _req.getHeader("Accept-Encoding");
        if (accencoding != null) {
            ret = accencoding.indexOf("gzip") >= 0;
        }
        return ret;
    }

    /**
     * The class is used to map from the administrational image name
     * to the image file name and image object id.
     */
    private static final class ContentMapper
        implements CacheObjectInterface
    {
        /**
         * The instance variable stores the administational name of the image.
         */
        private final String name;

        /**
         * The instance variable stores the file name of the image.
         */
        private final String file;

        /**
         * The instance variable stores the object id of the image.
         */
        private final String oid;

        /**
         * Length of the file.
         */
        private final long filelength;

        /**
         * Current time.
         */
        private final Long time;

        /**
         * @param _name administrational name of the image
         * @param _file file name of the image
         * @param _oid object id of the image
         * @param _filelength length of the file
         * @param _time current time
         */
        private ContentMapper(final String _name,
                              final String _file,
                              final String _oid,
                              final Long _filelength,
                              final Long _time)
        {
            this.name = _name;
            this.oid = _oid;
            this.file = _file;
            this.filelength = _filelength;
            this.time = _time;
        }

        /**
         * This is the getter method for instance variable {@link #name}.
         *
         * @return value of instance variable {@link #name}
         * @see #name
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * The method is not needed in this cache implementation,
         * but to implemente interface {@link CacheInterface} the
         * method is required.
         *
         * @return always <code>null</code>
         */
        public UUID getUUID()
        {
            return null;
        }

        /**
         * The method is not needed in this cache implementation,
         * but to implemente interface {@link CacheInterface} the
         * method is required.
         *
         * @return always <code>0</code>
         */
        public long getId()
        {
            return 0;
        }
    }
}
