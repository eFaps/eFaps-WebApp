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


package org.efaps.ui.wicket.models;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.efaps.admin.program.esjp.EFapsClassLoader;


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
     * ClassNaem of the esjp that will be invoked.
     */
    private final String esjp;

    /**
     * @param _esjp
     */
    public EsjpInvoker(final String _esjp)
    {
       this.esjp = _esjp;
    }

    /**
     * @return
     */
    public CharSequence getHtmlSnipplet()
    {
        CharSequence ret = "";
        try {
            final Class<?> clazz = Class.forName("org.efaps.esjp.ui.dashboard.StatusPanel", false,
                            EFapsClassLoader.getInstance());
            final Method method = clazz.getMethod("getHtmlSnipplet");
            ret = (CharSequence) method.invoke(clazz.newInstance());
        } catch (final ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret;
    }

}
