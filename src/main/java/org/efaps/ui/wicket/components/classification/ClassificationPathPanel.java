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

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.form.valuepicker.ValuePicker;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.pages.classification.ClassificationPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationPathPanel extends Panel
{
    public static final EFapsContentReference ICON = new EFapsContentReference(ValuePicker.class, "valuepicker.png");

    /** Needed for serialization. */
    private static final long serialVersionUID = 1L;

    private final ModalWindowContainer modal;

    /**
     * @param _wicketId wicket id of this component
     * @param _model model for this component
     */
    public ClassificationPathPanel(final String _wicketId, final IModel<UIClassification> _model)
    {
        super(_wicketId, _model);
        final UIClassification classification = _model.getObject();
        this.add(new Label("breadCrumb", classification.getClassificationName()));
        this.add(new ClassTreeOpener("button", _model));

        this.modal = new ModalWindowContainer("modal");
        add(this.modal);
        this.modal.reset();
        this.modal.setPageMapName("modal-2");
        // it must be used a Page Creator, because only a modal window using a
        // page creator can be moved over the whole sreen
        this.modal.setPageCreator(new ModalWindowContainer.PageCreator()
        {
            /** Needed for serialization */
            private static final long serialVersionUID = 1L;

            public Page createPage()
            {
                return new ClassificationPage(_model, ClassificationPathPanel.this.modal, ClassificationPathPanel.this.getPage());
            }
        });
    }

    public class ClassTreeOpener extends WebComponent
    {

        /**
         * @param id
         */
        public ClassTreeOpener(final String _wicketId, final IModel<UIClassification> _model)
        {
            super(_wicketId, _model);
            this.add(new AjaxOpenClassTreeBehavior());
        }

        /**
         * @see org.apache.wicket.Component#onComponentTagBody(org.apache.wicket.markup.MarkupStream,
         *      org.apache.wicket.markup.ComponentTag)
         * @param _markupStream
         * @param _openTag
         */
        @Override
        protected void onComponentTagBody(final MarkupStream _markupStream, final ComponentTag _openTag)
        {
            super.onComponentTagBody(_markupStream, _openTag);
            final StringBuilder html = new StringBuilder();
            html.append("<img alt=\"\" src=\"").append(ICON.getImageUrl()).append("\"/>");
            replaceComponentTagBody(_markupStream, _openTag, html);
        }

    }

    public class AjaxOpenClassTreeBehavior extends AjaxEventBehavior
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         */
        public AjaxOpenClassTreeBehavior()
        {
            super("onclick");
        }

        /**
         * This Method returns the JavaScript which is executed by the
         * JSCooKMenu.
         *
         * @return String with the JavaScript
         */
        public String getJavaScript()
        {
            final String script = super.getCallbackScript().toString();
            return "javascript:" + script.replace("'", "\"");
        }

        /**
         * Show the modal window.
         *
         * @param _target AjaxRequestTarget
         */
        @Override
        protected void onEvent(final AjaxRequestTarget _target)
        {
            ClassificationPathPanel.this.modal.show(_target);
        }

        /**
         * Method must be overwritten, otherwise the default would break the
         * execution of the JavaScript.
         *
         * @return null
         */
        @Override
        protected CharSequence getPreconditionScript()
        {
            return null;
        }
    }
}
