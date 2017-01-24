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
package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Command;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * The Class UICmdObject.
 */
public final class UICmdObject
    implements ICmdUIObject
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The cmd id. */
    private final long cmdId;

    /** The instance. */
    private Instance instance;

    /** The call cmdid. */
    private long callCmdId;

    /**
     * Instantiates a new UI cmd object.
     *
     * @param _cmdId the cmd id
     */
    private UICmdObject(final Long _cmdId)
    {
        this.cmdId = _cmdId;
    }

    @Override
    public AbstractCommand getCommand()
        throws EFapsException
    {
        return Command.get(this.cmdId);
    }

    @Override
    public Instance getInstance()
    {
        return this.instance;
    }

    /**
     * Setter method for instance variable {@link #instance}.
     *
     * @param _instance value for instance variable {@link #instance}
     * @return the UI cmd object
     */
    public UICmdObject setInstance(final Instance _instance)
    {
        this.instance = _instance;
        return this;
    }

    @Override
    public AbstractCommand getCallingCommand()
        throws EFapsException
    {
        return Command.get(this.callCmdId);
    }

    /**
     * Sets the calling command.
     *
     * @param _callCmdId the call cmd id
     * @return the UI cmd object
     */
    public UICmdObject setCallingCommand(final Long _callCmdId)
    {
        this.callCmdId = _callCmdId;
        return this;
    }

    /**
     * Execute the events.
     *
     * @param _eventType type of events to be executed
     * @param _objectTuples tuples of objects passed to the event
     * @return Lsit of returns from the events
     * @throws EFapsException on error
     */
    @Override
    public List<Return> executeEvents(final EventType _eventType,
                                      final Object... _objectTuples)
        throws EFapsException
    {
        List<Return> ret = new ArrayList<>();
        if (getCommand().hasEvents(_eventType)) {
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
            if (getInstance() != null) {
                final String[] contextoid = { getInstance().getOid() };
                Context.getThreadContext().getParameters().put("oid", contextoid);
                param.put(ParameterValues.CALL_INSTANCE, getInstance());
                param.put(ParameterValues.INSTANCE, getInstance());
            }
            ret = getCommand().executeEvents(_eventType, param);
        }
        return ret;
    }

    /**
     * Gets the.
     *
     * @param _cmdId the cmd id
     * @return the UI cmd object
     */
    public static UICmdObject get(final Long _cmdId)
    {
        final UICmdObject ret = new UICmdObject(_cmdId);
        return ret;
    }

    /**
     * Gets the model.
     *
     * @param _cmdId the cmd id
     * @param _instance the instance
     * @return the model
     */
    public static IModel<ICmdUIObject> getModel(final Long _cmdId,
                                                final Instance _instance)
    {
        return Model.<ICmdUIObject>of(UICmdObject.get(_cmdId).setInstance(_instance));
    }
}
