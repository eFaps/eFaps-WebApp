/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.components.table.filter;

import java.util.Date;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.models.field.FieldConfiguration;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITable.TableFilter;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.models.objects.UITableHeader.FilterValueType;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.wicketstuff.datetime.StyleDateConverter;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FreeTextPanel
    extends Panel
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Name of the fromfield.
     */
    private String fromFieldName;
    /**
     * Naem of the toField.
     */
    private String toFieldName;

    /**
     * Instantiates a new free text panel.
     *
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @throws EFapsException on error
     */
    public FreeTextPanel(final String _wicketId,
                         final IModel<UITableHeader> _model)
        throws EFapsException
    {
        super(_wicketId, _model);
        final UITableHeader tableHeader = (UITableHeader) super.getDefaultModelObject();
        final UITable uitable = (UITable) tableHeader.getUiHeaderObject();
        final FilterValueType filterType = tableHeader.getFilterType();

        if (filterType.equals(FilterValueType.TEXT)) {
            this.add(new Label("textFrom", DBProperties.getProperty("FilterPage.textFilter")));
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
            model.setObject(uitable.getFilter(tableHeader).getFrom());

            final TextField<String> stringFilter = new TextField<>("from", model);
            this.add(stringFilter);

            final WebMarkupContainer options = new WebMarkupContainer("options");
            this.add(options);
            options.add(new Label("expertModeLabel", DBProperties.getProperty("FilterPage.expertModeLabel")));
            options.add(new Label("ignoreCaseLabel", DBProperties.getProperty("FilterPage.ignoreCaseLabel")));
            final CheckBox checkBox = new CheckBox("expertMode",
                            Model.of(new Boolean(uitable.getFilter(tableHeader).isExpertMode())));
            checkBox.setOutputMarkupId(true);
            options.add(checkBox);
            final CheckBox checkBox2 = new CheckBox("ignoreCase",
                            Model.of(new Boolean(uitable.getFilter(tableHeader).isIgnoreCase())));
            checkBox2.setOutputMarkupId(true);
            options.add(checkBox2);

            this.add(new WebMarkupContainer("js").setVisible(false));
            this.add(new WebMarkupContainer("dateFrom").setVisible(false));
            this.add(new WebMarkupContainer("textTo").setVisible(false));
            this.add(new WebMarkupContainer("to").setVisible(false));
            this.add(new WebMarkupContainer("dateTo").setVisible(false));
            this.fromFieldName = "from";

        } else if (filterType.equals(FilterValueType.DATE)) {
            DateTime fromDate = null;
            DateTime toDate = null;
            final TableFilter filter = uitable.getFilter(tableHeader);
            if (filter != null) {
                fromDate = filter.getDateFrom();
                toDate = filter.getDateTo().minusDays(1);
            }

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
            js.append("<a href=\"#\" onclick=\"document.getElementsByName('").append(dateFrom.getDateFieldName())
                .append("')[0].value='").append(dateStr)
                .append("';document.getElementsByName('").append(dateTo.getDateFieldName())
                .append("')[0].value='")
                .append(dateStr).append("';\">")
                .append(DBProperties.getProperty("FilterPage.Today")).append("</a>");
            if (filter == null || filter.getDateFrom() == null) {
                js.append("<script type=\"text/javascript\">")
                    .append("Wicket.Event.add(window, \"domready\", function(event) {")
                    .append("document.getElementsByName('").append(dateFrom.getDateFieldName())
                    .append("')[0].value='';")
                    .append("document.getElementsByName('").append(dateTo.getDateFieldName())
                    .append("')[0].value='';")
                    .append(" });")
                    .append("</script>");
            }
            this.add(new LabelComponent("js", js.toString()));

            this.fromFieldName = "dateFrom";
            this.toFieldName = "dateTo";
            this.add(new WebMarkupContainer("from").setVisible(false));
            this.add(new WebMarkupContainer("to").setVisible(false));
            this.add(new WebMarkupContainer("options").setVisible(false));
        } else if (filterType.equals(FilterValueType.INTEGER) || filterType.equals(FilterValueType.DECIMAL)) {
            this.add(new Label("textFrom", DBProperties.getProperty("FilterPage.textFrom")));
            this.add(new Label("textTo", DBProperties.getProperty("FilterPage.textTo")));
            this.add(new TextField<>("from"));
            this.add(new TextField<>("to"));
            this.fromFieldName = "from";
            this.toFieldName = "to";
            this.add(new WebMarkupContainer("js").setVisible(false));
            this.add(new WebMarkupContainer("dateFrom").setVisible(false));
            this.add(new WebMarkupContainer("dateTo").setVisible(false));
            this.add(new WebMarkupContainer("expertMode").setVisible(false));
            this.add(new WebMarkupContainer("options").setVisible(false));
        }
    }

    /**
     * Getter method for instance variable {@link #fromFieldName}.
     *
     * @return value of instance variable {@link #fromFieldName}
     */
    public String getFromFieldName()
    {
        return this.fromFieldName;
    }

    /**
     * Getter method for instance variable {@link #toFieldName}.
     *
     * @return value of instance variable {@link #toFieldName}
     */
    public String getToFieldName()
    {
        return this.toFieldName;
    }
}
