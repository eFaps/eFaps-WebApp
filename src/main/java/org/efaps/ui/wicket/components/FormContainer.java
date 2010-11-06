/*
 * Copyright 2003 - 2010 The eFaps Team
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

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitListener;
import org.apache.wicket.util.string.JavascriptUtils;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIForm;

/**
 * Class for a form. Needed for file upload.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FormContainer
    extends Form<Object>
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
     * Is this form a used to upload a file.
     */
    private boolean fileUpload = false;

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
        final Object uiObject = getPage().getDefaultModelObject();
        if (uiObject instanceof UIForm && ((UIForm) uiObject).isFileUpload()
                        && (((UIForm) uiObject).isCreateMode() || ((UIForm) uiObject).isCreateMode())) {
            setMultiPart(true);
            setMaxSize(getApplication().getApplicationSettings().getDefaultMaximumUploadSize());
        }
        super.onComponentTag(_tag);
        this.actionUrl = urlFor(IFormSubmitListener.INTERFACE).toString();
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
    public String getActionUrl()
    {
        return this.actionUrl;
    }

    /**
     * On submit it is checked if it is a file upload from and in case that it
     * is the listeners executed.
     *
     * @see org.apache.wicket.markup.html.form.Form#onSubmit()
     */
    @Override
    protected void onSubmit()
    {
        super.onSubmit();
        if (this.fileUpload) {
            final List<IFileUploadListener> uploadListeners = this.getBehaviors(IFileUploadListener.class);
            for (final IFileUploadListener listener : uploadListeners) {
                listener.onSubmit();
            }
        }
    }

    /**
     * This is the getter method for the instance variable {@link #fileUpload}.
     *
     * @return value of instance variable {@link #fileUpload}
     */
    public boolean isFileUpload()
    {
        return this.fileUpload;
    }

    /**
     * This is the setter method for the instance variable {@link #fileUpload}.
     *
     * @param _fileUpload the fileUpload to set
     */
    public void setFileUpload(final boolean _fileUpload)
    {
        this.fileUpload = _fileUpload;
    }

    /**
     * Overwritten due to the reason that the mulitpart is handelt using
     * the Context.
     * @see org.apache.wicket.markup.html.form.Form#handleMultiPart()
     * @return true
     */
    @Override
    protected boolean handleMultiPart()
    {
        return true;
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
        JavascriptUtils.writeJavascript(getResponse(), bldr.toString());
    }
}
