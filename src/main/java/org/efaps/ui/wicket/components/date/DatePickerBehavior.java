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

import org.apache.wicket.Component;
import org.apache.wicket.Response;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.util.string.Strings;


/**
 * This behavior adds a datepicker.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DatePickerBehavior
    extends DatePicker
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Is the Picker nested.
     */
    private boolean nested = true;

    /**
     * the component the unnested DatePicker will be added to.
     */
    private UnnestedDatePickers unnestedComp;

    /**
     * Getter method for the instance variable {@link #nested}.
     *
     * @return value of instance variable {@link #nested}
     */
    public boolean isNested()
    {
        return this.nested;
    }

    /**
     * @param _unnested component to be added to
     */
    public void setUnNestedComponent(final UnnestedDatePickers _unnested)
    {
        this.unnestedComp = _unnested;
        this.unnestedComp.addPicker(this);
        setNested(false);
        final AjaxRequestTarget target = AjaxRequestTarget.get();
        if (target != null) {
            target.addListener(this.unnestedComp);
        }
    }

    /**
     * Setter method for instance variable {@link #nested}.
     *
     * @param _nested value for instance variable {@link #nested}
     */
    public void setNested(final boolean _nested)
    {
        this.nested = _nested;
    }

    @Override
    public void onRendered(final Component _component)
    {
        if (isNested()) {
            super.onRendered(_component);
        } else {
            final Response response = _component.getResponse();
            response.write("<img style=\"");
            response.write(getIconStyle());
            response.write("\" id=\"");
            response.write(getIconId());
            response.write("\" src=\"");
            final CharSequence iconUrl = getIconUrl();
            response.write(Strings.escapeMarkup(iconUrl != null ? iconUrl.toString() : ""));
            response.write("\" alt=\"");
            final CharSequence alt = getIconAltText();
            response.write(Strings.escapeMarkup((alt != null) ? alt.toString() : ""));
            response.write("\" title=\"");
            final CharSequence title = getIconTitle();
            response.write(Strings.escapeMarkup((title != null) ? title.toString() : ""));
            response.write("\"/>");
        }
    }

    /**
     * Get the tag body. Used by the Unnested DatePicker.
     * @return html
     */
    public StringBuilder getTagBody()
    {
        final StringBuilder ret = new StringBuilder();
        ret.append("<span class=\"yui-skin-sam\">&nbsp;<span style=\"");
        if (renderOnLoad()) {
            ret.append("display:block;");
        } else {
            ret.append("display:none;").append("position:absolute;");
        }
        ret.append("z-index: 99999;\" id=\"")
            .append(getEscapedComponentMarkupId())
            .append("Dp\"></span></span>");
        return ret;
    }

    @Override
    protected boolean enableMonthYearSelection()
    {
        return true;
    }
}
