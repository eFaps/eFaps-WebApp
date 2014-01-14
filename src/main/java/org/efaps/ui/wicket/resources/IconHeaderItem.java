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

package org.efaps.ui.wicket.resources;

import java.util.Collections;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.Response;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class IconHeaderItem
    extends HeaderItem
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Content Reference.
     */
    private final EFapsContentReference contentReference;

    /**
     * @param _contentReference reference to the content
     */
    public IconHeaderItem(final EFapsContentReference _contentReference)
    {
        this.contentReference = _contentReference;
    }

    @Override
    public Iterable<?> getRenderTokens()
    {
        return Collections.singletonList(getLink());
    }

    /**
     * @return link url
     */
    private String getLink()
    {
        return this.contentReference.getImageUrl();
    }

    @Override
    public void render(final Response _response)
    {
        _response.write("<link rel=\"icon\" type=\"image/png\" href=\"");
        _response.write(getLink());
        _response.write("\">");
    }
}
