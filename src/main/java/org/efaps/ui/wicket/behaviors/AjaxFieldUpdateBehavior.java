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

package org.efaps.ui.wicket.behaviors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.ValidationErrorFeedback;
import org.apache.wicket.model.IModel;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.values.ErrorMessageResource;
import org.efaps.ui.wicket.components.values.IFieldConfig;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxFieldUpdateBehavior
    extends AjaxFormSubmitBehavior
{
    /**
     * Reference to the javascript.
     */
    public static final EFapsContentReference JS = new EFapsContentReference(AjaxFieldUpdateBehavior.class,
                                                                             "FieldUpdate.js");

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AjaxFieldUpdateBehavior.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Model that will be used on submit (if not null).
     */
    private final IModel<?> model;

    /**
     * Is the callback beeing executed from dojo.
     */
    private boolean dojoCall = false;

    /**
     * Activate/Deactivate ErrorHandling.
     */
    private final boolean errorHandling;

    /**
     * @param _event event that this behavior should be executed on;
     */
    public AjaxFieldUpdateBehavior(final String _event)
    {
        this(_event, null);
    }

    /**
     * @param _event event that this behavior should be executed on;
     * @param _model model that willbe used on submit
     */
    public AjaxFieldUpdateBehavior(final String _event,
                                   final IModel<?> _model)
    {
        this(_event, _model, true);
    }

    /**
     * @param _event event that this behavior should be executed on;
     * @param _model model that willbe used on submit
     */
    public AjaxFieldUpdateBehavior(final String _event,
                                   final IModel<?> _model,
                                   final boolean _errorHandling)
    {
        super(_event);
        this.model = _model;
        this.errorHandling = _errorHandling;
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        _response.render(AbstractEFapsHeaderItem.forJavaScript(AjaxFieldUpdateBehavior.JS));
    }

    /**
     * Default means nothing is done on error.
     *
     * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onError(org.apache.wicket.ajax.AjaxRequestTarget)
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onError(final AjaxRequestTarget _target)
    {
        if (this.errorHandling) {
            final FeedbackCollector collector = new FeedbackCollector(getForm().getPage());
            final List<FeedbackMessage> msgs = collector.collect();
            final StringBuilder html = new StringBuilder()
                            .append("<table class=\"eFapsValidateFieldValuesTable\">");
            for (final FeedbackMessage msg : msgs) {
                msg.getReporter().add(AttributeModifier.append("class", "invalid"));
                _target.add(msg.getReporter());
                Serializable warn = null;
                if (msg.getMessage() instanceof ValidationErrorFeedback) {
                    // look if a message was set
                    warn = ((ValidationErrorFeedback) msg.getMessage()).getMessage();
                    // still no message, create one
                    if (warn == null) {
                        warn = ((ValidationErrorFeedback) msg.getMessage()).getError().getErrorMessage(
                                        new ErrorMessageResource());
                    }
                }
                String label = "";
                if (msg.getReporter() instanceof IFieldConfig) {
                    label = ((IFieldConfig) msg.getReporter()).getFieldConfig().getLabel();
                }
                html.append("<tr><td>").append(label).append(":</td><td>")
                                .append(warn).append("</td></tr>");
            }
            html.append("</table>");

            final ModalWindowContainer modal = ((AbstractContentPage) getComponent().getPage()).getModal();

            modal.setInitialWidth(350);
            modal.setInitialHeight(200);

            modal.setPageCreator(new ModalWindow.PageCreator()
            {

                private static final long serialVersionUID = 1L;

                @Override
                public Page createPage()
                {
                    return new DialogPage(((AbstractContentPage) getComponent().getPage()).getPageReference(),
                                    html.toString(), true, false);
                }
            });
            modal.show(_target);
        }
    }

    @Override
    public boolean getDefaultProcessing()
    {
        final boolean ret;
        if (this.errorHandling) {
            ret = super.getDefaultProcessing();
        } else {
            ret = false;
        }
        return ret;
    }

    /**
     * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onSubmit(final AjaxRequestTarget _target)
    {
        final Object uiObject;
        if (this.model == null) {
            uiObject = getComponent().getDefaultModelObject();
        } else {
            uiObject = this.model.getObject();
        }
        final List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
        if (uiObject instanceof UITableCell) {
            try {
                final AbstractUIPageObject pageObject = (AbstractUIPageObject) getComponent().getPage()
                                .getDefaultModelObject();
                final Map<String, String> uiID2Oid = pageObject == null ? null : pageObject.getUiID2Oid();
                final List<Return> returns = ((UITableCell) uiObject).getFieldUpdate(getComponent().getMarkupId(),
                                uiID2Oid);
                for (final Return aReturn : returns) {
                    final Object ob = aReturn.get(ReturnValues.VALUES);
                    if (ob instanceof List) {
                        @SuppressWarnings("unchecked")
                        final List<Map<String, Object>> list = (List<Map<String, Object>>) ob;
                        values.addAll(list);
                    }
                }
            } catch (final EFapsException e) {
                AjaxFieldUpdateBehavior.LOG.error("onSubmit", e);
            }
        } else {
            try {
                final AbstractUIPageObject pageObject = (AbstractUIPageObject) getComponent().getPage()
                                .getDefaultModelObject();
                final Map<String, String> uiID2Oid = pageObject == null ? null : pageObject.getUiID2Oid();
                final List<Return> returns = ((AbstractUIField) uiObject).executeEvents(EventType.UI_FIELD_UPDATE,
                                getComponent().getMarkupId(), uiID2Oid);
                for (final Return aReturn : returns) {
                    final Object ob = aReturn.get(ReturnValues.VALUES);
                    if (ob instanceof List) {
                        @SuppressWarnings("unchecked")
                        final List<Map<String, Object>> list = (List<Map<String, Object>>) ob;
                        values.addAll(list);
                    }
                }
            } catch (final EFapsException e) {
                AjaxFieldUpdateBehavior.LOG.error("onSubmit", e);
            }
        }

        final StringBuilder js = new StringBuilder();
        int i = 0;
        for (final Map<String, Object> map : values) {
            if (map.size() > 0) {
                final boolean useId = map.containsKey(EFapsKey.FIELDUPDATE_USEID.getKey());
                final boolean useIdx = map.containsKey(EFapsKey.FIELDUPDATE_USEIDX.getKey());
                for (final String keyString : map.keySet()) {
                    // if the map contains a key that is not defined in this class
                    // it is assumed to be the name of a field
                    if (!EFapsKey.FIELDUPDATE_JAVASCRIPT.getKey().equals(keyString)
                                    && !EFapsKey.FIELDUPDATE_USEID.getKey().equals(keyString)
                                    && !EFapsKey.FIELDUPDATE_USEIDX.getKey().equals(keyString)) {
                        js.append("eFapsSetFieldValue(");
                        if (useId || values.size() == 1 && !useIdx) {
                            js.append("'").append(map.get(EFapsKey.FIELDUPDATE_USEID.getKey()) == null
                                            ? getComponentMarkupId()
                                            : map.get(EFapsKey.FIELDUPDATE_USEID.getKey())).append("'");
                        } else {
                            js.append(map.get(EFapsKey.FIELDUPDATE_USEIDX.getKey()) == null ? i
                                            : map.get(EFapsKey.FIELDUPDATE_USEIDX.getKey()));
                        }
                        js.append(",'").append(keyString).append("',");
                        final Object value = map.get(keyString);
                        final String strValue;
                        final String strLabel;
                        if (value instanceof String[] && ((String[]) value).length == 2) {
                            strValue = ((String[]) value)[0];
                            strLabel = ((String[]) value)[1];
                        } else {
                            strValue = String.valueOf(value);
                            strLabel = null;
                        }
                        js.append(strValue.contains("Array(") ? "" : "'")
                            .append(strValue)
                            .append(strValue.contains("Array(") ? "" : "'");
                        if (strLabel != null) {
                            js.append(",'").append(strLabel).append("'");
                        }
                        js.append(");");
                    }
                }
            }
            if (map.containsKey(EFapsKey.FIELDUPDATE_JAVASCRIPT.getKey())) {
                js.append(map.get(EFapsKey.FIELDUPDATE_JAVASCRIPT.getKey()));
            }
            i++;
        }
        _target.appendJavaScript(js.toString());
    }

    /**
     * Method to get the ComponentMarkupId.
     *
     * @return markup id of the component.
     */
    protected String getComponentMarkupId()
    {
        return getComponent().getMarkupId();
    }

    /**
     * @return charequence
     */
    public CharSequence getCallbackScript4Dojo()
    {
        return super.getCallbackScript(getComponent());
    }


    @Override
    protected CharSequence getCallbackScript(final Component _component)
    {
        CharSequence ret;
        if (isDojoCall()) {
            ret = "";
        } else {
            ret = super.getCallbackScript(_component);
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #dojoCall}.
     *
     * @return value of instance variable {@link #dojoCall}
     */
    public boolean isDojoCall()
    {
        return this.dojoCall;
    }

    /**
     * Setter method for instance variable {@link #dojoCall}.
     *
     * @param _dojoCall value for instance variable {@link #dojoCall}
     */
    public void setDojoCall(final boolean _dojoCall)
    {
        this.dojoCall = _dojoCall;
    }
}
