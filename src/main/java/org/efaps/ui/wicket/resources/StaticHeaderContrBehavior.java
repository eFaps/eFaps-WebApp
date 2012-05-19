/*
 * Copyright 2003 - 2012 The eFaps Team
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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

/**
 * A HeaderContributor for Content which is stored inside the eFaps-DataBase.<br>
 * Unlike other HeaderContributor the Instance of StaticHeaderContributor might
 * be removed on rendering from the component to merge it with other
 * StaticHeaderContributor and added as a new merged StaticHeaderContributor to
 * a ParentComponent. To prevent a StaticHeaderContributor from merging for e.g.
 * keep the Sequence of Behaviors in a Component {@link #merged} must be set to
 * true.
 *
 * @author The eFaps Team
 * @version $Id: StaticHeaderContributor.java 3447 2009-11-29 22:46:39Z
 *          tim.moxter $
 */
public class StaticHeaderContrBehavior
    extends Behavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * this enum is used to distinguish between the different Types of the
     * Header.
     */
    public static enum HeaderType
    {
        /** CSS. */
        CSS,
        /** JS. */
        JS;
    }

    /**
     * the Reference this Behavior is connected to.
     */
    private final EFapsContentReference reference;

    /**
     * is this StaticHeaderContributor allready merged.
     */
    private boolean merged = false;

    /**
     * The HeaderType of this StaticHeaderContributor.
     */
    private HeaderType headerType;

    /**
     * Component this behavior is bind to;
     */
    private Component component;

    /**
     * Constructor setting the IHeaderContributor in the SuperClass and the
     * Reference.
     *
     * @param _reference EFapsContentReference
     */
    private StaticHeaderContrBehavior(final EFapsContentReference _reference)
    {
        this.headerType = HeaderType.JS;
        this.reference = _reference;
    }

    /**
     * Static method to get a StaticHeaderContributor for CSS that will be
     * merged.
     *
     * @param _reference Reference to the Content
     * @return StaticHeaderContributor
     */
    public static final StaticHeaderContrBehavior forCss(final EFapsContentReference _reference)
    {
        return StaticHeaderContrBehavior.forCss(_reference, false);
    }

    /**
     * Static method to get a StaticHeaderContributor for CSS.
     *
     * @param _reference Reference to the Content
     * @param _noMerge should this StaticHeaderContributor merged
     * @return StaticHeaderContributor
     */
    public static final StaticHeaderContrBehavior forCss(final EFapsContentReference _reference,
                                                                final boolean _noMerge)
    {
        final StaticHeaderContrBehavior ret = new StaticHeaderContrBehavior(_reference);
        ret.setHeaderType(StaticHeaderContrBehavior.HeaderType.CSS);
        ret.setMerged(_noMerge);
        return ret;
    }

    /**
     * Static method to get a StaticHeaderContributor for JavaScript that will
     * be merged.
     *
     * @param _reference Reference to the Content
     * @return StaticHeaderContributor
     */
    public static final StaticHeaderContrBehavior forJavaScript(final EFapsContentReference _reference)
    {
        return StaticHeaderContrBehavior.forJavaScript(_reference, false);
    }

    /**
     * * Static method to get a StaticHeaderContributor for JavaScript.
     *
     * @param _reference Reference to the Content
     * @param _nomerge should this StaticHeaderContributor not bemerged
     * @return StaticHeaderContributor
     */
    public static final StaticHeaderContrBehavior forJavaScript(final EFapsContentReference _reference,
                                                                       final boolean _nomerge)
    {

        final StaticHeaderContrBehavior ret = new StaticHeaderContrBehavior(_reference);
        ret.setHeaderType(StaticHeaderContrBehavior.HeaderType.JS);
        ret.setMerged(_nomerge);
        return ret;
    }


    /* (non-Javadoc)
     * @see org.apache.wicket.behavior.Behavior#bind(org.apache.wicket.Component)
     */
    @Override
    public void bind(final Component _component)
    {
        super.bind(_component);
        this.component = _component;
    }

    /**
     * Getter method for the instance variable {@link #component}.
     *
     * @return value of instance variable {@link #component}
     */
    public Component getComponent()
    {
        return this.component;
    }

    /**
     * This is the getter method for the instance variable {@link #merged}.
     *
     * @return value of instance variable {@link #merged}
     */
    public boolean isMerged()
    {
        return this.merged;
    }

    /**
     * This is the setter method for the instance variable {@link #merged}.
     *
     * @param _merged the merged to set
     */
    public void setMerged(final boolean _merged)
    {
        this.merged = _merged;
    }

    /**
     * This is the getter method for the instance variable {@link #reference}.
     *
     * @return value of instance variable {@link #reference}
     */
    public EFapsContentReference getReference()
    {
        return this.reference;
    }

    /**
     * This is the getter method for the instance variable {@link #headerType}.
     *
     * @return value of instance variable {@link #headerType}
     */
    public HeaderType getHeaderType()
    {
        return this.headerType;
    }

    /**
     * This is the setter method for the instance variable {@link #headerType}.
     *
     * @param _headerType the headerType to set
     */
    public void setHeaderType(final HeaderType _headerType)
    {
        this.headerType = _headerType;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.apache.wicket.behavior.Behavior#renderHead(org.apache.wicket.Component
     * , org.apache.wicket.markup.head.IHeaderResponse)
     */
    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        final HeaderItem headerItem;
        if (this.headerType.equals(HeaderType.CSS)) {
            headerItem = CssHeaderItem.forUrl(this.reference.getStaticContentUrl());
        } else {
            headerItem = JavaScriptHeaderItem.forUrl(this.reference.getStaticContentUrl());
        }
        _response.render(headerItem);
    }
}
