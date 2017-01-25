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

package org.efaps.ui.wicket.components.footer;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.table.TableModel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ValidationErrorFeedback;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.db.Context;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.autocomplete.AutoCompleteComboBox;
import org.efaps.ui.wicket.components.datagrid.SetDataGrid;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.values.DropDownField;
import org.efaps.ui.wicket.components.values.ErrorMessageResource;
import org.efaps.ui.wicket.components.values.IFieldConfig;
import org.efaps.ui.wicket.components.values.IValueConverter;
import org.efaps.ui.wicket.models.field.IFilterable;
import org.efaps.ui.wicket.models.field.IUIElement;
import org.efaps.ui.wicket.models.field.set.UIFieldSet;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.models.objects.IModeObject;
import org.efaps.ui.wicket.models.objects.IPageObject;
import org.efaps.ui.wicket.models.objects.IWizardElement;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.UIFieldForm;
import org.efaps.ui.wicket.models.objects.UIFieldTable;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.Element;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
import org.efaps.ui.wicket.models.objects.UIForm.FormElement;
import org.efaps.ui.wicket.models.objects.UIForm.FormRow;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.models.objects.UIWizardObject;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.request.EFapsRequestParametersAdapter;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;
import org.efaps.ui.wicket.util.ParameterUtil;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class renders the footer in a form. It is responsible for performing the
 * actions like executing the related esjp etc.
 *
 * @author The eFaps Team
 */
