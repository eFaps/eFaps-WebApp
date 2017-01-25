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

package org.efaps.ui.wicket.components.menu;

import java.io.File;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.IRecent;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsException;

/**
 * Class renders a standard link for the menu.
 *
 * @author The eFaps Team
 */
public class LinkItem
    extends Link<UIMenuItem>
    implements IRecent
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     */
    public LinkItem(final String _wicketId,
                    final IModel<UIMenuItem> _model)
    {
        super(_wicketId, _model);
    }

    /**
     * On click it is evaluated what must be responded.
     */
    @Override
    public void onClick()
    {
        ((EFapsSession) getSession()).addRecent(this);
        final UIMenuItem model = super.getModelObject();
        try {
            final AbstractCommand command = model.getCommand();
            final PagePosition pagePosition = isPopup() ? PagePosition.POPUP : PagePosition.CONTENT;

            if (command.getTargetTable() != null) {
                final WebPage page;
                if (command.getTargetStructurBrowserField() != null) {
                    page = new StructurBrowserPage(model.getCommandUUID(), model.getInstanceKey(), getPage()
                                    .getPageReference());
                } else {
                    if ("GridX".equals(Configuration.getAttribute(ConfigAttribute.TABLEDEFAULTTYPECONTENT))) {
                        page = new GridPage(Model.of(UIGrid.get(command.getUUID(), pagePosition)));
                    } else {
                        final UITable uiTable = new UITable(model.getCommandUUID(), model.getInstanceKey())
                                        .setPagePosition(pagePosition);
                        page = new TablePage(Model.of(uiTable), null, getPage().getPageReference());
                    }
                }
                setResponsePage(page);
            } else if (command.getTargetForm() != null || command.getTargetSearch() != null) {
                final UIForm uiForm = new UIForm(model.getCommandUUID(), model.getInstanceKey()).setPagePosition(
                                pagePosition);
                final FormPage page = new FormPage(Model.of(uiForm), getPage().getPageReference());
                setResponsePage(page);
            } else {
                try {
                    final List<Return> rets = model.executeEvents(ParameterValues.OTHERS, this);
                    if (command.isTargetShowFile()) {
                        final Object object = rets.get(0).get(ReturnValues.VALUES);
                        if (object instanceof File) {
                            getRequestCycle().scheduleRequestHandlerAfterCurrent(new RedirectRequestHandler(
                                            "/usage.html"));
                        }
                    }
                } catch (final EFapsException e) {
                    throw new RestartResponseException(new ErrorPage(e));
                }
            }
        } catch (final EFapsException e) {
            setResponsePage(new ErrorPage(e));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(final Component _openComponent)
        throws EFapsException
    {
        final UIMenuItem model = super.getModelObject();

        final AbstractCommand command = model.getCommand();
        _openComponent.getPage();
        if (command.getTargetTable() != null) {
            if (command.getTargetStructurBrowserField() != null) {
                final StructurBrowserPage page = new StructurBrowserPage(model.getCommandUUID(), model
                                .getInstanceKey());
                setResponsePage(page);
            } else {
                if ("GridX".equals(Configuration.getAttribute(ConfigAttribute.TABLEDEFAULTTYPECONTENT))) {
                    final GridPage page = new GridPage(Model.of(UIGrid.get(command.getUUID(), PagePosition.CONTENT)));
                    setResponsePage(page);
                } else {
                    final TablePage page = new TablePage(model.getCommandUUID(), model.getInstanceKey());
                    setResponsePage(page);
                }
            }
        } else if (command.getTargetForm() != null) {
            final UIForm uiForm = new UIForm(model.getCommandUUID(), model.getInstanceKey()).setPagePosition(
                            PagePosition.CONTENT);
            final FormPage page = new FormPage(Model.of(uiForm));
            setResponsePage(page);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabel(final int _maxLength)
        throws EFapsException
    {
        final UIMenuItem model = super.getModelObject();
        String label = model.getLabel();
        if (label.length() > _maxLength) {
            label = label.substring(0, _maxLength - 3) + "...";
        }
        return label;
    }

    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        super.onComponentTagBody(_markupStream, _openTag);
        final StringBuilder html = new StringBuilder();
        if (getModelObject().getImage() == null) {
            html.append("<div class=\"eFapsMenuImagePlaceHolder\">").append("&nbsp;</div>");
        } else {
            html.append("<img src=\"").append(EFapsContentReference.getImageURL(getModelObject().getImage())).append(
                            "\" class=\"eFapsMenuImage\"/>");
        }
        html.append("<span class=\"eFapsMenuLabel\">").append(getModelObject().getLabel()).append("</span>");
        replaceComponentTagBody(_markupStream, _openTag, html);
    }

    @Override
    protected CharSequence getOnClickScript(final CharSequence _url)
    {
        final StringBuilder js = new StringBuilder()
                        .append("registry.byId(\"").append("mainPanel").append(
                                        "\").set(\"content\", domConstruct.create(\"iframe\", {").append("\"id\": \"")
                        .append(MainPage.IFRAME_ID).append("\",\"src\": \"").append(_url).append(
                                        "\",\"style\": \"border: 0; width: 100%; height: 99%\"").append(",\"id\": \"")
                        .append(MainPage.IFRAME_ID).append("\"").append("}));");
        return DojoWrapper.require(js, DojoClasses.registry, DojoClasses.domConstruct);
    }

    /**
     * Checks if is popup.
     *
     * @return true, if is popup
     */
    protected boolean isPopup()
    {
        return false;
    }
}
