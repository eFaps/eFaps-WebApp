/*
 * Copyright 2003 - 2013 The eFaps Team
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


package org.efaps.ui.wicket.components.table.filter;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.ui.wicket.components.classification.ClassificationTree;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationPanel
    extends Panel
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Classifications.
     */
    private final UIClassification uiClassification;


    /**
     * @param _wicketId         wicket id for this component
     * @param _model            model for this component
     * @param _uitableHeader    header this panel belongs to
     * @throws EFapsException on error
     */
    public ClassificationPanel(final String _wicketId,
                               final IModel<?> _model,
                               final UITableHeader _uitableHeader)
        throws EFapsException
    {
        super(_wicketId, _model);
        final UITable table = (UITable) super.getDefaultModelObject();
        this.uiClassification = new UIClassification(_uitableHeader.getField(), table);
        if (!this.uiClassification.isInitialized()) {
            this.uiClassification.execute(table.getInstance());
        }
        final ClassificationTree tree = new ClassificationTree("tree", Model.of(this.uiClassification));
        this.add(tree);
    }

    /**
     * Getter method for the instance variable {@link #uiClassification}.
     *
     * @return value of instance variable {@link #uiClassification}
     */
    public UIClassification getUiClassification()
    {
        return this.uiClassification;
    }
}
