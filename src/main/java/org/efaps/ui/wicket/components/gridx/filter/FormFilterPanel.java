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

package org.efaps.ui.wicket.components.gridx.filter;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.efaps.api.ui.IMapFilter;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.components.LazyIframe;
import org.efaps.ui.wicket.components.LazyIframe.IFrameProvider;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.pages.content.grid.filter.FormFilterPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class FormFilterPanel
    extends GenericPanel<IMapFilter>
{

    /**
     * Id of the Iframe.
     */
    public static final String IFRAME_ID = "eFapsFilterFrame";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new form filter panel.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @param _uiGrid the ui grid
     */
    public FormFilterPanel(final String _wicketId,
                           final IModel<IMapFilter> _model,
                           final UIGrid _uiGrid)
    {
        super(_wicketId);
        final String id = RandomStringUtils.randomAlphabetic(8);
        final LazyIframe frame = new LazyIframe("content", new IFrameProvider()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public Page getPage()
            {
                Page error = null;
                WebPage page = null;
                try {
                    page = new FormFilterPage(_model, _uiGrid);
                } catch (final EFapsException e) {
                    error = new ErrorPage(e);
                }
                return error == null ? page : error;
            }
        }, id);
        frame.setMarkupId(id);
        frame.setOutputMarkupId(true);
        frame.add(new ContentPaneBehavior(null, false));
        this.add(frame);
    }
}
