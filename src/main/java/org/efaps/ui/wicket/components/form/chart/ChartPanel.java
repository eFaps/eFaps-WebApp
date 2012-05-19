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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.ui.wicket.components.form.chart;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.models.cell.UIFormCellChart;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.EFapsKey;
import org.efaps.util.EFapsException;



/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ChartPanel
    extends Panel
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id
     * @param _model    model for this component
     */
    public ChartPanel(final String _wicketId,
                      final IModel<UIFormCellChart> _model)
    {
        super(_wicketId, _model);
        this.add(new AttributeAppender("class", true, new Model<String>("eFapsChart"), " "));
        try {
            _model.getObject().initialize();
        } catch (final EFapsException e) {
            setResponsePage(new ErrorPage(e));
        }
        final ChartResource chart = new ChartResource(_model.getObject());
        this.add(new Image("chart", chart) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                if (_model.getObject().hasMap()) {
                    _tag.put("usemap", "#" + _model.getObject().getName() + EFapsKey.CHARTMAPPOSTFIX.getKey());
                }
            }
        });
        if (_model.getObject().hasSnipllet()) {
            this.add(new WebComponent("snipplet") {

                private static final long serialVersionUID = 1L;

                @Override
                public void onComponentTagBody(final MarkupStream _markupStream,
                                                  final ComponentTag _openTag)
                {
                    super.onComponentTagBody(_markupStream, _openTag);
                    replaceComponentTagBody(_markupStream, _openTag, _model.getObject().getSnipplet());
                }
            });
        } else {
            this.add(new WebComponent("snipplet").setVisible(false));
        }
    }
}
