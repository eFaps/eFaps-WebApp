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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.ui.wicket.components.form.field;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.field.AbstractUIField;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class FieldPanel
    extends Panel
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(FieldPanel.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

   /**
     * @param _wicketId wicketId for this component
     * @param _model    model for this component
     */
    public FieldPanel(final String _wicketId,
                      final IModel<AbstractUIField> _model)
    {
        super(_wicketId, _model);

        final AbstractUIField uiField = _model.getObject();
        try {
            final WebMarkupContainer labelField = new WebMarkupContainer("labelField");
            final WebMarkupContainer nonLabelField = new WebMarkupContainer("nonLabelField");
            add(labelField);
            add(nonLabelField);
            if (uiField.getFieldConfiguration().isHideLabel()) {
                nonLabelField.add(uiField.getComponent("field"));
                labelField.setVisible(false);
            } else {
                labelField.add(uiField.getComponent("field"));
                nonLabelField.setVisible(false);
            }
        } catch (final EFapsException e) {
            FieldPanel.LOG.error("Catched error during population of a TaskPage", e);
        }
    }
}
