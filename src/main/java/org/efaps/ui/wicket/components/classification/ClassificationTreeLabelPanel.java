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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;

import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.models.objects.UIClassification;

/**
 * Panel used to render one leaf of the tree for Classifcation.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationTreeLabelPanel extends Panel
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    private final UIClassification classification;

    /**
     * @param _wicketId         wicket id for this component
     * @param _classification   classifcation of this leaf
     */
    public ClassificationTreeLabelPanel(final String _wicketId, final UIClassification _classification)
    {
        super(_wicketId);
        this.classification = _classification;
        add(new AjaxCheckBox("checkbox"));
        add(new LabelComponent("content", this.classification.getLabel()));
    }

    public class AjaxCheckBox extends WebComponent {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId         wicket id for this component
         * @param _classification   classifcation of this leaf
         */
        public AjaxCheckBox(final String _wicketId)
        {
            super(_wicketId);
            add(new AjaxCheckBoxClickBehavior());
        }

        /**
         * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
         * @param _tag  tag
         */
        @Override
        protected void onComponentTag(final ComponentTag _tag)
        {
            super.onComponentTag(_tag);
            if (ClassificationTreeLabelPanel.this.classification.isSelected()) {
                _tag.put("checked", "checked");
            }
        }
    }

    public class AjaxCheckBoxClickBehavior extends AjaxEventBehavior {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _classification
         * @param event
         */
        public AjaxCheckBoxClickBehavior()
        {
            super("onClick");
        }

        /**
         * @see org.apache.wicket.ajax.AjaxEventBehavior#onEvent(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            ClassificationTreeLabelPanel.this.classification
                            .setSelected(!ClassificationTreeLabelPanel.this.classification.isSelected());
        }
    }
}

