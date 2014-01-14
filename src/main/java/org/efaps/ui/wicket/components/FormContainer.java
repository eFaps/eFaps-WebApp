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

package org.efaps.ui.wicket.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.upload.FileItem;
import org.efaps.db.Context;
import org.efaps.ui.wicket.EFapsSession.FileParameter;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.date.IDateListener;
import org.efaps.ui.wicket.components.values.IValueConverter;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * Class for a form. Needed for file upload.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FormContainer
    extends Form<Object>
    implements IDateListener
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Url for the action that must be called.
     */
    private String actionUrl;

    /**
     * Set contains the date components of this formpanel.
     */
    private final Set<DateTimePanel> dateComponents = new HashSet<DateTimePanel>();

    /**
     * Set contains value Converters.
     */
    private final Set<IValueConverter> valueConverters = new LinkedHashSet<IValueConverter>();

    /**
     * Constructor setting the wicket id of this component.
     *
     * @param _wicketId wicket id of this component
     */
    public FormContainer(final String _wicketId)
    {
        super(_wicketId);
    }

    /**
     * On component tag.
     *
     * @param _tag the tag
     *
     * @see org.apache.wicket.markup.html.form.Form
     *      #onComponentTag(org.apache.wicket.markup.ComponentTag)
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        if (this.actionUrl == null) {
            this.actionUrl = urlFor(getRequestCycle().getActiveRequestHandler()).toString();
        }
        super.onComponentTag(_tag);
        if (getPage().getDefaultModelObject() != null) {
            // only on SearchMode we want normal submit, in any other case we
            // use AjaxSubmit
            if (!((AbstractUIObject) getPage().getDefaultModelObject()).isSearchMode()) {
                //_tag.put("onSubmit", "return false;");
                _tag.put("action", "");
            }
        }
    }

    /**
     * This is the getter method for the instance variable {@link #actionUrl}.
     *
     * @return value of instance variable {@link #actionUrl}
     */
    @Override
    public String getActionUrl()
    {
        return this.actionUrl;
    }

    /**
     * If a default IFormSubmittingComponent was set on this form, this method
     * will be called to render an extra field with an invisible style so that
     * pressing enter in one of the textfields will do a form submit using
     * this component.
     * The method is overwritten to correct some script problems with the
     * default behavior from Firefox, that actually sends and reloads the form.
     *
     * @param _markupStream The markup stream
     * @param _openTag The open tag for the body
     */
    @Override
    protected void appendDefaultButtonField(final MarkupStream _markupStream,
                                            final ComponentTag _openTag)
    {
        final StringBuilder divBldr = new StringBuilder();
        // div that is not visible (but not display:none either)
        divBldr.append("<div style=\"width:0px;height:0px;position:absolute;left:-100px;top:-100px;overflow:hidden\">");

        // add an empty textfield (otherwise IE doesn't work)
        divBldr.append("<input type=\"text\" autocomplete=\"false\"/>");

        final Component submittingComponent = (Component) getDefaultButton();
        divBldr.append("<input type=\"submit\" onclick=\" var b=Wicket.$('");
        divBldr.append(submittingComponent.getMarkupId());
        divBldr.append("'); if (typeof(b.onclick) != 'undefined') " + "{ b.onclick();  }" + " \" ");
        divBldr.append(" /></div>");
        getResponse().write(divBldr);
        // this trick prevents that the default behavior is executed
        final StringBuilder bldr = new StringBuilder();
        bldr.append("Wicket.Event.add(Wicket.$('").append(this.getMarkupId())
            .append("'), 'submit', function (evt){ evt.preventDefault();});");
        JavaScriptUtils.writeJavaScript(getResponse(), bldr.toString());
    }

    /**
     * Add a date component.
     *
     * @param _dateTimePanel date picker
     */
    public void addDateComponent(final DateTimePanel _dateTimePanel)
    {
        this.dateComponents.add(_dateTimePanel);
    }

    /**
     * Getter method for instance variable {@link #dateComponents}.
     *
     * @return instance variable {@link #dateComponents}
     */
    public Set<DateTimePanel> getDateComponents()
    {
        return this.dateComponents;
    }

    /**
     * @param _valueConverter value Converter to be added
     */
    public void addValueConverter(final IValueConverter _valueConverter)
    {
        this.valueConverters.add(_valueConverter);
    }

    /**
     * Getter method for the instance variable {@link #valueConverters}.
     *
     * @return value of instance variable {@link #valueConverters}
     */
    public Set<IValueConverter> getValueConverters()
    {
        return this.valueConverters;
    }

    /**
     * @param _valueConverter valueConverter to be removed
     */
    public void removeValueConverter(final IValueConverter _valueConverter)
    {
        this.valueConverters.remove(_valueConverter);
    }

    /**
     * Handle the multipart to store the files and parameters in the context also.
     * @return true if multipart
     */
    @Override
    protected boolean handleMultiPart()
    {
        final boolean ret = super.handleMultiPart();
        try {
            if (isMultiPart() && getRequest() instanceof MultipartServletWebRequest) {
                for (final Entry<String, List<FileItem>> entry : ((MultipartServletWebRequest) getRequest()).getFiles()
                                .entrySet()) {
                    for (final FileItem fileItem : entry.getValue()) {
                        final FileParameter parameter = new FileParameter(entry.getKey(), fileItem);
                        Context.getThreadContext().getFileParameters().put(entry.getKey(), parameter);
                    }
                }

                final Map<String, String[]> parameters = new HashMap<String, String[]>();
                final IRequestParameters reqPara = getRequest().getRequestParameters();
                for (final String name : reqPara.getParameterNames()) {
                    final List<StringValue> values = reqPara.getParameterValues(name);
                    final String[] valArray = new String[values.size()];
                    int i = 0;
                    for (final StringValue value : values) {
                        valArray[i] = value.toString();
                        i++;
                    }
                    parameters.put(name, valArray);
                }
                Context.getThreadContext().getParameters().putAll(parameters);
            }
        } catch (final EFapsException e) {
            throw new RestartResponseAtInterceptPageException(new ErrorPage(e));
        }
        return ret;
    }
}
