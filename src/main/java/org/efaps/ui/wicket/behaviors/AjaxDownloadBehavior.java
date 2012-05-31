/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.ui.wicket.behaviors;

import java.io.File;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.efaps.ui.wicket.EFapsSession;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxDownloadBehavior
    extends AbstractAjaxBehavior
{
    /**
     *  Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Add the anticache tags or not.
     */
    private final boolean addAntiCache;

    /**
     * The file that will be downloaded.
     */
    private File file;

    /**
     * Constructor using antichache.
     */
    public AjaxDownloadBehavior()
    {
        this(true);
    }

    /**
     * @param _addAntiCache use anticache tags
     */
    public AjaxDownloadBehavior(final boolean _addAntiCache)
    {
        super();
        this.addAntiCache = _addAntiCache;
    }

    /**
     * Call this method to initiate the download.
     */
    public void initiate(final AjaxRequestTarget _target)
    {
        this.file = ((EFapsSession) getComponent().getSession()).getFile();
        if (this.file != null && this.file.exists()) {

            String url = getCallbackUrl().toString();

            if (this.addAntiCache) {
                url = url + (url.contains("?") ? "&" : "?");
                url = url + "antiCache=" + System.currentTimeMillis();
            }

            // the timeout is needed to let Wicket release the channel
            _target.appendJavaScript("setTimeout(\"window.location.href='" + url + "'\", 100);");
        }
    }

    /**
     * On request, respond with a ResourcStream.
     */
    public void onRequest()
    {
        final ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(getResourceStream(),
                        getFileName());
        handler.setContentDisposition(ContentDisposition.ATTACHMENT);
        getComponent().getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
    }

    /**
     * Override this method for a file name which will let the browser prompt
     * with a save/open dialog.
     *
     * @see ResourceStreamRequestTarget#getFileName()
     * @return filename
     */
    protected String getFileName()
    {
        return this.file.getName();
    }

    /**
     * Hook method providing the actual resource stream.
     * @return stream
     */
    protected IResourceStream getResourceStream()
    {
        return new FileResourceStream(this.file);
    }
}
