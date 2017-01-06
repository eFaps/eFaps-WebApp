/*
 * Copyright 2003 - 2015 The eFaps Team
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

package org.efaps.ui.wicket.components.embeddedlink;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.behaviors.update.AbstractRemoteUpdateListenerBehavior;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.menutree.MenuUpdateBehavior;
import org.efaps.ui.wicket.models.EmbeddedLink;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class LinkElementComponent
    extends WebMarkupContainer
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id of the component
     * @param _embededLink link the component is representing
     */
    public LinkElementComponent(final String _wicketId,
                                final EmbeddedLink _embededLink)
    {
        super(_wicketId, Model.of(_embededLink));
        setMarkupId(_embededLink.getId());
        this.add(new AjaxMenuContentBehavior());
        this.add(new UpdateMenuBehavior(this));
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        // nothing to add, because only the javascript added is wanted
    }

    /**
     * Behavior to update the menu.
     */
    public class UpdateMenuBehavior
        extends AbstractRemoteUpdateListenerBehavior
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _component Component the behavior belongs to
         */
        public UpdateMenuBehavior(final Component _component)
        {
            super(_component);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onEvent(final Component _component,
                            final AjaxRequestTarget _target)
        {
            try {
                final EmbeddedLink link = (EmbeddedLink) getComponent().getDefaultModelObject();
                Instance instance = null;
                if (link.getInstanceKey() != null) {
                    Menu menu = null;
                    try {
                        instance = Instance.get(link.getInstanceKey());
                        menu = Menu.getTypeTreeMenu(instance.getType());
                        // CHECKSTYLE:OFF
                    } catch (final Exception e) {
                        throw new RestartResponseException(new ErrorPage(e));
                    } // CHECKSTYLE:ON
                    if (menu == null) {
                        final Exception ex = new Exception("no tree menu defined for type "
                                        + instance.getType().getName());
                        throw new RestartResponseException(new ErrorPage(ex));
                    }
                    final Page page = getPage();
                    if (page instanceof AbstractContentPage) {
                        final MenuTree menutree = (MenuTree) _component;
                        menutree.addChildMenu(menu.getUUID(), link.getInstanceKey(), _target);
                    }
                }
            } catch (final CacheReloadException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
        }
    }

    /**
     * Class is used to call an event from inside istself.
     *
     */
    public class AjaxMenuContentBehavior
        extends AjaxEventBehavior
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         */
        public AjaxMenuContentBehavior()
        {
            super("click");
        }

        @Override
        protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
        {
            super.updateAjaxAttributes(_attributes);

            final PageReference calledByPageRef = ((AbstractContentPage) getPage()).getCalledByPageReference();
            if (calledByPageRef != null) {
                final UpdateMenuBehavior up = getComponent().getBehaviors(UpdateMenuBehavior.class).get(0);
                ((ContentContainerPage) calledByPageRef.getPage()).getMenuTree().registerListener(up);
                final AjaxCallListener listener = new AjaxCallListener();
                final StringBuilder js = new StringBuilder()
                    .append("var frameWin = top.dojo.doc.getElementById(\"").append(MainPage.IFRAME_ID)
                    .append("\").contentWindow;")
                    .append(" frameWin.")
                    .append(MenuUpdateBehavior.FUNCTION_NAME).append("(\"").append(up.getKey())
                    .append("\");")
                    .append("return true;");
                listener.onPrecondition(js);
                _attributes.getAjaxCallListeners().add(listener);
            }
        }

        /**
         * Method is executed on click.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final EmbeddedLink link = (EmbeddedLink) getComponent().getDefaultModelObject();
            Instance instance = null;
            if (link.getInstanceKey() != null) {
                AbstractCommand menu = null;
                try {
                    instance = Instance.get(link.getInstanceKey());
                    menu = Menu.getTypeTreeMenu(instance.getType());
                    // CHECKSTYLE:OFF
                } catch (final Exception e) {
                    throw new RestartResponseException(new ErrorPage(e));
                } // CHECKSTYLE:ON
                if (menu == null) {
                    final Exception ex = new Exception("no tree menu defined for type " + instance.getType().getName());
                    throw new RestartResponseException(new ErrorPage(ex));
                }

                for (final AbstractCommand childcmd : ((Menu) menu).getCommands()) {
                    if (childcmd.isDefaultSelected()) {
                        menu = childcmd;
                        break;
                    }
                }

                final PageReference calledByPageRef = ((AbstractContentPage) getPage()).getCalledByPageReference();
                if (calledByPageRef != null) {
                    Page page;
                    try {
                        if (menu.getTargetTable() != null) {
                            if (menu.getTargetStructurBrowserField() == null) {
                                page = new TablePage(menu.getUUID(), link.getInstanceKey(), calledByPageRef);
                            } else {
                                page = new StructurBrowserPage(menu.getUUID(),
                                                link.getInstanceKey(), calledByPageRef);
                            }
                        } else {
                            final UIForm uiForm = new UIForm(menu.getUUID(), link.getInstanceKey());
                            page = new FormPage(Model.of(uiForm), calledByPageRef);
                        }
                    } catch (final EFapsException e) {
                        page = new ErrorPage(e);
                    }
                    super.getComponent().getRequestCycle().setResponsePage(page);
                }
            }
        }
    }
}