public class AjaxSubmitCloseButton
    extends AbstractFooterButton<ICmdUIObject>
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AjaxSubmitCloseButton.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Has this form been already validated.
     */
    private boolean validated = false;

    /**
     * Instantiates a new ajax submit close button.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     * @param _reference the reference
     * @param _label the label
     */
    public AjaxSubmitCloseButton(final String _wicketId,
                                 final IModel<ICmdUIObject> _model,
                                 final EFapsContentReference _reference,
                                 final String _label)
    {
       super(_wicketId, _model, _reference, _label);
    }

    /**
     * Setter method for instance variable {@link #validated}.
     *
     * @param _validated value for instance variable {@link #validated}
     */
    public void setValidated(final boolean _validated)
    {
        this.validated = _validated;
    }

    @Override
    protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
    {
        super.updateAjaxAttributes(_attributes);
        _attributes.getAjaxCallListeners().add(new AjaxCallListener().onBefore(DojoWrapper.require(new StringBuilder()
            .append("topic.publish(\"eFaps/submitClose")
            .append("\");\n"), DojoClasses.topic)));
    }

    /**
     * On submit the action must be done.
     *
     * @param _target AjaxRequestTarget
     */
    @Override
    public void onRequest(final AjaxRequestTarget _target)
    {
        AjaxSubmitCloseButton.LOG.trace("entering onSubmit");
        final String[] oids = ParameterUtil.parameter2Array(getRequest().getRequestParameters(), "selectedRow");
        final Map<String, String[]> others = new HashMap<>();
        others.put("selectedRow", oids);

        final ICmdUIObject cmdUIObject = (ICmdUIObject) getDefaultModelObject();
        final List<Classification> classifications = new ArrayList<>();
        try {
            if (cmdUIObject instanceof UIForm) {
                final UIForm uiform = (UIForm) cmdUIObject;
                others.putAll(uiform.getNewValues());
                // if the form contains classifications, they are added to a list and passed on to the esjp
                if (uiform.isClassified()) {
                    for (final Element element : uiform.getElements()) {
                        if (element.getType().equals(ElementType.SUBFORM)) {
                            final UIFieldForm uifieldform = (UIFieldForm) element.getElement();
                            classifications.add((Classification) Type.get(uifieldform.getClassificationUUID()));
                        }
                    }
                }
            }
            if (convertDateFieldValues(_target) && convertFieldValues(_target)
                            && checkForRequired(_target) && validateFieldValues(_target)
                            && validateForm(_target, others, classifications)) {
                if (executeEvents(_target, others, classifications)) {
                    // to be able to see the changes the context must be commited and reopened
                    ((EFapsSession) Session.get()).saveContext();

                    if (cmdUIObject.getCommand().getTargetCommand() != null) {
                        final AbstractCommand targetCmd = cmdUIObject.getCommand().getTargetCommand();
                        final AbstractUIPageObject newUIObject;
                        if (targetCmd.getTargetTable() != null) {
                            newUIObject = new UITable(cmdUIObject.getCommand().getTargetCommand().getUUID(),
                                            cmdUIObject.getInstance().getOid());
                        } else {
                            final PagePosition pp = cmdUIObject instanceof IPageObject
                                            ? ((IPageObject) cmdUIObject).getPagePosition() : PagePosition.CONTENT;
                            newUIObject = new UIForm(cmdUIObject.getCommand().getTargetCommand().getUUID(),
                                            cmdUIObject.getInstance().getOid())
                                            .setPagePosition(pp);
                        }
                        final UIWizardObject wizard = new UIWizardObject((IWizardElement) newUIObject);
                        wizard.addParameters((IWizardElement) cmdUIObject, Context.getThreadContext().getParameters());
                        wizard.insertBefore((IWizardElement) cmdUIObject);
                        newUIObject.setWizard(wizard);
                        newUIObject.setPartOfWizardCall(true);
                        newUIObject.setRenderRevise(cmdUIObject.getCommand().isTargetCmdRevise());
                        if (cmdUIObject.getCommand().isSubmit()) {
                            newUIObject.setSubmit(true);
                            //newUIObject.setCallingCommandUUID(this.uiObject.getCallingCommandUUID());
                        }
                        final FooterPanel footer = findParent(FooterPanel.class);
                        final ModalWindowContainer modal = footer.getModalWindow();
                        final AbstractContentPage page;
                        if (targetCmd.getTargetTable() != null) {
                            page = new TablePage(Model.of((UITable) newUIObject), modal);
                        } else {
                            page = new FormPage(Model.of((UIForm) newUIObject), modal);
                        }
                        if (cmdUIObject.getCommand().isTargetShowFile()) {
                            page.getDownloadBehavior().initiate();
                        }
                        getRequestCycle().setResponsePage(page);
                    } else {
                        final FooterPanel footer = findParent(FooterPanel.class);
                        // if inside a modal
                        if (PagePosition.CONTENTMODAL.equals(((IPageObject) getPage().getDefaultModelObject())
                                        .getPagePosition()) || PagePosition.TREEMODAL.equals(((IPageObject) getPage()
                                                        .getDefaultModelObject()).getPagePosition())) {
                            footer.getModalWindow().setReloadChild(!cmdUIObject.getCommand().isNoUpdateAfterCmd());
                            footer.getModalWindow().setTargetShowFile(cmdUIObject.getCommand().isTargetShowFile());
                            footer.getModalWindow().close(_target, cmdUIObject);
                        }
                        footer.setSuccess(true);
                    }
                }
            }
        } catch (final EFapsException e) {
            final ModalWindowContainer modal = ((AbstractContentPage) getPage()).getModal();
            modal.setPageCreator(new ModalWindow.PageCreator() {

                private static final long serialVersionUID = 1L;

                @Override
                public Page createPage()
                {
                    return new ErrorPage(e);
                }
            });
            modal.show(_target);
        }
    }

    /**
     * Method used to convert the date value from the ui in date values for
     * eFaps.
     * @param _target AjaxRequestTarget
     * @return true if converted successfully, else false
     * @throws EFapsException on error
     */
    private boolean convertDateFieldValues(final AjaxRequestTarget _target)
        throws EFapsException
    {
        AjaxSubmitCloseButton.LOG.trace("entering convertDateFieldValues");
        boolean ret = true;
        final StringBuilder html = new StringBuilder();
        html.append("<table class=\"eFapsValidateFieldValuesTable\">");

        final EFapsRequestParametersAdapter parameters = (EFapsRequestParametersAdapter)
                        getRequest().getRequestParameters();
        final Set<String> names = parameters.getParameterNames();
        for (final DateTimePanel datepicker : ((FormContainer) getForm()).getDateComponents()) {
            if (names.contains(datepicker.getDateFieldName())) {
                final List<StringValue> date = parameters.getParameterValues(datepicker.getDateFieldName());
                final List<StringValue> hour = parameters.getParameterValues(datepicker.getHourFieldName());
                final List<StringValue> minute = parameters.getParameterValues(datepicker.getMinuteFieldName());
                final List<StringValue> ampm = parameters.getParameterValues(datepicker.getAmPmFieldName());
                ret = datepicker.validate(date, hour, minute, ampm, html);
                if (ret) {
                    parameters.setParameterValues(datepicker.getFieldName(),
                                datepicker.getDateAsString(date, hour, minute, ampm));
                } else {
                    break;
                }
            }
        }
        if (!ret) {
            html.append("</table>");
            showDialog(_target, html.toString(), true, false);
        }
        return ret;
    }

    /**
     * Method used to convert the values from the ui in values for
     * eFaps.
     * @param _target AjaxRequestTarget
     * @return true if converted successfully, else false
     * @throws EFapsException on error
     */
    private boolean convertFieldValues(final AjaxRequestTarget _target)
        throws EFapsException
    {
        AjaxSubmitCloseButton.LOG.trace("entering convertFieldValues");
        final EFapsRequestParametersAdapter parameters = (EFapsRequestParametersAdapter) getRequest()
                        .getRequestParameters();
        final FormContainer frmContainer = (FormContainer) getForm();
        for (final IValueConverter converter : frmContainer.getValueConverters()) {
            converter.convertValue(parameters);
        }
        return true;
    }

    /**
     * Method is not used, but needed from the api.
     *
     * @param _target AjaxRequestTarget
     */

    @Override
    public void onError(final AjaxRequestTarget _target)
    {
        final FeedbackCollector collector = new FeedbackCollector(getForm().getPage());
        final List<FeedbackMessage> msgs = collector.collect();
        final ErrorMessageResource msgResource = new ErrorMessageResource();
        final StringBuilder html = new StringBuilder()
                        .append("<table class=\"eFapsValidateFieldValuesTable\">");
        for (final FeedbackMessage msg : msgs) {
            if (!(msg.getReporter() instanceof Form)) {
                if (msg.getReporter() instanceof AutoCompleteComboBox) {
                    final StringBuilder js = new StringBuilder()
                                .append("domClass.add(dom.byId('").append(msg.getReporter().getMarkupId())
                                .append("').parentNode, 'invalid');");
                    _target.prependJavaScript(DojoWrapper.require(js, DojoClasses.dom, DojoClasses.domClass));
                } else {
                    msg.getReporter().add(AttributeModifier.append("class", "invalid"));
                    _target.add(msg.getReporter());
                }
            }
            Serializable warn = null;
            if (msg.getMessage() instanceof ValidationErrorFeedback) {
                // look if a message was set
                warn = ((ValidationErrorFeedback) msg.getMessage()).getMessage();
                // still no message, create one
                if (warn == null) {
                    warn = ((ValidationErrorFeedback) msg.getMessage()).getError().getErrorMessage(msgResource);
                }
            } else {
                warn = String.valueOf(msg.getMessage());
            }
            html.append("<tr>");
            if (msg.getReporter() instanceof IFieldConfig) {
                html.append("<td>")
                    .append(((IFieldConfig) msg.getReporter()).getFieldConfig().getLabel())
                    .append(":</td><td>")
                    .append(warn).append("</td>");
            } else {
                html.append("<td colspan=\"2\">")
                    .append(warn).append("</td></tr>");
            }
            msg.getReporter().getFeedbackMessages().clear();
        }
        html.append("</table>");
        showDialog(_target, html.toString(), true, false);

        // after every commit the fieldset must be resteted
        getForm().getPage().visitChildren(SetDataGrid.class, new IVisitor<SetDataGrid, Void>()
        {

            @Override
            public void component(final SetDataGrid _setDataGrid,
                                  final IVisit<Void> _visit)
            {
                final UIFieldSet fieldSet = (UIFieldSet) _setDataGrid.getDefaultModelObject();
                fieldSet.resetIndex();
            }
        });
        getForm().getPage().visitChildren(DropDownField.class, new IVisitor<DropDownField, Void>()
        {

            @Override
            public void component(final DropDownField _dropDown,
                                  final IVisit<Void> _visit)
            {
                _dropDown.setConverted(false);
            }
        });
    }

    /**
     * Execute the events which are related to CommandAbstract calling the Form.
     *
     * @param _target   AjaxRequestTarget to be used in the case a ModalPage
     *                  should be called
     * @param _other    Parameters to be passed on to the Event, defined as
     *                  {@link org.efaps.admin.event.Parameter.ParameterValues.OTHERS}
     * @param _classifications  List of uuid of classifcationsto be passed on
     *                  to the Event, defined as
     *                  {@link org.efaps.admin.event.Parameter.ParameterValues.OTHERS}
     * @return true if the events where executed successfully, otherwise false
     * @throws EFapsException on error
     */
    private boolean executeEvents(final AjaxRequestTarget _target,
                                  final Map<String, String[]> _other,
                                  final List<Classification> _classifications)
        throws EFapsException
    {
        AjaxSubmitCloseButton.LOG.trace("entering executeEvents");
        boolean ret = true;
        ;
        final ICmdUIObject cmdUIObject = (ICmdUIObject) getPage().getDefaultModelObject();
        TargetMode mode;
        if (cmdUIObject instanceof IModeObject) {
            mode = ((IModeObject) cmdUIObject).getMode();
        } else {
            mode = TargetMode.UNKNOWN;
        }
        final List<Object> tuplets = new ArrayList<>();
        tuplets.add(ParameterValues.OTHERS);
        tuplets.add(_other);
        tuplets.add(ParameterValues.ACCESSMODE);
        tuplets.add(mode);

        if (cmdUIObject instanceof AbstractUIPageObject) {
            tuplets.add(ParameterValues.OIDMAP4UI);
            tuplets.add(((AbstractUIPageObject) getForm().getPage().getDefaultModelObject()).getUiID2Oid());
        }
        if (_classifications.size() > 0) {
            tuplets.add(ParameterValues.CLASSIFICATIONS);
            tuplets.add(_classifications);
        }

        final List<Return> returns = cmdUIObject.executeEvents(EventType.UI_COMMAND_EXECUTE,tuplets.toArray());

        for (final Return oneReturn : returns) {
            if (oneReturn.get(ReturnValues.TRUE) == null && !oneReturn.isEmpty()
                            && oneReturn.get(ReturnValues.VALUES) instanceof String) {
                boolean sniplett = false;
                String key = (String) oneReturn.get(ReturnValues.VALUES);
                if (key == null) {
                    key = (String) oneReturn.get(ReturnValues.SNIPLETT);
                    sniplett = true;
                }
                showDialog(_target, key, sniplett, false);

                ret = false;
                break;
            } else if (oneReturn.get(ReturnValues.TRUE) != null && !oneReturn.isEmpty()
                            && cmdUIObject.getCommand().isTargetShowFile()) {
                if (oneReturn.get(ReturnValues.VALUES) instanceof File) {
                    final File file = (File) oneReturn.get(ReturnValues.VALUES);
                    ((EFapsSession) getSession()).setFile(file);
                }
            }
            if (oneReturn.get(ReturnValues.INSTANCE) != null) {
                //cmdUIObject. .setInstanceKey(((Instance) oneReturn.get(ReturnValues.INSTANCE)).getKey());
            }
        }

        if (cmdUIObject instanceof AbstractUIPageObject && ((AbstractUIPageObject) cmdUIObject).isOpenedByPicker()) {
            final PageReference pageRef = ((AbstractContentPage) getForm().getPage()).getCalledByPageReference();
            ((AbstractUIPageObject) cmdUIObject).getPicker().executeEvents(EventType.UI_COMMAND_EXECUTE,
                            ParameterValues.OTHERS, _other);
            ((AbstractUIObject) pageRef.getPage().getDefaultModelObject()).setPicker(
                            ((AbstractUIPageObject) cmdUIObject).getPicker());
        }
        return ret;
    }

    /**
     * Method to validate the values for fields.
     * @param _target   AjaxRequestTarget
     * @return true if validation was valid, else false
     * @throws EFapsException on error
     */
    private boolean validateFieldValues(final AjaxRequestTarget _target)
        throws EFapsException
    {
        AjaxSubmitCloseButton.LOG.trace("entering validateFieldValues");
        boolean ret = true;
        final ICmdUIObject uiobject = (ICmdUIObject) getPage().getDefaultModelObject();
        final StringBuilder html = new StringBuilder();
        html.append("<table class=\"eFapsValidateFieldValuesTable\">");
        if (uiobject instanceof UIForm) {
            final UIForm uiform = (UIForm) uiobject;
            ret = evalFormElement(_target, html, uiform);
        }
        if (!ret) {
            html.append("</table>");
            showDialog(_target, html.toString(), true, false);
        }
        return ret;
    }

    /**
     * Recursive method to validate the elements of the form.
     * @param _target   AjaxRequestTarget
     * @param _html     StringBuilder for the warning message
     * @param _uiform   UIForm to start the validation
     * @return true if validation was valid, else false
     * @throws EFapsException on error
     */
    private boolean evalFormElement(final AjaxRequestTarget _target,
                                    final StringBuilder _html,
                                    final UIForm _uiform)
        throws EFapsException
    {
        AjaxSubmitCloseButton.LOG.trace("entering evalFormElement");
        boolean ret = true;
        for (final Element element : _uiform.getElements()) {
            if (element.getType().equals(ElementType.FORM)) {
                final FormElement formElement = (FormElement) element.getElement();
                for (final Iterator<FormRow> uiRowIter = formElement.getRowModels(); uiRowIter.hasNext();) {
                    for (final IUIElement object : uiRowIter.next().getValues()) {

                    }
                }
            } else if (element.getType().equals(ElementType.SUBFORM)) {
                final UIFieldForm uiFieldForm = (UIFieldForm) element.getElement();
                final boolean tmp = evalFormElement(_target, _html, uiFieldForm);
                ret = ret ? tmp : ret;
            } else if (element.getType().equals(ElementType.TABLE)) {
                final UIFieldTable uiFieldTable = (UIFieldTable) element.getElement();
                final List<UITableHeader> headers = uiFieldTable.getHeaders();
                for (final UIRow uiRow : uiFieldTable.getValues()) {
                    uiRow.getUserinterfaceId();
                    final Iterator<UITableHeader> headerIter = headers.iterator();
                    for (final IFilterable filterable : uiRow.getCells()) {
                        headerIter.next();

                        }
                    }
                }
            }

        return ret;
    }

    /**
     * Executes the Validation-Events related to the CommandAbstract which
     * called this Form.
     *
     * @param _target   AjaxRequestTarget to be used in the case a ModalPage
     *                  should be called
     * @param _other    other parameters
     * @param _classifications lis of classifications
     * @return true if the Validation was valid, otherwise false
     * @throws EFapsException on error
     */
    private boolean validateForm(final AjaxRequestTarget _target,
                                 final Map<String, String[]> _other,
                                 final List<Classification> _classifications)
        throws EFapsException
    {
        AjaxSubmitCloseButton.LOG.trace("entering validateForm");
        boolean ret = true;
        if (!this.validated) {
            TargetMode mode;
            if (getForm().getPage().getDefaultModelObject() instanceof IModeObject) {
                mode = ((IModeObject) getForm().getPage().getDefaultModelObject()).getMode();
            } else {
                mode = TargetMode.UNKNOWN;
            }

            final List<Object> tuplets = new ArrayList<>();
            tuplets.add(ParameterValues.OTHERS);
            tuplets.add(_other);
            tuplets.add(ParameterValues.ACCESSMODE);
            tuplets.add(mode);

            if (getPage().getDefaultModelObject() instanceof AbstractUIPageObject) {
                tuplets.add(ParameterValues.OIDMAP4UI);
                tuplets.add(((AbstractUIPageObject) getForm().getPage().getDefaultModelObject()).getUiID2Oid());
            }
            if (_classifications.size() > 0) {
                tuplets.add(ParameterValues.CLASSIFICATIONS);
                tuplets.add(_classifications);
            }

            final List<Return> returns = ((ICmdUIObject) getPage().getDefaultModelObject())
                            .executeEvents(EventType.UI_VALIDATE, tuplets.toArray());

            boolean goOn = true;
            boolean sniplett = false;
            String key = "";
            for (final Return oneReturn : returns) {
                if (oneReturn.get(ReturnValues.VALUES) != null || oneReturn.get(ReturnValues.SNIPLETT) != null) {
                    if (oneReturn.get(ReturnValues.VALUES) != null) {
                        key = key + (String) oneReturn.get(ReturnValues.VALUES);
                    } else {
                        key = key + (String) oneReturn.get(ReturnValues.SNIPLETT);
                        sniplett = true;
                    }
                    ret = false;
                    if (oneReturn.get(ReturnValues.TRUE) == null) {
                        goOn = false;
                    }
                } else {
                    if (oneReturn.get(ReturnValues.TRUE) == null) {
                        ret = false;
                        // that is the case if it is wrong configured!
                    }
                }
            }
            if (!ret) {
                showDialog(_target, key, sniplett, goOn);
            }
        }
        return ret;
    }

    /**
     * Method checking if the mandatory field of the Form are filled with a value,
     * and if not opens a WarnDialog and marks the fields in the Form via Ajax.
     *
     * @param _target RequestTarget used for this Request
     * @return true if all mandatory fields are filled, else false
     */
    private boolean checkForRequired(final AjaxRequestTarget _target)
    {
        AjaxSubmitCloseButton.LOG.trace("entering checkForRequired");
        boolean ret = true;
        if (!(getForm().getParent().getDefaultModel() instanceof TableModel)) {
            final IRequestParameters parameters = getRequest().getRequestParameters();
            final List<FormPanel> panels = getFormPanels();
            for (final FormPanel panel : panels) {
                for (final Entry<String, Label> entry : panel.getRequiredComponents().entrySet()) {
                    final StringValue value = parameters.getParameterValue(entry.getKey());
                    if (value.isNull() || value.isEmpty()) {
                        final Label label = entry.getValue();
                        label.add(AttributeModifier.replace("class", "eFapsFormLabelRequiredForce"));
                        _target.add(label);
                        ret = false;
                    }
                }
            }
            if (!ret) {
                showDialog(_target, "MandatoryDialog", false, false);
            }
        }
        return ret;
    }

    /**
     * Method to get the FormPanel of this Page.
     *
     * @return FormPanel
     */
    private List<FormPanel> getFormPanels()
    {
        AjaxSubmitCloseButton.LOG.trace("entering getFormPanels");
        final List<FormPanel> ret = new ArrayList<>();
        final Iterator<?> iterator = getForm().iterator();
        while (iterator.hasNext()) {
            final Object object = iterator.next();
            if (object instanceof WebMarkupContainer) {
                final Iterator<?> iterator2 = ((WebMarkupContainer) object).iterator();
                while (iterator2.hasNext()) {
                    final Object object2 = iterator2.next();
                    if (object2 instanceof FormPanel) {
                        ret.add((FormPanel) object2);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Shows a modal DialogPage.
     *
     * @param _target       AjaxRequestTarget to be used for opening the modal
     *                      DialogPage
     * @param _key the      Key to get the DBProperties from the eFapsDataBaase or a
     *                      code sniplett
     * @param _isSniplett   is the parameter _key a key to a property or a
     *                       sniplett
     * @param _goOnButton   should a button to go on be rendered
     */
    private void showDialog(final AjaxRequestTarget _target,
                            final String _key,
                            final boolean _isSniplett,
                            final boolean _goOnButton)
    {
        final ModalWindowContainer modal = ((AbstractContentPage) getPage()).getModal();

        modal.setInitialWidth(350);
        modal.setInitialHeight(200);

        modal.setPageCreator(new ModalWindow.PageCreator() {

            private static final long serialVersionUID = 1L;

            @Override
            public Page createPage()
            {
                return new DialogPage(((AbstractContentPage) getPage()).getPageReference(),
                                _key, _isSniplett, _goOnButton);
            }
        });

        if (_goOnButton) {
            modal.setWindowClosedCallback(new WindowClosedCallback()
            {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClose(final AjaxRequestTarget _target)
                {
                    if (AjaxSubmitCloseButton.this.validated) {
                        _target.appendJavaScript(getExecuteScript());
                    }
                }
            });
        }
        modal.show(_target);
    }

    /**
     * @return script that can be used to execute this Ajax behavior.
     */
    protected CharSequence getExecuteScript()
    {
        final CharSequence script = visitChildren(ButtonLink.class, new IVisitor<ButtonLink<?>, CharSequence>()
        {

            @Override
            public void component(final org.efaps.ui.wicket.components.button.AjaxButton.ButtonLink<?> _btl,
                                  final IVisit<CharSequence> _visit)
            {
                final AjaxFormSubmitBehavior behavior = _btl.getBehaviors(AjaxFormSubmitBehavior.class).get(0);
                _visit.stop(behavior.getCallbackScript());
            }
        });
        return script.toString().replaceFirst("Wicket\\.Ajax\\.", "new Wicket.Ajax.Call().");
    }
}
