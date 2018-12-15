/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.ui.wicket.models.objects.grid;

import java.util.ArrayList;
import java.util.Collection;

import org.efaps.api.ui.ITree;
import org.efaps.db.Instance;

public class GridTree
    implements ITree<Instance>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private Instance node;

    private final Collection<ITree<Instance>> children = new ArrayList<>();

    @Override
    public Instance getNode()
    {
        return this.node;
    }

    @Override
    public Collection<ITree<Instance>> getChildren()
    {
        return this.children;
    }

    public static ITree<Instance> get(final Collection<Instance> _result)
    {
        final GridTree ret = new GridTree();
        _result.forEach(_inst -> ret.children.add(of(_inst)));
        return ret;
    }

    public static GridTree of(final Instance _instance)
    {
        final GridTree ret = new GridTree();
        ret.node = _instance;
        return ret;
    }
}
