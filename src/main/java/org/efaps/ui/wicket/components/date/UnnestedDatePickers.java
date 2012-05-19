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


package org.efaps.ui.wicket.components.date;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.IJavaScriptResponse;
import org.apache.wicket.ajax.AjaxRequestTarget.IListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;


/**
 * Component is used to contain the DatPickers in case that the picker
 * must not be nested in the html dom.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UnnestedDatePickers
    extends WebComponent
    implements IListener
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Pickers contained in this component.
     */
    private final List<DatePickerBehavior> pickers = new ArrayList<DatePickerBehavior>();

    /**
     * @param _wicketId wicket id
     */
    public UnnestedDatePickers(final String _wicketId)
    {
        super(_wicketId);
        setOutputMarkupId(true);
    }


    /**
     * Replace the content with our own markup.
     *
     * @param _markupStream MarkupSream
     * @param _openTag      Open tag
     * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
     * org.apache.wicket.markup.ComponentTag)
     */
    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _openTag)
    {
        final StringBuilder html = new StringBuilder();
        for (final DatePickerBehavior picker : this.pickers) {
            html.append(picker.getTagBody());
        }
        replaceComponentTagBody(_markupStream, _openTag, html);
    }

    /**
     * Add a picker to this Component.
     * @param _picker picker to add
     * @return this
     */
    public UnnestedDatePickers addPicker(final DatePickerBehavior _picker)
    {
        this.pickers.add(_picker);
        return this;
    }

    /**
     * The component will be update in any case if registered as listener
     * by one of the pickers.
     *
     * @param _map      map
     * @param _target   AjaxRequestTarget
     * @see org.apache.wicket.ajax.AjaxRequestTarget.IListener#onBeforeRespond(java.util.Map,
     * org.apache.wicket.ajax.AjaxRequestTarget)
     */
    @Override
    public void onBeforeRespond(final Map<String, Component> _map,
                                final AjaxRequestTarget _target)
    {
        _target.add(this);
    }


    /* (non-Javadoc)
     * @see org.apache.wicket.ajax.AjaxRequestTarget.IListener#onAfterRespond(java.util.Map,
     * org.apache.wicket.ajax.AjaxRequestTarget.IJavaScriptResponse)
     */
    @Override
    public void onAfterRespond(final Map<String, Component> _map,
                               final IJavaScriptResponse _response)
    {
        // nothing must be done
    }
}
