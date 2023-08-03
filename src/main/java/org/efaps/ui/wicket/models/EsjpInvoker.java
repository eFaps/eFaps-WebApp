/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.ui.wicket.models;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.ui.IEsjpSnipplet;
import org.efaps.api.ui.IEsjpSnippletProvider;
import org.efaps.util.EFapsBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class EsjpInvoker
    implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EsjpInvoker.class);

    /**
     * ClassName of the esjp that will be invoked.
     */
    private String esjp;

    /** The init. */
    private boolean init = false;

    /** The snipplet. */
    private IEsjpSnipplet snipplet;

    /** The provider. */
    private IEsjpSnippletProvider provider;

    /** The key. */
    private String key;

    /**
     * Instantiates a new esjp invoker.
     *
     * @param _esjp the _esjp
     */
    public EsjpInvoker(final String _esjp)
    {
        this.esjp = _esjp;
    }

    /**
     * Instantiates a new esjp invoker.
     *
     * @param _provider the _provider
     * @param _key the _key
     */
    public EsjpInvoker(final IEsjpSnippletProvider _provider,
                       final String _key)
    {
        this.provider = _provider;
        this.key = _key;
    }

    /**
     * Gets the html snipplet.
     *
     * @return the html snipplet
     * @throws EFapsBaseException the e faps base exception
     */
    public CharSequence getHtmlSnipplet()
        throws EFapsBaseException
    {
        initialize();
        return this.snipplet.getHtmlSnipplet();
    }

    /**
     * @return
     */
    private void initialize()
    {
        try {
            if (!this.init) {
                if (this.esjp != null) {
                    final Class<?> clazz = Class.forName(this.esjp, false, EFapsClassLoader.getInstance());
                    this.snipplet = (IEsjpSnipplet) clazz.getConstructor().newInstance();
                    this.init = true;
                } else if (this.provider != null) {
                    this.snipplet = this.provider.getEsjpSnipplet(this.key);
                    this.init = true;
                }
            }
        } catch (final ClassNotFoundException e) {
            EsjpInvoker.LOG.error("ClassNotFoundException", e);
        } catch (final SecurityException e) {
            EsjpInvoker.LOG.error("SecurityException", e);
        } catch (final IllegalAccessException e) {
            EsjpInvoker.LOG.error("IllegalAccessException", e);
        } catch (final IllegalArgumentException e) {
            EsjpInvoker.LOG.error("IllegalArgumentException", e);
        } catch (final InstantiationException e) {
            EsjpInvoker.LOG.error("InstantiationException", e);
        } catch (final InvocationTargetException e) {
            EsjpInvoker.LOG.error("InvocationTargetException", e);
        } catch (final NoSuchMethodException e) {
            EsjpInvoker.LOG.error("NoSuchMethodException", e);
        }
    }

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    public boolean isVisible()
    {
        initialize();
        boolean ret = false;
        try {
            ret = this.snipplet.isVisible();
        } catch (final EFapsBaseException e) {
            EsjpInvoker.LOG.error("EFapsException", e);
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #snipplet}.
     *
     * @return value of instance variable {@link #snipplet}
     */
    public IEsjpSnipplet getSnipplet()
    {
        return this.snipplet;
    }

    /**
     * Getter method for the instance variable {@link #provider}.
     *
     * @return value of instance variable {@link #provider}
     */
    public IEsjpSnippletProvider getProvider()
    {
        return this.provider;
    }
}
