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

package org.efaps.ui.wicket.components.classification;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.objects.UIClassification;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationPath
    extends WebComponent
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     */
    public ClassificationPath(final String _wicketId,
                              final IModel<UIClassification> _model)
    {
        super(_wicketId, _model);
    }

    /**
     * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
     *      org.apache.wicket.markup.ComponentTag)
     * @param _markupStream MarkupStream
     * @param _openTag      open tag
     */
    @Override
    protected void onComponentTagBody(final MarkupStream _markupStream,
                                      final ComponentTag _openTag)
    {
        super.onComponentTagBody(_markupStream, _openTag);
        final StringBuilder html = new StringBuilder();
        final UIClassification uiclass = (UIClassification) getDefaultModelObject();
        if (!uiclass.isInitialized()) {
            uiclass.execute();
        }
        final List<UIClassification> leafs = new ArrayList<UIClassification>();

        if (uiclass.isSelected()) {
            findSelectedLeafs(leafs, uiclass);
        }
        for (final UIClassification leaf : leafs) {
            html.append("<div class=\"classPath\">");
            buildHtml(html, leaf);
            html.append("</div>");
        }

        replaceComponentTagBody(_markupStream, _openTag, html);
    }

    /**
     * @param _leafs        classification leaf
     * @param _uiclass      current classification
     */
    private void findSelectedLeafs(final List<UIClassification> _leafs,
                                   final UIClassification _uiclass)
    {
        boolean add = true;
        for (final UIClassification child : _uiclass.getChildren()) {
            if (child.isSelected()) {
                findSelectedLeafs(_leafs, child);
                add = false;
            }
        }
        if (add) {
            _leafs.add(_uiclass);
        }
    }

    /**
     * @param _bldr     StringBUilder for the snipplet
     * @param _uiclass  classifcation
     */
    private void buildHtml(final StringBuilder _bldr,
                           final UIClassification _uiclass)
    {
        UIClassification tmp = _uiclass;
        String path = "<span class=\"classPathEntry\">" + tmp.getLabel() + "</span>";
        while (!tmp.isRoot()) {
            tmp = tmp.getParent();
            path = "<span class=\"classPathEntry\">" + tmp.getLabel() + "</span>" + path;
        }
        _bldr.append(path);
    }
}
