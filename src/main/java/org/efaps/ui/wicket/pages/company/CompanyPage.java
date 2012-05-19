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
 * Revision:        $Rev:1490 $
 * Last Changed:    $Date:2007-10-15 18:04:02 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.io.IClusterable;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.user.Company;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.menu.AjaxSetCompanyLink;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.util.EFapsException;

/**
 * Class renders the page that is used as a dialog to select the current
 * company. On close of the page the current company in the context will
 * be set and written to the user properties.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CompanyPage
    extends AbstractMergePage
{
    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(CompanyPage.class, "CompanyPage.css");

    /**
     * Modal window this page is opened in.
     */
    private final ModalWindowContainer modal;

    /**
     * Link that opened this page.
     */
    private final AjaxSetCompanyLink link;

    /**
     * Constructor adding all Components to this Page.
     *
     * @param _modal modal window
     * @param _link AjaxSetCompanyLink
     */
    public CompanyPage(final ModalWindowContainer _modal,
                       final AjaxSetCompanyLink _link)
    {
        super();
        this.modal = _modal;
        this.link = _link;

        // set the title for the Page
        add(new Label("pageTitle", DBProperties.getProperty("Logo.Version.Label")));

        this.add(StaticHeaderContrBehavior.forCss(CompanyPage.CSS));

        this.add(new LabelComponent("title",
                                    DBProperties.getProperty("org.efaps.ui.wicket.pages.company.title.Label")));

        final Form<Object> form = new Form<Object>("form") {
            private static final long serialVersionUID = 1L;

            /**
             * Implemented only for API reason.
             * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onError(org.apache.wicket.ajax.AjaxRequestTarget)
             * @param _target
             */
            @Override
            protected void onSubmit()
            {
                // nothing must be done
            }

            /**
             * Disable normal submit, because ajax is used.
             *
             * @see org.apache.wicket.markup.html.form.Form#onComponentTag(org.apache.wicket.markup.ComponentTag)
             * @param _tag
             */
            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("action", "");
            }
        };
        add(form);

        final Select choices = new Select("choices", new Model<CompanyObject>());
        form.add(choices);
        final IOptionRenderer<CompanyObject> renderer = new IOptionRenderer<CompanyObject>() {
            private static final long serialVersionUID = 1L;

            public String getDisplayValue(final CompanyObject _company)
            {
                return _company.name;
            }

            public IModel<CompanyObject> getModel(final CompanyObject _company)
            {
                return new Model<CompanyObject>(_company);
            }
        };

        final List<CompanyObject> companies = new ArrayList<CompanyObject>();
        try {
            for (final Long comp :  Context.getThreadContext().getPerson().getCompanies()) {
                companies.add(new CompanyObject(Company.get(comp)));
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        Collections.sort(companies, new Comparator<CompanyObject>() {
            @Override
            public int compare(final CompanyObject _o1,
                               final CompanyObject _o2)
            {
                return _o1.getName().compareTo(_o2.getName());
            }

        });
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final IModel<Collection<? extends CompanyObject>> model = new Model((Serializable) companies);
        final SelectOptions<CompanyObject> options = new SelectOptions<CompanyObject>("manychoices", model, renderer);
        choices.add(options);

        final AjaxSubmitLink ajaxGoOnLink = new AjaxSubmitLink(Button.LINKID, form);
        this.add(new Button("submitButton", ajaxGoOnLink,
                        DBProperties.getProperty("org.efaps.ui.wicket.pages.company.next.Label"),
                        Button.ICON.ACCEPT.getReference()));

        final AjaxCloseLink ajaxCloseLink = new AjaxCloseLink(Button.LINKID);
        this.add(new Button("closeButton", ajaxCloseLink,
                        DBProperties.getProperty("org.efaps.ui.wicket.pages.company.cancel.Label"),
                        Button.ICON.CANCEL.getReference()));
    }

    /**
     * Class to pass the companies as a serializable Object.
     */
    private final class CompanyObject
        implements IClusterable
    {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * Id of this company.
         */
        private final String id;

        /**
         * Name of this company.
         */
        private final String name;

        /**
         * @param _company Company
         */
        private CompanyObject(final Company _company)
        {
            this.id = ((Long) _company.getId()).toString();
            this.name = _company.getName();
        }

        /**
         * Getter method for the instance variable {@link #name}.
         *
         * @return value of instance variable {@link #name}
         */
        protected String getName()
        {
            return this.name;
        }
    }

    /**
     * Class used to submit this page.
     */
    public class AjaxSubmitLink
        extends WebMarkupContainer
    {
        /** Needed for serialization. */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id of this component
         * @param _form form this link submits
         *
         */
        public AjaxSubmitLink(final String _wicketId,
                              final Form<Object> _form)
        {
            super(_wicketId);
            final AjaxFormSubmitBehavior behavior = new AjaxFormSubmitBehavior(_form, "onClick") {

                private static final long serialVersionUID = 1L;

                /**
                 * Implemented only for API reason.
                 * @param _target
                 */
                @Override
                protected void onError(final AjaxRequestTarget _target)
                {
                    // Not used here
                }

                /**
                 * Close the form and set the current company.
                 * @param _target
                 */
                @Override
                protected void onSubmit(final AjaxRequestTarget _target)
                {
                    CompanyPage.this.link.setReload(true);
                    final Iterator<? extends Component> iter = _form.iterator();
                    final Component comp = iter.next();
                    final CompanyObject obj = (CompanyObject) comp.getDefaultModelObject();
                    try {
                        Context.getThreadContext().setUserAttribute(Context.CURRENTCOMPANY, obj.id);
                    } catch (final EFapsException e) {
                        throw new RestartResponseException(new ErrorPage(e));
                    }
                    CompanyPage.this.modal.close(_target);
                    _target.getPage().getApplication().getHomePage();
                }
            };
            this.add(behavior);
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
         * Close the form without reload.
         *
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target request target
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            CompanyPage.this.link.setReload(false);
            CompanyPage.this.modal.close(_target);
        }
    }
}
