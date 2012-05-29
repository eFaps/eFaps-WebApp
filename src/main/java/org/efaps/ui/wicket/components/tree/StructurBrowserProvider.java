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


package org.efaps.ui.wicket.components.tree;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableTreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StructurBrowserProvider
extends SortableTreeProvider<UIStructurBrowser>
{

    /* (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#getRoots()
     */
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final List<UIStructurBrowser> roots;

    /**
     * @param _model
     */
    public StructurBrowserProvider(final IModel<UIStructurBrowser> _model)
    {
       this.roots = _model.getObject().getChilds();

    }

    @Override
    public Iterator<? extends UIStructurBrowser> getRoots()
    {
        return this.roots.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(final UIStructurBrowser _node)
    {
        return _node.isParent() || _node.hasChilds();
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#getChildren(java.lang.Object)
     */
    @Override
    public Iterator<? extends UIStructurBrowser> getChildren(final UIStructurBrowser _node)
    {
        if (_node.isParent() && !_node.hasChilds()) {
            _node.addChildren();
        }
        return _node.getChilds().iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider#model(java.lang.Object)
     */
    @Override
    public IModel<UIStructurBrowser> model(final UIStructurBrowser _object)
    {
        return Model.of(_object);
    }

}
