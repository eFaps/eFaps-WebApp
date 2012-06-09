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

package org.efaps.ui.wicket.components.autocomplete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.field.Field;
import org.efaps.ui.wicket.behaviors.AjaxFieldUpdateBehavior;
import org.efaps.ui.wicket.behaviors.SetSelectedRowBehavior;
import org.efaps.ui.wicket.components.form.command.AjaxCmdBehavior;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AutoCompleteField
    extends AutoCompleteTextField<Map<String, String>>
{

    /**
     * Reference to the stylesheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(AutoCompleteField.class,
                    "AutoCompleteField.css");

    /**
     * Logger.
     */
    private static final Logger LOG =  LoggerFactory.getLogger(AutoCompleteField.class);


    /** Needed for serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Model of this Component.
     */
    private final IModel<?> model;

    /**
     * Behavior that will be executed onchange, if this AutoCompleteField is
     * used in CommandCellPanel.
     */
    private AjaxCmdBehavior cmdBehavior;


    /**
     * Name of the field this AutoCompleteField belongs to.
     */
    private final String fieldName;

    /**
     * NUmber of cols this field has got.
     */
    private int cols;

    /**
     * Cell this complete field belongs to.
     */
    private final UITableCell uiAbstractCell;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _selectRow name for the field
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public AutoCompleteField(final String _wicketId,
                             final IModel<?> _model,
                             final boolean _selectRow)
    {
        super(_wicketId, new Model());
        this.model = _model;
        this.uiAbstractCell = (UITableCell) _model.getObject();
        this.fieldName = this.uiAbstractCell.getName();

        final Field field = Field.get(this.uiAbstractCell.getFieldId());
        if (field != null) {
            this.cols = field.getCols();
        }

        if (_selectRow) {
            this.add(new SetSelectedRowBehavior(this.fieldName));
        }
        final UITableCell uiObject = (UITableCell) this.model.getObject();
        if (uiObject.isFieldUpdate()) {
            this.add(new AjaxFieldUpdateBehavior("onchange", this.model) {

                /** Needed for serialization. */
                private static final long serialVersionUID = 1L;

                @Override
                protected String getComponentMarkupId()
                {
                    return getMarkupId() + "_hidden";
                }
            });
        }
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(AutoCompleteField.CSS));
        _response.render(AbstractEFapsHeaderItem.forJavaScript(AjaxFieldUpdateBehavior.JS));
    }

    /**
     * The Name must be set too trick the parent classes.
     *
     * @param _tag tag to modify
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
        _tag.setName("input");
        super.onComponentTag(_tag);
        _tag.put("name", this.fieldName + "AutoComplete");
        if (this.cols > 0) {
            _tag.put("size", this.cols);
        }
        if (this.uiAbstractCell.getParent().isEditMode() || this.uiAbstractCell.getParent().isCreateMode()) {
            _tag.put("value", this.uiAbstractCell.getCellTitle());
        }
    }

    /**
     * Factory method for autocomplete behavior that will be added to this
     * textfield.
     *
     * @param _renderer auto complete renderer
     * @param _settings auto complete settings
     * @return auto complete behavior
     */
    @Override
    protected AutoCompleteBehavior<Map<String, String>> newAutoCompleteBehavior(
                    final IAutoCompleteRenderer<Map<String, String>> _renderer,
                    final AutoCompleteSettings _settings)
    {
        _settings.setAdjustInputWidth(false);
        _settings.setUseSmartPositioning(true);
        return new AutoCompleteFieldBehavior(this, new AutoCompleteRenderer(this), _settings);
    }

    /**
     * @see org.apache.wicket.MarkupContainer#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
     *      org.apache.wicket.markup.ComponentTag)
     * @param _markupStream markup stream
     * @param _tag tag
     */
    @Override
    public void onComponentTagBody(final MarkupStream _markupStream,
                                   final ComponentTag _tag)
    {
        final StringBuilder cmp = new StringBuilder();
        cmp.append("<input type=\"hidden\" ").append("name=\"").append(this.fieldName).append("\" id=\"").append(
                        _tag.getAttribute("id")).append("_hidden\" ");
        try {
            if ((this.uiAbstractCell.getParent().isEditMode() || this.uiAbstractCell.getParent().isCreateMode())
                            && this.uiAbstractCell.getInstance() != null) {
                cmp.append(" value=\"").append(this.uiAbstractCell.getInstance().getOid()).append("\"");
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
        cmp.append(">");
        replaceComponentTagBody(_markupStream, _tag, cmp);
    }

    /**
     * Method to get the values from the esjp.
     *
     * @see org.apache.wicket.extensions.ajax.markup.html.autocomplete
     *  .AutoCompleteTextField#getChoices(java.lang.String)
     * @param _input input from the webform
     * @return iterator
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Iterator<Map<String, String>> getChoices(final String _input)
    {
        final UITableCell uiObject = (UITableCell) this.model.getObject();
        final List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
        try {
            final AbstractUIPageObject pageObject = (AbstractUIPageObject) (getPage().getDefaultModelObject());
            final Map<String, String> uiID2Oid = pageObject == null ? null : pageObject.getUiID2Oid();
            final List<Return> returns = uiObject.getAutoCompletion(_input, uiID2Oid);
            for (final Return aReturn : returns) {
                final Object ob = aReturn.get(ReturnValues.VALUES);
                if (ob instanceof List) {
                    retList.addAll((Collection<? extends Map<String, String>>) ob);
                }
            }
        } catch (final EFapsException e) {
            AutoCompleteField.LOG.error("Error in getChoice()", e);
        }
        return retList.iterator();
    }

    /**
     * Add a AjaxCmdBehavior to this .
     *
     * @param _cmdBehavior behavior to add
     */
    public void addCmdBehavior(final AjaxCmdBehavior _cmdBehavior)
    {
        this.cmdBehavior = _cmdBehavior;
    }

    /**
     * Getter method for the instance variable {@link #cmdBehavior}.
     *
     * @return value of instance variable {@link #cmdBehavior}
     */
    public AjaxCmdBehavior getCmdBehavior()
    {
        return this.cmdBehavior;
    }
}
