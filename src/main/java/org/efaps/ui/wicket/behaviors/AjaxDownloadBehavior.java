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
package org.efaps.ui.wicket.behaviors;

import java.io.File;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;

/**
 * TODO comment!
 *
 * @author The eFaps Team
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
     * Add the script to the page.
     */
    private boolean addScript2Page = false;

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
     * @param _target Ajaxtarget
     */
    public void initiate(final IPartialPageRequestHandler _target)
    {
        final String url = getCallBackURL();
        if (url != null) {
            _target.prependJavaScript(getCallBackScript(url));
        }
    }

    /**
     * @return the callback url, null if no
     */
    protected String getCallBackURL()
    {
        String ret = null;
        final File file = ((EFapsSession) getComponent().getSession()).getFile();
        if (file != null && file.exists()) {
            String url = getCallbackUrl().toString();
            if (this.addAntiCache) {
                url = url + (url.contains("?") ? "&" : "?");
                url = url + "antiCache=" + System.currentTimeMillis();
            }
            //since version 6.15 there might be a .. add the beginning that must be
            //removed if the link comes from the main page
            if (url.startsWith("..")) {
                url = url.substring(1);
            }
            ret = url;
        }
        return ret;
    }

    /**
     * Initiate the download.
     */
    public void initiate()
    {
        this.addScript2Page = true;
    }

    /**
     * On request, respond with a ResourcStream.
     */
    @Override
    public void onRequest()
    {
        final String fileName = getFileName();
        final IResourceStream stream = getResourceStream();
        final ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(stream, fileName);
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
        final File file = EFapsSession.get().getFile();
        return file.getName();
    }

    /**
     * Hook method providing the actual resource stream.
     * @return stream
     */
    protected IResourceStream getResourceStream()
    {
        final File file = EFapsSession.get().getFile();
        final FileResourceStream ret = new FileResourceStream(file);
        EFapsSession.get().setFile(null);
        return ret;
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        if (this.addScript2Page) {
            final String url = getCallBackURL();
            if (url != null) {
                _response.render(JavaScriptHeaderItem.forScript(getCallBackScript(url),
                                AjaxDownloadBehavior.class.getName()));
            }
            this.addScript2Page = false;
        }
    }

    /**
     * Script that searches for an existing iframe to use it for the download of the frame.
     * If it does not exist it will be created.
     *
     * @param _url the url
     * @return the callback script
     */
    protected CharSequence getCallBackScript(final String _url)
    {
        final StringBuilder js = new StringBuilder()
            .append("baseWindow.withDoc(top.dojo.doc, function () {\n")
            .append("var node = dom.byId('downloadFrame');\n")
            .append("if (node == null) {\n")
            .append("node = domConstruct.place('<iframe id=\"downloadFrame\" src=\"about:blank\" ")
                .append("style=\"position: absolute; left: 1px; top: 1px; height: 1px; width: 1px; ")
                .append("visibility: hidden\">', win.body());\n")
            .append("}\n")
            .append("node.src='").append(_url).append("';")
            .append("});\n");
        return DojoWrapper.require(js, DojoClasses.baseWindow, DojoClasses.dom, DojoClasses.domConstruct);
    }
}
