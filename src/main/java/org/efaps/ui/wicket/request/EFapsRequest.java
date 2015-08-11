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

package org.efaps.ui.wicket.request;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.lang.Bytes;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsRequest
    extends ServletWebRequest
{

    /**
     * Parameters of this request.
     */
    private EFapsRequestParametersAdapter parameters;

    /**
     * @param _httpServletRequest original request
     * @param _filterPrefix filter prefix
     */
    public EFapsRequest(final HttpServletRequest _httpServletRequest,
                        final String _filterPrefix)
    {
        super(_httpServletRequest, _filterPrefix);
    }

    @Override
    public IRequestParameters getRequestParameters()
    {
        if (this.parameters == null) {
            this.parameters = new EFapsRequestParametersAdapter(getQueryParameters(), getPostParameters());
        }
        return this.parameters;
    }

    @Override
    public MultipartServletWebRequest newMultipartWebRequest(final Bytes _maxSize,
                                                             final String _upload)
        throws FileUploadException
    {
        return new EFapsMultipartRequest(getContainerRequest(), getFilterPrefix(), _maxSize, _upload);
    }

    @Override
    public MultipartServletWebRequest newMultipartWebRequest(final Bytes _maxSize,
                                                             final String _upload,
                                                             final FileItemFactory _factory)
        throws FileUploadException
    {
        return new EFapsMultipartRequest(getContainerRequest(), getFilterPrefix(), _maxSize, _upload, _factory);
    }
}
