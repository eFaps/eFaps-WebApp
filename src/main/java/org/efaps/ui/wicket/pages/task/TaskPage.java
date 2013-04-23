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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.bpm.Bpm;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.ui.wicket.models.field.UIGroup;
import org.efaps.ui.wicket.models.objects.UITaskObject;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
import org.jbpm.task.query.TaskSummary;

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
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the StyleSheet for this Page.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(TaskPage.class, "TaskPage.css");


    /**
     * @param _rowModel
     */
    public TaskPage(final IModel<UITaskObject> _rowModel,
                    final PageReference _pageReference)
    {
        super(_rowModel);

        final Form<TaskSummary> form = new Form<TaskSummary>("form");
        add(form);

        final RefreshingView<UIGroup> groupRepeater = new RefreshingView<UIGroup>(
                        "groupRepeater", Model.ofList(_rowModel.getObject().getGroups()))
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
        String aprove = DBProperties.getProperty(_rowModel.getObject().getTaskSummary().getName() + ".aprove", false);

        if (aprove == null) {
            aprove = DBProperties.getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.aprove");
        }

        String reject = DBProperties.getProperty(_rowModel.getObject().getTaskSummary().getName() + ".reject", false);
        if (reject == null) {
            reject = DBProperties.getProperty("org.efaps.ui.wicket.pages.task.TaskPage.default.Button.reject");
        }

        form.add(new Button("aprove", new DecisionLink(Button.LINKID, _rowModel, _pageReference, true),
                        aprove, Button.ICON.ACCEPT.getReference()));

        form.add(new Button("reject", new DecisionLink(Button.LINKID, _rowModel, _pageReference, false),
                        reject, Button.ICON.CANCEL.getReference()));

    }

    public final class FieldRepeater
        extends RefreshingView<AbstractUIField>
    {

        /**
     *
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public class DecisionLink
        extends WebMarkupContainer
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _id
         * @param _model
         */
        public DecisionLink(final String _id,
                            final IModel<UITaskObject> _model,
                            final PageReference _pageReference,
                            final boolean _decision)
        {
            super(_id, _model);
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
                        Bpm.executeTask(((UITaskObject) getComponent().getDefaultModelObject()).getTaskSummary(),
                                        _decision, values);
                    } catch (final EFapsException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    modal.close(_target);
               }
            });
        }
    }


    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(TaskPage.CSS));
    }
}
