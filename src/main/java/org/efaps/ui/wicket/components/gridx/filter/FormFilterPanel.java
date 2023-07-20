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

package org.efaps.ui.wicket.components.gridx.filter;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.field.Field;
import org.efaps.api.ci.UIFormFieldProperty;
import org.efaps.api.ui.IMapFilter;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.components.LazyIframe;
import org.efaps.ui.wicket.components.LazyIframe.IFrameProvider;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.content.grid.filter.FormFilterPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class FormFilterPanel
    extends GenericPanel<IMapFilter>
{

    /** The Constant CSS. */
    public static final ResourceReference CSS = new CssResourceReference(AbstractDojoBehavior.class,
                    "dojox/layout/resources/ResizeHandle.css");

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new form filter panel.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @param _uiGrid the ui grid
     * @throws CacheReloadException on error
     */
    public FormFilterPanel(final String _wicketId,
                           final IModel<IMapFilter> _model,
                           final UIGrid _uiGrid)
        throws CacheReloadException
    {
        super(_wicketId);
        final LazyIframe frame = new LazyIframe("content", new IFrameProvider()
        {
            private static final long serialVersionUID = 1L;

            @Override
            public Page getPage(final Component _component)
            {
                Page error = null;
                WebPage page = null;
                try {
                    page = new FormFilterPage(_model, _uiGrid, _component.getPage().getPageReference());
                } catch (final EFapsException e) {
                    error = new ErrorPage(e);
                }
                return error == null ? page : error;
            }
        }, null, false);

        final String id = RandomStringUtils.randomAlphabetic(8);
        frame.setMarkupId(id);
        frame.setOutputMarkupId(true);
        frame.add(new ContentPaneBehavior(null, false).setJsExecuteable(true));
        frame.add(new LoadFormBehavior());
        frame.setDefaultModel(_model);
        this.add(frame);

        final String cmdName = Field.get(_model.getObject().getFieldId()).getProperty(UIFormFieldProperty.FILTER_CMD.value());
        final Command cmd = Command.get(cmdName);
        this.add(AttributeModifier.append("style", "width:" + cmd.getWindowWidth() + "px"));
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(CssHeaderItem.forReference(FormFilterPanel.CSS));
    }

    /**
     * The Class LoadFormBehavior.
     *
     */
    public static class LoadFormBehavior
        extends AbstractDojoBehavior
    {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public void renderHead(final Component _component,
                               final IHeaderResponse _response)
        {
            super.renderHead(_component, _response);
            final String fttd = "fttd_" + ((IMapFilter) _component.getDefaultModelObject()).getFieldId();

            final StringBuilder js = new StringBuilder()
                .append("ready(function() {\n")
                .append("var pd = registry.byId(\"").append(fttd).append("\");\n")
                .append("aspect.before(pd, 'onOpen', function() {\n")
                .append("registry.byId(\"").append(_component.getMarkupId()).append("\").set(\"href\",\"")
                .append(_component.urlForListener(new PageParameters())).append("\");\n")
                .append("});\n")
                .append("});");
            _response.render(JavaScriptHeaderItem.forScript(DojoWrapper.require(js, DojoClasses.ready,
                            DojoClasses.registry, DojoClasses.aspect, DojoClasses.domConstruct),
                            _component.getMarkupId() + "-Script"));
        }
    }
}
