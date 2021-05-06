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

package org.efaps.ui.wicket.models.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.ui.UIValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.ui.field.Field;
import org.efaps.api.ui.IOption;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.behaviors.dojo.AutoCompleteBehavior;
import org.efaps.ui.wicket.components.values.LabelField;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.field.AutoCompleteSettings.EditValue;
import org.efaps.ui.wicket.models.field.factories.AutoCompleteFactory;
import org.efaps.ui.wicket.models.field.factories.BitEnumUIFactory;
import org.efaps.ui.wicket.models.field.factories.BooleanUIFactory;
import org.efaps.ui.wicket.models.field.factories.DateTimeUIFactory;
import org.efaps.ui.wicket.models.field.factories.DateUIFactory;
import org.efaps.ui.wicket.models.field.factories.DecimalUIFactory;
import org.efaps.ui.wicket.models.field.factories.DecimalWithUoMFactory;
import org.efaps.ui.wicket.models.field.factories.EnumUIFactory;
import org.efaps.ui.wicket.models.field.factories.FormatedStringUIFactory;
import org.efaps.ui.wicket.models.field.factories.HRefFactory;
import org.efaps.ui.wicket.models.field.factories.IComponentFactory;
import org.efaps.ui.wicket.models.field.factories.JaxbUIFactory;
import org.efaps.ui.wicket.models.field.factories.LinkWithRangesUIFactory;
import org.efaps.ui.wicket.models.field.factories.NumberUIFactory;
import org.efaps.ui.wicket.models.field.factories.PasswordUIFactory;
import org.efaps.ui.wicket.models.field.factories.RateUIFactory;
import org.efaps.ui.wicket.models.field.factories.StringUIFactory;
import org.efaps.ui.wicket.models.field.factories.TypeUIFactory;
import org.efaps.ui.wicket.models.field.factories.UITypeFactory;
import org.efaps.ui.wicket.models.field.factories.UserUIFactory;
import org.efaps.ui.wicket.models.objects.AbstractUIModeObject;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public abstract class AbstractUIField
    extends AbstractInstanceObject
    implements IPickable, IHidden, IFilterable, IAutoComplete, IUIElement
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractUIField.class);

    /**
     * The factories used to construct the components.
     */
    private static final Map<String, IComponentFactory> FACTORIES = new LinkedHashMap<>();

    static {
        AbstractUIField.FACTORIES.put(HRefFactory.get().getKey(), HRefFactory.get());
        AbstractUIField.FACTORIES.put(AutoCompleteFactory.get().getKey(), AutoCompleteFactory.get());
        AbstractUIField.FACTORIES.put(NumberUIFactory.get().getKey(), NumberUIFactory.get());
        AbstractUIField.FACTORIES.put(PasswordUIFactory.get().getKey(), PasswordUIFactory.get());
        AbstractUIField.FACTORIES.put(StringUIFactory.get().getKey(), StringUIFactory.get());
        AbstractUIField.FACTORIES.put(LinkWithRangesUIFactory.get().getKey(), LinkWithRangesUIFactory.get());
        AbstractUIField.FACTORIES.put(BooleanUIFactory.get().getKey(), BooleanUIFactory.get());
        AbstractUIField.FACTORIES.put(DateUIFactory.get().getKey(), DateUIFactory.get());
        AbstractUIField.FACTORIES.put(DateTimeUIFactory.get().getKey(), DateTimeUIFactory.get());
        AbstractUIField.FACTORIES.put(DecimalUIFactory.get().getKey(), DecimalUIFactory.get());
        AbstractUIField.FACTORIES.put(UserUIFactory.get().getKey(), UserUIFactory.get());
        AbstractUIField.FACTORIES.put(TypeUIFactory.get().getKey(), TypeUIFactory.get());
        AbstractUIField.FACTORIES.put(RateUIFactory.get().getKey(), RateUIFactory.get());
        AbstractUIField.FACTORIES.put(EnumUIFactory.get().getKey(), EnumUIFactory.get());
        AbstractUIField.FACTORIES.put(BitEnumUIFactory.get().getKey(), BitEnumUIFactory.get());
        AbstractUIField.FACTORIES.put(JaxbUIFactory.get().getKey(), JaxbUIFactory.get());
        AbstractUIField.FACTORIES.put(FormatedStringUIFactory.get().getKey(), FormatedStringUIFactory.get());
        AbstractUIField.FACTORIES.put(DecimalWithUoMFactory.get().getKey(), DecimalWithUoMFactory.get());
        AbstractUIField.FACTORIES.put(UITypeFactory.get().getKey(), UITypeFactory.get());
    }

    /**
     * Configuration of the related field.
     */
    private FieldConfiguration fieldConfiguration;

    /**
     * Parent Object.
     */
    private final AbstractUIModeObject parent;

    /**
     * UserInterface Value.
     */
    private UIValue value;

    /**
     * Picker related to this field.
     */
    private UIPicker picker;

    /**
     * Already added.
     */
    private boolean added;

    /**
     * Factory applied for this field.
     */
    private String factoryKey;

    /**
     * Value as shown for a picklist.
     */
    private String pickListValue;

    /**
     * Settings for the AutoComplete.
     */
    private AutoCompleteSettings autoCompleteSetting;

    /** The sort value. */
    private Comparable<?> sortValue;

    /** The hide. */
    private boolean hide;

    /**
     * @param _instanceKey key to the instance
     * @param _parent       parent object
     * @param _value        value
     * @throws EFapsException on error
     */
    public AbstractUIField(final AbstractUIModeObject _parent,
                           final String _instanceKey,
                           final UIValue _value)
        throws EFapsException
    {
        super(_instanceKey);
        parent = _parent;
        value = _value;
        fieldConfiguration = getNewFieldConfiguration();
    }

    /**
     * Getter method for the instance variable {@link #autoCompleteSetting}.
     *
     * @return value of instance variable {@link #autoCompleteSetting}
     */
    @Override
    public AutoCompleteSettings getAutoCompleteSetting()
    {
        if (autoCompleteSetting == null && isAutoComplete()) {
            autoCompleteSetting = new AutoCompleteSettings(getFieldConfiguration());

            final List<EventDefinition> events = getFieldConfiguration().getField().getEvents(
                            EventType.UI_FIELD_AUTOCOMPLETE);
            for (final EventDefinition event : events) {
                autoCompleteSetting.setMinInputLength(event.getProperty("MinInputLength") == null
                                ? 1 : Integer.valueOf(event.getProperty("MinInputLength")));
                autoCompleteSetting.setMaxChoiceLength(event.getProperty("MaxChoiceLength") == null
                                ? -1 : Integer.valueOf(event.getProperty("MaxChoiceLength")));
                autoCompleteSetting.setMaxValueLength(event.getProperty("MaxValueLength") == null
                                ? -1 : Integer.valueOf(event.getProperty("MaxValueLength")));
                if (event.getProperty("MaxResult") != null) {
                    autoCompleteSetting.setMaxResult(Integer.valueOf(event.getProperty("MaxResult")));
                }
                if (event.getProperty("HasDownArrow") != null) {
                    autoCompleteSetting
                                    .setHasDownArrow("true".equalsIgnoreCase(event.getProperty("HasDownArrow")));
                }
                if (event.getProperty("Required") != null) {
                    autoCompleteSetting
                                    .setRequired(!"false".equalsIgnoreCase(event.getProperty("Required")));
                }

                if (event.getProperty("AutoType") != null) {
                    autoCompleteSetting.setAutoType(EnumUtils.getEnum(AutoCompleteBehavior.Type.class,
                                    event.getProperty("AutoType")));
                }

                // add the ExtraParameter definitions
                final String ep = event.getProperty("ExtraParameter");
                if (ep != null) {
                    autoCompleteSetting.getExtraParameters().add(ep);
                }
                int i = 1;
                String keyTmp = "ExtraParameter" + String.format("%02d", i);
                while (event.getProperty(keyTmp) != null) {
                    autoCompleteSetting.getExtraParameters().add(event.getProperty(keyTmp));
                    i++;
                    keyTmp = "ExtraParameter" + String.format("%02d", i);
                }

                final String value4EditStr = event.getProperty("Value4Edit");
                if (value4EditStr != null) {
                    autoCompleteSetting.setValue4Edit(EditValue.valueOf(value4EditStr));
                }
            }
        }
        return autoCompleteSetting;
    }

    /**
     * Getter method for the instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    @Override
    public AbstractUIModeObject getParent()
    {
        return parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasInstanceManager()
        throws EFapsException
    {
        return getParent() != null ? getParent().hasInstanceManager() : false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instance getInstanceFromManager()
        throws EFapsException
    {
        return getParent().getInstanceFromManager();
    }

    /**
     * Getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */
    public UIValue getValue()
    {
        return value;
    }

    /**
     * Setter method for instance variable {@link #value}.
     *
     * @param _value value for instance variable {@link #value}
     */
    public void setValue(final UIValue _value)
    {
        value = _value;
    }

    /**
     * Getter method for the instance variable {@link #fieldConfiguration}.
     *
     * @return value of instance variable {@link #fieldConfiguration}
     */
    public FieldConfiguration getFieldConfiguration()
    {
        return fieldConfiguration;
    }

    /**
     * @return the label for the UserInterface
     * @throws CacheReloadException on error
     */
    @Override
    public String getLabel()
        throws EFapsException
    {
        final String ret;
        if (getFieldConfiguration() != null) {
            ret = getFieldConfiguration().evalLabel(getValue(), getInstance());
        } else {
            ret = DBProperties.getProperty(FieldConfiguration.class.getName() + ".NoLabel");
        }
        return ret;
    }

    /**
     * Setter method for instance variable {@link #fieldConfiguration}.
     *
     * @param _fieldConfiguration value for instance variable {@link #fieldConfiguration}
     */

    protected void setFieldConfiguration(final FieldConfiguration _fieldConfiguration)
    {
        fieldConfiguration = _fieldConfiguration;
    }

    /**
     * @return a new FieldConfiguration
     * @throws EFapsException on error
     */
    protected FieldConfiguration getNewFieldConfiguration()
        throws EFapsException
    {
        final FieldConfiguration ret;
        if (getValue() == null) {
            ret = null;
        } else {
            ret = new FieldConfiguration(getValue().getField().getId());
        }
        return ret;
    }

    /**
     * @return is this value editable
     */
    public boolean editable()
    {
        return getValue().getField().isEditableDisplay(getParent().getMode());
    }

    /**
     * @return is this value editable
     */
    public boolean hidden()
    {
        return getValue().getField().isHiddenDisplay(getParent().getMode());
    }

    /**
     * @return the List of Factories used for this Field on construction of the component.
     */
    public Map<String, IComponentFactory> getFactories()
    {
        return AbstractUIField.FACTORIES;
    }

    /**
     * @param _wicketId wicket id
     * @return Component
     * @throws EFapsException on error
     */
    @Override
    public Component getComponent(final String _wicketId)
        throws EFapsException
    {
        Component ret = null;
        final IComponentFactory factory = getFactory();
        if (factory == null) {
            ret = new LabelField(_wicketId, Model.of("No Factory was applied successfully"),
                            fieldConfiguration, "NONE");
        } else {
            if (hidden()) {
                ret = factory.getHidden(_wicketId, this);
            } else if (editable()) {
                ret = factory.getEditable(_wicketId, this);
            } else {
                ret = factory.getReadOnly(_wicketId, this);
            }
        }
        if (ret == null) {
            throw new EFapsException(AbstractUIField.class, "factoryReturnsNoComponent",
                            getFieldConfiguration().getField());
        }
        return ret;
    }

    /**
     * Execute events.
     *
     * @param _eventType the event type
     * @param _others the others
     * @param _uiID2Oid the ui i d2 oid
     * @return the list< return>
     * @throws EFapsException on error
     */
    public List<Return> executeEvents(final EventType _eventType,
                                      final Object _others,
                                      final Map<String, String> _uiID2Oid)
        throws EFapsException
    {
        List<Return> ret = new ArrayList<>();
        final Field field = getFieldConfiguration().getField();
        if (field.hasEvents(_eventType)) {
            final Context context = Context.getThreadContext();
            final String[] contextoid = { getInstanceKey() };
            context.getParameters().put("oid", contextoid);
            ret = field.executeEvents(_eventType,
                            ParameterValues.INSTANCE, getInstance(),
                            ParameterValues.OTHERS, _others,
                            ParameterValues.PARAMETERS, context.getParameters(),
                            ParameterValues.CLASS, this,
                            ParameterValues.OIDMAP4UI, _uiID2Oid,
                            ParameterValues.CALL_INSTANCE, getParent().getInstance(),
                            ParameterValues.CALL_CMD, ((AbstractUIObject) getParent()).getCommand());
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #picker}.
     *
     * @return value of instance variable {@link #picker}
     */
    @Override
    public UIPicker getPicker()
    {
        return picker;
    }

    /**
     * Setter method for instance variable {@link #picker}.
     *
     * @param _picker value for instance variable {@link #picker}
     */
    public void setPicker(final UIPicker _picker)
    {
        picker = _picker;
    }

    /**
     * @return true if a picker is assigned else false;
     */
    public boolean hasPicker()
    {
        return getPicker() != null;
    }

    /**
     * Hide label.
     *
     * @return true, if successful
     */
    public boolean hideLabel()
    {
        return hasPicker() && getPicker().isButton() || getFieldConfiguration().isHideLabel();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                        .append("Field", getFieldConfiguration().getField().getName())
                        .append("Value", getValue()).build();
    }

    @Override
    public IHidden setAdded(final boolean _added)
    {
        added = _added;
        return this;
    }

    @Override
    public boolean isAdded()
    {
        return added;
    }

    @Override
    public boolean belongsTo(final Long _fieldId)
    {
        return getFieldConfiguration().getField().getId() == _fieldId;
    }

    @Override
    public String getPickListValue()
        throws EFapsException
    {
        if (pickListValue == null) {
            pickListValue = getFactory().getPickListValue(this);
        }
        return pickListValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Comparable<?> getCompareValue()
    {
        Comparable<?> ret = null;
        try {
            if (sortValue == null) {
                ret = getFactory().getCompareValue(this);
            } else {
                ret = sortValue;
            }
        } catch (final EFapsException e) {
            LOG.error("Catched error", e);
        }
        return ret;
    }

    @Override
    public ISortable setCompareValue(final Object _object)
    {
        if (_object != null && _object instanceof Comparable) {
            sortValue = (Comparable<?>) _object;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public int compareTo(final ISortable _arg0)
    {
        return ObjectUtils.compare((Comparable) getCompareValue(), (Comparable)_arg0.getCompareValue());
    }

    /**
     * Getter method for the instance variable {@link #factory}.
     *
     * @return value of instance variable {@link #factory}
     * @throws EFapsException on error
     */
    public IComponentFactory getFactory()
        throws EFapsException
    {
        if (getFactoryKey() == null) {
            for (final IComponentFactory factory : getFactories().values()) {
                if (factory.applies(this)) {
                    setFactory(factory);
                    break;
                }
            }
            if (getFactoryKey() == null) {
                LOG.warn("Could not find factory for field '{}' in '{}'", getFieldConfiguration().getName(),
                                getFieldConfiguration().getField().getCollection().getName());
            }
        }
        return getFactories().get(getFactoryKey());
    }

    /**
     * Setter method for instance variable {@link #factory}.
     *
     * @param _factory value for instance variable {@link #factory}
     */
    public void setFactory(final IComponentFactory _factory)
    {
        factoryKey = _factory.getKey();
    }

    /**
     * Getter method for the instance variable {@link #factoryKey}.
     *
     * @return value of instance variable {@link #factoryKey}
     */
    public String getFactoryKey()
    {
        return factoryKey;
    }

    /**
     * Setter method for instance variable {@link #pickListValue}.
     *
     * @param _pickListValue value for instance variable {@link #pickListValue}
     */
    public void setPickListValue(final String _pickListValue)
    {
        pickListValue = _pickListValue;
    }

    /**
     * Checks if is auto complete.
     *
     * @return true, if is auto complete
     */
    @Override
    public boolean isAutoComplete()
    {
        return getFieldConfiguration().getField().hasEvents(EventType.UI_FIELD_AUTOCOMPLETE);
    }

    @Override
    public boolean isFieldUpdate()
    {
        return getFieldConfiguration().getField().hasEvents(EventType.UI_FIELD_UPDATE);
    }

    @Override
    public List<Return> getAutoCompletion(final String _input,
                                          final Map<String, String> _uiID2Oid)
        throws EFapsException
    {
        return executeEvents(EventType.UI_FIELD_AUTOCOMPLETE, _input, _uiID2Oid);
    }

    @Override
    public String getAutoCompleteValue()
        throws EFapsException
    {
        final Object val = getValue().getReadOnlyValue(getParent().getMode());
        return val == null ? null : String.valueOf(val);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<IOption> getTokens()
        throws EFapsException
    {
        final List<IOption> ret = new ArrayList<>();
        if (isAutoComplete() && getParent().isEditMode() && editable()
                        && getFieldConfiguration().getField().hasEvents(EventType.UI_FIELD_VALUE)) {
            final Object obj = getValue().getEditValue(getParent().getMode());
            if (obj instanceof List<?>) {
                ret.addAll((Collection<? extends IOption>) obj);
            }
        }
        return ret;
    }

    @Override
    public Instance getInstance()
        throws EFapsException
    {
        final Instance ret;
        // in case of an autocomplete in editmode give the chance to set the instance
        if (isAutoComplete() && getParent().isEditMode() && editable()
                        && getFieldConfiguration().getField().hasEvents(EventType.UI_FIELD_VALUE)) {
            getValue().getEditValue(getParent().getMode());
            ret = getValue().getInstance();
        } else {
            ret = super.getInstance();
        }
        return ret;
    }

    /**
     * Checks if is hide.
     *
     * @return the hide
     */
    public boolean isHide()
    {
        return hide;
    }

    /**
     * Sets the hide.
     *
     * @param _hide the new hide
     */
    public void setHide(final boolean _hide)
    {
        hide = _hide;
    }
}
