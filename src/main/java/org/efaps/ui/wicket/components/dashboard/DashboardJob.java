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

import org.efaps.api.background.IExecutionBridge;
import org.efaps.api.background.IJob;
import org.efaps.ui.wicket.background.ExecutionBridge;
import org.efaps.ui.wicket.models.EsjpInvoker;
import org.efaps.util.EFapsBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DashboardJob.
 *
 * @author The eFaps Team
 */
public class DashboardJob
    implements IJob
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DashboardJob.class);


    /** The esjp invoker. */
    private final EsjpInvoker esjpInvoker;

    /**
     * Instantiates a new dashboard job.
     *
     * @param _esjpInvoker the _esjp invoker
     */
    public DashboardJob(final EsjpInvoker _esjpInvoker)
    {
        this.esjpInvoker = _esjpInvoker;
    }

    @Override
    public void execute(final IExecutionBridge _bridge)
    {
        try {
            final String content = this.esjpInvoker.getHtmlSnipplet().toString();
            ((ExecutionBridge) _bridge).setContent(content);
            ((ExecutionBridge) _bridge).setProgress(100);
        } catch (final EFapsBaseException e) {
            LOG.error("Execute", e);
        }
    }
}
