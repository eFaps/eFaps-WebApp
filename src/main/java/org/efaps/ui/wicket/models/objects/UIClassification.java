/*
 * Copyright 2003 - 2009 The eFaps Team
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
import java.util.Set;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.IClusterable;

import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.FieldClassification;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * Class is used as a model for a classification.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIClassification implements IFormElement, IClusterable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Id of the field this UIClassification belongs to.
     */
    private final long fieldId;

    /**
     * Is this UIClassification initialized.
     */
    private boolean initialized = false;

    /**
     * Label for this UIClassification.
     */
    private String label;

    /**
     * Is this Classification selected.
     */
    private boolean selected;

    /**
     * Set containing the children of this UIClassification.
     */
    private final Set<UIClassification> children = new HashSet<UIClassification>();

    /**
     * UUID of the classification this UIClassification belongs to.
     */
    private final UUID classificationUUID;

    /**
     * List of classification UUIDs that are connected to the given base
     * instance. This variable is used only for the root. In any instances
     * it will be empty.
     */
    private final Set<UUID> selectedUUID = new HashSet<UUID>();

    /**
     * Contains the parent UIClassification.
     */
    private UIClassification parent;

    /**
     * Is this UIClassification the root.
     */
    private final boolean root;

    private final TargetMode mode;

    /**
     * Stores the name of the command that called this object. Needed for
     * getting DBProperties.
     */
    private String commandName;

    /**
     * @param _field FielClassification
     */
    public UIClassification(final FieldClassification _field, final AbstractUIObject _uiObject)
    {
        this.fieldId = _field.getId();
        this.classificationUUID = Type.get(_field.getClassificationName()).getUUID();
        this.root = true;
        this.mode = _uiObject.getMode();
        this.commandName = _uiObject.getCommand().getName();

    }

    /**
     * Private constructor used for instantiating child UIClassification.
     *
     * @param _uuid UUID of the classification type
     */
    private UIClassification(final UUID _uuid, final TargetMode _mode)
    {
        this.fieldId = 0;
        this.classificationUUID = _uuid;
        this.label = DBProperties.getProperty(Type.get(this.classificationUUID).getName() + ".Label");
        this.root = false;
        this.mode = _mode;
    }

    /**
     * Getter method for instance variable {@link #fieldId}.
     *
     * @return value of instance variable {@link #fieldId}
     */
    public long getFieldId()
    {
        return this.fieldId;
    }

    /**
     * Method to get the tree for the classification.
     * @return a TreeModel containing the classification hirachy
     */
    public TreeModel getTreeModel()
    {
        if (!this.initialized) {
            execute();
        }
        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(this);
        addNodes(rootNode, this.children);
        final TreeModel model = new DefaultTreeModel(rootNode);
        return model;
    }

    /**
     * Getter method for instance variable {@link #selected}.
     *
     * @return value of instance variable {@link #selected}
     */
    public boolean isSelected()
    {
        return this.selected;
    }

    /**
     * Setter method for instance variable {@link #selected}.
     *
     * @param _selected value for instance variable {@link #selected}
     */
    public void setSelected(final boolean _selected)
    {
        this.selected = _selected;
    }

    /**
     * Getter method for instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Recursive method used to fill the TreeModel.
     *
     * @see #getTreeModel()
     * @param _parent ParentNode children should be added
     * @param _children to be added as childs
     */
    private void addNodes(final DefaultMutableTreeNode _parent, final Set<UIClassification> _children)
    {
        for (final UIClassification child : _children) {
            final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
            child.addNodes(childNode, child.children);
            _parent.add(childNode);
        }
    }


    /**
     *
     */
    public void execute()
    {
        this.initialized = true;
        final Classification type = (Classification) Type.get(this.classificationUUID);
        if (this.selectedUUID.contains(this.classificationUUID)) {
            this.selected = true;
        }
        this.label = DBProperties.getProperty(type.getName() + ".Label");
        addChildren(this, type.getChildClassifications(), this.selectedUUID);
    }

    /**
     * Recursive method used to add the children to this UIClassification.
     * @param _parent        parent
     * @param _children     children
     * @param _selectedUUID set of selected classification uuids
     */
    private void addChildren(final UIClassification _parent, final Set<Classification> _children,
                             final Set<UUID> _selectedUUID)
    {
        for (final Classification child : _children) {
            final UIClassification childUI = new UIClassification(child.getUUID(), _parent.mode);
            if (_selectedUUID.contains(child.getUUID())) {
                childUI.selected = true;
            }
            childUI.addChildren(childUI, child.getChildClassifications(), _selectedUUID);
            _parent.children.add(childUI);
            childUI.setParent(_parent);
        }
    }

    /**
     * Method to get the key to the instances related to this classification.
     *
     * @param _instance          Instance the related instance key are searched for
     * @return list of instance keys
     * @throws EFapsException on error
     */
    public List<String> getClassInstanceKeys(final Instance _instance)
            throws EFapsException
    {
        final List<String> ret = new ArrayList<String>();
        final Classification classType = (Classification) Type.get(this.classificationUUID);
        final SearchQuery query = new SearchQuery();
        query.setExpand(_instance,
                        classType.getClassifyRelationType().getName() + "\\" + classType.getRelLinkAttributeName());
        query.addSelect(classType.getRelTypeAttributeName());
        query.execute();
        while (query.next()) {
            final Long typeid = (Long) query.get(classType.getRelTypeAttributeName());
            final Classification subClassType = (Classification) Type.get(typeid);
            final SearchQuery subquery = new SearchQuery();
            subquery.setExpand(_instance,
                               subClassType.getName() + "\\" + subClassType.getLinkAttributeName());
            subquery.addSelect("OID");
            subquery.execute();
            if (subquery.next()) {
                //TODO must return an instanceKey!!! not necessary the oid
                final String instanceKey = (String) subquery.get("OID");
                ret.add(instanceKey);
                this.selectedUUID.add(subquery.getType().getUUID());
            }
            subquery.close();
        }
        query.close();
        return ret;
    }

    /**
     * Getter method for instance variable {@link #initialized}.
     *
     * @return value of instance variable {@link #initialized}
     */
    public boolean isInitialized()
    {
        return this.initialized;
    }

    /**
     * Getter method for instance variable {@link #classificationUUID}.
     *
     * @return value of instance variable {@link #classificationUUID}
     */
    public UUID getClassificationUUID()
    {
        return this.classificationUUID;
    }

    /**
     * Getter method for instance variable {@link #children}.
     *
     * @return value of instance variable {@link #children}
     */
    public Set<UIClassification> getChildren()
    {
        return this.children;
    }

    /**
     * Getter method for instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public UIClassification getParent()
    {
        return this.parent;
    }

    /**
     * Setter method for instance variable {@link #parent}.
     * @param _parent value for instance variable {@link #parent}
     */
    private void setParent(final UIClassification _parent)
    {
       this.parent = _parent;
    }

    /**
     * Getter method for instance variable {@link #root}.
     *
     * @return value of instance variable {@link #root}
     */
    public boolean isRoot()
    {
        return this.root;
    }

    /**
     * Getter method for instance variable {@link #mode}.
     *
     * @return value of instance variable {@link #mode}
     */
    public TargetMode getMode()
    {
        return this.mode;
    }

    /**
     * Getter method for instance variable {@link #commandName}.
     *
     * @return value of instance variable {@link #commandName}
     */
    public String getCommandName()
    {
        return this.commandName;
    }


}
