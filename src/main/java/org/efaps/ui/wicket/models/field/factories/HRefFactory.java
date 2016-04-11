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

package org.efaps.ui.wicket.models.field.factories;

import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IComponentRequestHandler;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.Menu;
import org.efaps.ui.wicket.components.links.ContentContainerLink;
import org.efaps.ui.wicket.components.links.IconMenuContentAjaxLink;
import org.efaps.ui.wicket.components.links.MenuContentAjaxLink;
import org.efaps.ui.wicket.components.split.header.RecentLink;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public class HRefFactory
    implements IComponentFactory
// CHECKSTYLE:ON
{

    /**
     * Factory Instance.
     */
    private static HRefFactory FACTORY;

    /**
     * Singelton.
     */
    private HRefFactory()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        // never ever do a link when it is editable
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getHidden(final String _wicketId,
                               final AbstractUIField _abstractUIField)
        throws EFapsException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getReadOnly(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            String icon = _uiField.getFieldConfiguration().getField().getIcon();
            if (icon == null && _uiField.getInstance() != null
                            && _uiField.getFieldConfiguration().getField().isShowTypeIcon()
                            && _uiField.getInstance().getType() != null) {
                final Image image = Image.getTypeIcon(_uiField.getInstance().getType());
                if (image != null) {
                    icon = image.getUrl();
                }
            }
            String content = null;
            for (final IComponentFactory factory : _uiField.getFactories().values()) {
                if (factory instanceof AbstractUIFactory) {
                    final AbstractUIFactory uiFactory = (AbstractUIFactory) factory;
                    if (uiFactory.applies(_uiField)) {
                        content = uiFactory.getReadOnlyValue(_uiField);
                        break;
                    }
                }
            }
            // evaluate which kind of link must be done
            Class<? extends IRequestablePage> clazz = null;
            if (RequestCycle.get().getActiveRequestHandler() instanceof IPageRequestHandler) {
                clazz = ((IPageRequestHandler) RequestCycle.get().getActiveRequestHandler()).getPageClass();
            }
            boolean ajax = clazz != null && ContentContainerPage.class.isAssignableFrom(clazz);
            if (ajax && RequestCycle.get().getActiveRequestHandler() instanceof IComponentRequestHandler) {
                ajax = ajax && !(((IComponentRequestHandler) RequestCycle.get().getActiveRequestHandler())
                                .getComponent() instanceof RecentLink);
            }
            if (icon == null) {
                if (ajax) {
                    ret = new MenuContentAjaxLink(_wicketId, Model.of(_uiField), content);
                } else {
                    ret = new ContentContainerLink(_wicketId, Model.of(_uiField), content);
                }
            } else {
                ret = new IconMenuContentAjaxLink(_wicketId, Model.of(_uiField), content, icon);
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
        return HRefFactory.class.getName();
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
     * {@inheritDoc}
     */
    @Override
    public String getStringValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        String ret = "";
        for (final IComponentFactory factory : _uiField.getFactories().values()) {
            if (factory instanceof AbstractUIFactory) {
                final AbstractUIFactory uiFactory = (AbstractUIFactory) factory;
                if (uiFactory.applies(_uiField)) {
                    ret = uiFactory.getStringValue(_uiField);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applies(final AbstractUIField _uiField)
        throws EFapsException
    {
        return _uiField.getParent().isViewMode()
                        && _uiField.getFieldConfiguration().getField().getReference() != null
                        && _uiField.getInstanceKey() != null && hasAccess2Menu(_uiField);
    }

    /**
     * Checks for access to menu.
     *
     * @param _uiField the ui field
     * @return true, if successful
     * @throws EFapsException on error
     */
    private boolean hasAccess2Menu(final AbstractUIField _uiField)
        throws EFapsException
    {
        final Menu menu = Menu.getTypeTreeMenu(_uiField.getInstance().getType());
        return menu != null && menu.hasAccess(_uiField.getParent().getMode(), _uiField.getInstance())
                        && (!((AbstractUIPageObject) _uiField.getParent()).getAccessMap().containsKey(
                                        _uiField.getInstance())
                        || ((AbstractUIPageObject) _uiField.getParent()).getAccessMap().containsKey(
                                        _uiField.getInstance())
                                        && ((AbstractUIPageObject) _uiField.getParent()).getAccessMap().get(
                                                        _uiField.getInstance()));
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (HRefFactory.FACTORY == null) {
            HRefFactory.FACTORY = new HRefFactory();
        }
        return HRefFactory.FACTORY;
    }
}
