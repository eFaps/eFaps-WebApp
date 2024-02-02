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
package org.efaps.ui.wicket.background;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.efaps.admin.user.Company;
import org.efaps.api.background.IExecutionBridge;
import org.efaps.api.background.IJob;
import org.efaps.db.Context;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class JobRunnable.
 *
 * @author The eFaps Team
 */
public class JobRunnable
    implements Runnable
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JobRunnable.class);

    /** The job. */
    private final IJob job;

    /** The bridge. */
    private final IExecutionBridge bridge;

    /** The application. */
    private final Application application;

    /** The session. */
    private final EFapsSession session;

    /**
     * Instantiates a new job runnable.
     *
     * @param _job the _job
     * @param _bridge the _bridge
     */
    public JobRunnable(final IJob _job,
                       final IExecutionBridge _bridge)
    {
        this.job = _job;
        this.bridge = _bridge;
        this.application = Application.get();
        this.session = Session.exists() ? EFapsSession.get() : null;
    }

    @Override
    public void run()
    {
        try {
            ThreadContext.setApplication(this.application);
            ThreadContext.setSession(this.session);
            final Context context = Context.begin(this.bridge.getJobContext().getUserName(),
                            this.bridge.getJobContext().getLocale(), null, null, null, Context.Inheritance.Local);
            context.setCompany(Company.get(this.bridge.getJobContext().getCompanyUUID()));
            this.job.execute(this.bridge);
            Context.commit(true);
        } catch (final EFapsException e) {
            LOG.debug("run", e);
        } finally {
            ThreadContext.detach();
        }
    }
}
