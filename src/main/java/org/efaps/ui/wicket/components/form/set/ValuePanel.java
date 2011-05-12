/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.ui.wicket.components.form.set;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.form.cell.ValueCellPanel;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.util.EFapsException;

/**
 * Class renders one row for a AttributeSet.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ValuePanel extends Panel
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId     wicketID for this component
     * @param _set          set this value belongs to
     * @param _yCoord       y-coordinate
     * @param _value        value object
     * @param _formmodel    formModel
     * @throws EFapsException on error
     */
    public ValuePanel(final String _wicketId,
                      final UIFormCellSet _set,
                      final Integer _yCoord,
                      final Map<Integer, UIFormCell> _value,
                      final UIForm _formmodel)
        throws EFapsException
    {
        super(_wicketId);
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumIntegerDigits(2);

        final WebMarkupContainer table = new WebMarkupContainer("setTable");
        add(table);
        final RepeatingView repeater = new RepeatingView("repeater");
        table.add(repeater);
        table.setOutputMarkupId(true);
        if (_set.isEditMode()) {
            final WebMarkupContainer td = new WebMarkupContainer("removeTD");
            table.add(td);
            final AjaxRemove remove = new AjaxRemove("remove", _set.getInstance(_yCoord).getId(), _set.getName());
            td.add(remove);
            final StaticImageComponent image = new StaticImageComponent("removeIcon");
            image.setReference(YPanel.ICON_DELETE);
            remove.add(image);

            final Component oid = new WebComponent("oid") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onComponentTag(final ComponentTag _tag)
                {
                    final StringBuilder name = new StringBuilder().append("hiddenId_").append(_set.getName())
                        .append("_").append(nf.format(_yCoord));
                    _tag.put("name", name);
                    _tag.put("value", ((Long) _set.getInstance(_yCoord).getId()).toString());
                    super.onComponentTag(_tag);
                }
            };
            td.add(oid);
        } else {
            table.add(new WebMarkupContainer("removeTD").setVisible(false));
        }

        final Pattern tagpattern = Pattern
                .compile("</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>");
        final StringBuilder regex = new StringBuilder().append("(?i)name\\s*=\\s*\"(?-i)").append(_set.getName())
                    .append("\"");


        for (final Entry<Integer, UIFormCell> entry : _value.entrySet()) {
            final StringBuilder name = new StringBuilder().append(" name=\"").append(_set.getName())
                            .append("_").append(nf.format(_yCoord)).append(nf.format(entry.getKey())).append("\" ");
            final String value = entry.getValue().getCellValue();
            final StringBuilder bldr = new StringBuilder();
            if (value != null) {
                final Matcher matcher = tagpattern.matcher(value);
                int start = 0;
                while (matcher.find()) {
                    value.substring(start, matcher.start());
                    bldr.append(value.substring(start, matcher.start()));
                    final String tag = matcher.group();

                    bldr.append(tag.replaceAll(regex.toString(), name.toString()));
                    start = matcher.end();
                }
                bldr.append(value.substring(start, value.length()));
            }
            entry.getValue().setCellValue(bldr.toString());
            final ValueCellPanel cell = new ValueCellPanel(repeater.newChildId(),
                            new UIModel<UIFormCell>(entry.getValue()), _formmodel, false);
            repeater.add(cell);
        }
    }

    /**
     * Render a link to remove the row.
     */
    public class AjaxRemove
        extends AjaxLink<UIFormCellSet>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * the id of the value.
         */
        private final Long valueID;

        /**
         * Name oif the set this Component belongs to.
         */
        private final String name;


        /**
         * @param _wicketId     wicketId for this component
         * @param _valueID      id of the value
         * @param _name         name of the set
         */
        public AjaxRemove(final String _wicketId,
                          final long _valueID,
                          final String _name)
        {
            super(_wicketId);
            this.valueID = _valueID;
            this.name = _name;
        }

        /**
         * (non-Javadoc).
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target AjaxRequestTarget
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            final UIForm formmodel = (UIForm) getPage().getDefaultModelObject();

            final Map<String, String[]> newmap = formmodel.getNewValues();
            final String keyString = this.name + "eFapsRemove";
            if (!newmap.containsKey(keyString)) {
                newmap.put(keyString, new String[] { this.valueID.toString() });
            } else {
                final String[] oldvalues = newmap.get(keyString);
                final String[] newvalues = new String[oldvalues.length + 1];
                for (int i = 0; i < oldvalues.length; i++) {
                    newvalues[i] = oldvalues[i];
                }
                newvalues[oldvalues.length] = this.valueID.toString();
                newmap.put(keyString, newvalues);
            }
            getParent().getParent().setVisible(false);
            _target.addComponent(getParent().getParent());
        }
    }
}
