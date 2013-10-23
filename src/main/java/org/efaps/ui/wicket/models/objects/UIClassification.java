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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.wicket.util.io.IClusterable;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class is used as a model for a classification.
 *
 * @author The eFaps Team
 * @version $Id: UIClassification.java 8530 2013-01-16 01:56:29Z jan@moxter.net$
 */
public class UIClassification
implements IFormElement, IClusterable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Static part of the key to get the Information stored in the session in
     * relation to this StruturBrowser.
     */
    private static final String USERSESSIONKEY = "org.efaps.ui.wicket.models.objects.UIClassification.UserSessionKey";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UIClassification.class);

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
    private final List<UIClassification> children = new ArrayList<UIClassification>();

    /**
     * UUID of the classification this UIClassification belongs to.
     */
    private final UUID classificationUUID;

    /**
     * List of classification UUIDs that are connected to the given base
     * instance. This variable is used only for the root. In any instances it
     * will be empty.
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

    /**
     * Is this UIClassification a base classification.
     */
    private final boolean base;

    /**
     * Target mode.
     */
    private final TargetMode mode;

    /**
     * Stores the name of the command that called this object. Needed for
     * getting DBProperties.
     */
    private String commandName;

    /**
     * Is this classification multipleselect.
     */
    private final boolean multipleSelect;

    /**
     * Is this model expanded.
     */
    private boolean expanded;

    /**
     * Instance thisclassificatio belongs to.
     */
    private Instance instance;

    /**
     * @param _fieldId id of the FieldClassification
     * @param _uiObject ui object
     * @throws EFapsException on error
     */
    private UIClassification(final long _fieldId,
                             final AbstractUIObject _uiObject)
                                             throws EFapsException
                                             {
        this.classificationUUID = null;
        this.multipleSelect = true;
        this.fieldId = _fieldId;
        this.root = true;
        this.base = false;
        this.mode = _uiObject.getMode();
        this.commandName = _uiObject.getCommand().getName();
        this.instance = _uiObject.getInstance();
                                             }

    /**
     * Private constructor used for instantiating child UIClassification.
     *
     * @param _uuid UUID of the classification type
     * @param _mode target mode
     * @param _base is base classfication
     * @throws CacheReloadException on error
     */
    private UIClassification(final UUID _uuid,
                             final TargetMode _mode,
                             final boolean _base)
                                             throws CacheReloadException
                                             {
        this.multipleSelect = Classification.get(_uuid).isMultipleSelect();
        this.fieldId = 0;
        this.classificationUUID = _uuid;
        this.label = DBProperties.getProperty(Type.get(this.classificationUUID).getName() + ".Label");
        this.root = false;
        this.mode = _mode;
        this.base = _base;
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
     * Getter method for the instance variable {@link #multipleSelect}.
     *
     * @return value of instance variable {@link #multipleSelect}
     */
    public boolean isMultipleSelect()
    {
        return this.multipleSelect;
    }

    /**
     * Getter method for the instance variable {@link #expanded}.
     *
     * @return value of instance variable {@link #expanded}
     */
    public boolean isExpanded()
    {
        return this.expanded;
    }

    /**
     * Setter method for instance variable {@link #expanded}.
     *
     * @param _expanded value for instance variable {@link #expanded}
     */
    private void setExpandedInternal(final boolean _expanded)
    {
        this.expanded = _expanded;
    }

    /**
     * Setter method for instance variable {@link #expanded}.
     *
     * @param _expanded value for instance variable {@link #expanded}
     */
    public void setExpanded(final boolean _expanded)
    {
        this.expanded = _expanded;
        storeInSession();
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
     * Getter method for instance variable {@link #root}.
     *
     * @return value of instance variable {@link #root}
     */
    public boolean isRoot()
    {
        return this.root;
    }

    /**
     * Getter method for the instance variable {@link #base}.
     *
     * @return value of instance variable {@link #base}
     */
    public boolean isBase()
    {
        return this.base;
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
     * Execute the model.
     *
     * @throws EFapsException on error
     */
    public void execute(final Instance _instance)
                    throws EFapsException
                    {
        UIClassification clazz = this;
        while (!clazz.isRoot()) {
            clazz = clazz.getParent();
        }
        clazz.initialized = true;
        for (final UIClassification child :  clazz.getChildren()) {
            final Classification type = (Classification) Type.get(child.getClassificationUUID());
            if (clazz.selectedUUID.contains(child.getClassificationUUID())) {
                child.setSelected(true);
            }
            child.addChildren(child, type.getChildClassifications(), clazz.selectedUUID, _instance);
            clazz.expand();
        }
                    }

    /**
     * Expand the Tree.
     */
    @SuppressWarnings("unchecked")
    private void expand()
    {
        try {
            final String key = getCacheKey();
            if (Context.getThreadContext().containsSessionAttribute(key)) {
                final Set<UUID> sessMap = (Set<UUID>) Context
                                .getThreadContext().getSessionAttribute(key);
                setExpandedInternal(sessMap.contains(this.classificationUUID));
                for (final UIClassification uiClazz : getDescendants()) {
                    if (sessMap.contains(uiClazz.classificationUUID)) {
                        uiClazz.setExpandedInternal(true);
                    }
                }
            }
        } catch (final EFapsException e) {
            UIClassification.LOG.error("Error reading Session info for UICLassificagtion called by Filed with ID: {}",
                            this.fieldId, e);
        }
    }

    /**
     * Store the Information in the Session.
     */
    @SuppressWarnings("unchecked")
    private void storeInSession()
    {
        try {
            final Set<UUID> sessMap;
            final String key = getCacheKey();
            if (Context.getThreadContext().containsSessionAttribute(key)) {
                sessMap = (Set<UUID>) Context.getThreadContext().getSessionAttribute(key);
            } else {
                sessMap = new HashSet<UUID>();
            }
            if (this.expanded) {
                sessMap.add(this.classificationUUID);
            } else if (sessMap.contains(this.classificationUUID)) {
                sessMap.remove(this.classificationUUID);
            }
            Context.getThreadContext().setSessionAttribute(key, sessMap);
        } catch (final EFapsException e) {
            UIClassification.LOG.error("Error storing Session info for UICLassificagtion called by Filed with ID: {}",
                            this.fieldId, e);
        }
    }

    /**
     * Recursive method used to add the children to this UIClassification.
     *
     * @param _parent parent
     * @param _children children
     * @param _selectedUUID set of selected classification uuids
     * @param _instance instance the classifcation belongs to
     * @throws EFapsException on error
     */
    private void addChildren(final UIClassification _parent,
                             final Set<Classification> _children,
                             final Set<UUID> _selectedUUID,
                             final Instance _instance)
                                             throws EFapsException
                                             {
        for (final Classification child : _children) {
            boolean access;
            if (!child.isAbstract()) {
                final Instance inst = AbstractInstanceObject.getInstance4Create(child);

                access = child.hasAccess(inst, getMode() == TargetMode.CREATE
                                || getMode() == TargetMode.EDIT ? AccessTypeEnums.CREATE.getAccessType()
                                                : AccessTypeEnums.SHOW.getAccessType());
            } else {
                access = true;
            }
            if (access) {
                final UIClassification childUI = new UIClassification(child.getUUID(), _parent.mode, false);
                if (_selectedUUID.contains(child.getUUID())) {
                    childUI.selected = true;
                }
                childUI.addChildren(childUI, child.getChildClassifications(), _selectedUUID, _instance);
                _parent.children.add(childUI);
                childUI.setParent(_parent);
            }
        }
        Collections.sort(_parent.children, new Comparator<UIClassification>()
                        {

            @Override
            public int compare(final UIClassification _class0,
                               final UIClassification _class2)
            {
                return _class0.getLabel().compareTo(_class2.getLabel());
            }
                        });
                                             }

    /**
     * Method to get the key to the instances related to this classification.
     *
     * @param _instance Instance the related instance key are searched for
     * @return Map of instance keys
     * @throws EFapsException on error
     */
    public Map<UUID, String> getClassInstanceKeys(final Instance _instance)
                    throws EFapsException
                    {
        final Map<UUID, String> ret = new HashMap<UUID, String>();
        UIClassification clazz = this;
        while (!clazz.isRoot()) {
            clazz = clazz.getParent();
        }
        Type reltype = null;
        for (final UIClassification child :  clazz.getChildren()) {
            final Classification classType = (Classification) Type.get(child.getClassificationUUID());
            if (!classType.getClassifyRelationType().equals(reltype)) {
                final QueryBuilder queryBldr = new QueryBuilder(classType.getClassifyRelationType());
                queryBldr.addWhereAttrEqValue(classType.getRelLinkAttributeName(), _instance.getId());
                final MultiPrintQuery multi = queryBldr.getPrint();
                multi.addAttribute(classType.getRelTypeAttributeName());
                multi.execute();
                while (multi.next()) {
                    final Long typeid = multi.<Long>getAttribute(classType.getRelTypeAttributeName());
                    final Classification subClassType = (Classification) Type.get(typeid);
                    final QueryBuilder subQueryBldr = new QueryBuilder(subClassType);
                    subQueryBldr.addWhereAttrEqValue(subClassType.getLinkAttributeName(), _instance.getId());
                    subQueryBldr.addOrderByAttributeAsc("ID");
                    final InstanceQuery query = subQueryBldr.getQuery();
                    query.execute();
                    if (query.next()) {
                        // TODO must return an instanceKey!!! not necessary the oid
                        final String instanceKey = query.getCurrentValue().getOid();
                        ret.put(query.getCurrentValue().getType().getUUID(), instanceKey);
                        this.selectedUUID.add(query.getCurrentValue().getType().getUUID());
                    }
                }
            }
            reltype = classType.getClassifyRelationType();
        }
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
    public List<UIClassification> getChildren()
    {
        return this.children;
    }

    /**
     * @return a flat list of all decscandants.
     */
    public List<UIClassification> getDescendants()
    {
        final List<UIClassification> ret = new ArrayList<UIClassification>();
        for (final UIClassification uiClass : getChildren()) {
            ret.add(uiClass);
            ret.addAll(uiClass.getDescendants());
        }
        return ret;
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
     *
     * @param _parent value for instance variable {@link #parent}
     */
    private void setParent(final UIClassification _parent)
    {
        this.parent = _parent;
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

    /**
     * Method to add a uuid to the set of selected classifications. This method
     * should only be called on a root classification. e.;g. on cretae mode to
     * set the default selected classifications.
     *
     * @param _uuid uuid to set as selected
     */
    public void addSelectedUUID(final UUID _uuid)
    {
        this.selectedUUID.add(_uuid);
    }

    /**
     * This method generates the Key for a UserAttribute by using the UUID of
     * the Command and the given static part, so that for every StruturBrowser a
     * unique key for expand etc, is created.
     *
     * @return String with the key
     */
    public String getCacheKey()
    {
        String ret = "noKey";
        UIClassification clazz = this;
        while (!clazz.isRoot()) {
            clazz = clazz.getParent();
        }
        final Field field = Field.get(clazz.getFieldId());
        if (field != null) {
            try {
                ret = field.getCollection().getUUID().toString() + "-" + field.getName() + "-"
                                + UIClassification.USERSESSIONKEY;
            } catch (final CacheReloadException e) {
                UIClassification.LOG.error("Cannot generate CacheKey", e);
            }
        }
        return ret;
    }

    @Override
    public String toString()
    {
        return getLabel();
    }

    /**
     * @return instance
     */
    public Instance getInstance()
    {
        Instance ret = this.instance;
        UIClassification clazz = this;
        while (!clazz.isRoot() && ret == null) {
            clazz = clazz.getParent();
            ret = clazz.instance;
        }
        return ret;
    }

    /**
     * @param _field FieldClassification
     * @param _uiObject ui object
     * @throws EFapsException on error
     * @return new UIClassifcation
     */
    public static UIClassification getUIClassification(final Field _field,
                                                       final AbstractUIObject _uiObject)
                                                                       throws EFapsException
                                                                       {
        final String[] names = _field.getClassificationName().split(";");
        final UIClassification root = new UIClassification(_field.getId(), _uiObject);
        for (final String className : names) {
            final Classification clazz = Classification.get(className);
            if (clazz.hasAccess(root.getInstance(), root.getMode() == TargetMode.CREATE
                            || root.getMode() == TargetMode.EDIT ? AccessTypeEnums.CREATE.getAccessType()
                                            : AccessTypeEnums.SHOW.getAccessType())) {
                final UIClassification childUI = new UIClassification(clazz.getUUID(), root.getMode(), true);
                root.children.add(childUI);
                childUI.setParent(root);
            }
        }
        return root;
                                                                       }
}
