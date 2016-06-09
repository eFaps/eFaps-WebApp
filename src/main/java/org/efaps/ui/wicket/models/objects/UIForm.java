/*
 * Copyright 2003 - 2016 The eFaps Team
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldClassification;
import org.efaps.admin.ui.field.FieldCommand;
import org.efaps.admin.ui.field.FieldGroup;
import org.efaps.admin.ui.field.FieldHeading;
import org.efaps.admin.ui.field.FieldPicker;
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.PrintQuery;
import org.efaps.db.SelectBuilder;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.field.UICmdField;
import org.efaps.ui.wicket.models.field.UIField;
import org.efaps.ui.wicket.models.field.UIPicker;
import org.efaps.ui.wicket.models.field.set.UIFieldSet;
import org.efaps.ui.wicket.models.field.set.UIFieldSetColHeader;
import org.efaps.ui.wicket.models.field.set.UIFieldSetValue;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class is used to instantiate a form from eFaps into a Form with all Values
 * for the wicket webapp.
 *
 * @author The eFaps Team
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
        /** Element is a StructurBrowser. */
        STRUCBRWS,
        /** Element is SubForm. e.g. for classification */
        SUBFORM,
        /** Element is a table. */
        TABLE
    }

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(UIForm.class);

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
     * @throws CacheReloadException on error
     */
    public UIForm(final UUID _commandUUID,
                  final String _instanceKey)
        throws CacheReloadException
    {
        this(_commandUUID, _instanceKey, null);
    }

    /**
     * Constructor.
     *
     * @param _commandUUID UUID of the command
     * @param _instanceKey oid for this model
     * @param _openerId id of the opener
     * @throws CacheReloadException on error
     */
    public UIForm(final UUID _commandUUID,
                  final String _instanceKey,
                  final String _openerId)
        throws CacheReloadException
    {
        super(_commandUUID, _instanceKey, _openerId);
        if (_commandUUID != null) {
            final AbstractCommand command = super.getCommand();
            if (command == null) {
                this.formUUID = null;
            } else if (command.getTargetForm() != null) {
                this.formUUID = command.getTargetForm().getUUID();
            }
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
        getHidden().clear();
    }

    /**
     * Method is used to execute the UIForm. (Fill it with data).
     */
    @Override
    public void execute()
    {
        try {
            // evaluate now to give the chance to change the mode
            if (evaluate4Instance()) {
                if (isCreateMode() || isSearchMode()) {
                    execute4NoInstance();
                } else {
                    if (getInstance() == null) {
                        execute4NoInstance();
                    } else {
                        execute4Instance();
                    }
                }
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        super.setInitialized(true);
    }

    /**
     * This is a possibility to replace the current Instance for the form with
     * another one using an table evaluate esjp.
     *
     * @return true, if successful
     * @throws EFapsException on error
     */
    protected boolean evaluate4Instance()
        throws EFapsException
    {
        boolean ret = true;
        if (!isSearchMode()) {
            final List<Return> returns = getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
                            ParameterValues.INSTANCE, getInstance(),
                            ParameterValues.PARAMETERS, Context.getThreadContext().getParameters(),
                            ParameterValues.CLASS, this);
            for (final Return retu : returns) {
                if (retu.contains(ReturnValues.INSTANCE)) {
                    final Object object = retu.get(ReturnValues.INSTANCE);
                    if (object != null && object instanceof Instance && ((Instance) object).isValid()) {
                        setInstanceKey(((Instance) object).getOid());
                    } else {
                        UIForm.LOG.error("The esjp called by Command '{}' must return a valid instance",
                                        getCommand().getName());
                    }
                } else {
                    ret = false;
                }
            }
        }
        return ret;
    }

    /**
     * Method to execute the form in case that a instance is existing.
     *
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
        final Set<String> altOIDSel = new HashSet<String>();
        final Form form = Form.get(this.formUUID);
        // evaluate the Form to make the query
        final PrintQuery print = new PrintQuery(getInstance());
        for (final Field field : form.getFields()) {
            if (field.hasAccess(getMode(), getInstance(), getCommand(), getInstance())
                            && !field.isNoneDisplay(getMode())) {
                if (field.getSelect() != null) {
                    print.addSelect(field.getSelect());
                } else if (field.getAttribute() != null) {
                    print.addAttribute(field.getAttribute());
                } else if (field.getPhrase() != null) {
                    print.addPhrase(field.getName(), field.getPhrase());
                }  else if (field.getMsgPhrase() != null) {
                    print.addMsgPhrase(new SelectBuilder(getBaseSelect4MsgPhrase(field)), field.getMsgPhrase());
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
            boolean firstTable = true;
            for (final Field field : form.getFields()) {
                if (field.hasAccess(getMode(), getInstance(), getCommand(), getInstance())
                                && !field.isNoneDisplay(getMode())) {
                    if (field instanceof FieldGroup) {
                        final FieldGroup group = (FieldGroup) field;
                        // in case that the first field is a group the element
                        // must be initiated
                        if (formElement == null) {
                            formElement = new FormElement();
                            this.elements.add(new Element(UIForm.ElementType.FORM, formElement));
                            addNew = false;
                        }

                        rowgroupcount = group.getGroupCount();
                    } else if (field instanceof FieldTable) {
                        if (((FieldTable) field).getTargetStructurBrowserField() == null) {
                            final UIFieldTable uiFieldTable = new UIFieldTable(getCommandUUID(), getInstanceKey(),
                                            (FieldTable) field);
                            uiFieldTable.setMode(getMode());
                            this.elements.add(new Element(UIForm.ElementType.TABLE, uiFieldTable));
                            if (firstTable) {
                                firstTable = false;
                            } else {
                                uiFieldTable.setFirstTable(false);
                            }
                        } else {
                            final UIFieldStructurBrowser uiFieldStrucBrws = new UIFieldStructurBrowser(
                                            getCommandUUID(),
                                            getInstanceKey(), (FieldTable) field);
                            uiFieldStrucBrws.setMode(getMode());
                            this.elements.add(new Element(UIForm.ElementType.STRUCBRWS, uiFieldStrucBrws));
                        }
                        addNew = true;
                    } else if (field instanceof FieldHeading) {
                        this.elements.add(new Element(UIForm.ElementType.HEADING,
                                        new UIHeading((FieldHeading) field)));
                        addNew = true;
                    } else if (field instanceof FieldClassification) {
                        uiclass = UIClassification.getUIClassification(field, this);
                        this.elements.add(new Element(UIForm.ElementType.CLASSIFICATION, uiclass));
                        addNew = true;
                        this.classified = true;
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
                                 //   formElement.addRowModel(row);
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
                        uiclass.execute(getInstance());
                    }
                    addClassElements(uiclass, instanceKeys);
                }
            }
        }
    }

    /**
     * Method that adds the classification forms as elements to the form by
     * walking down the tree.
     *
     * @param _uiclass the classification to be added
     * @param _instanceKeys map from uuid to instance keys
     * @throws EFapsException on error
     */
    public void addClassElements(final UIClassification _uiclass,
                                 final Map<UUID, String> _instanceKeys)
        throws EFapsException
    {
        this.elements.addAll(getClassElements(_uiclass, _instanceKeys));
    }

    /**
     * Method that removes all classifcations and afterwards adds the
     * classification forms as elements to the form by walking down the tree.
     *
     * @param _uiclass the classification to be added
     * @throws EFapsException on error
     */
    public void updateClassElements(final UIClassification _uiclass)
        throws EFapsException
    {
        // remove previous added classification forms
        final Iterator<Element> iter2 = this.elements.iterator();
        final Map<UUID, String> uuid2InstanceKey = new HashMap<UUID, String>();
        while (iter2.hasNext()) {
            final IFormElement element = iter2.next().getElement();
            if (element instanceof UIFieldForm) {
                final String instanceKey = ((UIFieldForm) element).getInstanceKey();
                if (instanceKey != null) {
                    final UUID classUUID = ((UIFieldForm) element).getClassificationUUID();
                    uuid2InstanceKey.put(classUUID, instanceKey);
                }
                iter2.remove();
            }
        }
        addClassElements(_uiclass, uuid2InstanceKey);
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

        final Instance fieldInstance;
        if (_field.getSelectAlternateOID() != null
                        && _query.getSelect(_field.getSelectAlternateOID()) instanceof String) {
            fieldInstance = Instance.get(_query.<String>getSelect(_field.getSelectAlternateOID()));
        } else {
            fieldInstance = getInstance();
        }
        if (_field.isHiddenDisplay(getMode())) {
            if (_field.getAttribute() != null) {
                _query.getAttribute(_field.getAttribute());
            } else if (_field.getSelect() != null) {
                _query.getSelect(_field.getSelect());
            } else if (_field.getPhrase() != null) {
                _query.getPhrase(_field.getName());
            }
            addHidden(evaluateUIProvider(_row, _query, _field, fieldInstance, attr));
            ret = false;
        } else {
            // fieldset
            if (_field instanceof FieldSet) {
                evaluateFieldSet(_row, _query, _field, fieldInstance);
            } else if (_field instanceof FieldCommand) {
                final UICmdField cell = new UICmdField(this, (FieldCommand) _field, getInstance());
                _row.add(cell);
            } else if (_field instanceof FieldPicker) {
                final UIField cell = new UIField(this, getInstance().getKey(),
                                UIValue.get(_field, attr, null).setClassObject(this).setInstance(getInstance())
                                    .setCallInstance(getInstance()));
                final UIPicker picker = new UIPicker((FieldPicker) _field, cell);
                cell.setPicker(picker);
                _row.add(cell);
            } else {
                _row.add(evaluateUIProvider(_row, _query, _field, fieldInstance, attr));
            }
        }
        return ret;
    }

    /**
     * Method evaluates a Field and adds it to the row.
     *
     * @param _row FormRow to add the cell to
     * @param _print query containing the values
     * @param _field field the cell belongs to
     * @param _fieldInstance instance of the Field
     * @param _attr attribute for the Field
     * @return the UI field
     * @throws EFapsException on error
     */
    private UIField evaluateUIProvider(final FormRow _row,
                                       final PrintQuery _print,
                                       final Field _field,
                                       final Instance _fieldInstance,
                                       final Attribute _attr)
        throws EFapsException
    {
        Object value = null;
        if (_field.getAttribute() != null) {
            value = _print.<Object>getAttribute(_field.getAttribute());
        } else if (_field.getSelect() != null) {
            value = _print.<Object>getSelect(_field.getSelect());
        } else if (_field.getPhrase() != null) {
            value = _print.getPhrase(_field.getName());
        } else if (_field.getMsgPhrase() != null) {
            value = _print.getMsgPhrase(new SelectBuilder(getBaseSelect4MsgPhrase(_field)), _field.getMsgPhrase());
        }
        final UIField uiField = new UIField(this, getInstance().getKey(), UIValue.get(_field, _attr, value)
                        .setInstance(_fieldInstance).setClassObject(this));
        return uiField;
    }

    /**
     * Method evaluates a FieldSet and adds it to the row.
     *
     * @param _row FormRow to add the cell to
     * @param _query query containing the values
     * @param _field field the cell belongs to
     * @param _fieldInstance instance of the FieldSet
     * @throws EFapsException on error
     */
    private void evaluateFieldSet(final FormRow _row,
                                  final PrintQuery _query,
                                  final Field _field,
                                  final Instance _fieldInstance)
        throws EFapsException
    {
        final AttributeSet attrSet = AttributeSet.find(getInstance().getType().getName(), _field.getAttribute());

        final Map<?, ?> tmp = (Map<?, ?>) _query.getAttributeSet(_field.getAttribute());

        final List<Instance> fieldins = new ArrayList<Instance>();

        if (tmp != null) {
            fieldins.addAll(_query.getInstances4Attribute(_field.getAttribute()));
        }
        final UIFieldSet set = new UIFieldSet(this, _fieldInstance,
                        UIValue.get(_field, getInstance().getType().getAttribute(_field.getAttribute()), null));
        set.setMode(getMode());

        for (final String attrName : ((FieldSet) _field).getOrder()) {
            final Attribute child = attrSet.getAttribute(attrName);
            final UIFieldSetColHeader column = new UIFieldSetColHeader(_field.getLabel(), child, _field);
            set.addHeader(column);
        }

        final Iterator<Instance> iter = fieldins.iterator();
        final Map<String, Iterator<?>> values = new HashMap<String, Iterator<?>>();
        while (iter.hasNext()) {
            final Instance rowInstance = iter.next();
            set.addRow(rowInstance);
            for (final String attrName : ((FieldSet) _field).getOrder()) {
                final Attribute child = attrSet.getAttribute(attrName);
                Iterator<?> valIter = values.get(attrName);
                if (valIter == null) {
                    final List<?> tmplist = (List<?>) tmp.get(attrName);
                    valIter = tmplist.iterator();
                    values.put(attrName, valIter);
                }
                final UIValue uiValue = UIValue.get(_field, child, valIter.hasNext() ? valIter.next() : null);
                set.addValue(rowInstance,
                                new UIFieldSetValue(this, rowInstance.getKey(), set, uiValue));
            }
        }
        _row.add(set);
    }

    /**
     * Recursive method to add the children classification forms.
     *
     * @param _uiclass parent classification form
     * @param _uuid2InstanceKey mapping of instancekeys
     * @return List of elements to be added
     * @throws EFapsException on error
     */
    private List<Element> getClassElements(final UIClassification _uiclass,
                                           final Map<UUID, String> _uuid2InstanceKey)
        throws EFapsException
    {
        final List<Element> ret = new ArrayList<Element>();
        if (_uiclass.isSelected() && !_uiclass.isRoot()) {
            final UIFieldForm fieldform;
            if (_uuid2InstanceKey.containsKey(_uiclass.getClassificationUUID())) {
                fieldform = new UIFieldForm(getCommandUUID(),
                                _uuid2InstanceKey.get(_uiclass.getClassificationUUID()));
            } else {
                fieldform = new UIFieldForm(getCommandUUID(), _uiclass);
                if (isEditMode()) {
                    // in edit mode, if there is no classification yet, create
                    // mode must be forced
                    fieldform.setMode(TargetMode.CREATE);
                }
            }
            ret.add(new Element(ElementType.SUBFORM, fieldform));
        }
        for (final UIClassification child : _uiclass.getChildren()) {
            ret.addAll(getClassElements(child, _uuid2InstanceKey));
        }
        Collections.sort(ret, new Comparator<Element>()
        {

            @Override
            public int compare(final Element _o1,
                               final Element _o2)
            {
                return ((UIFieldForm) _o1.getElement()).getWeight().compareTo(
                                ((UIFieldForm) _o2.getElement()).getWeight());
            }
        });
        return ret;
    }

    /**
     * Method to execute the form in case of create or search.
     *
     * @throws EFapsException on error
     */
    private void execute4NoInstance()
        throws EFapsException
    {
        final Form form = Form.get(this.formUUID);
        Type type = null;
        if (isCreateMode() || isEditMode()) {
            type = getCreateTargetType();
        } else {
            final List<EventDefinition> events = getCommand().getEvents(EventType.UI_TABLE_EVALUATE);
            if (events != null) {
                for (final EventDefinition eventDef : events) {
                    String tmp = eventDef.getProperty("Type");
                    if (tmp == null) {
                        tmp = eventDef.getProperty("Types");
                        if (tmp != null) {
                            UIForm.LOG.warn("Event '{}' uses deprecated API for type.", eventDef.getName());
                        } else {
                            UIForm.LOG.error("Event '{}' is not type property", eventDef.getName());
                        }
                    }
                    if (tmp != null) {
                        type = Type.get(tmp);
                        break;
                    }
                }
            }
        }

        FormElement currentFormElement = null;
        boolean addNew = true;
        UIClassification uiclass = null;
        boolean firstTable = true;
        for (final Field field : form.getFields()) {
            if (field.hasAccess(getMode(), AbstractInstanceObject.getInstance4Create(type), getCommand(), getInstance())
                            && !field.isNoneDisplay(getMode())) {
                if (field instanceof FieldGroup) {
                    final FieldGroup group = (FieldGroup) field;
                    // in case that the first field is a group the element must be initiated
                    if (currentFormElement == null) {
                        currentFormElement = new FormElement();
                        this.elements.add(new Element(UIForm.ElementType.FORM, currentFormElement));
                        addNew = false;
                    }
                    currentFormElement.setGroupCount(group.getGroupCount());
                } else if (field instanceof FieldHeading) {
                    this.elements.add(new Element(UIForm.ElementType.HEADING, new UIHeading((FieldHeading) field)));
                    addNew = true;
                } else if (field instanceof FieldClassification) {
                    uiclass = UIClassification.getUIClassification(field, this);
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
                        final FormElement formElement = new FormElement()
                                        .setGroupCount(currentFormElement == null
                                            ? 0
                                            :currentFormElement.getGroupCount());
                        currentFormElement = formElement;
                        this.elements.add(new Element(UIForm.ElementType.FORM, currentFormElement));
                        addNew = false;
                    }
                    final Attribute attr;
                    if (field.getAttribute() == null && field.getSelect() != null && type != null) {
                        final PrintQuery print = new PrintQuery(getInstance4Create(type));
                        print.addSelect(field.getSelect());
                        print.dryRun();
                        attr = print.getAttribute4Select(field.getSelect());
                    } else {
                        attr = type != null ? type.getAttribute(field.getAttribute()) : null;
                    }

                    if (field.isHiddenDisplay(getMode())) {
                        final UIField uiField = new UIField(this, null, UIValue.get(field, attr, null)
                                        .setClassObject(this)
                                        .setInstance(getInstance()).setCallInstance(getInstance()));
                        addHidden(uiField);
                    } else {
                        final AbstractInstanceObject cell;
                        if (field instanceof FieldSet) {
                            cell = new UIFieldSet(this,  getInstance(), UIValue.get(field, attr, null)
                                                .setClassObject(this)
                                                .setInstance(getInstance())
                                                .setCallInstance(getInstance()));
                            if (type == null) {
                                ((UIFieldSet) cell).addHeader(new UIFieldSetColHeader(field.getLabel(), null, field));
                            } else {
                                final AttributeSet set = AttributeSet.find(type.getName(), field.getAttribute());
                                for (final String attrName : ((FieldSet) field).getOrder()) {
                                    final Attribute child = set.getAttribute(attrName);
                                    final UIFieldSetColHeader column = new UIFieldSetColHeader(field.getLabel(), child,
                                                    field);
                                    ((UIFieldSet) cell).addHeader(column);
                                }
                            }
                        } else if (field instanceof FieldCommand) {
                            cell = new UICmdField(this, (FieldCommand) field, getInstance());
                        } else if (field instanceof FieldPicker) {
                            cell = new UIField(this, null, UIValue.get(field, attr, null).setClassObject(this)
                                            .setInstance(getInstance()).setCallInstance(getInstance()));
                            final UIPicker picker = new UIPicker((FieldPicker) field, cell);
                            ((UIField) cell).setPicker(picker);
                        } else {
                            cell = new UIField(this, null, UIValue.get(field, attr,
                                                super.isPartOfWizardCall() ? getValue4Wizard(field.getName()) : null)
                                                .setClassObject(this)
                                                .setInstance(AbstractInstanceObject.getInstance4Create(type))
                                                .setCallInstance(getInstance()));
                        }
                        currentFormElement.addValue(cell);
                    }
                }
            }
        }
        if (uiclass != null) {
            final Set<Classification> clazzes = getCommand().getTargetCreateClassification();
            for (final Classification clazz : clazzes) {
                uiclass.addSelectedUUID(clazz.getUUID());
                Classification parent = clazz.getParentClassification();
                while (parent != null) {
                    uiclass.addSelectedUUID(parent.getUUID());
                    parent = parent.getParentClassification();
                }
            }
            if (!uiclass.isInitialized()) {
                uiclass.execute(null);
            }
            this.elements.addAll(getClassElements(uiclass, new HashMap<UUID, String>()));
        }
    }

    /**
     * Method to get the type that will be created by a form. A method must be
     * used so it can be overwritten.
     *
     * @return Type to be created
     * @throws CacheReloadException on error
     */
    protected Type getCreateTargetType()
        throws CacheReloadException
    {
        return getCommand().getTargetCreateType();
    }

    /**
     * Method to get the Form from eFaps using the instance variable
     * {@link #formUUID}.
     *
     * @return Form from eFaps
     * @throws CacheReloadException on error
     */
    public Form getForm()
        throws CacheReloadException
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
    public final List<Element> getElements()
    {
        return Collections.unmodifiableList(this.elements);
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
    public static class FormRow
        implements IClusterable
    {

        /**
         * Used for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Stores the UIFormCell contained in this FormRow.
         */
        private final List<AbstractInstanceObject> values = new ArrayList<AbstractInstanceObject>();

        /**
         * Stores if the row must be spanned.
         */
        private boolean rowSpan = false;

        /**
         * Add a UIFormCell to this FormRow.
         *
         * @param _uiObject UIObject to add
         */
        public void add(final AbstractInstanceObject _uiObject)
        {
            this.values.add(_uiObject);
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
        public List<AbstractInstanceObject> getValues()
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
    public static class FormElement
        implements IFormElement, IClusterable
    {

        /**
         * Used for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Stores the FormRows for this FormElement.
         */
        private final ArrayDeque<FormRow> rowModels = new ArrayDeque<>();

        /** The group count. */
        private int groupCount = 0;

        /**
         * This is the getter method for the instance variable
         * {@link #rowModels}.
         *
         * @return value of instance variable {@link #rowModels}
         */
        public Iterator<FormRow> getRowModels()
        {
            return this.rowModels.iterator();
        }

        /**
         * Adds the value.
         *
         * @param _uiObject the ui object
         * @return the form element
         */
        public FormElement addValue(final AbstractInstanceObject _uiObject)
        {
            if (this.groupCount < 1) {
                this.rowModels.addLast(new FormRow());
            } else {
                this.groupCount--;
            }
            if (this.rowModels.isEmpty()) {
                this.rowModels.addLast(new FormRow());
            }
            this.rowModels.getLast().add(_uiObject);
            return this;
        }

        /**
         * Sets the group count.
         *
         * @param _groupCount the new group count
         */
        public FormElement setGroupCount(final int _groupCount)
        {
            this.groupCount = _groupCount;
            return this;
        }

        /**
         * Gets the group count.
         *
         * @return the group count
         */
        public int getGroupCount()
        {
            return this.groupCount;
        }
    }

    /**
     * Class represent one Element in a UIForm.
     */
    public static class Element
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
