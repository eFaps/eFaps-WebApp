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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.cell.UIHiddenCell;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AbstractUIPageObject.java 7611 2012-06-08 21:56:57Z
 *          jan@moxter.net $
 */
public abstract class AbstractUIPageObject
    extends AbstractUIObject
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Mapping between the id used for the interface and an oid from eFaps.
     */
    private final Map<String, String> uiID2Oid = new HashMap<String, String>();

    /**
     * Random that can be used for the id in the userinterface and stored in
     * {@link #uiID2Oid}.
     */
    private final Random random = new Random();

    /**
     * A list of cells that will be rendered hidden in tht page.
     */
    private final List<UIHiddenCell> hiddenCells = new ArrayList<UIHiddenCell>();

    /**
     * Map of instance with access definition.
     */
    private final Map<Instance, Boolean> accessMap = new HashMap<Instance, Boolean>();

    /**
     * This instance variable stores the UUID of the CommandAbstract that is the
     * target of this Page.
     */
    private UUID targetCmdUUID;

    /**
     * Stores a wizard object.
     */
    private UIWizardObject wizard;

    /**
     * Is this uiobject used in a wizard call.
     */
    private boolean partOfWizardCall;

    /**
     * Should the target of this object render a butto for revise.
     */
    private boolean targetCmdRevise;

    /**
     * Should a revise button be rendered.
     */
    private boolean renderRevise = false;

    /**
     * Must a file be shown that is returned from the esjp.
     */
    private boolean isTargetShowFile;

    /**
     * Name of the wiki file, that is the target of the help link shown in the
     * form, table etc.
     */
    private String helpTarget;

    /**
     * Constructor.
     *
     * @param _commandUUID UUID for this Model
     * @param _instanceKey instance id for this Model
     * @param _openerId id of the opener
     * @throws CacheReloadException on error
     */
    public AbstractUIPageObject(final UUID _commandUUID,
                                final String _instanceKey,
                                final String _openerId)
        throws CacheReloadException
    {
        super(_commandUUID, _instanceKey, _openerId);
        if (_commandUUID != null) {
            final AbstractCommand cmd = getCommand();
            if (cmd.getTargetCommand() != null) {
                final AbstractCommand trgCmd = cmd.getTargetCommand();
                this.targetCmdUUID = trgCmd.getUUID();
                this.targetCmdRevise = trgCmd.isTargetCmdRevise();
            } else if (TargetMode.SEARCH.equals(getMode())) {
                this.targetCmdRevise = cmd.isTargetCmdRevise();
            }
            this.isTargetShowFile = cmd.isTargetShowFile();
            this.helpTarget = cmd.getTargetHelp();
        }
    }

    /**
     * Constructor.
     *
     * @param _commandUUID UUID for this Model
     * @param _instanceKey instance id for this Model
     * @throws CacheReloadException on error
     */
    public AbstractUIPageObject(final UUID _commandUUID,
                                final String _instanceKey)
        throws CacheReloadException
    {
        this(_commandUUID, _instanceKey, null);
    }

    /**
     * Getter method for instance variable {@link #hiddenCells}.
     *
     * @return value of instance variable {@link #hiddenCells}
     */
    public List<UIHiddenCell> getHiddenCells()
    {
        return this.hiddenCells;
    }

    /**
     * Method to add a hidden Cell to the list {@link #hiddenCells}.
     *
     * @param _hiddenCell cell to add
     */
    public void addHidden(final UIHiddenCell _hiddenCell)
    {
        this.hiddenCells.add(_hiddenCell);
    }

    /**
     * Get the CommandAbstract that is the target of this Page.
     *
     *
     * @see #targetCmdUUID
     * @return the calling CommandAbstract UIClassification
     */
    public AbstractCommand getTargetCmd()
        throws CacheReloadException
    {
        AbstractCommand cmd = Command.get(this.targetCmdUUID);
        if (cmd == null) {
            cmd = Menu.get(this.targetCmdUUID);
        }
        return cmd;
    }

    /**
     * Getter method for instance variable {@link #targetCmdUUID}.
     *
     * @return value of instance variable {@link #targetCmdUUID}
     */
    public UUID getTargetCmdUUID()
    {
        return this.targetCmdUUID;
    }

    /**
     * Setter method for instance variable {@link #targetCmdUUID}.
     *
     * @param _targetCmdUUID value for instance variable {@link #targetCmdUUID}
     */
    public void setTargetCmdUUID(final UUID _targetCmdUUID)
    {
        this.targetCmdUUID = _targetCmdUUID;
    }

    /**
     * Method returns if this pageobject has a target command.
     *
     * @return true if targetCmdUUID !=null
     */
    public boolean hasTargetCmd()
    {
        return this.targetCmdUUID != null;
    }

    /**
     * Getter method for instance variable {@link #targetCmdRevise}.
     *
     * @return value of instance variable {@link #targetCmdRevise}
     */
    public boolean isTargetCmdRevise()
    {
        return this.targetCmdRevise;
    }

    /**
     * Getter method for instance variable {@link #isTargetShowFile}.
     *
     * @return value of instance variable {@link #isTargetShowFile}
     */
    public boolean isTargetShowFile()
    {
        return this.isTargetShowFile;
    }

    /**
     * Setter method for instance variable {@link #targetCmdRevise}.
     *
     * @param _targetCmdRevise value for instance variable
     *            {@link #targetCmdRevise}
     */
    public void setTargetCmdRevise(final boolean _targetCmdRevise)
    {
        this.targetCmdRevise = _targetCmdRevise;
    }

    /**
     * Getter method for instance variable {@link #wizard}.
     *
     * @return value of instance variable {@link #wizard}
     */
    public UIWizardObject getWizard()
    {
        return this.wizard;
    }

    /**
     * Setter method for instance variable {@link #wizard}.
     *
     * @param _wizard value for instance variable {@link #wizard}
     */
    public void setWizard(final UIWizardObject _wizard)
    {
        this.wizard = _wizard;
    }

    /**
     * Setter method for instance variable {@link #partOfWizardCall}.
     *
     * @param _wizardCall value for instance variable {@link #partOfWizardCall}
     */
    public void setPartOfWizardCall(final boolean _wizardCall)
    {
        this.partOfWizardCall = _wizardCall;
    }

    /**
     * Getter method for instance variable {@link #partOfWizardCall}.
     *
     * @return value of instance variable {@link #partOfWizardCall}
     */
    public boolean isPartOfWizardCall()
    {
        return this.partOfWizardCall;
    }

    /**
     * Method to get the value for a key in case of wizard.
     *
     * @param _key key for the value
     * @return value for the object, if found, else null
     */
    protected Object getValue4Wizard(final String _key)
    {
        Object ret = null;
        final Map<String, String[]> para = this.wizard.getParameters(this);
        if (para != null && para.containsKey(_key)) {
            final String[] value = para.get(_key);
            ret = value[0];
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #renderRevise}.
     *
     * @return value of instance variable {@link #renderRevise}
     */
    public boolean isRenderRevise()
    {
        return this.renderRevise;
    }

    /**
     * Setter method for instance variable {@link #renderRevise}.
     *
     * @param _renderRevise value for instance variable {@link #renderRevise}
     */
    public void setRenderRevise(final boolean _renderRevise)
    {
        this.renderRevise = _renderRevise;
    }

    /**
     * Getter method for instance variable {@link #helpTarget}.
     *
     * @return value of instance variable {@link #helpTarget}
     */
    public String getHelpTarget()
    {
        return this.helpTarget;
    }

    /**
     * Getter method for the instance variable {@link #uiID2Oid}.
     *
     * @return value of instance variable {@link #uiID2Oid}
     */
    public Map<String, String> getUiID2Oid()
    {
        return this.uiID2Oid;
    }

    /**
     * @return a new for this instance unique random key.
     */
    public String getNewRandom()
    {
        return ((Integer) this.random.nextInt()).toString();
    }

    /**
     * Used to check the access to e.g. TreeMenus.
     *
     * @param _instances Instances that must be checked for access
     * @return Map with Instance to Boolean
     * @throws EFapsException on error
     */
    protected Map<Instance, Boolean> checkAccessToInstances(final List<Instance> _instances)
        throws EFapsException
    {
        final Map<Type, List<Instance>> types = new HashMap<Type, List<Instance>>();
        for (final Instance instance : _instances) {
            List<Instance> list;
            if (!types.containsKey(instance.getType())) {
                list = new ArrayList<Instance>();
                types.put(instance.getType(), list);
            } else {
                list = types.get(instance.getType());
            }
            list.add(instance);
        }
        // check the access for the given instances
        for (final Entry<Type, List<Instance>> entry : types.entrySet()) {
            this.accessMap.putAll(entry.getKey().checkAccess(entry.getValue(), AccessTypeEnums.SHOW.getAccessType()));
        }
        return this.accessMap;
    }

    /**
     * Getter method for the instance variable {@link #accessMap}.
     *
     * @return value of instance variable {@link #accessMap}
     */
    public Map<Instance, Boolean> getAccessMap()
    {
        return this.accessMap;
    }

    /**
     *
     */
    public String registerOID(final String _oid)
    {
        final String ret = RandomStringUtils.randomAlphanumeric(8);
        getUiID2Oid().put(ret, _oid);
        return ret;
    }
}
