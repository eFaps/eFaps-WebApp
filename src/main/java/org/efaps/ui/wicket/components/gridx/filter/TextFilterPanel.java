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
package org.efaps.ui.wicket.components.gridx.filter;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.api.ui.IMapFilter;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class TextFilterPanel
    extends GenericPanel<IMapFilter>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new text filter panel.
     *
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @throws EFapsException on error
     */
    public TextFilterPanel(final String _wicketId,
                           final IModel<IMapFilter> _model)
                    throws EFapsException
    {
        super(_wicketId, _model);

        this.add(new Label("title", DBProperties.getProperty("FilterPage.textFilter")));
        final IModel<String> model = new IModel<String>()
        {

            private static final long serialVersionUID = 1L;

            private String value;

            @Override
            public String getObject()
            {
                return this.value;
            }

            @Override
            public void setObject(final String _object)
            {
                this.value = _object;
            }

            @Override
            public void detach()
            {
                // no detach needed
            }
        };
        final String filter = (String) getModelObject().get("filter");
        if (filter != null) {
            model.setObject(filter);
        }
        final TextField<String> stringFilter = new TextField<>("text", model);
        this.add(stringFilter);

        final Boolean expertMode = (Boolean) getModelObject().get("expertMode");
        final Boolean ignoreCase = (Boolean) getModelObject().get("ignoreCase");

        final WebMarkupContainer options = new WebMarkupContainer("options");
        this.add(options);
        options.add(new Label("expertModeLabel", DBProperties.getProperty("FilterPage.expertModeLabel")));
        options.add(new Label("ignoreCaseLabel", DBProperties.getProperty("FilterPage.ignoreCaseLabel")));
        final CheckBox checkBox = new CheckBox("expertMode", Model.of(expertMode));
        checkBox.setOutputMarkupId(true);
        options.add(checkBox);
        final CheckBox checkBox2 = new CheckBox("ignoreCase", Model.of(ignoreCase));
        checkBox2.setOutputMarkupId(true);
        options.add(checkBox2);
    }
}
