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

package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.field.Field;
import org.efaps.bpm.BPM;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.field.UIField;
import org.efaps.ui.wicket.models.field.UIGroup;
import org.efaps.ui.wicket.models.field.UISnippletField;
import org.efaps.util.EFapsException;
import org.jbpm.task.query.TaskSummary;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UITaskObject
    extends AbstractUIModeObject
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The related Task as Summary from Hibernate.
     */
    private final TaskSummary taskSummary;

    private final List<UIGroup> groups = new ArrayList<UIGroup>();

    /**
     * @param _taskSummary The related Task as Summary from Hibernate.
     */
    public UITaskObject(final TaskSummary _taskSummary)
        throws EFapsException
    {
        super("");
        this.taskSummary = _taskSummary;
        initialize();
    }

    protected void initialize()
        throws EFapsException
    {
        final Form form = Form.get(getTaskSummary().getName());
        if (form != null) {
            Instance inst = Instance.get("");
            final Object values = BPM.getTaskData(getTaskSummary());
            if (values instanceof Map) {
                final String oid = (String) ((Map<?, ?>) values).get("OID");
                inst = Instance.get(oid);
            }
            if (inst != null && inst.isValid()) {
                final PrintQuery print = new PrintQuery(inst);
                for (final Field field : form.getFields()) {
                    if (field.getAttribute() != null) {
                        print.addAttribute(field.getAttribute());
                    }
                    if (field.getSelect() != null) {
                        print.addSelect(field.getAttribute());
                    }
                }
                print.execute();

                for (final Field field : form.getFields()) {
                    final UIGroup uiGroup = new UIGroup();
                    this.groups.add(uiGroup);
                    if (field.hasEvents(EventType.UI_FIELD_VALUE)) {
                        final StringBuilder html = new StringBuilder();
                        final List<Return> returns = field.executeEvents(EventType.UI_FIELD_VALUE,
                                        ParameterValues.INSTANCE, inst,
                                        ParameterValues.BPM_VALUES, values);
                        for (final Return ret : returns) {
                            html.append(ret.get(ReturnValues.SNIPLETT));
                        }
                        final UISnippletField uiField = new UISnippletField(inst.getKey(), this,
                                        new FieldConfiguration(
                                                        field.getId()));
                        uiGroup.add(uiField);
                        uiField.setHtml(html.toString());
                    } else {
                        Attribute attr = null;
                        Object object = null;
                        if (field.getAttribute() != null) {
                            object = print.getAttribute(field.getAttribute());
                            attr = inst.getType().getAttribute(field.getAttribute());
                        } else if (field.getSelect() != null) {
                            object = print.getSelect(field.getSelect());
                            attr = print.getAttribute4Select(field.getSelect());
                        }
                        final UIField uiField = new UIField(inst.getKey(), this, UIValue.get(field, attr, object));
                        uiGroup.add(uiField);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TargetMode getMode()
    {
        return TargetMode.VIEW;
    }

    /**
     * Getter method for the instance variable {@link #taskSummary}.
     *
     * @return value of instance variable {@link #taskSummary}
     */
    public TaskSummary getTaskSummary()
    {
        return this.taskSummary;
    }

    /**
     * Getter method for the instance variable {@link #groups}.
     *
     * @return value of instance variable {@link #groups}
     */
    public List<UIGroup> getGroups()
    {
        return this.groups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instance getInstanceFromManager()
        throws EFapsException
    {
        return getInstance();
    }

    /**
     * {@inheritDoc}
     */
    /*
     * (non-Javadoc)
     * @see
     * org.efaps.ui.wicket.models.AbstractInstanceObject#hasInstanceManager()
     */
    @Override
    public boolean hasInstanceManager()
        throws EFapsException
    {
        return false;
    }

    /**
     * @param _object
     * @return
     */
    public static IModel<UITaskObject> getModelForTask(final TaskSummary _taskSummary)
        throws EFapsException
    {
        return Model.of(new UITaskObject(_taskSummary));
    }
}
