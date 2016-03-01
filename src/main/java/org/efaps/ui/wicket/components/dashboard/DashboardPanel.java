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
package org.efaps.ui.wicket.components.dashboard;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.util.time.Duration;
import org.efaps.api.background.IExecutionBridge;
import org.efaps.ui.wicket.EFapsApplication;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.embeddedlink.LinkElementLink;
import org.efaps.ui.wicket.models.EmbeddedLink;
import org.efaps.ui.wicket.models.EsjpInvoker;
import org.efaps.util.EFapsBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DashboardPanel.class);

    /**
     * The state.
     * 0:add loading component
     * 1:loading component added, waiting for ajax replace
     * 2:ajax replacement completed
     */
    private byte state = 0;

    /** The job name. */
    private String jobName;

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
        add(new WebMarkupContainer("hiddenRepeater").setOutputMarkupId(true).setVisible(false));

        add(new AbstractAjaxTimerBehavior(Duration.milliseconds(500))
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onTimer(final AjaxRequestTarget _target)
            {
                if (DashboardPanel.this.state < 2) {
                    if (DashboardPanel.this.jobName != null) {
                        final IExecutionBridge bridge = EFapsSession.get().getBridge4Job(DashboardPanel.this.jobName,
                                        true);
                        if (bridge == null) {
                            stop(_target);
                        } else if (bridge.isFinished()) {
                            final Component component = getLazyLoadComponent(LAZY_LOAD_COMPONENT_ID, (String) bridge
                                            .getContent());
                            DashboardPanel.this.replace(component);
                            setState((byte) 2);
                            stop(_target);
                        }
                    }
                    setUpdateInterval(Duration.seconds(3));
                    final RepeatingView hiddenRepeater = new RepeatingView("hiddenRepeater");
                    DashboardPanel.this.replace(hiddenRepeater);
                    final Iterator<EmbeddedLink> linksIter = EFapsSession.get().getEmbededLinks().iterator();
                    while (linksIter.hasNext()) {
                        final EmbeddedLink link = linksIter.next();
                        if (DashboardPanel.this.jobName.equals(link.getIdentifier())) {
                            hiddenRepeater.add(new LinkElementLink(hiddenRepeater.newChildId(), link));
                            linksIter.remove();
                        }
                    }
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
            try {
                this.jobName = ((EsjpInvoker) getDefaultModelObject()).getSnipplet().getIdentifier();
                IExecutionBridge bridge = EFapsSession.get().getBridge4Job(this.jobName, false);
                if (bridge == null) {
                    bridge = EFapsApplication.get().launch(new DashboardJob(
                                    (EsjpInvoker) getDefaultModelObject()), this.jobName);
                }
                this.jobName = bridge.getJobName();
            } catch (final EFapsBaseException e) {
                LOG.error("Catched error on startng background job.", e);
            }
        }
        super.onBeforeRender();
    }

    /**
     * Sets the state.
     *
     * @param _state the new state
     */
    private void setState(final byte _state)
    {
        this.state = _state;
        getPage().dirty();
    }

    /**
     * Gets the loading component.
     *
     * @param _markupId the markup id
     * @return The component to show while the real component is being created.
     */
    public Component getLoadingComponent(final String _markupId)
    {
        final IRequestHandler handler = new ResourceReferenceRequestHandler(AbstractDefaultAjaxBehavior.INDICATOR);
        return new Label(_markupId, "<img alt=\"Loading...\" src=\"" + RequestCycle.get().urlFor(handler) + "\"/>")
                        .setEscapeModelStrings(false);
    }
}
