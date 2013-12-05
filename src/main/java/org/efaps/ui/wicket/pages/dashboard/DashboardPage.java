/*
 * Copyright 2003 - 2013 The eFaps Team
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
        final boolean active = config != null
                        ? config.getAttributeValueAsBoolean(KernelSettings.ACTIVATE_BPM) : false;
        // BPM_DashBoard_AssignedTask
        final Command assCmd = Command.get(UUID.fromString("63933a70-82d3-4fbc-bef2-cdf06c77013f"));
        if (active && assCmd != null && assCmd.hasAccess(TargetMode.VIEW, null)) {
            final TaskTablePanel assignedTaskTable = new TaskTablePanel("assignedTaskTable", _pageReference,
                            new AssignedTaskSummaryProvider());
            add(assignedTaskTable);

            final int duration1 = Configuration.getAttributeAsInteger(ConfigAttribute.BOARD_ASSIGNED_AUTIME);
            if (duration1 > 0) {
                final SelfUpdatingTimerBehavior ajaxUpdate = new SelfUpdatingTimerBehavior(
                                Duration.seconds(duration1));
                assignedTaskTable.add(ajaxUpdate);

                if (Configuration.getAttributeAsBoolean(ConfigAttribute.BOARD_ASSIGNEDTASK_AU)) {
                    add(new AutomaticUpdateCheckbox("assignedTaskAU", ajaxUpdate));
                } else {
                    add(new WebMarkupContainer("assignedTaskAU").setVisible(false));
                }
            } else {
                add(new WebMarkupContainer("assignedTaskAU").setVisible(false));
            }
            add(new Label("assignedTaskHeader", DBProperties.getProperty(DashboardPage.class.getName()
                            + ".assignedTaskHeader")));
        } else {
            add(new WebMarkupContainer("assignedTaskTable").setVisible(false));
            add(new WebMarkupContainer("assignedTaskHeader").setVisible(false));
            add(new WebMarkupContainer("assignedTaskAU").setVisible(false));
        }

        // BPM_DashBoard_OwnedTask
        final Command ownCmd = Command.get(UUID.fromString("60a9bfcd-928e-4b96-a617-94d70fb0c8ab"));
        if (active && ownCmd != null && ownCmd.hasAccess(TargetMode.VIEW, null)) {
            final TaskTablePanel ownedTaskTable = new TaskTablePanel("ownedTaskTable", _pageReference,
                            new OwnedTaskSummaryProvider());
            add(ownedTaskTable);

            final int duration2 = Configuration.getAttributeAsInteger(ConfigAttribute.BOARD_OWNEDTASK_AUTIME);
            if (duration2 > 0) {
                final SelfUpdatingTimerBehavior ajaxUpdate = new SelfUpdatingTimerBehavior(
                                Duration.seconds(duration2));
                ownedTaskTable.add(ajaxUpdate);
                if (Configuration.getAttributeAsBoolean(ConfigAttribute.BOARD_OWNEDTASK_AU)) {
                    add(new AutomaticUpdateCheckbox("ownedTaskAU", ajaxUpdate));
                } else {
                    add(new WebMarkupContainer("ownedTaskAU").setVisible(false));
                }
            } else {
                add(new WebMarkupContainer("ownedTaskAU").setVisible(false));
            }
            add(new Label("ownedTaskHeader",
                            DBProperties.getProperty(DashboardPage.class.getName() + ".ownedTaskHeader")));
        } else {
            add(new WebMarkupContainer("ownedTaskTable").setVisible(false));
            add(new WebMarkupContainer("ownedTaskHeader").setVisible(false));
            add(new WebMarkupContainer("ownedTaskAU").setVisible(false));
        }
        final String esjp = Configuration.getAttribute(ConfigAttribute.BOARD_PANEL1);
        if (esjp != null && !esjp.isEmpty()) {
            add(new EsjpComponent("dashBoard1", Model.of(new EsjpInvoker(esjp))));
        } else {
            add(new WebMarkupContainer("dashBoard1").setVisible(false));
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

    public class SelfUpdatingTimerBehavior
        extends AbstractAjaxTimerBehavior
    {

        private static final long serialVersionUID = 1L;

        /**
         * deactivate on next tiner event.
         */
        private boolean deactivate;

        /**
         * Construct.
         *
         * @param updateInterval Duration between AJAX callbacks
         */
        public SelfUpdatingTimerBehavior(final Duration updateInterval)
        {
            super(updateInterval);
        }

        /**
         *
         */
        public void deactivate()
        {
           this.deactivate = true;
        }

        /**
         * @see org.apache.wicket.ajax.AbstractAjaxTimerBehavior#onTimer(AjaxRequestTarget)
         */
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
