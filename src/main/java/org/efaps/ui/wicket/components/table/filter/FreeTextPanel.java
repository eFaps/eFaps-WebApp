/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.ui.wicket.components.table.filter;

import java.util.Date;

import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.form.DateFieldWithPicker;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.models.objects.UITable.Filter;
import org.efaps.ui.wicket.models.objects.UITableHeader.FilterType;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FreeTextPanel extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String fromFieldName;
    private String toFieldName;

    /**
     * @param _id
     * @param model
     */
    public FreeTextPanel(final String _id, final IModel<UITable> model, final UITableHeader _uitableHeader)
    {
        super(_id, model);
        final UITable uitable = (UITable) super.getDefaultModelObject();
        final FilterType filterType = _uitableHeader.getFilterType();

        if (filterType.equals(FilterType.TEXT)) {
            this.add(new Label("textFrom", DBProperties.getProperty("FilterPage.textFilter")));
            final WebMarkupContainer stringFilter = new TextField<Object>("from");
            this.add(stringFilter);
            this.add(new WebMarkupContainer("js").setVisible(false));
            this.add(new WebMarkupContainer("dateFrom").setVisible(false));
            this.add(new WebMarkupContainer("textTo").setVisible(false));
            this.add(new WebMarkupContainer("to").setVisible(false));
            this.add(new WebMarkupContainer("dateTo").setVisible(false));
            this.fromFieldName = "from";

        } else if (filterType.equals(FilterType.DATE)) {
            final Date fromDate;
            final Date toDate;
            final Filter filter = uitable.getFilter(_uitableHeader);
            if (filter != null) {
                fromDate = filter.getDateFrom() == null ? new Date(0) : filter.getDateFrom().toDate();
                toDate = filter.getDateTo() == null ? new Date(0) : filter.getDateTo().minusDays(1).toDate();
            } else {
                fromDate = new Date();
                toDate = new Date();
            }

            final StyleDateConverter conv = new StyleDateConverter(false);
            final String dateStr = conv.convertToString(new Date(), getLocale());
            final StringBuilder js = new StringBuilder();
            js.append("<a href=\"#\" onclick=\"document.getElementsByName('dateTo')[0].value='")
                .append(dateStr).append("';document.getElementsByName('dateFrom')[0].value='")
                .append(dateStr).append("';\">")
                .append(DBProperties.getProperty("FilterPage.Today")).append("</a>");
            if (filter.getDateFrom() == null) {
                js.append("<script type=\"text/javascript\">")
                    .append("Wicket.Event.add(window, \"domready\", function(event) {")
                    .append("document.getElementsByName('dateTo')[0].value='';")
                    .append("document.getElementsByName('dateFrom')[0].value='';")
                    .append(" });")
                    .append("</script>");
            }
            this.add(new LabelComponent("js", js.toString()));
            this.add(new Label("textFrom", DBProperties.getProperty("FilterPage.textFrom")));
            this.add(new Label("textTo", DBProperties.getProperty("FilterPage.textTo")));

            final DateFieldWithPicker dateFrom = new DateFieldWithPicker("dateFrom", new Model<Date>(fromDate),
                                new StyleDateConverter(false), "dateFrom");
            this.add(dateFrom);
            final DateFieldWithPicker dateTo = new DateFieldWithPicker("dateTo", new Model<Date>(toDate),
                                new StyleDateConverter(false), "dateTo");
            this.add(dateTo);

            this.fromFieldName = "dateFrom";
            this.toFieldName = "dateTo";
            this.add(new WebMarkupContainer("from").setVisible(false));
            this.add(new WebMarkupContainer("to").setVisible(false));

        }  else if (filterType.equals(FilterType.INTEGER) || filterType.equals(FilterType.DECIMAL)) {
            this.add(new Label("textFrom", DBProperties.getProperty("FilterPage.textFrom")));
            this.add(new Label("textTo", DBProperties.getProperty("FilterPage.textTo")));
            this.add(new TextField<Object>("from"));
            this.add(new TextField<Object>("to"));
            this.fromFieldName = "from";
            this.toFieldName = "to";
            this.add(new WebMarkupContainer("js").setVisible(false));
            this.add(new WebMarkupContainer("dateFrom").setVisible(false));
            this.add(new WebMarkupContainer("dateTo").setVisible(false));
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
