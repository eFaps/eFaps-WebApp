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

import org.apache.commons.lang3.BooleanUtils;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.efaps.json.index.result.DimValue;
import org.efaps.ui.wicket.components.search.IndexSearch.DimTreeNode;
import org.efaps.ui.wicket.util.DojoClasses;
import org.efaps.ui.wicket.util.DojoWrapper;

/**
 * The Class DimValuePanel.
 *
 * @author The eFaps Team
 */
public class DimValuePanel
    extends GenericPanel<DimTreeNode>
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
                         final IModel<DimTreeNode> _model)
    {
        super(_id, _model);
        final WebMarkupContainer cont = new WebMarkupContainer("triStateDiv");
        cont.setOutputMarkupId(true);

        if (_model.getObject().getStatus() != null) {
            cont.add(new AttributeAppender("class", _model.getObject().getStatus() ? "on" : "off", " "));
        }

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
                lstnr.onBefore(getJavaScript());
                _attributes.getAjaxCallListeners().add(lstnr);
            }
        };
        cont.add(btn);

        cont.add(new Label("label", _model.getObject().getLabel()));
        cont.add(new Label("value", String.valueOf(((DimValue) _model.getObject().getValue()).getValue())));

        cont.add(new HiddenField<Boolean>("triStateValue", PropertyModel.of(_model.getObject(), "status")) {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isInputNullable()
            {
                return true;
            };

            @Override
            protected String getModelValue()
            {
                final Boolean val = (Boolean) getDefaultModelObject();
                final String ret;
                if (BooleanUtils.isFalse(val)) {
                    ret = "off";
                } else if (BooleanUtils.isTrue(val)) {
                    ret = "on";
                } else {
                    ret = "";
                }
                return ret;
            };

        } .setOutputMarkupId(true));
    }

    /**
     * Gets the java script.
     *
     * @return the java script
     */
    private CharSequence getJavaScript()
    {

        final String divId = get("triStateDiv").getMarkupId(true);
        get("triStateDiv:triStateBtn").getMarkupId(true);
        final String valId = get("triStateDiv:triStateValue").getMarkupId(true);

        final StringBuilder ret = new StringBuilder()
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
                .append("}");
        return  DojoWrapper.require(ret, DojoClasses.domClass, DojoClasses.dom, DojoClasses.on,
                        DojoClasses.query, DojoClasses.NodeListTraverse, DojoClasses.domReady);
    }
}
