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
package org.efaps.ui.wicket.background;

import java.io.Serializable;

import org.efaps.api.background.IExecutionBridge;
import org.efaps.api.background.IJobContext;

/**
 * The Class ExecutionBridge.
 *
 * @author The eFaps Team
 */
public class ExecutionBridge
    implements IExecutionBridge
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** The stop. */
    private volatile boolean stop = false;

    /** The cancel. */
    private volatile boolean cancel = false;

    /** The progress. */
    private volatile int progress = 0;

    /** The job name. */
    private String jobName;

    /** The content. */
    private Serializable content;

    /** The job context. */
    private IJobContext jobContext;

    /**
     * Instantiates a new execution bridge.
     */
    public ExecutionBridge()
    {
    }

    @Override
    public boolean isStop()
    {
        return this.stop;
    }

    /**
     * Sets the stop.
     *
     * @param _stop the new stop
     */
    public void setStop(final boolean _stop)
    {
        this.stop = _stop;
    }

    @Override
    public boolean isCancel()
    {
        return this.cancel;
    }

    /**
     * Sets the cancel.
     *
     * @param _cancel the new cancel
     */
    public void setCancel(final boolean _cancel)
    {
        this.cancel = _cancel;
    }

    @Override
    public int getProgress()
    {
        return this.progress;
    }

    /**
     * Sets the progress.
     *
     * @param _progress the new progress
     */
    public void setProgress(final int _progress)
    {
        this.progress = _progress;
    }

    @Override
    public boolean isFinished()
    {
        return isCancel() || this.progress >= 100;
    }

    @Override
    public String getJobName()
    {
        return this.jobName;
    }

    /**
     * Sets the job name.
     *
     * @param _jobName the new job name
     */
    public void setJobName(final String _jobName)
    {
        this.jobName = _jobName;
    }

    @Override
    public Serializable getContent()
    {
        return this.content;
    }

    /**
     * Sets the content.
     *
     * @param _content the new content
     */
    public void setContent(final Serializable _content)
    {
        this.content = _content;
    }

    @Override
    public IJobContext getJobContext()
    {
        return this.jobContext;
    }

    /**
     * Sets the job context.
     *
     * @param _jobContext the new job context
     */
    public void setJobContext(final IJobContext _jobContext)
    {
        this.jobContext = _jobContext;
    }
}
