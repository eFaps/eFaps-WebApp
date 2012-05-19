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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content.table.filter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.StringValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.table.filter.FreeTextPanel;
import org.efaps.ui.wicket.components.table.filter.PickerPanel;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.models.objects.UITableHeader.FilterType;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContrBehavior;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id:FilterPage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class FilterPage
    extends WebPage
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to a stylesheet in the eFaps DataBase.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(FilterPage.class, "FilterPage.css");

    /**
     * @param _model tablemodel
     * @param _modalwindow modalwindow this page is in
     * @param _uitableHeader uitablehaeder this FilterPage belongs to
     * @throws EFapsException on error
     */
    public FilterPage(final IModel<UITable> _model,
                      final ModalWindowContainer _modalwindow,
                      final UITableHeader _uitableHeader)
        throws EFapsException
    {
        super(_model);
        final UITable uiTable = (UITable) super.getDefaultModelObject();
        add(StaticHeaderContrBehavior.forCss(FilterPage.CSS));
        _modalwindow.setTitle(DBProperties.getProperty("FilterPage.Title") + _uitableHeader.getLabel());
        final FormContainer form = new FormContainer("eFapsForm");
        this.add(form);
        final Panel panel;
        if (_uitableHeader.isFilterPickList()) {
            panel = new PickerPanel("filterPanel", _model, _uitableHeader);
        } else {
            panel = new FreeTextPanel("filterPanel", _model, _uitableHeader);
        }
        form.add(panel);
        final AjaxButton ajaxbutton = new AjaxButton(Button.LINKID, form)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(final AjaxRequestTarget _target,
                                    final Form<?> _form)
            {
                try {
                    if (_uitableHeader.isFilterPickList()) {
                        final List<StringValue> selection = getRequest().getRequestParameters().getParameterValues(PickerPanel.CHECKBOXNAME);

                        if (selection != null) {
                            final List<?> picklist = ((PickerPanel) panel).getPickList();
                            // all value are selected, meaning that nothing must be
                            // filtered
                            if (selection.size() == picklist.size()) {
                                uiTable.removeFilter(_uitableHeader);
                            } else {
                                final Set<Object> filterList = new HashSet<Object>();
                                for (final StringValue value :  selection) {
                                    final Integer intpos = Integer.valueOf(value.toString());
                                    filterList.add(picklist.get(intpos));
                                }
                                uiTable.addFilterList(_uitableHeader, filterList);
                            }
                            _modalwindow.setUpdateParent(true);
                        } else {
                            _modalwindow.setUpdateParent(false);
                        }
                        _modalwindow.close(_target);
                    } else if (_uitableHeader.getFilterType().equals(FilterType.DATE)) {
                        final FreeTextPanel freeTextPanel = (FreeTextPanel) panel;
                        final Iterator<? extends Component> iter = freeTextPanel.iterator();
                        String from = null;
                        String to = null;
                        while (iter.hasNext()) {
                            final Component comp = iter.next();
                            if (comp instanceof DateTimePanel) {
                                final DateTimePanel datePanel = (DateTimePanel) comp;
//                                if (datePanel.getId().equals(freeTextPanel.getFromFieldName())) {
//                                    final List<StringValue> selection = getRequest().getRequestParameters().getParameterValues(PickerPanel.CHECKBOXNAME);
//
//
//                                    final String[] tmp = getRequestCycle().getRequest().getParameters(
//                                                    datePanel.getDateFieldName());
//                                    if (tmp.length > 0) {
//                                        final String[] fromTmp = datePanel.getDateAsString(tmp, null, null, null);
//                                        if (fromTmp != null) {
//                                            from = fromTmp[0];
//                                        }
//                                    }
//                                } else {
//                                    final String[] tmp = getRequestCycle().getRequest().getParameters(
//                                                    datePanel.getDateFieldName());
//                                    if (tmp.length > 0) {
//                                        final String[] toTmp = datePanel.getDateAsString(tmp, null, null, null);
//                                        if (toTmp != null) {
//                                            to = toTmp[0];
//                                        }
//                                    }
//                                }
                            }
                        }
                        uiTable.addFilterRange(_uitableHeader, from, to);
                        if (!_uitableHeader.isFilterMemoryBased()) {
                            uiTable.resetModel();
                        }
                        _modalwindow.setUpdateParent(true);
                        _modalwindow.close(_target);
                    }
                } catch (final EFapsException e) {
                    throw new RestartResponseException(new ErrorPage(e));
                }
            }

            @Override
            protected void onError(final AjaxRequestTarget _target,
                                   final Form<?> _form)
            {
                // TODO Auto-generated method stub

            }
        };

        form.add(new Button("submitButton", ajaxbutton, DBProperties.getProperty("FilterPage.Button.filter"),
                            Button.ICON.ACCEPT.getReference()));

        if (_uitableHeader.isFilterRequired()) {
            form.add(new WebMarkupContainer("clearButton").setVisible(false));
        } else {
            final AjaxLink<Object> ajaxclear = new AjaxLink<Object>(Button.LINKID)
            {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(final AjaxRequestTarget _target)
                {
                    uiTable.removeFilter(_uitableHeader);
                    _modalwindow.setUpdateParent(true);
                    _modalwindow.close(_target);
                }
            };

            form.add(new Button("clearButton", ajaxclear, DBProperties.getProperty("FilterPage.Button.clear"),
                                Button.ICON.DELETE.getReference()));
        }
        final AjaxLink<Object> ajaxcancel = new AjaxLink<Object>(Button.LINKID)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget _target)
            {
                _modalwindow.setUpdateParent(false);
                _modalwindow.close(_target);
            }
        };
        form.add(new Button("closeButton", ajaxcancel, DBProperties.getProperty("FilterPage.Button.cancel"),
                            Button.ICON.CANCEL.getReference()));
    }
}
