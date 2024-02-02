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
package org.efaps.ui.wicket.components.help;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.api.ui.IHelpProvider;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.menu.ajax.AbstractItemBehavior;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowAjaxPageCreator;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.IPageObject;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class HelpLink.
 *
 * @author The eFaps Team
 */
public class HelpLink
    extends WebMarkupContainer
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(HelpLink.class);

    /**
     * Instantiates a new help link.
     *
     * @param _wicketID the wicket ID
     * @param _model the model
     */
    public HelpLink(final String _wicketID,
                    final IModel<Long> _model)
    {
        super(_wicketID, _model);
        add(AttributeModifier.append("class", "eFapsHelpLink"));
        final boolean hasHelp = HelpUtil.hasHelp(_model.getObject());
        if (!hasHelp) {
            add(AttributeModifier.append("class", "eFapsNoHelp"));
        }
        if (HelpUtil.isHelpAdmin() && HelpUtil.isEditMode()) {
            add(new OpenEditModalBehavior());
        } else {
            add(AttributeModifier.append("onclick", "top.eFaps.help('" + _model.getObject() + "')"));
        }
        setVisible(hasHelp || HelpUtil.isHelpAdmin());
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        replaceComponentTagBody(_markupStream, _tag, DBProperties.getProperty(
                        "org.efaps.ui.wicket.pages.content.AbstractContentPage.HelpLink"));
    }

    /**
     * The Class OpenEditModalBehavior.
     */
    public class OpenEditModalBehavior
        extends AbstractItemBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public OpenEditModalBehavior()
        {
            super("click");
        }

        /**
         * Show the modal window.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            final ModalWindowContainer modal;
            final PagePosition pagePosition;
            if (getPage() instanceof MainPage) {
                modal = ((MainPage) getPage()).getModal();
                pagePosition = PagePosition.CONTENTMODAL;
            } else {
                modal = ((AbstractContentPage) getPage()).getModal();
                if (getPage().getDefaultModelObject() instanceof IPageObject) {
                    switch (((IPageObject) getPage().getDefaultModelObject()).getPagePosition()) {
                        case TREE:
                            pagePosition = PagePosition.TREEMODAL;
                            break;
                        case CONTENT:
                        default:
                            pagePosition = PagePosition.CONTENTMODAL;
                            break;
                    }
                } else {
                    pagePosition = PagePosition.TREEMODAL;
                }
            }
            try {
                Context.getThreadContext().setSessionAttribute(IHelpProvider.class.getName() + ".CmdId",
                                getComponent().getDefaultModelObject());
            } catch (final EFapsException e) {
                HelpLink.LOG.error("EFapsException", e);
            }
            modal.reset();
            final UIMenuItem menuItem = HelpUtil.getHelpMenuItem();
            final ModalWindowAjaxPageCreator pageCreator = new ModalWindowAjaxPageCreator(menuItem, modal,
                            pagePosition);
            modal.setPageCreator(pageCreator);
            modal.setInitialHeight(menuItem.getWindowHeight());
            modal.setInitialWidth(menuItem.getWindowWidth());
            modal.show(_target);
        }
    }
}
