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

package org.efaps.ui.wicket.models;

import java.io.Serializable;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.ui.IEsjpSnipplet;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
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
    private final String esjp;

    private boolean init = false;

    private IEsjpSnipplet snipplet;

    /**
     * @param _esjp
     */
    public EsjpInvoker(final String _esjp)
    {
        this.esjp = _esjp;
    }

    public CharSequence getHtmlSnipplet()
        throws EFapsException
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
                final Class<?> clazz = Class.forName(this.esjp, false, EFapsClassLoader.getInstance());
                this.snipplet = (IEsjpSnipplet) clazz.newInstance();
                this.init = true;
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
        }
    }

    /**
     * @return
     */
    public boolean isVisible()
    {
        initialize();
        boolean ret = false;
        try {
            ret = this.snipplet.isVisible();
        } catch (final EFapsException e) {
            EsjpInvoker.LOG.error("EFapsException", e);
        }
        return ret;
    }

}
