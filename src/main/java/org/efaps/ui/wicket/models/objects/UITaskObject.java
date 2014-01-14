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

package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.user.Role;
import org.efaps.bpm.BPM;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.ui.wicket.models.cell.FieldConfiguration;
import org.efaps.ui.wicket.models.field.UIField;
import org.efaps.ui.wicket.models.field.UIGroup;
import org.efaps.ui.wicket.models.field.UISnippletField;
import org.efaps.ui.wicket.models.task.DelegateRole;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.kie.api.task.model.Status;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.Operation;


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
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The related Task as Summary from Hibernate.
     */
    private final UITaskSummary uiTaskSummary;

    /**
     * List of Field Groups.
     */
    private final List<UIGroup> groups = new ArrayList<UIGroup>();

    /**
     * Allowed Operations for this UITaskObject.
     */
    private final Set<Operation> operations = new HashSet<Operation>();

    /**
     * UUID top the form belonging to this UITaskObject.
     */
    private UUID formUUID = null;

    /**
     * Was the access checked allready.
     */
    private boolean accessChecked = false;

    /**
     * Delegate Roles allowed.
     */
    private List<DelegateRole> delegates;

    /**
     * Status of the related task.
     */
    private final Status status;

    /**
     * @param _taskSummary The related Task as Summary from Hibernate.
     * @throws EFapsException on error
     */
    public UITaskObject(final UITaskSummary _taskSummary)
        throws EFapsException
    {
        super("");
        this.uiTaskSummary = _taskSummary;
        this.status = _taskSummary.getTaskSummary().getStatus();
        initialize();
    }

    /**
     * Initialize the Task Object.
     * @throws EFapsException on error
     */
    protected void initialize()
        throws EFapsException
    {
        final InternalTask task = BPM.getTaskById(getUITaskSummary().getTaskSummary());
        final Form form;
        if (task.getFormName() == null) {
            form = Form.get(UUID.fromString(Configuration.getAttribute(ConfigAttribute.BPM_DEFAULTTASKFROM)));
        } else {
            final Form formTmp = Form.get(task.getFormName());
            if (formTmp == null) {
                form = Form.get(UUID.fromString(Configuration.getAttribute(ConfigAttribute.BPM_DEFAULTTASKFROM)));
            } else {
                form = formTmp;
            }
        }

        if (form != null) {
            this.formUUID = form.getUUID();

            final Object values = BPM.getTaskData(getUITaskSummary().getTaskSummary());
            if (values instanceof Map) {
                final String oid = (String) ((Map<?, ?>) values).get("OID");
                setInstanceKey(oid);
            }
            if (getInstanceKey() != null && getInstance().isValid()) {
                final PrintQuery print = new PrintQuery(getInstance());
                for (final Field field : form.getFields()) {
                    if (field.getAttribute() != null) {
                        print.addAttribute(field.getAttribute());
                    }
                    if (field.getSelect() != null) {
                        print.addSelect(field.getSelect());
                    }
                }
                print.execute();

                for (final Field field : form.getFields()) {
                    final UIGroup uiGroup = new UIGroup();
                    this.groups.add(uiGroup);
                    if (field.hasEvents(EventType.UI_FIELD_VALUE)) {
                        final StringBuilder html = new StringBuilder();
                        final List<Return> returns = field.executeEvents(EventType.UI_FIELD_VALUE,
                                        ParameterValues.INSTANCE, getInstance(),
                                        ParameterValues.BPM_VALUES, values);
                        for (final Return ret : returns) {
                            html.append(ret.get(ReturnValues.SNIPLETT));
                        }
                        final UISnippletField uiField = new UISnippletField(getInstance().getKey(), this,
                                        new FieldConfiguration(field.getId()));
                        uiGroup.add(uiField);
                        uiField.setHtml(html.toString());
                    } else {
                        Attribute attr = null;
                        Object object = null;
                        if (field.getAttribute() != null) {
                            object = print.getAttribute(field.getAttribute());
                            attr = getInstance().getType().getAttribute(field.getAttribute());
                        } else if (field.getSelect() != null) {
                            object = print.getSelect(field.getSelect());
                            attr = print.getAttribute4Select(field.getSelect());
                        }
                        final UIField uiField = new UIField(getInstance().getKey(), this, UIValue.get(field, attr,
                                        object));
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
    public UITaskSummary getUITaskSummary()
    {
        return this.uiTaskSummary;
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
    @Override
    public boolean hasInstanceManager()
        throws EFapsException
    {
        return false;
    }

    /**
     * @return true if aprove is allowed
     * @throws EFapsException on error
     */
    public boolean isComplete()
        throws EFapsException
    {
        checkAccess();
        return this.operations.isEmpty() ? true : this.operations.contains(Operation.Complete);
    }

    /**
     * @return true if fail is allowed
     * @throws EFapsException on error
     */
    public boolean isFail()
        throws EFapsException
    {
        checkAccess();
        return this.operations.isEmpty() ? true : this.operations.contains(Operation.Fail);
    }

    /**
     * @return true if claim is allowed
     * @throws EFapsException on error
     */
    public boolean isClaim()
        throws EFapsException
    {
        checkAccess();
        return this.operations.isEmpty()
                        ? !this.status.equals(Status.Reserved) : this.operations.contains(Operation.Claim);
    }

    /**
     * @return true if delegate is allowed
     * @throws EFapsException on error
     */
    public boolean isDelegate()
        throws EFapsException
    {
        checkAccess();
        final boolean ret = this.operations.isEmpty() ? true : this.operations.contains(Operation.Delegate);
        if (ret && this.delegates == null) {
            this.delegates  = new ArrayList<DelegateRole>();
            final List<Role> roles = BPM.getDelegates4Task(getUITaskSummary().getTaskSummary());
            for (final Role role  :roles) {
                this.delegates.add(new DelegateRole(role));
            }
        }
        return ret && !this.delegates.isEmpty();
    }

    /**
     * Exit is only an admin event.!!
     * @return true if exit allowed
     */
    public boolean isExit()
        throws EFapsException
    {
        return this.operations.isEmpty()
                        ? Context.getThreadContext().getPerson()
                                        .isAssigned(Role.get(KernelSettings.USER_ROLE_ADMINISTRATION))
                        : this.operations.contains(Operation.Exit);
    }

    /**
     * @return List of DelegateRoles.
     * @throws EFapsException on erro
     */
    public List<? extends DelegateRole> getDelegateRoles()
        throws EFapsException
    {
        isDelegate();
        return this.delegates;
    }

    /**
     * @return true if release is allowed
     * @throws EFapsException on error
     */
    public boolean isRelease()
        throws EFapsException
    {
        checkAccess();
        return this.operations.isEmpty()
                        ? this.status.equals(Status.Reserved) : this.operations.contains(Operation.Release);
    }

    /**
     * @return true if release is allowed
     * @throws EFapsException on error
     */
    public boolean isStop()
        throws EFapsException
    {
        checkAccess();
        return this.operations.isEmpty()
                        ?  this.status.equals(Status.InProgress) : this.operations.contains(Operation.Stop);
    }

    /**
     * @throws EFapsException on error
     */
    private void checkAccess()
        throws EFapsException
    {
        if (!this.accessChecked) {
            List<Return> returns = new ArrayList<Return>();
            final Form form = getForm();

            if (form != null && form.hasEvents(EventType.UI_ACCESSCHECK)) {
                final Parameter param = new Parameter();
                param.put(ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
                if (getInstance() != null) {
                    final String[] contextoid = { getInstanceKey() };
                    Context.getThreadContext().getParameters().put("oid", contextoid);
                    param.put(ParameterValues.INSTANCE, getInstance());
                    param.put(ParameterValues.BPM_TASK, getUITaskSummary().getTaskSummary());
                }
                final Object values = BPM.getTaskData(getUITaskSummary().getTaskSummary());
                param.put(ParameterValues.BPM_VALUES, values);
                returns = form.executeEvents(EventType.UI_ACCESSCHECK, param);
            }
            for (final Return ret : returns) {
                final Set<?> vals = (Set<?>) ret.get(ReturnValues.VALUES);
                for (final Object val : vals) {
                    if (val instanceof Operation) {
                        this.operations.add((Operation) val);
                    } else if (val instanceof String) {
                        this.operations.add(Operation.valueOf((String) val));
                    }
                }
            }
            this.accessChecked = true;
        }
    }

    /**
     * @return form this UITaskObject belongs to
     * @throws CacheReloadException on error
     */
    public Form getForm()
        throws CacheReloadException
    {
        return Form.get(this.formUUID);
    }

    /**
     * @param _taskSummary  taskSummary the Model is wanted for
     * @return model for a taskObject
     * @throws EFapsException on error
     */
    public static IModel<UITaskObject> getModelForTask(final UITaskSummary _taskSummary)
        throws EFapsException
    {
        return Model.of(new UITaskObject(_taskSummary));
    }


}
