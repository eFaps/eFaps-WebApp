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

import java.util.UUID;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Command;
import org.efaps.api.ui.IDashboard;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.bpm.task.AssignedTaskSummaryProvider;
import org.efaps.ui.wicket.components.bpm.task.OwnedTaskSummaryProvider;
import org.efaps.ui.wicket.components.bpm.task.TaskTablePanel;
import org.efaps.ui.wicket.models.EsjpInvoker;
import org.efaps.ui.wicket.pages.dashboard.DashboardPage;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
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
     * Instantiates a new dachboard container panel.
     *
     * @param _wicketId the _wicket id
     * @param _pageReference the _page reference
     * @param _dashboard the _dashboard
     * @param _main the _main
     * @throws EFapsException on error
     */
    public DachboardContainerPanel(final String _wicketId,
                                   final PageReference _pageReference,
                                   final IDashboard _dashboard,
                                   final boolean _main)
        throws EFapsException
    {
        super(_wicketId);
        final SystemConfiguration config = EFapsSystemConfiguration.get();
        boolean used = false;
        final boolean active = _main && config != null ? config.getAttributeValueAsBoolean(KernelSettings.ACTIVATE_BPM)
                        : false;
        // BPM_DashBoard_AssignedTask
        final Command assCmd = Command.get(UUID.fromString("63933a70-82d3-4fbc-bef2-cdf06c77013f"));
        if (active && assCmd != null && assCmd.hasAccess(TargetMode.VIEW, null)) {
            final WebMarkupContainer assignedTask = new WebMarkupContainer("assignedTask");
            add(assignedTask);

            final TaskTablePanel assignedTaskTable = new TaskTablePanel("assignedTaskTable", _pageReference,
                            new AssignedTaskSummaryProvider());
            assignedTask.add(assignedTaskTable);

            final int duration1 = Configuration.getAttributeAsInteger(ConfigAttribute.BOARD_ASSIGNED_AUTIME);
            if (duration1 > 0) {
                final SelfUpdatingTimerBehavior ajaxUpdate = new SelfUpdatingTimerBehavior(Duration.seconds(duration1));
                assignedTaskTable.add(ajaxUpdate);

                if (Configuration.getAttributeAsBoolean(ConfigAttribute.BOARD_ASSIGNEDTASK_AU)) {
                    assignedTask.add(new AutomaticUpdateCheckbox("assignedTaskAU", ajaxUpdate));
                } else {
                    assignedTask.add(new WebMarkupContainer("assignedTaskAU").setVisible(false));
                }
            } else {
                assignedTask.add(new WebMarkupContainer("assignedTaskAU").setVisible(false));
            }
            assignedTask.add(new Label("assignedTaskHeader", DBProperties.getProperty(DashboardPage.class.getName()
                            + ".assignedTaskHeader")));
            used = true;
        } else {
            add(new WebMarkupContainer("assignedTask").setVisible(false));
        }

        // BPM_DashBoard_OwnedTask
        final Command ownCmd = Command.get(UUID.fromString("60a9bfcd-928e-4b96-a617-94d70fb0c8ab"));
        if (active && ownCmd != null && ownCmd.hasAccess(TargetMode.VIEW, null)) {
            final WebMarkupContainer ownedTask = new WebMarkupContainer("ownedTask");
            add(ownedTask);
            final TaskTablePanel ownedTaskTable = new TaskTablePanel("ownedTaskTable", _pageReference,
                            new OwnedTaskSummaryProvider());
            ownedTask.add(ownedTaskTable);

            final int duration2 = Configuration.getAttributeAsInteger(ConfigAttribute.BOARD_OWNEDTASK_AUTIME);
            if (duration2 > 0) {
                final SelfUpdatingTimerBehavior ajaxUpdate = new SelfUpdatingTimerBehavior(Duration.seconds(duration2));
                ownedTaskTable.add(ajaxUpdate);
                if (Configuration.getAttributeAsBoolean(ConfigAttribute.BOARD_OWNEDTASK_AU)) {
                    ownedTask.add(new AutomaticUpdateCheckbox("ownedTaskAU", ajaxUpdate));
                } else {
                    ownedTask.add(new WebMarkupContainer("ownedTaskAU").setVisible(false));
                }
            } else {
                ownedTask.add(new WebMarkupContainer("ownedTaskAU").setVisible(false));
            }
            ownedTask.add(new Label("ownedTaskHeader", DBProperties.getProperty(DashboardPage.class.getName()
                            + ".ownedTaskHeader")));
            used = true;
        } else {
            add(new WebMarkupContainer("ownedTask").setVisible(false));
        }

        if (used) {
            add(new WebMarkupContainer("dashBoard11").setVisible(false));
        }

        if (!used) {
            add(new DashboardPanel("dashBoard11", Model.of(new EsjpInvoker(_dashboard, "11"))));
        }
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
            this.ajaxUpdate = _ajaxUpdate;
            if (!activated) {
                this.ajaxUpdate.deactivate();
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
                this.ajaxUpdate.restart(_target);
            } else {
                this.ajaxUpdate.stop(_target);
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
            this.deactivate = true;
        }

        @Override
        protected final void onTimer(final AjaxRequestTarget _target)
        {
            if (this.deactivate) {
                this.deactivate = false;
                stop(_target);
            } else {
                ((TaskTablePanel) getComponent()).updateData();
                _target.add(getComponent());
            }
        }
    }
}
