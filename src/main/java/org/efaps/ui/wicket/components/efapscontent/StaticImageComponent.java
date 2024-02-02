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

    /** The reference. */
    private EFapsContentReference reference;

    /**
     * @param _wicketid wicket id for the component
     */
    public StaticImageComponent(final String _wicketid)
    {
        super(_wicketid);
    }

    /**
     * @param _wicketid wicket id for the component
     * @param _reference reference to the content
     */
    public StaticImageComponent(final String _wicketid,
                                final EFapsContentReference _reference)
    {
        super(_wicketid);
        this.reference = _reference;
    }

    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        super.onComponentTag(_tag);
        checkComponentTag(_tag, "img");
        _tag.put("src", getUrl());
    }

    /**
     * This is the getter method for the instance variable {@link #url}.
     *
     * @return value of instance variable {@link #url}
     */
    public String getUrl()
    {
        return this.reference.getImageUrl();
    }

    /**
     * @param _reference reference to be set
     */
    public void setReference(final EFapsContentReference _reference)
    {
        this.reference = _reference;
    }
}
