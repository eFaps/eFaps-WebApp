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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.wicket.IClusterable;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldClassification;
import org.efaps.admin.ui.field.FieldCommand;
import org.efaps.admin.ui.field.FieldGroup;
import org.efaps.admin.ui.field.FieldHeading;
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.cell.UIFormCellCmd;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * Class is used to instantiate a form from eFaps into a Form with all Values
 * for the wicket webapp.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIForm extends AbstractUIObject
{

    /**
     * Enum is used to differ the different elements a form can contain.
     */
    public enum ElementType
    {
        /** ELement is a Classification. */
        CLASSIFICATION,
        /** Element is a Form. */
        FORM,
        /** Element is a Heading. */
        HEADING,
        /** Element is SubForm. e.g. for classification*/
        SUBFORM,
        /** Element is a table.*/
        TABLE
    }

    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 3026168649146801622L;

    /**
     * The instance variable stores the different elements of the Form.
     *
     * @see #getElements
     */
    private final List<Element> elements = new ArrayList<Element>();

    /**
     * The instance variable stores the form which must be shown.
     *
     * @see #getForm
     */
    private UUID formUUID;

    /**
     * Used to set if the form is used to upload a file.
     */
    private boolean fileUpload = false;

    /**
     * Map is used to store the new values passed during the creation process
     * from the webapp.
     */
    private final Map<String, String[]> newValues = new HashMap<String, String[]>();

    /**
     * Is this form classified by classification.
     */
    private boolean classified = false;

    /**
     * Constructor using PageParameters.
     *
     * @param _parameters PageParameters from wicket
     */
    public UIForm(final PageParameters _parameters)
    {
        super(_parameters);
        final AbstractCommand command = super.getCommand();
        if (command == null) {
            this.formUUID = null;
        } else if (command.getTargetForm() != null) {
            this.formUUID = command.getTargetForm().getUUID();
        }
    }

    /**
     * Constructor.
     *
     * @param _commandUUID UUID of the command
     * @param _instanceKey oid for this model
     */
    public UIForm(final UUID _commandUUID, final String _instanceKey)
    {
        this(_commandUUID, _instanceKey, null);
    }

    /**
     * Constructor.
     *
     * @param _commandUUID UUID of the command
     * @param _instanceKey oid for this model
     * @param _openerId id of the opener
     */
    public UIForm(final UUID _commandUUID, final String _instanceKey, final String _openerId)
    {
        super(_commandUUID, _instanceKey, _openerId);
        final AbstractCommand command = super.getCommand();
        if (command == null) {
            this.formUUID = null;
        } else if (command.getTargetForm() != null) {
            this.formUUID = command.getTargetForm().getUUID();
        }
    }

    /**
     * Method used to reset this UIForm.
     */
    @Override
    public void resetModel()
    {
        setInitialized(false);
        this.elements.clear();
    }

    /**
     * Method is used to execute the UIForm. (Fill it with data).
     */
    public void execute()
    {
        try {
            if (isCreateMode() || isSearchMode() || getInstance() == null) {
                execute4NoInstance();
            } else {
                execute4Instance();
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        super.setInitialized(true);
    }

    /**
     * Method to execute the form in case that a instance is existing.
     *
     * @throws EFapsException on error
     */
    private void execute4Instance() throws EFapsException
    {
        int rowgroupcount = 1;
        int rowspan = 1;
        FormRow row = new FormRow();

        final Form form = Form.get(this.formUUID);
        // evaluate the ListQuery
        final ListQuery query = evaluateListQuery(form);
        query.execute();
        FormElement formElement = null;
        if (query.next()) {
            boolean addNew = true;
            UIClassification uiclass = null;
            for (final Field field : form.getFields()) {
                if (field.hasAccess(getMode())) {
                    if (field instanceof FieldGroup) {
                        final FieldGroup group = (FieldGroup) field;
                        if (getMaxGroupCount() < group.getGroupCount()) {
                            setMaxGroupCount(group.getGroupCount());
                        }
                        rowgroupcount = group.getGroupCount();
                    } else if (field instanceof FieldTable) {
                        if (!isEditMode()) {
                            final UIFieldTable uiFieldTable = new UIFieldTable(getCommandUUID(), getInstanceKey(),
                                                                               ((FieldTable) field));
                            this.elements.add(new Element(UIForm.ElementType.TABLE, uiFieldTable));
                            addNew = true;
                        }
                    } else if (field instanceof FieldHeading) {
                        if (!isEditMode()) {
                            this.elements.add(new Element(UIForm.ElementType.HEADING,
                                                          new UIHeading((FieldHeading) field)));
                            addNew = true;
                        }
                    } else if (field instanceof FieldClassification) {
                        uiclass = new UIClassification((FieldClassification) field, this);
                        this.elements.add(new Element(UIForm.ElementType.CLASSIFICATION, uiclass));
                        addNew = true;
                        this.classified  = true;
                    } else if (!field.isNoneDisplay(getMode())) {
                        if (addNew) {
                            formElement = new FormElement();
                            this.elements.add(new Element(UIForm.ElementType.FORM, formElement));
                            addNew = false;
                        }
                        addCell2FormRow(row, query, field);

                        if (field.getRowSpan() > 0) {
                            rowspan = field.getRowSpan();
                        }
                        rowgroupcount--;
                        if (rowgroupcount < 1) {
                            rowgroupcount = 1;
                            if (row.getGroupCount() > 0) {
                                formElement.addRowModel(row);
                                row = new FormRow();
                                if (rowspan > 1) {
                                    rowspan--;
                                    row.setRowSpan(true);
                                }
                            }
                        }
                    }
                }
            }
            if (uiclass != null) {
                final List<String> instanceKeys = uiclass.getClassInstanceKeys(getInstance());
                for (final String instanceKey : instanceKeys) {
                    this.elements.add(new Element(UIForm.ElementType.SUBFORM, new UIFieldForm(getCommandUUID(),
                                                                                              instanceKey)));
                }
            }
        }
    }

    /**
     * Method to evaluate the ListQuery for the form. Meaning the selects are
     * added.
     *
     * @param _form Form the query should be evaluated for
     * @return ListQury with all selects for the form
     * @throws EFapsException if error on accesscheck
     */
    private ListQuery evaluateListQuery(final Form _form) throws EFapsException
    {
        final List<Instance> instances = new ArrayList<Instance>();
        instances.add(getInstance());
        final ListQuery ret = new ListQuery(instances);

        for (final Field field : _form.getFields()) {
            if (field.hasAccess(getMode())) {
                if (field.getExpression() != null) {
                    ret.addSelect(field.getExpression());
                }
                if (field.getAlternateOID() != null) {
                    ret.addSelect(field.getAlternateOID());
                }
            }
        }
        return ret;
    }

    /**
     * Method to add a Cell to the given Row.
     *
     * @see #evaluateField(FormRow, ListQuery, Field, Instance, String,
     *      Attribute)
     * @see #evaluateFieldSet(FormRow, ListQuery, Field, String, String)
     *
     * @param _row FormRow to add the cell to
     * @param _query query containing the values
     * @param _field field the cell belongs to
     * @throws EFapsException on error
     */
    private void addCell2FormRow(final FormRow _row, final ListQuery _query, final Field _field)
            throws EFapsException
    {

        Attribute attr = null;
        if (_field.getExpression() != null) {
            attr = _query.getAttribute(_field.getExpression());
        }

        // evaluate the label of the field
        String label;
        if (_field.getLabel() != null) {
            label = _field.getLabel();
        } else if (attr != null) {
            label = attr.getParent().getName() + "/" + attr.getName() + ".Label";
        } else {
            label = "Unknown";
        }

        Instance fieldInstance;
        if (_field.getAlternateOID() != null) {
            fieldInstance = Instance.get((String) _query.get(_field.getAlternateOID()));
        } else {
            fieldInstance = getInstance();
        }

        // fieldset
        if (_field instanceof FieldSet) {
            evaluateFieldSet(_row, _query, _field, fieldInstance, label);
        } else if (_field instanceof FieldCommand) {
            final UIFormCellCmd fieldCmd = new UIFormCellCmd(this, (FieldCommand) _field, fieldInstance, label);
            _row.add(fieldCmd);
        } else {
            evaluateField(_row, _query, _field, fieldInstance, label, attr);
        }
    }

    /**
     * Method evaluates a FieldSet and adds it to the row.
     *
     * @param _row          FormRow to add the cell to
     * @param _query        query containing the values
     * @param _field        field the cell belongs to
     * @param _instance     instance of the FieldSet
     * @param _label        label for the FieldSet
     * @throws EFapsException on error
     */
    private void evaluateFieldSet(final FormRow _row, final ListQuery _query, final Field _field,
                                  final Instance _instance, final String _label)
            throws EFapsException
    {

        final AttributeSet set = AttributeSet.find(getInstance().getType().getName(), _field.getExpression());

        final Map<?, ?> tmp = (Map<?, ?>) _query.get(_field.getExpression());

        final List<Instance> fieldins = new ArrayList<Instance>();

        if (tmp != null) {
            fieldins.addAll(_query.getInstances(_field.getExpression()));
        }
        int idy = 0;
        boolean add = true;
        final UIFormCellSet cellset = new UIFormCellSet(this, new FieldValue(_field, null, "", getInstance()),
                        _instance, "", "", _label, isEditMode());

        final Iterator<Instance> iter = fieldins.iterator();

        while (add) {
            int idx = 0;
            if (iter.hasNext()) {
                cellset.addInstance(idy, iter.next());
            }
            for (final String attrName : ((FieldSet) _field).getOrder()) {
                final Attribute child = set.getAttribute(attrName);
                if (isEditMode()) {
                    final FieldValue fValue = new FieldValue(_field, child, "", getInstance());
                    cellset.addDefiniton(idx, fValue.getEditHtml(getMode(), getInstance(), null));
                }
                if (tmp == null) {
                    add = false;
                } else {
                    final List<?> tmplist = (List<?>) tmp.get(child.getName());
                    if (idy < tmplist.size()) {
                        final Object value = tmplist.get(idy);

                        final FieldValue fieldvalue = new FieldValue(_field, child, value, getInstance());

                        String tmpStr = null;
                        if (_field.isEditableDisplay(getMode())) {
                            tmpStr = fieldvalue.getEditHtml(getMode(), getInstance(), null);
                        } else if (_field.isReadonlyDisplay(getMode())) {
                            tmpStr = fieldvalue.getReadOnlyHtml(getMode(), getInstance(), null);
                        }
                        cellset.add(idx, idy, tmpStr);
                    } else {
                        add = false;
                    }
                }
                idx++;
            }
            idy++;
        }
        // we only add multiline if we have a value or we are in
        // editmodus
        if (tmp != null || isEditMode()) {
            _row.add(cellset);
        }
    }

    /**
     * Method evaluates a FieldSet and adds it to the row.
     *
     * @param _row FormRow to add the cell to
     * @param _query query containing the values
     * @param _field field the cell belongs to
     * @param _fieldInstance instance of the Field
     * @param _label label for the Field
     * @param _attr attribute for the Field
     * @throws EFapsException on error
     */
    private void evaluateField(final FormRow _row, final ListQuery _query, final Field _field,
                    final Instance _fieldInstance, final String _label, final Attribute _attr) throws EFapsException
    {
        Object value = null;
        if (_field.getExpression() != null) {
            value = _query.get(_field.getExpression());
        }

        final FieldValue fieldvalue = new FieldValue(_field, _attr, value, _fieldInstance);

        String strValue = null;
        if (isEditMode() && _field.isEditableDisplay(getMode())) {
            strValue = fieldvalue.getEditHtml(getMode(), getInstance(), null);
        } else if (_field.isReadonlyDisplay(getMode())) {
            strValue = fieldvalue.getReadOnlyHtml(getMode(), getInstance(), null);
        }
        if (strValue != null && !this.fileUpload) {
            final String tmp = strValue.replaceAll(" ", "");
            if (tmp.toLowerCase().contains("type=\"file\"")) {
                this.fileUpload = true;
            }
        }
        String icon = _field.getIcon();
        if (_fieldInstance != null) {
            if (_field.isShowTypeIcon() && _fieldInstance.getType() != null) {
                final Image image = Image.getTypeIcon(_fieldInstance.getType());
                if (image != null) {
                    icon = image.getUrl();
                }
            }
            final String uiType = (_attr != null) ? _attr.getAttributeType().getName() : "";

            _row.add(new UIFormCell(this, fieldvalue, _fieldInstance, strValue, icon, _label, uiType));
        }
    }

    /**
     * Method to execute the form in case of create or search.
     *
     * @throws EFapsException on error
     */
    private void execute4NoInstance() throws EFapsException
    {
        int rowgroupcount = 1;
        FormRow row = new FormRow();
        final Form form = Form.get(this.formUUID);

        Type type = null;
        if (isCreateMode() || isEditMode()) {
            type = getCreateTargetType();
        } else {
            final List<EventDefinition> events = getCommand().getEvents(EventType.UI_TABLE_EVALUATE);
            if (events != null) {
                for (final EventDefinition eventDef : events) {
                    final String tmp = eventDef.getProperty("Types");
                    if (tmp != null) {
                        type = Type.get(tmp);
                        break;
                    }
                }
            }
        }

        FormElement formelement = new FormElement();
        boolean addNew = true;
        for (final Field field : form.getFields()) {
            if (field.hasAccess(getMode()) && !field.isNoneDisplay(getMode())) {
                if (field instanceof FieldGroup) {
                    final FieldGroup group = (FieldGroup) field;
                        if (getMaxGroupCount() < group.getGroupCount()) {
                            setMaxGroupCount(group.getGroupCount());
                        }
                    rowgroupcount = group.getGroupCount();
                } else if (field instanceof FieldHeading) {
                    this.elements.add(new Element(UIForm.ElementType.HEADING, new UIHeading((FieldHeading) field)));
                    addNew = true;
                } else if (field instanceof FieldClassification) {
                    this.elements.add(new Element(UIForm.ElementType.CLASSIFICATION,
                                                  new UIClassification((FieldClassification) field, this)));
                    this.classified = true;
                    addNew = true;
                } else if (field instanceof FieldTable) {
                    final UIFieldTable uiFieldTable = new UIFieldTable(getCommandUUID(), getInstanceKey(),
                                                                       ((FieldTable) field));
                    this.elements.add(new Element(UIForm.ElementType.TABLE, uiFieldTable));
                    addNew = true;
                } else {
                    if (addNew) {
                        formelement = new FormElement();
                        this.elements.add(new Element(UIForm.ElementType.FORM, formelement));
                        addNew = false;
                    }

                    final Attribute attr = type != null ? type.getAttribute(field.getExpression()) : null;

                    String label;
                    if (field.getLabel() != null) {
                        label = field.getLabel();
                    } else if (attr != null) {
                        label = attr.getParent().getName() + "/" + attr.getName() + ".Label";
                    } else {
                        label = "Unknown";
                    }
                    final Instance fieldInstance = getInstance();
                    final FieldValue fieldvalue = new FieldValue(field, attr,
                                    super.isWizardCall() ? getValue4Wizard(field.getName()) : null, fieldInstance);

                    String strValue = null;
                    if (isCreateMode() || isSearchMode()) {
                        strValue = fieldvalue.getEditHtml(getMode(), getInstance(), null);
                    } else if (isViewMode()) {
                        strValue = fieldvalue.getReadOnlyHtml(getMode(), getInstance(), null);
                    }
                    final String attrTypeName = attr != null ? attr.getAttributeType().getName() : null;

                    final UIFormCell cell;
                    if (field instanceof FieldCommand) {
                        cell = new UIFormCellCmd(this, (FieldCommand) field, null, label);
                    } else {
                        cell = new UIFormCell(this, fieldvalue, strValue, label, attrTypeName);
                        if (isSearchMode()) {
                            cell.setReference(null);
                        } else if (strValue != null && !this.fileUpload) {
                            final String tmp = strValue.replaceAll(" ", "");
                            if (tmp.toLowerCase().contains("type=\"file\"")) {
                                this.fileUpload = true;
                            }
                        }
                    }

                    row.add(cell);
                    rowgroupcount--;
                    if (rowgroupcount < 1) {
                        rowgroupcount = 1;
                        if (row.getGroupCount() > 0) {
                            formelement.addRowModel(row);
                            row = new FormRow();
                        }
                    }
                }
            }

        }
    }

    /**
     * Method to get the type that will be created by a form. A method must be
     * used so it can be overwritten.
     * @return Type to be created
     */
    protected Type getCreateTargetType()
    {
        return getCommand().getTargetCreateType();
    }

    /**
     * Method to get the Form from eFaps using the instance variable
     * {@link #formUUID}.
     *
     * @return From from eFaps
     */
    public Form getForm()
    {
        return Form.get(this.formUUID);
    }

    /**
     * This is the getter method for the instance variable {@link #formUUID}.
     *
     * @return value of instance variable {@link #formUUID}
     */
    public UUID getUUID()
    {
        return this.formUUID;
    }

    /**
     * This is the getter method for the instance variable {@link #formUUID}.
     *
     * @return value of instance variable {@link #formUUID}
     */
    public UUID getFormUUID()
    {
        return this.formUUID;
    }

    /**
     * This is the setter method for the instance variable {@link #formUUID}.
     *
     * @param _formUUID the formUUID to set
     */
    public void setFormUUID(final UUID _formUUID)
    {
        this.formUUID = _formUUID;
    }

    /**
     * This is the getter method for the instance variable {@link #elements}.
     *
     * @return value of instance variable {@link #elements}
     */
    public List<Element> getElements()
    {
        return this.elements;
    }

    /**
     * This is the getter method for the instance variable {@link #fileUpload}.
     *
     * @return value of instance variable {@link #fileUpload}
     */
    public boolean isFileUpload()
    {
        return this.fileUpload;
    }

    /**
     * This is the setter method for the instance variable {@link #fileUpload}.
     *
     * @param _fileUpload the fileUpload to set
     */
    public void setFileUpload(final boolean _fileUpload)
    {
        this.fileUpload = _fileUpload;
    }

    /**
     * This is the getter method for the instance variable {@link #newValues}.
     *
     * @return value of instance variable {@link #newValues}
     */
    public Map<String, String[]> getNewValues()
    {
        return this.newValues;
    }

    /**
     * Getter method for instance variable {@link #classified}.
     *
     * @return value of instance variable {@link #classified}
     */
    public boolean isClassified()
    {
        return this.classified;
    }

    /**
     * Class is used as store for one Row in the UIForm.
     */
    public class FormRow implements IClusterable
    {

        /**
         * Used for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Stores the UIFormCell contained in this FormRow.
         */
        private final List<UIFormCell> values = new ArrayList<UIFormCell>();

        /**
         * Stores if the row must be spanned.
         */
        private boolean rowSpan = false;

        /**
         * Add a UIFormCell to this FormRow.
         *
         * @param _uiFormCell UIFormCell to add
         */
        public void add(final UIFormCell _uiFormCell)
        {
            this.values.add(_uiFormCell);
        }

        /**
         * Setter method for the instance variable {@link #rowSpan}.
         *
         * @param _rowspan value for instance variable {@link #rowSpan}
         */
        public void setRowSpan(final boolean _rowspan)
        {
            this.rowSpan = _rowspan;
        }

        /**
         * Getter method for the instance variable {@link #rowSpan}.
         *
         * @return value of instance variable {@link #rowSpan}
         */
        public boolean isRowSpan()
        {
            return this.rowSpan;
        }

        /**
         * Getter method for the instance variable {@link #values}.
         *
         * @return value of instance variable {@link #values}
         */
        public List<UIFormCell> getValues()
        {
            return this.values;
        }

        /**
         * Method to get the group count of this row.
         *
         * @return size of the values
         */
        public int getGroupCount()
        {
            return this.values.size();
        }
    }

    /**
     * Class represents a Element of Type Form used in a Form.
     */
    public class FormElement implements IFormElement, IClusterable
    {
        /**
         * Used for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Stores the FormRows for this FormElement.
         */
        private final List<UIForm.FormRow> rowModels = new ArrayList<UIForm.FormRow>();

        /**
         * Add a FormRow to this FormElement.
         *
         * @param _formRow FormRow to add
         */
        public void addRowModel(final FormRow _formRow)
        {
            this.rowModels.add(_formRow);
        }

        /**
         * This is the getter method for the instance variable
         * {@link #rowModels}.
         *
         * @return value of instance variable {@link #rowModels}
         */
        public List<UIForm.FormRow> getRowModels()
        {
            return this.rowModels;
        }
    }

    /**
     * Class represent one Element in a UIForm.
     */
    public class Element implements IClusterable
    {

        /**
         * Used for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * ElementType of this Element.
         */
        private final ElementType type;

        /**
         * Model of this Element.
         */
        private final IFormElement element;

        /**
         * Constructor setting the instance variables.
         *
         * @param _type ElementType of this Element
         * @param _formElement Model of this Element
         */
        public Element(final ElementType _type, final IFormElement _formElement)
        {
            this.type = _type;
            this.element = _formElement;
        }

        /**
         * Getter method for the instance variable {@link #type}.
         *
         * @return value of instance variable {@link #type}
         */
        public ElementType getType()
        {
            return this.type;
        }

        /**
         * Getter method for the instance variable {@link #element}.
         *
         * @return value of instance variable {@link #element}
         */
        public IFormElement getElement()
        {
            return this.element;
        }
    }
}
