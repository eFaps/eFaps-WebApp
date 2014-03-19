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

package org.efaps.ui.wicket.behaviors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxFieldUpdateBehavior
    extends AjaxFormSubmitBehavior
{
    /**
     * Reference to the javascript.
     */
    public static final EFapsContentReference JS = new EFapsContentReference(AjaxFieldUpdateBehavior.class,
                                                                             "FieldUpdate.js");

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AjaxFieldUpdateBehavior.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Model that will be used on submit (if not null).
     */
    private final IModel<?> model;

    /**
     * @param _event event that this behavior should be executed on;
     */
    public AjaxFieldUpdateBehavior(final String _event)
    {
        this(_event, null);
    }

    /**
     * @param _event event that this behavior should be executed on;
     * @param _model model that willbe used on submit
     */
    public AjaxFieldUpdateBehavior(final String _event,
                                   final IModel<?> _model)
    {
        super(_event);
        this.model = _model;
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        super.renderHead(_component, _response);
        _response.render(AbstractEFapsHeaderItem.forJavaScript(AjaxFieldUpdateBehavior.JS));
    }

    /**
     * Default means nothing is done on error.
     *
     * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onError(org.apache.wicket.ajax.AjaxRequestTarget)
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onError(final AjaxRequestTarget _target)
    {
    }

    /**
     * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onSubmit(final AjaxRequestTarget _target)
    {
        final UITableCell uiObject;
        if (this.model == null) {
            uiObject = (UITableCell) getComponent().getDefaultModelObject();
        } else {
            uiObject = (UITableCell) this.model.getObject();
        }
        final List<Map<String, String>> values = new ArrayList<Map<String, String>>();
        try {
            final AbstractUIPageObject pageObject = (AbstractUIPageObject) (getComponent().getPage()
                            .getDefaultModelObject());
            final Map<String, String> uiID2Oid = pageObject == null ? null : pageObject.getUiID2Oid();
            final List<Return> returns = uiObject.getFieldUpdate(getComponent().getMarkupId(), uiID2Oid);
            for (final Return aReturn : returns) {
                final Object ob = aReturn.get(ReturnValues.VALUES);
                if (ob instanceof List) {
                    @SuppressWarnings("unchecked")
                    final List<Map<String, String>> list = (List<Map<String, String>>) ob;
                    values.addAll(list);
                }
            }
        } catch (final EFapsException e) {
            AjaxFieldUpdateBehavior.LOG.error("onSubmit", e);
        }
        final StringBuilder js = new StringBuilder();
        int i = 0;
        for (final Map<String, String> map : values) {
            if (map.size() > 0) {
                final boolean useId = map.containsKey(EFapsKey.FIELDUPDATE_USEID.getKey());
                final boolean useIdx = map.containsKey(EFapsKey.FIELDUPDATE_USEIDX.getKey());
                for (final String keyString : map.keySet()) {
                    // if the map contains a key that is not defined in this class
                    // it is assumed to be the name of a field
                    if (!(EFapsKey.FIELDUPDATE_JAVASCRIPT.getKey().equals(keyString))
                                    && !(EFapsKey.FIELDUPDATE_USEID.getKey().equals(keyString))
                                    && !(EFapsKey.FIELDUPDATE_USEIDX.getKey().equals(keyString))) {
                        js.append("eFapsSetFieldValue(");
                        if (useId || (values.size() == 1 && !useIdx)) {
                            js.append("'").append(map.get(EFapsKey.FIELDUPDATE_USEID.getKey()) == null
                                            ? getComponentMarkupId()
                                            : map.get(EFapsKey.FIELDUPDATE_USEID.getKey())).append("'");
                        } else {
                            js.append(map.get(EFapsKey.FIELDUPDATE_USEIDX.getKey()) == null ? i
                                            : map.get(EFapsKey.FIELDUPDATE_USEIDX.getKey()));
                        }
                        js.append(",'").append(keyString).append("',")
                            .append(map.get(keyString).contains("Array(") ? "" : "'")
                            .append(map.get(keyString))
                            .append(map.get(keyString).contains("Array(") ? "" : "'").append(");");
                    }
                }
            }
            if (map.containsKey(EFapsKey.FIELDUPDATE_JAVASCRIPT.getKey())) {
                js.append(map.get(EFapsKey.FIELDUPDATE_JAVASCRIPT.getKey()));
            }
            i++;
        }
        _target.appendJavaScript(js.toString());
    }

    /**
     * Method to get the ComponentMarkupId.
     *
     * @return markup id of the component.
     */
    protected String getComponentMarkupId()
    {
        return getComponent().getMarkupId();
    }

    /**
     * Overwritten to deactivate the visit of all other components and the
     * setting of model objects. This would lead to
     * an error, because this component does not have a model.
     *
     * @see org.apache.wicket.ajax.form.AjaxFormSubmitBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onEvent(final AjaxRequestTarget _target)
    {
        onSubmit(_target);
    }
}
