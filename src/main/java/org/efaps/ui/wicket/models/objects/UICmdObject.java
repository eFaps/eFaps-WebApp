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
public class UICmdObject
    implements ICmdUIObject
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The cmd id. */
    private final long cmdId;

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
        return null;
    }

    /**
     * This method executes the Events which are related to this Model. It will
     * take the Events of the CallingCommand {@link #callingCmdUUID}, if it is
     * declared, otherwise it will take the Events of the Command
     * {@link #cmdUUID}. The Method also adds the oid {@link #instanceKey} to
     * the Context, so that it is accessible for the esjp.<br>
     * This method throws an eFpasError to provide the possibility for different
     * responses in the components.
     *
     * @param _objectTuples n tuples of ParamterValue and Object
     * @throws EFapsException on error
     * @return List of Returns
     */
    @Override
    public List<Return> executeEvents(final Object... _objectTuples)
        throws EFapsException
    {
        return executeEvents(EventType.UI_COMMAND_EXECUTE, _objectTuples);
    }

    /**
     * Execute the events.
     *
     * @param _eventType type of events to be executed
     * @param _objectTuples tuples of objects passed to the event
     * @return Lsit of returns from the events
     * @throws EFapsException on error
     */
    private List<Return> executeEvents(final EventType _eventType,
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
            ret = getCommand().executeEvents(_eventType, param);
        }
        return ret;
    }


    public static UICmdObject get(final Long _cmdId)
    {
        final UICmdObject ret = new UICmdObject(_cmdId);
        return ret;
    }


    public static IModel<ICmdUIObject> getModel(final Long _cmdId)
    {
        return Model.<ICmdUIObject>of(UICmdObject.get(_cmdId));
    }
}
