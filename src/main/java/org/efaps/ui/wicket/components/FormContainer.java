/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.ui.wicket.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior.AjaxFormSubmitter;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.protocol.http.servlet.MultipartServletWebRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.efaps.db.Context;
import org.efaps.ui.wicket.EFapsSession.FileParameter;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.date.IDateListener;
import org.efaps.ui.wicket.components.gridx.GridXComponent;
import org.efaps.ui.wicket.components.values.DropDownField;
import org.efaps.ui.wicket.components.values.IValueConverter;
import org.efaps.ui.wicket.models.field.IUIElement;
import org.efaps.ui.wicket.models.field.set.UIFieldSet;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.DropDownOption;
import org.efaps.ui.wicket.models.objects.ISelectedRowObject;
import org.efaps.ui.wicket.models.objects.UIFieldForm;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.Element;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
import org.efaps.ui.wicket.models.objects.UIForm.FormRow;
import org.efaps.ui.wicket.models.objects.grid.GridRow;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.request.EFapsRequest;
import org.efaps.ui.wicket.request.EFapsRequestParametersAdapter;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for a form. Needed for file upload.
 *
 * @author The eFaps Team
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
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FormContainer.class);

    /**
     * Url for the action that must be called.
     */
    private String actionUrl;

    /**
     * Set contains the date components of this formpanel.
     */
    private final Set<DateTimePanel> dateComponents = new HashSet<>();

    /**
     * Set contains value Converters.
     */
    private final Set<IValueConverter> valueConverters = new LinkedHashSet<>();

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
            if (getPage().getDefaultModelObject() instanceof AbstractUIObject
                            && !((AbstractUIObject) getPage().getDefaultModelObject()).isSearchMode()) {
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
     * Add a date component.
     *
     * @param _dateTimePanel date picker
     */
    @Override
    public void addDateComponent(final DateTimePanel _dateTimePanel)
    {
        this.dateComponents.add(_dateTimePanel);
    }

    /**
     * Getter method for instance variable {@link #dateComponents}.
     *
     * @return instance variable {@link #dateComponents}
     */
    @Override
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
                    int i = -1;
                    if (entry.getValue().size() > 1) {
                        i = 0;
                    }
                    for (final FileItem fileItem : entry.getValue()) {
                        final String key = i > -1 ? entry.getKey() + "_" + i : entry.getKey();
                        final FileParameter parameter = new FileParameter(key, fileItem);
                        Context.getThreadContext().getFileParameters().put(key, parameter);
                        if (i > -1) {
                            i++;
                        }
                    }
                }

                final Map<String, String[]> parameters = new HashMap<>();
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

    @Override
    public void process(final IFormSubmitter _submittingComponent)
    {
        // if their is a GridXComponent convert the ids to oids
        visitChildren(GridXComponent.class, (_gridX, _visit) -> {
            try {
                final EFapsRequestParametersAdapter parameters = (EFapsRequestParametersAdapter) getRequest()
                                .getRequestParameters();
                final List<StringValue> selectedRows = parameters.getParameterValues("selectedRow");
                if (CollectionUtils.isNotEmpty(selectedRows)) {
                    final List<StringValue> newValues = new ArrayList<>();
                    for (final StringValue value : selectedRows) {
                        final UIGrid uiGrid = (UIGrid) _gridX.getDefaultModelObject();
                        final GridRow row = uiGrid.getValues().get(value.toInt());
                        newValues.add(StringValue.valueOf(row.getInstance().getOid()));
                    }
                    parameters.setParameterValues("selectedRow", newValues);
                }
            } catch (final StringValueConversionException | EFapsException e) {
                FormContainer.LOG.error("Catched exeption", e);
            }
        });

        // for a dropdown add the previous value as a parameter
        if (_submittingComponent instanceof AjaxFormSubmitter && ((AjaxFormSubmitter) _submittingComponent)
                        .getFormSubmittingComponent() != null && ((AjaxFormSubmitter) _submittingComponent)
                                        .getFormSubmittingComponent() instanceof DropDownField) {
            final Object object = ((DropDownField) ((AjaxFormSubmitter) _submittingComponent)
                            .getFormSubmittingComponent()).getDefaultModelObject();
            if (object instanceof DropDownOption) {
                final String key = ((DropDownField) ((AjaxFormSubmitter) _submittingComponent)
                                .getFormSubmittingComponent()).getInputName();
                ((EFapsRequestParametersAdapter) ((EFapsRequest) RequestCycle.get().getRequest())
                                .getRequestParameters()).addParameterValue(key + "_eFapsPrevious",
                                                ((DropDownOption) object).getValue());
            }
        }
        if (getPage().getDefaultModelObject() instanceof ISelectedRowObject
                        && ((ISelectedRowObject) getPage().getDefaultModelObject()).hasSelected()) {
            final EFapsRequestParametersAdapter parameters = (EFapsRequestParametersAdapter) getRequest()
                            .getRequestParameters();
            if (parameters.getParameterValues("selectedRow") == null) {
                parameters.setParameterValues("selectedRow",
                                ((ISelectedRowObject) getPage().getDefaultModelObject()).getSelected());
            }
        }
        super.process(_submittingComponent);
        // it must be ensured that the counter for sets is rested or we have big problems
        resetSetCounter();
    }

    /**
     * Reset the counters for sets.
     */
    private void resetSetCounter()
    {
        if (getPage().getDefaultModelObject() instanceof UIForm) {
            for (final Element element : ((UIForm) getPage().getDefaultModelObject()).getElements()) {
                if (element.getType().equals(ElementType.FORM)) {
                    final Iterator<FormRow> iter = ((UIForm.FormElement) element.getElement()).getRowModels();
                    while (iter.hasNext()) {
                        final FormRow row = iter.next();
                        for (final IUIElement uiElement : row.getValues()) {
                            if (uiElement instanceof UIFieldSet) {
                                ((UIFieldSet) uiElement).resetIndex();
                            }
                        }
                    }
                } else if (element.getType().equals(ElementType.SUBFORM)) {
                    for (final Element nElement : ((UIFieldForm) element.getElement()).getElements()) {
                        if (nElement.getType().equals(ElementType.FORM)) {
                            final Iterator<FormRow> iter = ((UIForm.FormElement) nElement.getElement()).getRowModels();
                            while (iter.hasNext()) {
                                final FormRow row = iter.next();
                                for (final IUIElement uiElement : row.getValues()) {
                                    if (uiElement instanceof UIFieldSet) {
                                        ((UIFieldSet) uiElement).resetIndex();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
