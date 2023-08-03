/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.components.menu.behaviors;

import java.io.File;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.menu.SlideIn;
import org.efaps.ui.wicket.components.modalwindow.LegacyModalWindow;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;

/**
 * The Class OpenInOpenerBehavior.
 *
 * @author The eFaps Team
 */
public class ExecBehavior
    extends AjaxEventBehavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public ExecBehavior()
    {
        super("click");
    }

    @Override
    protected void onEvent(final AjaxRequestTarget _target)
    {
        final StringValue key = getComponent().getRequestCycle().getRequest().getQueryParameters().getParameterValue(
                        "m");
        getComponent().getParent().visitChildren(SlideIn.class, (_slideIn,
                                                                 _visit) -> {
            final UIMenuItem menuItem = ((SlideIn) _slideIn).getMenuItems().get(key.toString());
            AbstractCommand command = null;
            try {
                command = menuItem.getCommand();
                if (menuItem.isAskUser()) {
                    final ModalWindowContainer modal;
                    if (super.getComponent().getPage() instanceof MainPage) {
                        modal = ((MainPage) super.getComponent().getPage()).getModal();
                    } else {
                        modal = ((AbstractContentPage) super.getComponent().getPage()).getModal();
                    }
                    modal.setPageCreator(new LegacyModalWindow.PageCreator()
                    {

                        private static final long serialVersionUID = 1L;

                        @Override
                        public Page createPage()
                        {
                            Page page = null;
                            try {
                                page = new DialogPage(getComponent().getPage().getPageReference(), Model.of(menuItem),
                                                null);
                            } catch (final EFapsException e) {
                                page = new ErrorPage(e);
                            }
                            return page;
                        }
                    });
                    modal.setInitialHeight(150);
                    modal.setInitialWidth(350);
                    modal.show(_target);
                } else {
                    final List<Return> rets = menuItem.executeEvents(ParameterValues.OTHERS, this);
                    if (command.isTargetShowFile()) {
                        final Object object = rets.get(0).get(ReturnValues.VALUES);
                        if (object instanceof File) {
                            ((EFapsSession) getComponent().getSession()).setFile((File) object);
                            ((AbstractMergePage) getComponent().getPage()).getDownloadBehavior().initiate(_target);
                        }
                    }
                }
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
            if (command != null && command.isNoUpdateAfterCmd()) {
                getComponent().getRequestCycle().replaceAllRequestHandlers(new EmptyRequestHandler());
            }
        });
    }
}
