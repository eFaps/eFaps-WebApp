/*
 * Copyright 2003 - 2009 The eFaps Team
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.db.Context;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.Opener;
import org.efaps.ui.wicket.behaviors.update.UpdateInterface;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIAbstractPageObject;
import org.efaps.ui.wicket.models.objects.UIFieldForm;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UIWizardObject;
import org.efaps.ui.wicket.models.objects.UIForm.Element;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * Class renders the footer in a form. It is responsible for performing the
 * actions like executing the related esjp etc.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxSubmitCloseBehavior extends AjaxFormSubmitBehavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instance variable storing the model, because the super classes of a
     * behavior, doesn't store the model.
     */
    private final UIAbstractPageObject uiObject;

    /**
     * Instance variable storing the form to be submited.
     */
    private final FormContainer form;

    /**
     * Constructor.
     *
     * @param _uiobject UUIOBject
     * @param _form form
     */
    public AjaxSubmitCloseBehavior(final UIAbstractPageObject _uiobject, final FormContainer _form)
    {
        super(_form, "onclick");
        this.uiObject = _uiobject;
        this.form = _form;
    }

    /**
     * On submit the action must be done.
     *
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onSubmit(final AjaxRequestTarget _target)
    {

        final Map<String, String[]> others = new HashMap<String, String[]>();
        final String[] other = getComponent().getRequestCycle().getRequest().getParameters("selectedRow");
        final List<Classification> classifications = new ArrayList<Classification>();
        others.put("selectedRow", other);

        convertDateFieldValues();
        if (this.uiObject instanceof UIForm) {
            final UIForm uiform = (UIForm) this.uiObject;
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
        try {
            if (checkForRequired(_target) && (validateForm(_target, others, classifications))) {
                if (this.uiObject instanceof UIForm && ((UIForm) this.uiObject).isFileUpload()) {
                    doFileUpload(_target);
                } else {
                    if (executeEvents(_target, others, classifications)) {
                        if (this.uiObject.hasTargetCmd()) {
                            final AbstractCommand targetCmd = this.uiObject.getTargetCmd();
                            UIAbstractPageObject newUIObject;
                            if (targetCmd.getTargetTable() != null) {
                                newUIObject = new UITable(this.uiObject.getTargetCmdUUID(), this.uiObject
                                                .getInstanceKey(), this.uiObject.getOpenerId());
                            } else {
                                newUIObject = new UIForm(this.uiObject.getTargetCmdUUID(), this.uiObject
                                                .getInstanceKey(), this.uiObject.getOpenerId());
                            }

                            final UIWizardObject wizard = new UIWizardObject(newUIObject);
                            this.uiObject.setWizard(wizard);
                            try {
                                wizard.addParameters(this.uiObject, Context.getThreadContext().getParameters());
                            } catch (final EFapsException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            wizard.insertBefore(this.uiObject);
                            newUIObject.setWizard(wizard);
                            newUIObject.setPartOfWizardCall(true);
                            newUIObject.setRenderRevise(this.uiObject.isTargetCmdRevise());
                            if (this.uiObject.isSubmit()) {
                                newUIObject.setSubmit(true);
                                newUIObject.setCallingCommandUUID(this.uiObject.getCallingCommandUUID());
                            }
                            final FooterPanel footer = getComponent().findParent(FooterPanel.class);
                            final ModalWindowContainer modal = footer.getModalWindow();
                            final AbstractContentPage page;
                            if (targetCmd.getTargetTable() != null) {
                                page = new TablePage(new TableModel((UITable) newUIObject), modal);
                            } else {
                                page = new FormPage(new FormModel((UIForm) newUIObject), modal);
                            }
                            page.setMenuTreeKey(((AbstractContentPage) getComponent().getPage()).getMenuTreeKey());
                            getComponent().getPage().getRequestCycle().setResponsePage(page);
                        } else {
                            final FooterPanel footer = getComponent().findParent(FooterPanel.class);
                            // if inside a modal
                            if (this.uiObject.getCommand().getTarget() == Target.MODAL) {
                                footer.getModalWindow().setReloadChild(true);
                                footer.getModalWindow().close(_target);
                            } else {
                                final Opener opener = ((EFapsSession) Session.get()).getOpener(this.uiObject
                                                .getOpenerId());
                                // mark the opener that it can be removed
                                opener.setMarked4Remove(true);

                                Class<? extends Page> clazz;
                                if (opener.getModel() instanceof TableModel) {
                                    clazz = TablePage.class;
                                } else {
                                    clazz = FormPage.class;
                                }

                                final PageParameters parameters = new PageParameters();
                                parameters.add(Opener.OPENER_PARAKEY, this.uiObject.getOpenerId());

                                final CharSequence url = this.form.urlFor(PageMap.forName(opener.getPageMapName()),
                                                clazz, parameters);

                                _target.appendJavascript("opener.location.href = '" + url + "'; self.close();");
                            }
                            footer.setSuccess(true);

                            // execute the CallBacks
                            final List<UpdateInterface> updates = ((EFapsSession) getComponent().getSession())
                                            .getUpdateBehavior(this.uiObject.getInstanceKey());
                            if (updates != null) {
                                for (final UpdateInterface update : updates) {
                                    if (update.isAjaxCallback()) {
                                        update.setInstanceKey(this.uiObject.getInstanceKey());
                                        update.setMode(this.uiObject.getMode());
                                        _target.prependJavascript(update.getAjaxCallback());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (final EFapsException e) {
            final ModalWindowContainer modal = ((AbstractContentPage) getComponent().getPage()).getModal();
            modal.setPageCreator(new ModalWindow.PageCreator() {

                private static final long serialVersionUID = 1L;

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
     */
    private void convertDateFieldValues()
    {
        final List<FormPanel> formpl = getFormPanels();
        for (final FormPanel panel : formpl) {
            for (final DateTimePanel datepicker : panel.getDateComponents()) {
                final Map<String, String[]> map = getComponent().getRequestCycle().getRequest().getParameterMap();
                if (map.containsKey(datepicker.getDateFieldName())) {
                    final String[] date = map.get(datepicker.getDateFieldName());
                    final String[] hour = map.get(datepicker.getHourFieldName());
                    final String[] minute = map.get(datepicker.getMinuteFieldName());
                    final String[] ampm = map.get(datepicker.getAmPmFieldName());
                    map.put(datepicker.getFieldName(), new String[] {datepicker.getDateAsString(date, hour, minute, ampm)});
                }
            }
        }
    }


    /**
     * Method to enable file upload.
     *
     * @param _target AjaxRequestTarget
     */
    private void doFileUpload(final AjaxRequestTarget _target)
    {
        final StringBuilder script = new StringBuilder();
        script.append("var f=document.getElementById('").append(this.form.getMarkupId()).append(
                        "');f.onsubmit=undefined;f.action=\"").append(this.form.getActionUrl())
                        .append("\";f.submit();");
        this.form.setFileUpload(true);
        _target.appendJavascript(script.toString());
    }

    /**
     * Method is not used, but needed from the api.
     *
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onError(final AjaxRequestTarget _target)
    {
        // not useful here
    }

    /**
     * In case of a file upload a precondition is needed.
     *
     * @return precondition JavaScript
     */
    @Override
    protected CharSequence getPreconditionScript()
    {
        String ret = null;
        if (this.uiObject instanceof UIForm && ((UIForm) this.uiObject).isFileUpload()) {
            ret = "return eFapsFileInput()";
        }
        return ret;
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
    private boolean executeEvents(final AjaxRequestTarget _target, final Map<String, String[]> _other,
                                  final List<Classification> _classifications)
                    throws EFapsException
    {
        boolean ret = true;
        final List<Return> returns;
        if (_classifications.size() > 0) {
            returns = ((AbstractUIObject) this.form.getParent().getDefaultModelObject()).executeEvents(
                            ParameterValues.OTHERS, _other,
                            ParameterValues.CLASSIFICATIONS, _classifications);
        } else {
            returns = ((AbstractUIObject) this.form.getParent().getDefaultModelObject()).executeEvents(
                            ParameterValues.OTHERS, _other);
        }

        for (final Return oneReturn : returns) {
            if (oneReturn.get(ReturnValues.TRUE) == null && !oneReturn.isEmpty()) {
                boolean sniplett = false;
                String key = (String) oneReturn.get(ReturnValues.VALUES);
                if (key == null) {
                    key = (String) oneReturn.get(ReturnValues.SNIPLETT);
                    sniplett = true;
                }
                showDialog(_target, key, sniplett);

                ret = false;
                break;
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
    private boolean validateForm(final AjaxRequestTarget _target, final Map<String, String[]> _other,
                                 final List<Classification> _classifications)
            throws EFapsException
    {
        boolean ret = true;
        final List<Return> returns;
        if (_classifications.size() > 0) {
            returns = ((AbstractUIObject) this.form.getParent().getDefaultModelObject()).validate(
                            ParameterValues.OTHERS, _other,
                            ParameterValues.CLASSIFICATIONS, _classifications);
        } else {
            returns = ((AbstractUIObject) this.form.getParent().getDefaultModelObject()).validate(
                            ParameterValues.OTHERS, _other);
        }

        for (final Return oneReturn : returns) {
            if (oneReturn.get(ReturnValues.TRUE) == null) {
                boolean sniplett = false;
                String key = (String) oneReturn.get(ReturnValues.VALUES);
                if (key == null) {
                    key = (String) oneReturn.get(ReturnValues.SNIPLETT);
                    sniplett = true;
                }
                showDialog(_target, key, sniplett);

                ret = false;
                break;
            }
        }
        return ret;
    }

    /**
     * Method checking if the mandatory field of the Form are filled in, and if
     * not opens a WarnDialog and marks the fields in the Form via Ajax.
     *
     * @param _target RequestTarget used for this Request
     * @return true if all mandatory fields are filled, else false
     */
    private boolean checkForRequired(final AjaxRequestTarget _target)
    {
        boolean ret = true;
        if (!(this.form.getParent().getDefaultModel() instanceof TableModel)) {
            final Map<?, ?> map = getComponent().getRequestCycle().getRequest().getParameterMap();
            final List<FormPanel> panels = getFormPanels();
            for (final FormPanel panel : panels) {
                for (final Entry<String, Label> entry : panel.getRequiredComponents().entrySet()) {
                    final String[] values = (String[]) map.get(entry.getKey());
                    final String value = values[0];
                    if (value == null || value.length() == 0) {
                        final Label label = entry.getValue();
                        label.add(new SimpleAttributeModifier("class", "eFapsFormLabelRequiredForce"));
                        _target.addComponent(label);
                        ret = false;
                    }
                }
            }
            if (!ret) {
                showDialog(_target, "MandatoryDialog", false);
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
        final List<FormPanel> ret = new ArrayList<FormPanel>();
        final Iterator<?> iterator = this.form.iterator();
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
     * @param _target AjaxRequestTarget to be used for opening the modal
     *            DialogPage
     * @param _key the Key to get the DBProperties from the eFapsDataBaase or a
     *            code sniplett
     * @param _isSniplett is the parameter _key a key to a property or a
     *            sniplett
     */
    private void showDialog(final AjaxRequestTarget _target, final String _key, final boolean _isSniplett)
    {
        final ModalWindowContainer modal = ((AbstractContentPage) getComponent().getPage()).getModal();

        modal.setInitialWidth(200);
        modal.setInitialHeight(150);
        modal.setPageMapName("warn");

        modal.setPageCreator(new ModalWindow.PageCreator() {

            private static final long serialVersionUID = 1L;

            public Page createPage()
            {
                return new DialogPage(modal, _key, _isSniplett);
            }
        });
        modal.show(_target);
    }
}
