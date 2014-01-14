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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.efapscontent;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * Component to show static images.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StaticImageComponent
    extends WebComponent
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * URL for the static image.
     */
    private String url;

    /**
     * @param _wicketid     wicket id for the component
     */
    public StaticImageComponent(final String _wicketid)
    {
        super(_wicketid);
    }

    /**
     * @param _wicketid     wicket id for the component
     * @param _scope        class scope used to determine the url
     * @param _name         name of the image to be used
     */
    public StaticImageComponent(final String _wicketid,
                                final Class<?> _scope,
                                final String _name)
    {
        this(_wicketid, _scope.getPackage().getName() + "." + _name);
    }

    /**
     * @param _wicketid     wicket id for the component
     * @param _url          url to the image
     */
    public StaticImageComponent(final String _wicketid,
                                final String _url)
    {
        super(_wicketid);
        this.url = _url;
    }

    /**
     * @param _wicketid     wicket id for the component
     * @param _reference    reference to the content
     */
    public StaticImageComponent(final String _wicketid,
                                final EFapsContentReference _reference)
    {
        super(_wicketid);
        this.url = _reference.getImageUrl();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        checkComponentTag(_tag, "img");
        _tag.put("src", this.url);
    }

    /**
     * This is the getter method for the instance variable {@link #url}.
     *
     * @return value of instance variable {@link #url}
     */
    public String getUrl()
    {
        return this.url;
    }

    /**
     * This is the setter method for the instance variable {@link #url}.
     *
     * @param _url the url to set
     */
    public void setUrl(final String _url)
    {
        this.url = _url;
    }

    /**
     * @param _reference    reference to be set
     */
    public void setReference(final EFapsContentReference _reference)
    {
        this.url = _reference.getImageUrl();
    }
}
