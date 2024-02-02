/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.components.button;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;


/**
 * @author The eFaps Team
 * @param <T> the generic type
 */
@SuppressWarnings("checkstyle:abstractclassname")
public abstract class AjaxButton<T>
    extends GenericPanel<T>
{

    /**
     * Reference to an icon in the eFaps Database.
     */
    public enum ICON {
        /** accept.png. */
        ACCEPT("accept.png"),
        /** add.png. */
        ADD("add.png"),
        /** cancel.png. */
        CANCEL("cancel.png"),
        /** delete.png. */
        DELETE("delete.png"),
        /** next.png. */
        NEXT("next.png"),
        /** previous. */
        PREVIOUS("previous.png"),
        /** config. */
        CONFIG("config.png");

        /**
         * reference.
         */
        private final EFapsContentReference reference;

        /**
         * @param _image image
         */
        ICON(final String _image)
        {
            this.reference = new EFapsContentReference(AjaxButton.class, _image);
        }

        /**
         * Getter method for the instance variable {@link #reference}.
         *
         * @return value of instance variable {@link #reference}
         */
        public EFapsContentReference getReference()
        {
            return this.reference;
        }
    }

    /**
     *
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /** The submit. */
    private boolean submit = true;

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
        _response.render(AbstractEFapsHeaderItem.forCss(ButtonStyleBehavior.CSS));
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
    public abstract void onRequest(AjaxRequestTarget _target);

    /**
     * Listener method invoked on the ajax request generated when
     * the user clicks the link.
     *
     * @param _target AjaxRequestTarget
     */
    public void onError(final AjaxRequestTarget _target)
    {
        // to be used by implementations
    }

    /**
     * Update ajax attributes.
     *
     * @param _attributes the attributes
     */
    protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
    {
        // to be able to overwrite
    }

    /**
     * Gets the form.
     *
     * @return the form
     */
    public Form<?> getForm()
    {
        return null;
    }

    /**
     * Checks if is submit.
     *
     * @return true, if is submit
     */
    protected boolean isSubmit()
    {
        return this.submit;
    }

    /**
     * Setter method for instance variable {@link #submit}.
     *
     * @param _submit value for instance variable {@link #submit}
     * @return the ajax button< t>
     */
    public AjaxButton<T> setSubmit(final boolean _submit)
    {
        this.submit = _submit;
        return this;
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
            final AjaxButton<?> pBtn = findParent(AjaxButton.class);
            if (pBtn.isSubmit()) {
                add(new AjaxFormSubmitBehavior("click")
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onSubmit(final AjaxRequestTarget _target)
                    {
                        pBtn.onRequest(_target);
                    }

                    @Override
                    protected void onError(final AjaxRequestTarget _target)
                    {
                        pBtn.onError(_target);
                    }

                    @Override
                    public boolean getDefaultProcessing()
                    {
                        return pBtn.getDefaultProcessing();
                    }

                    @Override
                    protected void updateAjaxAttributes(final AjaxRequestAttributes _attributes)
                    {
                        super.updateAjaxAttributes(_attributes);
                        pBtn.updateAjaxAttributes(_attributes);
                    }

                    @Override
                    protected Form<?> findForm()
                    {
                        final Form<?> ret;
                        if (pBtn.getForm() != null) {
                            ret = pBtn.getForm();
                        } else {
                            ret = super.findForm();
                        }
                        return ret;
                    }
                });
            } else {
                add(new AjaxEventBehavior("click") {

                    /** The Constant serialVersionUID. */
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void onEvent(final AjaxRequestTarget _target)
                    {
                        pBtn.onRequest(_target);
                    }
                });
            }
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
