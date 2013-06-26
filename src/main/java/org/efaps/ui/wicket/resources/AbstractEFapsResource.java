/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.ui.wicket.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Checkout;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * A subclass of WebResource that uses the EFapsResourceStream to provide the
 * Resource.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractEFapsResource
    extends AbstractResource
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * the Name of this AbstractEFapsResource.
     */
    private final String name;

    /**
     * the Type used to query the eFaps-DataBase.
     */
    private final String type;

    /**
     * the ResourceStream for this AbstractEFapsResource.
     */
    private final AbstractEFapsResourceStream stream;

    /**
     * @param _name name
     * @param _type type
     */
    public AbstractEFapsResource(final String _name,
                                 final String _type)
    {
        super();
        this.name = _name;
        this.type = _type;
        this.stream = setNewResourceStream();
    }

    /**
     * this method is used to set the AbstractEFapsResourceStream for the
     * instance variable {@link #stream}. It si called from the Constructor
     * {@link #AbstractEFapsResource(String, String)} and in case that the
     * instance variable {@link #stream} is still null from
     * {@link #getResourceStream()}. The method is implemented as abstract so
     * that all subclasses can use there on subclass of an
     * AbstractEFapsResourceStream.
     *
     * @return AbstractEFapsResourceStream
     */
    protected abstract AbstractEFapsResourceStream setNewResourceStream();



    /**
     * Abstract class implementing the IResourceStream. It is used to retreive
     * an InputStream from Data based on an Object from the eFaps-DataBase.
     */
    protected abstract class AbstractEFapsResourceStream
        implements IResourceStream
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * the InputStream wich will contain the data.
         *
         * @see #getInputStream()
         */
        private InputStream inputStream;

        /**
         * this variable stores the time of the instanciation of this
         * AbstractEFapsResourceStream.
         *
         * @see #AbstractEFapsResource()
         * @see #lastModifiedTime()
         */
        private final Time time;

        /**
         * this instance variiable stores the actual data wich will be returned
         * by the Inputstream.
         *
         * @see #getInputStream()
         * @see #setData()
         */
        private byte[] data;

        /**
         * constructor that stores the time of the instanciation.
         */
        public AbstractEFapsResourceStream()
        {
            this.time = Time.now();
        }

        /**
         * close the resource.
         * @throws IOException on error
         */
        @Override
        public void close()
            throws IOException
        {
            if (this.inputStream != null) {
                this.inputStream.close();
                this.inputStream = null;
            }
        }

        /**
         * get the underlying inputstream.
         * @return InputStream
         * @throws ResourceStreamNotFoundException on error
         */
        public InputStream getInputStream()
            throws ResourceStreamNotFoundException
        {
            if (this.inputStream == null) {
                checkData(false);
                this.inputStream = new ByteArrayInputStream(this.data);
            }
            return this.inputStream;
        }

        /**
         * @return The Locale where this stream did resolve to
         */
        public Locale getLocale()
        {
            return null;
        }

       /**
        * Gets the size of this resource in bytes.
        *
        * @return The size of this resource in the number of bytes, or -1 if unknown
        */
        public Bytes length()
        {
            checkData(true);
            return this.data != null ? Bytes.bytes(this.data.length) : Bytes.bytes(0);
        }

        /**
         * This method shouldn't be used for the outside,
         * It is used by the Loaders to set the resolved locale.
         *
         * @param _locale The Locale where this stream did resolve to.
         */
        public void setLocale(final Locale _locale)
        {
            // not used here
        }

        /**
         * Gets the last time this modifiable thing changed.
         *
         * @return the last modification <code>Time</code>
         */
        public Time lastModifiedTime()
        {
            return this.time;
        }

        /**
         * method that checks if the data was allready retrieved from the
         * eFaps-DataBase or if it must be reloaded due to cache expire.
         *
         * @param _checkDuration should be checked if the cache is expired or
         *            not
         */
        protected void checkData(final boolean _checkDuration)
        {
            if (this.data == null) {
                setData();
            }
        }

        /**
         * set the Data into the instance variable {@link #data}.
         */
        protected void setData()
        {
            try {
                final QueryBuilder queryBldr = new QueryBuilder(Type.get(AbstractEFapsResource.this.type));
                queryBldr.addWhereAttrMatchValue("Name", AbstractEFapsResource.this.name);
                final InstanceQuery query = queryBldr.getQuery();
                query.execute();
                if (query.next()) {
                    final Checkout checkout = new Checkout(query.getCurrentValue());
                    final InputStream tmp = checkout.execute();
                    this.data = IOUtils.toByteArray(tmp);
                    tmp.close();
                    checkout.close();
                }
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            } catch (final IOException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
        }
    }


    /**
     * Getter method for the instance variable {@link #stream}.
     *
     * @return value of instance variable {@link #stream}
     */
    public AbstractEFapsResourceStream getStream()
    {
        return this.stream;
    }
}
