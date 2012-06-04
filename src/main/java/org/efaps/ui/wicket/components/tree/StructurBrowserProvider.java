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

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
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
    implements ITreeProvider<UIStructurBrowser>
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * List of root nodes.
     */
    private final List<UIStructurBrowser> roots;

    /**
     * @param _model model
     */
    public StructurBrowserProvider(final IModel<UIStructurBrowser> _model)
    {
        this.roots = _model.getObject().getChildren();
    }

    @Override
    public Iterator<? extends UIStructurBrowser> getRoots()
    {
        return this.roots.iterator();
    }

    /**
     * Does the given object have children - note that this method may
     * return <code>true</code> even if {@link #getChildren(Object)}
     * returns an empty iterator.
     *
     * @param _node the node to check for children
     * @return {@code true} if node has children
     */
    @Override
    public boolean hasChildren(final UIStructurBrowser _node)
    {
        return _node.isParent() || _node.hasChildren();
    }

    /**
     * Get the children of the given node.
     *
     * @param _node node to get children for
     * @return children of node
     */
    @Override
    public Iterator<? extends UIStructurBrowser> getChildren(final UIStructurBrowser _node)
    {
        if (_node.isParent() && !_node.hasChildren()) {
            _node.addChildren();
        }
        return _node.getChildren().iterator();
    }

    /**
     * Callback used by the consumer of this tree provider to wrap objects retrieved from
     * {@link #getRoots()} or {@link #getChildren(Object)} with a model (usually a detachable one).
     * <p>
     * Important note: The model must implement {@link Object#equals(Object)} and
     * {@link Object#hashCode()} !
     *
     * @param _object the object that needs to be wrapped
     *
     * @return the model representation of the object
     */
    @Override
    public IModel<UIStructurBrowser> model(final UIStructurBrowser _object)
    {
        return Model.of(_object);
    }

    /**
     * Detaches model after use. This is generally used to null out transient references that can be
     * re-attached later.
     */
    @Override
    public void detach()
    {
        // not implemented
    }
}
