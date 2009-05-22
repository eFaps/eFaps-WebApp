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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.IClusterable;

import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.field.FieldClassification;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
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
     * Name of the base classification type.
     */
    private final String classificationName;

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
     * @param _field FielClassification
     */
    public UIClassification(final FieldClassification _field)
    {
        this.fieldId = _field.getId();
        this.classificationName = _field.getClassificationName();
    }

    /**
     * Private constructor used for instantiating child UIClassification.
     * @param _classificationName name of the classificaton type
     */
    private UIClassification(final String _classificationName)
    {
        this.fieldId = 0;
        this.classificationName = _classificationName;
        this.label = DBProperties.getProperty(this.classificationName + ".Label");
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
     * Getter method for instance variable {@link #classificationName}.
     *
     * @return value of instance variable {@link #classificationName}
     */
    public String getClassificationName()
    {
        return this.classificationName;
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
    private void execute()
    {
        this.initialized = true;
        this.label = DBProperties.getProperty(this.classificationName + ".Label");
        final Classification type = (Classification) Type.get(this.classificationName);
        addChildren(type.getChildClassifications());
    }

    private void addChildren(final Set<Classification> _children) {
        for (final Classification child : _children) {
            final UIClassification childUI = new UIClassification(child.getName());
            childUI.addChildren(child.getChildClassifications());
            this.children.add(childUI);
        }
    }

    /**
     * @param classification
     * @param instance
     * @throws EFapsException
     */
    public static List<String> getClassification(final String _classification, final Instance _instance)
            throws EFapsException
    {
        final List<String> ret = new ArrayList<String>();
        final Classification classType = (Classification) Type.get(_classification);
        final SearchQuery query = new SearchQuery();
        query.setExpand(_instance,
                        classType.getClassifyRelation().getName() + "\\" + classType.getRelLinkAttribute());
        query.addSelect(classType.getRelTypeAttribute());
        query.execute();
        while (query.next()) {
            final Long typeid = (Long) query.get(classType.getRelTypeAttribute());
            final Classification subClassType = (Classification) Type.get(typeid);
            final SearchQuery subquery = new SearchQuery();
            subquery.setExpand(_instance,
                               subClassType.getName() + "\\" + subClassType.getLinkAttribute());
            subquery.addSelect("OID");
            subquery.execute();
            if (subquery.next()) {
                //TODO must return an instanceKey!!! not necessary the oid
                final String instanceKey = (String) subquery.get("OID");
                ret.add(instanceKey);
            }
            subquery.close();
        }
        query.close();
        return ret;
    }
}
