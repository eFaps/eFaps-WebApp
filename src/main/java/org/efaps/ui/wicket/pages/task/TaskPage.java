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
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.bpm.BPM;
import org.efaps.db.Context;
import org.efaps.ui.wicket.behaviors.dojo.AbstractDojoBehavior;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.UIGroup;
import org.efaps.ui.wicket.models.objects.UITaskObject;
import org.efaps.ui.wicket.models.task.DelegatePerson;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.kie.api.task.model.TaskSummary;
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
     * @param _adminMode open page in admin mode
     */
    public TaskPage(final IModel<UITaskObject> _taskObjModel,
                    final PageReference _pageReference,
                    final boolean _adminMode)
    {
        super(_taskObjModel);
        add(new AbstractDojoBehavior()
        {
            private static final long serialVersionUID = 1L;
        });

        add(new FeedbackPanel("feedback").setOutputMarkupId(true));

        final Form<TaskSummary> form = new Form<>("form");
        add(form);

        final RefreshingView<UIGroup> groupRepeater = new RefreshingView<UIGroup>(
                        "groupRepeater", Model.ofList(_taskObjModel.getObject().getGroups()))
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Iterator<IModel<UIGroup>> getItemModels()
            {
                final List<IModel<UIGroup>> ret = new ArrayList<>();
                final List<?> groups = (List<?>) getDefaultModelObject();
                for (final Object group : groups) {
                    ret.add(new Model<>((UIGroup) group));
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
            if (_taskObjModel.getObject().isComplete() || _adminMode) {
                String aprove = DBProperties.getProperty(_taskObjModel.getObject().getKey() + ".approve", false);
                if (aprove == null) {
                    aprove = DBProperties.getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.approve");
                }

                form.add(new Button("aprove", new DecisionLink(Button.LINKID, _taskObjModel, _pageReference, true),
                                aprove, AjaxButton.ICON.ACCEPT.getReference()));
            } else {
                form.add(new WebMarkupContainer("aprove").setVisible(false));
            }

            if (_taskObjModel.getObject().isFail() || _adminMode) {
                String reject = DBProperties.getProperty(_taskObjModel.getObject().getKey() + ".reject", false);
                if (reject == null) {
                    reject = DBProperties.getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.reject");
                }
                form.add(new Button("reject", new DecisionLink(Button.LINKID, _taskObjModel, _pageReference, false),
                                reject, AjaxButton.ICON.CANCEL.getReference()));
            } else {
                form.add(new WebMarkupContainer("reject").setVisible(false));
            }

            if (_taskObjModel.getObject().isClaim() || _adminMode) {
                String claim = DBProperties.getProperty(_taskObjModel.getObject().getKey() + ".claim", false);
                if (claim == null) {
                    claim = DBProperties.getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.claim");
                }
                form.add(new Button("claim", new ClaimLink(Button.LINKID, _taskObjModel, _pageReference),
                                claim, AjaxButton.ICON.NEXT.getReference()));
            } else {
                form.add(new WebMarkupContainer("claim").setVisible(false));
            }

            if (_taskObjModel.getObject().isDelegate()) {
                String delegate = DBProperties.getProperty(_taskObjModel.getObject().getKey() + ".delegate", false);
                if (delegate == null) {
                    delegate = DBProperties
                                    .getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.delegate");
                }
                form.add(new Button("delegate", new DelegateLink(Button.LINKID, _taskObjModel, form, _pageReference),
                                delegate, AjaxButton.ICON.ADD.getReference()));
                final DropDownChoice<DelegatePerson> choice = new DropDownChoice<>("delegateChoice",
                                DelegatePerson.getModel(), _taskObjModel.getObject().getDelegateRoles(),
                                new DelegatePersonRenderer());
                form.add(choice);
                choice.setOutputMarkupPlaceholderTag(true);
                choice.setVisible(false);
            } else {
                form.add(new WebMarkupContainer("delegate").setVisible(false));
                form.add(new WebMarkupContainer("delegateChoice").setVisible(false));
            }

            if (_taskObjModel.getObject().isRelease() || _adminMode) {
                String release = DBProperties.getProperty(_taskObjModel.getObject().getKey() + ".release", false);
                if (release == null) {
                    release = DBProperties
                                    .getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.release");
                }
                form.add(new Button("release", new ReleaseLink(Button.LINKID, _taskObjModel, _pageReference),
                                release, AjaxButton.ICON.PREVIOUS.getReference()));
            } else {
                form.add(new WebMarkupContainer("release").setVisible(false));
            }

            if (_taskObjModel.getObject().isStop() || _adminMode) {
                String release = DBProperties.getProperty(_taskObjModel.getObject().getKey() + ".stop", false);
                if (release == null) {
                    release = DBProperties
                                    .getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.stop");
                }
                form.add(new Button("stop", new StopLink(Button.LINKID, _taskObjModel, _pageReference),
                                release, AjaxButton.ICON.CANCEL.getReference()));
            } else {
                form.add(new WebMarkupContainer("stop").setVisible(false));
            }

            if (_taskObjModel.getObject().isExit() || _adminMode) {
                String release = DBProperties.getProperty(_taskObjModel.getObject().getKey() + ".exit", false);
                if (release == null) {
                    release = DBProperties
                                    .getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.exit");
                }
                form.add(new Button("exit", new ExitLink(Button.LINKID, _taskObjModel, _pageReference),
                                release, AjaxButton.ICON.CANCEL.getReference()));
            } else {
                form.add(new WebMarkupContainer("exit").setVisible(false));
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
            final List<IModel<AbstractUIField>> ret = new ArrayList<>();
            final List<?> values = (List<?>) getDefaultModelObject();
            for (final Object value : values) {
                ret.add(new Model<>((AbstractUIField) value));
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
                if (uiField.getFieldConfiguration() != null && !uiField.getFieldConfiguration().isHideLabel()) {
                    labelField.add(uiField.getComponent("field"));
                    nonLabelField.setVisible(false);
                } else {
                    nonLabelField.add(uiField.getComponent("field"));
                    labelField.setVisible(false);
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
            add(new AjaxFormSubmitBehavior("click")
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent(final AjaxRequestTarget _target)
                {
                    final Map<String, Object> values = new HashMap<>();
                    try {
                        BPM.executeTask(((UITaskObject) getComponent().getDefaultModelObject()).getUITaskSummary()
                                        .getTaskSummary(),
                                        _decision, values);
                        Context.save();

                        final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                        modal.setReloadChild(true);
                        modal.close(_target);
                    } catch (final EFapsException e) {
                        TaskPage.LOG.error("Catched error during execute of a task", e);
                    } catch (final PermissionDeniedException e) {
                        TaskPage.LOG.warn("Catched error during execute of a task", e);
                        error(DBProperties.getProperty(TaskPage.class.getName() + ".NoAccess"));
                        _target.addChildren(getPage(), FeedbackPanel.class);
                    }
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
            add(new AjaxFormSubmitBehavior("click")
            {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent(final AjaxRequestTarget _target)
                {
                    try {
                        BPM.claimTask(((UITaskObject) getComponent().getDefaultModelObject()).getUITaskSummary()
                                        .getTaskSummary());
                        Context.save();
                        final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                        modal.setReloadChild(true);
                        modal.close(_target);
                    } catch (final EFapsException e) {
                        TaskPage.LOG.error("Catched error during claiming of a task", e);
                    }

                }
            });
        }
    }

    /**
     * Stop a task Link.
     */
    public static class StopLink
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
        public StopLink(final String _wicketId,
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
                    try {
                        BPM.stopTask(((UITaskObject) getComponent().getDefaultModelObject()).getUITaskSummary()
                                        .getTaskSummary());
                        Context.save();
                        final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                        modal.setReloadChild(true);
                        modal.close(_target);
                    } catch (final EFapsException e) {
                        TaskPage.LOG.error("Catched error during claiming of a task", e);
                    } catch (final PermissionDeniedException e) {
                        TaskPage.LOG.warn("Catched error during execute of a task", e);
                        error(DBProperties.getProperty(TaskPage.class.getName() + ".NoAccess"));
                        _target.addChildren(getPage(), FeedbackPanel.class);
                    }

                }
            });
        }
    }

    /**
     * Exit a task Link.
     */
    public static class ExitLink
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
        public ExitLink(final String _wicketId,
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
                    try {
                        BPM.exitTask(((UITaskObject) getComponent().getDefaultModelObject()).getUITaskSummary()
                                        .getTaskSummary());
                        Context.save();
                        final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                        modal.setReloadChild(true);
                        modal.close(_target);
                    } catch (final EFapsException e) {
                        TaskPage.LOG.error("Catched error during claiming of a task", e);
                    } catch (final PermissionDeniedException e) {
                        TaskPage.LOG.warn("Catched error during execute of a task", e);
                        error(DBProperties.getProperty(TaskPage.class.getName() + ".NoAccess"));
                        _target.addChildren(getPage(), FeedbackPanel.class);
                    }
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
                    try {
                        final ModalWindowContainer modal = ((MainPage) _pageReference.getPage()).getModal();
                        modal.setReloadChild(true);
                        modal.close(_target);
                        BPM.releaseTask(((UITaskObject) getComponent().getDefaultModelObject()).getUITaskSummary()
                                        .getTaskSummary());
                        Context.save();
                    } catch (final EFapsException e) {
                        TaskPage.LOG.error("Catched error during claiming of a task", e);
                    } catch (final PermissionDeniedException e) {
                        TaskPage.LOG.warn("Catched error during execute of a task", e);
                        error(DBProperties.getProperty(TaskPage.class.getName() + ".NoAccess"));
                        _target.addChildren(getPage(), FeedbackPanel.class);
                    }
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
                        DelegatePerson selected = null;
                        if (_model.getObject().getDelegateRoles().size() > 1) {
                            selected = getPage().visitChildren(DropDownChoice.class,
                                            new IVisitor<DropDownChoice<?>, DelegatePerson>()
                                {

                                    @Override
                                    public void component(final DropDownChoice<?> _choice,
                                                          final IVisit<DelegatePerson> _visit)
                                    {
                                        final DelegatePerson roleObj = (DelegatePerson) _choice
                                                        .getDefaultModelObject();
                                        if (roleObj.getUuid() == null) {
                                            _choice.setVisible(true);
                                            _target.add(_choice);
                                        } else {
                                            _visit.stop(roleObj);
                                        }
                                    }
                                });
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
                    } catch (final PermissionDeniedException e) {
                        TaskPage.LOG.warn("Catched error during execute of a task", e);
                        error(DBProperties.getProperty(TaskPage.class.getName() + ".NoAccess"));
                        _target.addChildren(getPage(), FeedbackPanel.class);
                    }
                }
            });
        }
    }

    /**
     * Render the Roles.
     */
    public static class DelegatePersonRenderer
        implements IChoiceRenderer<DelegatePerson>
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        @Override
        public Object getDisplayValue(final DelegatePerson _object)
        {
            return _object.getLastName() + ", " + _object.getFirstName();
        }

        @Override
        public String getIdValue(final DelegatePerson _object,
                                 final int _index)
        {
            return Integer.valueOf(_index).toString();
        }

        @Override
        public DelegatePerson getObject(final String _id,
                                        final IModel<? extends List<? extends DelegatePerson>> _choices)
        {
            DelegatePerson ret = null;
            @SuppressWarnings("unchecked")
            final List<DelegatePerson> choices = (List<DelegatePerson>) _choices.getObject();
            for (int index = 0; index < choices.size(); index++) {
                // Get next choice
                final DelegatePerson choice = choices.get(index);
                if (getIdValue(choice, index).equals(_id)) {
                    ret = choice;
                    break;
                }
            }
            return ret;
        }
    }
}
