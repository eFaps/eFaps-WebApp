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

package org.efaps.ui.wicket.pages.classification;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.classification.ClassificationPathPanel;
import org.efaps.ui.wicket.components.classification.ClassificationTree;
import org.efaps.ui.wicket.models.objects.UIClassification;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationPage
    extends WebPage
{

    /**
     * @param _model model for this page
     * @param _panel    classification panel used for the tree
     */
    public ClassificationPage(final IModel<UIClassification> _model,
                              final ClassificationPathPanel _panel)
    {
        super(_model);
        final ClassificationTree tree = new ClassificationTree("tree", _model, _panel);
        this.add(tree);
    }

}
