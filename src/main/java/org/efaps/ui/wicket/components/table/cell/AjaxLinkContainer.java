/*
 * Copyright 2003 - 2012 The eFaps Team
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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.table.cell;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.AbstractAjaxCallBackBehavior;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * Class extends a Link to work inside a content container. Used also by the
 * StructurBrowserTable.
 * @author The eFaps Team
 * @version $Id:AjaxLinkContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxLinkContainer
    extends WebMarkupContainer
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     * @param _model model for thid component
     */
    public AjaxLinkContainer(final String _wicketId,
                             final IModel<?> _model)
    {
        super(_wicketId, _model);
        this.add(new AjaxSelfCallBackBehavior());
        this.add(new AjaxParentCallBackBehavior());
    }

    /**
     * The tag must be overwritten.
     *
     * @param _tag tag to write.
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        _tag.put("href", "#");
    }

    /**
     * Class is used to call an event from inside the parent.
     *
     */
    public class AjaxParentCallBackBehavior
        extends AbstractAjaxCallBackBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public AjaxParentCallBackBehavior()
        {
            super("onmouseup", Target.PARENT);
        }

        /**
         * Method is executed on mouseup.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final UITableCell cellmodel = (UITableCell) super.getComponent().getDefaultModelObject();
            Instance instance = null;
            if (cellmodel.getInstanceKey() != null) {

                Menu menu = null;
                try {
                    instance = cellmodel.getInstance();
                    menu = Menu.getTypeTreeMenu(instance.getType());
                  //CHECKSTYLE:OFF
                } catch (final Exception e) {
                    throw new RestartResponseException(new ErrorPage(e));
                } //CHECKSTYLE:ON
                if (menu == null) {
                    final Exception ex = new Exception("no tree menu defined for type " + instance.getType().getName());
                    throw new RestartResponseException(new ErrorPage(ex));
                }

                final String listMenuKey = ((AbstractContentPage) getComponent().getPage()).getMenuTreeKey();
                final MenuTree menutree = (MenuTree) ((EFapsSession) getComponent().getSession())
                                .getFromCache(listMenuKey);

                menutree.addChildMenu(menu.getUUID(), cellmodel.getInstanceKey(), _target);
            }
        }
    }

    /**
     * Class is used to call an event from inside istself.
     *
     */
    public class AjaxSelfCallBackBehavior
        extends AjaxEventBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public AjaxSelfCallBackBehavior()
        {
            super("onClick");
        }

        /**
         * Method is executed on click.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final UITableCell cellmodel = (UITableCell) super.getComponent().getDefaultModelObject();
            Instance instance = null;
            if (cellmodel.getInstanceKey() != null) {
                AbstractCommand menu = null;
                try {
                    instance = cellmodel.getInstance();
                    menu = Menu.getTypeTreeMenu(instance.getType());
                //CHECKSTYLE:OFF
                } catch (final Exception e) {
                    throw new RestartResponseException(new ErrorPage(e));
                } //CHECKSTYLE:ON
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

                Page page;
                try {
                    if (menu.getTargetTable() != null) {
                        if (menu.getTargetStructurBrowserField() == null) {
                            page = new TablePage(menu.getUUID(), cellmodel.getInstanceKey(), true)
                                        .setMenuTreeKey(((AbstractContentPage) getComponent().getPage())
                                                        .getMenuTreeKey());
                        } else {
                            page = new StructurBrowserPage(menu.getUUID(), cellmodel.getInstanceKey(), true)
                                            .setMenuTreeKey(((AbstractContentPage) getComponent().getPage())
                                                            .getMenuTreeKey());
                        }
                    } else {
                        page = new FormPage(menu.getUUID(),
                                        cellmodel.getInstanceKey(), true)
                                        .setMenuTreeKey(((AbstractContentPage) getComponent().getPage())
                                                        .getMenuTreeKey());
                    }
                } catch (final EFapsException e) {
                    page = new ErrorPage(e);
                }

                super.getComponent().getRequestCycle().setResponsePage(page);
            }
        }
    }
}
