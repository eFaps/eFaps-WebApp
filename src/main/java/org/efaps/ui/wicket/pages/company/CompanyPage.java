/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.pages.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.PageReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.access.user.AccessCache;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.user.Company;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.modalwindow.LegacyModalWindow;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * Class renders the page that is used as a dialog to select the current
 * company. On close of the page the current company in the context will be set
 * and written to the user properties.
 *
 * @author The eFaps Team
 */
public class CompanyPage
    extends AbstractMergePage
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(CompanyPage.class, "CompanyPage.css");

    /**
     * Modal window this page is opened in.
     */
    private final PageReference calledByReference;

    /**
     * Constructor adding all Components to this Page.
     *
     * @param _calledByReference the called by reference
     */
    public CompanyPage(final PageReference _calledByReference)
    {
        super();
        this.calledByReference = _calledByReference;

        // set the title for the Page
        add2Page(new Label("pageTitle", DBProperties.getProperty("Logo.Version.Label")));

        add(new LabelComponent("title",
                        DBProperties.getProperty("org.efaps.ui.wicket.pages.company.title.Label")));

        final Form<Object> form = new Form<>("form")
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit()
            {
                // nothing must be done
            }

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("action", "");
            }
        };
        add(form);

        final IOptionRenderer<CompanyObject> renderer = new IOptionRenderer<>()
        {

            private static final long serialVersionUID = 1L;

            @Override
            public String getDisplayValue(final CompanyObject _company)
            {
                return _company.getName();
            }

            @Override
            public IModel<CompanyObject> getModel(final CompanyObject _company)
            {
                return new Model<>(_company);
            }
        };

        CompanyObject selected = null;
        final List<CompanyObject> companies = new ArrayList<>();
        try {
            for (final Long comp : Context.getThreadContext().getPerson().getCompanies()) {
                final CompanyObject compObj = new CompanyObject(Company.get(comp));
                if (comp == Context.getThreadContext().getCompany().getId()) {
                    selected = compObj;
                }
                companies.add(compObj);
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        Collections.sort(companies, Comparator.comparing(CompanyObject::getName));
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final IModel<Collection<? extends CompanyObject>> model = new Model((Serializable) companies);
        final SelectOptions<CompanyObject> options = new SelectOptions<>("manychoices", model, renderer);

        final Select<CompanyObject> choices = new Select<>("choices", Model.of(selected));
        form.add(choices);

        choices.add(options);

        add(new AjaxSubmitBtn("submitButton"));
        add(new AjaxCloseBtn("closeButton"));
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(CompanyPage.CSS));
    }

    /**
     * Class to pass the companies as a serializable Object.
     */
    private static final class CompanyObject
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

        @Override
        public String toString()
        {
            return this.id;
        }
    }

    /**
     * Class used to submit this page.
     */
    public class AjaxSubmitBtn
        extends AjaxButton<Void>
    {

        /** Needed for serialization. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new ajax submit btn.
         *
         * @param _wicketId wicket id of this component
         */
        public AjaxSubmitBtn(final String _wicketId)
        {
            super(_wicketId, AjaxButton.ICON.ACCEPT.getReference(),
                            DBProperties.getProperty("org.efaps.ui.wicket.pages.company.next.Label"));
        }

        @Override
        public Form<?> getForm()
        {
            return getPage().visitChildren(Form.class, new IVisitor<Form<?>, Form<?>>()
            {
                @Override
                public void component(final Form<?> _form,
                                      final IVisit<Form<?>> _visit)
                {
                    _visit.stop(_form);
                }
            });
        }

        @Override
        public void onRequest(final AjaxRequestTarget _target)
        {
            final CompanyObject obj = getPage().visitChildren(Select.class,
                            (_select,
                             _visit) -> _visit.stop((CompanyObject) _select.getDefaultModelObject()));

            try {
                Context.getThreadContext().setUserAttribute(Context.CURRENTCOMPANY, obj.id);
                AccessCache.clean4Person(Context.getThreadContext().getPersonId());
            } catch (final EFapsException e) {
                throw new RestartResponseException(new ErrorPage(e));
            }
            final ModalWindowContainer modal = ((MainPage) CompanyPage.this.calledByReference.getPage()).getModal();
            modal.close(_target);
            modal.setWindowClosedCallback(new LegacyModalWindow.WindowClosedCallback()
            {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClose(final AjaxRequestTarget _target)
                {
                    setResponsePage(getApplication().getHomePage());
                }
            });
        }
    }

    /**
     * AjaxLink that closes the ModalWindow this Page was opened in.
     */
    public class AjaxCloseBtn
        extends AjaxButton<Void>
    {

        /** Needed for serialization. */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id of this component
         */
        public AjaxCloseBtn(final String _wicketId)
        {
            super(_wicketId, AjaxButton.ICON.CANCEL.getReference(),
                            DBProperties.getProperty("org.efaps.ui.wicket.pages.company.cancel.Label"));
        }

        @Override
        public void onRequest(final AjaxRequestTarget _target)
        {
            final ModalWindowContainer modal = ((MainPage) CompanyPage.this.calledByReference.getPage()).getModal();
            modal.close(_target);
        }

        @Override
        protected boolean isSubmit()
        {
            return false;
        }
    }
}
