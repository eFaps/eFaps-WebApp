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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.RestartResponseException;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Search;
import org.efaps.admin.user.Role;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.ui.wicket.components.modalwindow.ICmdUIObject;
import org.efaps.ui.wicket.models.cell.UIPicker;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author The eFaps Team
 * @version $Id: AbstractUIObject.java 8237 2012-11-23 04:37:45Z jan@moxter.net
 *          $
 */
public abstract class AbstractUIObject
    extends AbstractUIModeObject
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * This instance variable stores the UUID of the CommandAbstract which was
     * originally called from the Frontend and let to the construction of this
     * model.
     *
     * @see #getCallingCommandUUID()
     * @see #getCallingCommand()
     * @see #setCallingCommandUUID(UUID)
     */
    private UUID callingCmdUUID;

    /**
     * The instance variable stores the UUID of the Command for this Model.
     *
     * @see #getCommandUUID()
     * @see #getCommand
     */
    private UUID cmdUUID;

    /**
     * The instance variable is the flag if this class instance is already
     * Initialized.
     *
     * @see #isInitialised
     * @see #setInitialised
     */
    private boolean initialized = false;

    /**
     * This instance variable stores, if the Model is supposed to be submitted.
     *
     * @see #isSubmit()
     * @see #setSubmit(boolean)
     */
    private boolean submit = false;

    /**
     * This instance variable stores the Target of this Model.
     *
     * @see #getTarget()
     */
    private Target target = Target.UNKNOWN;

    /**
     * In case that the model was opened as a popup this variable stores the id
     * of the opener, so that it can be accessed in the EFapsSession.
     */
    private String openerId;

    /**
     * Picker that opened this object.
     */
    private UIPicker picker;

    /**
     * Constructor.
     *
     * @param _commandUUID UUID for this Model
     * @param _instanceKey instance id for this Model
     * @throws CacheReloadException on error
     */
    public AbstractUIObject(final UUID _commandUUID,
                            final String _instanceKey)
        throws CacheReloadException
    {
        this(_commandUUID, _instanceKey, null);
    }

    /**
     * Constructor.
     *
     * @param _commandUUID UUID for this Model
     * @param _instanceKey instance id for this Model
     * @param _openerId id of the opener UIClassification
     * @throws CacheReloadException on error
     */
    public AbstractUIObject(final UUID _commandUUID,
                            final String _instanceKey,
                            final String _openerId)
        throws CacheReloadException
    {
        super(_instanceKey);
        initialize(_commandUUID, _openerId);
    }

    /**
     * Method initializes the model.
     *
     * @param _commandUUID UUID for this Model
     * @param _openerId id of the opener
     * @throws CacheReloadException on error
     */
    protected void initialize(final UUID _commandUUID,
                              final String _openerId)
        throws CacheReloadException
    {
        this.openerId = _openerId;
        final AbstractCommand command = getCommand(_commandUUID);
        this.cmdUUID = command.getUUID();
        setMode(command.getTargetMode());
        this.target = command.getTarget();
        this.submit = command.isSubmit();
        if (command.getTargetSearch() != null && !(this instanceof UIMenuItem)) {
            this.callingCmdUUID = this.cmdUUID;
            this.cmdUUID = command.getTargetSearch().getDefaultCommand().getUUID();
            setMode(TargetMode.SEARCH);
            if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
                this.submit = true;
            }
        }
    }

    /**
     * @see org.efaps.ui.wicket.models.
     *      AbstractInstanceObject#getInstanceFromManager()
     * @return instance from a esjp
     * @throws EFapsException on error
     */
    @Override
    public Instance getInstanceFromManager()
        throws EFapsException
    {
        final AbstractCommand cmd = getCommand();
        final List<Return> rets = cmd.executeEvents(EventType.UI_INSTANCEMANAGER,
                        ParameterValues.OTHERS, getInstanceKey(),
                        ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());

        return (Instance) rets.get(0).get(ReturnValues.VALUES);
    }

    /**
     * @see org.efaps.ui.wicket.models.AbstractInstanceObject#hasInstanceManager()
     * @return true if related command has got a event of type
     *         <code>UI_INSTANCEMANAGER</code> else false
     * @throws CacheReloadException on eror
     */
    @Override
    public boolean hasInstanceManager()
        throws CacheReloadException
    {
        return getCommand().hasEvents(EventType.UI_INSTANCEMANAGER);
    }

    /**
     * Method is used to execute the UIObject (Fill it with data).
     */
    public abstract void execute();

    /**
     * This Method resets the Model, so that the next time the Model is going to
     * be connected, the underlying Data will be received newly from the
     * eFapsDataBase.
     */
    public abstract void resetModel();

    /**
     * Get the CommandAbstract which was originally called from the Frontend and
     * let to the construction of this model.
     *
     * @see #callingCmdUUID
     * @return the calling CommandAbstract UIClassification
     * @throws CacheReloadException on error
     */
    public AbstractCommand getCallingCommand()
        throws CacheReloadException
    {
        AbstractCommand cmd = Command.get(this.callingCmdUUID);
        if (cmd == null) {
            cmd = Menu.get(this.callingCmdUUID);
        }
        return cmd;
    }

    /**
     * This is the getter method for the instance variable
     * {@link #callingCmdUUID}.
     *
     * @return value of instance variable {@link #cmdUUID}
     */
    public UUID getCallingCommandUUID()
    {
        return this.callingCmdUUID;
    }

    /**
     * This is the setter method for the instance variable
     * {@link #callingCmdUUID}.
     *
     * @param _uuid UUID of the CommandAbstract
     */
    public void setCallingCommandUUID(final UUID _uuid)
    {
        this.callingCmdUUID = _uuid;
    }

    /**
     * get the CommandAbstract for the instance variable {@link #cmdUUID}.
     *
     * @return CommandAbstract for the instance variable {@link #cmdUUID}
     * @throws CacheReloadException on error
     */
    public AbstractCommand getCommand()
        throws CacheReloadException
    {
        AbstractCommand cmd = Command.get(this.cmdUUID);
        if (cmd == null) {
            cmd = Menu.get(this.cmdUUID);
        }
        if (cmd == null) {
            cmd = Search.get(this.cmdUUID);
        }
        return cmd;
    }

    /**
     * For given UUID of command / menu / Search, the related command / menu
     * /search Java instance is searched and, if found, returned.
     *
     * @param _uuid UUID of searched command object
     * @return found command / menu instance, or <code>null</code> if not found
     * @throws CacheReloadException on error
     */
    protected AbstractCommand getCommand(final UUID _uuid)
        throws CacheReloadException
    {
        AbstractCommand cmd = Command.get(_uuid);
        if (cmd == null) {
            cmd = Menu.get(_uuid);
            if (cmd == null) {
                cmd = Search.get(_uuid);
            }
        }
        return cmd;
    }

    /**
     * This is the getter method for the instance variable {@link #cmdUUID}.
     *
     * @return value of instance variable {@link #cmdUUID}
     */
    public UUID getCommandUUID()
    {
        return this.cmdUUID;
    }

    /**
     * This is the setter method for the instance variable {@link #cmdUUID}.
     *
     * @param _uuid UUID to set for teh instance varaiable {@link #cmdUUID}.
     */
    public void setCommandUUID(final UUID _uuid)
    {
        this.cmdUUID = _uuid;
    }

    /**
     * Getter method for instance variable {@link #openerId}.
     *
     * @return value of instance variable {@link #openerId}
     */
    public String getOpenerId()
    {
        return this.openerId;
    }

    /**
     * Setter method for instance variable {@link #openerId}.
     *
     * @param _openerId value for instance variable {@link #openerId}
     */
    public void setOpenerId(final String _openerId)
    {
        this.openerId = _openerId;
    }

    /**
     * This is the getter method for the instance variable {@link #target}.
     *
     * @return value of instance variable {@link #target}
     */
    public Target getTarget()
    {
        return this.target;
    }

    /**
     * This method retrieves the Value for the Titel from the eFaps Database.
     *
     * @return Value of the Title
     * @throws Exception
     */
    public String getTitle()
    {
        String title = "";
        try {
            title = DBProperties.getProperty(this.getCommand().getName() + ".Title");
            if ((title != null) && (getInstance() != null)) {
                final PrintQuery print = new PrintQuery(getInstance());
                final ValueParser parser = new ValueParser(new StringReader(title));
                final ValueList list = parser.ExpressionString();
                list.makeSelect(print);
                if (print.execute()) {
                    title = list.makeString(getInstance(), print, getMode());
                }
                // Administration
                if (Configuration.getAttributeAsBoolean(Configuration.ConfigAttribute.SHOW_OID)
                                && Context.getThreadContext()
                                                .getPerson()
                                                .isAssigned(Role.get(UUID
                                                                .fromString("1d89358d-165a-4689-8c78-fc625d37aacd")))) {
                    title = title + " " + getInstance().getOid();
                }
            }
        } catch (final ParseException e) {
            throw new RestartResponseException(new ErrorPage(new EFapsException(this.getClass(), "",
                            "Error reading the Title")));
            // CHECKSTYLE:OFF
        } catch (final Exception e) {
            throw new RestartResponseException(new ErrorPage(new EFapsException(this.getClass(), "",
                            "Error reading the Title")));
        } // CHECKSTYLE:ON
        return title;
    }

    /**
     * Method to check if mode is create.
     *
     * @see #mode
     * @return true if mode is create
     */
    public boolean isCreateMode()
    {
        return getMode() == TargetMode.CREATE;
    }

    /**
     * Method to check if mode is edit.
     *
     * @see #mode
     * @return true if mode is edit
     */
    public boolean isEditMode()
    {
        return getMode() == TargetMode.EDIT;
    }

    /**
     * Method to check if mode is search.
     *
     * @see #mode
     * @return true if mode is search
     */
    public boolean isSearchMode()
    {
        return getMode() == TargetMode.SEARCH;
    }

    /**
     * Method to check if mode is view.
     *
     * @see #mode
     * @return true if mode is view
     */
    public boolean isViewMode()
    {
        return getMode() == TargetMode.VIEW || getMode() == TargetMode.UNKNOWN;
    }

    /**
     * Method to check if mode is view.
     *
     * @see #mode
     * @return true if mode is print
     */
    public boolean isPrintMode()
    {
        return getMode() == TargetMode.PRINT;
    }

    /**
     * This is the getter method for the instance variable {@link #initialized}.
     *
     * @return value of instance variable {@link #initialized}
     * @see #initialized
     * @see #setInitialised
     */
    public boolean isInitialized()
    {
        return this.initialized;
    }

    /**
     * This is the setter method for the instance variable {@link #initialized}.
     *
     * @param _initialized new value for instance variable {@link #initialized}
     * @see #initialized
     * @see #isInitialised
     */
    public void setInitialized(final boolean _initialized)
    {
        this.initialized = _initialized;
    }

    /**
     * This is the getter method for the instance variable {@link #submit}.
     *
     * @return value of instance variable {@link #submit}
     * @see #setSubmit(boolean)
     */
    public boolean isSubmit()
    {
        return this.submit;
    }

    /**
     * This is the setter method for the instance variable {@link #submit}.
     *
     * @param _submit submit
     * @see #isSubmit()
     */
    public void setSubmit(final boolean _submit)
    {
        this.submit = _submit;
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
        List<Return> ret = new ArrayList<Return>();
        AbstractCommand command;
        if (this.callingCmdUUID == null) {
            command = this.getCommand();
        } else {
            command = getCallingCommand();
        }

        if (command.hasEvents(_eventType)) {
            final Parameter param = new Parameter();
            if (_objectTuples != null) {
                // add all parameters
                for (int i = 0; i < _objectTuples.length; i += 2) {
                    if (((i + 1) < _objectTuples.length) && (_objectTuples[i] instanceof ParameterValues)) {
                        param.put((ParameterValues) _objectTuples[i], _objectTuples[i + 1]);
                    }
                }
            }
            param.put(ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
            if (getInstance() != null) {
                final String[] contextoid = { getInstanceKey() };
                Context.getThreadContext().getParameters().put("oid", contextoid);
                param.put(ParameterValues.CALL_INSTANCE, getInstance());
                param.put(ParameterValues.INSTANCE, getInstance());
            }
            ret = command.executeEvents(_eventType, param);
        }

        return ret;
    }

    /**
     * This method executes the Validate-Events which are related to this Model.
     * It will take the Events of the Command {@link #cmdUUID}.
     *
     * @param _objectTuples tuples of Objects to be added to the event
     * @return List with Return from the esjp
     * @throws EFapsException on error
     */
    public List<Return> validate(final Object... _objectTuples)
        throws EFapsException
    {
        return executeEvents(EventType.UI_VALIDATE, _objectTuples);
    }

    /**
     * Getter method for the instance variable {@link #picker}.
     *
     * @return value of instance variable {@link #picker}
     */
    public boolean isOpenedByPicker()
    {
        return this.picker != null;
    }

    /**
     * Setter method for instance variable {@link #picker}.
     *
     * @param _picker value for instance variable {@link #picker}
     */
    public void setPicker(final ICmdUIObject _uiObject)
    {
        if (_uiObject instanceof UIPicker) {
            this.picker = (UIPicker) _uiObject;
        } else if (_uiObject == null) {
            this.picker = null;
        }
    }

    /**
     * Getter method for the instance variable {@link #picker}.
     *
     * @return value of instance variable {@link #picker}
     */
    public UIPicker getPicker()
    {
        return this.picker;
    }
}
