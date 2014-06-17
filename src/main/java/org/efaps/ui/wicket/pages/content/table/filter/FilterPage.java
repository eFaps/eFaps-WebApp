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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content.table.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.PageReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.field.Filter;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.date.DateTimePanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.modalwindow.UpdateParentCallback;
import org.efaps.ui.wicket.components.table.filter.ClassificationPanel;
import org.efaps.ui.wicket.components.table.filter.FreeTextPanel;
import org.efaps.ui.wicket.components.table.filter.PickerPanel;
import org.efaps.ui.wicket.components.table.filter.StatusPanel;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.Element;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
import org.efaps.ui.wicket.models.objects.UIStatusSet;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.models.objects.UITableHeader.FilterType;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;
/**
 * @author The eFaps Team
 * @version $Id:FilterPage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class FilterPage
    extends AbstractMergePage
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
     * Refernce top the page.
     */
    private final PageReference pageReference;

    /**
     * @param _pageReference reference to the page opening this filterpage
     * @param _uitableHeader uitablehaeder this FilterPage belongs to
     * @throws EFapsException on error
     */
    public FilterPage(final PageReference _pageReference,
                      final UITableHeader _uitableHeader)
        throws EFapsException
    {
        super(_pageReference.getPage().getDefaultModel());
        this.pageReference = _pageReference;
        final FormContainer form = new FormContainer("eFapsForm");
        this.add(form);
        final Panel panel;

        switch (_uitableHeader.getFilter().getType()) {
            case CLASSIFICATION:
                panel = new ClassificationPanel("filterPanel", Model.of(_uitableHeader));
                break;
            case PICKLIST:
                panel = new PickerPanel("filterPanel", Model.of(_uitableHeader));
                break;
            case STATUS:
                panel = new StatusPanel("filterPanel", Model.of(_uitableHeader));
                break;
            default:
                panel = new FreeTextPanel("filterPanel", Model.of(_uitableHeader));
                break;
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
                    final AbstractContentPage page = (AbstractContentPage) FilterPage.this.pageReference.getPage();
                    final ModalWindowContainer modal = page.getModal();
                    // to ensure that it is never null, set the wrong one and then analyze
                    UITable uiTable = (UITable) _uitableHeader.getUiHeaderObject();
                    if (page.getDefaultModelObject() instanceof UITable) {
                        uiTable = (UITable) page.getDefaultModelObject();
                    } else {
                        // in case that the table is inside a form it must be searched for
                        final UIForm uiForm = (UIForm) page.getDefaultModelObject();
                        for (final Element element : uiForm.getElements()) {
                            if (element.getType().equals(ElementType.TABLE)) {
                                final UITable uiTableTmp = (UITable) element.getElement();
                                // compare them, if tableid is the same this it is
                                if (_uitableHeader.getUiHeaderObject().getTableId() == uiTableTmp.getTableId()) {
                                    uiTable = uiTableTmp;
                                    break;
                                }
                            }
                        }

                    }
                    if (_uitableHeader.getFilter().getType().equals(Filter.Type.PICKLIST)) {
                        final List<StringValue> selection = getRequest().getRequestParameters()
                                        .getParameterValues(PickerPanel.CHECKBOXNAME);

                        if (selection != null) {
                            final List<?> picklist = ((PickerPanel) panel).getPickList();
                            // all value are selected, meaning that nothing must be filtered
                            if (selection.size() == picklist.size()) {
                                uiTable.removeFilter(_uitableHeader);
                            } else {
                                final Set<Object> filterList = new HashSet<Object>();
                                for (final StringValue value : selection) {
                                    final Integer intpos = Integer.valueOf(value.toString());
                                    filterList.add(picklist.get(intpos));
                                }
                                uiTable.addFilterList(_uitableHeader, filterList);
                            }
                            modal.setWindowClosedCallback(new UpdateParentCallback(FilterPage.this.pageReference,
                                            modal, false));
                            modal.setUpdateParent(true);
                        } else {
                            modal.setUpdateParent(false);
                        }
                        modal.close(_target);
                    } else if (_uitableHeader.getFilter().getType().equals(Filter.Type.CLASSIFICATION)) {
                        final ClassificationPanel classPanel = (ClassificationPanel) panel;
                        final UIClassification uiClass = classPanel.getUiClassification();
                        uiTable.addFilterClassifcation(_uitableHeader, uiClass);
                        modal.setWindowClosedCallback(new UpdateParentCallback(FilterPage.this.pageReference,
                                        modal, _uitableHeader.getFilter().getBase().equals(Filter.Base.DATABASE)));
                        modal.setUpdateParent(true);
                        modal.close(_target);
                    } else if (_uitableHeader.getFilter().getType().equals(Filter.Type.STATUS)) {
                        final List<StringValue> selection = getRequest().getRequestParameters()
                                        .getParameterValues(StatusPanel.CHECKBOXNAME);
                        if (selection != null) {
                            final List<UIStatusSet> sets = ((StatusPanel) panel).getStatusSets();
                            // all value are selected and the not required, meaning that nothing must be filtered
                            if (selection.size() == sets.size()
                                             && !_uitableHeader.getFilter().isRequired()) {
                                uiTable.removeFilter(_uitableHeader);
                            } else {
                                final Set<Object> filterList = new HashSet<Object>();
                                for (final StringValue value : selection) {
                                    final Integer intpos = Integer.valueOf(value.toString());
                                    filterList.addAll(sets.get(intpos).getIds());
                                }
                                uiTable.addFilterList(_uitableHeader, filterList);
                            }
                            modal.setWindowClosedCallback(new UpdateParentCallback(FilterPage.this.pageReference,
                                             modal, _uitableHeader.getFilter().getBase().equals(Filter.Base.DATABASE)));
                            modal.setUpdateParent(true);
                        }  else {
                            modal.setUpdateParent(false);
                        }
                        modal.close(_target);
                    } else if (_uitableHeader.getFilterType().equals(FilterType.DATE)) {
                        final FreeTextPanel freeTextPanel = (FreeTextPanel) panel;
                        final Iterator<? extends Component> iter = freeTextPanel.iterator();
                        String from = null;
                        String to = null;
                        while (iter.hasNext()) {
                            final Component comp = iter.next();
                            if (comp instanceof DateTimePanel) {
                                final DateTimePanel datePanel = (DateTimePanel) comp;
                                if (datePanel.getId().equals(freeTextPanel.getFromFieldName())) {
                                    final List<StringValue> tmp = getRequest().getRequestParameters()
                                                    .getParameterValues(
                                                                    datePanel.getDateFieldName());
                                    if (!tmp.isEmpty()) {
                                        final List<StringValue> fromTmp = datePanel.getDateAsString(tmp, null, null,
                                                        null);
                                        if (fromTmp != null) {
                                            from = fromTmp.get(0).toString();
                                        }
                                    }
                                } else {
                                    final List<StringValue> tmp = getRequest().getRequestParameters()
                                                    .getParameterValues(datePanel.getDateFieldName());
                                    if (!tmp.isEmpty()) {
                                        final List<StringValue> toTmp = datePanel
                                                        .getDateAsString(tmp, null, null, null);
                                        if (toTmp != null) {
                                            to = toTmp.get(0).toString();
                                        }
                                    }
                                }
                            }
                        }
                        uiTable.addFilterRange(_uitableHeader, from, to);
                        modal.setWindowClosedCallback(new UpdateParentCallback(FilterPage.this.pageReference,
                                        modal, _uitableHeader.getFilter().getBase().equals(Filter.Base.DATABASE)));
                        modal.setUpdateParent(true);
                        modal.close(_target);
                    } else if (_uitableHeader.getFilterType().equals(FilterType.TEXT)) {
                        final FreeTextPanel freeTextPanel = (FreeTextPanel) panel;

                        final String from = freeTextPanel.visitChildren(TextField.class,
                                        new IVisitor<TextField<?>, String>()
                            {
                                @Override
                                public void component(final TextField<?> _object,
                                                      final IVisit<String> _visit)
                                {
                                    @SuppressWarnings("unchecked")
                                    final TextField<String> stringFilter = (TextField<String>) _object;
                                    _visit.stop(stringFilter.getDefaultModelObjectAsString());
                                }
                            });

                        final Map<String, Boolean> modes = new HashMap<String, Boolean>();
                        modes.put("expertMode", false);
                        modes.put("ignoreCase", false);

                        freeTextPanel.visitChildren(CheckBox.class, new IVisitor<CheckBox, Void>()
                        {
                            @Override
                            public void component(final CheckBox _object,
                                                  final IVisit<Void> _visit)
                            {
                                if ("expertMode".equals(_object.getId())) {
                                    modes.put("expertMode", (boolean) _object.getDefaultModelObject());
                                } else {
                                    modes.put("ignoreCase", (boolean) _object.getDefaultModelObject());
                                }
                            }
                        });

                        final boolean expertMode = modes.get("expertMode");
                        final boolean ignoreCase = modes.get("ignoreCase");

                        uiTable.addFilterTextLike(_uitableHeader, from, expertMode, ignoreCase);
                        modal.setWindowClosedCallback(new UpdateParentCallback(FilterPage.this.pageReference,
                                        modal, _uitableHeader.getFilter().getBase().equals(Filter.Base.DATABASE)));
                        modal.setUpdateParent(true);
                        modal.close(_target);
                    }
                } catch (final EFapsException e) {
                    throw new RestartResponseException(new ErrorPage(e));
                }
            }

            @Override
            protected void onError(final AjaxRequestTarget _target,
                                   final Form<?> _form)
            {
                // Nothing done here
            }
        };

        form.add(new Button("submitButton", ajaxbutton, DBProperties.getProperty("FilterPage.Button.filter"),
                            Button.ICON.ACCEPT.getReference()));

        if (_uitableHeader.getFilter().isRequired()) {
            form.add(new WebMarkupContainer("clearButton").setVisible(false));
        } else {
            final AjaxLink<Object> ajaxclear = new AjaxLink<Object>(Button.LINKID)
            {

                private static final long serialVersionUID = 1L;

                @Override
                public void onClick(final AjaxRequestTarget _target)
                {
                    final UITable uiTable = (UITable) _pageReference.getPage().getDefaultModelObject();
                    uiTable.removeFilter(_uitableHeader);
                    _uitableHeader.setFilterApplied(false);
                    final ModalWindowContainer modal = ((AbstractContentPage) FilterPage.this.pageReference.getPage())
                                    .getModal();
                    modal.setUpdateParent(true);
                    modal.setWindowClosedCallback(new UpdateParentCallback(FilterPage.this.pageReference,
                                    modal, _uitableHeader.getFilter().getBase().equals(Filter.Base.DATABASE)));
                    modal.close(_target);
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
                final ModalWindowContainer modal = ((AbstractContentPage) FilterPage.this.pageReference.getPage())
                                .getModal();
                modal.setUpdateParent(false);
                modal.close(_target);
            }
        };
        form.add(new Button("closeButton", ajaxcancel, DBProperties.getProperty("FilterPage.Button.cancel"),
                            Button.ICON.CANCEL.getReference()));
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(FilterPage.CSS));
    }
}
