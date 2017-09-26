/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.ui.wicket.models.field.factories;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.PageReference;
import org.apache.wicket.core.request.handler.IComponentRequestHandler;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.Menu;
import org.efaps.api.ui.HRef;
import org.efaps.ui.wicket.components.links.CheckOutLink;
import org.efaps.ui.wicket.components.links.ContentContainerLink;
import org.efaps.ui.wicket.components.links.IconCheckOutLink;
import org.efaps.ui.wicket.components.links.IconContentContainerLink;
import org.efaps.ui.wicket.components.links.IconLoadInTargetAjaxLink;
import org.efaps.ui.wicket.components.links.IconMenuContentLink;
import org.efaps.ui.wicket.components.links.LoadInTargetAjaxLink;
import org.efaps.ui.wicket.components.links.LoadInTargetAjaxLink.ScriptTarget;
import org.efaps.ui.wicket.components.links.MenuContentLink;
import org.efaps.ui.wicket.components.split.header.RecentLink;
import org.efaps.ui.wicket.components.values.LabelField;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.models.objects.IPageObject;
import org.efaps.ui.wicket.models.objects.IWizardElement;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@SuppressWarnings("checkstyle:abstractclassname")
public final class HRefFactory
    implements IComponentFactory
{

    /**
     * Factory Instance.
     */
    private static HRefFactory FACTORY;

    /**
     * Singelton.
     */
    private HRefFactory()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getEditable(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        // never ever do a link when it is editable
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getHidden(final String _wicketId,
                               final AbstractUIField _uiField)
        throws EFapsException
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getReadOnly(final String _wicketId,
                                 final AbstractUIField _uiField)
        throws EFapsException
    {
        Component ret = null;
        if (applies(_uiField)) {
            String icon = _uiField.getFieldConfiguration().getField().getIcon();
            if (icon == null && _uiField.getInstance() != null
                            && _uiField.getFieldConfiguration().getField().isShowTypeIcon()
                            && _uiField.getInstance().getType() != null) {
                final Image image = Image.getTypeIcon(_uiField.getInstance().getType());
                if (image != null) {
                    icon = image.getName();
                }
            }
            String content = null;
            for (final IComponentFactory factory : _uiField.getFactories().values()) {
                if (factory instanceof AbstractUIFactory) {
                    final AbstractUIFactory uiFactory = (AbstractUIFactory) factory;
                    if (uiFactory.applies(_uiField)) {
                        content = uiFactory.getReadOnlyValue(_uiField);
                        break;
                    }
                }
            }
            if (StringUtils.isEmpty(content)) {
                if (isCheckOut(_uiField) && icon != null) {
                    ret = new IconCheckOutLink(_wicketId, Model.of(_uiField), content, icon);
                } else {
                    ret = new LabelField(_wicketId, Model.of(""), _uiField);
                }
            } else if (isCheckOut(_uiField)) {
                if (icon == null) {
                    ret = new CheckOutLink(_wicketId, Model.of(_uiField), content);
                } else {
                    ret = new IconCheckOutLink(_wicketId, Model.of(_uiField), content, icon);
                }
            } else {
                // evaluate which kind of link must be done
                IRequestablePage page = null;
                IRequestablePage calledByPage = null;
                if (RequestCycle.get().getActiveRequestHandler() instanceof IPageRequestHandler) {
                    page = ((IPageRequestHandler) RequestCycle.get().getActiveRequestHandler()).getPage();
                    if (page != null && page instanceof AbstractContentPage) {
                        final PageReference pageRef = ((AbstractContentPage) page).getCalledByPageReference();
                        if (pageRef != null) {
                            calledByPage = pageRef.getPage();
                        }
                    }
                }

                // first priority is the pagePosition than..
                boolean ajax = page != null
                                && ((Component) page).getDefaultModelObject() instanceof IPageObject
                                && PagePosition.TREE.equals(((IPageObject) ((Component) page).getDefaultModelObject())
                                                .getPagePosition());
                if (!ajax) {
                    ajax = _uiField.getParent() != null
                                && _uiField.getParent() instanceof IPageObject
                                && PagePosition.TREE.equals(((IPageObject) _uiField.getParent()).getPagePosition());
                }

                if (!ajax) {
                    // ajax if the page or the reference is a ContentContainerPage,
                //  or the table was called as part of a WizardCall meaning connect is done
                //  or it was opened after a form in modal mode
                    ajax = page != null && (page instanceof ContentContainerPage
                                || calledByPage instanceof ContentContainerPage)
                                || page instanceof TablePage
                                        && ((AbstractUIPageObject) ((Component) page).getDefaultModelObject())
                                                .isPartOfWizardCall()
                        // form opened modal over a form
                        || page instanceof FormPage && calledByPage instanceof FormPage
                            && Target.MODAL.equals(((UIForm) ((Component) page).getDefaultModelObject()).getTarget())
                        // form opened modal over a table that itself is part of a contentcontainer
                        || page instanceof FormPage
                            && Target.MODAL.equals(((UIForm) ((Component) page).getDefaultModelObject()).getTarget())
                            && calledByPage instanceof TablePage
                            && ((TablePage) calledByPage).getCalledByPageReference() != null
                            && !(((TablePage) calledByPage).getCalledByPageReference().getPage() instanceof MainPage);
                }
                // verify ajax by checking if is not a recent link
                if (ajax && RequestCycle.get().getActiveRequestHandler() instanceof IComponentRequestHandler) {
                    ajax = ajax && !(((IComponentRequestHandler) RequestCycle.get().getActiveRequestHandler())
                                    .getComponent() instanceof RecentLink);
                }

                // check if for searchmode the page is in an pop up window
                boolean isInPopUp = false;
                if (_uiField.getParent().isSearchMode()) {
                    if (((IWizardElement) _uiField.getParent()).isWizardCall()) {
                        final IWizardElement pageObj = ((IWizardElement) _uiField
                                        .getParent()).getUIWizardObject().getWizardElement().get(0);
                        if (pageObj instanceof IPageObject) {
                            isInPopUp = PagePosition.POPUP.equals(((IPageObject) pageObj).getPagePosition());
                        } else {
                            isInPopUp = Target.POPUP.equals(((ICmdUIObject) pageObj).getCommand().getTarget());
                        }
                    }
                }

                if (icon == null) {
                    if (ajax) {
                        // checking if is a link of a structurbrowser browserfield
                        if (page instanceof StructurBrowserPage
                                        && ((UIStructurBrowser) _uiField.getParent()).isBrowserField(_uiField)) {
                            ret = new LoadInTargetAjaxLink(_wicketId, Model.of(_uiField), content, ScriptTarget.TOP);
                        } else {
                            ret = new MenuContentLink(_wicketId, Model.of(_uiField), content);
                        }
                    } else if (isInPopUp) {
                        ret = new LoadInTargetAjaxLink(_wicketId, Model.of(_uiField), content, ScriptTarget.OPENER);
                    } else {
                        ret = new ContentContainerLink(_wicketId, Model.of(_uiField), content);
                    }
                } else {
                    if (ajax) {
                        ret = new IconMenuContentLink(_wicketId, Model.of(_uiField), content, icon);
                    } else if (isInPopUp) {
                        ret = new IconLoadInTargetAjaxLink(_wicketId, Model.of(_uiField), content,
                                        ScriptTarget.OPENER, icon);
                    } else {
                        ret = new IconContentContainerLink(_wicketId, Model.of(_uiField), content, icon);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return HRefFactory.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPickListValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        String ret = null;
        for (final IComponentFactory factory : _uiField.getFactories().values()) {
            if (factory instanceof AbstractUIFactory) {
                final AbstractUIFactory uiFactory = (AbstractUIFactory) factory;
                if (uiFactory.applies(_uiField)) {
                    ret = uiFactory.getPickListValue(_uiField);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparable<?> getCompareValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        Comparable<?> ret = null;
        for (final IComponentFactory factory : _uiField.getFactories().values()) {
            if (factory instanceof AbstractUIFactory) {
                final AbstractUIFactory uiFactory = (AbstractUIFactory) factory;
                if (uiFactory.applies(_uiField)) {
                    ret = uiFactory.getCompareValue(_uiField);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue(final AbstractUIField _uiField)
        throws EFapsException
    {
        String ret = "";
        for (final IComponentFactory factory : _uiField.getFactories().values()) {
            if (factory instanceof AbstractUIFactory) {
                final AbstractUIFactory uiFactory = (AbstractUIFactory) factory;
                if (uiFactory.applies(_uiField)) {
                    ret = uiFactory.getStringValue(_uiField);
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean applies(final AbstractUIField _uiField)
        throws EFapsException
    {
        return (_uiField.getParent().isViewMode() || _uiField.getParent().isSearchMode())
                        && _uiField.getFieldConfiguration().getField().getReference() != null
                        && _uiField.getInstanceKey() != null && (isCheckOut(_uiField) || hasAccess2Menu(_uiField));
    }

    /**
     * Checks if is a check out.
     *
     * @param _uiField the _ui field
     * @return true, if is check out
     */
    private boolean isCheckOut(final AbstractUIField _uiField)
    {
        return StringUtils.containsIgnoreCase(_uiField.getFieldConfiguration().getField().getReference(),
                        HRef.CHECKOUT.toString());
    }

    /**
     * Checks for access to menu.
     *
     * @param _uiField the ui field
     * @return true, if successful
     * @throws EFapsException on error
     */
    private boolean hasAccess2Menu(final AbstractUIField _uiField)
        throws EFapsException
    {
        final Menu menu = Menu.getTypeTreeMenu(_uiField.getInstance().getType());
        return menu != null && menu.hasAccess(_uiField.getParent().getMode(), _uiField.getInstance())
                        && (!((AbstractUIPageObject) _uiField.getParent()).getAccessMap().containsKey(
                                        _uiField.getInstance())
                        || ((AbstractUIPageObject) _uiField.getParent()).getAccessMap().containsKey(
                                        _uiField.getInstance())
                                        && ((AbstractUIPageObject) _uiField.getParent()).getAccessMap().get(
                                                        _uiField.getInstance()));
    }

    /**
     * @return IComponentFactory instance
     */
    public static IComponentFactory get()
    {
        if (HRefFactory.FACTORY == null) {
            HRefFactory.FACTORY = new HRefFactory();
        }
        return HRefFactory.FACTORY;
    }
}
