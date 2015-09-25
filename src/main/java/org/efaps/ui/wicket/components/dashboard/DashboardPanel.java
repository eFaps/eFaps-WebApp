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
package org.efaps.ui.wicket.components.dashboard;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.util.time.Duration;
import org.efaps.ui.wicket.EFapsApplication;
import org.efaps.ui.wicket.ExecutionBridge;
import org.efaps.ui.wicket.models.EsjpInvoker;

/**
 * The Class DashboardPanel.
 *
 * @author The eFaps Team
 */
public class DashboardPanel
    extends Panel
{

    /**
     * The component id which will be used to load the lazily loaded component.
     */
    public static final String LAZY_LOAD_COMPONENT_ID = "content";

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // state,
    // 0:add loading component
    // 1:loading component added, waiting for ajax replace
    // 2:ajax replacement completed
    private byte state = 0;

    /** The bridge. */
    private ExecutionBridge bridge;

    /**
     * Instantiates a new panel.
     *
     * @param _wicketId the _wicket id
     * @param _model the _model
     */
    public DashboardPanel(final String _wicketId,
                          final IModel<EsjpInvoker> _model)
    {
        super(_wicketId, _model);

        setOutputMarkupId(true);

        add(new AbstractAjaxTimerBehavior(Duration.milliseconds(500))
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onTimer(final AjaxRequestTarget _target)
            {
                if (DashboardPanel.this.state < 2) {
                    if (DashboardPanel.this.bridge != null && DashboardPanel.this.bridge.isFinished()) {
                        final Component component = getLazyLoadComponent(LAZY_LOAD_COMPONENT_ID,
                                        (String) DashboardPanel.this.bridge.getContent());
                        DashboardPanel.this.replace(component);
                        setState((byte) 2);
                        stop(_target);
                    }
                    setUpdateInterval(Duration.seconds(3));
                }
                _target.add(DashboardPanel.this);
            }
        });
    }

    /**
     * Gets the lazy load component.
     *
     * @param _markupId the _markup id
     * @param _html the _html
     * @return the lazy load component
     */
    public Component getLazyLoadComponent(final String _markupId,
                                          final String _html)
    {
        return new WebMarkupContainer(_markupId)
        {
            /** */
            private static final long serialVersionUID = 1L;

            @Override
            public void onComponentTagBody(final MarkupStream _markupStream,
                                           final ComponentTag _openTag)
            {
                replaceComponentTagBody(_markupStream, _openTag, _html);
            }
        };
    }

    @Override
    public boolean isVisible()
    {
        final EsjpInvoker invoker = (EsjpInvoker) getDefaultModelObject();
        return invoker.isVisible();
    }

    /**
     * @see org.apache.wicket.Component#onBeforeRender()
     */
    @Override
    protected void onBeforeRender()
    {
        if (this.state == 0) {
            add(getLoadingComponent(LAZY_LOAD_COMPONENT_ID));
            setState((byte) 1);
            this.bridge = EFapsApplication.get().launch(new DashboardJob((EsjpInvoker) getDefaultModelObject()));
        }
        super.onBeforeRender();
    }

    /**
     *
     * @param state
     */
    private void setState(final byte state)
    {
        this.state = state;
        getPage().dirty();
    }

    /**
     * @param markupId The components markupid.
     * @return The component to show while the real component is being created.
     */
    public Component getLoadingComponent(final String _markupId)
    {
        final IRequestHandler handler = new ResourceReferenceRequestHandler(AbstractDefaultAjaxBehavior.INDICATOR);
        return new Label(_markupId, "<img alt=\"Loading...\" src=\"" + RequestCycle.get().urlFor(handler) + "\"/>")
                        .setEscapeModelStrings(false);
    }
}
