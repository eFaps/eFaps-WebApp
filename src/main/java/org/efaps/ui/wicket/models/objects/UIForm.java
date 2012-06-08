/*
 * Copyright 2003 - 2012 The eFaps Team
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.util.io.IClusterable;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldChart;
import org.efaps.admin.ui.field.FieldClassification;
import org.efaps.admin.ui.field.FieldCommand;
import org.efaps.admin.ui.field.FieldGroup;
import org.efaps.admin.ui.field.FieldHeading;
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.ui.wicket.models.cell.CellSetValue;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.cell.UIFormCellChart;
import org.efaps.ui.wicket.models.cell.UIFormCellCmd;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.cell.UIHiddenCell;
import org.efaps.ui.wicket.models.cell.UISetColumnHeader;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;
/**
 * Class is used to instantiate a form from eFaps into a Form with all Values
 * for the wicket webapp.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIForm
    extends AbstractUIPageObject
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
        /** Element is a StructurBrowser.*/
        STRUCBRWS,
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
     * Constructor.
     *
     * @param _commandUUID UUID of the command
     * @param _instanceKey oid for this model
     */
    public UIForm(final UUID _commandUUID,
                  final String _instanceKey)
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
    public UIForm(final UUID _commandUUID,
                  final String _instanceKey,
                  final String _openerId)
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
        getHiddenCells().clear();
    }

    /**
     * Method is used to execute the UIForm. (Fill it with data).
     */
    @Override
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
     * @throws EFapsException
     *
     * @throws EFapsException on error
     */
    private void execute4Instance()
        throws EFapsException
    {
        int rowgroupcount = 1;
        int rowspan = 1;
        FormRow row = new FormRow();
        final Set<String>altOIDSel = new HashSet<String>();
        final Form form = Form.get(this.formUUID);
        // evaluate the Form to make the query
        final PrintQuery print = new PrintQuery(getInstance());
        for (final Field field : form.getFields()) {
            if (field.hasAccess(getMode(), getInstance(), getCommand()) && !field.isNoneDisplay(getMode())) {
                if (field.getSelect() != null) {
                    print.addSelect(field.getSelect());
                } else if (field.getAttribute() != null) {
                    print.addAttribute(field.getAttribute());
                } else if (field.getPhrase() != null) {
                    print.addPhrase(field.getName(), field.getPhrase());
                }
                if (field.getSelectAlternateOID() != null) {
                    print.addSelect(field.getSelectAlternateOID());
                    altOIDSel.add(field.getSelectAlternateOID());
                }
            }
        }
        if (print.execute()) {
            if (!altOIDSel.isEmpty()) {
                final List<Instance> inst = new ArrayList<Instance>();
                for (final String sel : altOIDSel) {
                    inst.addAll(print.getInstances4Select(sel));
                }
                checkAccessToInstances(inst);
            }
            FormElement formElement = null;
            boolean addNew = true;
            UIClassification uiclass = null;
            for (final Field field : form.getFields()) {
                if (field.hasAccess(getMode(), getInstance(), getCommand()) && !field.isNoneDisplay(getMode())) {
                    if (field instanceof FieldGroup) {
                        final FieldGroup group = (FieldGroup) field;
                        // in case that the first field is a group the element must be initiated
                        if (formElement == null) {
                            formElement = new FormElement();
                            this.elements.add(new Element(UIForm.ElementType.FORM, formElement));
                            addNew = false;
                        }
                        if (formElement.getMaxGroupCount() < group.getGroupCount()) {
                            formElement.setMaxGroupCount(group.getGroupCount());
                        }
                        rowgroupcount = group.getGroupCount();
                    } else if (field instanceof FieldTable) {
                        if (((FieldTable) field).getTargetStructurBrowserField() == null) {
                            final UIFieldTable uiFieldTable = new UIFieldTable(getCommandUUID(), getInstanceKey(),
                                                                                   (FieldTable) field);
                            this.elements.add(new Element(UIForm.ElementType.TABLE, uiFieldTable));
                        } else {
                            final UIFieldStructurBrowser uiFieldStrucBrws = new UIFieldStructurBrowser(getCommandUUID(),
                                            getInstanceKey(), (FieldTable) field);
                            this.elements.add(new Element(UIForm.ElementType.STRUCBRWS, uiFieldStrucBrws));
                        }
                        addNew = true;
                    } else if (field instanceof FieldHeading) {
                        this.elements.add(new Element(UIForm.ElementType.HEADING,
                                                          new UIHeading((FieldHeading) field)));
                        addNew = true;
                    } else if (field instanceof FieldClassification) {
                        uiclass = new UIClassification((FieldClassification) field, this);
                        this.elements.add(new Element(UIForm.ElementType.CLASSIFICATION, uiclass));
                        addNew = true;
                        this.classified  = true;
                    } else {
                        if (addNew) {
                            formElement = new FormElement();
                            this.elements.add(new Element(UIForm.ElementType.FORM, formElement));
                            addNew = false;
                        }
                        if (addCell2FormRow(row, print, field)) {
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
            }
            if (uiclass != null) {
                final Map<UUID, String> instanceKeys = uiclass.getClassInstanceKeys(getInstance());
                if (instanceKeys.size() > 0) {
                    if (!uiclass.isInitialized()) {
                        uiclass.execute();
                    }
                    // add the root classification
                    this.elements.add(new Element(UIForm.ElementType.SUBFORM, new UIFieldForm(getCommandUUID(),
                                     instanceKeys.get(uiclass.getClassificationUUID()))));
                    addChildrenClassificationForms(uiclass, instanceKeys);
                }
            }
        }
    }

    /**
     * Method to add a Cell to the given Row.
     *
     * @param _row FormRow to add the cell to
     * @param _query query containing the values
     * @param _field field the cell belongs to
     * @throws EFapsException on error
     * @return true if the cell was actually added, else false
     */
    private boolean addCell2FormRow(final FormRow _row,
                                    final PrintQuery _query,
                                    final Field _field)
        throws EFapsException
    {
        boolean ret = true;
        Attribute attr = null;
        if (_field.getAttribute() != null) {
            attr = _query.getAttribute4Attribute(_field.getAttribute());
        } else if (_field.getSelect() != null) {
            attr = _query.getAttribute4Select(_field.getSelect());
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
        if (_field.getSelectAlternateOID() != null
                        && (_query.getSelect(_field.getSelectAlternateOID()) instanceof String)) {
            fieldInstance = Instance.get(_query.<String>getSelect(_field.getSelectAlternateOID()));
        } else {
            fieldInstance = getInstance();
        }
        if (_field.isHiddenDisplay(getMode())) {
            Object value = null;
            if (_field.getAttribute() != null) {
                value = _query.getAttribute(_field.getAttribute());
            } else if (_field.getSelect() != null) {
                value = _query.getSelect(_field.getSelect());
            } else if (_field.getPhrase() != null) {
                value = _query.getPhrase(_field.getName());
            }
            final FieldValue fieldvalue = new FieldValue(_field, attr, value, fieldInstance, getInstance());
            final String strValue = fieldvalue.getHiddenHtml(getMode());
            addHidden(new UIHiddenCell(this, fieldvalue, null, strValue));
            ret = false;
        } else {
            // fieldset
            if (_field instanceof FieldSet) {
                evaluateFieldSet(_row, _query, _field, fieldInstance, label);
            } else if (_field instanceof FieldCommand) {
                final UIFormCellCmd fieldCmd = new UIFormCellCmd(this, (FieldCommand) _field, fieldInstance, label);
                _row.add(fieldCmd);
            } else if (_field instanceof FieldChart) {
                final UIFormCellChart fieldChart = new UIFormCellChart(this, (FieldChart) _field, fieldInstance, label);
                _row.add(fieldChart);
            } else {
                evaluateField(_row, _query, _field, fieldInstance, label, attr);
            }
        }
        return ret;

    }

    /**
     * Method evaluates a Field and adds it to the row.
     *
     * @param _row              FormRow to add the cell to
     * @param _query            query containing the values
     * @param _field            field the cell belongs to
     * @param _fieldInstance    instance of the Field
     * @param _label            label for the Field
     * @param _attr             attribute for the Field
     * @throws EFapsException on error
     */
    private void evaluateField(final FormRow _row,
                               final PrintQuery _query,
                               final Field _field,
                               final Instance _fieldInstance,
                               final String _label,
                               final Attribute _attr)
        throws EFapsException
    {
        Object value = null;
        if (_field.getAttribute() != null) {
            value = _query.<Object>getAttribute(_field.getAttribute());
        } else if (_field.getSelect() != null) {
            value = _query.<Object>getSelect(_field.getSelect());
        } else if (_field.getPhrase() != null) {
            value = _query.getPhrase(_field.getName());
        }

        final FieldValue fieldvalue = new FieldValue(_field, _attr, value, _fieldInstance, getInstance());

        String strValue = null;
        if (isPrintMode()) {
            strValue = fieldvalue.getStringValue(getMode());
        } else {
            if (isEditMode() && _field.isEditableDisplay(getMode())) {
                strValue = fieldvalue.getEditHtml(getMode());
            } else if (_field.isReadonlyDisplay(getMode())) {
                strValue = fieldvalue.getReadOnlyHtml(getMode());
            }
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
            _row.add(new UIFormCell(this, fieldvalue, _fieldInstance, strValue, null, icon, _label, uiType));
        }
    }

    /**
     * Method evaluates a FieldSet and adds it to the row.
     *
     * @param _row              FormRow to add the cell to
     * @param _query            query containing the values
     * @param _field            field the cell belongs to
     * @param _fieldInstance    instance of the FieldSet
     * @param _label            label for the FieldSet
     * @throws EFapsException on error
     */
    private void evaluateFieldSet(final FormRow _row,
                                  final PrintQuery _query,
                                  final Field _field,
                                  final Instance _fieldInstance,
                                  final String _label)
        throws EFapsException
    {
        final AttributeSet set = AttributeSet.find(getInstance().getType().getName(), _field.getAttribute());

        final Map<?, ?> tmp = (Map<?, ?>) _query.getAttributeSet(_field.getAttribute());

        final List<Instance> fieldins = new ArrayList<Instance>();

        if (tmp != null) {
            fieldins.addAll(_query.getInstances4Attribute(_field.getAttribute()));
        }

        final FieldValue fieldValue = new FieldValue(_field, null, "", _fieldInstance, getInstance());
        final UIFormCellSet cellset = new UIFormCellSet(this, fieldValue, _fieldInstance, "", "", _label, isEditMode());

        for (final String attrName : ((FieldSet) _field).getOrder()) {
            final Attribute child = set.getAttribute(attrName);
            final UISetColumnHeader column = new UISetColumnHeader(_field.getLabel(), child);
            cellset.addHeader(column);
        }

        final Iterator<Instance> iter = fieldins.iterator();
        final Map<String, Iterator<?>> values = new HashMap<String, Iterator<?>>();
        while (iter.hasNext()) {
            final Instance rowInstance = iter.next();
            cellset.addRow(rowInstance);
            for (final String attrName : ((FieldSet) _field).getOrder()) {
                final Attribute child = set.getAttribute(attrName);
                Iterator<?> valIter = values.get(attrName);
                if (valIter == null) {
                    final List<?> tmplist = (List<?>) tmp.get(attrName);
                    valIter = tmplist.iterator();
                    values.put(attrName, valIter);
                }
                final UIValue uiValue = UIValue.get(_field, child, valIter.next());
                cellset.addValue(rowInstance,
                                new CellSetValue(rowInstance.getKey(), this, cellset, uiValue));
            }
        }
        _row.add(cellset);
    }

    /**
     * Recursive method to add the children classification forms.
     * @param _uiclass      parent classification form
     * @param _instanceKeys mapo of instancekeys
     * @throws EFapsException o nerro
     */
    private void addChildrenClassificationForms(final UIClassification _uiclass,
                                                final Map<UUID, String> _instanceKeys)
        throws EFapsException
    {
        for (final UIClassification childClass : _uiclass.getChildren()) {
            if (_instanceKeys.containsKey(childClass.getClassificationUUID())) {
                this.elements.add(new Element(UIForm.ElementType.SUBFORM, new UIFieldForm(getCommandUUID(),
                                    _instanceKeys.get(childClass.getClassificationUUID()))));
            }
            addChildrenClassificationForms(childClass, _instanceKeys);
        }
    }

    /**
     * Method to execute the form in case of create or search.
     *
     * @throws EFapsException on error
     */
    private void execute4NoInstance()
        throws EFapsException
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

        FormElement formelement = null;
        boolean addNew = true;
        UIClassification uiclass = null;
        boolean firstTable = true;
        for (final Field field : form.getFields()) {
            if (field.hasAccess(getMode(), getInstance(), getCommand()) && !field.isNoneDisplay(getMode())) {
                if (field instanceof FieldGroup) {
                    final FieldGroup group = (FieldGroup) field;
                 // in case that the first field is a group the element must be initiated
                    if (formelement == null) {
                        formelement = new FormElement();
                        this.elements.add(new Element(UIForm.ElementType.FORM, formelement));
                        addNew = false;
                    }
                    if (formelement.getMaxGroupCount() < group.getGroupCount()) {
                        formelement.setMaxGroupCount(group.getGroupCount());
                    }
                    rowgroupcount = group.getGroupCount();
                } else if (field instanceof FieldHeading) {
                    this.elements.add(new Element(UIForm.ElementType.HEADING, new UIHeading((FieldHeading) field)));
                    addNew = true;
                } else if (field instanceof FieldClassification) {
                    uiclass = new UIClassification((FieldClassification) field, this);
                    this.elements.add(new Element(UIForm.ElementType.CLASSIFICATION, uiclass));
                    this.classified = true;
                    addNew = true;
                } else if (field instanceof FieldTable) {
                    if (((FieldTable) field).getTargetStructurBrowserField() == null) {
                        final UIFieldTable uiFieldTable = new UIFieldTable(getCommandUUID(), getInstanceKey(),
                                                                           (FieldTable) field);
                        this.elements.add(new Element(UIForm.ElementType.TABLE, uiFieldTable));
                        if (firstTable) {
                            firstTable = false;
                        } else {
                            uiFieldTable.setFirstTable(false);
                        }
                    } else {
                        final UIFieldStructurBrowser uiFieldStrucBrws = new UIFieldStructurBrowser(getCommandUUID(),
                                        getInstanceKey(), (FieldTable) field);
                        this.elements.add(new Element(UIForm.ElementType.STRUCBRWS, uiFieldStrucBrws));
                    }
                    addNew = true;
                } else {
                    if (addNew) {
                        formelement = new FormElement();
                        this.elements.add(new Element(UIForm.ElementType.FORM, formelement));
                        addNew = false;
                    }
                    final String fieldAttrName = field.getAttribute();
                    final Attribute attr = type != null ? type.getAttribute(fieldAttrName) : null;

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
                                    super.isPartOfWizardCall() ? getValue4Wizard(field.getName()) : null,
                                                    fieldInstance, getInstance());

                    String strValue = null;
                    boolean hidden = false;
                    if (isPrintMode()) {
                        strValue = fieldvalue.getStringValue(getMode());
                    } else {
                        if ((isCreateMode() || isSearchMode()) && field.isEditableDisplay(getMode())) {
                            strValue = fieldvalue.getEditHtml(getMode());
                        } else if (field.isHiddenDisplay(getMode())) {
                            strValue = fieldvalue.getHiddenHtml(getMode());
                            hidden = true;
                        } else {
                            strValue = fieldvalue.getReadOnlyHtml(getMode());
                        }
                    }

                    final String attrTypeName = attr != null ? attr.getAttributeType().getName() : null;
                    if (hidden) {
                        addHidden(new UIHiddenCell(this, fieldvalue, null, strValue));
                    } else {
                        final UIFormCell cell;
                        if (field instanceof FieldSet) {
                            cell = new UIFormCellSet(this, fieldvalue, null, "", "", label, isCreateMode());
                            final AttributeSet set = AttributeSet.find(type.getName(), fieldAttrName);
                            for (final String attrName : ((FieldSet) field).getOrder()) {
                                final Attribute child = set.getAttribute(attrName);
                                final UISetColumnHeader column = new UISetColumnHeader(field.getLabel(), child);
                                ((UIFormCellSet) cell).addHeader(column);
                            }
                        } else if (field instanceof FieldCommand) {
                            cell = new UIFormCellCmd(this, (FieldCommand) field, null, label);
                        } else {
                            cell = new UIFormCell(this, fieldvalue, strValue, "", label, attrTypeName);
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
        if (uiclass != null) {
            final Set<Classification> clazzes = getCommand().getTargetCreateClassification();
            for (final Classification clazz : clazzes) {
                uiclass.addSelectedUUID(clazz.getUUID());
                Classification parent = (Classification) clazz.getParentClassification();
                while (parent != null) {
                    uiclass.addSelectedUUID(parent.getUUID());
                    parent = (Classification) parent.getParentClassification();
                }
            }
            if (!uiclass.isInitialized()) {
                uiclass.execute();
            }
            add2Elements4Create(uiclass);
        }
    }

    /**
     * Recursive method that adds the classification forms as elements to the
     * form by walking down the tree.
     *
     * @param _parentClass the classification to be added
     * @throws EFapsException on error
     */
    private void add2Elements4Create(final UIClassification _parentClass)
        throws EFapsException
    {
        if (_parentClass.isSelected()) {
            final UIFieldForm fieldform = new UIFieldForm(getCommandUUID(), _parentClass);
            fieldform.setMode(TargetMode.CREATE);
            this.elements.add(new Element(UIForm.ElementType.SUBFORM, fieldform));
        }
        for (final UIClassification child : _parentClass.getChildren()) {
            add2Elements4Create(child);
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
    public class FormElement
        implements IFormElement, IClusterable
    {
        /**
         * Used for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Stores the maximal group count for a row.
         *
         * @see #getMaxGroupCount
         * @see #setMaxGroupCount
         */
        private int maxGroupCount = 1;

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

        /**
         * This is the getter method for the instance variable
         * {@link #maxGroupCount}.
         *
         * @return value of instance variable {@link #maxGroupCount}
         * @see #maxGroupCount
         * @see #setMaxGroupCount
         */
        public int getMaxGroupCount()
        {
            return this.maxGroupCount;
        }

        /**
         * This is the setter method for the instance variable
         * {@link #maxGroupCount}.
         *
         * @param _maxGroupCount new value for instance variable {@link #maxGroupCount}
         * @see #maxGroupCount
         * @see #getMaxGroupCount
         */
        protected void setMaxGroupCount(final int _maxGroupCount)
        {
            this.maxGroupCount = _maxGroupCount;
        }
    }

    /**
     * Class represent one Element in a UIForm.
     */
    public class Element
        implements IClusterable
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
        public Element(final ElementType _type,
                       final IFormElement _formElement)
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
