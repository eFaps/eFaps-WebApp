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

package org.efaps.ui.wicket.components.form.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.models.cell.UIFormCellCmd;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class AjaxCmdBehavior
    extends AjaxFormSubmitBehavior
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AjaxCmdBehavior.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Target component.
     */
    private Component targetComponent;

    /**
     * Others.
     */
    private String others;

    /**
     * @param _form     Form this behavior belongs to
     * @param _targetComponent  target component
     */
    public AjaxCmdBehavior(final FormContainer _form,
                           final Component _targetComponent)
    {
        super(_form, "click");
        this.targetComponent = _targetComponent;
    }

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        // do not render the script to the head
    }

    @Override
    protected void onError(final AjaxRequestTarget _target)
    {
        // nothing to do
    }

    @Override
    public void onSubmit(final AjaxRequestTarget _target)
    {
        final UIFormCellCmd uiObject = (UIFormCellCmd) getComponent().getDefaultModelObject();

        final StringBuilder snip = new StringBuilder();
        try {
            final AbstractUIPageObject pageObject = (AbstractUIPageObject) getComponent().getPage()
                            .getDefaultModelObject();
            final Map<String, String> uiID2Oid = pageObject == null ? null : pageObject.getUiID2Oid();
            final List<Return> returns = uiObject.executeEvents(this.others, uiID2Oid);
            for (final Return oneReturn : returns) {
                if (oneReturn.contains(ReturnValues.SNIPLETT)) {
                    snip.append(oneReturn.get(ReturnValues.SNIPLETT));
                }
            }
        } catch (final EFapsException e) {
            AjaxCmdBehavior.LOG.error("onSubmit", e);
        }

        final List<Map<String, String>> values = new ArrayList<Map<String, String>>();
        final AbstractUIPageObject pageObject = (AbstractUIPageObject) getComponent().getPage()
                        .getDefaultModelObject();
        pageObject.getUiID2Oid();
        final List<Return> returns = null; ///uiObject.getFieldUpdate(getComponent().getMarkupId(), uiID2Oid);
        for (final Return aReturn : returns) {
            final Object ob = aReturn.get(ReturnValues.VALUES);
            if (ob instanceof List) {
                @SuppressWarnings("unchecked")
                final List<Map<String, String>> list = (List<Map<String, String>>) ob;
                values.addAll(list);
            }
        }
        final StringBuilder js = new StringBuilder();
        int i = 0;
        for (final Map<String, String> map : values) {
            if (map.size() > 0) {
                for (final String keyString : map.keySet()) {
                    // if the map contains a key that is not defined in this class
                    // it is assumed to be the name of a field
                    if (!EFapsKey.FIELDUPDATE_JAVASCRIPT.getKey().equals(keyString)
                                    && !EFapsKey.FIELDUPDATE_USEID.getKey().equals(keyString)
                                    && !EFapsKey.FIELDUPDATE_USEIDX.getKey().equals(keyString)) {
                        js.append("eFapsSetFieldValue(").append(i).append(",'")
                            .append(keyString).append("',")
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

        if (uiObject.isTargetField()) {
            final FormPanel formPanel = getComponent().findParent(FormPanel.class);
            this.targetComponent = getModelFromChild(formPanel, uiObject.getTargetField());
        }
        if (!uiObject.isAppend() || !this.targetComponent.isVisible()) {
            final MarkupContainer parent = this.targetComponent.getParent();
            final LabelComponent newComp = new LabelComponent(this.targetComponent.getId(), snip.toString());
            parent.addOrReplace(newComp);
            this.targetComponent = newComp;
            _target.add(parent);
        } else {
            final StringBuilder jScript = new StringBuilder();
            jScript.append("var ele = document.getElementById('")
                .append(this.targetComponent.getMarkupId()).append("');")
                .append("var nS = document.createElement('span');")
                .append("ele.appendChild(nS);")
                .append("nS.innerHTML='").append(snip).append("'");
            _target.prependJavaScript(jScript.toString());
        }
    }

    @Override
    protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
    {
        super.updateAjaxAttributes(_attributes);
        _attributes.setEventNames(new String[]{});
    }

    /**
     * @param _container    container the model is searched for
     * @param _name         name
     * @return component
     */
    private Component getModelFromChild(final WebMarkupContainer _container,
                                        final String _name)
    {
        Component ret = null;
        final Iterator<? extends Component> iter = _container.iterator();
        while (iter.hasNext() && ret == null) {
            final Component comp = iter.next();
            /*
            if (comp.getDefaultModelObject() instanceof UIFormCell) {
                final UIFormCell cell = (UIFormCell) comp.getDefaultModelObject();
                if (_name.equals(cell.getName())) {
                    if (comp instanceof ValueCellPanel) {
                        final Iterator<? extends Component> celliter = ((WebMarkupContainer) comp).iterator();
                        while (celliter.hasNext()) {
                            final Component label = celliter.next();
                            if (label instanceof LabelComponent) {
                                ret = label;
                            }
                        }
                    } else {
                        ret = comp;
                    }
                }
            }
            */
            if (ret == null && comp instanceof WebMarkupContainer) {
                ret = getModelFromChild((WebMarkupContainer) comp, _name);
            }
        }
        return ret;
    }

    /**
     * Don't do anything on the tag. Must be overwritten so that the event is not added to the tag.
     *
     * @param _tag tag to modify
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag)
    {
    }

    /**
     * @param _target   ajax target
     * @param _value    value for others
     */
    public void onSubmit4AutoComplete(final AjaxRequestTarget _target,
                                      final String _value)
    {
        this.others = _value;
        onSubmit(_target);
    }
}
