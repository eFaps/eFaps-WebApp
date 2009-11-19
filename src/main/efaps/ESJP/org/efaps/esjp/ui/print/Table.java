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

package org.efaps.esjp.ui.print;

import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.wicket.Component;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.esjp.common.jasperreport.StandartReport;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.UIAbstractPageObject;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.util.EFapsException;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.ColumnBuilderException;
import ar.com.fdvs.dj.domain.builders.DynamicReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Stretching;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("99ce434b-4177-4e65-99d1-0195434f628d")
@EFapsRevision("$Rev$")
public class Table extends StandartReport implements EventExecution
{

    /**
     * @param _parameter Parameter
     * @return return PDF with Table
     * @throws EFapsException on error
     */
    @Override
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();

        final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
        final Object object = (_parameter.get(ParameterValues.OTHERS));
        if (object instanceof Component) {
            final UIAbstractPageObject page = (UIAbstractPageObject) ((Component) object).getPage()
                            .getDefaultModelObject();

            if (page instanceof UITable) {

                final Style detailStyle = new Style();
                final Style headerStyle = new Style();
                final Style titleStyle = new Style();
                titleStyle.setFont(Font.VERDANA_BIG_BOLD);
                final Style subtitleStyle = new Style();

                final Style columnStyle = new Style();
                columnStyle.setBorder(Border.PEN_1_POINT);

                final Style columnHeaderStyle = new Style();
                columnHeaderStyle.setBackgroundColor(Color.gray);
                columnHeaderStyle.setBorder(Border.PEN_1_POINT);
                columnHeaderStyle.setTransparency(Transparency.OPAQUE);
                columnHeaderStyle.setTextColor(Color.white);
                columnHeaderStyle.setStreching(Stretching.NO_STRETCH);

                final DynamicReportBuilder drb = new DynamicReportBuilder();
                drb.setTitle(page.getTitle())
                    .setDetailHeight(15)
                    .setHeaderHeight(15)
                    .setMargins(20, 20, 20, 20) // (top, bottom, left and right)
                    .setDefaultStyles(titleStyle, subtitleStyle, headerStyle, detailStyle)
                    .setColumnsPerPage(1)
                    .setPageSizeAndOrientation(Page.Page_A4_Landscape());
                try {
                    final int widthWeight = ((UITable) page).getWidthWeight();
                    for (final UITableHeader header : ((UITable) page).getHeaders()) {
                        final BigDecimal width = new BigDecimal(header.getWidth()).setScale(2).divide(
                                        new BigDecimal(widthWeight), BigDecimal.ROUND_HALF_UP).multiply(
                                        new BigDecimal(555));
                        final AbstractColumn column = ColumnBuilder.getInstance()
                            .setColumnProperty(header.getFieldName(), String.class.getName())
                            .setTitle(header.getLabel())
                            .setStyle(columnStyle)
                            .setHeaderStyle(columnHeaderStyle)
                            .setWidth(header.isFixedWidth() ? header.getWidth() : width.intValue()).build();
                        drb.addColumn(column);
                    }

                    final List<Map <String, String>> values = new ArrayList<Map <String, String>>();
                    for (final UIRow row : ((UITable) page).getValues()) {
                        final Map<String, String> map = new HashMap<String, String>();
                        for (final UITableCell cell : row.getValues()) {
                            map.put(cell.getName(), cell.getCellTitle());
                        }
                        values.add(map);
                    }
                    drb.setUseFullPageWidth(true);
                    drb.addAutoText(AutoText.AUTOTEXT_PAGE_X_OF_Y, AutoText.POSITION_FOOTER, AutoText.ALIGMENT_RIGHT,
                                    200, 40);
                    drb.addAutoText(AutoText.AUTOTEXT_CREATED_ON, AutoText.POSITION_HEADER, AutoText.ALIGMENT_RIGHT,
                                    AutoText.PATTERN_DATE_DATE_TIME);

                    drb.setReportLocale(Context.getThreadContext().getLocale());
                    final DynamicReport dr = drb.build(); // Finally build the
                                                          // report!
                    final JRDataSource ds = new TableSource(values);
                    // here contains dummy hardcoded objects...
                    final JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);

                    String mime = (String) properties.get("Mime");
                    if (mime == null) {
                        mime = _parameter.getParameterValue("mime");
                    }
                    ret.put(ReturnValues.VALUES, super.getFile(jp, mime));
                    ret.put(ReturnValues.TRUE, true);
                } catch (final ColumnBuilderException e) {
                    // TODO Auto-generated catch block
                } catch (final JRException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (final IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                // ??
            }
        }
        return ret;
    }

    /**
     * Source for a Table.
     */
    private class TableSource implements JRDataSource
    {
        /**
         * Values for the rows.
         */
        private final Iterator<Map<String, String>> values;

        /**
         * Value for the current row.
         */
        private Map<String, String> current;

        /**
         * @param _values values for the rows
         */
        public TableSource(final List<Map<String, String>> _values)
        {
            this.values = _values.iterator();
        }

        /**
         * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
         * @param _field Field the value is returned for
         * @return value for the field
         * @throws JRException on error
         */
        public Object getFieldValue(final JRField _field)
            throws JRException
        {
            return this.current.get(_field.getName());
        }

        /**
         * @see net.sf.jasperreports.engine.JRDataSource#next()
         * @return true if next
         * @throws JRException on error
         */
        public boolean next()
            throws JRException
        {
            final boolean ret = this.values.hasNext();
            if (ret) {
                this.current = this.values.next();
            }
            return ret;
        }
    }
}
