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
package org.efaps.ui.wicket.components.menu.behaviors;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.util.string.StringValue;
import org.efaps.ui.wicket.components.menu.SlideIn;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowAjaxPageCreator;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.main.MainPage;

/**
 * The Class OpenModalBehavior.
 */
public class OpenModalBehavior
    extends AjaxEventBehavior
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new open modal behavior.
     */
    public OpenModalBehavior()
    {
        super("click");
    }

    @Override
    protected void onEvent(final AjaxRequestTarget _target)
    {
        final StringValue key = getComponent().getRequestCycle().getRequest().getQueryParameters()
                        .getParameterValue("m");
        getComponent().getParent().visitChildren(SlideIn.class, (_slideIn,
                                                                 _visit) -> {
            final UIMenuItem menuItem = ((SlideIn) _slideIn).getMenuItems().get(key.toString());
            final ModalWindowContainer modal = ((MainPage) getComponent().getPage()).getModal();
            modal.reset();
            final ModalWindowAjaxPageCreator pageCreator = new ModalWindowAjaxPageCreator(menuItem, modal,
                            PagePosition.CONTENTMODAL);
            modal.setPageCreator(pageCreator);
            modal.show(_target);
        });
    }
}
