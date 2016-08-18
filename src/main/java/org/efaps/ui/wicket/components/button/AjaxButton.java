/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */


package org.efaps.ui.wicket.components.button;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.resources.EFapsContentReference;


/**
 * TODO comment!.
 *
 * @author The eFaps Team
 * @param <T> the generic type
 */
@SuppressWarnings("checkstyle:abstractclassname")
public abstract class AjaxButton<T>
    extends GenericPanel<T>
{

    /**
     *
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new ajax button.
     *
     * @param _wicketId wicketid for this component
     */
    public AjaxButton(final String _wicketId)
    {
        this(_wicketId, (String) null);
    }

    /**
     * Instantiates a new ajax button.
     *
     * @param _wicketId     WicketId for this component
     * @param _label        label of the button
     */
    public AjaxButton(final String _wicketId,
                      final String _label)
    {
        this(_wicketId, null, _label);
    }

    /**
     * Instantiates a new ajax button.
     *
     * @param _wicketId     WicketId for this component
     * @param _reference    reference to an icon
     */
    public AjaxButton(final String _wicketId,
                      final EFapsContentReference _reference)
    {
        this(_wicketId, _reference, null);
    }

    /**
     * Instantiates a new ajax button.
     *
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
     * Instantiates a new ajax button.
     *
     * @param _wicketId     WicketId for this component
     * @param _model        Model for this component
     */
    public AjaxButton(final String _wicketId,
                      final IModel<T> _model)
    {
        this(_wicketId, _model, null);
    }

    /**
     * Instantiates a new ajax button.
     *
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
     * Instantiates a new ajax button.
     *
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
        final ButtonLink<T> link;
        if (_model == null) {
            link = new ButtonLink<>("button");
        } else {
            link = new ButtonLink<>("button", _model);
        }
        add(link);
        link.add(new ButtonImage("icon", _reference));
        link.add(new Label("label", _label == null ? "" : _label));
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(CssHeaderItem.forUrl(ButtonStyleBehavior.CSS.getStaticContentUrl()));
    }

    /**
     * Gets the default processing.
     *
     * @return {@code true} for default processing
     */
    protected boolean getDefaultProcessing()
    {
        return true;
    }

    /**
     * Listener method invoked on the ajax request generated when
     * the user clicks the link.
     *
     * @param _target AjaxRequestTarget
     */
    public abstract void onSubmit(final AjaxRequestTarget _target);

    /**
     * Update ajax attributes.
     *
     * @param attributes the attributes
     */
    protected void updateAjaxAttributes(final AjaxRequestAttributes attributes)
    {
        // to be able to overwrite
    }


    /**
     * Underlying link.
     *
     * @param <T> the generic type
     */
    public static class ButtonLink<T>
        extends WebMarkupContainer
        implements IAjaxIndicatorAware
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new button link.
         *
         * @param _wicketId wicketid for this component
         * @param _model   model for this component
         */
        public ButtonLink(final String _wicketId,
                          final IModel<T> _model)
        {
            super(_wicketId, _model);
        }

        /**
         * Instantiates a new button link.
         *
         * @param _wicketId wicketid for this component
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
        protected void onInitialize()
        {
            super.onInitialize();

            add(new AjaxFormSubmitBehavior("click")
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(final AjaxRequestTarget _target)
                {
                    findParent(AjaxButton.class).onSubmit(_target);
                }

                @Override
                public boolean getDefaultProcessing()
                {
                    return findParent(AjaxButton.class).getDefaultProcessing();
                }

                @Override
                protected void updateAjaxAttributes(final AjaxRequestAttributes attributes)
                {
                    super.updateAjaxAttributes(attributes);
                    findParent(AjaxButton.class).updateAjaxAttributes(attributes);
                }
            });
        }
    }

    /**
     * Render the image span.
     *
     * @author The eFaps Team
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
         * Instantiates a new button image.
         *
         * @param _wicketId wicketid for this component
         */
        public ButtonImage(final String _wicketId)
        {
            this(_wicketId, null);
        }

        /**
         * Instantiates a new button image.
         *
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
         * Checks for reference.
         *
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
