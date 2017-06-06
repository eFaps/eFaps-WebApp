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

package org.efaps.ui.servlet;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The servlet checks out user interface images depending on the
 * administrational name (not file name).<br/>
 * E.g.:<br/>
 * <code>/efaps/servlet/image/Admin_UI_Image</code>.
 *
 * @author The eFaps Team
 */
public class ImageServlet
    extends HttpServlet
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = -2469349574113406199L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ImageServlet.class);

    /**
     * Name of the Cache.
     */
    private static final String CACHENAME = ImageServlet.class.getName() + ".Cache";;

    /**
     * Used as <code>null</code> for caching purpose.
     */
    private static ImageMapper NULL = new ImageMapper(null, null, null, Long.valueOf(0), Long.valueOf(0));

    /**
     * The method checks the image from the user interface image object out and
     * returns them in a output stream to the web client. The name of the user
     * interface image object must given as name at the end of the path.
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
        String imgName = _req.getRequestURI();

        imgName = imgName.substring(imgName.lastIndexOf('/') + 1);

        try {
            final Cache<String, ImageMapper> cache = InfinispanCache.get().<String, ImageMapper>getCache(
                            ImageServlet.CACHENAME);
            if (!cache.containsKey(imgName)) {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.Image);
                queryBldr.addWhereAttrEqValue(CIAdminUserInterface.Image.Name, imgName);
                final MultiPrintQuery multi = queryBldr.getPrint();
                final SelectBuilder selLabel = new SelectBuilder().file().label();
                final SelectBuilder selLength = new SelectBuilder().file().length();
                multi.addSelect(selLabel, selLength);
                multi.addAttribute(CIAdminUserInterface.Image.Name,
                                CIAdminUserInterface.Image.Modified);
                multi.executeWithoutAccessCheck();
                if (multi.next()) {
                    final String name = multi.<String>getAttribute(CIAdminUserInterface.Image.Name);
                    final String file = multi.<String>getSelect(selLabel);
                    final Long filelength = multi.<Long>getSelect(selLength);
                    final DateTime time = multi.<DateTime>getAttribute(CIAdminUserInterface.Image.Modified);
                    final ImageMapper imageMapper = new ImageMapper(multi.getCurrentInstance(),
                                    name, file, filelength, time.getMillis());
                    cache.put(imgName, imageMapper);
                } else {
                    cache.put(imgName, ImageServlet.NULL);
                }
            }

            final ImageMapper imageMapper = cache.get(imgName);
            if (imageMapper != null && !imageMapper.equals(ImageServlet.NULL)) {
                final Checkout checkout = new Checkout(imageMapper.instance);

                _res.setContentType(getServletContext().getMimeType(imageMapper.file));
                _res.setContentLength((int) imageMapper.filelength);
                _res.setDateHeader("Last-Modified", imageMapper.time);

                _res.setDateHeader("Expires", System.currentTimeMillis()
                                + (3600 * 1000));
                _res.setHeader("Cache-Control", "max-age=3600");

                checkout.execute(_res.getOutputStream());
                checkout.close();
            }
        } catch (final IOException e) {
            ImageServlet.LOG.error("while reading history data", e);
            throw new ServletException(e);
        } catch (final CacheReloadException e) {
            ImageServlet.LOG.error("while reading history data", e);
            throw new ServletException(e);
        } catch (final EFapsException e) {
            ImageServlet.LOG.error("while reading history data", e);
            throw new ServletException(e);
        }
    }

    /**
     * The class is used to map from the administrational image name to the
     * image file name and image object id.
     */
    private static final class ImageMapper
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
         * Lenght of the image in long.
         */
        private final long filelength;

        /**
         * Time the image was last retrieved.
         */
        private final Long time;

        /**
         * Instance of this image.
         */
        private final Instance instance;

        /**
         * @param _instance Instance
         * @param _name administrational name of the image
         * @param _file file name of the image
         * @param _filelength lenght of the file
         * @param _time time
         */
        private ImageMapper(final Instance _instance,
                            final String _name,
                            final String _file,
                            final Long _filelength,
                            final Long _time)
        {
            this.name = _name;
            this.instance = _instance;
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
        @Override
        public String getName()
        {
            return this.name;
        }

        /**
         * The method is not needed in this cache implementation, but to
         * implement interface {@link CacheInterface} the method is required.
         *
         * @return always <code>null</code>
         */
        @Override
        public UUID getUUID()
        {
            return null;
        }

        /**
         * The method is not needed in this cache implementation, but to
         * implement interface {@link CacheInterface} the method is required.
         *
         * @return always <code>0</code>
         */
        @Override
        public long getId()
        {
            return 0;
        }
    }
}
