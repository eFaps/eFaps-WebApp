/*
 * Copyright 2003 - 2011 The eFaps Team
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

    private List<UIMenuItem> rootItems = new ArrayList<UIMenuItem>();


    /**
     * @param _commandUUID
     * @param _oid
     */
    public TreeMenuModel(final UUID _commandUUID,
                         final String _oid)
    {
        setModel(_commandUUID, _oid);
    }

    public void setModel(final UUID _commandUUID, final String _oid)
    {
        this.rootItems.clear();
        final UIMenuItem model = new UIMenuItem(_commandUUID, _oid);
        this.rootItems.add(model);
    }


    /**
     * Setter method for instance variable {@link #roots}.
     *
     * @param _roots value for instance variable {@link #roots}
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

    /* (non-Javadoc)
     * @see org.apache.wicket.model.IDetachable#detach()
     */
    @Override
    public void detach()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#getRoots()
     */
    @Override
    public Iterator<? extends UIMenuItem> getRoots()
    {
        return this.rootItems.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(final UIMenuItem _menuItem)
    {
        return _menuItem.hasChilds();
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#getChildren(java.lang.Object)
     */
    @Override
    public Iterator<? extends UIMenuItem> getChildren(final UIMenuItem _node)
    {
        return _node.getChilds().iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#model(java.lang.Object)
     */
    @Override
    public IModel<UIMenuItem> model(final UIMenuItem _object)
    {
        return Model.of(_object);
    }
}
