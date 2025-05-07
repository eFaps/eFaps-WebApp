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
package org.efaps.ui.wicket.models.field.factories;

import java.lang.reflect.InvocationTargetException;

import org.apache.wicket.Component;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IJaxb;
import org.efaps.admin.datamodel.ui.IUIValue;
import org.efaps.admin.datamodel.ui.JaxbUI;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public final class JaxbUIFactory
    extends AbstractUIFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static JaxbUIFactory FACTORY;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JaxbUIFactory.class);

    /**
     * Singleton Constructor.
     */
    private JaxbUIFactory()
    {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getReadOnly(final String _wicketId,
                                 final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        final Component ret = super.getReadOnly(_wicketId, _abstractUIField);
        if (ret != null) {
            ret.setEscapeModelStrings(false);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        // not editable
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applies(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return _abstractUIField.getValue().getUIProvider() instanceof JaxbUI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getReadOnlyValue(final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        final Attribute attr = _abstractUIField.getValue().getAttribute();
        String ret = null;
        if (attr != null) {
            try {
                final Class<?> clazz = Class.forName(attr.getClassName(), false,
                                EFapsClassLoader.getInstance());
                final IJaxb jaxb = (IJaxb) clazz.getConstructor().newInstance();
                ret = jaxb.getUISnipplet(_abstractUIField.getParent().getMode(), (IUIValue) _abstractUIField.getValue());
            } catch (final ClassNotFoundException e) {
                JaxbUIFactory.LOG.error("ClassNotFoundException", e);
            } catch (final InstantiationException e) {
                JaxbUIFactory.LOG.error("InstantiationException", e);
            } catch (final IllegalAccessException e) {
                JaxbUIFactory.LOG.error("IllegalAccessException", e);
            } catch (final IllegalArgumentException e) {
                JaxbUIFactory.LOG.error("IllegalArgumentException", e);
            } catch (final InvocationTargetException e) {
                JaxbUIFactory.LOG.error("InvocationTargetException", e);
            } catch (final NoSuchMethodException e) {
                JaxbUIFactory.LOG.error("NoSuchMethodException", e);
            } catch (final SecurityException e) {
                JaxbUIFactory.LOG.error("SecurityException", e);
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return JaxbUIFactory.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPickListValue(final AbstractUIField _uiField)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        return null;
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (JaxbUIFactory.FACTORY == null) {
            JaxbUIFactory.FACTORY = new JaxbUIFactory();
        }
        return JaxbUIFactory.FACTORY;
    }
}
