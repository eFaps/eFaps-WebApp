/*
 * Copyright 2003 - 2016 The eFaps Team
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


package org.efaps.ui.wicket.resources;

import java.util.Collections;

import org.apache.wicket.markup.head.HeaderItem;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class AbstractEFapsHeaderItem
    extends HeaderItem
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The reference for this HeaderItem.
     */
    private final EFapsContentReference reference;

    /**
     * On Aggregation of the HeaderItem the Collection will be sorted using
     * this variable influencing directly the provided Content form eFaps.
     */
    private Integer sortWeight = 0;

    /**
     * @param _reference reference for this HeaderItem
     */
    protected AbstractEFapsHeaderItem(final EFapsContentReference _reference)
    {
        this.reference = _reference;
    }

    /**
     * Creates a {@link EFapsCssHeaderItem} for the given reference.
     *
     * @param _reference a reference to a CSS resource
     * @return A newly created {@link EFapsCssHeaderItem} for the given reference.
     */
    public static EFapsCssHeaderItem forCss(final EFapsContentReference _reference)
    {
        return new EFapsCssHeaderItem(_reference);
    }

    /**
     * Creates a {@link EFapsJavaScriptHeaderItem} for the given reference.
     *
     * @param _reference a reference to a JavaScript resource
     * @return A newly created {@link EFapsJavaScriptHeaderItem} for the given reference.
     */
    public static EFapsJavaScriptHeaderItem forJavaScript(final EFapsContentReference _reference)
    {
        return new EFapsJavaScriptHeaderItem(_reference);
    }

    /**
     * @return empty Collection because no rendering will be donw
     */
    @Override
    public Iterable<?> getRenderTokens()
    {
        return Collections.emptyList();
    }

    /**
     * Getter method for the instance variable {@link #reference}.
     *
     * @return value of instance variable {@link #reference}
     */
    public EFapsContentReference getReference()
    {
        return this.reference;
    }

    /**
     * Getter method for the instance variable {@link #sortWeight}.
     *
     * @return value of instance variable {@link #sortWeight}
     */
    public Integer getSortWeight()
    {
        return this.sortWeight;
    }

    /**
     * Setter method for instance variable {@link #sortWeight}.
     *
     * @param _sortWeight value for instance variable {@link #sortWeight}
     * @return this instance to allow chaining
     */
    public AbstractEFapsHeaderItem setSortWeight(final int _sortWeight)
    {
        this.sortWeight = _sortWeight;
        return this;
    }
}
