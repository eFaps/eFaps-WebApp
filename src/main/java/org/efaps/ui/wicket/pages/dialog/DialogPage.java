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
 * Revision:        $Rev:1489 $
 * Last Changed:    $Date:2007-10-15 17:50:46 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.dialog;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ui.wicket.behaviors.dojo.RequireBehavior;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.footer.AjaxSubmitCloseBehavior;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.modalwindow.UpdateParentCallback;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * This Page renders a Dialog for Userinterference.<br>
 * e.g. "Do you really want to...?"
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DialogPage
    extends AbstractMergePage
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet of this Page stored in the eFaps-DataBase.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(DialogPage.class, "DialogPage.css");

    /**
     * Reference to the page that opened this dialog.
     */
    private final PageReference pageReference;

    /**
     * Constructor used for a DialogPage that renders a Question like: "Are you
     * sure that??" with a Cancel and a SubmitButton.
     *
     * @param _pageReference Reference to the page that opened this dialog
     * @param _model the MenuItem that called this DialogPage
     * @param _oids oids which must be past on, in case of submit
     */
    public DialogPage(final PageReference _pageReference,
                      final IModel<UIMenuItem> _model,
                      final String[] _oids)
        throws CacheReloadException
    {
        super(_model);
        this.pageReference = _pageReference;
        final UIMenuItem menuItem = _model.getObject();

        final String cmdName = menuItem.getCommand().getName();

        add(new Label("textLabel", DBProperties.getProperty(cmdName + ".Question")).setOutputMarkupId(true));

        add(new Button("submitButton", new AjaxSubmitLink(Button.LINKID, _model, _oids),
                        DialogPage.getLabel(cmdName, "Submit"), Button.ICON.ACCEPT.getReference())
                        .setOutputMarkupId(true));

        add(new Button("closeButton", new AjaxCloseLink(Button.LINKID), DialogPage.getLabel(cmdName, "Cancel"),
                        Button.ICON.CANCEL.getReference()));
        add(new RequireBehavior("dojo/query", "dojo/NodeList-dom"));
    }

    /**
     * Constructor setting the ModalWindow.
     *
     * @param _pageReference Reference to the page that opened this dialog
     * @param _value value is depending on parameter "_isSniplett" the key to a
     *            DBProperty or a snipplet
     * @param _isSniplett is it a snipplet or not
     * @param _goOn go on?
     */
    public DialogPage(final PageReference _pageReference,
                      final String _value,
                      final boolean _isSniplett,
                      final boolean _goOn)
    {
        this.pageReference = _pageReference;

        if (_isSniplett) {
            this.add(new LabelComponent("textLabel", _value));
        } else {
            this.add(new Label("textLabel", DBProperties.getProperty(_value + ".Message")));
        }
        if (_goOn) {
            final AjaxGoOnLink ajaxGoOnLink = new AjaxGoOnLink(Button.LINKID);
            this.add(new Button("submitButton", ajaxGoOnLink, DialogPage.getLabel(_value, "Create"),
                            Button.ICON.ACCEPT.getReference()));
        } else {
            this.add(new WebMarkupContainer("submitButton").setVisible(false));
        }

        final AjaxCloseLink ajaxCloseLink = new AjaxCloseLink(Button.LINKID);
        this.add(new Button("closeButton", ajaxCloseLink, DialogPage.getLabel(_value, "Close"),
                        Button.ICON.CANCEL.getReference()));

        ajaxCloseLink.add(new KeyListenerBehavior());
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(DialogPage.CSS));
    }

    /**
     * Method that gets the Value for the Buttons from the DBProperties.
     *
     * @param _cmdName Name of the Command, that Label for the Button should be
     *            retrieved
     * @param _keytype type of the key e.g. "Cancel", "Submit", "Close"
     * @return Label
     */
    private static String getLabel(final String _cmdName,
                                   final String _keytype)
    {
        String ret;
        if (DBProperties.hasProperty(_cmdName + ".Button." + _keytype)) {
            ret = DBProperties.getProperty(_cmdName + ".Button." + _keytype);
        } else {
            ret = DBProperties.getProperty("default.Button." + _keytype);
        }
        return ret;
    }

    /**
     * AjaxLink that closes the ModalWindow this Page was opened in.
     */
    public class AjaxGoOnLink
        extends AjaxLink<Object>
    {

        /** Needed for serialization. */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id of this component
         *
         */
        public AjaxGoOnLink(final String _wicketId)
        {
            super(_wicketId);
        }

        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target AjaxRequestTarget
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final AbstractContentPage page = (AbstractContentPage) DialogPage.this.pageReference.getPage();

            final AjaxSubmitCloseBehavior beh = page.visitChildren(new IVisitor<Component, AjaxSubmitCloseBehavior>()
            {

                @Override
                public void component(final Component _component,
                                      final IVisit<AjaxSubmitCloseBehavior> _visit)
                {
                    final List<AjaxSubmitCloseBehavior> behaviors = _component
                                    .getBehaviors(AjaxSubmitCloseBehavior.class);
                    if (!behaviors.isEmpty()) {
                        _visit.stop(behaviors.get(0));
                    }
                }
            });

            beh.setValidated(true);
            final ModalWindowContainer modal = page.getModal();
            modal.close(_target);
        }
    }

    /**
     * AjaxLink that closes the ModalWindow this Page was opened in.
     */
    public class AjaxCloseLink
        extends AjaxLink<Object>
    {

        /** Needed for serialization. */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id of this component
         */
        public AjaxCloseLink(final String _wicketId)
        {
            super(_wicketId);
        }

        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target request target
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final ModalWindowContainer modal = ((AbstractContentPage) DialogPage.this.pageReference.getPage())
                            .getModal();

            modal.close(_target);

            final StringBuilder bldr = new StringBuilder();
            bldr.append("var inp = top.frames[0].document").append(".getElementById('eFapsContentDiv')").append(
                            ".getElementsByTagName('input');").append("if(inp!=null){").append("  inp[0].focus();")
                            .append("}");
            _target.appendJavaScript(bldr.toString());
        }
    }

    /**
     * AjaxLink that submits the Parameters and closes the ModalWindow.
     */
    public class AjaxSubmitLink
        extends AjaxLink<UIMenuItem>
    {

        /** Needed for serialization. */
        private static final long serialVersionUID = 1L;

        /**
         * the Oids that will be submitted.
         */
        private final String[] oids;

        /**
         * Form was validated.
         */
        private boolean validated = false;

        /**
         * @param _wicketId wicket id of this component
         * @param _model model for this component
         * @param _oids oids
         */
        public AjaxSubmitLink(final String _wicketId,
                              final IModel<UIMenuItem> _model,
                              final String[] _oids)
        {
            super(_wicketId, _model);
            this.oids = _oids;
        }

        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target request target
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final UIMenuItem model = getModelObject();
            try {
                if (isValidated() || validate(_target)) {
                    model.executeEvents(ParameterValues.OTHERS, this.oids);
                    final ModalWindowContainer modal = ((AbstractContentPage) DialogPage.this.pageReference.getPage())
                                    .getModal();
                    modal.setWindowClosedCallback(new UpdateParentCallback(DialogPage.this.pageReference, modal));
                    modal.setUpdateParent(true);
                    modal.close(_target);
                }
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
        }

        /**
         * Executes the Validation-Events related to the CommandAbstract which
         * called this Form.
         *
         * @param _target AjaxRequestTarget to be used in the case a ModalPage
         *            should be called
         * @return true if the Validation was valid, otherwise false
         * @throws EFapsException on error
         */
        private boolean validate(final AjaxRequestTarget _target)
            throws EFapsException
        {
            setValidated(true);
            boolean ret = true;
            boolean goOn = true;
            final UIMenuItem menuItem = (UIMenuItem) getDefaultModelObject();
            final List<Return> returns = menuItem.validate(ParameterValues.OTHERS, this.oids);
            final StringBuilder bldr = new StringBuilder();
            for (final Return oneReturn : returns) {
                if (oneReturn.get(ReturnValues.VALUES) != null || oneReturn.get(ReturnValues.SNIPLETT) != null) {
                    if (oneReturn.get(ReturnValues.VALUES) != null) {
                        bldr.append(oneReturn.get(ReturnValues.VALUES));
                    } else {
                        bldr.append(oneReturn.get(ReturnValues.SNIPLETT));
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
                getPage().visitChildren(Label.class, new IVisitor<Label, Void>()
                {
                    @Override
                    public void component(final Label _label,
                                          final IVisit<Void> _visit)
                    {
                        final Component label = new Label("textLabel", bldr.toString()).setOutputMarkupId(true)
                                        .setEscapeModelStrings(false);
                        _label.replaceWith(label);
                        _target.add(label);
                        _visit.stop();
                    }
                });
                if (!goOn) {
                    final StringBuilder js = new StringBuilder()
                        .append("require([\"dojo/query\", \"dojo/NodeList-dom\"], function(query){")
                        .append(" query(\".eFapsWarnDialogButton1\").style(\"display\", \"none\");")
                        .append(" });");
                    _target.appendJavaScript(js);
                }
            }
            return ret;
        }

        /**
         * Getter method for the instance variable {@link #validated}.
         *
         * @return value of instance variable {@link #validated}
         */
        public boolean isValidated()
        {
            return this.validated;
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
    }

    /**
     * CLass is used to listen to keyboard entries.
     */
    private static final class KeyListenerBehavior
        extends Behavior
    {

        /** Needed for serialization. */
        private static final long serialVersionUID = 1L;

        @Override
        public void renderHead(final Component _component,
                               final IHeaderResponse _response)
        {
            super.renderHead(_component, _response);
            final StringBuilder js = new StringBuilder();
            js.append("function pressed (_event) {")
                            .append("var b=Wicket.$('").append(_component.getMarkupId())
                            .append("'); if (typeof(b.onclick) != 'undefined') { b.onclick();  }")
                            .append("}").append("window.onkeydown = pressed;");
            _response.render(JavaScriptHeaderItem.forScript(js, DialogPage.class.getName()));
        }
    }
}
