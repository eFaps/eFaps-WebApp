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
package org.efaps.ui.wicket.components.dashboard;

import java.time.Duration;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.efaps.api.ui.IDashboard;
import org.efaps.db.Context;
import org.efaps.ui.wicket.models.EsjpInvoker;
import org.efaps.ui.wicket.pages.dashboard.DashboardPage;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DachboardContainerPanel
    extends Panel
{

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DachboardContainerPanel.class);

    /**
     * Instantiates a new dashboard container panel.
     *
     * @param _wicketId the _wicket id
     * @param _pageReference the _page reference
     * @param _dashboard the _dashboard
     * @param _main the _main
     * @throws EFapsException on error
     */
    public DachboardContainerPanel(final String _wicketId,
                                   final IDashboard _dashboard,
                                   final boolean _main)
        throws EFapsException
    {
        super(_wicketId);
        add(new DashboardPanel("dashBoard11", Model.of(new EsjpInvoker(_dashboard, "11"))));
        add(new DashboardPanel("dashBoard12", Model.of(new EsjpInvoker(_dashboard, "12"))));
        add(new DashboardPanel("dashBoard13", Model.of(new EsjpInvoker(_dashboard, "13"))));
        add(new DashboardPanel("dashBoard21", Model.of(new EsjpInvoker(_dashboard, "21"))));
        add(new DashboardPanel("dashBoard22", Model.of(new EsjpInvoker(_dashboard, "22"))));
        add(new DashboardPanel("dashBoard23", Model.of(new EsjpInvoker(_dashboard, "23"))));
        add(new DashboardPanel("dashBoard31", Model.of(new EsjpInvoker(_dashboard, "31"))));
        add(new DashboardPanel("dashBoard32", Model.of(new EsjpInvoker(_dashboard, "32"))));
        add(new DashboardPanel("dashBoard33", Model.of(new EsjpInvoker(_dashboard, "33"))));
    }

    /**
     * CheckBox to be able to activate and disactivate the Update.
     */
    public class AutomaticUpdateCheckbox
        extends AjaxCheckBox
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * related updateBehavior.
         */
        private final SelfUpdatingTimerBehavior ajaxUpdate;

        /**
         * @param _wicketId wicketId for this component
         * @param _ajaxUpdate related updateBehavior.
         * @throws EFapsException on error
         */
        public AutomaticUpdateCheckbox(final String _wicketId,
                                       final SelfUpdatingTimerBehavior _ajaxUpdate)
                                                       throws EFapsException
        {
            super(_wicketId);
            final boolean activated = !"false".equalsIgnoreCase(Context.getThreadContext().getUserAttribute(
                            DashboardPage.class.getName() + "." + _wicketId));
            setModel(Model.of(activated));
            ajaxUpdate = _ajaxUpdate;
            if (!activated) {
                ajaxUpdate.deactivate();
            }
        }

        @Override
        protected void onUpdate(final AjaxRequestTarget _target)
        {
            try {
                Context.getThreadContext().setUserAttribute(DashboardPage.class.getName() + "." + getId(),
                                getConvertedInput().toString());
            } catch (final EFapsException e) {
                LOG.error("error on saving UserAttribute", e);
            }
            if (getConvertedInput()) {
                ajaxUpdate.restart(_target);
            } else {
                ajaxUpdate.stop(_target);
            }
        }
    }

    /**
     * Update behavior.
     */
    public class SelfUpdatingTimerBehavior
        extends AbstractAjaxTimerBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * deactivate on next tiner event.
         */
        private boolean deactivate;

        /**
         * Construct.
         *
         * @param _updateInterval Duration between AJAX callbacks
         */
        public SelfUpdatingTimerBehavior(final Duration _updateInterval)
        {
            super(_updateInterval);
        }

        /**
         *
         */
        public void deactivate()
        {
            deactivate = true;
        }

        @Override
        protected final void onTimer(final AjaxRequestTarget _target)
        {
            if (deactivate) {
                deactivate = false;
                stop(_target);
            }
        }
    }
}
