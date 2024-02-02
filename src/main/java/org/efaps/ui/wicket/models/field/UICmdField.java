/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.models.field;

import java.util.List;
import java.util.Map;

import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.field.FieldCommand;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;

/**
 * The Class UICmdField.
 *
 * @author The eFaps Team
 */
public class UICmdField
    extends AbstractUIField
{

    /**
     * Enum is used to set for this UIFormCellCmd which status of execution it
     * is in.
     *
     * @author The eFaps Team
     */
    public enum ExecutionStatus
    {
        /** Method evaluateRenderedContent is executed. */
        RENDER,
        /** Method execute is executed. */
        EXECUTE;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Must a button be rendered.
     */
    private final boolean renderButton;

    /**
     * Stores the actual execution status.
     */
    private ExecutionStatus executionStatus;

    /**
     * Must the field be appended.
     */
    private final boolean append;

    /**
     * Target field for the value.
     */
    private final String targetField;

    /**
     * The icon for the button.
     */
    private final String buttonIcon;

    /**
     * UI form cell cmd.
     *
     * @param _parent Parent object
     * @param _field field this cellbelongs to
     * @param _instance instance this field belongs to
     * @throws EFapsException on error
     */
    public UICmdField(final AbstractUIObject _parent,
                      final FieldCommand _field,
                      final Instance _instance)
        throws EFapsException
    {
        super(_parent, _instance == null ? null : _instance.getOid(), null);
        this.renderButton = _field.isRenderButton();
        this.append = _field.isAppend();
        this.targetField = _field.getTargetField();
        this.buttonIcon = _field.getButtonIcon();
        setFieldConfiguration(new FieldConfiguration(_field.getId()));
    }

    /**
     * Execute the underlying events.
     *
     * @param _others others
     * @param _uiID2Oid the ui i d2 oid
     * @return list of returns
     * @throws EFapsException on error
     */
    public List<Return> executeEvents(final Object _others,
                                      final Map<String, String> _uiID2Oid)
        throws EFapsException
    {
        if (this.executionStatus == null) {
            this.executionStatus = ExecutionStatus.EXECUTE;
        }
        final List<Return> ret = executeEvents(EventType.UI_FIELD_CMD, _others, _uiID2Oid);

        if (this.executionStatus == ExecutionStatus.EXECUTE) {
            this.executionStatus = null;
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #renderButton}.
     *
     * @return value of instance variable {@link #renderButton}
     */
    public boolean isRenderButton()
    {
        return this.renderButton;
    }

    /**
     * Getter method for instance variable {@link #append}.
     *
     * @return value of instance variable {@link #append}
     */
    public boolean isAppend()
    {
        return this.append;
    }

    /**
     * Get the script to render the content for the UserInterface in case that
     * not a standard button should be rendered.
     *
     * @param _script additional script from the UserInterface
     * @param _uiID2Oid the ui i d2 oid
     * @return html snipplet
     * @throws EFapsException on error
     */
    public String getRenderedContent(final String _script,
                                     final Map<String, String> _uiID2Oid)
        throws EFapsException
    {
        this.executionStatus = UICmdField.ExecutionStatus.RENDER;
        final StringBuilder snip = new StringBuilder();
        final List<Return> returns = executeEvents(_script, _uiID2Oid);
        for (final Return oneReturn : returns) {
            if (oneReturn.contains(ReturnValues.SNIPLETT)) {
                snip.append(oneReturn.get(ReturnValues.SNIPLETT));
            }
        }
        this.executionStatus = null;
        return snip.toString();
    }

    /**
     * Getter method for instance variable {@link #executionStatus}.
     *
     * @return value of instance variable {@link #executionStatus}
     */
    public ExecutionStatus getExecutionStatus()
    {
        return this.executionStatus;
    }

    /**
     * Get the field this UIFormCellCmd belongs to.
     *
     * @return fieldcommand
     */
    public FieldCommand getFieldCommand()
    {
        return null;
    }

    /**
     * Getter method for the instance variable {@link #buttonIcon}.
     *
     * @return value of instance variable {@link #buttonIcon}
     */
    public String getButtonIcon()
    {
        return this.buttonIcon;
    }

    /**
     * Checks if is target field for the value.
     *
     * @return true if not null
     */
    public boolean isTargetField()
    {
        return this.targetField != null;
    }

    /**
     * Checks if is hide label.
     *
     * @return true, if is hide label
     */
    public boolean isHideLabel()
    {
        return this.renderButton;
    }

    /**
     * Getter method for instance variable {@link #targetField}.
     *
     * @return value of instance variable {@link #targetField}
     */
    public String getTargetField()
    {
        return this.targetField;
    }
}
