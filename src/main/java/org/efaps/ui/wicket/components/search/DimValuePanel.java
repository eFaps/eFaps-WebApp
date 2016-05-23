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
package org.efaps.ui.wicket.components.search;

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.json.index.result.DimValue;

/**
 * The Class DimValuePanel.
 *
 * @author The eFaps Team
 */
public class DimValuePanel
    extends GenericPanel<DimValue>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new dim value panel.
     *
     * @param _id the id
     * @param _model the model
     */
    public DimValuePanel(final String _id,
                         final IModel<DimValue> _model)
    {
        super(_id, _model);
        final WebMarkupContainer cont = new WebMarkupContainer("triStateDiv");
        cont.setOutputMarkupId(true);
        add(cont);

        final AjaxButton btn = new AjaxButton("triStateBtn")
        {
            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
            {
                super.updateAjaxAttributes(_attributes);
                final AjaxCallListener lstnr = new AjaxCallListener();
                lstnr. onBefore(getJavaScript());
                _attributes.getAjaxCallListeners().add(lstnr);
            }
        };
        cont.add(btn);

        cont.add(new Label("label", _model.getObject().getLabel()));
        cont.add(new Label("value", _model.getObject().getValue()));

        cont.add(new HiddenField<Boolean>("triStateValue", Model.of()) {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isInputNullable() {
                return true;
            };
        } .setOutputMarkupId(true));
    }

    /**
     * Gets the java script.
     *
     * @return the java script
     */
    private StringBuilder getJavaScript() {

        final String divId = get("triStateDiv").getMarkupId(true);
        get("triStateDiv:triStateBtn").getMarkupId(true);
        final String valId = get("triStateDiv:triStateValue").getMarkupId(true);

        final StringBuilder ret = new StringBuilder()
                .append("require(['dojo/dom-class', 'dojo/dom', 'dojo/on', 'dojo/query', 'dojo/NodeList-traverse',")
                .append("'dojo/domReady!'], function (domClass, dom, on, query) {\n")
                .append("switch (dom.byId('").append(valId).append("').value)")
                .append("{")
                .append("case 'on':")
                .append("dom.byId('").append(valId).append("').value = 'off';")
                .append("domClass.replace('").append(divId).append("', 'off', 'on');")
                .append("break;")
                .append("case 'off':")
                .append("dom.byId('").append(valId).append("').value = '';")
                .append("domClass.remove('").append(divId).append("', 'off');")
                .append("break;")
                .append("default:")
                .append("dom.byId('").append(valId).append("').value = 'on';")
                .append("domClass.add('").append(divId).append("', 'on');")
                .append("}")
                .append("});");
        return ret;
    }
}
