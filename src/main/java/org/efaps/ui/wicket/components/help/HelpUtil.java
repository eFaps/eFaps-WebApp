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

package org.efaps.ui.wicket.components.help;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.admin.ui.Command;
import org.efaps.api.ui.IHelpProvider;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsBaseException;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Help.
 *
 * @author The eFaps Team
 */
public final class HelpUtil
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HelpUtil.class);

    /**
     * Instantiates a new help util.
     */
    private HelpUtil()
    {
    }

    /**
     * Checks for help.
     *
     * @param _cmdId the cmd id
     * @return true, if successful
     */
    public static boolean hasHelp(final Long _cmdId)
    {
        boolean ret = false;
        try {
            final Class<?> clazz = Class.forName(Configuration.getAttribute(Configuration.ConfigAttribute.HELPSNIPPROV),
                            false, EFapsClassLoader.getInstance());
            final IHelpProvider provider = (IHelpProvider) clazz.getConstructor().newInstance();
            ret = provider.hasHelp(_cmdId);
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                        | EFapsBaseException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
            HelpUtil.LOG.error("ClassNotFoundException", e);
        }
        return ret;
    }

    /**
     * Checks for help.
     *
     * @return true, if successful
     */
    public static boolean isHelpAdmin()
    {
        boolean ret = false;
        try {
            final Class<?> clazz = Class.forName(Configuration.getAttribute(Configuration.ConfigAttribute.HELPSNIPPROV),
                            false, EFapsClassLoader.getInstance());
            final IHelpProvider provider = (IHelpProvider) clazz.getConstructor().newInstance();
            ret = provider.isHelpAdmin();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                        | EFapsBaseException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
            HelpUtil.LOG.error("ClassNotFoundException", e);
        }
        return ret;
    }

    /**
     * Checks for help.
     *
     * @return true, if successful
     */
    public static boolean isEditMode()
    {
        boolean ret = false;
        try {
            final Class<?> clazz = Class.forName(Configuration.getAttribute(Configuration.ConfigAttribute.HELPSNIPPROV),
                            false, EFapsClassLoader.getInstance());
            final IHelpProvider provider = (IHelpProvider) clazz.getConstructor().newInstance();
            ret = provider.isEditMode();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                        | EFapsBaseException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
            HelpUtil.LOG.error("ClassNotFoundException", e);
        }
        return ret;
    }

    /**
     * Gets the help.
     *
     * @param _cmdId the cmd id
     * @return the help
     */
    public static CharSequence getHelp(final long _cmdId)
    {
        CharSequence ret;
        try {
            final Class<?> clazz = Class.forName(Configuration.getAttribute(Configuration.ConfigAttribute.HELPSNIPPROV),
                            false, EFapsClassLoader.getInstance());
            final IHelpProvider provider = (IHelpProvider) clazz.getConstructor().newInstance();
            ret = provider.getHelp(_cmdId);
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                        | EFapsBaseException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
            HelpUtil.LOG.error("ClassNotFoundException", e);
            ret = "";
        }
        return ret;
    }

    /**
     * Gets the menu item.
     *
     * @return the menu item
     * @throws CacheReloadException the cache reload exception
     */
    public static UIMenuItem getHelpMenuItem()
    {
        UIMenuItem ret = null;
        try {
            final String cmdStr = Configuration.getAttribute(ConfigAttribute.HELPEDITCMD);
            final Command cmd = UUIDUtil.isUUID(cmdStr) ? Command.get(UUID.fromString(cmdStr)) : Command.get(cmdStr);
            ret = new UIMenuItem(cmd.getUUID());
        } catch (final EFapsBaseException e) {
            HelpUtil.LOG.error("ClassNotFoundException", e);
        }
        return ret;
    }
}
