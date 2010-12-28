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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.efaps.ui.wicket.components.autocomplete.AutoCompleteField;
import org.efaps.ui.wicket.components.autocomplete.AutoCompleteFieldBehavior;
import org.efaps.ui.wicket.components.form.cell.ValueCellPanel;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.objects.UIForm;


/**
 * Class used to render a ajax link to add a new field to the set.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AjaxAddNew
    extends AjaxLink<UIFormCellSet>
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Refreshing view this ajax link belongs to.
     */
    private final RepeatingView repeater;

    /**
     * FormModel to be passed to the Component.
     */
    private final UIForm formModel;

    /**
     * @param _wicketId wicket if for this component
     * @param _model model for this component
     * @param _repeater view
     * @param _formmodel FormModel
     */
    public AjaxAddNew(final String _wicketId,
                      final IModel<UIFormCellSet> _model,
                      final RepeatingView _repeater,
                      final UIForm _formmodel)
    {
        super(_wicketId, _model);
        this.repeater = _repeater;
        this.formModel = _formmodel;
    }

    /**
     * @param _target ajax request
     */
    @Override
    public void onClick(final AjaxRequestTarget _target)
    {

        final UIFormCellSet set = (UIFormCellSet) super.getDefaultModelObject();

        final UIForm formmodel = (UIForm) getPage().getDefaultModelObject();
        final Map<String, String[]> newmap = formmodel.getNewValues();
        final Integer count = set.getNewCount();
        final String keyName = set.getName() + "_eFapsNew";

        if (!newmap.containsKey(keyName)) {
            newmap.put(keyName, new String[] { count.toString() });
        } else {
            final String[] oldvalues = newmap.get(keyName);
            final String[] newvalues = new String[oldvalues.length + 1];
            for (int i = 0; i < oldvalues.length; i++) {
                newvalues[i] = oldvalues[i];
            }
            newvalues[oldvalues.length] = count.toString();
            newmap.put(keyName, newvalues);
        }

        final AjaxRemoveNew remove = new AjaxRemoveNew(this.repeater.newChildId(), set.getName(), count);
        this.repeater.add(remove);
        final StringBuilder html = new StringBuilder();
        html.append("<table class=\"").append(YPanel.STYLE_CLASS).append("\"").append(" id=\"")
            .append(remove.getMarkupId()).append("\" >")
            .append("<tr>")
            .append("<td>")
            .append("<a onclick=\"").append(remove.getJavaScript()).append("\"").append(" href=\"#\">")
            .append("<img src=\"").append(YPanel.ICON_DELETE.getImageUrl()).append("\"/>")
            .append("</a>")
            .append("</td>");

        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumIntegerDigits(2);

        final Pattern tagpattern = Pattern
            .compile("</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>");
        final StringBuilder regex = new StringBuilder().append("(?i)name\\s*=\\s*\"(?-i)").append(set.getName())
            .append("\"");

        for (final Entry<Integer, UIFormCell> entry : set.getX2definition().entrySet()) {
            html.append("<td>");
            final StringBuilder name = new StringBuilder().append(" name=\"").append(set.getName())
                            .append("_").append("eFapsNew_").append(nf.format(count))
                            .append(nf.format(entry.getKey())).append("\" ");
            final ValueCellPanel cell = new ValueCellPanel(this.repeater.newChildId(),
                            new UIModel<UIFormCell>(entry.getValue()), this.formModel, false);
            this.repeater.add(cell);
            cell.setRenderBodyOnly(true);
            cell.renderComponent();
            final Iterator<? extends Component> iter = cell.iterator();
            while (iter.hasNext()) {
                final Component comp = iter.next();
                if (comp instanceof AutoCompleteField) {
                    final List<IBehavior> list = ((AutoCompleteField) comp).getBehaviors();
                    for (final IBehavior behavior : list) {
                        if (behavior instanceof AutoCompleteFieldBehavior) {
                            _target.appendJavascript(((AutoCompleteFieldBehavior) behavior).getInitScript());
                        }
                    }
                }
            }

            final WebResponse response = (WebResponse) RequestCycle.get().getResponse();
            final String value = response.toString();
            response.reset();
            final Matcher matcher = tagpattern.matcher(value);
            int start = 0;
            final StringBuilder comp = new StringBuilder();
            while (matcher.find()) {
                comp.append(value.substring(start, matcher.start()));
                final String tag = matcher.group();
                comp.append(tag.replaceAll(regex.toString(), name.toString()));
                start = matcher.end();
            }
            comp.append(value.substring(start, value.length()));
            html.append(comp.toString().replace("\'", "\\'").replace("\"", "\\\"")).append("</td>");
        }
        html.append("</tr></table>");

        final StringBuilder script = new StringBuilder();
        script.append("var div = document.createElement('div');")
            .append("var container = document.getElementById('").append(getParent().getMarkupId()).append("');")
            .append("div.innerHTML='")
            .append(html.toString().replace("\n", "")).append("'; ")
            .append("container.insertBefore(div, document.getElementById('").append(this.getMarkupId())
            .append("'));");

        _target.prependJavascript(script.toString());
    }
}
