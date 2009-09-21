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

package org.efaps.ui.wicket.components.editor;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.efaps.ui.wicket.behaviors.dojo.DojoReference;
import org.efaps.ui.wicket.behaviors.dojo.EditorBehavior;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EditorPanel extends Panel
{
    /** Needed for serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId     wicketID for this component
     * @param _model        model for this componet
     */
    public EditorPanel(final String _wicketId, final IModel<UIFormCell> _model)
    {
        super(_wicketId, _model);

        final WebComponent text = new WebComponent("text", _model) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            /**
             * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
             * @param _tag
             */
            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("name", ((UIFormCell) super.getDefaultModelObject()).getName());
            }

            @Override
            protected void onComponentTagBody(final MarkupStream _markupStream, final ComponentTag _openTag)
            {
                replaceComponentTagBody(_markupStream, _openTag,
                                        ((UIFormCell) super.getDefaultModelObject()).getCellValue());
            }
        };
        this.add(text);
        text.setOutputMarkupId(true);

        final WebComponent editor = new WebComponent("editor", _model) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag _tag)
            {
                super.onComponentTag(_tag);
                _tag.put("onChange", "document.getElementById('" + text.getMarkupId(true) + "').value=arguments[0];");
            }

            /**
             * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream, org.apache.wicket.markup.ComponentTag)
             * @param _markupStream
             * @param _openTag
             */
            @Override
            protected void onComponentTagBody(final MarkupStream _markupStream, final ComponentTag _openTag)
            {
                replaceComponentTagBody(_markupStream, _openTag,
                                        ((UIFormCell) super.getDefaultModelObject()).getCellValue());
            }
        };
        editor.add(new EditorBehavior(null));
        this.add(editor);
    }


    /**
     * @see org.apache.wicket.Component#onBeforeRender()
     */
    @Override
    protected void onBeforeRender()
    {
        ((AbstractContentPage) getPage()).getBody().add(
                        new AttributeModifier("class", true, new Model<String>("tundra")));
        super.onBeforeRender();
    }

    /**
     * Prepares a page so that it is able top render this editor correctly,
     * even when called via ajax.
     *
     * @param _page Page the dojo is added to
     */
    public static void prepare(final Page _page)
    {
        _page.add(DojoReference.getHeaderContributerforDojo());
        _page.add(DojoReference.getHeaderContributerforEfapsDojo());
        ((AbstractContentPage) _page).getBody().add(new AttributeModifier("class", true, new Model<String>("tundra")));
    }
}
