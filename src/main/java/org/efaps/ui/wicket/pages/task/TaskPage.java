/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.ui.wicket.pages.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.bpm.BPM;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.UIGroup;
import org.efaps.ui.wicket.models.objects.UITaskObject;
import org.efaps.ui.wicket.models.task.DelegateRole;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.jbpm.task.query.TaskSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TaskPage
    extends AbstractMergePage
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TaskPage.class);

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(TaskPage.class, "TaskPage.css");

    /**
     * @param _taskObjModel model for the page
     * @param _pageReference page Reference
     */
    public TaskPage(final IModel<UITaskObject> _taskObjModel,
                    final PageReference _pageReference)
    {
        super(_taskObjModel);
        add(new AbstractDojoBehavior()
        {

            private static final long serialVersionUID = 1L;
        });
        final Form<TaskSummary> form = new Form<TaskSummary>("form");
        add(form);

        final RefreshingView<UIGroup> groupRepeater = new RefreshingView<UIGroup>(
                        "groupRepeater", Model.ofList(_taskObjModel.getObject().getGroups()))
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Iterator<IModel<UIGroup>> getItemModels()
            {
                final List<IModel<UIGroup>> ret = new ArrayList<IModel<UIGroup>>();
                final List<?> groups = (List<?>) getDefaultModelObject();
                for (final Object group : groups) {
                    ret.add(new Model<UIGroup>((UIGroup) group));
                }
                return ret.iterator();
            }

            @Override
            protected void populateItem(final Item<UIGroup> _item)
            {
                final List<AbstractUIField> field = ((UIGroup) _item.getDefaultModelObject()).getFields();
                _item.add(new FieldRepeater("fieldRepeater", Model.ofList(field)));
            }

        };
        form.add(groupRepeater);

        try {
            if (_taskObjModel.getObject().isComplete()) {
                String aprove = DBProperties.getProperty(_taskObjModel.getObject().getUITaskSummary().getName()
                                + ".aprove", false);
                if (aprove == null) {
                    aprove = DBProperties.getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.aprove");
                }

                form.add(new Button("aprove", new DecisionLink(Button.LINKID, _taskObjModel, _pageReference, true),
                                aprove, Button.ICON.ACCEPT.getReference()));
            } else {
                form.add(new WebMarkupContainer("aprove").setVisible(false));
            }

            if (_taskObjModel.getObject().isFail()) {
                String reject = DBProperties.getProperty(_taskObjModel.getObject().getUITaskSummary().getName()
                                + ".reject", false);
                if (reject == null) {
                    reject = DBProperties.getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.reject");
                }
                form.add(new Button("reject", new DecisionLink(Button.LINKID, _taskObjModel, _pageReference, false),
                                reject, Button.ICON.CANCEL.getReference()));
            } else {
                form.add(new WebMarkupContainer("reject").setVisible(false));
            }

            if (_taskObjModel.getObject().isClaim()) {
                String claim = DBProperties.getProperty(_taskObjModel.getObject().getUITaskSummary().getName()
                                + ".claim", false);
                if (claim == null) {
                    claim = DBProperties.getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.claim");
                }
                form.add(new Button("claim", new ClaimLink(Button.LINKID, _taskObjModel, _pageReference),
                                claim, Button.ICON.NEXT.getReference()));
            } else {
                form.add(new WebMarkupContainer("claim").setVisible(false));
            }

            if (_taskObjModel.getObject().isDelegate()) {
                String delegate = DBProperties.getProperty(_taskObjModel.getObject().getUITaskSummary().getName()
                                + ".delegate",
                                false);
                if (delegate == null) {
                    delegate = DBProperties
                                    .getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.delegate");
                }
                form.add(new Button("delegate", new DelegateLink(Button.LINKID, _taskObjModel, form, _pageReference),
                                delegate, Button.ICON.ACCEPT.getReference()));
                final DropDownChoice<DelegateRole> choice = new DropDownChoice<DelegateRole>("delegateChoice",
                                DelegateRole.getModel(), _taskObjModel.getObject().getDelegateRoles(),
                                new DelegateRoleRendere());
                form.add(choice);
                choice.setOutputMarkupPlaceholderTag(true);
                choice.setVisible(false);
            } else {
                form.add(new WebMarkupContainer("delegate").setVisible(false));
                form.add(new WebMarkupContainer("delegateModal").setVisible(false));
            }

            if (_taskObjModel.getObject().isRelease()) {
                String release = DBProperties.getProperty(_taskObjModel.getObject().getUITaskSummary().getName()
                                + ".release", false);
                if (release == null) {
                    release = DBProperties
                                    .getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.release");
                }
                form.add(new Button("release", new ReleaseLink(Button.LINKID, _taskObjModel, _pageReference),
                                release, Button.ICON.ACCEPT.getReference()));
            } else {
                form.add(new WebMarkupContainer("release").setVisible(false));
            }

        } catch (final EFapsException e) {
            TaskPage.LOG.error("Catched error on construction of TaskPage", e);
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(TaskPage.CSS));
    }

    /**
     * Repeater for the fields.
     */
    public final class FieldRepeater
        extends RefreshingView<AbstractUIField>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id
         * @param _model model
         */
        public FieldRepeater(final String _wicketId,
                             final IModel<?> _model)
        {
            super(_wicketId, _model);
        }

        @Override
        protected Iterator<IModel<AbstractUIField>> getItemModels()
        {
            final List<IModel<AbstractUIField>> ret = new ArrayList<IModel<AbstractUIField>>();
            final List<?> values = (List<?>) getDefaultModelObject();
            for (final Object value : values) {
                ret.add(new Model<AbstractUIField>((AbstractUIField) value));
            }
            return ret.iterator();
        }

        @Override
        protected void populateItem(final Item<AbstractUIField> _item)
        {
            final AbstractUIField uiField = _item.getModelObject();
            try {
                final WebMarkupContainer labelField = new WebMarkupContainer("labelField");
                final WebMarkupContainer nonLabelField = new WebMarkupContainer("nonLabelField");
                _item.add(labelField);
                _item.add(nonLabelField);
                if (uiField.getFieldConfiguration().isHideLabel()) {
                    nonLabelField.add(uiField.getComponent("field"));
                    labelField.setVisible(false);
                } else {
                    labelField.add(uiField.getComponent("field"));
                    nonLabelField.setVisible(false);
                }
            } catch (final EFapsException e) {
                TaskPage.LOG.error("Catched error during population of a TaskPage", e);
            }
        }
    }

    /**
     * Link for registering the decision.
     */
    public static class DecisionLink
        extends WebMarkupContainer
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId id of this component
         * @param _model model for this component
         * @param _pageReference reference to the page
         * @param _decision decision to be taken
         */
        public DecisionLink(final String _wicketId,
                            final IModel<UITaskObject> _model,
                            final PageReference _pageReference,
                            final boolean _decision)
        {
            super(_wicketId, _model);
            add(new AjaxFormSubmitBehavior("onclick")
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent(final AjaxRequestTarget _target)
                {
                    final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                    modal.setReloadChild(true);

                    final Map<String, Object> values = new HashMap<String, Object>();
                    try {
                        BPM.executeTask(((UITaskObject) getComponent().getDefaultModelObject()).getUITaskSummary()
                                        .getTaskSummary(),
                                        _decision, values);
                    } catch (final EFapsException e) {
                        TaskPage.LOG.error("Catched error during execute of a task", e);
                    }
                    modal.close(_target);
                }
            });
        }
    }

    /**
     * Claim a task Link.
     */
    public static class ClaimLink
        extends WebMarkupContainer
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket if fo this link
         * @param _model model for this component
         * @param _pageReference reference to the page
         */
        public ClaimLink(final String _wicketId,
                         final IModel<UITaskObject> _model,
                         final PageReference _pageReference)
        {
            super(_wicketId, _model);
            add(new AjaxFormSubmitBehavior("onclick")
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent(final AjaxRequestTarget _target)
                {
                    final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                    modal.setReloadChild(true);

                    try {
                        BPM.claimTask(((UITaskObject) getComponent().getDefaultModelObject()).getUITaskSummary()
                                        .getTaskSummary());
                    } catch (final EFapsException e) {
                        TaskPage.LOG.error("Catched error during claiming of a task", e);
                    }
                    modal.close(_target);
                }
            });
        }
    }

    /**
     * Release a task Link.
     */
    public static class ReleaseLink
        extends WebMarkupContainer
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket if fo this link
         * @param _model model for this component
         * @param _pageReference reference to the page
         */
        public ReleaseLink(final String _wicketId,
                           final IModel<UITaskObject> _model,
                           final PageReference _pageReference)
        {
            super(_wicketId, _model);
            add(new AjaxFormSubmitBehavior("onclick")
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent(final AjaxRequestTarget _target)
                {
                    final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                    modal.setReloadChild(true);

                    try {
                        BPM.releaseTask(((UITaskObject) getComponent().getDefaultModelObject()).getUITaskSummary()
                                        .getTaskSummary());
                    } catch (final EFapsException e) {
                        TaskPage.LOG.error("Catched error during claiming of a task", e);
                    }
                    modal.close(_target);
                }
            });
        }
    }

    /**
     * Delegate a task Link.
     */
    public static class DelegateLink
        extends WebMarkupContainer
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket if fo this link
         * @param _model model for this component
         * @param _form form to be used
         * @param _pageReference reference to the page
         */
        public DelegateLink(final String _wicketId,
                            final IModel<UITaskObject> _model,
                            final Form<TaskSummary> _form,
                            final PageReference _pageReference)
        {
            super(_wicketId, _model);
            add(new AjaxFormSubmitBehavior(_form, "onclick")
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(final AjaxRequestTarget _target)
                {
                    try {
                        DelegateRole selected = null;
                        if (_model.getObject().getDelegateRoles().size() > 1) {
                            final ComponentHierarchyIterator modalIter = getPage().visitChildren(DropDownChoice.class);
                            if (modalIter.hasNext()) {
                                final DropDownChoice<?> choice = (DropDownChoice<?>) modalIter.next();
                                final DelegateRole roleObj = (DelegateRole) choice.getDefaultModelObject();
                                if (roleObj.getUuid() == null) {
                                    choice.setVisible(true);
                                    _target.add(choice);
                                } else {
                                    selected = roleObj;
                                }
                            }
                        } else {
                            selected = _model.getObject().getDelegateRoles().get(0);
                        }
                        if (selected != null) {
                            final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                            modal.setReloadChild(true);
                            BPM.delegateTask(((UITaskObject) getComponent().getDefaultModelObject()).getUITaskSummary()
                                            .getTaskSummary(), selected.getUuid().toString());
                            modal.close(_target);
                        }
                    } catch (final EFapsException e) {
                        TaskPage.LOG.error("Catched error during delegation of a task", e);
                    }
                }
            });
        }
    }

    public static class DelegateRoleRendere
        implements IChoiceRenderer<DelegateRole>
    {

        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(final DelegateRole _object)
        {

            return _object.getName();
        }

        @Override
        public String getIdValue(final DelegateRole _object,
                                 final int _index)
        {
            return Integer.valueOf(_index).toString();
        }
    }

}
