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

package org.efaps.ui.wicket.components.gridx.filter;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.api.ui.IMapFilter;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;

/**
 * The Class DatePanel.
 */
public class DateFilterPanel
    extends GenericPanel<IMapFilter>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new date panel.
     *
     * @param _id the id
     * @param _model the model
     * @throws EFapsException on error
     */
    public DateFilterPanel(final String _id,
                           final IModel<IMapFilter> _model)
        throws EFapsException
    {
        super(_id, _model);
        final DateTime fromDate = StringUtils.isNotEmpty((CharSequence) getModelObject().get("from"))
                        ? new DateTime(getModelObject().get("from")) : null;
        final DateTime toDate = StringUtils.isNotEmpty((CharSequence) getModelObject().get("to"))
                        ? new DateTime(getModelObject().get("to")) : null;

        this.add(new Label("textFrom", DBProperties.getProperty("FilterPage.textFrom")));
        this.add(new Label("textTo", DBProperties.getProperty("FilterPage.textTo")));

        final FieldConfiguration dfConfig = FieldConfiguration.getSimFieldConfig("dateFrom");
        final DateTimePanel dateFrom = new DateTimePanel("dateFrom", Model.of(), dfConfig, fromDate, false);
        this.add(dateFrom);
        final FieldConfiguration dtConfig = FieldConfiguration.getSimFieldConfig("dateTo");
        final DateTimePanel dateTo = new DateTimePanel("dateTo", Model.of(), dtConfig, toDate, false);
        this.add(dateTo);

        final StyleDateConverter conv = new StyleDateConverter(false);
        final String dateStr = conv.convertToString(new Date(), getLocale());
        final StringBuilder js = new StringBuilder();
        js.append("<a href=\"#\" onclick=\"document.getElementsByName('")
            .append(dateFrom.getDateFieldName()).append("')[0].value='").append(dateStr)
            .append("';document.getElementsByName('").append(dateTo.getDateFieldName())
            .append("')[0].value='").append(dateStr).append("';\">")
            .append(DBProperties.getProperty("FilterPage.Today")).append("</a>");
        this.add(new LabelComponent("js", js.toString()));
    }
}
