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
package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.ui.wicket.models.objects.AbstractUIHeaderObject;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.EFapsKey;

/**
 * This class renders the Cells inside a Header, providing all necessary Links.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class HeaderCellPanel
    extends Panel
{
    /**
     * Reference to an icon from the eFaps DataBase.
     */
    public static final EFapsContentReference ICON_FILTER = new EFapsContentReference(HeaderCellPanel.class,
                                                                                      "Filter.gif");

    /**
     * Reference to an icon from the eFaps DataBase.
     */
    public static final EFapsContentReference ICON_FILTERACTIVE = new EFapsContentReference(HeaderCellPanel.class,
                                                                                            "FilterActive.gif");

    /**
     * Reference to an icon from the eFaps DataBase.
     */
    public static final EFapsContentReference ICON_SORTDESC = new EFapsContentReference(HeaderCellPanel.class,
                                                                                        "SortDescending.gif");

    /**
     * Reference to an icon from the eFaps DataBase.
     */
    public static final EFapsContentReference ICON_SORTASC = new EFapsContentReference(HeaderCellPanel.class,
                                                                                       "SortAscending.gif");

    /**
     * Reference to a stylesheet from the eFaps DataBase.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(HeaderCellPanel.class,
                                                                              "HeaderCellPanel.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor used to render only a CheckBoxCell or the placeholder in case of create.
     *
     * @param _wicketId wicket id for this component
     * @param _checkbox render a checkbox
     * @param _styleClass style class
     * @param _columnNumber number of the column
     */
    public HeaderCellPanel(final String _wicketId,
                           final boolean _checkbox,
                           final String _styleClass,
                           final int _columnNumber)
    {
        super(_wicketId);
        this.add(AttributeModifier.append("class", _styleClass
                        + (_columnNumber > 0 ? " eFapsCellFixedWidth" + _columnNumber : "")));
        if (_checkbox) {
            this.add(new Checkbox("checkBox"));
        } else {
            this.add(new WebMarkupContainer("checkBox").setVisible(false));
        }
        this.add(new WebMarkupContainer("sortlink").setVisible(false));
        this.add(new WebComponent("label").setVisible(false));
        this.add(new WebComponent("filterlink").setVisible(false));
    }

    /**
     * Constructor used to render a Cell for the Header with (depending on the model) SortLink, Filterlink etc.
     *
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     * @param _uitable uitable this component sits in
     */
    public HeaderCellPanel(final String _wicketId,
                           final IModel<UITableHeader> _model,
                           final AbstractUIHeaderObject _uitable)
    {
        super(_wicketId, _model);

        final UITableHeader uiTableHeader = (UITableHeader) super.getDefaultModelObject();

        this.add(AttributeModifier.replace("title", uiTableHeader.getLabel()));

        this.add(new WebComponent("checkBox").setVisible(false));

        if (uiTableHeader.isSortable()) {
            final SortLink sortlink = new SortLink("sortlink", _model);

            if (uiTableHeader.getSortDirection() == SortDirection.NONE) {
                sortlink.add(AttributeModifier.append("class", "eFapsHeaderSort"));
            } else if (uiTableHeader.getSortDirection() == SortDirection.ASCENDING) {
                sortlink.add(AttributeModifier.append("class", "eFapsHeaderSortAscending"));
                sortlink.add(AttributeModifier.replace("style", " background-image: url("
                                + HeaderCellPanel.ICON_SORTASC.getImageUrl() + ");"));
            } else if (uiTableHeader.getSortDirection() == SortDirection.DESCENDING) {
                sortlink.add(AttributeModifier.append("class", "eFapsHeaderSortDescending"));
                sortlink.add(AttributeModifier.replace("style", " background-image: url("
                                + HeaderCellPanel.ICON_SORTDESC.getImageUrl() + ");"));
            }

            this.add(sortlink);
            final Label sortlabel = new Label("sortlabel", uiTableHeader.getLabel());
            sortlink.add(sortlabel);
            this.add(new WebComponent("label").setVisible(false));
        } else {
            this.add(new WebMarkupContainer("sortlink").setVisible(false));
            final Label label = new Label("label", uiTableHeader.getLabel());
            this.add(label);
        }

        if (uiTableHeader.isFilter()) {

            final AjaxFilterLink filterlink = new AjaxFilterLink("filterlink", _model);

            if (uiTableHeader.isFilterApplied() && _uitable.isFiltered()) {
                filterlink.add(AttributeModifier.append("class", "eFapsHeaderFilterActive"));
                filterlink.add(AttributeModifier.append("style", " background-image: url("
                                + HeaderCellPanel.ICON_FILTERACTIVE.getImageUrl() + ");"));
            } else {
                filterlink.add(AttributeModifier.append("class", "eFapsHeaderFilter"));
                filterlink.add(AttributeModifier.append("style", " background-image: url("
                                + HeaderCellPanel.ICON_FILTER.getImageUrl() + ");"));
            }
            this.add(filterlink);
        } else {
            this.add(new WebComponent("filterlink").setVisible(false));
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(HeaderCellPanel.CSS));
    }

    /**
     * Set the markupid into the model.
     *
     * @see org.apache.wicket.Component#onAfterRender()
     */
    @Override
    protected void onAfterRender()
    {
        super.onAfterRender();
        if (getDefaultModel() != null) {
            final UITableHeader headermodel = (UITableHeader) getDefaultModelObject();
            headermodel.setMarkupId(this.getMarkupId());
        }
    }

    /**
     * Class to render a checkbox that selects all checkboxes in this row.
     */
    public class Checkbox
        extends WebComponent
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id for this component
         */
        public Checkbox(final String _wicketId)
        {
            super(_wicketId);
        }

        /**
         * Make a checkbox.
         *
         * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
         * @param _tag tag
         */
        @Override
        protected void onComponentTag(final ComponentTag _tag)
        {
            super.onComponentTag(_tag);
            _tag.put("type", "checkbox");
            _tag.put("onClick", getScript());
            _tag.put("name", EFapsKey.SELECTEALL_NAME.getKey());
        }

        /**
         * Get the script that will be added to the checkbox.
         *
         * @return String containing the script
         */
        private String getScript()
        {
            return new StringBuilder().append("var cb=document.getElementsByName('selectedRow');")
                            .append(" if(!isNaN(cb.length)) {")
                            .append(" for(var i=0;i<cb.length;i++){")
                            .append("   cb[i].checked=this.checked;}")
                            .append(" }else{")
                            .append(" cb.checked=this.checked;" + "}").toString();
        }
    }
}
