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
package org.efaps.ui.wicket.components.menutree;

import java.util.Iterator;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.behaviors.update.AbstractRemoteUpdateBehavior;
import org.efaps.ui.wicket.behaviors.update.IRemoteUpdateListener;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
/**
 * Behavior used to update the MenuTree remotely.
 */
public class MenuUpdateBehavior
    extends AbstractRemoteUpdateBehavior
{

    /** The Constant ROLE. */
    public static final MetaDataKey<Instance> METAKEY = new MetaDataKey<Instance>() {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;
    };

    /**
     * The Name of the function to be called.
     */
    public static final String FUNCTION_NAME = "eFapsUpdateMenu";

    /**
     * The Name of the function to be called.
     */
    public static final String PARAMETERKEY4UPDATE = "self";

    /**
     * The Name of the function to be called.
     */
    public static final String PARAMETERKEY4INSTANCE = "instance";

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
    @SuppressWarnings("checkstyle:illegalcatch")
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
        } else if (MenuUpdateBehavior.PARAMETERKEY4INSTANCE.equals(key.toString())) {
            final Instance instance = Session.get().getMetaData(MenuUpdateBehavior.METAKEY);
            if (instance != null && instance.isValid()) {
                try {
                    Menu menu = null;
                    try {
                        menu = Menu.getTypeTreeMenu(instance.getType());
                    } catch (final Exception e) {
                        throw new RestartResponseException(new ErrorPage(e));
                    }
                    if (menu == null) {
                        final Exception ex = new Exception("no tree menu defined for type " + instance.getType()
                                        .getName());
                        throw new RestartResponseException(new ErrorPage(ex));
                    }
                    final MenuTree menutree = (MenuTree) getComponent();
                    menutree.addChildMenu(menu.getUUID(), instance.getOid(), _target);
                } catch (final CacheReloadException e) {
                    throw new RestartResponseException(new ErrorPage(e));
                }
            }
        }
    }
}
