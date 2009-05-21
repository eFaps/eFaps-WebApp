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

package org.efaps.ui.wicket.components.classification;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;

import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.models.objects.UIClassification;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationLabelPanel extends Panel
{


    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId
     * @param object
     */
    public ClassificationLabelPanel(final String _wicketId, final UIClassification _classification)
    {
        super(_wicketId);
        add(new AjaxCheckBox("checkbox",_classification));
        add(new LabelComponent("content", _classification.getLabel()));
    }

    public class AjaxCheckBox extends WebComponent {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId
         * @param _classification
         */
        public AjaxCheckBox(final String _wicketId, final UIClassification _classification)
        {
            super(_wicketId);
            add(new AjaxCheckBoxClickBehavior(_classification));
        }
    }

    public class AjaxCheckBoxClickBehavior extends AjaxEventBehavior {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private final UIClassification classification;

        /**
         * @param _classification
         * @param event
         */
        public AjaxCheckBoxClickBehavior(final UIClassification _classification)
        {
            super("onClick");
            this.classification = _classification;
        }

        /**
         * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            this.classification.setSelected(!this.classification.isSelected());
        }
    }
}

