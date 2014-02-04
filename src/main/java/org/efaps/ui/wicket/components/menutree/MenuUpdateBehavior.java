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

import java.util.Iterator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.efaps.ui.wicket.behaviors.update.AbstractRemoteUpdateBehavior;
import org.efaps.ui.wicket.behaviors.update.IRemoteUpdateListener;
import org.efaps.ui.wicket.models.objects.UIMenuItem;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
/**
 * Behavior used to update the MenuTree remotely.
 */
public class MenuUpdateBehavior
    extends AbstractRemoteUpdateBehavior
{
    /**
     * The Name of the function to be called.
     */
    public static final String FUNCTION_NAME = "eFapsUpdateMenu";

    /**
     * The Name of the function to be called.
     */
    public static final String PARAMETERKEY4UPDATE = "self";


    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public MenuUpdateBehavior()
    {
        super(MenuUpdateBehavior.FUNCTION_NAME);
    }

    @Override
    protected void respond(final AjaxRequestTarget _target)
    {
        super.respond(_target);
        final RequestCycle requestCycle = RequestCycle.get();
        final StringValue key = requestCycle.getRequest().getRequestParameters()
                        .getParameterValue(IRemoteUpdateListener.PARAMETERKEY);
        if (MenuUpdateBehavior.PARAMETERKEY4UPDATE.equals(key.toString())) {
            final MenuTree tree = (MenuTree) getComponent();
            final TreeMenuModel treeModel = (TreeMenuModel) tree.getProvider();
            final Iterator<? extends UIMenuItem> iter = treeModel.getRoots();
            while (iter.hasNext()) {
                final UIMenuItem item = iter.next();
                if (item.requeryLabel()) {
                    tree.updateNode(item, _target);
                }
                for (final UIMenuItem desc : item.getDescendants()) {
                    if (desc.requeryLabel()) {
                        tree.updateNode(desc, _target);
                    }
                }
            }
        }
    }
}
