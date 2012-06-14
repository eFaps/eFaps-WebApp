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

package org.efaps.ui.wicket.request;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequestImpl;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.upload.FileItemFactory;
import org.apache.wicket.util.upload.FileUploadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsMultipartRequest
    extends MultipartServletWebRequestImpl
{

    /**
     * Parameters of this request.
     */
    private EFapsRequestParametersAdapter parameters;

    /**
     * Constructor.
     *
     * This constructor will use DiskFileItemFactory to store uploads.
     *
     * @param _request      the servlet request
     * @param _filterPrefix prefix to wicket filter mapping
     * @param _maxSize      the maximum size allowed for this request
     * @param _upload       upload identifier for UploadInfo
     * @throws FileUploadException Thrown if something goes wrong with upload
     */
    public EFapsMultipartRequest(final HttpServletRequest _request,
                                 final String _filterPrefix,
                                 final Bytes _maxSize,
                                 final String _upload)
        throws FileUploadException
    {
        super(_request, _filterPrefix, _maxSize, _upload);
    }

    /**
     * Constructor.
     *
     * @param _request      the servlet request
     * @param _filterPrefix prefix to wicket filter mapping
     * @param _maxSize      the maximum size allowed for this request
     * @param _upload       upload identifier for UploadInfo
     * @param _factory      DiskFileItemFactory to use when creating file
     *            items used to represent uploaded files
     * @throws FileUploadException Thrown if something goes wrong with upload
     */
    public EFapsMultipartRequest(final HttpServletRequest _request,
                                 final String _filterPrefix,
                                 final Bytes _maxSize,
                                 final String _upload,
                                 final FileItemFactory _factory)
        throws FileUploadException

    {
        super(_request, _filterPrefix, _maxSize, _upload, _factory);
    }

    @Override
    public IRequestParameters getRequestParameters()
    {
        if (this.parameters == null) {
            this.parameters = new EFapsRequestParametersAdapter(getQueryParameters(), getPostParameters());
        }
        return this.parameters;
    }

}
