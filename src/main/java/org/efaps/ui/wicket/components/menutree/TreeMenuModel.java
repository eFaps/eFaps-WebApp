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

package org.efaps.ui.wicket.components.menutree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TreeMenuModel
    implements ITreeProvider<UIMenuItem>
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Root items.
     */
    private List<UIMenuItem> rootItems = new ArrayList<UIMenuItem>();

    /**
     * @param _commandUUID UUID of the command/menu
     * @param _oid          oid of the instance
     * @throws CacheReloadException on error
     */
    public TreeMenuModel(final UUID _commandUUID,
                         final String _oid)
        throws CacheReloadException
    {
        setModel(_commandUUID, _oid);
    }

    /**
     * @param _commandUUID UUID of the command/menu
     * @param _oid          oid of the instance
     * @throws CacheReloadException on error
     */
    public void setModel(final UUID _commandUUID,
                         final String _oid)
        throws CacheReloadException
    {
        this.rootItems.clear();
        final UIMenuItem model = new UIMenuItem(_commandUUID, _oid);
        this.rootItems.add(model);
    }

    /**
     * Setter method for instance variable {@link #roots}.
     *
     * @param _rootItems value for instance variable {@link #roots}
     */
    public void setRootItems(final List<UIMenuItem> _rootItems)
    {
        this.rootItems = _rootItems;
    }

    /**
     * Getter method for the instance variable {@link #rootItems}.
     *
     * @return value of instance variable {@link #rootItems}
     */
    public List<UIMenuItem> getRootItems()
    {
        return this.rootItems;
    }

    @Override
    public void detach()
    {
        // We do nothing here
    }

    @Override
    public Iterator<? extends UIMenuItem> getRoots()
    {
        return this.rootItems.iterator();
    }

    @Override
    public boolean hasChildren(final UIMenuItem _menuItem)
    {
        return _menuItem.hasChildren();
    }

    @Override
    public Iterator<? extends UIMenuItem> getChildren(final UIMenuItem _node)
    {
        return _node.getChildren().iterator();
    }

    @Override
    public IModel<UIMenuItem> model(final UIMenuItem _object)
    {
        return Model.of(_object);
    }
}
