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
package org.efaps.ui.wicket.pages.content.grid;

import java.util.UUID;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.footer.FooterPanel;
import org.efaps.ui.wicket.components.gridx.GridXPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class GridPage.
 *
 * @author The eFaps Team
 */
public class GridPage
    extends AbstractMergePage
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the style sheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(GridPage.class, "GridPage.css");

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GridPage.class);

    public GridPage(final PageParameters _pageParameters)
    {
        this(Model.of(UIGrid.get(UUID.fromString(_pageParameters.get("id").toString()), PagePosition.CONTENT)), null);
    }

    /**
     * Instantiates a new grid page.
     *
     * @param _model the model
     */
    public GridPage(final IModel<UIGrid> _model)
    {
        this(_model, null);
    }

    /**
     * Instantiates a new grid page.
     *
     * @param _model the model
     * @param _modal the modal window this page is opened in to have a handle on it
     */
    public GridPage(final IModel<UIGrid> _model,
                    final ModalWindowContainer _modal)
    {
        super(_model);
        try {
            add2Page(new Label("pageTitle", DBProperties.getProperty("Logo.Version.Label")));
            add(new Label("titel", _model.getObject().getTitle()));
            add(new ModalWindowContainer("modal"));

            final FormContainer form = new FormContainer("form")
            {

                /** The Constant serialVersionUID. */
                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(final ComponentTag _tag)
                {
                    super.onComponentTag(_tag);
                    _tag.put("action", "");
                }
            };
            add(form);

            @SuppressWarnings("unchecked")
            final GridXPanel panel = new GridXPanel("gridPanel", (IModel<UIGrid>) getDefaultModel());
            form.add(panel);

            final UIGrid uiGrid = (UIGrid) getDefaultModelObject();
            switch (uiGrid.getCommand().getTargetMode()) {
                case CONNECT:
                case SEARCH:
                    form.add(new FooterPanel("footer", Model.of(uiGrid), _modal));
                    break;
                default:
                    form.add(new WebMarkupContainer("footer").setVisible(false));
                    break;
            }
        } catch (final EFapsException e) {
            GridPage.LOG.error("Catched", e);
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(GridPage.CSS));
        _response.render(AbstractEFapsHeaderItem.forCss(AbstractContentPage.CSS));
    }
}
