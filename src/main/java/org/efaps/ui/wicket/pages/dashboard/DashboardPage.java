/*
 * Copyright 2003 - 2014 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.pages.dashboard;

import java.util.Properties;
import java.util.UUID;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Command;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.bpm.task.AssignedTaskSummaryProvider;
import org.efaps.ui.wicket.components.bpm.task.OwnedTaskSummaryProvider;
import org.efaps.ui.wicket.components.bpm.task.TaskTablePanel;
import org.efaps.ui.wicket.components.dashboard.EsjpComponent;
import org.efaps.ui.wicket.models.EsjpInvoker;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DashboardPage
    extends AbstractMergePage
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DashboardPage.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(DashboardPage.class,
                    "DashboardPage.css");

    /**
     * @param _pageReference Reference to the calling page
     * @throws EFapsException on error
     */
    public DashboardPage(final PageReference _pageReference)
        throws EFapsException
    {
        super();
        final SystemConfiguration config = EFapsSystemConfiguration.get();
        boolean used = false;
        final boolean active = config != null
                        ? config.getAttributeValueAsBoolean(KernelSettings.ACTIVATE_BPM) : false;
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
                final SelfUpdatingTimerBehavior ajaxUpdate = new SelfUpdatingTimerBehavior(
                                Duration.seconds(duration1));
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
                final SelfUpdatingTimerBehavior ajaxUpdate = new SelfUpdatingTimerBehavior(
                                Duration.seconds(duration2));
                ownedTaskTable.add(ajaxUpdate);
                if (Configuration.getAttributeAsBoolean(ConfigAttribute.BOARD_OWNEDTASK_AU)) {
                    ownedTask.add(new AutomaticUpdateCheckbox("ownedTaskAU", ajaxUpdate));
                } else {
                    ownedTask.add(new WebMarkupContainer("ownedTaskAU").setVisible(false));
                }
            } else {
                ownedTask.add(new WebMarkupContainer("ownedTaskAU").setVisible(false));
            }
            ownedTask.add(new Label("ownedTaskHeader",
                            DBProperties.getProperty(DashboardPage.class.getName() + ".ownedTaskHeader")));
            used = true;
        } else {
            add(new WebMarkupContainer("ownedTask").setVisible(false));
        }

        if (used) {
            add(new WebMarkupContainer("dashBoard11").setVisible(false));
        }

        final Properties panelProperties = Configuration.getAttributeAsProperties(ConfigAttribute.BOARD_PANELS);
        if (panelProperties != null) {
            final String panel11 = panelProperties.getProperty("panel11");
            final String panel12 = panelProperties.getProperty("panel12");
            String panel21 = panelProperties.getProperty("panel21");
            final String panel22 = panelProperties.getProperty("panel22");
            String panel31 = panelProperties.getProperty("panel31");
            final String panel32 = panelProperties.getProperty("panel32");
            //shift down
            if (used && panel11 != null && !panel11.isEmpty()) {
                panel31 = panel21;
                panel21 = panel11;
            }

            if (panel11 != null && !panel11.isEmpty() && !used) {
                add(new EsjpComponent("dashBoard11", Model.of(new EsjpInvoker(panel11))));
            } else if (!used) {
                add(new WebMarkupContainer("dashBoard11").setVisible(false));
            }

            if (panel12 != null && !panel12.isEmpty()) {
                add(new EsjpComponent("dashBoard12", Model.of(new EsjpInvoker(panel12))));
            } else {
                add(new WebMarkupContainer("dashBoard12").setVisible(false));
            }

            if (panel21 != null && !panel21.isEmpty()) {
                add(new EsjpComponent("dashBoard21", Model.of(new EsjpInvoker(panel21))));
            } else {
                add(new WebMarkupContainer("dashBoard21").setVisible(false));
            }

            if (panel22 != null && !panel22.isEmpty()) {
                add(new EsjpComponent("dashBoard22", Model.of(new EsjpInvoker(panel22))));
            } else {
                add(new WebMarkupContainer("dashBoard22").setVisible(false));
            }

            if (panel31 != null && !panel31.isEmpty()) {
                add(new EsjpComponent("dashBoard31", Model.of(new EsjpInvoker(panel31))));
            } else {
                add(new WebMarkupContainer("dashBoard31").setVisible(false));
            }

            if (panel32 != null && !panel32.isEmpty()) {
                add(new EsjpComponent("dashBoard32", Model.of(new EsjpInvoker(panel32))));
            } else {
                add(new WebMarkupContainer("dashBoard32").setVisible(false));
            }
        } else {
            if (!used) {
                add(new WebMarkupContainer("dashBoard11").setVisible(false));
            }
            add(new WebMarkupContainer("dashBoard12").setVisible(false));
            add(new WebMarkupContainer("dashBoard21").setVisible(false));
            add(new WebMarkupContainer("dashBoard22").setVisible(false));
            add(new WebMarkupContainer("dashBoard31").setVisible(false));
            add(new WebMarkupContainer("dashBoard32").setVisible(false));
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(DashboardPage.CSS));
    }


    /**
     * CheckBox to be able to activate and disactivate the Update.
     */
    public class AutomaticUpdateCheckbox
        extends AjaxCheckBox
    {
        /**
         *Needed for serialization.
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
                DashboardPage.LOG.error("error on saving UserAttribute", e);
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
