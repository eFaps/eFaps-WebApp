/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.models.field;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.field.FieldPicker;
import org.efaps.api.ui.UIType;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class UIPicker
    extends AbstractInstanceObject
    implements Serializable, ICmdUIObject
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UIPicker.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * UUID of the command used for this PickerObject.
     */
    private final UUID cmdUUID;

    /**
     * Label of this Picker.
     */
    private final String label;

    /**
     * The parent UIObject.
     */
    private final AbstractInstanceObject parent;

    /**
     * Was the event already executed.
     */
    private boolean executed = false;

    /**
     * UItype used.
     */
    private UIType uiType;

    /**
     * Map returned by the esjp.
     */
    private Map<String, Object> returnMap = new HashMap<>();

    /**
     * Parameter map.
     */
    private Map<String, String[]> parentParameters;

    /**
     * @param _field    fieldPicker this UIObject belongs to
     * @param _parent   parent field this fieldpicker belongs to
     * @throws CacheReloadException on error during access to command
     */
    public UIPicker(final FieldPicker _field,
                    final AbstractInstanceObject _parent)
        throws CacheReloadException
    {
        this.cmdUUID = _field.getCommand().getUUID();
        this.label = _field.getCommand().getLabelProperty();
        this.parent = _parent;
        final String uiTypeStr = _field.getProperty("UIType");
        if (EnumUtils.isValidEnum(UIType.class, uiTypeStr)) {
            this.uiType = UIType.valueOf(uiTypeStr);
        } else {
            this.uiType = UIType.DEFAULT;
        }

    }

    /**
     * Getter method for the instance variable {@link #cmdUUID}.
     *
     * @return value of instance variable {@link #cmdUUID}
     */
    public UUID getCmdUUID()
    {
        return this.cmdUUID;
    }

    /**
     * @return the command underlying this picker
     * @throws CacheReloadException on error during access to command
     */
    @Override
    public Command getCommand()
        throws CacheReloadException
    {
        return Command.get(this.cmdUUID);
    }

    /**
     * Getter method for the instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * @return the height of the window to be opened
     */
    public int getWindowHeight()
    {
        int ret = 0;
        try {
            ret = getCommand().getWindowHeight();
        } catch (final CacheReloadException e) {
            UIPicker.LOG.error("Coould not read WindowHeight for Command with uuid:{}", getCmdUUID());
        }
        return ret;
    }

    /**
     * @return the width of the window to be opened
     */
    public int getWindowWidth()
    {
        int ret = 0;
        try {
            ret = getCommand().getWindowWidth();
        } catch (final CacheReloadException e) {
            UIPicker.LOG.error("Coould not read WindowWith for Command with uuid:{}", getCmdUUID());
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * @throws EFapsException
     */
    @Override
    public Instance getInstance()
        throws EFapsException
    {
        return this.parent.getInstance();
    }

    /**
     * Execute events.
     *
     * @param _eventType the event type
     * @param _objectTuples the object tuples
     * @return the list< return>
     * @throws EFapsException on error
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Return> executeEvents(final EventType _eventType,
                                      final Object... _objectTuples)
        throws EFapsException
    {
        List<Return> ret = new ArrayList<>();
        if (_eventType.equals(EventType.UI_COMMAND_EXECUTE) && getCommand().hasEvents(EventType.UI_PICKER)) {
            this.executed  = true;
            final Parameter param = new Parameter();
            if (_objectTuples != null) {
                // add all parameters
                for (int i = 0; i < _objectTuples.length; i += 2) {
                    if (i + 1 < _objectTuples.length && _objectTuples[i] instanceof ParameterValues) {
                        param.put((ParameterValues) _objectTuples[i], _objectTuples[i + 1]);
                    }
                }
            }
            param.put(ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
            param.put(ParameterValues.PARENTPARAMETERS, this.parentParameters);
            if (getInstance() != null) {
                final String[] contextoid = { getInstanceKey() };
                Context.getThreadContext().getParameters().put("oid", contextoid);
                param.put(ParameterValues.CALL_INSTANCE, getInstance());
                param.put(ParameterValues.INSTANCE, getInstance());
            }
            ret = getCommand().executeEvents(EventType.UI_PICKER, param);
            this.returnMap = (Map<String, Object>) ret.get(0).get(ReturnValues.VALUES);
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #executed}.
     *
     * @return value of instance variable {@link #executed}
     */
    public boolean isExecuted()
    {
        return this.executed;
    }

    /**
     * Setter method for instance variable {@link #executed}.
     *
     * @param _executed value for instance variable {@link #executed}
     */

    public void setExecuted(final boolean _executed)
    {
        this.executed = _executed;
    }

    /**
     * Getter method for the instance variable {@link #returnMap}.
     *
     * @return value of instance variable {@link #returnMap}
     */
    public Map<String, Object> getReturnMap()
    {
        return this.returnMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instance getInstanceFromManager()
        throws EFapsException
    {
        return getParent().getInstanceFromManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasInstanceManager()
        throws EFapsException
    {
        return getParent().hasInstanceManager();
    }

    /**
     * Checks if is button.
     *
     * @return true, if is button
     */
    public boolean isButton()
    {
        return UIType.BUTTON.equals(this.uiType);
    }

    /**
     * Gets the parent UIObject.
     *
     * @return the parent UIObject
     */
    public AbstractInstanceObject getParent()
    {
        return this.parent;
    }

    /**
     * Sets the parameter map.
     *
     * @param _parameters the new parameter map
     */
    public void setParentParameters(final Map<String, String[]> _parameters)
    {
        this.parentParameters = _parameters;
    }
}
