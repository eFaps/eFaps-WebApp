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


package org.efaps.ui.wicket.components.button;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.resources.EFapsContentReference;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 * @param <T>
 */
//CHECKSTYLE:OFF
public abstract class AjaxButton<T>
    extends GenericPanel<T>
{
//CHECKSTYLE:ON
    /**
     *
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicketid for this component
     */
    public AjaxButton(final String _wicketId)
    {
        this(_wicketId, (String) null);
    }

    /**
     * @param _wicketId     WicketId for this component
     * @param _label        label of the button
     */
    public AjaxButton(final String _wicketId,
                      final String _label)
    {
        this(_wicketId, null, _label);
    }

    /**
     * @param _wicketId     WicketId for this component
     * @param _reference    reference to an icon
     */
    public AjaxButton(final String _wicketId,
                      final EFapsContentReference _reference)
    {
        this(_wicketId, _reference, null);
    }

    /**
     * @param _wicketId     WicketId for this component
     * @param _reference    reference to an icon
     * @param _label        label of the button
     */
    public AjaxButton(final String _wicketId,
                      final EFapsContentReference _reference,
                      final String _label)
    {
        super(_wicketId);
        initialize(null, _reference, _label);
    }

    /**
     * @param _wicketId     WicketId for this component
     * @param _model        Model for this component
     */
    public AjaxButton(final String _wicketId,
                      final IModel<T> _model)
    {
        this(_wicketId, _model, null);
    }

    /**
     * @param _wicketId     WicketId for this component
     * @param _model        Model for this component
     * @param _reference    reference to an icon
     */
    public AjaxButton(final String _wicketId,
                      final IModel<T> _model,
                      final EFapsContentReference _reference)
    {
        this(_wicketId, _model, _reference, null);
    }

    /**
     * @param _wicketId     WicketId for this component
     * @param _model        Model for this component
     * @param _reference    reference to an icon
     * @param _label        label of the button
     */
    public AjaxButton(final String _wicketId,
                      final IModel<T> _model,
                      final EFapsContentReference _reference,
                      final String _label)
    {
        super(_wicketId, _model);
        initialize(_model, _reference, _label);
    }

    /**
     * Init the component.
     * @param _model        Model for this component
     * @param _reference    refernce to an icon
     * @param _label        label of the button
     */
    protected void initialize(final IModel<T> _model,
                              final EFapsContentReference _reference,
                              final String _label)
    {
        ButtonLink<T> link;
        if (_model == null) {
            link = new ButtonLink<T>("button");
        } else {
            link = new ButtonLink<T>("button", _model);
        }
        add(link);
        link.add(new ButtonImage("icon", _reference));
        link.add(new Label("label", _label == null ? "" : _label));
    }

    /**
     * Listener method invoked on the ajax request generated when
     * the user clicks the link.
     *
     * @param _target AjaxRequestTarget
     */
    public abstract void onClick(final AjaxRequestTarget _target);

    /**
     * Underlying link.
     */
    public static class ButtonLink<T>
        extends AjaxLink<T>
        implements IAjaxIndicatorAware
    {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicketid for this component
         * @param _model   model for this component
         */
        public ButtonLink(final String _wicketId,
                          final IModel<T> _model)
        {
            super(_wicketId, _model);
        }

        /**
         * @param _wicketId wicketid for this component
         * @param _model
         */
        public ButtonLink(final String _wicketId)
        {
            super(_wicketId);
        }

        @Override
        public String getAjaxIndicatorMarkupId()
        {
            return "eFapsVeil";
        }

        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            ((AjaxButton<?>) getParent()).onClick(_target);
        }
    }

    /**
     * Render the image span.
     */
    public static class ButtonImage
        extends StaticImageComponent
    {
        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Has a reference or not.
         */
        private boolean hasReference = false;

        /**
         * @param _wicketId wicketid for this component
         */
        public ButtonImage(final String _wicketId)
        {
            this(_wicketId, null);
        }

        /**
         * @param _wicketId wicketid for this component
         * @param _reference reference to an icon that will be shown
         */
        public ButtonImage(final String _wicketId,
                           final EFapsContentReference _reference)
        {
            super(_wicketId);
            setReference(_reference);
        }

        /**
         * @return true if has reference, else false
         */
        public boolean hasReference()
        {
            return this.hasReference;
        }

        @Override
        public boolean isVisible()
        {
            return hasReference();
        }

        @Override
        public void setReference(final EFapsContentReference _reference)
        {
            if (_reference != null) {
                super.setReference(_reference);
                this.hasReference = true;
            }
        }

        @Override
        protected void onComponentTag(final ComponentTag _tag)
        {
            _tag.append("style", "background-image:url(" + super.getUrl() + ")", " ");
        }
    }

}